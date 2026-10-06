package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.ExampleDataset;
import no.anisa.scheduling.model.GanttSlice;
import no.anisa.scheduling.model.ProcessRecord;
import no.anisa.scheduling.model.SchedulingResult;
import no.anisa.scheduling.model.TraceEvent;
import no.anisa.scheduling.model.TraceStep;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** For every algorithm, the trace must agree with the Gantt chart it was produced alongside. */
class TraceConsistencyTest {

    /** Has an idle gap at the start and one in the middle. */
    private static final List<ProcessRecord> WITH_IDLE_GAPS = List.of(
            new ProcessRecord("A", 2, 3, 2),
            new ProcessRecord("B", 3, 1, 1),
            new ProcessRecord("C", 10, 2, 1));

    @ParameterizedTest
    @EnumSource(SchedulingAlgorithmType.class)
    void traceCoversEveryTickAndMatchesGantt(SchedulingAlgorithmType type) {
        for (List<ProcessRecord> processes : List.of(ExampleDataset.classicFiveProcesses(), WITH_IDLE_GAPS)) {
            SchedulingResult result = CpuSchedulerFactory.run(type, processes, 2);
            List<GanttSlice> gantt = result.ganttChart();
            int start = gantt.getFirst().start();
            int end = gantt.getLast().end();

            List<TraceStep> trace = result.trace();
            assertEquals(end - start + 1, trace.size(), type + " trace length");
            for (int i = 0; i < trace.size(); i++) {
                assertEquals(start + i, trace.get(i).time(), type + " tick order");
            }

            for (GanttSlice slice : gantt) {
                for (int t = slice.start(); t < slice.end(); t++) {
                    TraceStep step = trace.get(t - start);
                    String running = step.isIdle() ? null : step.running().processId();
                    assertEquals(slice.processId(), running, type + " running at t=" + t);
                }
            }

            TraceStep last = trace.getLast();
            assertTrue(last.isIdle(), type + " final step is idle");
            assertEquals(processes.size(), last.completed().size(), type + " all completed");
            long arrivals = trace.stream().flatMap(s -> s.events().stream())
                    .filter(e -> e.type() == TraceEvent.Type.ARRIVAL).count();
            assertEquals(processes.size(), arrivals, type + " one arrival per process");
        }
    }
}
