package org.rdftocsvconverter.RDFtoCSVW.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.rdftocsvconverter.RDFtoCSVW.model.ProgressEvent;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ProgressCaptureService.
 * 
 * This test demonstrates how to verify that progress messages are
 * correctly parsed and published.
 */
class ProgressCaptureServiceTest {

    private ProgressCaptureService progressCaptureService;

    @Mock
    private ProgressEventPublisher mockPublisher;

    private PrintStream originalOut;
    private ByteArrayOutputStream capturedOutput;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        progressCaptureService = new ProgressCaptureService();
        
        // Inject the mock publisher using reflection or setter
        try {
            java.lang.reflect.Field field = ProgressCaptureService.class.getDeclaredField("progressEventPublisher");
            field.setAccessible(true);
            field.set(progressCaptureService, mockPublisher);
        } catch (Exception e) {
            fail("Failed to inject mock publisher: " + e.getMessage());
        }

        // Capture System.out for verification
        originalOut = System.out;
        capturedOutput = new ByteArrayOutputStream();
    }

    /**
     * Test that progress messages are correctly parsed and published.
     */
    @Test
    void testProgressMessageParsing() throws Exception {
        String sessionId = "test-session-1";
        
        // Execute a task that prints a progress message
        String result = progressCaptureService.executeWithProgressCapture(sessionId, () -> {
            System.out.println("[PROGRESS] stage=PARSING progress=100% message=Completed Parsing RDF file");
            return "Task completed";
        });

        assertEquals("Task completed", result, "Task should return expected result");

        // Verify that publishProgress was called
        ArgumentCaptor<ProgressEvent> eventCaptor = ArgumentCaptor.forClass(ProgressEvent.class);
        verify(mockPublisher, atLeastOnce()).publishProgress(eventCaptor.capture());

        // Verify the captured event
        ProgressEvent event = eventCaptor.getValue();
        assertEquals("PARSING", event.getStage());
        assertEquals(100, event.getProgress());
        assertEquals("Completed Parsing RDF file", event.getMessage());
        assertEquals(sessionId, event.getSessionId());
    }

    /**
     * Test parsing multiple progress messages.
     */
    @Test
    void testMultipleProgressMessages() throws Exception {
        String sessionId = "test-session-2";
        
        progressCaptureService.executeWithProgressCapture(sessionId, () -> {
            System.out.println("[PROGRESS] stage=PARSING progress=0% message=Starting Parsing");
            System.out.println("[PROGRESS] stage=PARSING progress=50% message=Halfway through");
            System.out.println("[PROGRESS] stage=PARSING progress=100% message=Completed");
            return null;
        });

        // Verify publishProgress was called 3 times
        ArgumentCaptor<ProgressEvent> eventCaptor = ArgumentCaptor.forClass(ProgressEvent.class);
        verify(mockPublisher, times(3)).publishProgress(eventCaptor.capture());

        // Verify all captured events
        var events = eventCaptor.getAllValues();
        assertEquals(3, events.size(), "Should capture 3 events");

        assertEquals(0, events.get(0).getProgress());
        assertEquals("Starting Parsing", events.get(0).getMessage());

        assertEquals(50, events.get(1).getProgress());
        assertEquals("Halfway through", events.get(1).getMessage());

        assertEquals(100, events.get(2).getProgress());
        assertEquals("Completed", events.get(2).getMessage());
    }

    /**
     * Test parsing different stages.
     */
    @Test
    void testDifferentStages() throws Exception {
        String sessionId = "test-session-3";
        
        progressCaptureService.executeWithProgressCapture(sessionId, () -> {
            System.out.println("[PROGRESS] stage=PARSING progress=100% message=Parsing complete");
            System.out.println("[PROGRESS] stage=CONVERTING progress=25% message=Converting to CSV");
            System.out.println("[PROGRESS] stage=WRITING progress=50% message=Writing output");
            return null;
        });

        ArgumentCaptor<ProgressEvent> eventCaptor = ArgumentCaptor.forClass(ProgressEvent.class);
        verify(mockPublisher, times(3)).publishProgress(eventCaptor.capture());

        var events = eventCaptor.getAllValues();
        assertEquals("PARSING", events.get(0).getStage());
        assertEquals("CONVERTING", events.get(1).getStage());
        assertEquals("WRITING", events.get(2).getStage());
    }

    /**
     * Test that non-progress messages are ignored.
     */
    @Test
    void testNonProgressMessagesIgnored() throws Exception {
        String sessionId = "test-session-4";
        
        progressCaptureService.executeWithProgressCapture(sessionId, () -> {
            System.out.println("Regular log message");
            System.out.println("[INFO] This is an info message");
            System.out.println("[PROGRESS] stage=PARSING progress=50% message=Valid progress");
            System.out.println("Another regular message");
            return null;
        });

        // Only the valid progress message should be published
        ArgumentCaptor<ProgressEvent> eventCaptor = ArgumentCaptor.forClass(ProgressEvent.class);
        verify(mockPublisher, times(1)).publishProgress(eventCaptor.capture());

        ProgressEvent event = eventCaptor.getValue();
        assertEquals("PARSING", event.getStage());
        assertEquals(50, event.getProgress());
    }

    /**
     * Test that malformed progress messages are handled gracefully.
     */
    @Test
    void testMalformedProgressMessages() throws Exception {
        String sessionId = "test-session-5";
        
        // This should not throw an exception
        assertDoesNotThrow(() -> {
            progressCaptureService.executeWithProgressCapture(sessionId, () -> {
                System.out.println("[PROGRESS] invalid format");
                System.out.println("[PROGRESS] stage=PARSING");
                System.out.println("[PROGRESS] stage=PARSING progress=notanumber% message=Test");
                return "OK";
            });
        });

        // No valid progress messages, so publishProgress should not be called
        verify(mockPublisher, never()).publishProgress(any());
    }

    /**
     * Test exception handling in the captured task.
     */
    @Test
    void testExceptionInTask() {
        String sessionId = "test-session-6";
        
        Exception exception = assertThrows(Exception.class, () -> {
            progressCaptureService.executeWithProgressCapture(sessionId, () -> {
                System.out.println("[PROGRESS] stage=PARSING progress=50% message=Before error");
                throw new RuntimeException("Task failed");
            });
        });

        assertTrue(exception.getMessage().contains("Task failed"));

        // Progress message before exception should still be published
        verify(mockPublisher, times(1)).publishProgress(any(ProgressEvent.class));
    }

    /**
     * Test that task return value is preserved.
     */
    @Test
    void testTaskReturnValue() throws Exception {
        String sessionId = "test-session-7";
        
        String result = progressCaptureService.executeWithProgressCapture(sessionId, () -> {
            System.out.println("[PROGRESS] stage=PARSING progress=100% message=Done");
            return "Expected Result";
        });

        assertEquals("Expected Result", result, "Should return task result");
    }

    /**
     * Test complex progress message with special characters.
     */
    @Test
    void testProgressMessageWithSpecialCharacters() throws Exception {
        String sessionId = "test-session-8";
        
        progressCaptureService.executeWithProgressCapture(sessionId, () -> {
            System.out.println("[PROGRESS] stage=PARSING progress=75% message=Processing file: test-data.ttl (100KB)");
            return null;
        });

        ArgumentCaptor<ProgressEvent> eventCaptor = ArgumentCaptor.forClass(ProgressEvent.class);
        verify(mockPublisher, times(1)).publishProgress(eventCaptor.capture());

        ProgressEvent event = eventCaptor.getValue();
        assertEquals("Processing file: test-data.ttl (100KB)", event.getMessage());
    }

    /**
     * Test that original System.out is restored after execution.
     */
    @Test
    void testSystemOutRestored() throws Exception {
        PrintStream before = System.out;
        
        progressCaptureService.executeWithProgressCapture("test", () -> {
            return "OK";
        });
        
        PrintStream after = System.out;
        
        assertSame(before, after, "System.out should be restored");
    }

    /**
     * Test concurrent execution with different sessions.
     */
    @Test
    void testConcurrentSessions() throws Exception {
        // This test verifies that session IDs are correctly isolated
        String session1 = "session-1";
        String session2 = "session-2";
        
        progressCaptureService.executeWithProgressCapture(session1, () -> {
            System.out.println("[PROGRESS] stage=PARSING progress=100% message=Session 1 complete");
            return null;
        });
        
        progressCaptureService.executeWithProgressCapture(session2, () -> {
            System.out.println("[PROGRESS] stage=CONVERTING progress=100% message=Session 2 complete");
            return null;
        });

        ArgumentCaptor<ProgressEvent> eventCaptor = ArgumentCaptor.forClass(ProgressEvent.class);
        verify(mockPublisher, times(2)).publishProgress(eventCaptor.capture());

        var events = eventCaptor.getAllValues();
        assertEquals(session1, events.get(0).getSessionId());
        assertEquals("Session 1 complete", events.get(0).getMessage());
        
        assertEquals(session2, events.get(1).getSessionId());
        assertEquals("Session 2 complete", events.get(1).getMessage());
    }
}
