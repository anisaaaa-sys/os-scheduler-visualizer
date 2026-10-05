package no.anisa.scheduling.model;

public record GanttSlice(String processId, int start, int end) {

    public boolean isIdle() {
        return processId == null;
    }

    public int duration() {
        return end - start;
    }
}
