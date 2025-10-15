package com.ulog.backend.auth.dto;

public record TokenResponse(String tokenType, String accessToken, long expiresIn, String refreshToken, long refreshExpiresIn) {
}
