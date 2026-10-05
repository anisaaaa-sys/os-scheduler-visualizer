package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.ProcessRecord;
import no.anisa.scheduling.model.SchedulingResult;

import java.util.List;

public final class CpuSchedulerFactory {

    private CpuSchedulerFactory() {
    }

    public static CpuScheduler create(SchedulingAlgorithmType type, int roundRobinQuantum) {
        return switch (type) {
            case FCFS -> new FcfsScheduler();
            case SJF -> new SjfScheduler();
            case SRTF -> new SrtfScheduler();
            case ROUND_ROBIN -> new RoundRobinScheduler(roundRobinQuantum);
            case PRIORITY -> new PriorityScheduler();
        };
    }

    public static SchedulingResult run(SchedulingAlgorithmType type, List<ProcessRecord> processes, int roundRobinQuantum) {
        return create(type, roundRobinQuantum).schedule(processes);
    }
}
