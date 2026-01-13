package com.nines.nutsfact.api.v1.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String status;
    private int code;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private List<FieldError> errors;

    @Data
    @Builder
    public static class FieldError {
        private String field;
        private String message;
    }
}
