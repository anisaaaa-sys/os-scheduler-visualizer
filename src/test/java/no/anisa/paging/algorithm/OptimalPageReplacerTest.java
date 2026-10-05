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
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptimalPageReplacerTest {

    private final OptimalPageReplacer optimal = new OptimalPageReplacer();

    @Test
    void evictsPageUsedFurthestInFuture() {
        PageReplacementResult result = optimal.simulate(SPEC_EXAMPLE, 3);

        assertFrames(result,
                f(7, null, null),
                f(7, 0, null),
                f(7, 0, 1),
                f(2, 0, 1),
                f(2, 0, 1),
                f(2, 0, 3),
                f(2, 0, 3),
                f(2, 4, 3),
                f(2, 4, 3),
                f(2, 4, 3),
                f(2, 0, 3),
                f(2, 0, 3),
                f(2, 0, 3));
        assertFaultPattern(result, "FFFFHFHFHHFHH");
        assertEquals(7, result.pageFaults());
    }

    @Test
    void breaksNeverUsedAgainTiesByEarliestLoaded() {
        // At page 4, pages 1, 2 and 3 are never referenced again; 1 was loaded first.
        PageReplacementResult result = optimal.simulate(List.of(1, 2, 3, 4), 3);

        assertEquals(1, result.steps().get(3).evictedPage());
    }

    @Test
    void matchesTextbookFaultCount() {
        assertEquals(9, optimal.simulate(TEXTBOOK, 3).pageFaults());
    }

    @Test
    void neverFaultsMoreThanFifoOrLru() {
        for (int frames = 1; frames <= 7; frames++) {
            int opt = optimal.simulate(TEXTBOOK, frames).pageFaults();
            assertTrue(opt <= new FifoPageReplacer().simulate(TEXTBOOK, frames).pageFaults(), "vs FIFO, frames=" + frames);
            assertTrue(opt <= new LruPageReplacer().simulate(TEXTBOOK, frames).pageFaults(), "vs LRU, frames=" + frames);
        }
    }
}
