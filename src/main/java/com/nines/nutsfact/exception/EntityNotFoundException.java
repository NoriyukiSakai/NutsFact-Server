package com.nines.nutsfact.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends ApiException {

    public EntityNotFoundException(String entityName, Object id) {
        super(String.format("%s (ID: %s) が見つかりません", entityName, id), HttpStatus.NOT_FOUND);
    }

    public EntityNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
