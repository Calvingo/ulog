package com.ulog.backend.compliance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReportRequest {

    private Long reportedUserId;

    @NotNull(message = "Report type is required")
    @NotBlank(message = "Report type cannot be blank")
    private String reportType; // inappropriate_content, violation, harassment, spam, other

    private String reportCategory;

    @NotNull(message = "Content is required")
    @NotBlank(message = "Content cannot be blank")
    private String content;

    private String evidence; // JSON format with screenshot URLs, etc.

    public ReportRequest() {
    }

    public ReportRequest(Long reportedUserId, String reportType, String reportCategory, 
                        String content, String evidence) {
        this.reportedUserId = reportedUserId;
        this.reportType = reportType;
        this.reportCategory = reportCategory;
        this.content = content;
        this.evidence = evidence;
    }

    // Getters and Setters
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

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }
}

