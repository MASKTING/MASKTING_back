package com.maskting.backend.common.exception;

import com.maskting.backend.common.Messages;
import org.springframework.http.HttpStatus;

public class NoProfileException extends MasktingException {
    public NoProfileException() {
        super(Messages.NO_PROFILE, HttpStatus.BAD_REQUEST);
    }
}
