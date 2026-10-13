package com.healthcare.util;

import java.io.Serializable;
import java.util.Map;

/**
 * Generic JSON Response DTO for AJAX/fetch calls.
 * Demonstrates Generics parameter <T> for payload type safety.
 *
 * @param <T> Payload data type
 */
public class ApiResponse<T> implements Serializable {
    private final boolean success;
    private final String message;
    private final T data;
    private final Map<String, String> errors;

    private ApiResponse(boolean success, String message, T data, Map<String, String> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null);
    }

    public static <T> ApiResponse<T> error(String message, Map<String, String> errors) {
        return new ApiResponse<>(false, message, null, errors);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
