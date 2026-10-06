package no.anisa.paging.model;

import java.util.ArrayList;
import java.util.List;

public final class ReferenceString {

    /** The classic 20-reference textbook string, pre-filled in the Page Replacement view. */
    public static final String EXAMPLE = "7 0 1 2 0 3 0 4 2 3 0 3 2 1 2 0 1 7 0 1";

    private ReferenceString() {
    }

    /**
     * Parses page numbers separated by whitespace and/or commas.
     *
     * @throws IllegalArgumentException if the input is blank or contains anything other than non-negative integers
     */
    public static List<Integer> parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Enter at least one page number");
        }
        List<Integer> pages = new ArrayList<>();
        for (String token : input.trim().split("[\\s,]+")) {
            if (token.isEmpty()) {
                continue;
            }
            int page;
            try {
                page = Integer.parseInt(token);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("\"" + token + "\" is not a page number");
            }
            if (page < 0) {
                throw new IllegalArgumentException("Page numbers cannot be negative");
            }
            pages.add(page);
        }
        if (pages.isEmpty()) {
            throw new IllegalArgumentException("Enter at least one page number");
        }
        return List.copyOf(pages);
    }
}
