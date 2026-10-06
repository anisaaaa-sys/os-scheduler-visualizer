package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.ExampleDataset;
import no.anisa.scheduling.model.GanttSlice;
import no.anisa.scheduling.model.SchedulingResult;
import no.anisa.scheduling.model.TraceEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static no.anisa.scheduling.algorithm.TraceAssertions.assertCompleted;
import static no.anisa.scheduling.algorithm.TraceAssertions.assertEvents;
import static no.anisa.scheduling.algorithm.TraceAssertions.assertState;
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

    @Test
    void traceRecordsQueueOrderedByRemainingTime() {
        SchedulingResult result = new SrtfScheduler().schedule(ExampleDataset.classicFiveProcesses());

        assertEquals(25, result.trace().size());

        assertState(result, 0, "P1:5");
        assertEvents(result, 0, TraceEvent.arrival("P1"));

        // P2 (3) arrives with less remaining than P1 (4) and preempts it
        assertState(result, 1, "P2:3", "P1:4");
        assertEvents(result, 1, TraceEvent.arrival("P2"), TraceEvent.preemption("P2", "P1"));

        assertState(result, 3, "P2:1", "P1:4", "P4:6", "P3:8");
        assertEvents(result, 3, TraceEvent.arrival("P4"));

        // P2 finishes as P5 arrives; P5 is shortest, and nothing is preempted
        assertState(result, 4, "P5:2", "P1:4", "P4:6", "P3:8");
        assertEvents(result, 4, TraceEvent.completion("P2"), TraceEvent.arrival("P5"));
        assertCompleted(result, 4, "P2");

        assertState(result, 6, "P1:4", "P4:6", "P3:8");
        assertState(result, 10, "P4:6", "P3:8");
        assertCompleted(result, 10, "P2", "P5", "P1");

        assertState(result, 24, null);
        assertEvents(result, 24, TraceEvent.completion("P3"));
        assertCompleted(result, 24, "P2", "P5", "P1", "P4", "P3");
    }
}
