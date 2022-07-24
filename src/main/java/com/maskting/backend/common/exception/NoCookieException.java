package com.maskting.backend.common.exception;

import com.maskting.backend.common.Messages;
import org.springframework.http.HttpStatus;

public class NoCookieException extends MasktingException {
    public NoCookieException() {
        super(Messages.NO_COOKIE, HttpStatus.BAD_REQUEST);
    }
}
