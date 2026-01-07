package org.rdftocsvconverter.RDFtoCSVW.model;

import org.junit.jupiter.api.Test;
import org.rdftocsvconverter.RDFtoCSVW.enums.ComputationStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ComputationTask model.
 */
class ComputationTaskTest {

    @Test
    void testDefaultConstructor() {
        // When
        ComputationTask task = new ComputationTask();

        // Then
        assertNotNull(task.getCreatedAt());
        assertEquals(ComputationStatus.COMPUTING, task.getStatus());
        assertNull(task.getSessionId());
        assertNull(task.getResult());
        assertNull(task.getErrorMessage());
        assertNull(task.getCompletedAt());
    }

    @Test
    void testConstructorWithSessionId() {
        // Given
        String sessionId = "test-session-123";

        // When
        ComputationTask task = new ComputationTask(sessionId);

        // Then
        assertEquals(sessionId, task.getSessionId());
        assertNotNull(task.getCreatedAt());
        assertEquals(ComputationStatus.COMPUTING, task.getStatus());
        assertNull(task.getResult());
        assertNull(task.getErrorMessage());
        assertNull(task.getCompletedAt());
    }

    @Test
    void testMarkAsCompleted() {
        // Given
        ComputationTask task = new ComputationTask("session-123");
        byte[] result = "test result".getBytes();

        // When
        task.markAsCompleted(result);

        // Then
        assertEquals(ComputationStatus.DONE, task.getStatus());
        assertArrayEquals(result, task.getResult());
        assertNotNull(task.getCompletedAt());
        assertNull(task.getErrorMessage());
    }

    @Test
    void testMarkAsFailed() {
        // Given
        ComputationTask task = new ComputationTask("session-123");
        String errorMessage = "Something went wrong";

        // When
        task.markAsFailed(errorMessage);

        // Then
        assertEquals(ComputationStatus.FAILED, task.getStatus());
        assertEquals(errorMessage, task.getErrorMessage());
        assertNotNull(task.getCompletedAt());
        assertNull(task.getResult());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        ComputationTask task = new ComputationTask();
        String sessionId = "new-session";
        byte[] result = "result data".getBytes();
        String error = "error message";

        // When
        task.setSessionId(sessionId);
        task.setStatus(ComputationStatus.DONE);
        task.setResult(result);
        task.setErrorMessage(error);

        // Then
        assertEquals(sessionId, task.getSessionId());
        assertEquals(ComputationStatus.DONE, task.getStatus());
        assertArrayEquals(result, task.getResult());
        assertEquals(error, task.getErrorMessage());
    }

    @Test
    void testTimestampTracking() throws InterruptedException {
        // Given
        ComputationTask task = new ComputationTask("session-123");
        assertNotNull(task.getCreatedAt());
        assertNull(task.getCompletedAt());

        // When
        Thread.sleep(100); // Small delay to ensure different timestamps
        task.markAsCompleted("result".getBytes());

        // Then
        assertNotNull(task.getCompletedAt());
        assertTrue(task.getCompletedAt().isAfter(task.getCreatedAt()));
    }
}
