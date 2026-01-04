package org.rdftocsvconverter.RDFtoCSVW.model;

import org.rdftocsvconverter.RDFtoCSVW.enums.ComputationStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Model representing a computation task with its status and result.
 */
public class ComputationTask implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sessionId;
    private ComputationStatus status;
    private byte[] result;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public ComputationTask() {
        this.createdAt = LocalDateTime.now();
        this.status = ComputationStatus.COMPUTING;
    }

    public ComputationTask(String sessionId) {
        this.sessionId = sessionId;
        this.createdAt = LocalDateTime.now();
        this.status = ComputationStatus.COMPUTING;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public ComputationStatus getStatus() {
        return status;
    }

    public void setStatus(ComputationStatus status) {
        this.status = status;
    }

    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public void markAsCompleted(byte[] result) {
        this.status = ComputationStatus.DONE;
        this.result = result;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = ComputationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
}
