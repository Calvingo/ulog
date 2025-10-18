package com.ulog.backend.conversation.enums;

public enum SessionStatus {
    // 收集阶段
    ACTIVE("收集中", "正常的信息收集流程"),
    REQUESTING_MINIMUM("请求必要信息", "用户想结束但信息不足"),
    CONFIRMING_END("确认结束", "询问用户是否结束收集"),
    
    // 完成阶段
    COMPLETED("收集完成", "信息收集完成，联系人已创建"),
    
    // 后续阶段
    QA_ACTIVE("问答模式", "可以问关于该联系人的问题"),
    
    // 终止阶段
    ABANDONED("已放弃", "用户放弃创建"),
    EXPIRED("已过期", "超过24小时未活动");
    
    private final String displayName;
    private final String description;
    
    SessionStatus(String displayName, String description) {
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

