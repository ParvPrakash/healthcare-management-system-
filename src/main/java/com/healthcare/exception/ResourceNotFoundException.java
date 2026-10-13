package com.healthcare.exception;

/**
 * Thrown when an entity (user, appointment, record) does not exist. (HTTP 404)
 */
public class ResourceNotFoundException extends HealthcareException {
    public ResourceNotFoundException(String message) {
        super(message, 404);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause, 404);
    }
}
