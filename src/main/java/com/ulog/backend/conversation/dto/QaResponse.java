package com.ulog.backend.conversation.dto;

public class QaResponse {
    
    private String answer;
    private Long contactId;
    private Boolean needsMoreInfo;
    private String supplementQuestion;        // 新增：补充信息的问题
    private SupplementAnalysis analysis;     // 新增：分析结果
    private Boolean isSupplementAnswer;       // 新增：是否为补充信息后的回答
    
    public QaResponse() {
    }
    
    public QaResponse(String answer, Long contactId) {
        this.answer = answer;
        this.contactId = contactId;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final QaResponse response = new QaResponse();
        
        public Builder answer(String answer) {
            response.answer = answer;
            return this;
        }
        
        public Builder contactId(Long contactId) {
            response.contactId = contactId;
            return this;
        }
        
        public Builder needsMoreInfo(Boolean needsMoreInfo) {
            response.needsMoreInfo = needsMoreInfo;
            return this;
        }
        
        public Builder supplementQuestion(String supplementQuestion) {
            response.supplementQuestion = supplementQuestion;
            return this;
        }
        
        public Builder analysis(SupplementAnalysis analysis) {
            response.analysis = analysis;
            return this;
        }
        
        public Builder isSupplementAnswer(Boolean isSupplementAnswer) {
            response.isSupplementAnswer = isSupplementAnswer;
            return this;
        }
        
        public QaResponse build() {
            return response;
        }
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public Long getContactId() {
        return contactId;
    }
    
    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }
    
    public Boolean getNeedsMoreInfo() {
        return needsMoreInfo;
    }
    
    public void setNeedsMoreInfo(Boolean needsMoreInfo) {
        this.needsMoreInfo = needsMoreInfo;
    }
    
    public String getSupplementQuestion() {
        return supplementQuestion;
    }
    
    public void setSupplementQuestion(String supplementQuestion) {
        this.supplementQuestion = supplementQuestion;
    }
    
    public SupplementAnalysis getAnalysis() {
        return analysis;
    }
    
    public void setAnalysis(SupplementAnalysis analysis) {
        this.analysis = analysis;
    }
    
    public Boolean getIsSupplementAnswer() {
        return isSupplementAnswer;
    }
    
    public void setIsSupplementAnswer(Boolean isSupplementAnswer) {
        this.isSupplementAnswer = isSupplementAnswer;
    }
}

