package no.anisa.paging.algorithm;

import no.anisa.paging.model.PageReplacementResult;
import no.anisa.paging.model.PageStep;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PageReplacementAssertions {

    /** Spec example reference string. */
    static final List<Integer> SPEC_EXAMPLE = List.of(7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2);

    /** Classic 20-reference textbook string (Silberschatz). */
    static final List<Integer> TEXTBOOK = List.of(7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 1, 2, 0, 1, 7, 0, 1);

    private PageReplacementAssertions() {
    }

    /** Each expected row is the frame contents after that step; {@code null} means an empty frame. */
    static void assertFrames(PageReplacementResult result, Integer[]... expected) {
        assertEquals(expected.length, result.steps().size(), "step count");
        for (int i = 0; i < expected.length; i++) {
            assertEquals(Arrays.asList(expected[i]), result.steps().get(i).frames(), "frames after step " + i);
        }
    }

    static void assertFaultPattern(PageReplacementResult result, String pattern) {
        StringBuilder actual = new StringBuilder();
        for (PageStep step : result.steps()) {
            actual.append(step.fault() ? 'F' : 'H');
        }
        assertEquals(pattern, actual.toString());
    }

    static Integer[] f(Integer... frames) {
        return frames;
    }
}
