package org.rdftocsvconverter.RDFtoCSVW.model;

import org.rdftocsvconverter.RDFtoCSVW.enums.ComputationStatus;

/**
 * Response DTO for computation status endpoint.
 */
public class ComputationStatusResponse {
    private String sessionId;
    private ComputationStatus status;
    private String message;
    private byte[] result;

    public ComputationStatusResponse(String sessionId, ComputationStatus status) {
        this.sessionId = sessionId;
        this.status = status;
    }

    public ComputationStatusResponse(String sessionId, ComputationStatus status, String message) {
        this.sessionId = sessionId;
        this.status = status;
        this.message = message;
    }

    public ComputationStatusResponse(String sessionId, ComputationStatus status, byte[] result) {
        this.sessionId = sessionId;
        this.status = status;
        this.result = result;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
    }
}
