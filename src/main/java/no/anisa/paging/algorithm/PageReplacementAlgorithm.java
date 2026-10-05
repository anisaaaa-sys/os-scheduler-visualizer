package no.anisa.paging.algorithm;

import no.anisa.paging.model.PageReplacementResult;

import java.util.List;

public interface PageReplacementAlgorithm {

    PageReplacementResult simulate(List<Integer> references, int frameCount);
}
