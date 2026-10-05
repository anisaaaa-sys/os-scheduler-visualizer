package no.anisa.paging.algorithm;

import no.anisa.paging.model.PageReplacementResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static no.anisa.paging.algorithm.PageReplacementAssertions.SPEC_EXAMPLE;
import static no.anisa.paging.algorithm.PageReplacementAssertions.TEXTBOOK;
import static no.anisa.paging.algorithm.PageReplacementAssertions.assertFaultPattern;
import static no.anisa.paging.algorithm.PageReplacementAssertions.assertFrames;
import static no.anisa.paging.algorithm.PageReplacementAssertions.f;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LruPageReplacerTest {

    private final LruPageReplacer lru = new LruPageReplacer();

    @Test
    void evictsLeastRecentlyUsedPage() {
        PageReplacementResult result = lru.simulate(SPEC_EXAMPLE, 3);

        assertFrames(result,
                f(7, null, null),
                f(7, 0, null),
                f(7, 0, 1),
                f(2, 0, 1),
                f(2, 0, 1),
                f(2, 0, 3),
                f(2, 0, 3),
                f(4, 0, 3),
                f(4, 0, 2),
                f(4, 3, 2),
                f(0, 3, 2),
                f(0, 3, 2),
                f(0, 3, 2));
        assertFaultPattern(result, "FFFFHFHFFFFHH");
        assertEquals(9, result.pageFaults());
    }

    @Test
    void hitRefreshesRecency() {
        // FIFO would evict 1 here; LRU keeps it because it was just used.
        PageReplacementResult result = lru.simulate(List.of(1, 2, 1, 3), 2);

        assertEquals(2, result.steps().get(3).evictedPage());
    }

    @Test
    void matchesTextbookFaultCount() {
        assertEquals(12, lru.simulate(TEXTBOOK, 3).pageFaults());
    }
}
