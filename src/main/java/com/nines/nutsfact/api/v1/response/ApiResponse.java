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
    private T item;
    private String message;
    private Integer records;

    public static <T> ApiResponse<T> success(T item) {
        return ApiResponse.<T>builder()
            .status("Success")
            .item(item)
            .build();
    }

    public static <T> ApiResponse<T> success(T item, int records) {
        return ApiResponse.<T>builder()
            .status("Success")
            .item(item)
            .records(records)
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
