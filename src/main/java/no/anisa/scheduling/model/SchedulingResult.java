package no.anisa.scheduling.model;

import java.util.List;

public record SchedulingResult(
        List<GanttSlice> ganttChart,
        List<ProcessMetrics> processMetrics,
        double averageWaitingTime,
        double averageTurnaroundTime,
        int contextSwitches
) {
}
