package com.ulog.backend.conversation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulog.backend.ai.DeepseekClient;
import com.ulog.backend.ai.dto.ChatCompletionRequest;
import com.ulog.backend.ai.dto.ChatCompletionResponse;
import com.ulog.backend.ai.dto.ChatMessage;
import com.ulog.backend.config.DeepseekProperties;
import com.ulog.backend.conversation.dto.SupplementAnalysis;
import com.ulog.backend.conversation.dto.SupplementResult;
import com.ulog.backend.conversation.util.PromptTemplates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InfoSupplementServiceImpl implements InfoSupplementService {
    
    private static final Logger log = LoggerFactory.getLogger(InfoSupplementServiceImpl.class);
    
    private final DeepseekClient deepseekClient;
    private final ObjectMapper objectMapper;
    private final DeepseekProperties deepseekProperties;
    
    public InfoSupplementServiceImpl(DeepseekClient deepseekClient, ObjectMapper objectMapper, DeepseekProperties deepseekProperties) {
        this.deepseekClient = deepseekClient;
        this.objectMapper = objectMapper;
        this.deepseekProperties = deepseekProperties;
    }
    
    @Override
    public SupplementAnalysis analyzeInfoNeeds(String question, String contactDescription, String userDescription) {
        String prompt = PromptTemplates.buildInfoAnalysisPrompt(question, contactDescription, userDescription);
        
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(deepseekProperties.getModel());
        request.setMessages(List.of(
            new ChatMessage("system", PromptTemplates.SYSTEM_ROLE),
            new ChatMessage("user", prompt)
        ));
        request.setTemperature(0.7);
        
        ChatCompletionResponse response = deepseekClient.chat(request).block();
        String jsonResponse = response.getChoices().get(0).getMessage().getContent();
        
        try {
            return objectMapper.readValue(jsonResponse, SupplementAnalysis.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse supplement analysis result: {}", jsonResponse, e);
            // 返回默认分析结果
            return new SupplementAnalysis(false, "分析失败", List.of(), "");
        }
    }
    
    @Override
    public String generateSupplementQuestion(SupplementAnalysis analysis) {
        if (!analysis.isNeedsSupplement()) {
            return "";
        }
        
        return analysis.getSupplementQuestion();
    }
    
    @Override
    public SupplementResult processSupplementInfo(String sessionId, String supplementInfo) {
        // 补充信息的处理逻辑
        // originalQuestion现在从ConversationSession.lastQuestion中恢复，不再通过这里返回
        Map<String, Object> supplementData = new HashMap<>();
        supplementData.put("supplementInfo", supplementInfo);
        supplementData.put("timestamp", System.currentTimeMillis());
        
        return new SupplementResult(
            supplementInfo,
            null,  // originalQuestion改为null（在QaService中从session恢复）
            supplementData,
            false  // 不更新用户描述
        );
    }
}
