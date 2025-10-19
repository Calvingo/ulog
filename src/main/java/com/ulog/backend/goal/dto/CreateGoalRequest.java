package com.ulog.backend.goal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateGoalRequest {

    @NotNull(message = "contactId is required")
    private Long contactId;

    @NotBlank(message = "goalDescription is required")
    private String goalDescription;

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public String getGoalDescription() {
        return goalDescription;
    }

    public void setGoalDescription(String goalDescription) {
        this.goalDescription = goalDescription;
    }
}

