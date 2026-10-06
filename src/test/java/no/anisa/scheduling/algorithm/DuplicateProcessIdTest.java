package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.ProcessMetrics;
import no.anisa.scheduling.model.ProcessRecord;
import no.anisa.scheduling.model.SchedulingResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/** Processes that share an ID must still be scheduled as separate processes. */
class DuplicateProcessIdTest {

    private static List<ProcessRecord> duplicates() {
        return List.of(new ProcessRecord("P1", 0, 2, 1), new ProcessRecord("P1", 0, 3, 1));
    }

    @Test
    void srtfCompletesBothProcesses() {
        SchedulingResult result = assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> new SrtfScheduler().schedule(duplicates()));

        assertEquals(List.of(2, 5), result.processMetrics().stream().map(ProcessMetrics::completionTime).sorted().toList());
        assertEquals(1, result.contextSwitches());
    }

    @Test
    void roundRobinCompletesBothProcesses() {
        SchedulingResult result = assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> new RoundRobinScheduler(2).schedule(duplicates()));

        assertEquals(List.of(2, 5), result.processMetrics().stream().map(ProcessMetrics::completionTime).sorted().toList());
        assertEquals(1, result.contextSwitches());
    }
}
