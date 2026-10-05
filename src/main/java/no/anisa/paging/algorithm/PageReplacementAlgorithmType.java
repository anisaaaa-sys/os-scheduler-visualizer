package no.anisa.paging.algorithm;

public enum PageReplacementAlgorithmType {

    FIFO("FIFO (First In, First Out)"),
    LRU("LRU (Least Recently Used)"),
    OPTIMAL("Optimal");

    private final String label;

    PageReplacementAlgorithmType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
