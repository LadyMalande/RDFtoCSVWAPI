package org.rdftocsvconverter.RDFtoCSVW.model;

/**
 * Represents a progress event from the RDF to CSV conversion process.
 */
public class ProgressEvent {
    private String stage;
    private int progress;
    private String message;
    private String sessionId;

    public ProgressEvent() {
    }

    public ProgressEvent(String stage, int progress, String message, String sessionId) {
        this.stage = stage;
        this.progress = progress;
        this.message = message;
        this.sessionId = sessionId;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "ProgressEvent{" +
                "stage='" + stage + '\'' +
                ", progress=" + progress +
                ", message='" + message + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
