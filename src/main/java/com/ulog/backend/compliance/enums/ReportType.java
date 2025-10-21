package com.ulog.backend.compliance.enums;

/**
 * 举报类型枚举
 * 针对AI助手产品的举报场景
 */
public enum ReportType {
    AI_INAPPROPRIATE_CONTENT("AI生成不当内容", "AI输出了不当、违反伦理或不安全的建议"),
    AI_POOR_QUALITY("AI回答质量差", "AI回答答非所问、无意义或错误"),
    SYSTEM_BUG("系统错误", "功能异常、崩溃或显示错误"),
    OTHER("其他问题", "其他未分类的问题或建议");

    private final String displayName;
    private final String description;

    ReportType(String displayName, String description) {
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

