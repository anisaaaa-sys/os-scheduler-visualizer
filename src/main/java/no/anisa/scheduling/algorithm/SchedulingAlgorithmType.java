package no.anisa.scheduling.algorithm;

public enum SchedulingAlgorithmType {

    FCFS("FCFS", "First Come First Served"),
    SJF("SJF", "Shortest Job First (non-preemptive)"),
    SRTF("SRTF", "Shortest Remaining Time First (preemptive)"),
    ROUND_ROBIN("RR", "Round Robin"),
    PRIORITY("Priority", "Priority (non-preemptive)");

    private final String shortLabel;
    private final String label;

    SchedulingAlgorithmType(String shortLabel, String label) {
        this.shortLabel = shortLabel;
        this.label = label;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public String getLabel() {
        return label;
    }
}
