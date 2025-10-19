package com.ulog.backend.pin.dto;

import com.ulog.backend.domain.pin.PinSourceType;
import java.time.LocalDateTime;
import java.util.List;

public class PinResponse {

    private Long pinId;
    private PinSourceType sourceType;
    private String sessionId;
    private Integer qaIndex;
    private Long contactId;
    private String contactName;
    private String question;
    private String answer;
    private String supplementQuestion;
    private String supplementAnswer;
    private Boolean hasSupplementInfo;
    private String note;
    private List<String> tags;
    private String qaTimestamp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PinResponse() {
    }

    public PinResponse(Long pinId, PinSourceType sourceType, String sessionId, Integer qaIndex,
                       Long contactId, String contactName, String question, String answer,
                       String supplementQuestion, String supplementAnswer, Boolean hasSupplementInfo,
                       String note, List<String> tags, String qaTimestamp,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.pinId = pinId;
        this.sourceType = sourceType;
        this.sessionId = sessionId;
        this.qaIndex = qaIndex;
        this.contactId = contactId;
        this.contactName = contactName;
        this.question = question;
        this.answer = answer;
        this.supplementQuestion = supplementQuestion;
        this.supplementAnswer = supplementAnswer;
        this.hasSupplementInfo = hasSupplementInfo;
        this.note = note;
        this.tags = tags;
        this.qaTimestamp = qaTimestamp;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getSupplementQuestion() {
        return supplementQuestion;
    }

    public void setSupplementQuestion(String supplementQuestion) {
        this.supplementQuestion = supplementQuestion;
    }

    public String getSupplementAnswer() {
        return supplementAnswer;
    }

    public void setSupplementAnswer(String supplementAnswer) {
        this.supplementAnswer = supplementAnswer;
    }

    public Boolean getHasSupplementInfo() {
        return hasSupplementInfo;
    }

    public void setHasSupplementInfo(Boolean hasSupplementInfo) {
        this.hasSupplementInfo = hasSupplementInfo;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getQaTimestamp() {
        return qaTimestamp;
    }

    public void setQaTimestamp(String qaTimestamp) {
        this.qaTimestamp = qaTimestamp;
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

