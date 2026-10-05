package no.anisa.scheduling.model;

public record ProcessMetrics(
        String processId,
        int arrivalTime,
        int burstTime,
        int completionTime,
        int turnaroundTime,
        int waitingTime
) {
}
