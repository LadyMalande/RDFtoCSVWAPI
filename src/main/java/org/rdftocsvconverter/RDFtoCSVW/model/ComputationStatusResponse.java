package org.rdftocsvconverter.RDFtoCSVW.model;

import org.rdftocsvconverter.RDFtoCSVW.enums.ComputationStatus;

/**
 * Response DTO for computation status endpoint.
 * Used to return the current state of an async computation task.
 */
public class ComputationStatusResponse {
    /**
     * Unique identifier for the computation session.
     */
    private String sessionId;
    
    /**
     * Current status of the computation.
     */
    private ComputationStatus status;
    
    /**
     * Optional message providing additional information.
     */
    private String message;
    
    /**
     * Computation result if completed successfully.
     */
    private byte[] result;

    /**
     * Constructs a status response with session ID and status only.
     *
     * @param sessionId the unique session identifier
     * @param status the current computation status
     */
    public ComputationStatusResponse(String sessionId, ComputationStatus status) {
        this.sessionId = sessionId;
        this.status = status;
    }

    /**
     * Constructs a status response with session ID, status, and message.
     *
     * @param sessionId the unique session identifier
     * @param status the current computation status
     * @param message additional information or error message
     */
    public ComputationStatusResponse(String sessionId, ComputationStatus status, String message) {
        this.sessionId = sessionId;
        this.status = status;
        this.message = message;
    }

    /**
     * Constructs a status response with session ID, status, and result.
     *
     * @param sessionId the unique session identifier
     * @param status the current computation status
     * @param result the computation result
     */
    public ComputationStatusResponse(String sessionId, ComputationStatus status, byte[] result) {
        this.sessionId = sessionId;
        this.status = status;
        this.result = result;
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
     * Gets the computation status.
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
     * Gets the status message.
     *
     * @return the message, or null if not set
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the status message.
     *
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the computation result.
     *
     * @return the result byte array, or null if not completed
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
}
