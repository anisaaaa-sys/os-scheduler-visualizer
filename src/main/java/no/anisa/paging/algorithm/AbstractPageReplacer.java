package no.anisa.paging.algorithm;

import no.anisa.paging.model.PageReplacementResult;
import no.anisa.paging.model.PageStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Shared simulation loop. Empty frames are filled lowest index first; when all frames are full,
 * the subclass picks which frame to evict and the new page takes that frame's slot.
 */
abstract class AbstractPageReplacer implements PageReplacementAlgorithm {

    @Override
    public PageReplacementResult simulate(List<Integer> references, int frameCount) {
        if (frameCount < 1) {
            throw new IllegalArgumentException("Frame count must be at least 1");
        }

        Integer[] frames = new Integer[frameCount];
        int[] loadedAt = new int[frameCount];
        int[] lastUsedAt = new int[frameCount];
        List<PageStep> steps = new ArrayList<>();
        int faults = 0;

        for (int position = 0; position < references.size(); position++) {
            int page = references.get(position);
            int frame = indexOf(frames, page);
            boolean fault = frame < 0;
            Integer evicted = null;

            if (fault) {
                faults++;
                frame = indexOf(frames, null);
                if (frame < 0) {
                    frame = selectVictim(frames, loadedAt, lastUsedAt, references, position);
                    evicted = frames[frame];
                }
                frames[frame] = page;
                loadedAt[frame] = position;
            }
            lastUsedAt[frame] = position;

            steps.add(new PageStep(page, Arrays.asList(frames.clone()), fault, frame, evicted));
        }

        return new PageReplacementResult(List.copyOf(steps), frameCount, faults);
    }

    /**
     * Returns the index of the frame to evict. Called only when every frame is occupied.
     *
     * @param loadedAt   position in {@code references} at which each frame's page was loaded
     * @param lastUsedAt position in {@code references} at which each frame's page was last referenced
     * @param position   position of the reference currently being handled
     */
    protected abstract int selectVictim(Integer[] frames, int[] loadedAt, int[] lastUsedAt,
                                        List<Integer> references, int position);

    protected static int indexOfMin(int[] values) {
        int best = 0;
        for (int i = 1; i < values.length; i++) {
            if (values[i] < values[best]) {
                best = i;
            }
        }
        return best;
    }

    private static int indexOf(Integer[] frames, Integer page) {
        for (int i = 0; i < frames.length; i++) {
            if (page == null ? frames[i] == null : page.equals(frames[i])) {
                return i;
            }
        }
        return -1;
    }
}
