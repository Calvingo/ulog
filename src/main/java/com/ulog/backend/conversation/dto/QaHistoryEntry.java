package com.ulog.backend.conversation.dto;

import java.time.LocalDateTime;

/**
 * QA对话历史条目DTO
 */
public class QaHistoryEntry {
    
    private String timestamp;
    private String question;      // 用户问题
    private String answer;        // AI回答
    private String supplementQuestion;  // 补充问题（如果有）
    private String supplementAnswer;    // 补充回答（如果有）
    private Boolean needsMoreInfo;      // 是否需要补充信息
    
    public QaHistoryEntry() {
    }
    
    public QaHistoryEntry(String question, String answer) {
        this.timestamp = LocalDateTime.now().toString();
        this.question = question;
        this.answer = answer;
        this.needsMoreInfo = false;
    }
    
    public QaHistoryEntry(String question, String answer, Boolean needsMoreInfo) {
        this.timestamp = LocalDateTime.now().toString();
        this.question = question;
        this.answer = answer;
        this.needsMoreInfo = needsMoreInfo;
    }
    
    public QaHistoryEntry(String question, String answer, String supplementQuestion, 
                         String supplementAnswer, Boolean needsMoreInfo) {
        this.timestamp = LocalDateTime.now().toString();
        this.question = question;
        this.answer = answer;
        this.supplementQuestion = supplementQuestion;
        this.supplementAnswer = supplementAnswer;
        this.needsMoreInfo = needsMoreInfo;
    }
    
    // Getters and Setters
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public String getSupplementQuestion() {
        return supplementQuestion;
    }
    
    public void setSupplementQuestion(String supplementQuestion) {
        this.supplementQuestion = supplementQuestion;
    }
    
    public String getSupplementAnswer() {
        return supplementAnswer;
    }
    
    public void setSupplementAnswer(String supplementAnswer) {
        this.supplementAnswer = supplementAnswer;
    }
    
    public Boolean getNeedsMoreInfo() {
        return needsMoreInfo;
    }
    
    public void setNeedsMoreInfo(Boolean needsMoreInfo) {
        this.needsMoreInfo = needsMoreInfo;
    }
}
