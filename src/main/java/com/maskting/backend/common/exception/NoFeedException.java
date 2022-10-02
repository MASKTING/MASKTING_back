package com.maskting.backend.common.exception;

import com.maskting.backend.common.Messages;
import org.springframework.http.HttpStatus;

public class NoFeedException extends MasktingException {
    public NoFeedException() {
        super(Messages.NO_FEED, HttpStatus.BAD_REQUEST);
    }
}
