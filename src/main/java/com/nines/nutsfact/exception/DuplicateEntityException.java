package com.nines.nutsfact.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEntityException extends ApiException {

    public DuplicateEntityException(String entityName, String field, Object value) {
        super(String.format("%s (%s: %s) は既に存在します", entityName, field, value), HttpStatus.CONFLICT);
    }

    public DuplicateEntityException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
