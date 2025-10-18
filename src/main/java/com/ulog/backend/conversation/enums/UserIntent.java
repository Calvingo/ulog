package com.ulog.backend.conversation.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserIntent {
    ANSWER("answer", "回答问题"),
    CORRECTION("correction", "修正信息"),
    SUPPLEMENT("supplement", "补充信息"),
    SKIP_QUESTION("skip", "跳过问题"),
    WANT_TO_END("want_to_end", "想要结束"),
    CONFIRM_END("confirm_end", "确认结束"),
    CONTINUE("continue", "继续回答");
    
    private final String jsonValue;
    private final String description;
    
    UserIntent(String jsonValue, String description) {
        this.jsonValue = jsonValue;
        this.description = description;
    }
    
    @JsonValue
    public String getJsonValue() {
        return jsonValue;
    }
    
    @JsonCreator
    public static UserIntent fromJsonValue(String jsonValue) {
        for (UserIntent intent : UserIntent.values()) {
            if (intent.jsonValue.equals(jsonValue)) {
                return intent;
            }
        }
        throw new IllegalArgumentException("Unknown UserIntent: " + jsonValue);
    }
    
    public String getDescription() {
        return description;
    }
}

