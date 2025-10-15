package com.ulog.backend.contact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ContactRequest {

    @NotBlank(message = "name is required")
    @Size(max = 128, message = "name max length 128")
    private String name;

    @Size(max = 1024, message = "description max length 1024")
    private String description;

    @Size(max = 2048, message = "aiSummary max length 2048")
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
