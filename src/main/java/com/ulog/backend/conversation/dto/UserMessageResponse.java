package com.ulog.backend.conversation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageResponse {
    private String nextQuestion;
    private Boolean isCompleted;
    private Integer progress;
    private String currentDimension;
    private String updatedDescription;
    private String sessionId;
    private String nextMode;  // "qa" 表示进入QA模式
    private String completionMessage;
    private List<String> suggestedActions;
    private LocalDateTime completedAt;
    
    // 确认结束相关
    private Boolean isConfirmingEnd;
    private Boolean needsMinimumInfo;
    private String minimumInfoHint;
    private String collectedSummary;
    
    // 意图识别
    private String intent;
}

