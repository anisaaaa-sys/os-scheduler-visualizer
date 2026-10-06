package no.anisa.scheduling.model;

import java.util.List;

/**
 * Scheduler state at time {@code time}, describing the tick {@code [time, time + 1)}.
 *
 * @param running    the process on the CPU during this tick, or {@code null} if the CPU is idle
 * @param readyQueue processes waiting for the CPU, in the order the scheduler will dispatch them
 * @param completed  IDs of processes finished at or before {@code time}, in completion order
 * @param events     completions, arrivals and preemptions happening at {@code time}, in that order
 */
public record TraceStep(
        int time,
        ProcessState running,
        List<ProcessState> readyQueue,
        List<String> completed,
        List<TraceEvent> events
) {

    public boolean isIdle() {
        return running == null;
    }
}
