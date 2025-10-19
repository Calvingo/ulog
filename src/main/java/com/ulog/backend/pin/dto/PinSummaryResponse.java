package com.ulog.backend.pin.dto;

import com.ulog.backend.domain.pin.PinSourceType;
import java.time.LocalDateTime;
import java.util.List;

public class PinSummaryResponse {

    private Long pinId;
    private PinSourceType sourceType;
    private Long contactId;
    private String contactName;
    private String questionPreview;
    private String answerPreview;
    private Boolean hasSupplementInfo;
    private List<String> tags;
    private LocalDateTime createdAt;

    public PinSummaryResponse() {
    }

    public PinSummaryResponse(Long pinId, PinSourceType sourceType, Long contactId,
                              String contactName, String questionPreview, String answerPreview,
                              Boolean hasSupplementInfo, List<String> tags, LocalDateTime createdAt) {
        this.pinId = pinId;
        this.sourceType = sourceType;
        this.contactId = contactId;
        this.contactName = contactName;
        this.questionPreview = questionPreview;
        this.answerPreview = answerPreview;
        this.hasSupplementInfo = hasSupplementInfo;
        this.tags = tags;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getPinId() {
        return pinId;
    }

    public void setPinId(Long pinId) {
        this.pinId = pinId;
    }

    public PinSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(PinSourceType sourceType) {
        this.sourceType = sourceType;
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

    public String getQuestionPreview() {
        return questionPreview;
    }

    public void setQuestionPreview(String questionPreview) {
        this.questionPreview = questionPreview;
    }

    public String getAnswerPreview() {
        return answerPreview;
    }

    public void setAnswerPreview(String answerPreview) {
        this.answerPreview = answerPreview;
    }

    public Boolean getHasSupplementInfo() {
        return hasSupplementInfo;
    }

    public void setHasSupplementInfo(Boolean hasSupplementInfo) {
        this.hasSupplementInfo = hasSupplementInfo;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

