package org.rdftocsvconverter.RDFtoCSVW.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ParsingChoice enum.
 */
class ParsingChoiceTest {

    @Test
    void testEnumValues() {
        // When/Then
        assertEquals(3, ParsingChoice.values().length);
        assertNotNull(ParsingChoice.valueOf("RDF4J"));
        assertNotNull(ParsingChoice.valueOf("STREAMING"));
        assertNotNull(ParsingChoice.valueOf("BIGFILESTREAMING"));
    }

    @Test
    void testRDF4JValue() {
        // When
        ParsingChoice choice = ParsingChoice.RDF4J;

        // Then
        assertEquals("RDF4J", choice.name());
        assertEquals(ParsingChoice.RDF4J, ParsingChoice.valueOf("RDF4J"));
    }

    @Test
    void testStreamingValue() {
        // When
        ParsingChoice choice = ParsingChoice.STREAMING;

        // Then
        assertEquals("STREAMING", choice.name());
        assertEquals(ParsingChoice.STREAMING, ParsingChoice.valueOf("STREAMING"));
    }

    @Test
    void testBigFileStreamingValue() {
        // When
        ParsingChoice choice = ParsingChoice.BIGFILESTREAMING;

        // Then
        assertEquals("BIGFILESTREAMING", choice.name());
        assertEquals(ParsingChoice.BIGFILESTREAMING, ParsingChoice.valueOf("BIGFILESTREAMING"));
    }

    @Test
    void testInvalidValue() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            ParsingChoice.valueOf("INVALID");
        });
    }

    @Test
    void testEnumOrdering() {
        // When
        ParsingChoice[] values = ParsingChoice.values();

        // Then
        assertEquals(ParsingChoice.RDF4J, values[0]);
        assertEquals(ParsingChoice.STREAMING, values[1]);
        assertEquals(ParsingChoice.BIGFILESTREAMING, values[2]);
    }

    @Test
    void testEnumComparison() {
        // When/Then
        assertTrue(ParsingChoice.RDF4J.ordinal() < ParsingChoice.STREAMING.ordinal());
        assertTrue(ParsingChoice.STREAMING.ordinal() < ParsingChoice.BIGFILESTREAMING.ordinal());
    }
}
