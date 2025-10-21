package com.ulog.backend.conversation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulog.backend.ai.AiSummaryService;
import com.ulog.backend.ai.DeepseekClient;
import com.ulog.backend.ai.dto.ChatCompletionRequest;
import com.ulog.backend.ai.dto.ChatCompletionResponse;
import com.ulog.backend.ai.dto.ChatMessage;
import com.ulog.backend.config.DeepseekProperties;
import com.ulog.backend.common.exception.BadRequestException;
import com.ulog.backend.common.exception.NotFoundException;
import com.ulog.backend.conversation.dto.QaHistoryEntry;
import com.ulog.backend.conversation.dto.UserQaResponse;
import com.ulog.backend.conversation.enums.SessionStatus;
import com.ulog.backend.conversation.util.PromptTemplates;
import com.ulog.backend.domain.conversation.UserConversationSession;
import com.ulog.backend.domain.user.User;
import com.ulog.backend.repository.UserConversationSessionRepository;
import com.ulog.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserQaServiceImpl implements UserQaService {
    
    private static final Logger log = LoggerFactory.getLogger(UserQaServiceImpl.class);
    
    private final UserConversationSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final DeepseekClient deepseekClient;
    private final AiSummaryService aiSummaryService;
    private final QaHistoryService qaHistoryService;
    private final ObjectMapper objectMapper;
    private final DeepseekProperties deepseekProperties;
    
    public UserQaServiceImpl(
        UserConversationSessionRepository sessionRepository,
        UserRepository userRepository,
        DeepseekClient deepseekClient,
        AiSummaryService aiSummaryService,
        QaHistoryService qaHistoryService,
        ObjectMapper objectMapper,
        DeepseekProperties deepseekProperties
    ) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.deepseekClient = deepseekClient;
        this.aiSummaryService = aiSummaryService;
        this.qaHistoryService = qaHistoryService;
        this.objectMapper = objectMapper;
        this.deepseekProperties = deepseekProperties;
    }
    
    @Override
    @Transactional
    public UserQaResponse processQuestion(String sessionId, Long userId, String question) {
        log.info("Processing user self-QA question for session: {}, userId: {}, question: {}", 
            sessionId, userId, question);
        
        // 1. 验证会话和权限
        UserConversationSession session = validateSession(sessionId, userId);
        User user = loadUser(userId);
        
        // 2. 分析信息需求
        AnalysisResult analysis = analyzeInfoNeeds(question, user.getDescription());
        
        // 3. 判断是否需要补充信息
        if (analysis.needsMoreInfo) {
            log.info("Question needs more info, asking follow-up for question: {}", question);
            
            // 保存用户的原始问题到session
            session.setLastQuestion(question);
            session.setStatus(SessionStatus.QA_ACTIVE);
            sessionRepository.save(session);
            
            // 立即保存部分QA历史（包含原始问题和追问）
            QaHistoryEntry partialEntry = new QaHistoryEntry();
            partialEntry.setQuestion(question);
            partialEntry.setSupplementQuestion(analysis.followUpQuestion);
            partialEntry.setNeedsMoreInfo(true);
            // answer和supplementAnswer待补充后填写
            qaHistoryService.addUserQaEntry(sessionId, partialEntry);
            
            log.info("Saved partial QA history for user session {}", sessionId);
            
            UserQaResponse response = new UserQaResponse();
            response.setAnswer(null);
            response.setSessionId(sessionId);
            response.setUserDescription(user.getDescription());
            response.setNeedsMoreInfo(true);
            response.setFollowUpQuestion(analysis.followUpQuestion);
            response.setStatus("needs_info");
            return response;
        }
        
        // 4. 生成答案
        log.info("Sufficient info available, generating answer");
        String answer = generateAnswer(question, user.getDescription(), user.getSelfValue(), sessionId);
        
        // 更新会话状态
        if (session.getStatus() != SessionStatus.QA_ACTIVE) {
            session.setStatus(SessionStatus.QA_ACTIVE);
            sessionRepository.save(session);
        }
        
        UserQaResponse response = new UserQaResponse();
        response.setAnswer(answer);
        response.setSessionId(sessionId);
        response.setUserDescription(user.getDescription());
        response.setNeedsMoreInfo(false);
        response.setFollowUpQuestion(null);
        response.setStatus("answering");
        return response;
    }
    
    @Override
    @Transactional
    public String generateSummary(String sessionId, Long userId) {
        log.info("Generating summary for user self-conversation session: {}", sessionId);
        
        validateSession(sessionId, userId);
        User user = loadUser(userId);
        
        if (user.getDescription() == null || user.getDescription().trim().isEmpty()) {
            throw new BadRequestException("暂无个人信息可以生成总结");
        }
        
        // 使用AiSummaryService生成总结
        String summary = aiSummaryService.generateAiSummary(user.getDescription());
        
        log.info("Generated summary for session: {}", sessionId);
        return summary;
    }
    
    @Override
    @Transactional
    public UserQaResponse processSupplementInfo(String sessionId, Long userId, String supplementInfo) {
        log.info("Processing supplement info for session: {}, supplementInfo: {}", sessionId, supplementInfo);
        
        // 1. 验证会话
        UserConversationSession session = validateSession(sessionId, userId);
        User user = loadUser(userId);
        
        // 2. 从session中恢复原始问题
        String originalQuestion = session.getLastQuestion();
        if (originalQuestion == null || originalQuestion.trim().isEmpty()) {
            log.warn("Session {} has no original question stored", sessionId);
            originalQuestion = "（用户的原始问题未记录）";
        }
        
        log.info("Processing supplement for session {}, original question: {}", 
            sessionId, originalQuestion);
        
        // 3. 异步更新用户描述（整合补充信息）
        asyncUpdateUserDescriptionWithSupplement(
            user,
            originalQuestion,
            supplementInfo
        );
        
        log.info("Triggered async update for user {} description with supplement info", userId);
        
        // 4. 生成答案（基于原始问题和补充信息）
        String answer = generateAnswerWithSupplement(originalQuestion, user.getDescription(), user.getSelfValue(), sessionId, supplementInfo);
        
        // 5. 清空lastQuestion（问题已处理完毕）
        session.setLastQuestion(null);
        sessionRepository.save(session);
        
        // 6. 返回响应
        UserQaResponse response = new UserQaResponse();
        response.setAnswer(answer);
        response.setSessionId(sessionId);
        response.setUserDescription(user.getDescription());  // 返回原始描述
        response.setNeedsMoreInfo(false);
        response.setFollowUpQuestion(null);
        response.setStatus("answered");
        return response;
    }
    
    @Override
    @Transactional
    public void endSession(String sessionId, Long userId) {
        log.info("Ending user self-QA session: {}", sessionId);
        
        UserConversationSession session = validateSession(sessionId, userId);
        
        // 标记会话为已完成（如果还不是）
        if (session.getStatus() == SessionStatus.QA_ACTIVE) {
            session.setStatus(SessionStatus.COMPLETED);
            sessionRepository.save(session);
        }
        
        log.info("Session {} ended", sessionId);
    }
    
    /**
     * 验证会话
     */
    private UserConversationSession validateSession(String sessionId, Long userId) {
        UserConversationSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("会话不存在"));
        
        if (!session.getUserId().equals(userId)) {
            throw new BadRequestException("无权访问此会话");
        }
        
        // 必须是COMPLETED或QA_ACTIVE状态才能进行QA
        if (session.getStatus() != SessionStatus.COMPLETED && session.getStatus() != SessionStatus.QA_ACTIVE) {
            throw new BadRequestException("会话状态不正确，必须先完成信息收集");
        }
        
        return session;
    }
    
    /**
     * 加载用户
     */
    private User loadUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("用户不存在"));
    }
    
    /**
     * 分析信息需求
     */
    private AnalysisResult analyzeInfoNeeds(String question, String userDescription) {
        String prompt = PromptTemplates.buildUserSelfQaAnalysisPrompt(question, userDescription);
        
        // 直接调用Deepseek
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(deepseekProperties.getModel());
        request.setMessages(List.of(
            new ChatMessage("system", "你是一个专业的个人分析助手，帮助用户更好地了解自己。"),
            new ChatMessage("user", prompt)
        ));
        request.setTemperature(0.7);
        
        // 使用 reasoner 模型进行问答
        request.setModel(deepseekProperties.getReasonerModel());
        ChatCompletionResponse response = deepseekClient.chat(request).block();
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new BadRequestException("AI服务暂时不可用");
        }
        
        String jsonResponse = response.getChoices().get(0).getMessage().getContent().trim();
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(jsonResponse, Map.class);
            
            AnalysisResult analysis = new AnalysisResult();
            analysis.needsMoreInfo = (Boolean) result.getOrDefault("needsMoreInfo", false);
            analysis.missingInfo = (String) result.get("missingInfo");
            analysis.followUpQuestion = (String) result.get("followUpQuestion");
            
            log.info("Analysis result - needsMoreInfo: {}, followUpQuestion: {}", 
                analysis.needsMoreInfo, analysis.followUpQuestion);
            
            return analysis;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse analysis result: {}", jsonResponse, e);
            
            // 兜底：如果解析失败，认为不需要补充信息
            AnalysisResult fallback = new AnalysisResult();
            fallback.needsMoreInfo = false;
            return fallback;
        }
    }
    
    /**
     * 生成答案（重载方法，无补充信息）
     */
    private String generateAnswer(String question, String userDescription, String userSelfValue, String sessionId) {
        return generateAnswer(question, userDescription, userSelfValue, sessionId, null);
    }
    
    /**
     * 生成答案（原生多轮对话版本）
     */
    private String generateAnswer(
        String question, 
        String userDescription, 
        String userSelfValue, 
        String sessionId,
        String supplementInfo
    ) {
        // Step 1: 构建基础系统Prompt（不包含历史对话）
        String systemPrompt = PromptTemplates.buildBaseUserSelfQaSystemPrompt(
            userDescription,
            userSelfValue
        );
        
        // Step 2: 构建原生多轮消息数组
        List<ChatMessage> messages = new ArrayList<>();
        
        // 添加系统提示
        messages.add(new ChatMessage("system", systemPrompt));
        
        // Step 3: 添加历史对话（原生格式）
        List<QaHistoryEntry> qaHistory = qaHistoryService.getUserQaHistory(sessionId);
        for (QaHistoryEntry entry : qaHistory) {
            // 添加用户的历史问题
            if (entry.getQuestion() != null && !entry.getQuestion().trim().isEmpty()) {
                messages.add(new ChatMessage("user", entry.getQuestion()));
            }
            
            // 如果有补充信息流程
            if (entry.getNeedsMoreInfo() != null && entry.getNeedsMoreInfo()) {
                // Deepseek的补充问题
                if (entry.getSupplementQuestion() != null) {
                    messages.add(new ChatMessage("assistant", entry.getSupplementQuestion()));
                }
                // 用户的补充回答
                if (entry.getSupplementAnswer() != null) {
                    messages.add(new ChatMessage("user", entry.getSupplementAnswer()));
                }
            }
            
            // 添加AI的历史回答
            if (entry.getAnswer() != null) {
                messages.add(new ChatMessage("assistant", entry.getAnswer()));
            }
        }
        
        // Step 4: 添加当前问题
        messages.add(new ChatMessage("user", question));
        
        // Step 5: 如果有补充信息，添加补充回答
        if (supplementInfo != null && !supplementInfo.trim().isEmpty()) {
            messages.add(new ChatMessage("user", supplementInfo));
        }
        
        // Step 6: 调用Deepseek
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setMessages(messages);
        request.setTemperature(0.7);
        
        log.info("Calling Deepseek with {} messages for user session {}", 
            messages.size(), sessionId);
        
        // 使用 reasoner 模型进行问答
        request.setModel(deepseekProperties.getReasonerModel());
        ChatCompletionResponse response = deepseekClient.chat(request).block();
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new BadRequestException("AI服务暂时不可用");
        }
        
        String answer = response.getChoices().get(0).getMessage().getContent().trim();
        
        // Step 7: 保存或更新QA历史
        if (supplementInfo != null && !supplementInfo.trim().isEmpty()) {
            // 有补充信息的情况 - 更新最后一条历史记录
            List<QaHistoryEntry> history = qaHistoryService.getUserQaHistory(sessionId);
            if (!history.isEmpty()) {
                QaHistoryEntry lastEntry = history.get(history.size() - 1);
                // 更新补充回答和最终答案
                lastEntry.setSupplementAnswer(supplementInfo);
                lastEntry.setAnswer(answer);
                qaHistoryService.updateLastUserQaEntry(sessionId, lastEntry);
                
                log.info("Updated last QA entry with supplement for user session {}, question: {}", 
                    sessionId, question);
            } else {
                // 如果历史为空（异常情况），创建新记录
                log.warn("No history found when processing supplement, creating new entry for user session {}", 
                    sessionId);
                QaHistoryEntry qaEntry = new QaHistoryEntry(
                    question,
                    answer,
                    "（系统请求补充信息）",
                    supplementInfo,
                    true
                );
                qaHistoryService.addUserQaEntry(sessionId, qaEntry);
            }
        } else {
            // 直接回答的情况 - 添加新记录
            QaHistoryEntry qaEntry = new QaHistoryEntry(question, answer);
            qaHistoryService.addUserQaEntry(sessionId, qaEntry);
            
            log.info("Saved new QA entry for user session {}, question: {}", sessionId, question);
        }
        
        return answer;
    }
    
    /**
     * 追加补充信息到用户描述（已废弃，保留以防其他地方使用）
     * @deprecated 不再使用，补充信息不落库到用户描述
     */
    @Deprecated
    private String appendSupplementInfo(String originalDescription, String supplementInfo) {
        if (originalDescription == null || originalDescription.trim().isEmpty()) {
            return supplementInfo;
        }
        
        return originalDescription + "\n\n【补充信息】\n" + supplementInfo;
    }
    
    /**
     * 异步更新用户描述（整合补充信息）
     */
    @Async
    public void asyncUpdateUserDescriptionWithSupplement(
        User user, 
        String supplementQuestion, 
        String supplementInfo
    ) {
        try {
            String prompt = PromptTemplates.buildIntegrateSupplementToUserDescriptionPrompt(
                user.getDescription(),
                supplementQuestion, // 使用AI的补充问题
                supplementInfo
            );
            
            ChatCompletionRequest request = new ChatCompletionRequest();
            request.setModel(deepseekProperties.getModel());
            request.setMessages(List.of(
                new ChatMessage("system", "你是一个专业的信息整合助手。"),
                new ChatMessage("user", prompt)
            ));
            request.setTemperature(0.3); // 较低温度，保证稳定性
            
            ChatCompletionResponse response = deepseekClient.chat(request).block();
            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                log.error("AI service unavailable for async user description update");
                return;
            }
            
            String updatedDescription = response.getChoices().get(0).getMessage().getContent().trim();
            
            // 更新用户描述
            user.setDescription(updatedDescription);
            userRepository.save(user);
            
            log.info("Async updated user {} description with supplement info, original length: {}, new length: {}", 
                user.getId(),
                user.getDescription() != null ? user.getDescription().length() : 0, 
                updatedDescription.length());
        } catch (Exception e) {
            log.error("Failed to async update user {} description: {}", user.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * 使用原始描述和补充信息直接回答问题的专用方法
     */
    private String generateAnswerWithSupplement(
        String question, 
        String userDescription,
        String userSelfValue,
        String sessionId,
        String supplementInfo
    ) {
        // Step 1: 构建基础系统Prompt（不包含历史对话）
        String systemPrompt = PromptTemplates.buildBaseUserSelfQaSystemPrompt(
            userDescription,
            userSelfValue
        );
        
        // Step 2: 构建原生多轮消息数组
        List<ChatMessage> messages = new ArrayList<>();
        
        // 添加系统提示
        messages.add(new ChatMessage("system", systemPrompt));
        
        // Step 3: 添加历史对话（原生格式）
        List<QaHistoryEntry> qaHistory = qaHistoryService.getUserQaHistory(sessionId);
        for (QaHistoryEntry entry : qaHistory) {
            // 添加用户的历史问题
            if (entry.getQuestion() != null && !entry.getQuestion().trim().isEmpty()) {
                messages.add(new ChatMessage("user", entry.getQuestion()));
            }
            
            // 如果有补充信息流程
            if (entry.getNeedsMoreInfo() != null && entry.getNeedsMoreInfo()) {
                // Deepseek的补充问题
                if (entry.getSupplementQuestion() != null) {
                    messages.add(new ChatMessage("assistant", entry.getSupplementQuestion()));
                }
                // 用户的补充回答
                if (entry.getSupplementAnswer() != null) {
                    messages.add(new ChatMessage("user", entry.getSupplementAnswer()));
                }
            }
            
            // 添加AI的历史回答
            if (entry.getAnswer() != null) {
                messages.add(new ChatMessage("assistant", entry.getAnswer()));
            }
        }
        
        // Step 4: 添加当前问题
        messages.add(new ChatMessage("user", question));
        
        // Step 5: 添加补充信息作为额外的上下文
        if (supplementInfo != null && !supplementInfo.trim().isEmpty()) {
            messages.add(new ChatMessage("user", "补充信息：" + supplementInfo));
        }
        
        // Step 6: 调用Deepseek
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(deepseekProperties.getReasonerModel());
        request.setMessages(messages);
        request.setTemperature(0.7);
        
        log.info("Calling Deepseek with {} messages for user session {} (with supplement)", 
            messages.size(), sessionId);
        
        ChatCompletionResponse response = deepseekClient.chat(request).block();
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new BadRequestException("AI服务暂时不可用");
        }
        
        String answer = response.getChoices().get(0).getMessage().getContent().trim();
        
        return answer;
    }
    
    /**
     * 分析结果内部类
     */
    private static class AnalysisResult {
        boolean needsMoreInfo;
        String missingInfo;
        String followUpQuestion;
    }
}

