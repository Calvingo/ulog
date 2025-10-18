package com.ulog.backend.conversation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartUserCollectionResponse {
    private String sessionId;
    private String firstQuestion;
    private String currentDimension;
    private LocalDateTime startedAt;
    private String message;  // "开始收集你的个人信息"
}

