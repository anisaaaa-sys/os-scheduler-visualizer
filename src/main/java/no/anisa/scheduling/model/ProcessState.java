package no.anisa.scheduling.model;

/** A process and its remaining burst time at the start of a tick. */
public record ProcessState(String processId, int remainingBurst) {
}
