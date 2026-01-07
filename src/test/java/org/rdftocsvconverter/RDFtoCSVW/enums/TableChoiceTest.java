package org.rdftocsvconverter.RDFtoCSVW.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for TableChoice enum.
 */
class TableChoiceTest {

    @Test
    void testEnumValues() {
        // When/Then
        assertEquals(2, TableChoice.values().length);
        assertNotNull(TableChoice.valueOf("ONE"));
        assertNotNull(TableChoice.valueOf("MORE"));
    }

    @Test
    void testOneValue() {
        // When
        TableChoice choice = TableChoice.ONE;

        // Then
        assertEquals("ONE", choice.name());
        assertEquals(TableChoice.ONE, TableChoice.valueOf("ONE"));
    }

    @Test
    void testMoreValue() {
        // When
        TableChoice choice = TableChoice.MORE;

        // Then
        assertEquals("MORE", choice.name());
        assertEquals(TableChoice.MORE, TableChoice.valueOf("MORE"));
    }

    @Test
    void testInvalidValue() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            TableChoice.valueOf("INVALID");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            TableChoice.valueOf("MULTIPLE");
        });
    }

    @Test
    void testEnumOrdering() {
        // When
        TableChoice[] values = TableChoice.values();

        // Then
        assertEquals(TableChoice.ONE, values[0]);
        assertEquals(TableChoice.MORE, values[1]);
    }

    @Test
    void testEnumComparison() {
        // When/Then
        assertTrue(TableChoice.ONE.ordinal() < TableChoice.MORE.ordinal());
        assertEquals(0, TableChoice.ONE.ordinal());
        assertEquals(1, TableChoice.MORE.ordinal());
    }
}
