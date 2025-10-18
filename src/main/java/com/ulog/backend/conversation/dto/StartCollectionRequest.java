package com.ulog.backend.conversation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StartCollectionRequest {
    
    @NotBlank(message = "联系人姓名不能为空")
    @Size(max = 128, message = "姓名最大长度128字符")
    private String contactName;
    
    public String getContactName() {
        return contactName;
    }
    
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
}

