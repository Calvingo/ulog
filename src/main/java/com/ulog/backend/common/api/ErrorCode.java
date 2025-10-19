package com.ulog.backend.common.api;

public enum ErrorCode {
    SUCCESS(0, "ok"),

    BAD_REQUEST(1000, "invalid request"),
    VALIDATION_FAILED(1001, "validation failed"),
    RESOURCE_NOT_FOUND(1004, "resource not found"),
    SMS_CODE_INVALID(1005, "invalid verification code"),
    USER_ALREADY_EXISTS(1006, "user already exists"),
    RATE_LIMITED(1007, "too many requests"),

    AUTH_UNAUTHORIZED(2001, "unauthorized"),
    TOKEN_EXPIRED(2002, "token expired"),
    TOKEN_INVALID(2003, "invalid token"),
    LOGIN_FAILED(2004, "invalid credentials"),
    ACCOUNT_LOCKED(2005, "account locked"),
    FORBIDDEN(2006, "forbidden"),

    STORAGE_ERROR(4000, "storage error"),

    SERVER_ERROR(5000, "internal server error");

    private final int code;
    private final String defaultMessage;

    ErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
