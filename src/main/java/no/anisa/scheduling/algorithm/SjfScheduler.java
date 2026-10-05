package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.GanttSlice;
import no.anisa.scheduling.model.ProcessMetrics;
import no.anisa.scheduling.model.ProcessRecord;
import no.anisa.scheduling.model.SchedulingResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SjfScheduler implements CpuScheduler {

    @Override
    public SchedulingResult schedule(List<ProcessRecord> processes) {
        List<ProcessRecord> remaining = new ArrayList<>(processes);
        List<GanttSlice> gantt = new ArrayList<>();
        List<ProcessMetrics> metrics = new ArrayList<>();
        int time = 0;
        int contextSwitches = 0;
        String previousProcessId = null;

        while (!remaining.isEmpty()) {
            final int currentTime = time;
            List<ProcessRecord> arrived = remaining.stream()
                    .filter(p -> p.getArrivalTime() <= currentTime)
                    .toList();

            if (arrived.isEmpty()) {
                int nextArrival = remaining.stream().mapToInt(ProcessRecord::getArrivalTime).min().orElse(time);
                gantt.add(new GanttSlice(null, time, nextArrival));
                time = nextArrival;
                continue;
            }

            ProcessRecord next = arrived.stream()
                    .min(Comparator.comparingInt(ProcessRecord::getBurstTime)
                            .thenComparingInt(ProcessRecord::getArrivalTime)
                            .thenComparing(ProcessRecord::getId))
                    .orElseThrow();

            if (previousProcessId != null && !previousProcessId.equals(next.getId())) {
                contextSwitches++;
            }

            int start = time;
            int end = start + next.getBurstTime();
            gantt.add(new GanttSlice(next.getId(), start, end));
            time = end;
            previousProcessId = next.getId();

            int turnaround = end - next.getArrivalTime();
            int waiting = turnaround - next.getBurstTime();
            metrics.add(new ProcessMetrics(next.getId(), next.getArrivalTime(), next.getBurstTime(), end, turnaround, waiting));

            remaining.remove(next);
        }

        return SchedulingResultFactory.build(gantt, metrics, contextSwitches);
    }
}
