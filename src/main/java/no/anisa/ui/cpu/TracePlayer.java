package no.anisa.ui.cpu;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.shared.Registration;

import no.anisa.scheduling.model.SchedulingResult;
import no.anisa.scheduling.model.TraceStep;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Plays a scheduling trace over the Gantt chart. Playback ticks run on a background scheduler and reach
 * the browser through server push ({@code @Push} on the application shell). The scheduler only exists
 * while this component is attached; detaching stops playback and shuts it down.
 */
public class TracePlayer extends VerticalLayout {

    private static final long MILLIS_PER_TICK_AT_1X = 1000;

    private final GanttChart ganttChart = new GanttChart();
    private final TraceStatePanel statePanel = new TraceStatePanel();

    private final Button playPauseButton = new Button();
    private final Button stepBackButton = new Button(VaadinIcon.STEP_BACKWARD.create(), _ -> stepBy(-1));
    private final Button stepForwardButton = new Button(VaadinIcon.STEP_FORWARD.create(), _ -> stepBy(1));
    private final Button resetButton = new Button(VaadinIcon.ROTATE_LEFT.create(), _ -> reset());
    private final Button skipToEndButton = new Button("Skip to end", VaadinIcon.FAST_FORWARD.create(), _ -> skipToEnd());
    private final Select<Double> speedSelect = new Select<>();
    private final HorizontalLayout controls;

    private List<TraceStep> trace = List.of();
    private int position;
    private final List<SerializableRunnable> endReachedListeners = new ArrayList<>();

    // Set while attached and touched only with the session locked. Transient: none of it outlives a detach.
    private transient ScheduledExecutorService timer;
    private transient SerializableConsumer<Integer> pushTick;
    private transient ScheduledFuture<?> playback;
    // Identifies the current playback run, so ticks queued by a cancelled run are ignored.
    private int playbackRun;

    public TracePlayer() {
        setPadding(false);
        setWidthFull();

        playPauseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        playPauseButton.addClickListener(_ -> togglePlayback());
        label(stepBackButton, "Step back");
        label(stepForwardButton, "Step forward");
        label(resetButton, "Reset");

        speedSelect.setItems(0.5, 1.0, 2.0);
        speedSelect.setItemLabelGenerator(speed -> (speed == 0.5 ? "0.5" : String.valueOf(speed.intValue())) + "x");
        speedSelect.setValue(1.0);
        speedSelect.setWidth("6rem");
        speedSelect.setAriaLabel("Playback speed");
        speedSelect.addValueChangeListener(_ -> {
            if (isPlaying()) {
                startPlayback();
            }
        });

        Span speedLabel = new Span("Speed");
        speedLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
        controls = new HorizontalLayout(playPauseButton, stepBackButton, stepForwardButton, resetButton, skipToEndButton,
                speedLabel, speedSelect);
        controls.setAlignItems(FlexComponent.Alignment.CENTER);
        controls.setVisible(false);

        ganttChart.getStyle().set("flex", "1 1 24rem").set("min-width", "0");
        statePanel.getStyle().set("flex", "0 1 20rem");
        statePanel.setVisible(false);
        HorizontalLayout chartAndState = new HorizontalLayout(ganttChart, statePanel);
        chartAndState.setWidthFull();
        chartAndState.setWrap(true);
        chartAndState.setAlignItems(FlexComponent.Alignment.START);

        add(controls, chartAndState);
        updatePlayPauseButton();

        whenAttached(ui -> {
            timer = Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "trace-player");
                thread.setDaemon(true);
                return thread;
            });
            pushTick = ui.accessLater(this::advanceFromTimer, null);
            return () -> {
                stopPlayback();
                timer.shutdownNow();
                timer = null;
                pushTick = null;
            };
        });
    }

    /** Loads {@code result}, moves the cursor to the first tick and starts playing at the selected speed. */
    public void play(SchedulingResult result) {
        stopPlayback();
        ganttChart.setSlices(result.ganttChart());
        trace = result.trace();
        position = 0;
        boolean hasTrace = !trace.isEmpty();
        controls.setVisible(hasTrace);
        statePanel.setVisible(hasTrace);
        showCurrentStep();
        if (!atEnd()) {
            startPlayback();
        }
    }

    /** Pauses playback, leaving the cursor where it is. */
    public void stop() {
        stopPlayback();
    }

    /** Called whenever the cursor arrives at the final step, by playback, stepping or Skip to end. */
    public Registration addEndReachedListener(SerializableRunnable listener) {
        endReachedListeners.add(listener);
        return () -> endReachedListeners.remove(listener);
    }

    private void togglePlayback() {
        if (isPlaying()) {
            stopPlayback();
        } else {
            if (atEnd()) {
                position = 0;
                showCurrentStep();
            }
            startPlayback();
        }
    }

    private void startPlayback() {
        stopPlayback();
        if (timer == null || trace.isEmpty()) {
            return;
        }
        long period = Math.round(MILLIS_PER_TICK_AT_1X / speedSelect.getValue());
        int run = ++playbackRun;
        SerializableConsumer<Integer> tick = pushTick;
        playback = timer.scheduleAtFixedRate(() -> tick.accept(run), period, period, TimeUnit.MILLISECONDS);
        updatePlayPauseButton();
    }

    private void stopPlayback() {
        if (playback != null) {
            playback.cancel(false);
            playback = null;
        }
        updatePlayPauseButton();
    }

    private boolean isPlaying() {
        return playback != null;
    }

    /** Runs inside {@code UI.access()}, so the session is locked. */
    private void advanceFromTimer(int run) {
        if (!isPlaying() || run != playbackRun) {
            return; // queued before a pause or restart
        }
        position = Math.min(position + 1, trace.size() - 1);
        if (atEnd()) {
            stopPlayback();
        }
        showCurrentStep();
    }

    private void stepBy(int delta) {
        stopPlayback();
        position = Math.clamp(position + delta, 0, Math.max(trace.size() - 1, 0));
        showCurrentStep();
    }

    private void skipToEnd() {
        stopPlayback();
        position = Math.max(trace.size() - 1, 0);
        showCurrentStep();
    }

    private void reset() {
        stopPlayback();
        position = 0;
        showCurrentStep();
    }

    private void showCurrentStep() {
        if (trace.isEmpty()) {
            ganttChart.setCursor(null);
            return;
        }
        TraceStep step = trace.get(position);
        ganttChart.setCursor(step.time());
        statePanel.show(step);
        stepBackButton.setEnabled(position > 0);
        stepForwardButton.setEnabled(!atEnd());
        skipToEndButton.setEnabled(!atEnd());
        if (atEnd()) {
            List.copyOf(endReachedListeners).forEach(SerializableRunnable::run);
        }
    }

    private boolean atEnd() {
        return position >= trace.size() - 1;
    }

    private void updatePlayPauseButton() {
        boolean playing = isPlaying();
        playPauseButton.setIcon(playing ? VaadinIcon.PAUSE.create() : VaadinIcon.PLAY.create());
        playPauseButton.setText(playing ? "Pause" : "Play");
    }

    private static void label(Button button, String text) {
        button.setAriaLabel(text);
        button.setTooltipText(text);
    }
}
