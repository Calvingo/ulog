package com.ulog.backend.conversation.service;

import com.ulog.backend.conversation.dto.UserQaResponse;

public interface UserQaService {
    
    /**
     * 处理用户的自我问答
     */
    UserQaResponse processQuestion(String sessionId, Long userId, String question);
    
    /**
     * 生成AI总结
     */
    String generateSummary(String sessionId, Long userId);
    
    /**
     * 处理补充信息
     */
    UserQaResponse processSupplementInfo(String sessionId, Long userId, String supplementInfo);
    
    /**
     * 结束QA会话
     */
    void endSession(String sessionId, Long userId);
}

