package com.ulog.backend.compliance.dto;

import com.ulog.backend.compliance.enums.ReportTarget;
import com.ulog.backend.compliance.enums.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 举报请求DTO
 * 用于AI内容反馈和系统问题报告
 */
public class ReportRequest {

    @NotNull(message = "举报类型不能为空")
    private ReportType reportType;

    @NotNull(message = "举报目标类型不能为空")
    private ReportTarget targetType;

    @Size(max = 255, message = "目标ID长度不能超过255")
    private String targetId;

    @NotBlank(message = "问题描述不能为空")
    @Size(max = 2000, message = "问题描述长度不能超过2000字符")
    private String description;

    private String context; // JSON格式的上下文信息

    private String evidenceUrls; // JSON数组格式的证据URLs

    public ReportRequest() {
    }

    public ReportRequest(ReportType reportType, ReportTarget targetType, 
                        String targetId, String description, 
                        String context, String evidenceUrls) {
        this.reportType = reportType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.description = description;
        this.context = context;
        this.evidenceUrls = evidenceUrls;
    }

    // Getters and Setters
    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public ReportTarget getTargetType() {
        return targetType;
    }

    public void setTargetType(ReportTarget targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getEvidenceUrls() {
        return evidenceUrls;
    }

    public void setEvidenceUrls(String evidenceUrls) {
        this.evidenceUrls = evidenceUrls;
    }
}

