package com.maskting.backend.common.exception;

import com.maskting.backend.common.Messages;
import org.springframework.http.HttpStatus;

public class NoNicknameException extends MasktingException {
    public NoNicknameException() {
        super(Messages.NO_NICKNAME, HttpStatus.BAD_REQUEST);
    }
}
