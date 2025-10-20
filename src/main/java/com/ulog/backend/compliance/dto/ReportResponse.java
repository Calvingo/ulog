package com.ulog.backend.compliance.dto;

import java.time.LocalDateTime;

public class ReportResponse {

    private Long id;
    private Long reporterId;
    private Long reportedUserId;
    private String reportType;
    private String reportCategory;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public ReportResponse() {
    }

    public ReportResponse(Long id, Long reporterId, Long reportedUserId, String reportType,
                         String reportCategory, String content, String status, 
                         LocalDateTime createdAt, LocalDateTime processedAt) {
        this.id = id;
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.reportType = reportType;
        this.reportCategory = reportCategory;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public Long getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(Long reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getReportCategory() {
        return reportCategory;
    }

    public void setReportCategory(String reportCategory) {
        this.reportCategory = reportCategory;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}

