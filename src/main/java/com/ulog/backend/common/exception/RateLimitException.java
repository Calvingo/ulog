package com.ulog.backend.common.exception;

import com.ulog.backend.common.api.ErrorCode;

public class RateLimitException extends ApiException {
    public RateLimitException() {
        super(ErrorCode.RATE_LIMITED);
    }

    public RateLimitException(String message) {
        super(ErrorCode.RATE_LIMITED, message);
    }
}
