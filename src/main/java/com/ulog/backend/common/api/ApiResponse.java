package com.ulog.backend.common.api;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.MDC;

public record ApiResponse<T>(int code, String message, T data, String traceId, long ts) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getDefaultMessage(), data, resolveTraceId(), Instant.now().toEpochMilli());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(), message, data, resolveTraceId(), Instant.now().toEpochMilli());
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(errorCode.getCode(), message, null, resolveTraceId(), Instant.now().toEpochMilli());
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getDefaultMessage(), null, resolveTraceId(), Instant.now().toEpochMilli());
    }

    private static String resolveTraceId() {
        String traceId = MDC.get("traceId");
        if (Objects.nonNull(traceId) && !traceId.isBlank()) {
            return traceId;
        }
        traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        return traceId;
    }
}
