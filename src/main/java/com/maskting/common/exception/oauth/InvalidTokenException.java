package com.maskting.common.exception.oauth;

import com.maskting.common.Messages;
import com.maskting.common.exception.MasktingException;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends MasktingException {
    public InvalidTokenException() {
        super(Messages.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
    }
}
