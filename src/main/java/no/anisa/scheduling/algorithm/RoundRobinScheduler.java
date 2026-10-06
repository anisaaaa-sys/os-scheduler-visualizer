package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.GanttSlice;
import no.anisa.scheduling.model.ProcessMetrics;
import no.anisa.scheduling.model.ProcessRecord;
import no.anisa.scheduling.model.SchedulingResult;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class RoundRobinScheduler implements CpuScheduler {

    private final int timeQuantum;

    public RoundRobinScheduler(int timeQuantum) {
        if (timeQuantum <= 0) {
            throw new IllegalArgumentException("Time quantum must be positive");
        }
        this.timeQuantum = timeQuantum;
    }

    @Override
    public SchedulingResult schedule(List<ProcessRecord> processes) {
        List<ProcessRecord> arrivalOrder = processes.stream()
                .sorted(Comparator.comparingInt(ProcessRecord::getArrivalTime).thenComparing(ProcessRecord::getId))
                .toList();

        // Keyed by record identity, not ID, so processes that share an ID are still tracked separately.
        Map<ProcessRecord, Integer> remainingTime = new IdentityHashMap<>();
        for (ProcessRecord p : arrivalOrder) {
            remainingTime.put(p, p.getBurstTime());
        }

        Deque<ProcessRecord> queue = new ArrayDeque<>();
        List<GanttSlice> gantt = new ArrayList<>();
        Map<ProcessRecord, Integer> completionTime = new IdentityHashMap<>();
        int contextSwitches = 0;
        ProcessRecord previous = null;

        int n = arrivalOrder.size();
        int time = n == 0 ? 0 : arrivalOrder.get(0).getArrivalTime();
        int nextToArriveIndex = 0;

        while (nextToArriveIndex < n && arrivalOrder.get(nextToArriveIndex).getArrivalTime() <= time) {
            queue.add(arrivalOrder.get(nextToArriveIndex));
            nextToArriveIndex++;
        }

        while (!queue.isEmpty()) {
            ProcessRecord current = queue.poll();

            if (previous != null && previous != current) {
                contextSwitches++;
            }

            int runFor = Math.min(timeQuantum, remainingTime.get(current));
            int start = time;
            time += runFor;
            gantt.add(new GanttSlice(current.getId(), start, time));
            remainingTime.put(current, remainingTime.get(current) - runFor);
            previous = current;

            while (nextToArriveIndex < n && arrivalOrder.get(nextToArriveIndex).getArrivalTime() <= time) {
                queue.add(arrivalOrder.get(nextToArriveIndex));
                nextToArriveIndex++;
            }

            if (remainingTime.get(current) > 0) {
                queue.add(current);
            } else {
                completionTime.put(current, time);
            }

            if (queue.isEmpty() && nextToArriveIndex < n) {
                int nextArrival = arrivalOrder.get(nextToArriveIndex).getArrivalTime();
                if (nextArrival > time) {
                    gantt.add(new GanttSlice(null, time, nextArrival));
                    time = nextArrival;
                }
                while (nextToArriveIndex < n && arrivalOrder.get(nextToArriveIndex).getArrivalTime() <= time) {
                    queue.add(arrivalOrder.get(nextToArriveIndex));
                    nextToArriveIndex++;
                }
            }
        }

        List<ProcessMetrics> metrics = new ArrayList<>();
        for (ProcessRecord p : arrivalOrder) {
            int completion = completionTime.get(p);
            int turnaround = completion - p.getArrivalTime();
            int waiting = turnaround - p.getBurstTime();
            metrics.add(new ProcessMetrics(p.getId(), p.getArrivalTime(), p.getBurstTime(), completion, turnaround, waiting));
        }

        return SchedulingResultFactory.build(gantt, metrics, contextSwitches);
    }
}
