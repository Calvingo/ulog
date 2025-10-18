package com.ulog.backend.conversation.dto;

import java.util.List;

public class SupplementAnalysis {
    
    private boolean needsSupplement;
    private String reason;
    private List<String> supplementFields;
    private String supplementQuestion;
    
    public SupplementAnalysis() {
    }
    
    public SupplementAnalysis(boolean needsSupplement, String reason, List<String> supplementFields, String supplementQuestion) {
        this.needsSupplement = needsSupplement;
        this.reason = reason;
        this.supplementFields = supplementFields;
        this.supplementQuestion = supplementQuestion;
    }
    
    public boolean isNeedsSupplement() {
        return needsSupplement;
    }
    
    public void setNeedsSupplement(boolean needsSupplement) {
        this.needsSupplement = needsSupplement;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public List<String> getSupplementFields() {
        return supplementFields;
    }
    
    public void setSupplementFields(List<String> supplementFields) {
        this.supplementFields = supplementFields;
    }
    
    public String getSupplementQuestion() {
        return supplementQuestion;
    }
    
    public void setSupplementQuestion(String supplementQuestion) {
        this.supplementQuestion = supplementQuestion;
    }
}
