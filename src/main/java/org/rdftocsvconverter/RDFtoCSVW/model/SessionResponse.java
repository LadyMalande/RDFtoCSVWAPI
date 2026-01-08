package org.rdftocsvconverter.RDFtoCSVW.model;

/**
 * Response DTO for session ID when a computation is initiated.
 * Contains the session identifier and information about checking computation status.
 */
public class SessionResponse {
    /**
     * Unique identifier for the computation session.
     */
    private String sessionId;
    
    /**
     * Message describing the computation status or result.
     */
    private String message;
    
    /**
     * URL endpoint to check the computation status.
     */
    private String statusUrl;

    /**
     * Constructs a SessionResponse with default success message.
     *
     * @param sessionId the unique session identifier
     */
    public SessionResponse(String sessionId) {
        this.sessionId = sessionId;
        this.message = "Computation started. Use the sessionId to check status.";
        this.statusUrl = "/status/" + sessionId;
    }

    /**
     * Constructs a SessionResponse with custom message.
     *
     * @param sessionId the unique session identifier
     * @param message custom status or error message
     */
    public SessionResponse(String sessionId, String message) {
        this.sessionId = sessionId;
        this.message = message;
        this.statusUrl = "/status/" + sessionId;
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
     * Gets the status or error message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the status or error message.
     *
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the status URL endpoint.
     *
     * @return the status URL
     */
    public String getStatusUrl() {
        return statusUrl;
    }

    /**
     * Sets the status URL endpoint.
     *
     * @param statusUrl the status URL to set
     */
    public void setStatusUrl(String statusUrl) {
        this.statusUrl = statusUrl;
    }
}
