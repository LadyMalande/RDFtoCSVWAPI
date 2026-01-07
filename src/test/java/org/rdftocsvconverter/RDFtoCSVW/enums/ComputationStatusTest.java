package org.rdftocsvconverter.RDFtoCSVW.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ComputationStatus enum.
 */
class ComputationStatusTest {

    @Test
    void testEnumValues() {
        // When/Then
        assertEquals(3, ComputationStatus.values().length);
        assertNotNull(ComputationStatus.valueOf("COMPUTING"));
        assertNotNull(ComputationStatus.valueOf("DONE"));
        assertNotNull(ComputationStatus.valueOf("FAILED"));
    }

    @Test
    void testComputingValue() {
        // When
        ComputationStatus status = ComputationStatus.COMPUTING;

        // Then
        assertEquals("COMPUTING", status.name());
        assertEquals(ComputationStatus.COMPUTING, ComputationStatus.valueOf("COMPUTING"));
    }

    @Test
    void testDoneValue() {
        // When
        ComputationStatus status = ComputationStatus.DONE;

        // Then
        assertEquals("DONE", status.name());
        assertEquals(ComputationStatus.DONE, ComputationStatus.valueOf("DONE"));
    }

    @Test
    void testFailedValue() {
        // When
        ComputationStatus status = ComputationStatus.FAILED;

        // Then
        assertEquals("FAILED", status.name());
        assertEquals(ComputationStatus.FAILED, ComputationStatus.valueOf("FAILED"));
    }

    @Test
    void testInvalidValue() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            ComputationStatus.valueOf("INVALID");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ComputationStatus.valueOf("PENDING");
        });
    }

    @Test
    void testEnumOrdering() {
        // When
        ComputationStatus[] values = ComputationStatus.values();

        // Then
        assertEquals(ComputationStatus.COMPUTING, values[0]);
        assertEquals(ComputationStatus.DONE, values[1]);
        assertEquals(ComputationStatus.FAILED, values[2]);
    }

    @Test
    void testEnumComparison() {
        // When/Then
        assertTrue(ComputationStatus.COMPUTING.ordinal() < ComputationStatus.DONE.ordinal());
        assertTrue(ComputationStatus.DONE.ordinal() < ComputationStatus.FAILED.ordinal());
        assertEquals(0, ComputationStatus.COMPUTING.ordinal());
        assertEquals(1, ComputationStatus.DONE.ordinal());
        assertEquals(2, ComputationStatus.FAILED.ordinal());
    }

    @Test
    void testEnumEquality() {
        // When/Then
        assertEquals(ComputationStatus.COMPUTING, ComputationStatus.COMPUTING);
        assertEquals(ComputationStatus.DONE, ComputationStatus.DONE);
        assertEquals(ComputationStatus.FAILED, ComputationStatus.FAILED);
        
        assertNotEquals(ComputationStatus.COMPUTING, ComputationStatus.DONE);
        assertNotEquals(ComputationStatus.DONE, ComputationStatus.FAILED);
        assertNotEquals(ComputationStatus.COMPUTING, ComputationStatus.FAILED);
    }
}
