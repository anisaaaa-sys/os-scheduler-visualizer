package no.anisa.scheduling.model;

/**
 * Something that happens at the start of a tick. For {@link Type#PREEMPTION}, {@code processId} is the
 * process that takes the CPU and {@code otherProcessId} the one that loses it; otherwise it is {@code null}.
 */
public record TraceEvent(Type type, String processId, String otherProcessId) {

    public enum Type {
        COMPLETION, ARRIVAL, PREEMPTION
    }

    public static TraceEvent arrival(String processId) {
        return new TraceEvent(Type.ARRIVAL, processId, null);
    }

    public static TraceEvent completion(String processId) {
        return new TraceEvent(Type.COMPLETION, processId, null);
    }

    public static TraceEvent preemption(String processId, String preemptedProcessId) {
        return new TraceEvent(Type.PREEMPTION, processId, preemptedProcessId);
    }

    public String describe() {
        return switch (type) {
            case ARRIVAL -> processId + " arrives";
            case COMPLETION -> processId + " completes";
            case PREEMPTION -> processId + " preempts " + otherProcessId;
        };
    }
}
