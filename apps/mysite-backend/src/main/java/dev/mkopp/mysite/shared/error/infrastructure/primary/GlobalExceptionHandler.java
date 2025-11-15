package dev.mkopp.mysite.shared.error.infrastructure.primary;

import java.net.URI;
import java.util.UUID;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Centralized exception handling for the entire application.
 * Maps exceptions to standardized API responses.
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - 1000)
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class) // @Valid request body validation errors
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());

        ApiError error = new ApiError(
                "VALIDATION_ERROR",
                "The request contains invalid values",
                HttpStatus.BAD_REQUEST);

        ex.getBindingResult().getAllErrors().forEach(e -> {
            String field = e instanceof FieldError ? ((FieldError) e).getField() : "unknown";
            error.addValidationError(field, e.getDefaultMessage());
        });

        String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        error.setInstance(URI.create(uri));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class) // e.g. updateUser(@NotNull Long id, @NotBlank String name) {
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());

        ApiError error = new ApiError(
                "VALIDATION_ERROR",
                "The request contains invalid values",
                HttpStatus.BAD_REQUEST);

        ex.getConstraintViolations().forEach(v -> {
            String field = v.getPropertyPath().toString();
            error.addValidationError(field, v.getMessage());
        });

        String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        error.setInstance(URI.create(uri));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Type mismatch: {}", ex.getMessage());

        String message = String.format("Parameter '%s' has an invalid value: '%s'",
                ex.getName(), ex.getValue());

        ApiError error = new ApiError(
                "INVALID_PARAMETER",
                "Invalid request parameter",
                HttpStatus.BAD_REQUEST);
        error.setDetail(message);

        // Instance: Path of the failed request
        String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        error.setInstance(URI.create(uri));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        ApiError error = new ApiError(
                "FORBIDDEN",
                "Access Denied",
                HttpStatus.FORBIDDEN);
        error.setDetail(ex.getMessage());

        String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        error.setInstance(URI.create(uri));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("Unhandled exception [{}]: {}", errorId, ex.getMessage(), ex);

        ApiError error = new ApiError(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error has occurred",
                HttpStatus.INTERNAL_SERVER_ERROR);
        error.setDetail("An internal server error has occurred. Error ID: " + errorId);

        String uri = ((ServletWebRequest) request).getRequest().getRequestURI();
        error.setInstance(URI.create(uri));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}