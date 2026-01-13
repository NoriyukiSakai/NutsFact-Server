package com.nines.nutsfact.exception;

import org.springframework.http.HttpStatus;

public class ForeignKeyConstraintException extends ApiException {

    public ForeignKeyConstraintException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    public ForeignKeyConstraintException() {
        super("他のデータで使用されているため、この操作を実行できません", HttpStatus.CONFLICT);
    }
}
