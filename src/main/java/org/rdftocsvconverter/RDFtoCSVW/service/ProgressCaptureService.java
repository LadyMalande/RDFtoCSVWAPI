package org.rdftocsvconverter.RDFtoCSVW.service;

import org.rdftocsvconverter.RDFtoCSVW.model.ProgressEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for capturing progress logs from the RDFtoCSV library.
 * 
 * This service intercepts System.out to capture [PROGRESS] messages
 * and publishes them as ProgressEvents.
 */
@Service
public class ProgressCaptureService {
    
    @Autowired
    private ProgressEventPublisher progressEventPublisher;
    
    // Pattern to match: [PROGRESS] stage=PARSING progress=100% message=Completed Parsing RDF file
    private static final Pattern PROGRESS_PATTERN = Pattern.compile(
        "\\[PROGRESS\\]\\s+stage=(\\w+)\\s+progress=(\\d+)%\\s+message=(.+)"
    );
    
    /**
     * Execute a task while capturing progress output.
     * 
     * @param sessionId the session identifier for this conversion
     * @param task the task to execute
     * @param <T> the return type
     * @return the result of the task
     * @throws Exception if the task throws an exception
     */
    public <T> T executeWithProgressCapture(String sessionId, ThrowingSupplier<T> task) throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captureStream = new ByteArrayOutputStream();
        PrintStream captureOut = new PrintStream(captureStream) {
            @Override
            public void println(String x) {
                super.println(x);
                // Check if this is a progress message
                if (x != null && x.contains("[PROGRESS]")) {
                    parseAndPublishProgress(x, sessionId);
                }
                // Also write to original output
                originalOut.println(x);
            }
        };
        
        try {
            System.setOut(captureOut);
            return task.get();
        } finally {
            System.setOut(originalOut);
            captureOut.close();
        }
    }
    
    /**
     * Parse a progress log line and publish it as a ProgressEvent.
     * 
     * @param logLine the log line to parse
     * @param sessionId the session identifier
     */
    private void parseAndPublishProgress(String logLine, String sessionId) {
        Matcher matcher = PROGRESS_PATTERN.matcher(logLine);
        if (matcher.find()) {
            String stage = matcher.group(1);
            int progress = Integer.parseInt(matcher.group(2));
            String message = matcher.group(3);
            
            ProgressEvent event = new ProgressEvent(stage, progress, message, sessionId);
            progressEventPublisher.publishProgress(event);
            
            System.err.println("Published progress: " + event);
        }
    }
    
    /**
     * Functional interface for suppliers that can throw exceptions.
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
