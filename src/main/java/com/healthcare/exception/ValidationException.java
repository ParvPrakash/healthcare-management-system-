package com.healthcare.exception;

import java.util.Collections;
import java.util.Map;

/**
 * Thrown when form input fails business validation. (HTTP 400)
 */
public class ValidationException extends HealthcareException {
    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super(message, 400);
        this.fieldErrors = Collections.emptyMap();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, 400);
        this.fieldErrors = fieldErrors != null ? fieldErrors : Collections.emptyMap();
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
