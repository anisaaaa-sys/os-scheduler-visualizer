package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.GanttSlice;
import no.anisa.scheduling.model.ProcessMetrics;
import no.anisa.scheduling.model.SchedulingResult;
import no.anisa.scheduling.model.TraceStep;

import java.util.Comparator;
import java.util.List;

final class SchedulingResultFactory {

    private SchedulingResultFactory() {
    }

    static SchedulingResult build(List<GanttSlice> gantt, List<ProcessMetrics> metrics, int contextSwitches, List<TraceStep> trace) {
        List<ProcessMetrics> sorted = metrics.stream()
                .sorted(Comparator.comparing(ProcessMetrics::processId))
                .toList();
        double averageWaitingTime = sorted.stream().mapToInt(ProcessMetrics::waitingTime).average().orElse(0);
        double averageTurnaroundTime = sorted.stream().mapToInt(ProcessMetrics::turnaroundTime).average().orElse(0);
        return new SchedulingResult(gantt, sorted, averageWaitingTime, averageTurnaroundTime, contextSwitches, trace);
    }
}
