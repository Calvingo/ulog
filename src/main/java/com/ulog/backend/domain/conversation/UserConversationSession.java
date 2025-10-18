package com.ulog.backend.domain.conversation;

import com.ulog.backend.conversation.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_conversation_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConversationSession {
    
    @Id
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private SessionStatus status;
    
    @Column(name = "current_dimension", length = 100)
    private String currentDimension;
    
    @Column(name = "completed_dimensions", columnDefinition = "TEXT")
    private String completedDimensions;  // JSON: ["基本信息", "社会角色", ...]
    
    @Column(name = "collected_data", columnDefinition = "TEXT")
    private String collectedData;  // JSON: {"age": "30", "occupation": "PM", ...}
    
    @Column(name = "conversation_history", columnDefinition = "TEXT")
    private String conversationHistory;  // JSON: [{"question": "...", "user": "...", ...}]
    
    @Column(name = "qa_history", columnDefinition = "TEXT")
    private String qaHistory;  // JSON: [{"question": "...", "answer": "...", ...}]
    
    @Column(name = "final_description", columnDefinition = "TEXT")
    private String finalDescription;
    
    @Column(name = "last_question", columnDefinition = "TEXT")
    private String lastQuestion;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
    
    public SessionStatus getStatus() {
        return status;
    }
    
    public void setStatus(SessionStatus status) {
        this.status = status;
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
    
    public String getLastQuestion() {
        return lastQuestion;
    }
    
    public void setLastQuestion(String lastQuestion) {
        this.lastQuestion = lastQuestion;
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
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}

