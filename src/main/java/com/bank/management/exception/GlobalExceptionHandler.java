package com.bank.management.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the Bank Management System
 * 
 * This class handles all uncaught exceptions across the application:
 * - RuntimeException: Business logic errors (400 Bad Request)
 * - Generic Exception: Unexpected system errors (500 Internal Server Error)
 * 
 * All exceptions are logged with full stack traces for debugging.
 * Returns user-friendly error messages in HTTP responses.
 * Applied globally to all REST controllers via @RestControllerAdvice.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle business logic exceptions (validation errors, not found, etc.)
     * Returns 400 Bad Request with the exception message
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        logger.error("Runtime exception occurred: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle unexpected system exceptions
     * Returns 500 Internal Server Error with generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return new ResponseEntity<>("An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}