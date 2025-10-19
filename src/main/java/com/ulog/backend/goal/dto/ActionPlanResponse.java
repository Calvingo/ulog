package com.ulog.backend.goal.dto;

import com.ulog.backend.domain.goal.enums.ActionPlanStatus;
import java.time.LocalDateTime;

public class ActionPlanResponse {

    private Long planId;
    private String title;
    private String description;
    private LocalDateTime scheduledTime;
    private Boolean isAdopted;
    private ActionPlanStatus status;
    private LocalDateTime completedAt;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ActionPlanResponse() {
    }

    public ActionPlanResponse(Long planId, String title, String description, 
                              LocalDateTime scheduledTime, Boolean isAdopted,
                              ActionPlanStatus status, LocalDateTime completedAt, 
                              Integer orderIndex, LocalDateTime createdAt, 
                              LocalDateTime updatedAt) {
        this.planId = planId;
        this.title = title;
        this.description = description;
        this.scheduledTime = scheduledTime;
        this.isAdopted = isAdopted;
        this.status = status;
        this.completedAt = completedAt;
        this.orderIndex = orderIndex;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Boolean getIsAdopted() {
        return isAdopted;
    }

    public void setIsAdopted(Boolean isAdopted) {
        this.isAdopted = isAdopted;
    }

    public ActionPlanStatus getStatus() {
        return status;
    }

    public void setStatus(ActionPlanStatus status) {
        this.status = status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
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

