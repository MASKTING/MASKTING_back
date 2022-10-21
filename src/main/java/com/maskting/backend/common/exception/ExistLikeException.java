package com.maskting.backend.common.exception;

import com.maskting.backend.common.Messages;
import org.springframework.http.HttpStatus;

public class ExistLikeException extends MasktingException {
    public ExistLikeException() {
        super(Messages.EXIST_LIKE, HttpStatus.BAD_REQUEST);
    }
}
