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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FifoPageReplacerTest {

    private final FifoPageReplacer fifo = new FifoPageReplacer();

    @Test
    void evictsOldestLoadedPage() {
        PageReplacementResult result = fifo.simulate(SPEC_EXAMPLE, 3);

        assertFrames(result,
                f(7, null, null),
                f(7, 0, null),
                f(7, 0, 1),
                f(2, 0, 1),
                f(2, 0, 1),
                f(2, 3, 1),
                f(2, 3, 0),
                f(4, 3, 0),
                f(4, 2, 0),
                f(4, 2, 3),
                f(0, 2, 3),
                f(0, 2, 3),
                f(0, 2, 3));
        assertFaultPattern(result, "FFFFHFFFFFFHH");
        assertEquals(10, result.pageFaults());
        assertEquals(3, result.hits());
        assertEquals(3.0 / 13, result.hitRatio(), 1e-9);
    }

    @Test
    void recordsEvictedPageAndChangedFrame() {
        PageReplacementResult result = fifo.simulate(SPEC_EXAMPLE, 3);

        assertNull(result.steps().get(2).evictedPage());
        assertEquals(7, result.steps().get(3).evictedPage());
        assertEquals(0, result.steps().get(3).changedFrame());
        // Hit on page 0, which sits in frame 1
        assertEquals(1, result.steps().get(4).changedFrame());
    }

    @Test
    void matchesTextbookFaultCount() {
        assertEquals(15, fifo.simulate(TEXTBOOK, 3).pageFaults());
    }

    @Test
    void exhibitsBeladysAnomaly() {
        List<Integer> references = List.of(1, 2, 3, 4, 1, 2, 5, 1, 2, 3, 4, 5);

        assertEquals(9, fifo.simulate(references, 3).pageFaults());
        assertEquals(10, fifo.simulate(references, 4).pageFaults());
    }

    @Test
    void singleFrameFaultsOnEveryChange() {
        PageReplacementResult result = fifo.simulate(List.of(1, 1, 2, 1), 1);

        assertFaultPattern(result, "FHFF");
    }

    @Test
    void rejectsZeroFrames() {
        assertThrows(IllegalArgumentException.class, () -> fifo.simulate(SPEC_EXAMPLE, 0));
    }
}
