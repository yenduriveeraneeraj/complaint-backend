package com.example.complaintmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when business logic is violated.
 * Returns HTTP 400 Bad Request.
 * Examples:
 *   - Email already registered
 *   - Feedback already submitted
 *   - Cannot submit feedback before complaint is resolved
 *   - Passwords don't match
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
