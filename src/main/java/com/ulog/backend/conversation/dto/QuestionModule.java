package com.ulog.backend.conversation.dto;

import java.util.List;

/**
 * 问卷模块数据结构
 */
public class QuestionModule {
    private final String moduleId;
    private final String title;
    private final String openingText;
    private final String defaultQuestion;
    private final List<String> keywords;

    public QuestionModule(String moduleId, String title, String openingText, 
                         String defaultQuestion, List<String> keywords) {
        this.moduleId = moduleId;
        this.title = title;
        this.openingText = openingText;
        this.defaultQuestion = defaultQuestion;
        this.keywords = keywords;
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getTitle() {
        return title;
    }

    public String getOpeningText() {
        return openingText;
    }

    public String getDefaultQuestion() {
        return defaultQuestion;
    }

    public List<String> getKeywords() {
        return keywords;
    }
}

