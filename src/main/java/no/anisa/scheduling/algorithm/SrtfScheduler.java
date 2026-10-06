package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.GanttSlice;
import no.anisa.scheduling.model.ProcessMetrics;
import no.anisa.scheduling.model.ProcessRecord;
import no.anisa.scheduling.model.SchedulingResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class SrtfScheduler implements CpuScheduler {

    @Override
    public SchedulingResult schedule(List<ProcessRecord> processes) {
        // Keyed by record identity, not ID, so processes that share an ID are still tracked separately.
        Map<ProcessRecord, Integer> remainingTime = new IdentityHashMap<>();
        for (ProcessRecord p : processes) {
            remainingTime.put(p, p.getBurstTime());
        }

        int n = processes.size();
        int completed = 0;
        int time = processes.stream().mapToInt(ProcessRecord::getArrivalTime).min().orElse(0);

        List<GanttSlice> gantt = new ArrayList<>();
        Map<ProcessRecord, Integer> completionTime = new IdentityHashMap<>();
        int contextSwitches = 0;
        ProcessRecord previous = null;
        ProcessRecord currentSlice = null;
        int currentSliceStart = time;
        TraceRecorder trace = new TraceRecorder(processes);
        Comparator<ProcessRecord> dispatchOrder = Comparator.comparingInt((ProcessRecord p) -> remainingTime.get(p))
                .thenComparingInt(ProcessRecord::getArrivalTime)
                .thenComparing(ProcessRecord::getId);

        while (completed < n) {
            final int currentTime = time;
            ProcessRecord chosen = processes.stream()
                    .filter(p -> p.getArrivalTime() <= currentTime && remainingTime.get(p) > 0)
                    .min(dispatchOrder)
                    .orElse(null);

            if (chosen == null) {
                if (currentSlice != null) {
                    gantt.add(new GanttSlice(currentSlice.getId(), currentSliceStart, time));
                    currentSlice = null;
                }
                int nextArrival = processes.stream()
                        .filter(p -> remainingTime.get(p) > 0)
                        .mapToInt(ProcessRecord::getArrivalTime)
                        .filter(a -> a > currentTime)
                        .min().orElse(currentTime + 1);
                trace.recordIdle(time, nextArrival);
                gantt.add(new GanttSlice(null, time, nextArrival));
                time = nextArrival;
                currentSliceStart = time;
                continue;
            }

            if (chosen != currentSlice) {
                if (currentSlice != null) {
                    gantt.add(new GanttSlice(currentSlice.getId(), currentSliceStart, time));
                }
                if (previous != null && previous != chosen) {
                    contextSwitches++;
                }
                currentSlice = chosen;
                currentSliceStart = time;
            }

            boolean preempts = previous != null && previous != chosen && remainingTime.get(previous) > 0;
            List<ProcessRecord> ready = processes.stream()
                    .filter(p -> p != chosen && p.getArrivalTime() <= currentTime && remainingTime.get(p) > 0)
                    .sorted(dispatchOrder)
                    .toList();
            trace.record(time, chosen, remainingTime.get(chosen), ready, remainingTime::get, preempts ? previous : null);

            remainingTime.put(chosen, remainingTime.get(chosen) - 1);
            time++;
            previous = chosen;

            if (remainingTime.get(chosen) == 0) {
                completionTime.put(chosen, time);
                trace.complete(chosen, time);
                completed++;
            }
        }

        if (currentSlice != null) {
            gantt.add(new GanttSlice(currentSlice.getId(), currentSliceStart, time));
        }

        List<ProcessMetrics> metrics = new ArrayList<>();
        for (ProcessRecord p : processes) {
            int completion = completionTime.get(p);
            int turnaround = completion - p.getArrivalTime();
            int waiting = turnaround - p.getBurstTime();
            metrics.add(new ProcessMetrics(p.getId(), p.getArrivalTime(), p.getBurstTime(), completion, turnaround, waiting));
        }

        return SchedulingResultFactory.build(gantt, metrics, contextSwitches, trace.finish(time));
    }
}
