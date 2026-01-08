package org.rdftocsvconverter.RDFtoCSVW.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the RDFtoCSVW API.
 * Provides centralized exception handling for all controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exceptions when method argument type conversion fails.
     * Typically occurs when enum values are provided incorrectly.
     *
     * @param ex the exception thrown during type conversion
     * @return ResponseEntity with error details and BAD_REQUEST status
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleEnumConversionException(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String type = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";
        String message = String.format("Parameter '%s' should be of type %s. Provided value: '%s'.", name, type, value);

        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid parameter");
        error.put("message", message);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Catch-all handler for any unhandled exceptions.
     * Provides a generic error response for unexpected errors.
     *
     * @param ex the exception that was thrown
     * @return ResponseEntity with error details and INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Unexpected error");
        error.put("message", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}