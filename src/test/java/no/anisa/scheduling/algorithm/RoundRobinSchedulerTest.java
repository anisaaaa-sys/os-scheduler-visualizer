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
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoundRobinSchedulerTest {

    @Test
    void rotatesThroughReadyQueueWithQuantumFour() {
        SchedulingResult result = new RoundRobinScheduler(4).schedule(ExampleDataset.classicFiveProcesses());

        assertIterableEquals(List.of(
                new GanttSlice("P1", 0, 4),
                new GanttSlice("P2", 4, 7),
                new GanttSlice("P3", 7, 11),
                new GanttSlice("P4", 11, 15),
                new GanttSlice("P5", 15, 17),
                new GanttSlice("P1", 17, 18),
                new GanttSlice("P3", 18, 22),
                new GanttSlice("P4", 22, 24)
        ), result.ganttChart());

        Map<String, int[]> expected = Map.of(
                "P1", new int[]{18, 18, 13},
                "P2", new int[]{7, 6, 3},
                "P3", new int[]{22, 20, 12},
                "P4", new int[]{24, 21, 15},
                "P5", new int[]{17, 13, 11}
        );
        FcfsSchedulerTest.assertMetricsMatch(expected, result);

        assertEquals(10.8, result.averageWaitingTime(), 1e-9);
        assertEquals(15.6, result.averageTurnaroundTime(), 1e-9);
        assertEquals(7, result.contextSwitches());
    }

    @Test
    void traceRecordsFifoQueueIncludingMidSliceArrivals() {
        SchedulingResult result = new RoundRobinScheduler(4).schedule(ExampleDataset.classicFiveProcesses());

        assertEquals(25, result.trace().size());

        // Arrivals join the queue while P1 runs its first quantum
        assertState(result, 0, "P1:5");
        assertState(result, 1, "P1:4", "P2:3");
        assertEvents(result, 1, TraceEvent.arrival("P2"));
        assertState(result, 3, "P1:2", "P2:3", "P3:8", "P4:6");

        // Quantum expires: P5 arrives at the same instant and queues ahead of the preempted P1
        assertState(result, 4, "P2:3", "P3:8", "P4:6", "P5:2", "P1:1");
        assertEvents(result, 4, TraceEvent.arrival("P5"), TraceEvent.preemption("P2", "P1"));

        // P2 finishes within its quantum: completion, but no preemption
        assertState(result, 7, "P3:8", "P4:6", "P5:2", "P1:1");
        assertEvents(result, 7, TraceEvent.completion("P2"));

        assertState(result, 11, "P4:6", "P5:2", "P1:1", "P3:4");
        assertEvents(result, 11, TraceEvent.preemption("P4", "P3"));

        assertState(result, 15, "P5:2", "P1:1", "P3:4", "P4:2");
        assertState(result, 17, "P1:1", "P3:4", "P4:2");
        assertCompleted(result, 17, "P2", "P5");

        assertState(result, 24, null);
        assertCompleted(result, 24, "P2", "P5", "P1", "P3", "P4");
    }

    @Test
    void rejectsNonPositiveQuantum() {
        assertThrows(IllegalArgumentException.class, () -> new RoundRobinScheduler(0));
    }
}
