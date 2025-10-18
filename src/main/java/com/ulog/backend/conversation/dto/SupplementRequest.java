package com.ulog.backend.conversation.dto;

import jakarta.validation.constraints.NotBlank;

public class SupplementRequest {
    
    @NotBlank(message = "补充信息不能为空")
    private String supplementInfo;
    
    public SupplementRequest() {
    }
    
    public SupplementRequest(String supplementInfo) {
        this.supplementInfo = supplementInfo;
    }
    
    public String getSupplementInfo() {
        return supplementInfo;
    }
    
    public void setSupplementInfo(String supplementInfo) {
        this.supplementInfo = supplementInfo;
    }
}
