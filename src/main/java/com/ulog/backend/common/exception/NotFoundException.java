package com.ulog.backend.common.exception;

import com.ulog.backend.common.api.ErrorCode;

public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
