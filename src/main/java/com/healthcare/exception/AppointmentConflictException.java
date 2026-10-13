package com.healthcare.exception;

/**
 * Thrown when attempting to book an already occupied doctor slot. (HTTP 409 Conflict)
 */
public class AppointmentConflictException extends HealthcareException {
    public AppointmentConflictException(String message) {
        super(message, 409);
    }

    public AppointmentConflictException(String message, Throwable cause) {
        super(message, cause, 409);
    }
}
