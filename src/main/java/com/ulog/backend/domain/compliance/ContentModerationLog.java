package com.ulog.backend.domain.compliance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "content_moderation_log")
public class ContentModerationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "moderation_result", nullable = false, length = 20)
    private String moderationResult;

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    @Column(name = "risk_details", columnDefinition = "TEXT")
    private String riskDetails;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ContentModerationLog() {
        this.createdAt = LocalDateTime.now();
    }

    public ContentModerationLog(Long userId, String contentType, String content, 
                               String moderationResult, String riskLevel, String provider) {
        this.userId = userId;
        this.contentType = contentType;
        this.content = content;
        this.moderationResult = moderationResult;
        this.riskLevel = riskLevel;
        this.provider = provider;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getModerationResult() {
        return moderationResult;
    }

    public void setModerationResult(String moderationResult) {
        this.moderationResult = moderationResult;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskDetails() {
        return riskDetails;
    }

    public void setRiskDetails(String riskDetails) {
        this.riskDetails = riskDetails;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

