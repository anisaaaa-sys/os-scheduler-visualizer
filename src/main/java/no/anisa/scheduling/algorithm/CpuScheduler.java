package no.anisa.scheduling.algorithm;

import no.anisa.scheduling.model.ProcessRecord;
import no.anisa.scheduling.model.SchedulingResult;

import java.util.List;

public interface CpuScheduler {

    SchedulingResult schedule(List<ProcessRecord> processes);
}
