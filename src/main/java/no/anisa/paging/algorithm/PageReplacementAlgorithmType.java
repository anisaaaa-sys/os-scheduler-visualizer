package no.anisa.paging.algorithm;

public enum PageReplacementAlgorithmType {

    FIFO("FIFO", "FIFO (First In, First Out)"),
    LRU("LRU", "LRU (Least Recently Used)"),
    OPTIMAL("Optimal", "Optimal");

    private final String shortLabel;
    private final String label;

    PageReplacementAlgorithmType(String shortLabel, String label) {
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
