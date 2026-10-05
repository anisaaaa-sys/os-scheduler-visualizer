package no.anisa.paging.algorithm;

import no.anisa.paging.model.PageReplacementResult;

import java.util.List;

public final class PageReplacementFactory {

    private PageReplacementFactory() {
    }

    public static PageReplacementAlgorithm create(PageReplacementAlgorithmType type) {
        return switch (type) {
            case FIFO -> new FifoPageReplacer();
            case LRU -> new LruPageReplacer();
            case OPTIMAL -> new OptimalPageReplacer();
        };
    }

    public static PageReplacementResult run(PageReplacementAlgorithmType type, List<Integer> references, int frameCount) {
        return create(type).simulate(references, frameCount);
    }
}
