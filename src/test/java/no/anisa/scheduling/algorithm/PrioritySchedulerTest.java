package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.ExampleDataset;
import no.anisa.scheduling.model.GanttSlice;
import no.anisa.scheduling.model.SchedulingResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class PrioritySchedulerTest {

    @Test
    void picksLowestPriorityNumberAmongArrivedProcesses() {
        SchedulingResult result = new PriorityScheduler().schedule(ExampleDataset.classicFiveProcesses());

        assertIterableEquals(List.of(
                new GanttSlice("P1", 0, 5),
                new GanttSlice("P2", 5, 8),
                new GanttSlice("P4", 8, 14),
                new GanttSlice("P3", 14, 22),
                new GanttSlice("P5", 22, 24)
        ), result.ganttChart());

        Map<String, int[]> expected = Map.of(
                "P1", new int[]{5, 5, 0},
                "P2", new int[]{8, 7, 4},
                "P3", new int[]{22, 20, 12},
                "P4", new int[]{14, 11, 5},
                "P5", new int[]{24, 20, 18}
        );
        FcfsSchedulerTest.assertMetricsMatch(expected, result);

        assertEquals(7.8, result.averageWaitingTime(), 1e-9);
        assertEquals(12.6, result.averageTurnaroundTime(), 1e-9);
        assertEquals(4, result.contextSwitches());
    }
}
