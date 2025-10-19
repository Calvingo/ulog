package com.ulog.backend.pin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 从QA历史中创建Pin的请求
 */
public class CreatePinRequest {

    @NotBlank(message = "sessionId is required")
    private String sessionId;

    @NotNull(message = "qaIndex is required")
    @Min(value = 0, message = "qaIndex must be >= 0")
    private Integer qaIndex;

    private String note;

    private String tags;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getQaIndex() {
        return qaIndex;
    }

    public void setQaIndex(Integer qaIndex) {
        this.qaIndex = qaIndex;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}

