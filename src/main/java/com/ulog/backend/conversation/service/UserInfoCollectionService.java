package com.ulog.backend.conversation.service;

import com.ulog.backend.conversation.dto.StartUserCollectionResponse;
import com.ulog.backend.conversation.dto.UserMessageResponse;

public interface UserInfoCollectionService {
    
    /**
     * 开始用户自我信息收集
     */
    StartUserCollectionResponse startCollection(Long userId);
    
    /**
     * 处理用户消息（多轮对话）
     */
    UserMessageResponse processMessage(String sessionId, Long userId, String message);
    
    /**
     * 获取收集进度
     */
    Integer getProgress(String sessionId, Long userId);
    
    /**
     * 放弃会话
     */
    void abandonSession(String sessionId, Long userId);
}

