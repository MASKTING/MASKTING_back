package com.maskting.backend.common.exception.oauth;

import com.maskting.backend.common.Messages;
import com.maskting.backend.common.exception.MasktingException;
import org.springframework.http.HttpStatus;

public class ProviderMissMatchException extends MasktingException {
    public ProviderMissMatchException() {
        super(Messages.MISS_MATCH_PROVIDER, HttpStatus.UNAUTHORIZED);
    }
}
