package com.ulog.backend.common.exception;

import com.ulog.backend.common.api.ErrorCode;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(ErrorCode.AUTH_UNAUTHORIZED, message);
    }

    public UnauthorizedException() {
        super(ErrorCode.AUTH_UNAUTHORIZED);
    }
}
