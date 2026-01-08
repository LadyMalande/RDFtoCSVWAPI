package org.rdftocsvconverter.RDFtoCSVW.model;

import org.rdftocsvconverter.RDFtoCSVW.enums.ComputationStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model representing a computation task with its status and result.
 * Implements Serializable for Redis storage compatibility.
 */
public class ComputationTask implements Serializable {
    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for this computation session.
     */
    private String sessionId;
    
    /**
     * Current status of the computation (COMPUTING, DONE, or FAILED).
     */
    private ComputationStatus status;
    
    /**
     * Byte array containing the computation result (e.g., ZIP file content).
     */
    private byte[] result;
    
    /**
     * Error message if the computation failed.
     */
    private String errorMessage;
    
    /**
     * Timestamp when the task was created.
     */
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the task was completed (either successfully or with error).
     */
    private LocalDateTime completedAt;

    /**
     * Default constructor initializing the task with COMPUTING status.
     */
    public ComputationTask() {
        this.createdAt = LocalDateTime.now();
        this.status = ComputationStatus.COMPUTING;
    }

    /**
     * Constructs a ComputationTask with the given session ID.
     *
     * @param sessionId the unique session identifier
     */
    public ComputationTask(String sessionId) {
        this.sessionId = sessionId;
        this.createdAt = LocalDateTime.now();
        this.status = ComputationStatus.COMPUTING;
    }

    /**
     * Gets the session identifier.
     *
     * @return the session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the session identifier.
     *
     * @param sessionId the session ID to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Gets the current computation status.
     *
     * @return the computation status
     */
    public ComputationStatus getStatus() {
        return status;
    }

    /**
     * Sets the computation status.
     *
     * @param status the status to set
     */
    public void setStatus(ComputationStatus status) {
        this.status = status;
    }

    /**
     * Gets the computation result as a byte array.
     *
     * @return the result byte array
     */
    public byte[] getResult() {
        return result;
    }

    /**
     * Sets the computation result.
     *
     * @param result the result byte array to set
     */
    public void setResult(byte[] result) {
        this.result = result;
    }

    /**
     * Gets the error message if computation failed.
     *
     * @return the error message, or null if no error
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message.
     *
     * @param errorMessage the error message to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the task creation timestamp.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the task completion timestamp.
     *
     * @return the completion timestamp, or null if not completed
     */
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    /**
     * Sets the completion timestamp.
     *
     * @param completedAt the completion timestamp to set
     */
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * Marks the task as successfully completed with the given result.
     * Sets status to DONE and records the completion timestamp.
     *
     * @param result the computation result to store
     */
    public void markAsCompleted(byte[] result) {
        this.status = ComputationStatus.DONE;
        this.result = result;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Marks the task as failed with the given error message.
     * Sets status to FAILED and records the completion timestamp.
     *
     * @param errorMessage description of the error that occurred
     */
    public void markAsFailed(String errorMessage) {
        this.status = ComputationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
}
