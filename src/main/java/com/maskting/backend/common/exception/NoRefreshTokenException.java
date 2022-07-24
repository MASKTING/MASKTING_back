package com.maskting.backend.common.exception;

import com.maskting.backend.common.Messages;
import org.springframework.http.HttpStatus;

public class NoRefreshTokenException extends MasktingException {
    public NoRefreshTokenException() {
        super(Messages.NO_REFRESH_TOKEN, HttpStatus.BAD_REQUEST);
    }
}
