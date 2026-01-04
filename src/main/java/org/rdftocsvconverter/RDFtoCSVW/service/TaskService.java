package org.rdftocsvconverter.RDFtoCSVW.service;

import org.rdftocsvconverter.RDFtoCSVW.enums.ComputationStatus;
import org.rdftocsvconverter.RDFtoCSVW.model.ComputationTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing computation tasks with session IDs stored in Redis.
 */
@Service
public class TaskService {

    private static final String TASK_KEY_PREFIX = "task:";
    private static final long TASK_EXPIRATION_HOURS = 24; // Tasks expire after 24 hours

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Create a new computation task with a unique session ID.
     *
     * @return the session ID
     */
    public String createTask() {
        String sessionId = UUID.randomUUID().toString();
        ComputationTask task = new ComputationTask(sessionId);
        saveTask(task);
        return sessionId;
    }

    /**
     * Save a task to Redis.
     *
     * @param task the task to save
     */
    public void saveTask(ComputationTask task) {
        String key = TASK_KEY_PREFIX + task.getSessionId();
        redisTemplate.opsForValue().set(key, task, TASK_EXPIRATION_HOURS, TimeUnit.HOURS);
    }

    /**
     * Get a task by session ID.
     *
     * @param sessionId the session ID
     * @return the computation task, or null if not found
     */
    public ComputationTask getTask(String sessionId) {
        String key = TASK_KEY_PREFIX + sessionId;
        Object task = redisTemplate.opsForValue().get(key);
        return task != null ? (ComputationTask) task : null;
    }

    /**
     * Update task status to DONE with result.
     *
     * @param sessionId the session ID
     * @param result    the computation result
     */
    public void markTaskAsCompleted(String sessionId, byte[] result) {
        ComputationTask task = getTask(sessionId);
        if (task != null) {
            task.markAsCompleted(result);
            saveTask(task);
        }
    }

    /**
     * Update task status to FAILED with error message.
     *
     * @param sessionId    the session ID
     * @param errorMessage the error message
     */
    public void markTaskAsFailed(String sessionId, String errorMessage) {
        ComputationTask task = getTask(sessionId);
        if (task != null) {
            task.markAsFailed(errorMessage);
            saveTask(task);
        }
    }

    /**
     * Delete a task from Redis.
     *
     * @param sessionId the session ID
     */
    public void deleteTask(String sessionId) {
        String key = TASK_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
    }

    /**
     * Check if a task exists.
     *
     * @param sessionId the session ID
     * @return true if task exists, false otherwise
     */
    public boolean taskExists(String sessionId) {
        String key = TASK_KEY_PREFIX + sessionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Get the status of a task.
     *
     * @param sessionId the session ID
     * @return the computation status, or null if task not found
     */
    public ComputationStatus getTaskStatus(String sessionId) {
        ComputationTask task = getTask(sessionId);
        return task != null ? task.getStatus() : null;
    }
}
