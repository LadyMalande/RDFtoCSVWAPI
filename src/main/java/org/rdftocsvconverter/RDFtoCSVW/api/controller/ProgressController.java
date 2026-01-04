package org.rdftocsvconverter.RDFtoCSVW.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.rdftocsvconverter.RDFtoCSVW.service.ProgressEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller for Server-Sent Events (SSE) progress updates.
 */
@Tag(name = "Progress Updates", description = "API for receiving real-time progress updates during RDF to CSV conversion")
@RestController
@RequestMapping("/api/progress")
public class ProgressController {
    
    @Autowired
    private ProgressEventPublisher progressEventPublisher;
    
    /**
     * SSE endpoint for receiving progress updates.
     * 
     * @param sessionId the session identifier to subscribe to
     * @return SSE emitter for progress events
     */
    @Operation(
        summary = "Subscribe to progress updates",
        description = "Establishes a Server-Sent Events (SSE) connection to receive real-time progress updates for a conversion session. " +
                     "The client should call this endpoint with the sessionId before starting the conversion, then initiate the conversion " +
                     "in a separate request using the same sessionId."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "SSE connection established successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid session ID")
    })
    @GetMapping(value = "/stream/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @CrossOrigin(origins = {"http://localhost:4000", "https://ladymalande.github.io/"})
    public SseEmitter streamProgress(
            @Parameter(description = "Unique session identifier for tracking conversion progress", required = true)
            @PathVariable String sessionId) {
        
        // Create SSE emitter with 30 minute timeout
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        
        // Register the emitter
        progressEventPublisher.addEmitter(sessionId, emitter);
        
        System.out.println("SSE connection established for session: " + sessionId);
        
        return emitter;
    }
}
