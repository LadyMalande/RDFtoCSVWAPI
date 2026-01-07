package org.rdftocsvconverter.RDFtoCSVW.model;

import org.junit.jupiter.api.Test;
import org.rdftocsvconverter.RDFtoCSVW.enums.ComputationStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ComputationStatusResponse model.
 */
class ComputationStatusResponseTest {

    @Test
    void testConstructorWithSessionIdAndStatus() {
        // Given
        String sessionId = "test-session-123";
        ComputationStatus status = ComputationStatus.COMPUTING;

        // When
        ComputationStatusResponse response = new ComputationStatusResponse(sessionId, status);

        // Then
        assertEquals(sessionId, response.getSessionId());
        assertEquals(status, response.getStatus());
        assertNull(response.getMessage());
        assertNull(response.getResult());
    }

    @Test
    void testConstructorWithSessionIdStatusAndMessage() {
        // Given
        String sessionId = "test-session-456";
        ComputationStatus status = ComputationStatus.FAILED;
        String message = "Error occurred";

        // When
        ComputationStatusResponse response = new ComputationStatusResponse(sessionId, status, message);

        // Then
        assertEquals(sessionId, response.getSessionId());
        assertEquals(status, response.getStatus());
        assertEquals(message, response.getMessage());
        assertNull(response.getResult());
    }

    @Test
    void testConstructorWithSessionIdStatusAndResult() {
        // Given
        String sessionId = "test-session-789";
        ComputationStatus status = ComputationStatus.DONE;
        byte[] result = "test result data".getBytes();

        // When
        ComputationStatusResponse response = new ComputationStatusResponse(sessionId, status, result);

        // Then
        assertEquals(sessionId, response.getSessionId());
        assertEquals(status, response.getStatus());
        assertArrayEquals(result, response.getResult());
        assertNull(response.getMessage());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        ComputationStatusResponse response = new ComputationStatusResponse("initial-id", ComputationStatus.COMPUTING);
        
        String newSessionId = "new-session-id";
        ComputationStatus newStatus = ComputationStatus.DONE;
        String newMessage = "Completed successfully";
        byte[] newResult = "result bytes".getBytes();

        // When
        response.setSessionId(newSessionId);
        response.setStatus(newStatus);
        response.setMessage(newMessage);
        response.setResult(newResult);

        // Then
        assertEquals(newSessionId, response.getSessionId());
        assertEquals(newStatus, response.getStatus());
        assertEquals(newMessage, response.getMessage());
        assertArrayEquals(newResult, response.getResult());
    }

    @Test
    void testComputingStatusResponse() {
        // When
        ComputationStatusResponse response = new ComputationStatusResponse(
            "session-123", 
            ComputationStatus.COMPUTING, 
            "Computation is still in progress"
        );

        // Then
        assertEquals(ComputationStatus.COMPUTING, response.getStatus());
        assertEquals("Computation is still in progress", response.getMessage());
        assertNull(response.getResult());
    }

    @Test
    void testDoneStatusResponse() {
        // Given
        byte[] zipData = new byte[]{0x50, 0x4b, 0x03, 0x04}; // ZIP header

        // When
        ComputationStatusResponse response = new ComputationStatusResponse(
            "session-456",
            ComputationStatus.DONE,
            zipData
        );

        // Then
        assertEquals(ComputationStatus.DONE, response.getStatus());
        assertArrayEquals(zipData, response.getResult());
    }

    @Test
    void testFailedStatusResponse() {
        // When
        ComputationStatusResponse response = new ComputationStatusResponse(
            "session-789",
            ComputationStatus.FAILED,
            "File not found error"
        );

        // Then
        assertEquals(ComputationStatus.FAILED, response.getStatus());
        assertEquals("File not found error", response.getMessage());
        assertNull(response.getResult());
    }
}
