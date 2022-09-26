package com.maskting.backend.common.exception;

import com.maskting.backend.common.Messages;
import org.springframework.http.HttpStatus;

public class ExceedFeedLimitException extends MasktingException {
    public ExceedFeedLimitException() {
        super(Messages.EXCEED_FEED_LIMIT, HttpStatus.BAD_REQUEST);
    }
}
