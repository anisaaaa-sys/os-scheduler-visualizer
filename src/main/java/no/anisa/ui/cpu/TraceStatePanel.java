package no.anisa.ui.cpu;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import no.anisa.scheduling.model.ProcessState;
import no.anisa.scheduling.model.TraceEvent;
import no.anisa.scheduling.model.TraceStep;

import java.util.ArrayList;
import java.util.List;

/** Shows the scheduler state at one tick of the trace. */
public class TraceStatePanel extends Div {

    private final Span timeValue = new Span();
    private final Div runningValue = new Div();
    private final Div readyValue = new Div();
    private final Div completedValue = new Div();
    private final Div eventsValue = new Div();

    public TraceStatePanel() {
        addClassName("trace-state-panel");
        getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "auto 1fr")
                .set("gap", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("align-items", "center")
                .set("padding", "var(--lumo-space-m)")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("font-size", "var(--lumo-font-size-s)");

        for (Div row : List.of(runningValue, readyValue, completedValue, eventsValue)) {
            row.getStyle().set("display", "flex").set("flex-wrap", "wrap").set("gap", "var(--lumo-space-xs)");
        }
        timeValue.getStyle().set("font-weight", "600");

        add(label("Time"), timeValue,
                label("Running"), runningValue,
                label("Ready queue"), readyValue,
                label("Completed"), completedValue,
                label("Events"), eventsValue);
    }

    public void show(TraceStep step) {
        timeValue.setText("t = " + step.time());

        runningValue.removeAll();
        runningValue.add(step.isIdle() ? muted("idle") : chip(step.running()));

        readyValue.removeAll();
        if (step.readyQueue().isEmpty()) {
            readyValue.add(muted("empty"));
        }
        step.readyQueue().forEach(state -> readyValue.add(chip(state)));

        completedValue.removeAll();
        if (step.completed().isEmpty()) {
            completedValue.add(muted("none"));
        }
        step.completed().forEach(id -> completedValue.add(new Span(id)));

        eventsValue.removeAll();
        if (step.events().isEmpty()) {
            eventsValue.add(muted("none"));
        } else {
            Span summary = new Span("t=" + step.time() + ": " + String.join(", ", describe(step.events())));
            summary.getStyle()
                    .set("padding", "0 var(--lumo-space-xs)")
                    .set("border-radius", "var(--lumo-border-radius-s)")
                    .set("background", "var(--lumo-primary-color-10pct)")
                    .set("color", "var(--lumo-primary-text-color)")
                    .set("font-weight", "600");
            eventsValue.add(summary);
        }
    }

    /** Describes events, merging "P2 arrives" and "P2 preempts P1" into "P2 arrives, preempts P1". */
    static List<String> describe(List<TraceEvent> events) {
        List<String> parts = new ArrayList<>();
        for (TraceEvent event : events) {
            boolean arrivedNow = events.stream().anyMatch(e ->
                    e.type() == TraceEvent.Type.ARRIVAL && e.processId().equals(event.processId()));
            if (event.type() == TraceEvent.Type.PREEMPTION && arrivedNow) {
                int arrivalIndex = parts.indexOf(event.processId() + " arrives");
                parts.set(arrivalIndex, parts.get(arrivalIndex) + ", preempts " + event.otherProcessId());
            } else {
                parts.add(event.describe());
            }
        }
        return parts;
    }

    private static Span chip(ProcessState state) {
        Span chip = new Span(state.processId() + " (" + state.remainingBurst() + ")");
        chip.getElement().setAttribute("title", "Remaining burst: " + state.remainingBurst());
        chip.getStyle()
                .set("padding", "0 var(--lumo-space-xs)")
                .set("border-radius", "var(--lumo-border-radius-s)")
                .set("background", "var(--lumo-contrast-10pct)")
                .set("font-weight", "600");
        return chip;
    }

    private static Span label(String text) {
        Span label = new Span(text);
        label.getStyle().set("color", "var(--lumo-secondary-text-color)");
        return label;
    }

    private static Span muted(String text) {
        Span span = new Span(text);
        span.getStyle().set("color", "var(--lumo-tertiary-text-color)");
        return span;
    }
}
