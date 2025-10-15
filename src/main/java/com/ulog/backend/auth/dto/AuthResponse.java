package com.ulog.backend.auth.dto;

import com.ulog.backend.user.dto.UserResponse;

public record AuthResponse(UserResponse user, TokenResponse tokens) {
}
