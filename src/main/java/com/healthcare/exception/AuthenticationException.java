package com.healthcare.exception;

/**
 * Thrown when credentials are invalid, accounts are locked, or sessions expire. (HTTP 401)
 */
public class AuthenticationException extends HealthcareException {
    public AuthenticationException(String message) {
        super(message, 401);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause, 401);
    }
}
