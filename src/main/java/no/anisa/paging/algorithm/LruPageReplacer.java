package no.anisa.paging.algorithm;

import java.util.List;

/** Evicts the page whose most recent reference is furthest in the past. */
public class LruPageReplacer extends AbstractPageReplacer {

    @Override
    protected int selectVictim(Integer[] frames, int[] loadedAt, int[] lastUsedAt, List<Integer> references, int position) {
        return indexOfMin(lastUsedAt);
    }
}
