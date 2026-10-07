package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.ProcessRecord;
import no.anisa.scheduling.model.SchedulingResult;

import java.util.Arrays;
import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * Runs every scheduling algorithm on the same process set and identifies the best (lowest) value in each
 * metric. Ties are all reported as best.
 */
public record AlgorithmComparison(List<Entry> entries) {

    /** Averages are compared with this tolerance so that equal results computed differently still tie. */
    private static final double EPSILON = 1e-9;

    public record Entry(SchedulingAlgorithmType type, double averageWaitingTime, double averageTurnaroundTime,
                        int contextSwitches) {
    }

    public static AlgorithmComparison of(List<ProcessRecord> processes, int roundRobinQuantum) {
        List<Entry> entries = Arrays.stream(SchedulingAlgorithmType.values())
                .map(type -> {
                    SchedulingResult result = CpuSchedulerFactory.run(type, processes, roundRobinQuantum);
                    return new Entry(type, result.averageWaitingTime(), result.averageTurnaroundTime(),
                            result.contextSwitches());
                })
                .toList();
        return new AlgorithmComparison(entries);
    }

    public boolean hasBestWaitingTime(Entry entry) {
        return isMinimum(entry, Entry::averageWaitingTime);
    }

    public boolean hasBestTurnaroundTime(Entry entry) {
        return isMinimum(entry, Entry::averageTurnaroundTime);
    }

    public boolean hasFewestContextSwitches(Entry entry) {
        return isMinimum(entry, Entry::contextSwitches);
    }

    private boolean isMinimum(Entry entry, ToDoubleFunction<Entry> metric) {
        double best = entries.stream().mapToDouble(metric).min().orElse(Double.NaN);
        return metric.applyAsDouble(entry) <= best + EPSILON;
    }
}
