package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.ProcessState;
import no.anisa.scheduling.model.SchedulingResult;
import no.anisa.scheduling.model.TraceEvent;
import no.anisa.scheduling.model.TraceStep;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class TraceAssertions {

    private TraceAssertions() {
    }

    static TraceStep stepAt(SchedulingResult result, int time) {
        return result.trace().stream().filter(s -> s.time() == time).findFirst().orElseThrow();
    }

    /** Asserts the running process and ready queue at {@code time}; entries are written "P1:4" (ID:remaining). */
    static void assertState(SchedulingResult result, int time, String running, String... readyQueue) {
        TraceStep step = stepAt(result, time);
        if (running == null) {
            assertNull(step.running(), "running at t=" + time);
        } else {
            assertEquals(state(running), step.running(), "running at t=" + time);
        }
        assertEquals(Arrays.stream(readyQueue).map(TraceAssertions::state).toList(), step.readyQueue(),
                "ready queue at t=" + time);
    }

    static void assertEvents(SchedulingResult result, int time, TraceEvent... events) {
        assertEquals(List.of(events), stepAt(result, time).events(), "events at t=" + time);
    }

    static void assertCompleted(SchedulingResult result, int time, String... processIds) {
        assertEquals(List.of(processIds), stepAt(result, time).completed(), "completed at t=" + time);
    }

    private static ProcessState state(String entry) {
        String[] parts = entry.split(":");
        return new ProcessState(parts[0], Integer.parseInt(parts[1]));
    }
}
