package com.ulog.backend.goal.dto;

import com.ulog.backend.domain.goal.enums.ReminderStatus;
import java.time.LocalDateTime;

public class ReminderResponse {

    private Long reminderId;
    private Long planId;
    private String planTitle;
    private String planDescription;
    private Long goalId;
    private String goalDescription;
    private Long contactId;
    private String contactName;
    private LocalDateTime remindTime;
    private ReminderStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    public ReminderResponse() {
    }

    public ReminderResponse(Long reminderId, Long planId, String planTitle, 
                            String planDescription, Long goalId, String goalDescription,
                            Long contactId, String contactName, LocalDateTime remindTime, 
                            ReminderStatus status, LocalDateTime sentAt, 
                            LocalDateTime createdAt) {
        this.reminderId = reminderId;
        this.planId = planId;
        this.planTitle = planTitle;
        this.planDescription = planDescription;
        this.goalId = goalId;
        this.goalDescription = goalDescription;
        this.contactId = contactId;
        this.contactName = contactName;
        this.remindTime = remindTime;
        this.status = status;
        this.sentAt = sentAt;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getReminderId() {
        return reminderId;
    }

    public void setReminderId(Long reminderId) {
        this.reminderId = reminderId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public String getPlanTitle() {
        return planTitle;
    }

    public void setPlanTitle(String planTitle) {
        this.planTitle = planTitle;
    }

    public String getPlanDescription() {
        return planDescription;
    }

    public void setPlanDescription(String planDescription) {
        this.planDescription = planDescription;
    }

    public Long getGoalId() {
        return goalId;
    }

    public void setGoalId(Long goalId) {
        this.goalId = goalId;
    }

    public String getGoalDescription() {
        return goalDescription;
    }

    public void setGoalDescription(String goalDescription) {
        this.goalDescription = goalDescription;
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

    public LocalDateTime getRemindTime() {
        return remindTime;
    }

    public void setRemindTime(LocalDateTime remindTime) {
        this.remindTime = remindTime;
    }

    public ReminderStatus getStatus() {
        return status;
    }

    public void setStatus(ReminderStatus status) {
        this.status = status;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

