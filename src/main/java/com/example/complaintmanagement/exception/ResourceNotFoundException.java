package com.example.complaintmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource is not found in the database.
 * Returns HTTP 404 Not Found.
 * Example: Complaint ID 999 doesn't exist → throw new ResourceNotFoundException("Complaint", 999)
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with ID: " + id);
    }

    public ResourceNotFoundException(String resourceName, String field, String value) {
        super(resourceName + " not found with " + field + ": " + value);
    }
}
