package com.example.complaintmanagement.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — catches ALL exceptions in the app
 * and returns proper JSON error responses to the React frontend.
 *
 * @RestControllerAdvice = intercepts all @RestController exceptions
 *
 * Without this, Spring Boot would return HTML error pages.
 * With this, we return clean JSON like:
 * {
 *   "status": 400,
 *   "message": "Email already registered",
 *   "timestamp": "2026-10-05T10:00:00"
 * }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Resource Not Found (404) ─────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // ── Bad Request / Business Logic Error (400) ─────────────
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ── Validation Errors (400) — @Valid fields ───────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        ValidationErrorResponse error = new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed. Please check the highlighted fields.",
            LocalDateTime.now(),
            fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ── Wrong Email/Password (401) ────────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.value(),
            "Invalid email or password. Please try again.",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // ── Access Denied — wrong role (403) ─────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            "You do not have permission to access this resource.",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // ── File Too Large (413) ──────────────────────────────────
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleFileTooLarge(MaxUploadSizeExceededException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.PAYLOAD_TOO_LARGE.value(),
            "File size exceeds the maximum allowed limit of 10MB.",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    // ── Catch-all for unexpected errors (500) ─────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred. Please try again later.",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ── Inner Classes: Error Response shapes ─────────────────

    @Getter
    @Setter
    public static class ErrorResponse {
        private int status;
        private String message;
        private LocalDateTime timestamp;

        public ErrorResponse(int status, String message, LocalDateTime timestamp) {
            this.status = status;
            this.message = message;
            this.timestamp = timestamp;
        }
    }

    @Getter
    @Setter
    public static class ValidationErrorResponse extends ErrorResponse {
        private Map<String, String> fieldErrors;

        public ValidationErrorResponse(int status, String message,
                                        LocalDateTime timestamp, Map<String, String> fieldErrors) {
            super(status, message, timestamp);
            this.fieldErrors = fieldErrors;
        }
    }
}
