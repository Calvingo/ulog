package com.ulog.backend.common.exception;

import com.ulog.backend.common.api.ErrorCode;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
}
