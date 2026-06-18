package com.finflow.ai.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private List<String> errors;
    private T data;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .errors(new ArrayList<>())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation successful");
    }

    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .errors(errors != null ? errors : new ArrayList<>())
                .data(null)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(message, new ArrayList<>());
    }
}
