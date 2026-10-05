package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.GanttSlice;
import no.anisa.scheduling.model.ProcessMetrics;
import no.anisa.scheduling.model.ProcessRecord;
import no.anisa.scheduling.model.SchedulingResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SrtfScheduler implements CpuScheduler {

    @Override
    public SchedulingResult schedule(List<ProcessRecord> processes) {
        Map<String, Integer> remainingTime = new HashMap<>();
        for (ProcessRecord p : processes) {
            remainingTime.put(p.getId(), p.getBurstTime());
        }

        int n = processes.size();
        int completed = 0;
        int time = processes.stream().mapToInt(ProcessRecord::getArrivalTime).min().orElse(0);

        List<GanttSlice> gantt = new ArrayList<>();
        Map<String, Integer> completionTime = new HashMap<>();
        int contextSwitches = 0;
        String previousProcessId = null;
        String currentSliceProcessId = null;
        int currentSliceStart = time;

        while (completed < n) {
            final int currentTime = time;
            String chosen = processes.stream()
                    .filter(p -> p.getArrivalTime() <= currentTime && remainingTime.get(p.getId()) > 0)
                    .min(Comparator.comparingInt((ProcessRecord p) -> remainingTime.get(p.getId()))
                            .thenComparingInt(ProcessRecord::getArrivalTime)
                            .thenComparing(ProcessRecord::getId))
                    .map(ProcessRecord::getId)
                    .orElse(null);

            if (chosen == null) {
                if (currentSliceProcessId != null) {
                    gantt.add(new GanttSlice(currentSliceProcessId, currentSliceStart, time));
                    currentSliceProcessId = null;
                }
                int nextArrival = processes.stream()
                        .filter(p -> remainingTime.get(p.getId()) > 0)
                        .mapToInt(ProcessRecord::getArrivalTime)
                        .filter(a -> a > currentTime)
                        .min().orElse(currentTime + 1);
                gantt.add(new GanttSlice(null, time, nextArrival));
                time = nextArrival;
                currentSliceStart = time;
                continue;
            }

            if (!chosen.equals(currentSliceProcessId)) {
                if (currentSliceProcessId != null) {
                    gantt.add(new GanttSlice(currentSliceProcessId, currentSliceStart, time));
                }
                if (previousProcessId != null && !previousProcessId.equals(chosen)) {
                    contextSwitches++;
                }
                currentSliceProcessId = chosen;
                currentSliceStart = time;
            }

            remainingTime.put(chosen, remainingTime.get(chosen) - 1);
            time++;
            previousProcessId = chosen;

            if (remainingTime.get(chosen) == 0) {
                completionTime.put(chosen, time);
                completed++;
            }
        }

        if (currentSliceProcessId != null) {
            gantt.add(new GanttSlice(currentSliceProcessId, currentSliceStart, time));
        }

        List<ProcessMetrics> metrics = new ArrayList<>();
        for (ProcessRecord p : processes) {
            int completion = completionTime.get(p.getId());
            int turnaround = completion - p.getArrivalTime();
            int waiting = turnaround - p.getBurstTime();
            metrics.add(new ProcessMetrics(p.getId(), p.getArrivalTime(), p.getBurstTime(), completion, turnaround, waiting));
        }

        return SchedulingResultFactory.build(gantt, metrics, contextSwitches);
    }
}
