/*
 * Copyright 2025, David Snyderman
 */
package com.photoSort.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Standardized API response wrapper.
 * Matches the response format specified in PhotoSpecification.md.
 *
 * Success format:
 * {
 *   "success": true,
 *   "data": { ... }
 * }
 *
 * Error format:
 * {
 *   "success": false,
 *   "error": {
 *     "code": "ERROR_CODE",
 *     "message": "Human readable error message"
 *   }
 * }
 *
 * @param <T> Type of data in successful response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorDetails error;

    /**
     * Error details for failed responses.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetails {
        private String code;
        private String message;
    }

    /**
     * Factory method for successful response.
     *
     * @param data Response data
     * @param <T> Type of data
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * Factory method for error response.
     *
     * @param code Error code
     * @param message Error message
     * @param <T> Type of data (will be null)
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorDetails(code, message));
    }
}
