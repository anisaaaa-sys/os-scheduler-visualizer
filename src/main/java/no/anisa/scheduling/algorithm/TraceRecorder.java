package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.ProcessRecord;
import no.anisa.scheduling.model.ProcessState;
import no.anisa.scheduling.model.TraceEvent;
import no.anisa.scheduling.model.TraceStep;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * Builds a per-tick trace while a scheduler runs. Schedulers call {@link #record} once for every tick in
 * order, {@link #complete} as soon as a process finishes, and {@link #finish} at the end.
 * Processes are tracked by record identity, so duplicate IDs stay separate.
 */
final class TraceRecorder {

    private final List<ProcessRecord> processes;
    // ProcessRecord does not override equals/hashCode, so this map is keyed by identity.
    private final Map<ProcessRecord, Integer> completionTime = new LinkedHashMap<>();
    private final List<TraceStep> steps = new ArrayList<>();

    TraceRecorder(List<ProcessRecord> processes) {
        this.processes = processes;
    }

    void complete(ProcessRecord process, int time) {
        completionTime.put(process, time);
    }

    /**
     * Records the tick starting at {@code time}.
     *
     * @param running          the process on the CPU, or {@code null} when idle
     * @param runningRemaining remaining burst of {@code running} at the start of the tick
     * @param ready            waiting processes in dispatch order, excluding {@code running}
     * @param remainingOf      remaining burst of each waiting process
     * @param preempted        the process {@code running} took the CPU from at this tick, if any
     */
    void record(int time, ProcessRecord running, int runningRemaining, List<ProcessRecord> ready,
                ToIntFunction<ProcessRecord> remainingOf, ProcessRecord preempted) {
        List<ProcessState> readyQueue = ready.stream()
                .map(p -> new ProcessState(p.getId(), remainingOf.applyAsInt(p)))
                .toList();
        List<TraceEvent> events = eventsAt(time);
        if (preempted != null) {
            events.add(TraceEvent.preemption(running.getId(), preempted.getId()));
        }
        steps.add(new TraceStep(time, running == null ? null : new ProcessState(running.getId(), runningRemaining),
                readyQueue, completedBy(time), List.copyOf(events)));
    }

    void recordIdle(int from, int to) {
        for (int t = from; t < to; t++) {
            record(t, null, 0, List.of(), p -> 0, null);
        }
    }

    /** Adds the final state at {@code endTime}, when every process has completed, and returns the trace. */
    List<TraceStep> finish(int endTime) {
        if (!processes.isEmpty()) {
            steps.add(new TraceStep(endTime, null, List.of(), completedBy(endTime), List.copyOf(eventsAt(endTime))));
        }
        return List.copyOf(steps);
    }

    private List<TraceEvent> eventsAt(int time) {
        List<TraceEvent> events = new ArrayList<>();
        completionTime.forEach((process, completedAt) -> {
            if (completedAt == time) {
                events.add(TraceEvent.completion(process.getId()));
            }
        });
        for (ProcessRecord process : processes) {
            if (process.getArrivalTime() == time) {
                events.add(TraceEvent.arrival(process.getId()));
            }
        }
        return events;
    }

    private List<String> completedBy(int time) {
        List<String> completed = new ArrayList<>();
        completionTime.forEach((process, completedAt) -> {
            if (completedAt <= time) {
                completed.add(process.getId());
            }
        });
        return List.copyOf(completed);
    }

    /** Remaining burst lookup for processes that have not started yet. */
    static ToIntFunction<ProcessRecord> fullBurst() {
        return ProcessRecord::getBurstTime;
    }
}
