package no.anisa.paging.model;

import java.util.List;

public record PageReplacementResult(List<PageStep> steps, int frameCount, int pageFaults) {

    public int hits() {
        return steps.size() - pageFaults;
    }

    public double hitRatio() {
        return steps.isEmpty() ? 0 : (double) hits() / steps.size();
    }
}
