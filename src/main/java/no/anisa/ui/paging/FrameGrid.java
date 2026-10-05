package no.anisa.ui.paging;

import com.vaadin.flow.component.html.Div;

import no.anisa.paging.model.PageReplacementResult;
import no.anisa.paging.model.PageStep;

public class FrameGrid extends Div {

    private static final String CELL_WIDTH = "2.5rem";

    public FrameGrid() {
        addClassName("frame-grid");
        getStyle().set("width", "100%").set("overflow-x", "auto");
    }

    public void setResult(PageReplacementResult result) {
        removeAll();
        if (result.steps().isEmpty()) {
            return;
        }

        Div grid = new Div();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "auto repeat(" + result.steps().size() + ", " + CELL_WIDTH + ")")
                .set("gap", "2px")
                .set("width", "max-content")
                .set("font-size", "var(--lumo-font-size-s)");

        grid.add(rowLabel("Reference"));
        for (PageStep step : result.steps()) {
            Div header = cell(String.valueOf(step.page()));
            header.getStyle().set("font-weight", "600");
            grid.add(header);
        }

        for (int frame = 0; frame < result.frameCount(); frame++) {
            grid.add(rowLabel("Frame " + (frame + 1)));
            for (PageStep step : result.steps()) {
                Integer page = step.frames().get(frame);
                Div cell = cell(page == null ? "" : String.valueOf(page));
                cell.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
                if (frame == step.changedFrame()) {
                    highlight(cell, step.fault());
                }
                grid.add(cell);
            }
        }

        grid.add(rowLabel("Result"));
        for (PageStep step : result.steps()) {
            Div cell = cell(step.fault() ? "F" : "H");
            highlight(cell, step.fault());
            cell.getElement().setAttribute("title", step.evictedPage() == null
                    ? (step.fault() ? "Page fault" : "Hit")
                    : "Page fault, evicted " + step.evictedPage());
            grid.add(cell);
        }

        add(grid);
    }

    private static void highlight(Div cell, boolean fault) {
        cell.getStyle()
                .set("background", fault ? "var(--lumo-error-color-10pct)" : "var(--lumo-success-color-10pct)")
                .set("color", fault ? "var(--lumo-error-text-color)" : "var(--lumo-success-text-color)")
                .set("font-weight", "600");
    }

    private static Div rowLabel(String text) {
        Div label = new Div(text);
        label.getStyle()
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("white-space", "nowrap");
        return label;
    }

    private static Div cell(String text) {
        Div cell = new Div(text);
        cell.getStyle()
                .set("box-sizing", "border-box")
                .set("padding", "var(--lumo-space-xs) 0")
                .set("text-align", "center")
                .set("border-radius", "var(--lumo-border-radius-s)");
        return cell;
    }
}
