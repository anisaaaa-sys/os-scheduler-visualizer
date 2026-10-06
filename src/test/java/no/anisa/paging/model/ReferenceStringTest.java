package no.anisa.paging.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReferenceStringTest {

    @Test
    void parsesSpacesAndCommas() {
        assertEquals(List.of(7, 0, 1, 2), ReferenceString.parse("  7 0,1 ,  2 "));
    }

    @Test
    void parsesExample() {
        assertEquals(List.of(7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 1, 2, 0, 1, 7, 0, 1),
                ReferenceString.parse(ReferenceString.EXAMPLE));
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> ReferenceString.parse(""));
        assertThrows(IllegalArgumentException.class, () -> ReferenceString.parse(" , "));
        assertThrows(IllegalArgumentException.class, () -> ReferenceString.parse("1 a 2"));
        assertThrows(IllegalArgumentException.class, () -> ReferenceString.parse("1 -2"));
    }
}
