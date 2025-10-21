package com.ulog.backend.conversation.event;

/**
 * 用户描述更新事件
 * 当用户的 description 被更新时发布此事件，用于触发 self value 重新计算
 */
public class UserDescriptionUpdatedEvent {
    
    private final Long userId;
    private final String description;

    public UserDescriptionUpdatedEvent(Long userId, String description) {
        this.userId = userId;
        this.description = description;
    }

    public Long getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "UserDescriptionUpdatedEvent{" +
                "userId=" + userId +
                ", description='" + description + '\'' +
                '}';
    }
}

