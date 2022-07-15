package com.maskting.common.exception;

import com.maskting.common.Messages;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends MasktingException{
    public InvalidTokenException() {
        super(Messages.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
    }
}
