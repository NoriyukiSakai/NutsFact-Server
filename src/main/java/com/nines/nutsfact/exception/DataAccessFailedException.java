package com.nines.nutsfact.exception;

import org.springframework.http.HttpStatus;

public class DataAccessFailedException extends ApiException {

    public DataAccessFailedException(String operation) {
        super(String.format("データの%sに失敗しました", operation), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public DataAccessFailedException(String operation, Throwable cause) {
        super(String.format("データの%sに失敗しました", operation), cause, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
