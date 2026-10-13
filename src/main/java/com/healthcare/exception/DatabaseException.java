package com.healthcare.exception;

/**
 * Thrown when low-level SQL or database transaction failures occur. (HTTP 500)
 */
public class DatabaseException extends HealthcareException {
    public DatabaseException(String message) {
        super(message, 500);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause, 500);
    }
}
