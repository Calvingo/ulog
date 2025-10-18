package com.ulog.backend.conversation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserQaResponse {
    private String answer;
    private String sessionId;
    private String userDescription;  // 当前用户描述
    private Boolean needsMoreInfo;
    private String followUpQuestion;  // 如果需要更多信息，返回追问
    private String status;  // "answering", "needs_info", "completed"
}

