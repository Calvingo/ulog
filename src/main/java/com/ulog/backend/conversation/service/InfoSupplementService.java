package com.ulog.backend.conversation.service;

import com.ulog.backend.conversation.dto.SupplementAnalysis;
import com.ulog.backend.conversation.dto.SupplementResult;

public interface InfoSupplementService {
    
    /**
     * 分析是否需要补充信息
     */
    SupplementAnalysis analyzeInfoNeeds(String question, String contactDescription, String userDescription);
    
    /**
     * 生成补充信息的问题
     */
    String generateSupplementQuestion(SupplementAnalysis analysis);
    
    /**
     * 处理补充信息
     */
    SupplementResult processSupplementInfo(String sessionId, String supplementInfo);
}
