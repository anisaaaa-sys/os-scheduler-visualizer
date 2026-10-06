package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.GanttSlice;
import no.anisa.scheduling.model.ProcessMetrics;
import no.anisa.scheduling.model.ProcessRecord;
import no.anisa.scheduling.model.SchedulingResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SjfScheduler implements CpuScheduler {

    /** Dispatch order among arrived processes; also the order of the ready queue in the trace. */
    private static final Comparator<ProcessRecord> DISPATCH_ORDER = Comparator.comparingInt(ProcessRecord::getBurstTime)
            .thenComparingInt(ProcessRecord::getArrivalTime)
            .thenComparing(ProcessRecord::getId);

    @Override
    public SchedulingResult schedule(List<ProcessRecord> processes) {
        List<ProcessRecord> remaining = new ArrayList<>(processes);
        List<GanttSlice> gantt = new ArrayList<>();
        List<ProcessMetrics> metrics = new ArrayList<>();
        int time = 0;
        int contextSwitches = 0;
        String previousProcessId = null;
        TraceRecorder trace = new TraceRecorder(processes);

        while (!remaining.isEmpty()) {
            final int currentTime = time;
            List<ProcessRecord> arrived = remaining.stream()
                    .filter(p -> p.getArrivalTime() <= currentTime)
                    .toList();

            if (arrived.isEmpty()) {
                int nextArrival = remaining.stream().mapToInt(ProcessRecord::getArrivalTime).min().orElse(time);
                trace.recordIdle(time, nextArrival);
                gantt.add(new GanttSlice(null, time, nextArrival));
                time = nextArrival;
                continue;
            }

            ProcessRecord next = arrived.stream()
                    .min(DISPATCH_ORDER)
                    .orElseThrow();

            if (previousProcessId != null && !previousProcessId.equals(next.getId())) {
                contextSwitches++;
            }

            int start = time;
            int end = start + next.getBurstTime();
            gantt.add(new GanttSlice(next.getId(), start, end));
            for (int t = start; t < end; t++) {
                final int tick = t;
                List<ProcessRecord> ready = remaining.stream()
                        .filter(p -> p != next && p.getArrivalTime() <= tick)
                        .sorted(DISPATCH_ORDER)
                        .toList();
                trace.record(t, next, end - t, ready, TraceRecorder.fullBurst(), null);
            }
            trace.complete(next, end);
            time = end;
            previousProcessId = next.getId();

            int turnaround = end - next.getArrivalTime();
            int waiting = turnaround - next.getBurstTime();
            metrics.add(new ProcessMetrics(next.getId(), next.getArrivalTime(), next.getBurstTime(), end, turnaround, waiting));

            remaining.remove(next);
        }

        return SchedulingResultFactory.build(gantt, metrics, contextSwitches, trace.finish(time));
    }
}
