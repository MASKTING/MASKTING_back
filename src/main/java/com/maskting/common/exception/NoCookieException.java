package com.maskting.common.exception;

import com.maskting.common.Messages;
import org.springframework.http.HttpStatus;

public class NoCookieException extends MasktingException {
    public NoCookieException() {
        super(Messages.NO_COOKIE, HttpStatus.BAD_REQUEST);
    }
}
