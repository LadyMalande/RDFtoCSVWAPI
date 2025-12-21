package org.rdftocsvconverter.RDFtoCSVW.service;

import org.rdftocsvconverter.RDFtoCSVW.model.ProgressEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service for managing Server-Sent Events (SSE) connections and publishing progress events.
 */
@Service
public class ProgressEventPublisher {
    
    // Map of sessionId -> List of SSE emitters
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> sessionEmitters = new ConcurrentHashMap<>();
    
    /**
     * Register a new SSE emitter for a session.
     * 
     * @param sessionId the session identifier
     * @param emitter the SSE emitter
     */
    public void addEmitter(String sessionId, SseEmitter emitter) {
        sessionEmitters.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        
        emitter.onCompletion(() -> removeEmitter(sessionId, emitter));
        emitter.onTimeout(() -> removeEmitter(sessionId, emitter));
        emitter.onError(e -> removeEmitter(sessionId, emitter));
    }
    
    /**
     * Remove an emitter from a session.
     * 
     * @param sessionId the session identifier
     * @param emitter the SSE emitter to remove
     */
    private void removeEmitter(String sessionId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = sessionEmitters.get(sessionId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                sessionEmitters.remove(sessionId);
            }
        }
    }
    
    /**
     * Publish a progress event to all emitters for a session.
     * 
     * @param event the progress event
     */
    public void publishProgress(ProgressEvent event) {
        CopyOnWriteArrayList<SseEmitter> emitters = sessionEmitters.get(event.getSessionId());
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("progress")
                            .data(event));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                    removeEmitter(event.getSessionId(), emitter);
                }
            }
        }
    }
    
    /**
     * Complete all emitters for a session (when conversion is done).
     * 
     * @param sessionId the session identifier
     */
    public void completeSession(String sessionId) {
        CopyOnWriteArrayList<SseEmitter> emitters = sessionEmitters.get(sessionId);
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                emitter.complete();
            }
            sessionEmitters.remove(sessionId);
        }
    }
    
    /**
     * Send an error to all emitters for a session.
     * 
     * @param sessionId the session identifier
     * @param error the error
     */
    public void errorSession(String sessionId, Throwable error) {
        CopyOnWriteArrayList<SseEmitter> emitters = sessionEmitters.get(sessionId);
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                emitter.completeWithError(error);
            }
            sessionEmitters.remove(sessionId);
        }
    }
}
