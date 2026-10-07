package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.algorithm.AlgorithmComparison.Entry;
import no.anisa.scheduling.model.ExampleDataset;
import no.anisa.scheduling.model.ProcessRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlgorithmComparisonTest {

    @Test
    void comparesAllAlgorithmsOnTheClassicDataset() {
        AlgorithmComparison comparison = AlgorithmComparison.of(ExampleDataset.classicFiveProcesses(), 4);

        assertEquals(List.of(
                new Entry(SchedulingAlgorithmType.FCFS, 8.2, 13.0, 4),
                new Entry(SchedulingAlgorithmType.SJF, 5.6, 10.4, 4),
                new Entry(SchedulingAlgorithmType.SRTF, 5.2, 10.0, 5),
                new Entry(SchedulingAlgorithmType.ROUND_ROBIN, 10.8, 15.6, 7),
                new Entry(SchedulingAlgorithmType.PRIORITY, 7.8, 12.6, 4)
        ), rounded(comparison.entries()));

        assertEquals(List.of(SchedulingAlgorithmType.SRTF), best(comparison, comparison::hasBestWaitingTime));
        assertEquals(List.of(SchedulingAlgorithmType.SRTF), best(comparison, comparison::hasBestTurnaroundTime));
    }

    @Test
    void highlightsEveryAlgorithmThatTies() {
        AlgorithmComparison comparison = AlgorithmComparison.of(ExampleDataset.classicFiveProcesses(), 4);

        // The three non-preemptive algorithms each switch 4 times on this dataset
        assertEquals(List.of(SchedulingAlgorithmType.FCFS, SchedulingAlgorithmType.SJF, SchedulingAlgorithmType.PRIORITY),
                best(comparison, comparison::hasFewestContextSwitches));
    }

    @Test
    void allAlgorithmsTieWhenThereIsOnlyOneProcess() {
        AlgorithmComparison comparison = AlgorithmComparison.of(List.of(new ProcessRecord("P1", 0, 3, 1)), 2);

        List<SchedulingAlgorithmType> all = List.of(SchedulingAlgorithmType.values());
        assertEquals(all, best(comparison, comparison::hasBestWaitingTime));
        assertEquals(all, best(comparison, comparison::hasBestTurnaroundTime));
        assertEquals(all, best(comparison, comparison::hasFewestContextSwitches));
    }

    @Test
    void usesTheGivenRoundRobinQuantum() {
        AlgorithmComparison quantumTwo = AlgorithmComparison.of(ExampleDataset.classicFiveProcesses(), 2);
        AlgorithmComparison quantumFour = AlgorithmComparison.of(ExampleDataset.classicFiveProcesses(), 4);

        assertEquals(7, roundRobin(quantumFour).contextSwitches());
        // A smaller quantum means more slices, so more context switches
        assertTrue(roundRobin(quantumTwo).contextSwitches() > roundRobin(quantumFour).contextSwitches());
    }

    private static List<SchedulingAlgorithmType> best(AlgorithmComparison comparison, Predicate<Entry> isBest) {
        return comparison.entries().stream().filter(isBest).map(Entry::type).toList();
    }

    private static Entry roundRobin(AlgorithmComparison comparison) {
        return comparison.entries().stream()
                .filter(entry -> entry.type() == SchedulingAlgorithmType.ROUND_ROBIN).findFirst().orElseThrow();
    }

    /** Rounds averages to one decimal so expected values can be written as literals. */
    private static List<Entry> rounded(List<Entry> entries) {
        return entries.stream()
                .map(e -> new Entry(e.type(), Math.round(e.averageWaitingTime() * 10) / 10.0,
                        Math.round(e.averageTurnaroundTime() * 10) / 10.0, e.contextSwitches()))
                .toList();
    }
}
