package com.ulog.backend.conversation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EndConfidence {
    WEAK("weak", "弱信号", "可能只是跳过当前问题"),
    MEDIUM("medium", "中等信号", "可能想结束，需要确认"),
    STRONG("strong", "强烈信号", "明确想结束");
    
    private final String jsonValue;
    private final String displayName;
    private final String description;
    
    EndConfidence(String jsonValue, String displayName, String description) {
        this.jsonValue = jsonValue;
        this.displayName = displayName;
        this.description = description;
    }
    
    @JsonValue
    public String getJsonValue() {
        return jsonValue;
    }
    
    @JsonCreator
    public static EndConfidence fromJsonValue(String jsonValue) {
        for (EndConfidence confidence : EndConfidence.values()) {
            if (confidence.jsonValue.equals(jsonValue)) {
                return confidence;
            }
        }
        throw new IllegalArgumentException("Unknown EndConfidence: " + jsonValue);
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}

