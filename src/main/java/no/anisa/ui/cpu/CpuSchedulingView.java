package no.anisa.ui.cpu;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import no.anisa.scheduling.algorithm.CpuSchedulerFactory;
import no.anisa.scheduling.algorithm.SchedulingAlgorithmType;
import no.anisa.scheduling.model.ExampleDataset;
import no.anisa.scheduling.model.ProcessMetrics;
import no.anisa.scheduling.model.ProcessRecord;
import no.anisa.scheduling.model.SchedulingResult;
import no.anisa.ui.MainLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.ObjIntConsumer;

@Route(value = "cpu-scheduling", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("CPU Scheduling | OS Scheduler Visualizer")
public class CpuSchedulingView extends VerticalLayout {

    private final List<ProcessRecord> processes = new ArrayList<>();
    private final Grid<ProcessRecord> processGrid = new Grid<>(ProcessRecord.class, false);

    private final ComboBox<SchedulingAlgorithmType> algorithmSelect = new ComboBox<>("Algorithm");
    private final IntegerField quantumField = new IntegerField("Time quantum");

    private final TracePlayer tracePlayer = new TracePlayer();
    private final Grid<ProcessMetrics> resultsGrid = new Grid<>(ProcessMetrics.class, false);
    private final Span averagesLabel = new Span();
    private final VerticalLayout resultsSection = new VerticalLayout();

    public CpuSchedulingView() {
        setSizeFull();
        setPadding(true);

        add(new H2("CPU Scheduling Simulation"));
        add(buildToolbar());
        add(buildProcessGrid());
        add(buildControls());

        add(new H3("Gantt Chart"));
        add(tracePlayer);

        add(buildResultsSection());
        resultsSection.setVisible(false);
        tracePlayer.addEndReachedListener(() -> resultsSection.setVisible(true));

        loadExample();
    }

    private Component buildToolbar() {
        Button addButton = new Button("Add process", VaadinIcon.PLUS.create(), _ -> addBlankProcess());
        Button loadExampleButton = new Button("Load example", _ -> loadExample());

        HorizontalLayout toolbar = new HorizontalLayout(addButton, loadExampleButton);
        toolbar.setSpacing(true);
        return toolbar;
    }

    private Component buildProcessGrid() {
        processGrid.setAllRowsVisible(true);
        processGrid.setWidthFull();

        Binder<ProcessRecord> binder = new Binder<>(ProcessRecord.class);
        Editor<ProcessRecord> editor = processGrid.getEditor();
        editor.setBinder(binder);
        binder.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                tracePlayer.stop();
            }
        });

        Grid.Column<ProcessRecord> idColumn = processGrid.addColumn(ProcessRecord::getId).setHeader("ID").setAutoWidth(true);
        TextField idField = new TextField();
        idField.setWidthFull();
        binder.forField(idField).asRequired("Required").bind(ProcessRecord::getId, ProcessRecord::setId);
        idColumn.setEditorComponent(idField);

        Grid.Column<ProcessRecord> arrivalColumn = processGrid.addColumn(ProcessRecord::getArrivalTime).setHeader("Arrival time").setAutoWidth(true);
        IntegerField arrivalField = new IntegerField();
        arrivalField.setWidthFull();
        arrivalField.setMin(0);
        binder.forField(arrivalField).asRequired("Required").bind(ProcessRecord::getArrivalTime, intSetter(ProcessRecord::setArrivalTime));
        arrivalColumn.setEditorComponent(arrivalField);

        Grid.Column<ProcessRecord> burstColumn = processGrid.addColumn(ProcessRecord::getBurstTime).setHeader("Burst time").setAutoWidth(true);
        IntegerField burstField = new IntegerField();
        burstField.setWidthFull();
        burstField.setMin(1);
        binder.forField(burstField).asRequired("Required").bind(ProcessRecord::getBurstTime, intSetter(ProcessRecord::setBurstTime));
        burstColumn.setEditorComponent(burstField);

        Grid.Column<ProcessRecord> priorityColumn = processGrid.addColumn(ProcessRecord::getPriority).setHeader("Priority").setAutoWidth(true);
        IntegerField priorityField = new IntegerField();
        priorityField.setWidthFull();
        priorityField.setMin(1);
        binder.forField(priorityField).asRequired("Required").bind(ProcessRecord::getPriority, intSetter(ProcessRecord::setPriority));
        priorityColumn.setEditorComponent(priorityField);

        processGrid.addComponentColumn(process -> {
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(_ -> {
                tracePlayer.stop();
                processes.remove(process);
                processGrid.getDataProvider().refreshAll();
            });
            return deleteButton;
        }).setHeader("").setAutoWidth(true).setFlexGrow(0);

        processGrid.addItemDoubleClickListener(event -> {
            editor.editItem(event.getItem());
            idField.focus();
        });

        processGrid.setItems(processes);
        return processGrid;
    }

    private Component buildControls() {
        // Short name in the field, full name in the dropdown; typing matches either.
        algorithmSelect.setItems((type, filter) -> {
            String needle = filter.toLowerCase(Locale.ROOT);
            return type.getShortLabel().toLowerCase(Locale.ROOT).contains(needle)
                    || type.getLabel().toLowerCase(Locale.ROOT).contains(needle);
        }, SchedulingAlgorithmType.values());
        algorithmSelect.setItemLabelGenerator(SchedulingAlgorithmType::getShortLabel);
        algorithmSelect.setRenderer(new TextRenderer<>(SchedulingAlgorithmType::getLabel));
        algorithmSelect.getStyle().set("--vaadin-combo-box-overlay-width", "26rem");
        algorithmSelect.setValue(SchedulingAlgorithmType.FCFS);

        quantumField.setValue(4);
        quantumField.setMin(1);
        quantumField.setVisible(false);
        algorithmSelect.addValueChangeListener(event -> {
            quantumField.setVisible(event.getValue() == SchedulingAlgorithmType.ROUND_ROBIN);
            tracePlayer.stop();
        });
        quantumField.addValueChangeListener(_ -> tracePlayer.stop());

        Button runButton = new Button("Run", _ -> runSimulation());
        runButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout controls = new HorizontalLayout(algorithmSelect, quantumField, runButton);
        controls.setAlignItems(FlexComponent.Alignment.END);
        return controls;
    }

    private Component buildResultsSection() {
        resultsGrid.addColumn(ProcessMetrics::processId).setHeader("Process");
        resultsGrid.addColumn(ProcessMetrics::arrivalTime).setHeader("Arrival");
        resultsGrid.addColumn(ProcessMetrics::burstTime).setHeader("Burst");
        resultsGrid.addColumn(ProcessMetrics::completionTime).setHeader("Completion");
        resultsGrid.addColumn(ProcessMetrics::turnaroundTime).setHeader("Turnaround");
        resultsGrid.addColumn(ProcessMetrics::waitingTime).setHeader("Waiting");
        resultsGrid.setAllRowsVisible(true);
        resultsGrid.setWidthFull();

        resultsSection.add(new H3("Results"), resultsGrid, averagesLabel);
        resultsSection.setPadding(false);
        resultsSection.setSpacing(true);
        return resultsSection;
    }

    /**
     * Adapts a primitive {@code int} setter for an {@link IntegerField} binding. Bindings are {@code asRequired},
     * so a cleared field never reaches the bean; the null check only guards against auto-unboxing.
     */
    private static Setter<ProcessRecord, Integer> intSetter(ObjIntConsumer<ProcessRecord> setter) {
        return (process, value) -> {
            if (value != null) {
                setter.accept(process, value);
            }
        };
    }

    private void addBlankProcess() {
        tracePlayer.stop();
        int nextIndex = processes.size() + 1;
        while (hasProcessWithId("P" + nextIndex)) {
            nextIndex++;
        }
        processes.add(new ProcessRecord("P" + nextIndex, 0, 1, 1));
        processGrid.getDataProvider().refreshAll();
    }

    private void loadExample() {
        tracePlayer.stop();
        processes.clear();
        processes.addAll(ExampleDataset.classicFiveProcesses());
        processGrid.getDataProvider().refreshAll();
    }

    private void runSimulation() {
        if (processes.isEmpty()) {
            Notification.show("Add at least one process first").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        String duplicateId = findDuplicateId();
        if (duplicateId != null) {
            Notification.show("Process IDs must be unique: " + duplicateId + " is used more than once")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (algorithmSelect.getValue() == null) {
            algorithmSelect.setValue(SchedulingAlgorithmType.FCFS);
        }

        SchedulingAlgorithmType type = algorithmSelect.getValue();
        int quantum = quantumField.getValue() == null ? 1 : quantumField.getValue();

        SchedulingResult result = CpuSchedulerFactory.run(type, List.copyOf(processes), quantum);

        // Results stay hidden until playback reaches the end (or the user skips to it).
        resultsSection.setVisible(false);
        resultsGrid.setItems(result.processMetrics());
        averagesLabel.setText(String.format(
                "Average waiting time: %.2f | Average turnaround time: %.2f | Context switches: %d",
                result.averageWaitingTime(), result.averageTurnaroundTime(), result.contextSwitches()));
        tracePlayer.play(result);
    }

    private boolean hasProcessWithId(String id) {
        return processes.stream().anyMatch(process -> id.equals(process.getId()));
    }

    private String findDuplicateId() {
        Set<String> seen = new HashSet<>();
        for (ProcessRecord process : processes) {
            if (!seen.add(process.getId())) {
                return process.getId();
            }
        }
        return null;
    }
}
