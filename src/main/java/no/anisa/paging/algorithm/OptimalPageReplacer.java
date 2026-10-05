package no.anisa.paging.algorithm;

import java.util.List;

/**
 * Evicts the page whose next reference is furthest in the future. Pages never referenced again are
 * preferred; ties are broken by evicting the page loaded earliest (FIFO).
 */
public class OptimalPageReplacer extends AbstractPageReplacer {

    @Override
    protected int selectVictim(Integer[] frames, int[] loadedAt, int[] lastUsedAt, List<Integer> references, int position) {
        int victim = -1;
        int victimNextUse = -1;
        for (int i = 0; i < frames.length; i++) {
            int nextUse = nextUse(frames[i], references, position + 1);
            if (nextUse > victimNextUse || (nextUse == victimNextUse && loadedAt[i] < loadedAt[victim])) {
                victim = i;
                victimNextUse = nextUse;
            }
        }
        return victim;
    }

    private static int nextUse(int page, List<Integer> references, int from) {
        for (int i = from; i < references.size(); i++) {
            if (references.get(i) == page) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }
}
