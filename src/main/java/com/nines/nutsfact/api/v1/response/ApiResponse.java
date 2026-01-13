package com.nines.nutsfact.api.v1.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {

    private String status;
    private T data;
    private String message;
    private Integer totalRecords;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .status("Success")
            .data(data)
            .build();
    }

    public static <T> ApiResponse<T> success(T data, int totalRecords) {
        return ApiResponse.<T>builder()
            .status("Success")
            .data(data)
            .totalRecords(totalRecords)
            .build();
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
            .status("NotFound")
            .message(message)
            .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .status("Error")
            .message(message)
            .build();
    }
}
