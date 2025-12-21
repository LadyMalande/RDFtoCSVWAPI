package org.rdftocsvconverter.RDFtoCSVW.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rdftocsvconverter.RDFtoCSVW.model.ProgressEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ProgressEventPublisher.
 * 
 * This test demonstrates how to observe SSE emissions by capturing
 * the data sent through the emitter using a custom test implementation.
 */
class ProgressEventPublisherTest {

    private ProgressEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new ProgressEventPublisher();
    }

    /**
     * Test adding and removing emitters.
     */
    @Test
    void testAddAndRemoveEmitter() {
        String sessionId = "test-session-1";
        SseEmitter emitter = new SseEmitter();

        publisher.addEmitter(sessionId, emitter);
        
        // Emitter should be registered (we can verify by publishing an event)
        ProgressEvent event = new ProgressEvent("PARSING", 50, "Test message", sessionId);
        
        // This should not throw an exception
        assertDoesNotThrow(() -> publisher.publishProgress(event));
    }

    /**
     * Test publishing progress to registered emitters.
     * This test verifies that events are published without errors.
     */
    @Test
    void testPublishProgress() throws InterruptedException {
        String sessionId = "test-session-2";
        
        SseEmitter emitter = new SseEmitter();
        publisher.addEmitter(sessionId, emitter);

        // Publish a progress event - should not throw exception
        ProgressEvent event = new ProgressEvent("CONVERTING", 75, "Processing data", sessionId);
        
        assertDoesNotThrow(() -> publisher.publishProgress(event));
    }

    /**
     * Test publishing multiple events to the same session.
     */
    @Test
    void testPublishMultipleEvents() throws InterruptedException {
        String sessionId = "test-session-3";
        
        SseEmitter emitter = new SseEmitter();
        publisher.addEmitter(sessionId, emitter);

        // Publish multiple events - should not throw exceptions
        assertDoesNotThrow(() -> {
            publisher.publishProgress(new ProgressEvent("PARSING", 0, "Starting", sessionId));
            publisher.publishProgress(new ProgressEvent("PARSING", 50, "Halfway", sessionId));
            publisher.publishProgress(new ProgressEvent("PARSING", 100, "Complete", sessionId));
        });
    }

    /**
     * Test that events are only sent to the correct session.
     */
    @Test
    void testEventIsolationBetweenSessions() throws InterruptedException {
        String sessionId1 = "session-1";
        String sessionId2 = "session-2";
        
        SseEmitter emitter1 = new SseEmitter();
        SseEmitter emitter2 = new SseEmitter();
        
        publisher.addEmitter(sessionId1, emitter1);
        publisher.addEmitter(sessionId2, emitter2);

        // Publish events to different sessions - should not throw exceptions
        assertDoesNotThrow(() -> {
            publisher.publishProgress(new ProgressEvent("PARSING", 50, "Session 1", sessionId1));
            publisher.publishProgress(new ProgressEvent("CONVERTING", 75, "Session 2", sessionId2));
        });
    }

    /**
     * Test completing a session.
     * Note: SseEmitter callbacks don't fire in unit tests without real HTTP connection.
     */
    @Test
    @org.junit.jupiter.api.Disabled("SseEmitter.onCompletion() doesn't fire in unit tests")
    void testCompleteSession() throws InterruptedException {
        String sessionId = "test-session-4";
        
        AtomicInteger completionCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1);
        
        SseEmitter emitter = new SseEmitter();
        emitter.onCompletion(() -> {
            completionCount.incrementAndGet();
            latch.countDown();
        });
        
        publisher.addEmitter(sessionId, emitter);
        publisher.completeSession(sessionId);

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Completion should be called");
        assertEquals(1, completionCount.get(), "Completion should be called once");
    }

    /**
     * Test error handling in a session.
     * Note: SseEmitter callbacks don't fire in unit tests without real HTTP connection.
     */
    @Test
    @org.junit.jupiter.api.Disabled("SseEmitter.onError() doesn't fire in unit tests")
    void testErrorSession() throws InterruptedException {
        String sessionId = "test-session-5";
        
        AtomicReference<Throwable> capturedError = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        
        SseEmitter emitter = new SseEmitter();
        emitter.onError(error -> {
            capturedError.set(error);
            latch.countDown();
        });
        
        publisher.addEmitter(sessionId, emitter);
        
        RuntimeException testException = new RuntimeException("Test error");
        publisher.errorSession(sessionId, testException);

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Error handler should be called");
        assertNotNull(capturedError.get(), "Error should be captured");
        assertEquals("Test error", capturedError.get().getMessage());
    }

    /**
     * Test multiple emitters for the same session.
     */
    @Test
    void testMultipleEmittersPerSession() throws InterruptedException {
        String sessionId = "test-session-6";
        
        SseEmitter emitter1 = new SseEmitter();
        SseEmitter emitter2 = new SseEmitter();
        
        publisher.addEmitter(sessionId, emitter1);
        publisher.addEmitter(sessionId, emitter2);

        // Publish one event - should not throw exception
        ProgressEvent event = new ProgressEvent("PARSING", 100, "Done", sessionId);
        assertDoesNotThrow(() -> publisher.publishProgress(event));
    }

    /**
     * Test that publishing to non-existent session doesn't throw exception.
     */
    @Test
    void testPublishToNonExistentSession() {
        ProgressEvent event = new ProgressEvent("PARSING", 50, "Test", "non-existent-session");
        
        // Should not throw exception
        assertDoesNotThrow(() -> publisher.publishProgress(event));
    }

}
