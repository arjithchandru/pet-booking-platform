package com.petcare.booking.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", Instant.now().toString());

        String rootMsg = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();

        if (rootMsg != null && rootMsg.contains("exclude_overlapping_bookings")) {
            error.put("status", HttpStatus.CONFLICT.value());
            error.put("error", "Conflict");
            error.put("message", "Concurrent booking conflict: This time slot was just booked by another user.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        if (rootMsg != null && (rootMsg.contains("email") || rootMsg.contains("unique") || rootMsg.contains("Key (email)"))) {
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("error", "Bad Request");
            error.put("message", "A record with this unique identifier already exists.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Bad Request");
        error.put("message", "Database constraint violation. Please check your request parameters.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", Instant.now().toString());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Bad Request");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", Instant.now().toString());
        error.put("status", HttpStatus.NOT_FOUND.value());
        error.put("error", "Not Found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", Instant.now().toString());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Validation Failed");

        String firstError = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Invalid request payload");

        error.put("message", firstError);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}