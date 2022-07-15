package com.maskting.common.exception;

import org.springframework.http.HttpStatus;

public class MasktingException extends RuntimeException {
    private final HttpStatus status;

    public MasktingException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
