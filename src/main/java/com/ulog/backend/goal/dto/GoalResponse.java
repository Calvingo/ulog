package com.ulog.backend.goal.dto;

import com.ulog.backend.domain.goal.enums.GoalStatus;
import java.time.LocalDateTime;

public class GoalResponse {

    private Long goalId;
    private Long contactId;
    private String contactName;
    private String goalDescription;
    private String aiStrategy;
    private GoalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GoalResponse() {
    }

    public GoalResponse(Long goalId, Long contactId, String contactName, String goalDescription,
                        String aiStrategy, GoalStatus status, LocalDateTime createdAt, 
                        LocalDateTime updatedAt) {
        this.goalId = goalId;
        this.contactId = contactId;
        this.contactName = contactName;
        this.goalDescription = goalDescription;
        this.aiStrategy = aiStrategy;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getGoalDescription() {
        return goalDescription;
    }

    public void setGoalDescription(String goalDescription) {
        this.goalDescription = goalDescription;
    }

    public String getAiStrategy() {
        return aiStrategy;
    }

    public void setAiStrategy(String aiStrategy) {
        this.aiStrategy = aiStrategy;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

