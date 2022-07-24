package com.maskting.backend.common.exception;

import com.maskting.backend.common.Messages;
import org.springframework.http.HttpStatus;

public class InvalidProviderException extends MasktingException {
    public InvalidProviderException() {
        super(Messages.INVALID_PROVIDER, HttpStatus.BAD_REQUEST);
    }
}
