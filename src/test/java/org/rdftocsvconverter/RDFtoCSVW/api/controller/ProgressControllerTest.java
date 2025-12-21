package org.rdftocsvconverter.RDFtoCSVW.api.controller;

import org.junit.jupiter.api.Test;
import org.rdftocsvconverter.RDFtoCSVW.RDFToCSVWApiApplication;
import org.rdftocsvconverter.RDFtoCSVW.model.ProgressEvent;
import org.rdftocsvconverter.RDFtoCSVW.service.ProgressEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for ProgressController.
 * 
 * This test demonstrates how to observe SSE emissions in integration tests
 * by using MockMvc's async support and capturing streamed responses.
 */
@SpringBootTest(classes = RDFToCSVWApiApplication.class)
@AutoConfigureMockMvc
class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProgressEventPublisher progressEventPublisher;

    /**
     * Test that SSE endpoint returns correct content type.
     */
    @Test
    @org.junit.jupiter.api.Disabled("SSE integration test - hangs test suite, run separately")
    void testSseEndpointContentType() throws Exception {
        String sessionId = "test-session-1";

        mockMvc.perform(get("/api/progress/stream/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }

    /**
     * Test SSE endpoint with async result.
     * 
     * This test shows how to capture the async response which contains
     * the SSE emitter.
     */
    @Test
    @org.junit.jupiter.api.Disabled("SSE integration test - hangs test suite, run separately")
    void testSseEndpointAsync() throws Exception {
        String sessionId = "test-session-2";

        MvcResult result = mockMvc.perform(get("/api/progress/stream/{sessionId}", sessionId))
                .andExpect(request().asyncStarted())
                .andReturn();

        assertNotNull(result.getAsyncResult(), "Async result should not be null");
    }

    /**
     * Test publishing events through the SSE connection.
     * 
     * This test demonstrates the full flow:
     * 1. Client connects to SSE endpoint
     * 2. Server publishes progress events
     * 3. Events are received by the client
     */
    @Test
    @org.junit.jupiter.api.Disabled("SSE integration test - hangs test suite, run separately")
    void testPublishEventsThroughSse() throws Exception {
        String sessionId = "test-session-3";

        // Start the SSE connection in a separate thread
        CountDownLatch sseStarted = new CountDownLatch(1);
        AtomicReference<MvcResult> sseResult = new AtomicReference<>();

        Thread sseThread = new Thread(() -> {
            try {
                MvcResult result = mockMvc.perform(get("/api/progress/stream/{sessionId}", sessionId))
                        .andExpect(request().asyncStarted())
                        .andReturn();
                
                sseResult.set(result);
                sseStarted.countDown();

                // Keep the connection open for a bit
                Thread.sleep(2000);

            } catch (Exception e) {
                fail("SSE connection failed: " + e.getMessage());
            }
        });
        sseThread.start();

        // Wait for SSE connection to be established
        assertTrue(sseStarted.await(5, TimeUnit.SECONDS), "SSE connection should start");

        // Give it a moment to register
        Thread.sleep(100);

        // Now publish some events
        progressEventPublisher.publishProgress(
            new ProgressEvent("PARSING", 0, "Starting parsing", sessionId)
        );
        progressEventPublisher.publishProgress(
            new ProgressEvent("PARSING", 50, "Halfway through parsing", sessionId)
        );
        progressEventPublisher.publishProgress(
            new ProgressEvent("PARSING", 100, "Parsing complete", sessionId)
        );

        // Complete the session
        Thread.sleep(100);
        progressEventPublisher.completeSession(sessionId);

        // Wait for thread to finish
        sseThread.join(3000);

        // The async result should be the SseEmitter
        assertNotNull(sseResult.get(), "SSE result should be set");
    }

    /**
     * Test multiple clients connecting to different sessions.
     */
    @Test
    @org.junit.jupiter.api.Disabled("SSE integration test - hangs test suite, run separately")
    void testMultipleSessions() throws Exception {
        String session1 = "session-1";
        String session2 = "session-2";

        // Connect both sessions
        MvcResult result1 = mockMvc.perform(get("/api/progress/stream/{sessionId}", session1))
                .andExpect(request().asyncStarted())
                .andReturn();

        MvcResult result2 = mockMvc.perform(get("/api/progress/stream/{sessionId}", session2))
                .andExpect(request().asyncStarted())
                .andReturn();

        assertNotNull(result1.getAsyncResult());
        assertNotNull(result2.getAsyncResult());

        // Publish events to different sessions
        progressEventPublisher.publishProgress(
            new ProgressEvent("PARSING", 100, "Session 1 event", session1)
        );
        progressEventPublisher.publishProgress(
            new ProgressEvent("CONVERTING", 100, "Session 2 event", session2)
        );

        // Both sessions should handle their events independently
        // (This is verified in the ProgressEventPublisher tests)
    }

    /**
     * Test that session ID parameter is required.
     */
    @Test
    @org.junit.jupiter.api.Disabled("SSE integration test - hangs test suite, run separately")
    void testSessionIdRequired() throws Exception {
        // This should work fine - sessionId is a path variable, so it's required
        mockMvc.perform(get("/api/progress/stream/{sessionId}", "test-id"))
                .andExpect(status().isOk());
    }

    /**
     * Test CORS headers are set correctly.
     */
    @Test
    @org.junit.jupiter.api.Disabled("SSE integration test - hangs test suite, run separately")
    void testCorsHeaders() throws Exception {
        String sessionId = "test-session-cors";

        mockMvc.perform(get("/api/progress/stream/{sessionId}", sessionId)
                .header("Origin", "http://localhost:4000"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    /**
     * Integration test: Full flow from connection to completion.
     * 
     * This test demonstrates how to observe network activity by:
     * 1. Setting up SSE connection
     * 2. Publishing events
     * 3. Verifying the connection is managed correctly
     */
    @Test
    @org.junit.jupiter.api.Disabled("SSE integration test - hangs test suite, run separately")
    void testFullSseFlow() throws Exception {
        String sessionId = "integration-test-session";

        // Set up SSE connection
        MvcResult result = mockMvc.perform(get("/api/progress/stream/{sessionId}", sessionId))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        assertNotNull(result.getAsyncResult(), "SSE emitter should be created");

        // Simulate progress events being published
        ProgressEvent event1 = new ProgressEvent("PARSING", 0, "Starting", sessionId);
        ProgressEvent event2 = new ProgressEvent("PARSING", 50, "In progress", sessionId);
        ProgressEvent event3 = new ProgressEvent("PARSING", 100, "Complete", sessionId);

        // Publish events (in real scenario, these would come from the conversion process)
        progressEventPublisher.publishProgress(event1);
        Thread.sleep(100);
        progressEventPublisher.publishProgress(event2);
        Thread.sleep(100);
        progressEventPublisher.publishProgress(event3);
        Thread.sleep(100);

        // Complete the session
        progressEventPublisher.completeSession(sessionId);

        // The emitter should be removed from the publisher's registry
        // (We can't easily verify this without exposing internal state,
        // but we can verify that subsequent publishes don't cause errors)
        assertDoesNotThrow(() -> {
            progressEventPublisher.publishProgress(
                new ProgressEvent("PARSING", 100, "Should be ignored", sessionId)
            );
        });
    }

    /**
     * Test error handling when session errors occur.
     */
    @Test
    @org.junit.jupiter.api.Disabled("SSE integration test - hangs test suite, run separately")
    void testSessionError() throws Exception {
        String sessionId = "error-test-session";

        MvcResult result = mockMvc.perform(get("/api/progress/stream/{sessionId}", sessionId))
                .andExpect(request().asyncStarted())
                .andReturn();

        assertNotNull(result.getAsyncResult());

        // Simulate an error
        RuntimeException testError = new RuntimeException("Test error");
        
        assertDoesNotThrow(() -> {
            progressEventPublisher.errorSession(sessionId, testError);
        });
    }
}
