package com.ulog.backend.conversation.event;

/**
 * 联系人描述更新事件
 * 当联系人的 description 被更新时发布此事件，用于触发 self value 重新计算
 */
public class ContactDescriptionUpdatedEvent {
    
    private final Long contactId;
    private final String description;

    public ContactDescriptionUpdatedEvent(Long contactId, String description) {
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
        return "ContactDescriptionUpdatedEvent{" +
                "contactId=" + contactId +
                ", description='" + description + '\'' +
                '}';
    }
}

