package com.ulog.backend.goal.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateActionPlanAdoptionRequest {

    @NotNull(message = "isAdopted is required")
    private Boolean isAdopted;

    public Boolean getIsAdopted() {
        return isAdopted;
    }

    public void setIsAdopted(Boolean isAdopted) {
        this.isAdopted = isAdopted;
    }
}

