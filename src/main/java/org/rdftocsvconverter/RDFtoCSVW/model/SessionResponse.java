package org.rdftocsvconverter.RDFtoCSVW.model;

/**
 * Response DTO for session ID when a computation is initiated.
 */
public class SessionResponse {
    private String sessionId;
    private String message;
    private String statusUrl;

    public SessionResponse(String sessionId) {
        this.sessionId = sessionId;
        this.message = "Computation started. Use the sessionId to check status.";
        this.statusUrl = "/status/" + sessionId;
    }

    public SessionResponse(String sessionId, String message) {
        this.sessionId = sessionId;
        this.message = message;
        this.statusUrl = "/status/" + sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatusUrl() {
        return statusUrl;
    }

    public void setStatusUrl(String statusUrl) {
        this.statusUrl = statusUrl;
    }
}
