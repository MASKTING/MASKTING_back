package com.maskting.backend.common.exception;

import com.maskting.backend.common.Messages;
import org.springframework.http.HttpStatus;

public class NoCertificationException extends MasktingException {
    public NoCertificationException() {
        super(Messages.NO_CERTIFICATION, HttpStatus.BAD_REQUEST);
    }
}
