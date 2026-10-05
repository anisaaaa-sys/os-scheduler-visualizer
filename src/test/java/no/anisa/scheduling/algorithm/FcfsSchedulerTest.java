package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.GanttSlice;
import no.anisa.scheduling.model.SchedulingResult;
import no.anisa.scheduling.model.ExampleDataset;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class FcfsSchedulerTest {

    @Test
    void schedulesInArrivalOrderWithNoIdleTime() {
        SchedulingResult result = new FcfsScheduler().schedule(ExampleDataset.classicFiveProcesses());

        assertIterableEquals(List.of(
                new GanttSlice("P1", 0, 5),
                new GanttSlice("P2", 5, 8),
                new GanttSlice("P3", 8, 16),
                new GanttSlice("P4", 16, 22),
                new GanttSlice("P5", 22, 24)
        ), result.ganttChart());

        Map<String, int[]> expected = Map.of(
                // completion, turnaround, waiting
                "P1", new int[]{5, 5, 0},
                "P2", new int[]{8, 7, 4},
                "P3", new int[]{16, 14, 6},
                "P4", new int[]{22, 19, 13},
                "P5", new int[]{24, 20, 18}
        );
        assertMetricsMatch(expected, result);

        assertEquals(8.2, result.averageWaitingTime(), 1e-9);
        assertEquals(13.0, result.averageTurnaroundTime(), 1e-9);
        assertEquals(4, result.contextSwitches());
    }

    static void assertMetricsMatch(Map<String, int[]> expected, SchedulingResult result) {
        Function<String, int[]> lookup = id -> expected.get(id);
        result.processMetrics().forEach(metrics -> {
            int[] values = lookup.apply(metrics.processId());
            assertEquals(values[0], metrics.completionTime(), metrics.processId() + " completion");
            assertEquals(values[1], metrics.turnaroundTime(), metrics.processId() + " turnaround");
            assertEquals(values[2], metrics.waitingTime(), metrics.processId() + " waiting");
        });
    }
}
