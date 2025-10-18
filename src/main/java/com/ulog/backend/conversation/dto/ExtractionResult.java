package com.ulog.backend.conversation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ulog.backend.conversation.enums.EndConfidence;
import com.ulog.backend.conversation.enums.UserIntent;

import java.util.Map;

public class ExtractionResult {
    
    private UserIntent intent;
    private Map<String, Object> updates;
    
    @JsonProperty("shouldContinueCurrentQuestion")
    private boolean shouldContinueCurrentQuestion;
    
    @JsonProperty("wantsToEnd")
    private boolean wantsToEnd;
    
    private EndConfidence endConfidence;
    
    @JsonProperty("hasMinimumInfo")
    private boolean hasMinimumInfo;
    
    private String reasoning;
    
    public ExtractionResult() {
    }
    
    public UserIntent getIntent() {
        return intent;
    }
    
    public void setIntent(UserIntent intent) {
        this.intent = intent;
    }
    
    public Map<String, Object> getUpdates() {
        return updates;
    }
    
    public void setUpdates(Map<String, Object> updates) {
        this.updates = updates;
    }
    
    public boolean isShouldContinueCurrentQuestion() {
        return shouldContinueCurrentQuestion;
    }
    
    public void setShouldContinueCurrentQuestion(boolean shouldContinueCurrentQuestion) {
        this.shouldContinueCurrentQuestion = shouldContinueCurrentQuestion;
    }
    
    public boolean isWantsToEnd() {
        return wantsToEnd;
    }
    
    public void setWantsToEnd(boolean wantsToEnd) {
        this.wantsToEnd = wantsToEnd;
    }
    
    public EndConfidence getEndConfidence() {
        return endConfidence;
    }
    
    public void setEndConfidence(EndConfidence endConfidence) {
        this.endConfidence = endConfidence;
    }
    
    public boolean isHasMinimumInfo() {
        return hasMinimumInfo;
    }
    
    public void setHasMinimumInfo(boolean hasMinimumInfo) {
        this.hasMinimumInfo = hasMinimumInfo;
    }
    
    public String getReasoning() {
        return reasoning;
    }
    
    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }
}

