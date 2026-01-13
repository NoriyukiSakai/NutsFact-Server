package com.nines.nutsfact.exception.handler;

import com.nines.nutsfact.api.v1.response.ErrorResponse;
import com.nines.nutsfact.exception.ApiException;
import com.nines.nutsfact.exception.AuthenticationException;
import com.nines.nutsfact.exception.DataAccessFailedException;
import com.nines.nutsfact.exception.EntityNotFoundException;
import com.nines.nutsfact.exception.ForeignKeyConstraintException;
import com.nines.nutsfact.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        log.error("API Exception: {}", ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
            .status("Error")
            .code(ex.getStatus().value())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .status("Unauthorized")
            .code(HttpStatus.UNAUTHORIZED.value())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
        log.warn("Entity not found: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .status("NotFound")
            .code(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .status("NotFound")
            .code(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ForeignKeyConstraintException.class)
    public ResponseEntity<ErrorResponse> handleForeignKeyConstraintException(ForeignKeyConstraintException ex, HttpServletRequest request) {
        log.warn("Foreign key constraint violation: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .status("Error")
            .code(HttpStatus.CONFLICT.value())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation: {}", ex.getMessage(), ex);

        String message = "データの整合性エラーが発生しました";
        if (ex.getCause() instanceof SQLException) {
            SQLException sqlEx = (SQLException) ex.getCause();
            if (sqlEx.getErrorCode() == 1451 || sqlEx.getErrorCode() == 1452) {
                message = "他のデータで使用されているため、この操作を実行できません";
            } else if (sqlEx.getErrorCode() == 1062) {
                message = "重複するデータが既に存在します";
            }
        }

        ErrorResponse response = ErrorResponse.builder()
            .status("Error")
            .code(HttpStatus.CONFLICT.value())
            .message(message)
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed: {}", ex.getMessage());

        BindingResult bindingResult = ex.getBindingResult();
        List<ErrorResponse.FieldError> fieldErrors = bindingResult.getFieldErrors().stream()
            .map(error -> ErrorResponse.FieldError.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .build())
            .collect(Collectors.toList());

        ErrorResponse response = ErrorResponse.builder()
            .status("ValidationError")
            .code(HttpStatus.BAD_REQUEST.value())
            .message("入力値が不正です")
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .errors(fieldErrors)
            .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
            .status("Error")
            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("予期しないエラーが発生しました")
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
