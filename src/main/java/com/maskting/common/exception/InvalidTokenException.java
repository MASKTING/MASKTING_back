package com.maskting.common.exception;

import com.maskting.common.Messages;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends MasktingException{
    public InvalidTokenException(String message, HttpStatus status) {
        super(Messages.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
    }
}
