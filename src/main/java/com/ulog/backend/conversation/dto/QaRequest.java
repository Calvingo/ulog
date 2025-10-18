package com.ulog.backend.conversation.dto;

import jakarta.validation.constraints.NotBlank;

public class QaRequest {
    
    @NotBlank(message = "问题不能为空")
    private String message;
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}

