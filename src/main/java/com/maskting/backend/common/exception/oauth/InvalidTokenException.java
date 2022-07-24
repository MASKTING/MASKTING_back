package com.maskting.backend.common.exception.oauth;

import com.maskting.backend.common.Messages;
import com.maskting.backend.common.exception.MasktingException;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends MasktingException {
    public InvalidTokenException() {
        super(Messages.INVALID_TOKEN, HttpStatus.UNAUTHORIZED);
    }
}
