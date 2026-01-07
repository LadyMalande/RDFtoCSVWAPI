package org.rdftocsvconverter.RDFtoCSVW.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SessionResponse model.
 */
class SessionResponseTest {

    @Test
    void testConstructorWithSessionId() {
        // Given
        String sessionId = "test-session-123";

        // When
        SessionResponse response = new SessionResponse(sessionId);

        // Then
        assertEquals(sessionId, response.getSessionId());
        assertEquals("Computation started. Use the sessionId to check status.", response.getMessage());
        assertEquals("/status/" + sessionId, response.getStatusUrl());
    }

    @Test
    void testConstructorWithSessionIdAndMessage() {
        // Given
        String sessionId = "test-session-456";
        String customMessage = "Custom message";

        // When
        SessionResponse response = new SessionResponse(sessionId, customMessage);

        // Then
        assertEquals(sessionId, response.getSessionId());
        assertEquals(customMessage, response.getMessage());
        assertEquals("/status/" + sessionId, response.getStatusUrl());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        SessionResponse response = new SessionResponse("initial-id");
        
        String newSessionId = "new-session-id";
        String newMessage = "New message";
        String newStatusUrl = "/custom/status/url";

        // When
        response.setSessionId(newSessionId);
        response.setMessage(newMessage);
        response.setStatusUrl(newStatusUrl);

        // Then
        assertEquals(newSessionId, response.getSessionId());
        assertEquals(newMessage, response.getMessage());
        assertEquals(newStatusUrl, response.getStatusUrl());
    }

    @Test
    void testStatusUrlFormat() {
        // Given
        String sessionId = "abc-123-def-456";

        // When
        SessionResponse response = new SessionResponse(sessionId);

        // Then
        assertTrue(response.getStatusUrl().contains(sessionId));
        assertTrue(response.getStatusUrl().startsWith("/status/"));
    }
}
