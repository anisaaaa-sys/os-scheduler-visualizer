package no.anisa.ui.cpu;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

import no.anisa.scheduling.model.GanttSlice;

import java.util.List;

public class GanttChart extends Div {

    private static final String[] PALETTE = {
            "#3b82f6", "#22c55e", "#f97316", "#a855f7", "#ef4444",
            "#14b8a6", "#eab308", "#ec4899", "#6366f1", "#84cc16"
    };

    private final Div cursor = new Div();
    private final Div dimOverlay = new Div();
    private int chartStart;
    private int chartDuration;

    public GanttChart() {
        addClassName("gantt-chart");
        getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "4px").set("width", "100%");

        cursor.getStyle()
                .set("position", "absolute")
                .set("top", "-4px")
                .set("bottom", "-4px")
                .set("width", "2px")
                .set("transform", "translateX(-1px)")
                .set("background", "var(--lumo-primary-color)")
                .set("pointer-events", "none");
        dimOverlay.getStyle()
                .set("position", "absolute")
                .set("top", "0")
                .set("bottom", "0")
                .set("right", "0")
                .set("background", "var(--lumo-base-color)")
                .set("opacity", "0.6")
                .set("pointer-events", "none");
    }

    public void setSlices(List<GanttSlice> slices) {
        removeAll();
        if (slices.isEmpty()) {
            return;
        }

        Div bar = new Div();
        bar.getStyle().set("display", "flex").set("width", "100%")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("overflow", "hidden");

        Div markers = new Div();
        markers.getStyle().set("position", "relative").set("width", "100%").set("height", "1.5em")
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("color", "var(--lumo-secondary-text-color)");

        int totalDuration = slices.get(slices.size() - 1).end() - slices.get(0).start();
        chartStart = slices.get(0).start();
        chartDuration = totalDuration;

        double offset = 0;
        for (GanttSlice slice : slices) {
            double percent = totalDuration == 0 ? 100.0 / slices.size() : (100.0 * slice.duration() / totalDuration);

            Div block = new Div();
            block.setText(slice.isIdle() ? "idle" : slice.processId());
            block.getStyle()
                    .set("flex-basis", percent + "%")
                    .set("flex-grow", "0")
                    .set("box-sizing", "border-box")
                    .set("padding", "var(--lumo-space-s) 0")
                    .set("text-align", "center")
                    .set("font-weight", "600")
                    .set("color", slice.isIdle() ? "var(--lumo-secondary-text-color)" : "white")
                    .set("background", slice.isIdle() ? "var(--lumo-contrast-10pct)" : colorFor(slice.processId()))
                    .set("border-right", "1px solid var(--lumo-base-color)");
            bar.add(block);

            markers.add(marker(slice.start(), offset, offset == 0 ? "0" : "-50%"));
            offset += percent;
        }
        markers.add(marker(slices.get(slices.size() - 1).end(), 100, "-100%"));

        // The cursor and overlay sit on top of the bar, so the bar's own overflow clipping doesn't hide them.
        Div track = new Div(bar, dimOverlay, cursor);
        track.getStyle().set("position", "relative").set("width", "100%");
        add(track, markers);
        setCursor(null);
    }

    /**
     * Shows a vertical cursor at {@code time} and dims everything to its right, or hides both when
     * {@code time} is {@code null}.
     */
    public void setCursor(Integer time) {
        boolean visible = time != null && chartDuration > 0;
        cursor.setVisible(visible);
        dimOverlay.setVisible(visible);
        if (visible) {
            double percent = Math.clamp(100.0 * (time - chartStart) / chartDuration, 0, 100);
            cursor.getStyle().set("left", percent + "%");
            dimOverlay.getStyle().set("left", percent + "%");
        }
    }

    /** Places a time label at {@code leftPercent} of the chart width, shifted by {@code translateX} to align it to the boundary. */
    private Span marker(int time, double leftPercent, String translateX) {
        Span marker = new Span(String.valueOf(time));
        marker.getStyle()
                .set("position", "absolute")
                .set("left", leftPercent + "%")
                .set("transform", "translateX(" + translateX + ")")
                .set("white-space", "nowrap");
        return marker;
    }

    private String colorFor(String processId) {
        int hash = Math.abs(processId.hashCode());
        return PALETTE[hash % PALETTE.length];
    }
}
