package com.ulog.backend.conversation.dto;

import com.ulog.backend.contact.dto.ContactResponse;

import java.util.List;

public class MessageResponse {
    
    // 基本字段
    private String nextQuestion;
    private Boolean isCompleted;
    private Integer progress;
    private String currentDimension;
    
    // 完成相关
    private ContactResponse contact;
    private String sessionId;
    private String nextMode;
    private String completionMessage;
    private List<String> suggestedActions;
    
    // 状态标记
    private Boolean isConfirmingEnd;
    private Boolean needsMinimumInfo;
    private String minimumInfoHint;
    private String collectedSummary;
    
    // 意图识别
    private String intent;
    
    public MessageResponse() {
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final MessageResponse response = new MessageResponse();
        
        public Builder nextQuestion(String nextQuestion) {
            response.nextQuestion = nextQuestion;
            return this;
        }
        
        public Builder isCompleted(Boolean isCompleted) {
            response.isCompleted = isCompleted;
            return this;
        }
        
        public Builder progress(Integer progress) {
            response.progress = progress;
            return this;
        }
        
        public Builder currentDimension(String currentDimension) {
            response.currentDimension = currentDimension;
            return this;
        }
        
        public Builder contact(ContactResponse contact) {
            response.contact = contact;
            return this;
        }
        
        public Builder sessionId(String sessionId) {
            response.sessionId = sessionId;
            return this;
        }
        
        public Builder nextMode(String nextMode) {
            response.nextMode = nextMode;
            return this;
        }
        
        public Builder completionMessage(String completionMessage) {
            response.completionMessage = completionMessage;
            return this;
        }
        
        public Builder suggestedActions(List<String> suggestedActions) {
            response.suggestedActions = suggestedActions;
            return this;
        }
        
        public Builder isConfirmingEnd(Boolean isConfirmingEnd) {
            response.isConfirmingEnd = isConfirmingEnd;
            return this;
        }
        
        public Builder needsMinimumInfo(Boolean needsMinimumInfo) {
            response.needsMinimumInfo = needsMinimumInfo;
            return this;
        }
        
        public Builder minimumInfoHint(String minimumInfoHint) {
            response.minimumInfoHint = minimumInfoHint;
            return this;
        }
        
        public Builder collectedSummary(String collectedSummary) {
            response.collectedSummary = collectedSummary;
            return this;
        }
        
        public Builder intent(String intent) {
            response.intent = intent;
            return this;
        }
        
        public MessageResponse build() {
            return response;
        }
    }
    
    // Getters and Setters
    public String getNextQuestion() {
        return nextQuestion;
    }
    
    public void setNextQuestion(String nextQuestion) {
        this.nextQuestion = nextQuestion;
    }
    
    public Boolean getIsCompleted() {
        return isCompleted;
    }
    
    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
    
    public Integer getProgress() {
        return progress;
    }
    
    public void setProgress(Integer progress) {
        this.progress = progress;
    }
    
    public String getCurrentDimension() {
        return currentDimension;
    }
    
    public void setCurrentDimension(String currentDimension) {
        this.currentDimension = currentDimension;
    }
    
    public ContactResponse getContact() {
        return contact;
    }
    
    public void setContact(ContactResponse contact) {
        this.contact = contact;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getNextMode() {
        return nextMode;
    }
    
    public void setNextMode(String nextMode) {
        this.nextMode = nextMode;
    }
    
    public String getCompletionMessage() {
        return completionMessage;
    }
    
    public void setCompletionMessage(String completionMessage) {
        this.completionMessage = completionMessage;
    }
    
    public List<String> getSuggestedActions() {
        return suggestedActions;
    }
    
    public void setSuggestedActions(List<String> suggestedActions) {
        this.suggestedActions = suggestedActions;
    }
    
    public Boolean getIsConfirmingEnd() {
        return isConfirmingEnd;
    }
    
    public void setIsConfirmingEnd(Boolean isConfirmingEnd) {
        this.isConfirmingEnd = isConfirmingEnd;
    }
    
    public Boolean getNeedsMinimumInfo() {
        return needsMinimumInfo;
    }
    
    public void setNeedsMinimumInfo(Boolean needsMinimumInfo) {
        this.needsMinimumInfo = needsMinimumInfo;
    }
    
    public String getMinimumInfoHint() {
        return minimumInfoHint;
    }
    
    public void setMinimumInfoHint(String minimumInfoHint) {
        this.minimumInfoHint = minimumInfoHint;
    }
    
    public String getCollectedSummary() {
        return collectedSummary;
    }
    
    public void setCollectedSummary(String collectedSummary) {
        this.collectedSummary = collectedSummary;
    }
    
    public String getIntent() {
        return intent;
    }
    
    public void setIntent(String intent) {
        this.intent = intent;
    }
}

