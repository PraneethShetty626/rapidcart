package com.rapidcart.order_service.exception;


import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.amqp.AmqpException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * GlobalExceptionHandler provides centralized exception handling for all REST controllers
 * within the {@code com.rapidcart.order_service} package.
 * <p>
 * This class uses {@link RestControllerAdvice} to catch and process various exceptions
 * that may occur during request processing, converting them into consistent and meaningful
 * HTTP responses for the client.
 * </p>
 * <p>
 * It handles both framework-level exceptions (e.g., validation, database, and messaging errors)
 * and custom application exceptions (e.g., {@link ResourceNotFoundException},
 * {@link ProductNotFoundException}, and {@link InsufficientStockException}).
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *     <li>Centralized error response structure with timestamp, status, and message.</li>
 *     <li>Graceful handling of validation, database, and messaging exceptions.</li>
 *     <li>Improved client readability with user-friendly messages.</li>
 * </ul>
 *
 * @author
 * @version 1.0
 * @since 2025-10
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Builds a standardized error response with common metadata.
     *
     * @param status  the HTTP status to be returned (e.g., 400, 404, 500)
     * @param error   a short summary of the error type
     * @param message a descriptive message explaining the error (nullable)
     * @param details additional contextual data or field-level errors (nullable)
     * @return a {@link ResponseEntity} containing a structured JSON response body
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
     * Handles validation errors thrown when a request body fails {@code @Valid} checks.
     * <p>
     * Typically triggered when field constraints in DTOs (e.g., {@link jakarta.validation.constraints.NotNull},
     * {@link jakarta.validation.constraints.Size}) are violated.
     * </p>
     *
     * @param ex the exception thrown by the validation framework
     * @return a {@link ResponseEntity} containing details about invalid fields
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
     * Handles constraint violations triggered by validation annotations on method parameters.
     *
     * @param ex the {@link ConstraintViolationException} containing the violated constraints
     * @return a {@link ResponseEntity} listing all violated constraints and messages
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
     * Handles cases where a requested resource is not found in the system.
     * <p>
     * Typically used for entity lookups like orders or products that do not exist.
     * </p>
     *
     * @param ex the custom {@link ResourceNotFoundException}
     * @return a {@link ResponseEntity} with a 404 Not Found response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), null);
    }

    /**
     * Handles invalid or illegal arguments passed to methods or APIs.
     *
     * @param ex the {@link IllegalArgumentException} thrown by the application
     * @return a {@link ResponseEntity} with a 400 Bad Request response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad request", ex.getMessage(), null);
    }

    /**
     * Handles concurrent modification errors due to optimistic locking failures.
     * <p>
     * Commonly triggered when multiple transactions attempt to update the same resource simultaneously.
     * </p>
     *
     * @param ex the {@link OptimisticLockException}
     * @return a {@link ResponseEntity} with a 409 Conflict response
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
     * Handles database-level constraint violations, such as unique or foreign key violations.
     *
     * @param ex the {@link DataIntegrityViolationException} thrown by the database layer
     * @return a {@link ResponseEntity} describing the violated database constraint
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
     * Extracts a concise, user-friendly message from raw SQL exception details.
     * <p>
     * Supports PostgreSQL and MySQL-style error formats.
     * </p>
     *
     * @param detail the raw database exception message
     * @return a simplified version of the constraint violation message
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
     * Handles unexpected or unhandled exceptions within the application.
     *
     * @param ex the unexpected {@link Exception}
     * @return a generic 500 Internal Server Error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), null);
    }

    /**
     * Handles cases where a requested product cannot be found.
     *
     * @param ex the {@link ProductNotFoundException} indicating the missing product
     * @return a {@link ResponseEntity} with a 404 Not Found status
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotFoundException(ProductNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Product not found", ex.getMessage(), null);
    }

    /**
     * Handles scenarios where the requested product quantity exceeds available stock.
     *
     * @param ex the {@link InsufficientStockException} indicating insufficient stock
     * @return a {@link ResponseEntity} with a 400 Bad Request response
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStockException(InsufficientStockException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Insufficient stock", ex.getMessage(), null);
    }

    /**
     * Handles failures in inter-service communication via message brokers (e.g., RabbitMQ).
     *
     * @param ex the {@link AmqpException} representing a messaging or broker error
     * @return a {@link ResponseEntity} with a 500 Internal Server Error response
     */
    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<Map<String, Object>> handleNotificationEventException(AmqpException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Services Error", ex.getMessage(), null);
    }

    /**
     * Handles validation errors triggered by {@code @Valid} annotations in request DTOs.
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(HandlerMethodValidationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Request Validation failed ", null, ex.getMessage());
    }
}
