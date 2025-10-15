package com.ulog.backend.user.dto;

import jakarta.validation.constraints.Size;

public class UserUpdateRequest {

    @Size(min = 1, max = 64, message = "name length must be 1-64")
    private String name;

    @Size(max = 512, message = "description max length 512")
    private String description;

    @Size(max = 1024, message = "aiSummary max length 1024")
    private String aiSummary;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }
}
