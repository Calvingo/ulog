package com.ulog.backend.conversation.dto;

public class StartCollectionResponse {
    
    private String sessionId;
    private String firstQuestion;
    private Integer progress;
    private String currentDimension;
    
    public StartCollectionResponse() {
    }
    
    public StartCollectionResponse(String sessionId, String firstQuestion, Integer progress, String currentDimension) {
        this.sessionId = sessionId;
        this.firstQuestion = firstQuestion;
        this.progress = progress;
        this.currentDimension = currentDimension;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getFirstQuestion() {
        return firstQuestion;
    }
    
    public void setFirstQuestion(String firstQuestion) {
        this.firstQuestion = firstQuestion;
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
}

