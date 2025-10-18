package com.ulog.backend.domain.conversation;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_sessions")
public class ConversationSession {
    
    @Id
    @Column(name = "session_id", length = 64)
    private String sessionId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "contact_id")
    private Long contactId;
    
    @Column(name = "contact_name", length = 128)
    private String contactName;
    
    @Column(name = "current_dimension", length = 50)
    private String currentDimension;
    
    @Column(name = "completed_dimensions", columnDefinition = "TEXT")
    private String completedDimensions;
    
    @Column(name = "collected_data", columnDefinition = "TEXT")
    private String collectedData;
    
    @Column(name = "conversation_history", columnDefinition = "TEXT")
    private String conversationHistory;
    
    @Column(name = "qa_history", columnDefinition = "TEXT")
    private String qaHistory;
    
    @Column(name = "final_description", columnDefinition = "TEXT")
    private String finalDescription;
    
    @Column(name = "status", length = 20)
    private String status = "ACTIVE";
    
    @Column(name = "last_question", columnDefinition = "TEXT")
    private String lastQuestion;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // Constructors
    public ConversationSession() {
    }
    
    public ConversationSession(String sessionId, Long userId, String contactName) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.contactName = contactName;
        this.status = "ACTIVE";
    }
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
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
    
    public String getCurrentDimension() {
        return currentDimension;
    }
    
    public void setCurrentDimension(String currentDimension) {
        this.currentDimension = currentDimension;
    }
    
    public String getCompletedDimensions() {
        return completedDimensions;
    }
    
    public void setCompletedDimensions(String completedDimensions) {
        this.completedDimensions = completedDimensions;
    }
    
    public String getCollectedData() {
        return collectedData;
    }
    
    public void setCollectedData(String collectedData) {
        this.collectedData = collectedData;
    }
    
    public String getConversationHistory() {
        return conversationHistory;
    }
    
    public void setConversationHistory(String conversationHistory) {
        this.conversationHistory = conversationHistory;
    }
    
    public String getQaHistory() {
        return qaHistory;
    }
    
    public void setQaHistory(String qaHistory) {
        this.qaHistory = qaHistory;
    }
    
    public String getFinalDescription() {
        return finalDescription;
    }
    
    public void setFinalDescription(String finalDescription) {
        this.finalDescription = finalDescription;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getLastQuestion() {
        return lastQuestion;
    }
    
    public void setLastQuestion(String lastQuestion) {
        this.lastQuestion = lastQuestion;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}

