package org.rdftocsvconverter.RDFtoCSVW.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.rdftocsvconverter.RDFtoCSVW.enums.ComputationStatus;
import org.rdftocsvconverter.RDFtoCSVW.model.ComputationTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for TaskService using Spring Boot integration testing.
 * REQUIRES: Redis to be running (e.g., via docker-compose up -d)
 */
@Disabled("Requires Redis - run 'docker-compose up -d' to enable these tests")
@SpringBootTest
@TestPropertySource(properties = {
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379"
})
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private String testSessionId;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        if (testSessionId != null) {
            taskService.deleteTask(testSessionId);
        }
    }

    @Test
    void testCreateTask() {
        // When
        testSessionId = taskService.createTask();

        // Then
        assertNotNull(testSessionId);
        assertFalse(testSessionId.isEmpty());
        
        // Verify task was created in Redis
        ComputationTask task = taskService.getTask(testSessionId);
        assertNotNull(task);
        assertEquals(testSessionId, task.getSessionId());
        assertEquals(ComputationStatus.COMPUTING, task.getStatus());
    }

    @Test
    void testSaveTask() {
        // Given
        testSessionId = "test-save-session-" + System.currentTimeMillis();
        ComputationTask task = new ComputationTask(testSessionId);

        // When
        taskService.saveTask(task);

        // Then
        ComputationTask retrieved = taskService.getTask(testSessionId);
        assertNotNull(retrieved);
        assertEquals(testSessionId, retrieved.getSessionId());
        assertEquals(ComputationStatus.COMPUTING, retrieved.getStatus());
    }

    @Test
    void testGetTask_Found() {
        // Given
        testSessionId = taskService.createTask();

        // When
        ComputationTask result = taskService.getTask(testSessionId);

        // Then
        assertNotNull(result);
        assertEquals(testSessionId, result.getSessionId());
        assertEquals(ComputationStatus.COMPUTING, result.getStatus());
    }

    @Test
    void testGetTask_NotFound() {
        // Given
        String sessionId = "non-existent-session-" + System.currentTimeMillis();

        // When
        ComputationTask result = taskService.getTask(sessionId);

        // Then
        assertNull(result);
    }

    @Test
    void testMarkTaskAsCompleted() {
        // Given
        testSessionId = taskService.createTask();
        byte[] resultData = "test result".getBytes();

        // When
        taskService.markTaskAsCompleted(testSessionId, resultData);

        // Then
        ComputationTask task = taskService.getTask(testSessionId);
        assertNotNull(task);
        assertEquals(ComputationStatus.DONE, task.getStatus());
        assertNotNull(task.getResult());
        assertArrayEquals(resultData, task.getResult());
        assertNotNull(task.getCompletedAt());
    }

    @Test
    void testMarkTaskAsCompleted_TaskNotFound() {
        // Given
        String sessionId = "non-existent-session-" + System.currentTimeMillis();
        byte[] resultData = "test result".getBytes();

        // When - should not throw exception
        taskService.markTaskAsCompleted(sessionId, resultData);

        // Then - task still doesn't exist
        ComputationTask task = taskService.getTask(sessionId);
        assertNull(task);
    }

    @Test
    void testMarkTaskAsFailed() {
        // Given
        testSessionId = taskService.createTask();
        String errorMessage = "Test error occurred";

        // When
        taskService.markTaskAsFailed(testSessionId, errorMessage);

        // Then
        ComputationTask task = taskService.getTask(testSessionId);
        assertNotNull(task);
        assertEquals(ComputationStatus.FAILED, task.getStatus());
        assertEquals(errorMessage, task.getErrorMessage());
        assertNotNull(task.getCompletedAt());
    }

    @Test
    void testMarkTaskAsFailed_TaskNotFound() {
        // Given
        String sessionId = "non-existent-session-" + System.currentTimeMillis();
        String errorMessage = "Test error";

        // When - should not throw exception
        taskService.markTaskAsFailed(sessionId, errorMessage);

        // Then - task still doesn't exist
        ComputationTask task = taskService.getTask(sessionId);
        assertNull(task);
    }

    @Test
    void testDeleteTask() {
        // Given
        testSessionId = taskService.createTask();
        assertNotNull(taskService.getTask(testSessionId));

        // When
        taskService.deleteTask(testSessionId);

        // Then
        ComputationTask task = taskService.getTask(testSessionId);
        assertNull(task);
    }

    @Test
    void testTaskExists() {
        // Given
        testSessionId = taskService.createTask();

        // When
        boolean exists = taskService.taskExists(testSessionId);

        // Then
        assertTrue(exists);
    }

    @Test
    void testTaskDoesNotExist() {
        // Given
        String sessionId = "non-existent-session-" + System.currentTimeMillis();

        // When
        boolean exists = taskService.taskExists(sessionId);

        // Then
        assertFalse(exists);
    }
}
