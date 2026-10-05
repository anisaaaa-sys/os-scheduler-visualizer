package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.ExampleDataset;
import no.anisa.scheduling.model.GanttSlice;
import no.anisa.scheduling.model.SchedulingResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class SjfSchedulerTest {

    @Test
    void picksShortestBurstAmongArrivedProcesses() {
        SchedulingResult result = new SjfScheduler().schedule(ExampleDataset.classicFiveProcesses());

        assertIterableEquals(List.of(
                new GanttSlice("P1", 0, 5),
                new GanttSlice("P5", 5, 7),
                new GanttSlice("P2", 7, 10),
                new GanttSlice("P4", 10, 16),
                new GanttSlice("P3", 16, 24)
        ), result.ganttChart());

        Map<String, int[]> expected = Map.of(
                "P1", new int[]{5, 5, 0},
                "P2", new int[]{10, 9, 6},
                "P3", new int[]{24, 22, 14},
                "P4", new int[]{16, 13, 7},
                "P5", new int[]{7, 3, 1}
        );
        FcfsSchedulerTest.assertMetricsMatch(expected, result);

        assertEquals(5.6, result.averageWaitingTime(), 1e-9);
        assertEquals(10.4, result.averageTurnaroundTime(), 1e-9);
        assertEquals(4, result.contextSwitches());
    }
}
