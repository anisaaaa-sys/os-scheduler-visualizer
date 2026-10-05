package no.anisa.paging.algorithm;

import java.util.List;

/** Evicts the page that has been in memory the longest. */
public class FifoPageReplacer extends AbstractPageReplacer {

    @Override
    protected int selectVictim(Integer[] frames, int[] loadedAt, int[] lastUsedAt, List<Integer> references, int position) {
        return indexOfMin(loadedAt);
    }
}
