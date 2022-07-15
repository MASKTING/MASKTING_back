package com.maskting.common.exception.oauth;

import com.maskting.common.Messages;
import com.maskting.common.exception.MasktingException;
import org.springframework.http.HttpStatus;

public class ProviderMissMatchException extends MasktingException {
    public ProviderMissMatchException() {
        super(Messages.MISS_MATCH_PROVIDER, HttpStatus.UNAUTHORIZED);
    }
}
