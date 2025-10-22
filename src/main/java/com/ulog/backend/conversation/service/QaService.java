package com.ulog.backend.conversation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulog.backend.ai.AiSummaryService;
import com.ulog.backend.ai.DeepseekClient;
import com.ulog.backend.ai.dto.ChatCompletionRequest;
import com.ulog.backend.ai.dto.ChatCompletionResponse;
import com.ulog.backend.ai.dto.ChatMessage;
import com.ulog.backend.config.DeepseekProperties;
import com.ulog.backend.common.exception.BadRequestException;
import com.ulog.backend.common.exception.NotFoundException;
import com.ulog.backend.conversation.dto.QaResponse;
import com.ulog.backend.conversation.dto.QaHistoryEntry;
import com.ulog.backend.conversation.dto.SupplementAnalysis;
import com.ulog.backend.conversation.enums.SessionStatus;
import com.ulog.backend.conversation.event.ContactDescriptionUpdatedEvent;
import com.ulog.backend.conversation.event.UserDescriptionUpdatedEvent;
import com.ulog.backend.conversation.util.PromptTemplates;
import com.ulog.backend.compliance.service.OperationLogService;
import com.ulog.backend.domain.contact.Contact;
import com.ulog.backend.domain.conversation.ConversationSession;
import com.ulog.backend.domain.user.User;
import com.ulog.backend.repository.ContactRepository;
import com.ulog.backend.repository.ConversationSessionRepository;
import com.ulog.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class QaService {
    
    private static final Logger log = LoggerFactory.getLogger(QaService.class);
    
    private final ConversationSessionRepository sessionRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final DeepseekClient deepseekClient;
    private final AiSummaryService aiSummaryService;
    private final InfoSupplementService infoSupplementService;
    private final QaHistoryService qaHistoryService;
    private final ObjectMapper objectMapper;
    private final DeepseekProperties deepseekProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final OperationLogService operationLogService;
    
    public QaService(
        ConversationSessionRepository sessionRepository,
        ContactRepository contactRepository,
        UserRepository userRepository,
        DeepseekClient deepseekClient,
        AiSummaryService aiSummaryService,
        InfoSupplementService infoSupplementService,
        QaHistoryService qaHistoryService,
        ObjectMapper objectMapper,
        DeepseekProperties deepseekProperties,
        ApplicationEventPublisher eventPublisher,
        OperationLogService operationLogService
    ) {
        this.sessionRepository = sessionRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.deepseekClient = deepseekClient;
        this.aiSummaryService = aiSummaryService;
        this.infoSupplementService = infoSupplementService;
        this.qaHistoryService = qaHistoryService;
        this.objectMapper = objectMapper;
        this.deepseekProperties = deepseekProperties;
        this.eventPublisher = eventPublisher;
        this.operationLogService = operationLogService;
    }
    
    /**
     * 处理问答
     */
    @Transactional
    public QaResponse processQuestion(String sessionId, Long userId, String question) {
        // 1. 验证会话和权限
        ConversationSession session = validateSession(sessionId, userId);
        Contact contact = loadContact(session.getContactId(), userId);
        User user = loadUser(userId);
        
        // 记录 AI 对话开始日志
        operationLogService.logOperation(userId, "ai_conversation", 
            String.format("AI conversation started - session: %s, contact: %s, question: %s", 
                sessionId, contact.getName(), question.length() > 50 ? question.substring(0, 50) + "..." : question));
        
        // 2. 分析信息需求
        SupplementAnalysis analysis = infoSupplementService.analyzeInfoNeeds(
            question, 
            contact.getDescription(), 
            user.getDescription()
        );
        
        // 3. 判断是否需要补充信息
        if (analysis.isNeedsSupplement()) {
            return handleInfoSupplement(session, analysis, question);
        } else {
            return generateDirectAnswer(session, contact, user, question);
        }
    }
    
    /**
     * 处理补充信息
     */
    @Transactional
    public QaResponse processSupplementInfo(String sessionId, Long userId, String supplementInfo) {
        // 1. 验证会话状态
        ConversationSession session = validateSession(sessionId, userId);
        if (!SessionStatus.QA_ACTIVE.name().equals(session.getStatus())) {
            throw new BadRequestException("当前不需要补充信息");
        }
        
        // 2. 从session中恢复原始问题
        String originalQuestion = session.getLastQuestion();
        if (originalQuestion == null || originalQuestion.trim().isEmpty()) {
            log.warn("Session {} has no original question stored", sessionId);
            originalQuestion = "（用户的原始问题未记录）";
        }
        
        log.info("Processing supplement for session {}, original question: {}", 
            sessionId, originalQuestion);
        
        // 3. 异步更新联系人描述（整合补充信息）
        Contact contact = loadContact(session.getContactId(), userId);
        asyncUpdateDescriptionWithSupplement(
            contact,
            originalQuestion,
            supplementInfo
        );
        
        log.info("Triggered async update for contact {} description with supplement info", contact.getId());
        
        // 4. 清空lastQuestion（问题已处理完毕）
        session.setLastQuestion(null);
        sessionRepository.save(session);
        
        // 5. 生成最终回答
        return generateFinalAnswer(session, originalQuestion, supplementInfo);
    }
    
    /**
     * 生成AI总结
     */
    @Transactional
    public String generateSummary(String sessionId, Long userId) {
        // 1. 加载会话
        ConversationSession session = sessionRepository.findBySessionIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new NotFoundException("会话不存在或无权访问"));
        
        // 2. 检查是否有关联的联系人
        if (session.getContactId() == null) {
            throw new BadRequestException("会话未关联联系人");
        }
        
        // 3. 加载联系人
        Contact contact = loadContact(session.getContactId(), userId);
        
        // 4. 调用AI总结服务（使用正确的方法签名）
        String description = contact.getDescription();
        if (description == null || description.trim().isEmpty()) {
            throw new BadRequestException("联系人描述为空，无法生成AI总结");
        }
        
        String aiSummary = aiSummaryService.generateAiSummary(description);
        
        // 5. 更新联系人的aiSummary字段
        contact.setAiSummary(aiSummary);
        contactRepository.save(contact);
        
        log.info("Generated AI summary for contact {} in session {}", contact.getId(), sessionId);
        
        return aiSummary;
    }
    
    /**
     * 结束会话
     */
    @Transactional
    public void endSession(String sessionId, Long userId) {
        ConversationSession session = sessionRepository.findBySessionIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new NotFoundException("会话不存在或无权访问"));
        
        session.setStatus(SessionStatus.COMPLETED.name());
        sessionRepository.save(session);
        
        log.info("Ended session {} by user {}", sessionId, userId);
    }
    
    
    /**
     * 加载联系人
     */
    private Contact loadContact(Long contactId, Long userId) {
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(() -> new NotFoundException("联系人不存在"));
        
        // 验证权限
        if (!contact.getOwner().getId().equals(userId)) {
            throw new BadRequestException("无权访问该联系人");
        }
        
        if (contact.isDeleted()) {
            throw new NotFoundException("联系人已删除");
        }
        
        return contact;
    }
    
    
    /**
     * 验证会话和权限
     */
    private ConversationSession validateSession(String sessionId, Long userId) {
        ConversationSession session = sessionRepository.findBySessionIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new NotFoundException("会话不存在或无权访问"));
        
        // 检查状态
        if (!SessionStatus.COMPLETED.name().equals(session.getStatus()) &&
            !SessionStatus.QA_ACTIVE.name().equals(session.getStatus())) {
            throw new BadRequestException("会话未完成信息收集，无法进行问答");
        }
        
        // 检查是否有关联的联系人
        if (session.getContactId() == null) {
            throw new BadRequestException("会话未关联联系人");
        }
        
        return session;
    }
    
    /**
     * 加载用户
     */
    private User loadUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("用户不存在"));
        
        return user;
    }
    
    /**
     * 处理信息补充
     */
    private QaResponse handleInfoSupplement(
        ConversationSession session, 
        SupplementAnalysis analysis,
        String originalQuestion
    ) {
        // 保存用户的原始问题到session
        session.setLastQuestion(originalQuestion);
        session.setStatus(SessionStatus.QA_ACTIVE.name());
        sessionRepository.save(session);
        
        // 生成补充信息的问题
        String supplementQuestion = infoSupplementService.generateSupplementQuestion(analysis);
        
        // 立即保存部分QA历史（包含原始问题和补充问题）
        QaHistoryEntry partialEntry = new QaHistoryEntry();
        partialEntry.setQuestion(originalQuestion);
        partialEntry.setSupplementQuestion(supplementQuestion);
        partialEntry.setNeedsMoreInfo(true);
        // answer和supplementAnswer待补充后填写
        qaHistoryService.addContactQaEntry(session.getSessionId(), partialEntry);
        
        log.info("Session {} needs supplement info for question: {}, saved partial QA history", 
            session.getSessionId(), originalQuestion);
        
        return QaResponse.builder()
            .needsMoreInfo(true)
            .supplementQuestion(supplementQuestion)
            .analysis(analysis)
            .isSupplementAnswer(false)
            .build();
    }
    
    /**
     * 生成直接回答
     */
    private QaResponse generateDirectAnswer(ConversationSession session, Contact contact, User user, String question) {
        // 更新状态为QA_ACTIVE
        if (!SessionStatus.QA_ACTIVE.name().equals(session.getStatus())) {
            session.setStatus(SessionStatus.QA_ACTIVE.name());
            sessionRepository.save(session);
        }
        
        // 生成回答
        String answer = answerQuestionAboutContact(question, contact, user, session.getSessionId());
        
        log.info("Answered question for session {}: {}", session.getSessionId(), question);
        
        return QaResponse.builder()
            .answer(answer)
            .contactId(contact.getId())
            .needsMoreInfo(false)
            .isSupplementAnswer(false)
            .build();
    }
    
    /**
     * 生成最终回答
     */
    private QaResponse generateFinalAnswer(
        ConversationSession session, 
        String originalQuestion,
        String supplementInfo
    ) {
        Contact contact = loadContact(session.getContactId(), session.getUserId());
        User user = loadUser(session.getUserId());
        
        // 直接使用原始description和补充信息生成答案，不等待description更新
        String answer = answerQuestionAboutContactWithSupplement(
            originalQuestion, 
            contact.getDescription(), // 使用原始description
            user.getDescription(),
            session.getSessionId(),
            supplementInfo
        );
        
        log.info("Generated final answer for session {} with supplement, original question: {}", 
            session.getSessionId(), originalQuestion);
        
        return QaResponse.builder()
            .answer(answer)
            .contactId(contact.getId())
            .needsMoreInfo(false)
            .isSupplementAnswer(true)
            .build();
    }
    
    /**
     * 更新用户描述
     */
    private void updateUserDescription(Long userId, String updatedDescription) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("用户不存在"));
        
        user.setDescription(updatedDescription);
        userRepository.save(user);
        
        // 🔥 发布事件：触发 self value 重新计算（基于更新后的description）
        log.debug("Publishing UserDescriptionUpdatedEvent for user {}", userId);
        eventPublisher.publishEvent(new UserDescriptionUpdatedEvent(userId, updatedDescription));
        
        log.info("Updated user description for user {}", userId);
    }
    
    /**
     * 异步更新联系人描述（整合补充信息）
     */
    @Async
    public void asyncUpdateDescriptionWithSupplement(
        Contact contact, 
        String supplementQuestion, 
        String supplementInfo
    ) {
        try {
            String prompt = PromptTemplates.buildIntegrateSupplementToDescriptionPrompt(
                contact.getDescription(),
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
            String updatedDescription = response.getChoices().get(0).getMessage().getContent().trim();
            
            // 更新联系人描述
            contact.setDescription(updatedDescription);
            contactRepository.save(contact);
            
            // 🔥 发布事件：触发 self value 重新计算（基于更新后的description）
            log.debug("Publishing ContactDescriptionUpdatedEvent for contact {}", contact.getId());
            eventPublisher.publishEvent(new ContactDescriptionUpdatedEvent(contact.getId(), updatedDescription));
            
            log.info("Async updated contact {} description with supplement info, original length: {}, new length: {}", 
                contact.getId(),
                contact.getDescription() != null ? contact.getDescription().length() : 0, 
                updatedDescription.length());
        } catch (Exception e) {
            log.error("Failed to async update contact {} description: {}", contact.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * 使用原始描述和补充信息直接回答问题的专用方法
     */
    private String answerQuestionAboutContactWithSupplement(
        String question, 
        String contactDescription,
        String userDescription,
        String sessionId,
        String supplementInfo
    ) {
        // Step 1: 构建基础系统Prompt（不包含历史对话）
        String systemPrompt = PromptTemplates.buildBaseContactQaSystemPrompt(
            contactDescription,
            "", // 不使用self_value，因为这是临时回答
            userDescription,
            "" // 不使用self_value，因为这是临时回答
        );
        
        // Step 2: 构建原生多轮消息数组
        List<ChatMessage> messages = new ArrayList<>();
        
        // 添加系统提示
        messages.add(new ChatMessage("system", systemPrompt));
        
        // Step 3: 添加历史对话（原生格式）
        List<QaHistoryEntry> qaHistory = qaHistoryService.getContactQaHistory(sessionId);
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
        
        log.info("Calling Deepseek with {} messages for session {} (with supplement)", 
            messages.size(), sessionId);
        
        ChatCompletionResponse response = deepseekClient.chat(request).block();
        String answer = response.getChoices().get(0).getMessage().getContent();
        
        // Step 7: 保存或更新QA历史
        if (supplementInfo != null && !supplementInfo.trim().isEmpty()) {
            // 有补充信息的情况 - 更新最后一条历史记录
            List<QaHistoryEntry> history = qaHistoryService.getContactQaHistory(sessionId);
            if (!history.isEmpty()) {
                QaHistoryEntry lastEntry = history.get(history.size() - 1);
                // 更新补充回答和最终答案
                lastEntry.setSupplementAnswer(supplementInfo);
                lastEntry.setAnswer(answer);
                qaHistoryService.updateLastContactQaEntry(sessionId, lastEntry);
                
                log.info("Updated last QA entry with supplement for session {}, question: {}", 
                    sessionId, question);
            } else {
                // 如果历史为空（异常情况），创建新记录
                log.warn("No history found when processing supplement, creating new entry for session {}", 
                    sessionId);
                QaHistoryEntry qaEntry = new QaHistoryEntry(
                    question,
                    answer,
                    "（系统请求补充信息）",
                    supplementInfo,
                    true
                );
                qaHistoryService.addContactQaEntry(sessionId, qaEntry);
            }
        } else {
            // 直接回答的情况 - 添加新记录
            QaHistoryEntry qaEntry = new QaHistoryEntry(question, answer);
            qaHistoryService.addContactQaEntry(sessionId, qaEntry);
            
            log.info("Saved new QA entry for session {}, question: {}", sessionId, question);
        }
        
        return answer;
    }
    
    /**
     * 使用Deepseek回答关于联系人的问题（重载方法，无补充信息）
     */
    private String answerQuestionAboutContact(String question, Contact contact, User user, String sessionId) {
        return answerQuestionAboutContact(question, contact, user, sessionId, null);
    }
    
    /**
     * 使用Deepseek回答关于联系人的问题（原生多轮对话版本）
     */
    private String answerQuestionAboutContact(
        String question, 
        Contact contact, 
        User user, 
        String sessionId,
        String supplementInfo
    ) {
        // Step 1: 构建基础系统Prompt（不包含历史对话）
        String systemPrompt = PromptTemplates.buildBaseContactQaSystemPrompt(
            contact.getDescription(),
            contact.getSelfValue(),
            user.getDescription(),
            user.getSelfValue()
        );
        
        // Step 2: 构建原生多轮消息数组
        List<ChatMessage> messages = new ArrayList<>();
        
        // 添加系统提示
        messages.add(new ChatMessage("system", systemPrompt));
        
        // Step 3: 添加历史对话（原生格式）
        List<QaHistoryEntry> qaHistory = qaHistoryService.getContactQaHistory(sessionId);
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
        request.setModel(deepseekProperties.getReasonerModel());
        request.setMessages(messages);
        request.setTemperature(0.7);
        
        log.info("Calling Deepseek with {} messages for session {}", 
            messages.size(), sessionId);
        
        // 使用 reasoner 模型进行问答
        request.setModel(deepseekProperties.getReasonerModel());
        ChatCompletionResponse response = deepseekClient.chat(request).block();
        String answer = response.getChoices().get(0).getMessage().getContent();
        
        // Step 7: 保存或更新QA历史
        if (supplementInfo != null && !supplementInfo.trim().isEmpty()) {
            // 有补充信息的情况 - 更新最后一条历史记录
            List<QaHistoryEntry> history = qaHistoryService.getContactQaHistory(sessionId);
            if (!history.isEmpty()) {
                QaHistoryEntry lastEntry = history.get(history.size() - 1);
                // 更新补充回答和最终答案
                lastEntry.setSupplementAnswer(supplementInfo);
                lastEntry.setAnswer(answer);
                qaHistoryService.updateLastContactQaEntry(sessionId, lastEntry);
                
                log.info("Updated last QA entry with supplement for session {}, question: {}", 
                    sessionId, question);
            } else {
                // 如果历史为空（异常情况），创建新记录
                log.warn("No history found when processing supplement, creating new entry for session {}", 
                    sessionId);
                QaHistoryEntry qaEntry = new QaHistoryEntry(
                    question,
                    answer,
                    "（系统请求补充信息）",
                    supplementInfo,
                    true
                );
                qaHistoryService.addContactQaEntry(sessionId, qaEntry);
            }
        } else {
            // 直接回答的情况 - 添加新记录
            QaHistoryEntry qaEntry = new QaHistoryEntry(question, answer);
            qaHistoryService.addContactQaEntry(sessionId, qaEntry);
            
            log.info("Saved new QA entry for session {}, question: {}", sessionId, question);
        }
        
        return answer;
    }
}

