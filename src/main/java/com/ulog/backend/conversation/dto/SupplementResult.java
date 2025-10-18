package com.ulog.backend.conversation.dto;

import java.util.Map;

public class SupplementResult {
    
    private String updatedDescription;
    private String originalQuestion;
    private Map<String, Object> supplementData;
    private boolean shouldUpdateUserDescription;
    
    public SupplementResult() {
    }
    
    public SupplementResult(String updatedDescription, String originalQuestion, Map<String, Object> supplementData, boolean shouldUpdateUserDescription) {
        this.updatedDescription = updatedDescription;
        this.originalQuestion = originalQuestion;
        this.supplementData = supplementData;
        this.shouldUpdateUserDescription = shouldUpdateUserDescription;
    }
    
    public String getUpdatedDescription() {
        return updatedDescription;
    }
    
    public void setUpdatedDescription(String updatedDescription) {
        this.updatedDescription = updatedDescription;
    }
    
    public String getOriginalQuestion() {
        return originalQuestion;
    }
    
    public void setOriginalQuestion(String originalQuestion) {
        this.originalQuestion = originalQuestion;
    }
    
    public Map<String, Object> getSupplementData() {
        return supplementData;
    }
    
    public void setSupplementData(Map<String, Object> supplementData) {
        this.supplementData = supplementData;
    }
    
    public boolean isShouldUpdateUserDescription() {
        return shouldUpdateUserDescription;
    }
    
    public void setShouldUpdateUserDescription(boolean shouldUpdateUserDescription) {
        this.shouldUpdateUserDescription = shouldUpdateUserDescription;
    }
}
