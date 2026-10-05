package no.anisa.paging.model;

import java.util.List;

/**
 * State after handling one reference. {@code frames} has one entry per frame; empty frames are {@code null}.
 * {@code changedFrame} is the frame the page was loaded into on a fault, or the frame that held it on a hit.
 */
public record PageStep(int page, List<Integer> frames, boolean fault, int changedFrame, Integer evictedPage) {
}
