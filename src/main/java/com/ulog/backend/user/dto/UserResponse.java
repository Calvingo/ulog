package com.ulog.backend.user.dto;

import java.time.LocalDateTime;

public record UserResponse(Long id, String phone, String name, String description, String aiSummary, Integer status, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
