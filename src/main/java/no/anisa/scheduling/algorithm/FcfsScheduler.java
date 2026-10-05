package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.GanttSlice;
import no.anisa.scheduling.model.ProcessMetrics;
import no.anisa.scheduling.model.ProcessRecord;
import no.anisa.scheduling.model.SchedulingResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FcfsScheduler implements CpuScheduler {

    @Override
    public SchedulingResult schedule(List<ProcessRecord> processes) {
        List<ProcessRecord> order = processes.stream()
                .sorted(Comparator.comparingInt(ProcessRecord::getArrivalTime).thenComparing(ProcessRecord::getId))
                .toList();

        List<GanttSlice> gantt = new ArrayList<>();
        List<ProcessMetrics> metrics = new ArrayList<>();
        int time = 0;
        int contextSwitches = 0;
        String previousProcessId = null;

        for (ProcessRecord process : order) {
            if (time < process.getArrivalTime()) {
                gantt.add(new GanttSlice(null, time, process.getArrivalTime()));
                time = process.getArrivalTime();
            }
            if (previousProcessId != null && !previousProcessId.equals(process.getId())) {
                contextSwitches++;
            }

            int start = time;
            int end = start + process.getBurstTime();
            gantt.add(new GanttSlice(process.getId(), start, end));
            time = end;
            previousProcessId = process.getId();

            int turnaround = end - process.getArrivalTime();
            int waiting = turnaround - process.getBurstTime();
            metrics.add(new ProcessMetrics(process.getId(), process.getArrivalTime(), process.getBurstTime(), end, turnaround, waiting));
        }

        return SchedulingResultFactory.build(gantt, metrics, contextSwitches);
    }
}
