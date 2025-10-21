package com.ulog.backend.conversation.event;

/**
 * 联系人创建事件
 * 当创建联系人时发布此事件，用于触发 self value 计算
 */
public class ContactCreatedEvent {
    
    private final Long contactId;
    private final String description;

    public ContactCreatedEvent(Long contactId, String description) {
        this.contactId = contactId;
        this.description = description;
    }

    public Long getContactId() {
        return contactId;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ContactCreatedEvent{" +
                "contactId=" + contactId +
                ", description='" + description + '\'' +
                '}';
    }
}

