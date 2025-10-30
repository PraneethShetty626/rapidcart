package com.rapidcart.product_service.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the RapidCart Product Service.
 *
 * <p>This class provides centralized exception handling for all REST controllers,
 * converting Java exceptions into well-structured JSON responses with consistent
 * formats, HTTP status codes, and timestamps.</p>
 *
 * <p>Each exception type is mapped to a specific HTTP status code
 * and includes optional validation or database error details.</p>
 *
 * <p>Example error response:</p>
 * <pre>
 * {
 *   "timestamp": "2025-10-30T12:00:00",
 *   "status": 400,
 *   "error": "Validation failed",
 *   "details": {
 *     "name": "Product name is required",
 *     "price": "Price must be positive"
 *   }
 * }
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Builds a standardized error response.
     *
     * @param status  HTTP status code to return
     * @param error   a short error type/summary
     * @param message a descriptive message (optional)
     * @param details additional contextual data or validation errors (optional)
     * @return a {@link ResponseEntity} containing the response map
     */
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String error,
            String message,
            Object details
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        if (message != null) body.put("message", message);
        if (details != null) body.put("details", details);
        return new ResponseEntity<>(body, status);
    }

    /**
     * Handles validation errors triggered by {@code @Valid} annotations in request DTOs.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (v1, v2) -> v1
                ));
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", null, fieldErrors);
    }

    /**
     * Handles validation constraint violations (e.g., {@code @Min}, {@code @Max}, etc.).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        var details = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.toList());
        return buildResponse(HttpStatus.BAD_REQUEST, "Constraint violation", null, details);
    }

    /**
     * Handles custom resource not found exceptions.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), null);
    }

    /**
     * Handles bad input or illegal arguments.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad request", ex.getMessage(), null);
    }

    /**
     * Handles concurrent update conflicts caused by optimistic locking.
     */
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockException(OptimisticLockException ex) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Concurrent update error",
                "The resource was modified by another transaction. Please refresh and try again.",
                null
        );
    }

    /**
     * Handles database constraint violations (e.g., unique constraint, foreign key violation).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = "Data integrity violation";
        String detail = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();

        if (detail != null) {
            String lowerDetail = detail.toLowerCase();
            if (lowerDetail.contains("duplicate key value") || lowerDetail.contains("unique constraint")) {
                message = "Duplicate value error";
                detail = extractConstraintDetail(detail);
            }
        }

        return buildResponse(HttpStatus.CONFLICT, message, detail, null);
    }

    /**
     * Extracts a concise constraint message from a raw SQL exception message.
     * <p>Supports PostgreSQL and MySQL error message formats.</p>
     *
     * @param detail the raw database error message
     * @return a cleaner, user-friendly constraint message
     */
    private String extractConstraintDetail(String detail) {
        if (detail == null) return null;

        int idx = detail.indexOf("Detail:");
        if (idx >= 0) {
            return detail.substring(idx + 7).trim();
        }

        if (detail.contains("Duplicate entry")) {
            return detail.substring(detail.indexOf("Duplicate entry")).trim();
        }

        return detail;
    }

    /**
     * Handles all unexpected exceptions that do not have a specific handler.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), null);
    }
}
