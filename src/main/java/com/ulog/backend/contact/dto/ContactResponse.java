package com.ulog.backend.contact.dto;

import java.time.LocalDateTime;

public record ContactResponse(Long id, String name, String description, String aiSummary, String interactionSuggestions, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
