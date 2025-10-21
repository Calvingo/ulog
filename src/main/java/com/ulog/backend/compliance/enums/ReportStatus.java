package com.ulog.backend.compliance.enums;

/**
 * 举报状态枚举
 */
public enum ReportStatus {
    PENDING("待处理", "举报已提交，等待处理"),
    REVIEWING("处理中", "正在审核和处理"),
    RESOLVED("已处理", "举报已处理完成"),
    DISMISSED("已忽略", "举报无效或重复");

    private final String displayName;
    private final String description;

    ReportStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}

