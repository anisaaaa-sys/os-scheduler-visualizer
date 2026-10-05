package no.anisa.scheduling.algorithm;

public enum SchedulingAlgorithmType {

    FCFS("First Come First Served"),
    SJF("Shortest Job First (non-preemptive)"),
    SRTF("Shortest Remaining Time First (preemptive)"),
    ROUND_ROBIN("Round Robin"),
    PRIORITY("Priority (non-preemptive)");

    private final String label;

    SchedulingAlgorithmType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
