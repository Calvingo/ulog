package com.ulog.backend.conversation.service;

import com.ulog.backend.conversation.dto.MessageResponse;
import com.ulog.backend.conversation.dto.StartCollectionResponse;

public interface InfoCollectionService {
    
    /**
     * 开始信息收集
     */
    StartCollectionResponse startCollection(Long userId, String contactName);
    
    /**
     * 处理用户消息
     */
    MessageResponse processMessage(String sessionId, Long userId, String userMessage);
    
    /**
     * 获取会话进度
     */
    Integer getProgress(String sessionId, Long userId);
    
    /**
     * 放弃会话
     */
    void abandonSession(String sessionId, Long userId);
}

