package org.rdftocsvconverter.RDFtoCSVW.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for GlobalExceptionHandler - simplified to avoid Mockito compatibility issues with Java 25.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void testHandleAllExceptions() {
        // Given
        Exception exception = new RuntimeException("Unexpected error occurred");

        // When
        ResponseEntity<?> response = exceptionHandler.handleAllExceptions(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertEquals("Unexpected error", errorBody.get("error"));
        assertEquals("Unexpected error occurred", errorBody.get("message"));
    }

    @Test
    void testHandleAllExceptionsWithNullMessage() {
        // Given
        Exception exception = new RuntimeException();

        // When
        ResponseEntity<?> response = exceptionHandler.handleAllExceptions(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertEquals("Unexpected error", errorBody.get("error"));
        // RuntimeException() with no message returns null for getMessage()
        assertNull(errorBody.get("message"));
    }

    @Test
    void testHandleAllExceptionsWithCustomException() {
        // Given
        Exception exception = new IllegalArgumentException("Invalid input provided");

        // When
        ResponseEntity<?> response = exceptionHandler.handleAllExceptions(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertEquals("Unexpected error", errorBody.get("error"));
        assertEquals("Invalid input provided", errorBody.get("message"));
    }
}
