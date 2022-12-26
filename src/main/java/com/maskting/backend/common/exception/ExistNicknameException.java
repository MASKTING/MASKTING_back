package com.maskting.backend.common.exception;

import com.maskting.backend.common.Messages;
import org.springframework.http.HttpStatus;

public class ExistNicknameException extends MasktingException {
    public ExistNicknameException() {
        super(Messages.EXIST_NICKNAME, HttpStatus.BAD_REQUEST);
    }
}
