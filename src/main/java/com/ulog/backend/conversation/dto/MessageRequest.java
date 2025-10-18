package com.ulog.backend.conversation.dto;

import jakarta.validation.constraints.NotBlank;

public class MessageRequest {
    
    @NotBlank(message = "消息不能为空")
    private String message;
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}

