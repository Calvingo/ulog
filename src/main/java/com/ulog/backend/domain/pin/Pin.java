package com.ulog.backend.domain.pin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "pins")
public class Pin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pin_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private PinSourceType sourceType;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "qa_index", nullable = false)
    private Integer qaIndex;

    @Column(name = "contact_id")
    private Long contactId;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "supplement_question", columnDefinition = "TEXT")
    private String supplementQuestion;

    @Column(name = "supplement_answer", columnDefinition = "TEXT")
    private String supplementAnswer;

    @Column(name = "needs_more_info")
    private Boolean needsMoreInfo;

    @Column(name = "context_info", columnDefinition = "TEXT")
    private String contextInfo;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "tags", length = 255)
    private String tags;

    @Column(name = "qa_timestamp", length = 50)
    private String qaTimestamp;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Pin() {
        this.needsMoreInfo = Boolean.FALSE;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Boolean getNeedsMoreInfo() {
        return needsMoreInfo;
    }

    public void setNeedsMoreInfo(Boolean needsMoreInfo) {
        this.needsMoreInfo = needsMoreInfo;
    }

    public String getContextInfo() {
        return contextInfo;
    }

    public void setContextInfo(String contextInfo) {
        this.contextInfo = contextInfo;
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

    public String getQaTimestamp() {
        return qaTimestamp;
    }

    public void setQaTimestamp(String qaTimestamp) {
        this.qaTimestamp = qaTimestamp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

