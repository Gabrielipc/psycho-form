package com.uam.psychoform.dto;

import java.util.UUID;

public record ApiResponse<T>(boolean success, T data, String message, ApiError error, String correlationId) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, "Operacion completada", null, UUID.randomUUID().toString());
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, null, null, new ApiError(code, message), UUID.randomUUID().toString());
    }

    public static ApiResponse<Void> error(String code, String message, java.util.List<String> details) {
        return new ApiResponse<>(false, null, null, new ApiError(code, message, details), UUID.randomUUID().toString());
    }
}

