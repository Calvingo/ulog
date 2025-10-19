package com.ulog.backend.ai.dto;

public class AiActionPlanItem {

    private String title;
    private String description;
    private Integer scheduledDays;

    public AiActionPlanItem() {
    }

    public AiActionPlanItem(String title, String description, Integer scheduledDays) {
        this.title = title;
        this.description = description;
        this.scheduledDays = scheduledDays;
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

    public Integer getScheduledDays() {
        return scheduledDays;
    }

    public void setScheduledDays(Integer scheduledDays) {
        this.scheduledDays = scheduledDays;
    }
}

