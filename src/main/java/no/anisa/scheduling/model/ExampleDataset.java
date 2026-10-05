package no.anisa.scheduling.model;

import java.util.List;

public final class ExampleDataset {

    private ExampleDataset() {
    }

    public static List<ProcessRecord> classicFiveProcesses() {
        return List.of(
                new ProcessRecord("P1", 0, 5, 3),
                new ProcessRecord("P2", 1, 3, 1),
                new ProcessRecord("P3", 2, 8, 4),
                new ProcessRecord("P4", 3, 6, 2),
                new ProcessRecord("P5", 4, 2, 5)
        );
    }
}
