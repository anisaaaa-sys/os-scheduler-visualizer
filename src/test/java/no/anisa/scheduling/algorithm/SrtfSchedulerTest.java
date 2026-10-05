package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.ExampleDataset;
import no.anisa.scheduling.model.GanttSlice;
import no.anisa.scheduling.model.SchedulingResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class SrtfSchedulerTest {

    @Test
    void preemptsForShorterRemainingTimeOnArrival() {
        SchedulingResult result = new SrtfScheduler().schedule(ExampleDataset.classicFiveProcesses());

        assertIterableEquals(List.of(
                new GanttSlice("P1", 0, 1),
                new GanttSlice("P2", 1, 4),
                new GanttSlice("P5", 4, 6),
                new GanttSlice("P1", 6, 10),
                new GanttSlice("P4", 10, 16),
                new GanttSlice("P3", 16, 24)
        ), result.ganttChart());

        Map<String, int[]> expected = Map.of(
                "P1", new int[]{10, 10, 5},
                "P2", new int[]{4, 3, 0},
                "P3", new int[]{24, 22, 14},
                "P4", new int[]{16, 13, 7},
                "P5", new int[]{6, 2, 0}
        );
        FcfsSchedulerTest.assertMetricsMatch(expected, result);

        assertEquals(5.2, result.averageWaitingTime(), 1e-9);
        assertEquals(10.0, result.averageTurnaroundTime(), 1e-9);
        assertEquals(5, result.contextSwitches());
    }
}
