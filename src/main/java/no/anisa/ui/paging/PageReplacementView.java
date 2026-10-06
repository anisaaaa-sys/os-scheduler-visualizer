package no.anisa.ui.paging;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import no.anisa.paging.algorithm.PageReplacementAlgorithmType;
import no.anisa.paging.algorithm.PageReplacementFactory;
import no.anisa.paging.model.PageReplacementResult;
import no.anisa.paging.model.ReferenceString;
import no.anisa.ui.MainLayout;

import java.util.Arrays;
import java.util.List;

@Route(value = "page-replacement", layout = MainLayout.class)
@PageTitle("Page Replacement | OS Scheduler Visualizer")
public class PageReplacementView extends VerticalLayout {

    private static final int MIN_FRAMES = 1;
    private static final int MAX_FRAMES = 7;

    private final TextField referenceField = new TextField("Reference string");
    private final IntegerField framesField = new IntegerField("Frames");
    private final ComboBox<PageReplacementAlgorithmType> algorithmSelect = new ComboBox<>("Algorithm");

    private final FrameGrid frameGrid = new FrameGrid();
    private final Span summaryLabel = new Span();

    private final H3 comparisonHeading = new H3("Comparison");
    private final Grid<ComparisonRow> comparisonGrid = new Grid<>(ComparisonRow.class, false);

    public PageReplacementView() {
        setSizeFull();
        setPadding(true);

        add(new H2("Page Replacement Simulation"));
        add(buildControls());

        add(new H3("Frames"));
        add(frameGrid, summaryLabel);

        add(comparisonHeading, buildComparisonGrid());
        comparisonHeading.setVisible(false);
        comparisonGrid.setVisible(false);
    }

    private Component buildControls() {
        referenceField.setValue(ReferenceString.EXAMPLE);
        referenceField.setHelperText("Page numbers separated by spaces or commas");
        referenceField.setWidth("24rem");

        framesField.setValue(3);
        framesField.setMin(MIN_FRAMES);
        framesField.setMax(MAX_FRAMES);
        framesField.setStepButtonsVisible(true);

        algorithmSelect.setItems(PageReplacementAlgorithmType.values());
        algorithmSelect.setItemLabelGenerator(PageReplacementAlgorithmType::getLabel);
        algorithmSelect.setValue(PageReplacementAlgorithmType.FIFO);

        Button runButton = new Button("Run", _ -> runSimulation());
        runButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button compareButton = new Button("Compare", _ -> compareAlgorithms());

        HorizontalLayout controls = new HorizontalLayout(referenceField, framesField, algorithmSelect, runButton, compareButton);
        controls.setAlignItems(FlexComponent.Alignment.BASELINE);
        controls.setWrap(true);
        return controls;
    }

    private Component buildComparisonGrid() {
        comparisonGrid.addColumn(row -> row.type().getLabel()).setHeader("Algorithm");
        comparisonGrid.addColumn(row -> row.result().pageFaults()).setHeader("Page faults");
        comparisonGrid.addColumn(row -> row.result().hits()).setHeader("Hits");
        comparisonGrid.addColumn(row -> formatPercent(row.result().hitRatio())).setHeader("Hit ratio");
        comparisonGrid.setAllRowsVisible(true);
        comparisonGrid.setWidthFull();
        return comparisonGrid;
    }

    private void runSimulation() {
        Input input = readInput();
        if (input == null) {
            return;
        }

        PageReplacementResult result = PageReplacementFactory.run(algorithmSelect.getValue(), input.references(), input.frames());

        frameGrid.setResult(result);
        summaryLabel.setText(String.format("Page faults: %d | Hits: %d | Hit ratio: %s",
                result.pageFaults(), result.hits(), formatPercent(result.hitRatio())));
    }

    private void compareAlgorithms() {
        Input input = readInput();
        if (input == null) {
            return;
        }

        List<ComparisonRow> rows = Arrays.stream(PageReplacementAlgorithmType.values())
                .map(type -> new ComparisonRow(type, PageReplacementFactory.run(type, input.references(), input.frames())))
                .toList();

        // Highlight every algorithm that ties for the fewest faults.
        int fewestFaults = rows.stream().mapToInt(row -> row.result().pageFaults()).min().orElse(0);
        comparisonGrid.setPartNameGenerator(row -> row.result().pageFaults() == fewestFaults ? "fewest-faults" : null);
        comparisonGrid.setItems(rows);
        comparisonHeading.setVisible(true);
        comparisonGrid.setVisible(true);
    }

    /** Validates the form, showing an error notification and returning {@code null} if it is invalid. */
    private Input readInput() {
        Integer frames = framesField.getValue();
        if (frames == null || frames < MIN_FRAMES || frames > MAX_FRAMES) {
            showError("Frames must be between " + MIN_FRAMES + " and " + MAX_FRAMES);
            return null;
        }
        if (algorithmSelect.getValue() == null) {
            algorithmSelect.setValue(PageReplacementAlgorithmType.FIFO);
        }
        try {
            return new Input(ReferenceString.parse(referenceField.getValue()), frames);
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            return null;
        }
    }

    private static void showError(String message) {
        Notification.show(message).addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private static String formatPercent(double ratio) {
        return String.format("%.1f%%", ratio * 100);
    }

    private record Input(List<Integer> references, int frames) {
    }

    private record ComparisonRow(PageReplacementAlgorithmType type, PageReplacementResult result) {
    }
}
