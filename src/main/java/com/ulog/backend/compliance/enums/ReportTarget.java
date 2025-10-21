package com.ulog.backend.compliance.enums;

/**
 * 举报目标类型枚举
 * 定义可举报的内容类型
 */
public enum ReportTarget {
    QA_RESPONSE("联系人QA回答", "联系人问答中的AI回答"),
    USER_QA_RESPONSE("用户自我QA回答", "用户自我问答中的AI回答"),
    AI_SUMMARY("AI总结", "AI生成的联系人或用户总结"),
    GOAL_STRATEGY("关系目标策略", "AI生成的关系目标策略和行动计划"),
    SYSTEM_FEATURE("系统功能", "整体功能或用户体验问题"),
    OTHER("其他", "其他未分类的内容");

    private final String displayName;
    private final String description;

    ReportTarget(String displayName, String description) {
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

