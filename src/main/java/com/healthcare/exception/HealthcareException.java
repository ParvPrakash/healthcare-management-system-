package com.healthcare.exception;

/**
 * Root checked exception for all domain and infrastructure failures in Healthcare Management System.
 */
public class HealthcareException extends Exception {
    private final int httpStatusCode;

    public HealthcareException(String message) {
        super(message);
        this.httpStatusCode = 500;
    }

    public HealthcareException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = 500;
    }

    public HealthcareException(String message, int httpStatusCode) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public HealthcareException(String message, Throwable cause, int httpStatusCode) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}
