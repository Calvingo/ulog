package com.ulog.backend.conversation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulog.backend.ai.DeepseekClient;
import com.ulog.backend.ai.dto.ChatCompletionRequest;
import com.ulog.backend.ai.dto.ChatCompletionResponse;
import com.ulog.backend.ai.dto.ChatMessage;
import com.ulog.backend.config.DeepseekProperties;
import com.ulog.backend.common.exception.BadRequestException;
import com.ulog.backend.common.exception.NotFoundException;
import com.ulog.backend.contact.dto.ContactRequest;
import com.ulog.backend.contact.dto.ContactResponse;
import com.ulog.backend.contact.service.ContactService;
import com.ulog.backend.conversation.dto.ExtractionResult;
import com.ulog.backend.conversation.dto.MessageResponse;
import com.ulog.backend.conversation.dto.StartCollectionResponse;
import com.ulog.backend.conversation.enums.SessionStatus;
import com.ulog.backend.conversation.util.PromptTemplates;
import com.ulog.backend.domain.conversation.ConversationSession;
import com.ulog.backend.repository.ConversationSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class InfoCollectionServiceImpl implements InfoCollectionService {
    
    private static final Logger log = LoggerFactory.getLogger(InfoCollectionServiceImpl.class);
    
    private final ConversationSessionRepository sessionRepository;
    private final DeepseekClient deepseekClient;
    private final ContactService contactService;
    private final ObjectMapper objectMapper;
    private final DeepseekProperties deepseekProperties;
    
    // 收集维度定义 - 基于专业框架
    private static final List<String> COLLECTION_DIMENSIONS = List.of(
        // 系统1: 基本画像系统
        "基本信息",
        "社会角色", 
        "生活方式",
        "社交风格",
        "性格特质",
        "自我价值",
        
        // 系统2: 心理与人格系统
        "核心动机",
        "情绪模式",
        "决策风格",
        
        // 系统3: 关系体验系统
        "互动频率",
        "互动能量",
        "信任水平",
        "价值互惠",
        "关系边界",
        "关系母型",
        
        // 系统4: 时间与发展系统
        "关系起点",
        "关系长度",
        "成长趋势",
        "临界事件",
        "未来潜力",
        
        // 系统5: 价值与意义系统
        "角色标签",
        "关系功能",
        "自我影响",
        "社交位置",
        "投入产出"
    );
    
    // 维度到要素的映射 - 基于专业框架
    private static final Map<String, List<String>> DIMENSION_FIELDS = createDimensionFieldsMap();
    
    private static Map<String, List<String>> createDimensionFieldsMap() {
        Map<String, List<String>> map = new HashMap<>();
        
        // 系统1: 基本画像系统
        map.put("基本信息", List.of("age", "occupation", "education", "city"));
        map.put("社会角色", List.of("work_type", "industry_status", "identity_tag"));
        map.put("生活方式", List.of("daily_routine", "exercise_frequency", "eating_habits", "leisure_hobby"));
        map.put("社交风格", List.of("social_frequency", "social_activity_preference"));
        map.put("性格特质", List.of("personality_characteristics", "mbti_type"));
        map.put("自我价值", List.of("self_esteem", "self_acceptance", "self_efficacy"));
        
        // 系统2: 心理与人格系统
        map.put("核心动机", List.of("core_values", "motivation_drivers"));
        map.put("情绪模式", List.of("emotional_stability", "empathy_level"));
        map.put("决策风格", List.of("decision_making_style", "thinking_preference"));
        
        // 系统3: 关系体验系统
        map.put("互动频率", List.of("meeting_frequency", "chat_frequency"));
        map.put("互动能量", List.of("interaction_energy", "emotional_support_level"));
        map.put("信任水平", List.of("trust_level", "information_transparency"));
        map.put("价值互惠", List.of("emotional_value", "information_value", "social_resource_value"));
        map.put("关系边界", List.of("privacy_respect", "balance_giving"));
        map.put("关系母型", List.of("relationship_archetype", "role_dynamics"));
        
        // 系统4: 时间与发展系统
        map.put("关系起点", List.of("acquaintance_channel", "first_meeting_context"));
        map.put("关系长度", List.of("years_known", "relationship_development_stage"));
        map.put("成长趋势", List.of("relationship_trend", "closeness_level"));
        map.put("临界事件", List.of("shared_experiences", "conflicts", "cooperation_events"));
        map.put("未来潜力", List.of("development_potential", "relationship_sustainability"));
        
        // 系统5: 价值与意义系统
        map.put("角色标签", List.of("role_tags", "identity_in_my_life"));
        map.put("关系功能", List.of("companionship", "reflection", "resource_exchange"));
        map.put("自我影响", List.of("enhancement_feeling", "pressure_feeling", "mirror_self"));
        map.put("社交位置", List.of("core_circle_position", "social_network_role"));
        map.put("投入产出", List.of("time_investment", "emotional_investment", "return_balance"));
        
        return map;
    }
    
    public InfoCollectionServiceImpl(
        ConversationSessionRepository sessionRepository,
        DeepseekClient deepseekClient,
        ContactService contactService,
        ObjectMapper objectMapper,
        DeepseekProperties deepseekProperties
    ) {
        this.sessionRepository = sessionRepository;
        this.deepseekClient = deepseekClient;
        this.contactService = contactService;
        this.objectMapper = objectMapper;
        this.deepseekProperties = deepseekProperties;
    }
    
    @Override
    @Transactional
    public StartCollectionResponse startCollection(Long userId, String contactName) {
        // 1. 创建会话
        String sessionId = "sess_" + UUID.randomUUID().toString().replace("-", "");
        ConversationSession session = new ConversationSession(sessionId, userId, contactName);
        session.setCurrentDimension(COLLECTION_DIMENSIONS.get(0));
        session.setCompletedDimensions("[]");
        session.setCollectedData("{}");
        session.setConversationHistory("[]");
        
        // 2. 生成第一个问题
        String firstQuestion = generateFirstQuestion(contactName);
        session.setLastQuestion(firstQuestion);
        
        // 3. 保存会话
        sessionRepository.save(session);
        
        log.info("Started collection session {} for user {} with contact {}", 
            sessionId, userId, contactName);
        
        return new StartCollectionResponse(
            sessionId,
            firstQuestion,
            0,
            COLLECTION_DIMENSIONS.get(0)
        );
    }
    
    @Override
    @Transactional
    public MessageResponse processMessage(String sessionId, Long userId, String userMessage) {
        // 1. 加载会话
        ConversationSession session = loadSession(sessionId, userId);
        
        // 2. 检查会话状态，根据不同状态处理
        String currentStatus = session.getStatus();
        
        if (SessionStatus.CONFIRMING_END.name().equals(currentStatus)) {
            log.info("Processing message in CONFIRMING_END state for session {}", sessionId);
            return handleConfirmingEndState(session, userMessage);
        }
        
        if (SessionStatus.REQUESTING_MINIMUM.name().equals(currentStatus)) {
            log.info("Processing message in REQUESTING_MINIMUM state for session {}", sessionId);
            return handleRequestingMinimumState(session, userMessage);
        }
        
        if (SessionStatus.COMPLETED.name().equals(currentStatus)) {
            throw new BadRequestException("会话已完成，请使用QA模式继续对话");
        }
        
        if (SessionStatus.ABANDONED.name().equals(currentStatus)) {
            throw new BadRequestException("会话已放弃");
        }
        
        // 3. ACTIVE状态：正常流程
        log.info("Processing message in ACTIVE state for session {}", sessionId);
        
        // 4. 解析已收集的数据
        Map<String, Object> collectedData = parseCollectedData(session);
        log.debug("Session {} collected data: {}", sessionId, collectedData);
        
        // 5. 智能提取信息
        ExtractionResult extraction = extractInformationWithIntent(
            userMessage,
            session.getCurrentDimension(),
            collectedData,
            session.getLastQuestion()
        );
        
        log.info("Session {}: extracted intent={}, wantsToEnd={}, updates={}", 
            sessionId, extraction.getIntent(), extraction.isWantsToEnd(), extraction.getUpdates());
        
        // 6. 更新数据
        if (extraction.getUpdates() != null && !extraction.getUpdates().isEmpty()) {
            collectedData.putAll(extraction.getUpdates());
            session.setCollectedData(toJson(collectedData));
        }
        
        // 7. 添加对话历史
        addToHistory(session, userMessage, extraction.getIntent().name());
        
        // 8. 检查是否想结束 - 增强逻辑
        boolean deepseekWantsToEnd = extraction != null && extraction.isWantsToEnd();
        boolean localWantsToEnd = isEndIntent(userMessage, calculateProgress(session));
        
        if (deepseekWantsToEnd || localWantsToEnd) {
            log.info("Detected end intent in session {}, message: {}, deepseek={}, local={}", 
                sessionId, userMessage, deepseekWantsToEnd, localWantsToEnd);
            return handleEndIntent(session, extraction, collectedData, localWantsToEnd);
        }
        
        // 9. 正常流程：决定下一个维度
        boolean shouldContinueCurrent = extraction.isShouldContinueCurrentQuestion();
        String nextDimension;
        
        if (shouldContinueCurrent) {
            // 继续当前维度，不标记完成
            nextDimension = session.getCurrentDimension();
        } else {
            // 切换到下一个维度，标记当前完成
            markDimensionCompleted(session, session.getCurrentDimension());
            nextDimension = getNextDimension(session);
        }
        
        // 10. 生成下一个问题
        String nextQuestion = generateNextQuestion(
            session.getContactName(),
            nextDimension,
            getCompletedDimensions(session),
            collectedData,
            userMessage
        );
        
        // 11. 更新会话
        session.setCurrentDimension(nextDimension);
        session.setLastQuestion(nextQuestion);
        sessionRepository.save(session);
        
        // 12. 检查是否应该完成
        int progress = calculateProgress(session);
        boolean shouldComplete = shouldComplete(session);
        
        log.info("Session {} progress: {}, shouldComplete: {}", sessionId, progress, shouldComplete);
        
        // 13. 返回响应
        MessageResponse response = new MessageResponse();
        response.setNextQuestion(nextQuestion);
        response.setIsCompleted(shouldComplete);
        response.setProgress(progress);
        response.setCurrentDimension(nextDimension);
        response.setIntent(extraction.getIntent().name());
        
        // 如果需要完成，添加完成相关信息
        if (shouldComplete) {
            response.setNeedsMinimumInfo(!checkMinimumInfo(collectedData));
            response.setMinimumInfoHint(checkMinimumInfo(collectedData) ? null : "需要至少2条有效信息才能创建联系人");
        }
        
        return response;
    }
    
    /**
     * 处理CONFIRMING_END状态
     */
    private MessageResponse handleConfirmingEndState(
        ConversationSession session,
        String userMessage
    ) {
        Map<String, Object> collectedData = parseCollectedData(session);
        
        // 检测用户是否确认
        if (isConfirmation(userMessage)) {
            log.info("User confirmed to end collection in session {}", session.getSessionId());
            return completeAndCreateContact(session, collectedData);
        } else if (isContinue(userMessage)) {
            log.info("User wants to continue in session {}", session.getSessionId());
            // 恢复ACTIVE状态，继续对话
            session.setStatus(SessionStatus.ACTIVE.name());
            sessionRepository.save(session);
            // 生成下一个问题
            String nextQuestion = generateNextQuestion(
                session.getContactName(),
                session.getCurrentDimension(),
                getCompletedDimensions(session),
                collectedData,
                userMessage
            );
            session.setLastQuestion(nextQuestion);
            sessionRepository.save(session);
            
            MessageResponse response = new MessageResponse();
            response.setNextQuestion(nextQuestion);
            response.setIsCompleted(false);
            response.setProgress(calculateProgress(session));
            response.setCurrentDimension(session.getCurrentDimension());
            return response;
        } else {
            // 重复确认问题
            return confirmEnd(session, collectedData);
        }
    }
    
    /**
     * 处理REQUESTING_MINIMUM状态
     */
    private MessageResponse handleRequestingMinimumState(
        ConversationSession session,
        String userMessage
    ) {
        Map<String, Object> collectedData = parseCollectedData(session);
        
        // 提取用户补充的信息
        ExtractionResult extraction = extractInformationWithIntent(
            userMessage,
            session.getCurrentDimension(),
            collectedData,
            session.getLastQuestion()
        );
        
        log.info("Extracted info in REQUESTING_MINIMUM state: {}", extraction.getUpdates());
        
        // 更新数据
        if (extraction.getUpdates() != null && !extraction.getUpdates().isEmpty()) {
            collectedData.putAll(extraction.getUpdates());
            session.setCollectedData(toJson(collectedData));
        }
        
        // 添加对话历史
        addToHistory(session, userMessage, extraction.getIntent().name());
        
        // 检查是否满足最低要求
        if (checkMinimumInfo(collectedData)) {
            log.info("Minimum info satisfied, creating contact for session {}", session.getSessionId());
            // 恢复ACTIVE状态并创建联系人
            return completeAndCreateContact(session, collectedData);
        } else {
            log.info("Minimum info still not satisfied for session {}", session.getSessionId());
            // 仍然不够，继续请求
            return askForMinimumInfo(session, collectedData);
        }
    }
    
    /**
     * 处理结束意图 - 修改逻辑，当本地识别到结束意图时强制处理
     */
    private MessageResponse handleEndIntent(
        ConversationSession session,
        ExtractionResult extraction,
        Map<String, Object> collectedData,
        boolean localWantsToEnd
    ) {
        boolean hasMinInfo = checkMinimumInfo(collectedData);
        
        // 如果本地识别到结束意图，强制按结束意图处理，不依赖Deepseek的EndConfidence
        if (localWantsToEnd) {
            log.info("Local end intent detected, forcing end flow for session {}", session.getSessionId());
            
            // 检查是否有最低信息
            if (!hasMinInfo) {
                log.info("Insufficient minimum info, requesting more info before end");
                return forceAskMinimumInfo(session, collectedData);
            } else {
                log.info("Sufficient info available, confirming end");
                return confirmEnd(session, collectedData);
            }
        }
        
        // 如果只是Deepseek识别到结束意图，按原来的逻辑处理
        switch (extraction.getEndConfidence()) {
            case WEAK:
                // 弱信号：只是跳过，继续下一个维度
                return continueWithNextDimension(session, collectedData);
                
            case MEDIUM:
                // 中等信号：确认是否真的要结束
                if (!hasMinInfo) {
                    return askForMinimumInfo(session, collectedData);
                } else {
                    return confirmEnd(session, collectedData);
                }
                
            case STRONG:
                // 强烈信号：直接结束
                if (!hasMinInfo) {
                    return forceAskMinimumInfo(session, collectedData);
                } else {
                    return completeAndCreateContact(session, collectedData);
                }
                
            default:
                return continueWithNextDimension(session, collectedData);
        }
    }
    
    /**
     * 继续下一个维度
     */
    private MessageResponse continueWithNextDimension(
        ConversationSession session,
        Map<String, Object> collectedData
    ) {
        // 标记当前维度为完成
        markDimensionCompleted(session, session.getCurrentDimension());
        
        String nextDimension = getNextDimension(session);
        String nextQuestion = generateNextQuestion(
            session.getContactName(),
            nextDimension,
            getCompletedDimensions(session),
            collectedData,
            "（用户跳过）"
        );
        
        session.setCurrentDimension(nextDimension);
        session.setLastQuestion(nextQuestion);
        sessionRepository.save(session);
        
        MessageResponse response = new MessageResponse();
        response.setNextQuestion(nextQuestion);
        response.setIsCompleted(false);
        response.setProgress(calculateProgress(session));
        response.setCurrentDimension(nextDimension);
        return response;
    }
    
    /**
     * 请求最低必要信息
     */
    private MessageResponse askForMinimumInfo(
        ConversationSession session,
        Map<String, Object> collectedData
    ) {
        String question = generateMinimumInfoQuestion(
            session.getContactName(),
            collectedData
        );
        
        session.setStatus(SessionStatus.REQUESTING_MINIMUM.name());
        session.setLastQuestion(question);
        sessionRepository.save(session);
        
        MessageResponse response = new MessageResponse();
        response.setNextQuestion(question);
        response.setIsCompleted(false);
        response.setNeedsMinimumInfo(true);
        response.setMinimumInfoHint("为了创建联系人，还需要一些基本信息");
        response.setProgress(calculateProgress(session));
        response.setCurrentDimension(session.getCurrentDimension());
        return response;
    }
    
    /**
     * 强制请求最低信息
     */
    private MessageResponse forceAskMinimumInfo(
        ConversationSession session,
        Map<String, Object> collectedData
    ) {
        String question = generateMinimumInfoQuestion(
            session.getContactName(),
            collectedData
        );
        
        session.setStatus(SessionStatus.REQUESTING_MINIMUM.name());
        session.setLastQuestion(question);
        sessionRepository.save(session);
        
        MessageResponse response = new MessageResponse();
        response.setNextQuestion(question);
        response.setIsCompleted(false);
        response.setNeedsMinimumInfo(true);
        response.setMinimumInfoHint("我理解你想结束了。不过为了创建联系人，只需要再回答1-2个关键问题😊");
        response.setProgress(calculateProgress(session));
        response.setCurrentDimension(session.getCurrentDimension());
        return response;
    }
    
    /**
     * 确认是否结束
     */
    private MessageResponse confirmEnd(
        ConversationSession session,
        Map<String, Object> collectedData
    ) {
        session.setStatus(SessionStatus.CONFIRMING_END.name());
        sessionRepository.save(session);
        
        MessageResponse response = new MessageResponse();
        response.setNextQuestion("好的，了解了。那我们就根据这些信息创建联系人吧？（回复'是'继续，或'再想想'继续补充）");
        response.setIsCompleted(false);
        response.setIsConfirmingEnd(true);
        response.setCollectedSummary(generateBriefSummary(collectedData));
        response.setProgress(calculateProgress(session));
        response.setCurrentDimension(session.getCurrentDimension());
        return response;
    }
    
    /**
     * 完成并创建联系人
     */
    @Transactional
    private MessageResponse completeAndCreateContact(
        ConversationSession session,
        Map<String, Object> collectedData
    ) {
        try {
            // 1. 预检查
            log.info("Starting contact creation for session {}, collected data: {}", 
                session.getSessionId(), collectedData);
            
            // 2. 生成description
            String description = generateDescription(
                session.getContactName(),
                collectedData
            );
            
            log.info("Generated description for {}: {}", session.getContactName(), description);
            
            // 3. 调用创建联系人API
            ContactRequest contactRequest = new ContactRequest();
            contactRequest.setName(session.getContactName());
            contactRequest.setDescription(description);
            
            ContactResponse contact = contactService.create(session.getUserId(), contactRequest);
            
            log.info("Successfully created contact {} for session {}", 
                contact.id(), session.getSessionId());
            
            // 4. 更新会话状态
            session.setStatus(SessionStatus.COMPLETED.name());
            session.setContactId(contact.id());
            session.setCompletedAt(LocalDateTime.now());
            session.setFinalDescription(description);
            session.setLastQuestion(null);  // 清空lastQuestion，避免干扰QA阶段
            sessionRepository.save(session);
            
            log.info("Session {} marked as COMPLETED", session.getSessionId());
            
            // 5. 返回完成响应
            MessageResponse response = new MessageResponse();
            response.setIsCompleted(true);
            response.setContact(contact);
            response.setSessionId(session.getSessionId());
            response.setNextMode("qa");
            response.setCompletionMessage(PromptTemplates.buildCompletionMessage(contact.name()));
            response.setSuggestedActions(PromptTemplates.getSuggestedActions());
            return response;
                
        } catch (Exception e) {
            log.error("Failed to create contact for session {}: {}", 
                session.getSessionId(), e.getMessage(), e);
            
            // 不恢复到ACTIVE状态，保持当前状态让用户知道发生了什么
            // 添加更详细的错误信息
            String errorMessage = "创建联系人失败: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += " (原因: " + e.getCause().getMessage() + ")";
            }
            
            throw new BadRequestException(errorMessage);
        }
    }
    
    /**
     * 检查是否有最低必要信息 - 改为只要有任意有效信息就通过
     */
    private boolean checkMinimumInfo(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            log.info("No collected data available");
            return false;
        }
        
        // 改为：只要有任意有效信息就通过
        int validInfoCount = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (hasValidValue(entry.getValue())) {
                validInfoCount++;
                log.info("Found valid info: {} = {}", entry.getKey(), entry.getValue());
            }
        }
        
        boolean result = validInfoCount > 0;
        log.info("Minimum info check result: {} (valid info count: {})", result, validInfoCount);
        return result;
    }
    
    /**
     * 生成第一个问题
     */
    private String generateFirstQuestion(String contactName) {
        String prompt = PromptTemplates.buildFirstQuestionPrompt(contactName);
        return callDeepseek(prompt);
    }
    
    /**
     * 获取下一个问题生成策略
     */
    private String getNextQuestionStrategy(String currentDimension, Map<String, Object> collectedData) {
        List<String> targetFields = DIMENSION_FIELDS.get(currentDimension);
        if (targetFields == null) {
            return "继续收集当前维度信息";
        }
        
        // 检查当前维度已收集的字段
        List<String> collectedFields = new ArrayList<>();
        for (String field : targetFields) {
            if (collectedData.containsKey(field) && hasValidValue(collectedData.get(field))) {
                collectedFields.add(field);
            }
        }
        
        // 基于收集情况确定策略
        if (collectedFields.isEmpty()) {
            return "开始收集" + currentDimension + "信息";
        } else if (collectedFields.size() < targetFields.size()) {
            return "继续收集" + currentDimension + "的更多信息";
        } else {
            return "当前维度信息已足够，可以进入下一个维度";
        }
    }
    
    /**
     * 获取问题生成模板
     */
    private String getQuestionTemplate(String currentDimension, String field) {
        Map<String, String> templates = createQuestionTemplates();
        return templates.getOrDefault(field, "请告诉我关于{}的更多信息");
    }
    
    private Map<String, String> createQuestionTemplates() {
        Map<String, String> templates = new HashMap<>();
        
        // 基本信息
        templates.put("age", "{}的年龄大概是多少？");
        templates.put("occupation", "{}的职业是什么？");
        templates.put("education", "{}的教育背景如何？");
        templates.put("city", "{}现在在哪个城市？");
        
        // 社会角色
        templates.put("work_type", "{}的工作类型是什么？");
        templates.put("industry_status", "{}在行业中的地位如何？");
        templates.put("identity_tag", "{}的身份标签是什么？");
        
        // 生活方式
        templates.put("daily_routine", "{}的作息规律如何？");
        templates.put("exercise_frequency", "{}的运动频率如何？");
        templates.put("eating_habits", "{}的饮食习惯如何？");
        templates.put("leisure_hobby", "{}的休闲爱好是什么？");
        
        // 社交风格
        templates.put("social_frequency", "{}的社交频率如何？");
        templates.put("social_activity_preference", "{}是否喜欢参加社交活动？");
        
        // 性格特质
        templates.put("personality_characteristics", "{}的性格特点是什么？");
        templates.put("mbti_type", "{}的MBTI类型是什么？");
        
        // 自我价值
        templates.put("self_esteem", "{}的自尊水平如何？");
        templates.put("self_acceptance", "{}的自我接纳程度如何？");
        templates.put("self_efficacy", "{}的自我效能感如何？");
        
        // 核心动机
        templates.put("core_values", "{}的核心价值观是什么？");
        templates.put("motivation_drivers", "{}的动机驱动因素是什么？");
        
        // 情绪模式
        templates.put("emotional_stability", "{}的情绪稳定性如何？");
        templates.put("empathy_level", "{}的共情能力如何？");
        
        // 决策风格
        templates.put("decision_making_style", "{}的决策风格是什么？");
        templates.put("thinking_preference", "{}的思维方式偏向感性还是理性？");
        
        // 关系体验系统
        templates.put("meeting_frequency", "你们每月见面的次数大概是多少？");
        templates.put("chat_frequency", "你们聊天的频率如何？");
        templates.put("interaction_energy", "和{}的互动让你感觉如何？");
        templates.put("emotional_support_level", "{}能给你提供情感支持吗？");
        templates.put("trust_level", "你对{}的信任程度如何？");
        templates.put("information_transparency", "你们之间的信息透明度如何？");
        templates.put("emotional_value", "{}能给你提供什么情感价值？");
        templates.put("information_value", "{}能给你提供什么信息价值？");
        templates.put("social_resource_value", "{}能给你提供什么社交资源价值？");
        templates.put("companionship_value", "{}能给你提供什么陪伴价值？");
        templates.put("privacy_respect", "{}是否尊重你的隐私？");
        templates.put("balance_giving", "你们之间的给予是否平衡？");
        templates.put("relationship_archetype", "你们的关系类型是什么？");
        templates.put("role_dynamics", "你们之间的角色动态如何？");
        
        // 时间与发展系统
        templates.put("acquaintance_channel", "你们是怎么认识的？");
        templates.put("first_meeting_context", "你们初次见面的背景是什么？");
        templates.put("years_known", "你们认识多少年了？");
        templates.put("relationship_development_stage", "你们的关系现在处于什么阶段？");
        templates.put("relationship_trend", "你们的关系发展趋势如何？");
        templates.put("closeness_level", "你们现在的亲密程度如何？");
        templates.put("shared_experiences", "你们一起经历过什么重要事件？");
        templates.put("conflicts", "你们之间有过什么冲突吗？");
        templates.put("cooperation_events", "你们有什么合作事件？");
        templates.put("development_potential", "你们的关系发展潜力如何？");
        templates.put("relationship_sustainability", "你们的关系可持续性如何？");
        
        // 价值与意义系统
        templates.put("role_tags", "{}在你生活中扮演什么角色？");
        templates.put("identity_in_my_life", "{}在你生活中的身份是什么？");
        templates.put("companionship", "{}能给你提供什么陪伴？");
        templates.put("reflection", "{}能给你提供什么反思？");
        templates.put("resource_exchange", "你们之间有什么资源交换？");
        templates.put("enhancement_feeling", "{}对你的自我提升有什么影响？");
        templates.put("pressure_feeling", "{}给你带来什么压力感？");
        templates.put("mirror_self", "{}如何反映你的自我？");
        templates.put("security_feeling", "{}给你带来什么安全感？");
        templates.put("core_circle_position", "{}在你的核心圈中处于什么位置？");
        templates.put("social_network_role", "{}在你的社交网络中扮演什么角色？");
        templates.put("time_investment", "你在{}身上投入多少时间？");
        templates.put("emotional_investment", "你在{}身上投入多少情感？");
        templates.put("return_balance", "你们之间的投入产出比如何？");
        
        return templates;
    }
    
    /**
     * 生成下一个问题
     */
    private String generateNextQuestion(
        String contactName,
        String currentDimension,
        List<String> completedDimensions,
        Map<String, Object> collectedData,
        String lastUserMessage
    ) {
        String prompt = PromptTemplates.buildNextQuestionPrompt(
            contactName,
            currentDimension,
            completedDimensions,
            collectedData,
            lastUserMessage
        );
        return callDeepseek(prompt);
    }
    
    /**
     * 生成最低信息问题 - 动态分析缺失信息
     */
    private String generateMinimumInfoQuestion(String contactName, Map<String, Object> data) {
        // 分析已收集的信息，确定缺失的关键信息
        List<String> missingInfo = analyzeMissingInfo(data);
        
        // 基于缺失信息生成动态问题
        String prompt = buildDynamicMinimumInfoPrompt(contactName, data, missingInfo);
        return callDeepseek(prompt);
    }
    
    /**
     * 分析缺失的关键信息
     */
    private List<String> analyzeMissingInfo(Map<String, Object> data) {
        List<String> missing = new ArrayList<>();
        
        // 检查基本信息
        if (!hasValidValue(data.get("age"))) {
            missing.add("age");
        }
        if (!hasValidValue(data.get("occupation"))) {
            missing.add("occupation");
        }
        if (!hasValidValue(data.get("relationship"))) {
            missing.add("relationship");
        }
        if (!hasValidValue(data.get("interaction"))) {
            missing.add("interaction");
        }
        
        log.info("Analyzed missing info: {}", missing);
        return missing;
    }
    
    /**
     * 构建动态最低信息提示词
     */
    private String buildDynamicMinimumInfoPrompt(String contactName, Map<String, Object> data, List<String> missingInfo) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("用户想结束问卷，但还需要补充一些关键信息。\n\n");
        prompt.append("联系人姓名：").append(contactName).append("\n");
        prompt.append("已收集信息：").append(formatCollectedData(data)).append("\n");
        prompt.append("缺失信息：").append(String.join(", ", missingInfo)).append("\n\n");
        
        prompt.append("请生成一个友好的问题，请求用户补充最重要的1条信息。\n");
        prompt.append("优先级：\n");
        
        if (missingInfo.contains("relationship")) {
            prompt.append("1. 关系（你们是什么关系？同事/朋友/家人等）\n");
        }
        if (missingInfo.contains("age")) {
            prompt.append("2. 年龄（大概多大？）\n");
        }
        if (missingInfo.contains("occupation")) {
            prompt.append("3. 职业（做什么工作？）\n");
        }
        if (missingInfo.contains("interaction")) {
            prompt.append("4. 互动方式（怎么联系？）\n");
        }
        
        prompt.append("\n要求：\n");
        prompt.append("1. 语气要理解用户想结束的心情\n");
        prompt.append("2. 说明只需要再回答1个问题\n");
        prompt.append("3. 问最重要的缺失信息\n");
        prompt.append("4. 简短、友好\n\n");
        prompt.append("只返回问题本身。");
        
        return prompt.toString();
    }
    
    /**
     * 生成description - 严格基于实际收集的数据
     */
    private String generateDescription(String contactName, Map<String, Object> collectedData) {
        log.info("Generating description for {} with collected data: {}", contactName, collectedData);
        
        // 先尝试使用Deepseek生成，但添加严格限制
        String prompt = PromptTemplates.buildDescriptionPrompt(contactName, collectedData);
        String deepseekDescription = callDeepseek(prompt);
        
        // 验证生成的描述是否包含编造内容
        if (isDescriptionValid(deepseekDescription, collectedData)) {
            log.info("Deepseek generated valid description: {}", deepseekDescription);
            return deepseekDescription;
        } else {
            log.warn("Deepseek generated invalid description, falling back to manual generation");
            return generateManualDescription(contactName, collectedData);
        }
    }
    
    /**
     * 验证描述是否有效（不包含编造内容）
     */
    private boolean isDescriptionValid(String description, Map<String, Object> collectedData) {
        if (description == null || description.trim().isEmpty()) {
            return false;
        }
        
        // 检查是否包含常见的编造词汇
        List<String> forbiddenWords = List.of(
            "专业能力扎实", "善于沟通", "思路清晰", "有建设性", "值得信赖", 
            "好搭档", "能力很强", "很专业", "经验丰富", "技术过硬"
        );
        
        String desc = description.toLowerCase();
        for (String word : forbiddenWords) {
            if (desc.contains(word.toLowerCase())) {
                log.warn("Description contains forbidden word: {}", word);
                return false;
            }
        }
        
        // 检查描述是否基于实际收集的数据
        boolean hasActualData = false;
        for (String key : collectedData.keySet()) {
            Object value = collectedData.get(key);
            if (value != null && hasValidValue(value)) {
                String valueStr = value.toString();
                if (description.contains(valueStr)) {
                    hasActualData = true;
                    break;
                }
            }
        }
        
        return hasActualData;
    }
    
    /**
     * 手动生成描述 - 严格基于实际数据
     */
    private String generateManualDescription(String contactName, Map<String, Object> collectedData) {
        StringBuilder description = new StringBuilder();
        description.append(contactName);
        
        // 基本信息
        if (collectedData.containsKey("age") && hasValidValue(collectedData.get("age"))) {
            description.append("，").append(collectedData.get("age"));
        }
        
        if (collectedData.containsKey("occupation") && hasValidValue(collectedData.get("occupation"))) {
            description.append("，职业是").append(collectedData.get("occupation"));
        }
        
        if (collectedData.containsKey("education") && hasValidValue(collectedData.get("education"))) {
            description.append("，教育背景是").append(collectedData.get("education"));
        }
        
        if (collectedData.containsKey("city") && hasValidValue(collectedData.get("city"))) {
            description.append("，在").append(collectedData.get("city"));
        }
        
        // 关系信息
        if (collectedData.containsKey("relationship") && hasValidValue(collectedData.get("relationship"))) {
            description.append("，是").append(collectedData.get("relationship"));
        }
        
        // 互动方式
        if (collectedData.containsKey("interaction") && hasValidValue(collectedData.get("interaction"))) {
            description.append("，").append(collectedData.get("interaction"));
        }
        
        // 性格特质
        if (collectedData.containsKey("personality") && hasValidValue(collectedData.get("personality"))) {
            description.append("，性格").append(collectedData.get("personality"));
        }
        
        // 兴趣爱好
        if (collectedData.containsKey("hobby") && hasValidValue(collectedData.get("hobby"))) {
            description.append("，爱好").append(collectedData.get("hobby"));
        }
        
        // 其他信息
        if (collectedData.containsKey("contact") && hasValidValue(collectedData.get("contact"))) {
            description.append("，联系方式").append(collectedData.get("contact"));
        }
        
        if (collectedData.containsKey("background") && hasValidValue(collectedData.get("background"))) {
            description.append("，").append(collectedData.get("background"));
        }
        
        description.append("。");
        
        String result = description.toString();
        log.info("Generated manual description: {}", result);
        return result;
    }
    
    /**
     * 智能提取信息 - 移除本地提取兜底，专注于AI响应
     */
    private ExtractionResult extractInformationWithIntent(
        String userMessage,
        String currentDimension,
        Map<String, Object> collectedData,
        String lastQuestion
    ) {
        String prompt = PromptTemplates.buildIntelligentExtractionPrompt(
            userMessage,
            currentDimension,
            collectedData,
            lastQuestion
        );
        
        log.debug("Calling Deepseek for extraction with prompt: {}", prompt);
        String jsonResponse = callDeepseek(prompt);
        log.info("Deepseek extraction response: {}", jsonResponse);
        
        try {
            // 解析JSON响应
            ExtractionResult result = objectMapper.readValue(jsonResponse, ExtractionResult.class);
            
            // 验证解析结果
            if (result == null) {
                log.error("Parsed result is null for response: {}", jsonResponse);
                throw new BadRequestException("AI响应解析失败，请重试");
            }
            
            log.info("Successfully parsed extraction result - intent: {}, updates: {}, wantsToEnd: {}", 
                result.getIntent(), result.getUpdates(), result.isWantsToEnd());
            
            return result;
            
        } catch (JsonProcessingException e) {
            log.error("Failed to parse extraction result: {}", jsonResponse, e);
            log.error("JSON parsing error details: {}", e.getMessage());
            
            // 直接抛出异常，不再使用本地提取兜底
            throw new BadRequestException("AI响应格式错误，请重试。错误详情: " + e.getMessage());
        }
    }
    
    
    /**
     * 调用Deepseek
     */
    private String callDeepseek(String prompt) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(deepseekProperties.getModel());
        request.setMessages(List.of(
            new ChatMessage("system", PromptTemplates.SYSTEM_ROLE),
            new ChatMessage("user", prompt)
        ));
        request.setTemperature(0.7);
        
        ChatCompletionResponse response = deepseekClient.chat(request).block();
        return response.getChoices().get(0).getMessage().getContent();
    }
    
    /**
     * 获取下一个维度（纯函数，无副作用）
     */
    private String getNextDimension(ConversationSession session) {
        int currentIndex = COLLECTION_DIMENSIONS.indexOf(session.getCurrentDimension());
        int nextIndex = currentIndex + 1;
        
        if (nextIndex < COLLECTION_DIMENSIONS.size()) {
            return COLLECTION_DIMENSIONS.get(nextIndex);
        } else {
            return session.getCurrentDimension();  // 已经是最后一个
        }
    }
    
    /**
     * 标记维度为完成
     */
    private void markDimensionCompleted(ConversationSession session, String dimension) {
        List<String> completed = getCompletedDimensions(session);
        if (!completed.contains(dimension)) {
            completed.add(dimension);
            session.setCompletedDimensions(toJson(completed));
            log.debug("Marked dimension '{}' as completed for session {}", dimension, session.getSessionId());
        }
    }
    
    /**
     * 计算进度 - 统一进度和完成判断逻辑
     */
    private Integer calculateProgress(ConversationSession session) {
        Map<String, Object> data = parseCollectedData(session);
        List<String> completed = getCompletedDimensions(session);
        
        // 维度进度 (60%)
        int dimensionProgress = (completed.size() * 60) / COLLECTION_DIMENSIONS.size();
        
        // 信息质量进度 (40%)
        int qualityProgress = calculateQualityProgress(data);
        
        return Math.min(dimensionProgress + qualityProgress, 100);
    }
    
    /**
     * 计算信息质量进度
     */
    private int calculateQualityProgress(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        
        int validInfoCount = 0;
        String[] keyFields = {"age", "occupation", "relationship", "interaction", "personality", "education"};
        
        for (String field : keyFields) {
            if (hasValidValue(data.get(field))) {
                validInfoCount++;
            }
        }
        
        return Math.min((validInfoCount * 40) / keyFields.length, 40);
    }
    
    /**
     * 检查是否有有效值
     */
    private boolean hasValidValue(Object value) {
        if (value == null) return false;
        String str = value.toString().trim();
        return !str.isEmpty() && !isNegativeAnswer(str);
    }
    
    /**
     * 判断是否为否定回答
     */
    private boolean isNegativeAnswer(String value) {
        if (value == null) return false;
        String str = value.trim().toLowerCase();
        return str.equals("无") || str.equals("没有") || str.equals("不知道") || 
               str.equals("不清楚") || str.equals("不确定") || str.equals("不太了解") ||
               str.equals("") || str.equals("null") || str.equals("undefined");
    }
    
    /**
     * 判断是否为结束意图 - 增强逻辑
     */
    private boolean isEndIntent(String message, int progress) {
        if (message == null) return false;
        
        String msg = message.trim().toLowerCase();
        
        // 明确结束信号
        List<String> endSignals = List.of(
            "结束", "完成", "够了", "差不多了", "就这些", "不想回答了",
            "结束问卷", "问卷结束", "不想继续", "不要了"
        );
        
        if (endSignals.stream().anyMatch(msg::contains)) {
            return true;
        }
        
        // 进度100% + 否定回答 = 可能想结束
        if (progress >= 100 && isNegativeAnswer(msg)) {
            return true;
        }
        
        // 连续否定回答 = 可能想结束
        return false; // 这个需要会话历史判断，暂时简化
    }
    
    /**
     * 统一完成判断逻辑
     */
    private boolean shouldComplete(ConversationSession session) {
        Map<String, Object> data = parseCollectedData(session);
        int progress = calculateProgress(session);
        
        // 条件1: 进度达到100%
        if (progress < 100) {
            return false;
        }
        
        // 条件2: 有最低必要信息
        if (!checkMinimumInfo(data)) {
            return false;
        }
        
        // 条件3: 用户明确表示结束或者所有维度都已完成
        List<String> completed = getCompletedDimensions(session);
        return completed.size() >= COLLECTION_DIMENSIONS.size() || 
               SessionStatus.CONFIRMING_END.name().equals(session.getStatus());
    }
    
    /**
     * 生成简要总结
     */
    private String generateBriefSummary(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        data.forEach((key, value) -> {
            if (value != null) {
                sb.append(key).append(": ").append(value).append("；");
            }
        });
        return sb.toString();
    }
    
    /**
     * 加载会话
     */
    private ConversationSession loadSession(String sessionId, Long userId) {
        return sessionRepository.findBySessionIdAndUserId(sessionId, userId)
            .orElseThrow(() -> new NotFoundException("会话不存在或无权访问"));
    }
    
    /**
     * 判断是否是确认词
     */
    private boolean isConfirmation(String message) {
        String msg = message.trim().toLowerCase();
        return msg.equals("是") || msg.equals("是的") || msg.equals("好") || 
               msg.equals("可以") || msg.equals("确认") || msg.equals("ok") || 
               msg.equals("yes") || msg.equals("好的") || msg.equals("没问题");
    }
    
    /**
     * 判断是否想继续
     */
    private boolean isContinue(String message) {
        String msg = message.trim();
        return msg.contains("继续") || msg.contains("再想想") || msg.contains("再说说") ||
               msg.contains("还有") || msg.contains("补充") || msg.contains("不是");
    }
    
    /**
     * 解析收集的数据
     */
    private Map<String, Object> parseCollectedData(ConversationSession session) {
        try {
            String json = session.getCollectedData();
            if (json == null || json.trim().isEmpty() || "{}".equals(json)) {
                return new HashMap<>();
            }
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse collected data", e);
            return new HashMap<>();
        }
    }
    
    /**
     * 格式化收集的数据用于显示
     */
    private String formatCollectedData(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return "（暂无）";
        }
        
        StringBuilder sb = new StringBuilder();
        data.forEach((key, value) -> {
            if (value != null) {
                sb.append(String.format("- %s: %s\n", key, value));
            }
        });
        
        return sb.toString();
    }
    
    /**
     * 获取已完成的维度
     */
    private List<String> getCompletedDimensions(ConversationSession session) {
        try {
            String json = session.getCompletedDimensions();
            if (json == null || json.trim().isEmpty() || "[]".equals(json)) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse completed dimensions", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 添加对话历史
     */
    private void addToHistory(ConversationSession session, String userMessage, String intent) {
        try {
            List<Map<String, String>> history = objectMapper.readValue(
                session.getConversationHistory() == null || session.getConversationHistory().isEmpty() 
                    ? "[]" 
                    : session.getConversationHistory(),
                new TypeReference<List<Map<String, String>>>() {}
            );
            
            Map<String, String> entry = new HashMap<>();
            entry.put("timestamp", LocalDateTime.now().toString());
            entry.put("user", userMessage);
            entry.put("intent", intent);
            entry.put("question", session.getLastQuestion());
            
            history.add(entry);
            session.setConversationHistory(objectMapper.writeValueAsString(history));
        } catch (JsonProcessingException e) {
            log.error("Failed to update conversation history", e);
        }
    }
    
    /**
     * 转JSON
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert object to JSON: {}", obj, e);
            // 抛出异常而不是返回空对象，避免数据丢失
            throw new BadRequestException("数据序列化失败，请联系技术支持。TraceId可用于问题排查");
        }
    }
    
    @Override
    public Integer getProgress(String sessionId, Long userId) {
        ConversationSession session = loadSession(sessionId, userId);
        return calculateProgress(session);
    }
    
    @Override
    @Transactional
    public void abandonSession(String sessionId, Long userId) {
        ConversationSession session = loadSession(sessionId, userId);
        session.setStatus(SessionStatus.ABANDONED.name());
        sessionRepository.save(session);
        log.info("Abandoned session {} by user {}", sessionId, userId);
    }
}

