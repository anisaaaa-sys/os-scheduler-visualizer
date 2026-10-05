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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
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
import java.util.List;

@Route(value = "cpu-scheduling", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("CPU Scheduling | OS Scheduler Visualizer")
public class CpuSchedulingView extends VerticalLayout {

    private final List<ProcessRecord> processes = new ArrayList<>();
    private final Grid<ProcessRecord> processGrid = new Grid<>(ProcessRecord.class, false);

    private final ComboBox<SchedulingAlgorithmType> algorithmSelect = new ComboBox<>("Algorithm");
    private final IntegerField quantumField = new IntegerField("Time quantum");

    private final GanttChart ganttChart = new GanttChart();
    private final Grid<ProcessMetrics> resultsGrid = new Grid<>(ProcessMetrics.class, false);
    private final Span averagesLabel = new Span();

    public CpuSchedulingView() {
        setSizeFull();
        setPadding(true);

        add(new H2("CPU Scheduling Simulation"));
        add(buildToolbar());
        add(buildProcessGrid());
        add(buildControls());

        add(new H3("Gantt Chart"));
        add(ganttChart);

        add(new H3("Results"));
        add(buildResultsSection());

        loadExample();
    }

    private Component buildToolbar() {
        Button addButton = new Button("Add process", VaadinIcon.PLUS.create(), event -> addBlankProcess());
        Button loadExampleButton = new Button("Load example", event -> loadExample());

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

        Grid.Column<ProcessRecord> idColumn = processGrid.addColumn(ProcessRecord::getId).setHeader("ID").setAutoWidth(true);
        TextField idField = new TextField();
        idField.setWidthFull();
        binder.forField(idField).asRequired("Required").bind(ProcessRecord::getId, ProcessRecord::setId);
        idColumn.setEditorComponent(idField);

        Grid.Column<ProcessRecord> arrivalColumn = processGrid.addColumn(ProcessRecord::getArrivalTime).setHeader("Arrival time").setAutoWidth(true);
        IntegerField arrivalField = new IntegerField();
        arrivalField.setWidthFull();
        arrivalField.setMin(0);
        binder.forField(arrivalField).asRequired("Required").bind(ProcessRecord::getArrivalTime, ProcessRecord::setArrivalTime);
        arrivalColumn.setEditorComponent(arrivalField);

        Grid.Column<ProcessRecord> burstColumn = processGrid.addColumn(ProcessRecord::getBurstTime).setHeader("Burst time").setAutoWidth(true);
        IntegerField burstField = new IntegerField();
        burstField.setWidthFull();
        burstField.setMin(1);
        binder.forField(burstField).asRequired("Required").bind(ProcessRecord::getBurstTime, ProcessRecord::setBurstTime);
        burstColumn.setEditorComponent(burstField);

        Grid.Column<ProcessRecord> priorityColumn = processGrid.addColumn(ProcessRecord::getPriority).setHeader("Priority").setAutoWidth(true);
        IntegerField priorityField = new IntegerField();
        priorityField.setWidthFull();
        priorityField.setMin(1);
        binder.forField(priorityField).asRequired("Required").bind(ProcessRecord::getPriority, ProcessRecord::setPriority);
        priorityColumn.setEditorComponent(priorityField);

        processGrid.addComponentColumn(process -> {
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(event -> {
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
        algorithmSelect.setItems(SchedulingAlgorithmType.values());
        algorithmSelect.setItemLabelGenerator(SchedulingAlgorithmType::getLabel);
        algorithmSelect.setValue(SchedulingAlgorithmType.FCFS);

        quantumField.setValue(4);
        quantumField.setMin(1);
        quantumField.setVisible(false);
        algorithmSelect.addValueChangeListener(event ->
                quantumField.setVisible(event.getValue() == SchedulingAlgorithmType.ROUND_ROBIN));

        Button runButton = new Button("Run", event -> runSimulation());
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

        VerticalLayout layout = new VerticalLayout(resultsGrid, averagesLabel);
        layout.setPadding(false);
        layout.setSpacing(true);
        return layout;
    }

    private void addBlankProcess() {
        int nextIndex = processes.size() + 1;
        processes.add(new ProcessRecord("P" + nextIndex, 0, 1, 1));
        processGrid.getDataProvider().refreshAll();
    }

    private void loadExample() {
        processes.clear();
        processes.addAll(ExampleDataset.classicFiveProcesses());
        processGrid.getDataProvider().refreshAll();
    }

    private void runSimulation() {
        if (processes.isEmpty()) {
            Notification.show("Add at least one process first").addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        SchedulingAlgorithmType type = algorithmSelect.getValue();
        int quantum = quantumField.getValue() == null ? 1 : quantumField.getValue();

        SchedulingResult result = CpuSchedulerFactory.run(type, List.copyOf(processes), quantum);

        ganttChart.setSlices(result.ganttChart());
        resultsGrid.setItems(result.processMetrics());
        averagesLabel.setText(String.format(
                "Average waiting time: %.2f | Average turnaround time: %.2f | Context switches: %d",
                result.averageWaitingTime(), result.averageTurnaroundTime(), result.contextSwitches()));
    }
}
