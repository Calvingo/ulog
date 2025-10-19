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
import com.ulog.backend.conversation.dto.ExtractionResult;
import com.ulog.backend.conversation.dto.StartUserCollectionResponse;
import com.ulog.backend.conversation.dto.UserMessageResponse;
import com.ulog.backend.conversation.enums.SessionStatus;
import com.ulog.backend.conversation.util.PromptTemplates;
import com.ulog.backend.domain.conversation.UserConversationSession;
import com.ulog.backend.repository.UserConversationSessionRepository;
import com.ulog.backend.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserInfoCollectionServiceImpl implements UserInfoCollectionService {
    
    private static final Logger log = LoggerFactory.getLogger(UserInfoCollectionServiceImpl.class);
    
    private final UserConversationSessionRepository sessionRepository;
    private final DeepseekClient deepseekClient;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final DeepseekProperties deepseekProperties;
    private final SelfValueCalculationService selfValueCalculationService;
    
    // 收集维度定义 - 基于专业框架（与联系人相同）
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
    
    public UserInfoCollectionServiceImpl(
        UserConversationSessionRepository sessionRepository,
        DeepseekClient deepseekClient,
        UserService userService,
        ObjectMapper objectMapper,
        DeepseekProperties deepseekProperties,
        SelfValueCalculationService selfValueCalculationService
    ) {
        this.sessionRepository = sessionRepository;
        this.deepseekClient = deepseekClient;
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.deepseekProperties = deepseekProperties;
        this.selfValueCalculationService = selfValueCalculationService;
    }
    
    @Override
    @Transactional
    public StartUserCollectionResponse startCollection(Long userId) {
        log.info("Starting user self-information collection for userId: {}", userId);
        
        // 检查是否已有活跃会话
        Optional<UserConversationSession> existingSession = sessionRepository.findByUserIdAndStatusIn(
            userId,
            List.of(SessionStatus.ACTIVE, SessionStatus.REQUESTING_MINIMUM, SessionStatus.CONFIRMING_END)
        );
        
        if (existingSession.isPresent()) {
            throw new BadRequestException("你已经有一个进行中的自我信息收集会话");
        }
        
        // 创建新会话
        UserConversationSession session = new UserConversationSession();
        session.setSessionId("user_sess_" + UUID.randomUUID().toString().replace("-", ""));
        session.setUserId(userId);
        session.setStatus(SessionStatus.ACTIVE);
        session.setCurrentDimension(COLLECTION_DIMENSIONS.get(0));
        session.setCompletedDimensions("[]");
        session.setCollectedData("{}");
        session.setConversationHistory("[]");
        
        // 生成第一个问题
        String firstQuestion = generateFirstQuestion();
        session.setLastQuestion(firstQuestion);
        
        sessionRepository.save(session);
        
        log.info("Created user conversation session: {}", session.getSessionId());
        
        StartUserCollectionResponse response = new StartUserCollectionResponse();
        response.setSessionId(session.getSessionId());
        response.setFirstQuestion(firstQuestion);
        response.setCurrentDimension(session.getCurrentDimension());
        response.setStartedAt(session.getCreatedAt());
        response.setMessage("开始收集你的个人信息");
        
        return response;
    }
    
    @Override
    @Transactional
    public UserMessageResponse processMessage(String sessionId, Long userId, String message) {
        log.info("Processing user message for session: {}, userId: {}, message: {}", 
            sessionId, userId, message);
        
        // 验证会话
        UserConversationSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("会话不存在"));
        
        if (!session.getUserId().equals(userId)) {
            throw new BadRequestException("无权访问此会话");
        }
        
        // 根据会话状态处理
        SessionStatus status = session.getStatus();
        
        if (status == SessionStatus.CONFIRMING_END) {
            return handleConfirmingEndState(session, message);
        } else if (status == SessionStatus.REQUESTING_MINIMUM) {
            return handleRequestingMinimumState(session, message);
        } else if (status == SessionStatus.ACTIVE) {
            return handleActiveState(session, message);
        } else {
            throw new BadRequestException("会话状态不正确: " + status);
        }
    }
    
    /**
     * 处理ACTIVE状态
     */
    private UserMessageResponse handleActiveState(UserConversationSession session, String userMessage) {
        Map<String, Object> collectedData = fromJson(session.getCollectedData());
        List<String> completedDimensions = fromJsonList(session.getCompletedDimensions());
        
        // 检查是否是结束意图
        boolean localWantsToEnd = isEndIntent(userMessage);
        
        // 提取信息
        ExtractionResult extraction = extractInformationWithIntent(
            userMessage,
            session.getCurrentDimension(),
            collectedData,
            session.getLastQuestion()
        );
        
        log.info("Extracted info - intent: {}, updates: {}, wantsToEnd: {}", 
            extraction.getIntent(), extraction.getUpdates(), extraction.isWantsToEnd());
        
        // 更新collected data
        if (extraction.getUpdates() != null && !extraction.getUpdates().isEmpty()) {
            collectedData.putAll(extraction.getUpdates());
            session.setCollectedData(toJson(collectedData));
        }
        
        // 更新对话历史
        addToConversationHistory(session, userMessage, extraction.getIntent().toString());
        
        // 判断是否要结束
        boolean deepseekWantsToEnd = extraction != null && extraction.isWantsToEnd();
        boolean shouldEnd = localWantsToEnd || deepseekWantsToEnd;
        
        if (shouldEnd) {
            return handleEndIntent(session, collectedData, localWantsToEnd);
        }
        
        // 继续下一个问题
        return continueCollection(session, collectedData, completedDimensions);
    }
    
    /**
     * 处理CONFIRMING_END状态
     */
    private UserMessageResponse handleConfirmingEndState(UserConversationSession session, String message) {
        if (isConfirmation(message)) {
            // 用户确认结束，完成并更新User.description
            Map<String, Object> collectedData = fromJson(session.getCollectedData());
            return completeAndUpdateUser(session, collectedData);
        } else if (isContinue(message)) {
            // 用户想继续
            session.setStatus(SessionStatus.ACTIVE);
            sessionRepository.save(session);
            
            Map<String, Object> collectedData = fromJson(session.getCollectedData());
            List<String> completedDimensions = fromJsonList(session.getCompletedDimensions());
            
            return continueCollection(session, collectedData, completedDimensions);
        } else {
            // 不确定，再次确认
            UserMessageResponse response = new UserMessageResponse();
            response.setNextQuestion("请明确回复'是'来完成信息收集，或'继续'来补充更多信息");
            response.setIsCompleted(false);
            response.setIsConfirmingEnd(true);
            response.setProgress(calculateProgress(session));
            return response;
        }
    }
    
    /**
     * 处理REQUESTING_MINIMUM状态
     */
    private UserMessageResponse handleRequestingMinimumState(UserConversationSession session, String message) {
        Map<String, Object> collectedData = fromJson(session.getCollectedData());
        
        // 再次检查是否是结束意图
        if (isEndIntent(message)) {
            // 强制进入确认
            return confirmEnd(session, collectedData);
        }
        
        // 提取信息
        ExtractionResult extraction = extractInformationWithIntent(
            message,
            session.getCurrentDimension(),
            collectedData,
            session.getLastQuestion()
        );
        
        // 更新collected data
        if (extraction.getUpdates() != null && !extraction.getUpdates().isEmpty()) {
            collectedData.putAll(extraction.getUpdates());
            session.setCollectedData(toJson(collectedData));
        }
        
        // 更新对话历史
        addToConversationHistory(session, message, extraction.getIntent().toString());
        
        // 检查最低信息
        if (checkMinimumInfo(collectedData)) {
            // 最低信息满足，进入确认
            return confirmEnd(session, collectedData);
        } else {
            // 继续请求最低信息
            return askForMinimumInfo(session, collectedData);
        }
    }
    
    /**
     * 处理结束意图
     */
    private UserMessageResponse handleEndIntent(
        UserConversationSession session,
        Map<String, Object> collectedData,
        boolean localWantsToEnd
    ) {
        log.info("▲ Handling end intent - localWantsToEnd: {}", localWantsToEnd);
        
        // 检查最低信息
        if (!checkMinimumInfo(collectedData)) {
            log.info("▲ Minimum info not met, requesting minimum info");
            return forceAskMinimumInfo(session, collectedData);
        }
        
        // 最低信息满足，确认结束
        log.info("▲ Minimum info met, confirming end");
        return confirmEnd(session, collectedData);
    }
    
    /**
     * 继续收集
     */
    private UserMessageResponse continueCollection(
        UserConversationSession session,
        Map<String, Object> collectedData,
        List<String> completedDimensions
    ) {
        // 判断当前维度是否完成
        String currentDimension = session.getCurrentDimension();
        String nextDimension = getNextDimension(currentDimension, collectedData, completedDimensions);
        
        if (nextDimension != null && !nextDimension.equals(currentDimension)) {
            // 标记当前维度完成
            markDimensionCompleted(session, currentDimension, completedDimensions);
            session.setCurrentDimension(nextDimension);
        }
        
        // 生成下一个问题
        String nextQuestion = generateNextQuestion(
            session.getCurrentDimension(),
            fromJsonList(session.getCompletedDimensions()),
            collectedData,
            session.getLastQuestion()
        );
        
        session.setLastQuestion(nextQuestion);
        sessionRepository.save(session);
        
        UserMessageResponse response = new UserMessageResponse();
        response.setNextQuestion(nextQuestion);
        response.setIsCompleted(false);
        response.setProgress(calculateProgress(session));
        response.setCurrentDimension(session.getCurrentDimension());
        return response;
    }
    
    /**
     * 请求最低必要信息
     */
    private UserMessageResponse askForMinimumInfo(
        UserConversationSession session,
        Map<String, Object> collectedData
    ) {
        String question = generateMinimumInfoQuestion(collectedData);
        
        session.setStatus(SessionStatus.REQUESTING_MINIMUM);
        session.setLastQuestion(question);
        sessionRepository.save(session);
        
        UserMessageResponse response = new UserMessageResponse();
        response.setNextQuestion(question);
        response.setIsCompleted(false);
        response.setNeedsMinimumInfo(true);
        response.setMinimumInfoHint("为了完善你的个人信息，还需要一些基本信息");
        response.setProgress(calculateProgress(session));
        response.setCurrentDimension(session.getCurrentDimension());
        return response;
    }
    
    /**
     * 强制请求最低信息
     */
    private UserMessageResponse forceAskMinimumInfo(
        UserConversationSession session,
        Map<String, Object> collectedData
    ) {
        String question = generateMinimumInfoQuestion(collectedData);
        
        session.setStatus(SessionStatus.REQUESTING_MINIMUM);
        session.setLastQuestion(question);
        sessionRepository.save(session);
        
        UserMessageResponse response = new UserMessageResponse();
        response.setNextQuestion(question);
        response.setIsCompleted(false);
        response.setNeedsMinimumInfo(true);
        response.setProgress(calculateProgress(session));
        return response;
    }
    
    /**
     * 确认是否结束
     */
    private UserMessageResponse confirmEnd(
        UserConversationSession session,
        Map<String, Object> collectedData
    ) {
        session.setStatus(SessionStatus.CONFIRMING_END);
        sessionRepository.save(session);
        
        UserMessageResponse response = new UserMessageResponse();
        response.setNextQuestion("好的，了解了。那我们就根据这些信息完善你的个人信息吧？（回复'是'继续，或'再想想'继续补充）");
        response.setIsCompleted(false);
        response.setIsConfirmingEnd(true);
        response.setCollectedSummary(generateBriefSummary(collectedData));
        response.setProgress(calculateProgress(session));
        response.setCurrentDimension(session.getCurrentDimension());
        return response;
    }
    
    /**
     * 完成并更新用户描述
     */
    @Transactional
    private UserMessageResponse completeAndUpdateUser(
        UserConversationSession session,
        Map<String, Object> collectedData
    ) {
        try {
            log.info("Completing user self-collection for session {}, collected data: {}", 
                session.getSessionId(), collectedData);
            
            // 生成自我描述
            String description = generateSelfDescription(collectedData);
            
            log.info("Generated self description: {}", description);
            
            // 更新用户描述
            userService.updateUserDescription(session.getUserId(), description);
            
            log.info("Successfully updated user description for session {}", session.getSessionId());
            
            // 🔥 异步计算并更新 selfValue（不阻塞返回）
            selfValueCalculationService.calculateAndUpdateUserAsync(
                session.getUserId(), 
                collectedData
            );
            
            // 更新会话状态
            session.setStatus(SessionStatus.COMPLETED);
            session.setCompletedAt(LocalDateTime.now());
            session.setFinalDescription(description);
            session.setLastQuestion(null);  // 清空lastQuestion，避免干扰QA阶段
            sessionRepository.save(session);
            
            log.info("Session {} marked as COMPLETED", session.getSessionId());
            
            // 返回完成响应
            UserMessageResponse response = new UserMessageResponse();
            response.setIsCompleted(true);
            response.setUpdatedDescription(description);
            response.setSessionId(session.getSessionId());
            response.setNextMode("qa");
            response.setCompletionMessage(PromptTemplates.buildUserCompletionMessage());
            response.setSuggestedActions(Arrays.asList(
                "我的优势是什么？",
                "分析我的性格特点",
                "给我一些个人发展建议"
            ));
            return response;
                
        } catch (Exception e) {
            log.error("Failed to update user description for session {}: {}", 
                session.getSessionId(), e.getMessage(), e);
            
            String errorMessage = "更新个人信息失败: " + e.getMessage();
            throw new BadRequestException(errorMessage);
        }
    }
    
    /**
     * 检查最低信息
     */
    private boolean checkMinimumInfo(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            log.info("No collected data available");
            return false;
        }
        
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
    private String generateFirstQuestion() {
        String prompt = PromptTemplates.buildFirstQuestionForUser();
        return callDeepseek(prompt);
    }
    
    /**
     * 生成下一个问题
     */
    private String generateNextQuestion(
        String currentDimension,
        List<String> completedDimensions,
        Map<String, Object> collectedData,
        String lastQuestion
    ) {
        String prompt = PromptTemplates.buildNextQuestionForUser(
            currentDimension,
            completedDimensions,
            collectedData,
            lastQuestion
        );
        return callDeepseek(prompt);
    }
    
    /**
     * 生成最低信息问题
     */
    private String generateMinimumInfoQuestion(Map<String, Object> data) {
        List<String> missingInfo = analyzeMissingInfo(data);
        String prompt = PromptTemplates.buildMinimumInfoQuestionForUser(data, missingInfo);
        return callDeepseek(prompt);
    }
    
    /**
     * 分析缺失信息
     */
    private List<String> analyzeMissingInfo(Map<String, Object> data) {
        List<String> missing = new ArrayList<>();
        if (!hasValidValue(data.get("age"))) { missing.add("age"); }
        if (!hasValidValue(data.get("occupation"))) { missing.add("occupation"); }
        if (!hasValidValue(data.get("education"))) { missing.add("education"); }
        if (!hasValidValue(data.get("personality_characteristics"))) { missing.add("personality_characteristics"); }
        log.info("Analyzed missing info: {}", missing);
        return missing;
    }
    
    /**
     * 生成自我描述
     */
    private String generateSelfDescription(Map<String, Object> collectedData) {
        String prompt = PromptTemplates.buildSelfDescriptionPrompt(collectedData);
        return callDeepseek(prompt);
    }
    
    /**
     * 提取信息
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
            ExtractionResult result = objectMapper.readValue(jsonResponse, ExtractionResult.class);
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
            throw new BadRequestException("AI响应格式错误，请重试。错误详情: " + e.getMessage());
        }
    }
    
    /**
     * 获取下一个维度
     */
    private String getNextDimension(
        String currentDimension,
        Map<String, Object> collectedData,
        List<String> completedDimensions
    ) {
        int currentIndex = COLLECTION_DIMENSIONS.indexOf(currentDimension);
        if (currentIndex == -1) {
            return COLLECTION_DIMENSIONS.get(0);
        }
        
        // 检查当前维度是否有足够信息
        List<String> currentFields = DIMENSION_FIELDS.get(currentDimension);
        if (currentFields != null) {
            int collectedCount = 0;
            for (String field : currentFields) {
                if (collectedData.containsKey(field) && hasValidValue(collectedData.get(field))) {
                    collectedCount++;
                }
            }
            
            // 至少收集到1个字段才考虑进入下一个维度
            if (collectedCount < 1) {
                return currentDimension;
            }
        }
        
        // 进入下一个维度
        if (currentIndex < COLLECTION_DIMENSIONS.size() - 1) {
            return COLLECTION_DIMENSIONS.get(currentIndex + 1);
        }
        
        return currentDimension;
    }
    
    /**
     * 标记维度完成
     */
    private void markDimensionCompleted(
        UserConversationSession session,
        String dimension,
        List<String> completedDimensions
    ) {
        if (!completedDimensions.contains(dimension)) {
            completedDimensions.add(dimension);
            session.setCompletedDimensions(toJson(completedDimensions));
            log.info("Marked dimension as completed: {}", dimension);
        }
    }
    
    /**
     * 计算进度
     */
    private int calculateProgress(UserConversationSession session) {
        List<String> completedDimensions = fromJsonList(session.getCompletedDimensions());
        Map<String, Object> collectedData = fromJson(session.getCollectedData());
        
        // 60%: 维度完成度
        int dimensionProgress = (completedDimensions.size() * 100) / COLLECTION_DIMENSIONS.size();
        int dimensionScore = (int) (dimensionProgress * 0.6);
        
        // 40%: 信息质量
        int qualityScore = calculateQualityProgress(collectedData);
        
        int totalProgress = Math.min(100, dimensionScore + qualityScore);
        log.debug("Progress calculation - dimensions: {}/{} ({}%), quality: {}%, total: {}%",
            completedDimensions.size(), COLLECTION_DIMENSIONS.size(), dimensionProgress, qualityScore, totalProgress);
        
        return totalProgress;
    }
    
    /**
     * 计算信息质量进度
     */
    private int calculateQualityProgress(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        
        int validCount = 0;
        int totalCount = data.size();
        
        for (Object value : data.values()) {
            if (hasValidValue(value)) {
                validCount++;
            }
        }
        
        if (totalCount == 0) {
            return 0;
        }
        
        // 最多40分
        return (int) ((validCount * 40.0) / Math.max(totalCount, 10));
    }
    
    /**
     * 检查是否有效值
     */
    private boolean hasValidValue(Object value) {
        if (value == null) {
            return false;
        }
        
        String strValue = value.toString().trim().toLowerCase();
        
        // 检查是否为否定答案
        if (isNegativeAnswer(strValue)) {
            return false;
        }
        
        // 检查是否为空或无意义
        return !strValue.isEmpty() 
            && !strValue.equals("null") 
            && !strValue.equals("undefined")
            && strValue.length() >= 2;
    }
    
    /**
     * 检查是否为否定答案
     */
    private boolean isNegativeAnswer(String value) {
        List<String> negativePatterns = Arrays.asList(
            "不知道", "不清楚", "不确定", "没有", "不了解", 
            "不太清楚", "不记得", "忘了", "说不上来"
        );
        
        for (String pattern : negativePatterns) {
            if (value.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 判断是否是结束意图
     */
    private boolean isEndIntent(String message) {
        String lowerMsg = message.toLowerCase().trim();
        return lowerMsg.contains("结束") 
            || lowerMsg.contains("完成") 
            || lowerMsg.contains("够了")
            || lowerMsg.contains("就这样")
            || lowerMsg.equals("结束问卷")
            || lowerMsg.equals("问卷结束");
    }
    
    /**
     * 判断是否是确认
     */
    private boolean isConfirmation(String message) {
        String lowerMsg = message.toLowerCase().trim();
        return lowerMsg.equals("是") 
            || lowerMsg.equals("确认") 
            || lowerMsg.equals("好的")
            || lowerMsg.equals("好")
            || lowerMsg.equals("可以")
            || lowerMsg.equals("对");
    }
    
    /**
     * 判断是否是继续
     */
    private boolean isContinue(String message) {
        String lowerMsg = message.toLowerCase().trim();
        return lowerMsg.contains("继续") 
            || lowerMsg.contains("再想想")
            || lowerMsg.contains("补充")
            || lowerMsg.equals("不");
    }
    
    /**
     * 生成简要总结
     */
    private String generateBriefSummary(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return "暂无收集信息";
        }
        
        StringBuilder summary = new StringBuilder();
        int count = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (hasValidValue(entry.getValue()) && count < 5) {
                summary.append(entry.getKey()).append(": ").append(entry.getValue()).append("; ");
                count++;
            }
        }
        
        return summary.toString();
    }
    
    /**
     * 添加到对话历史
     */
    private void addToConversationHistory(UserConversationSession session, String userMessage, String intent) {
        try {
            List<Map<String, Object>> history = objectMapper.readValue(
                session.getConversationHistory(),
                new TypeReference<List<Map<String, Object>>>() {}
            );
            
            Map<String, Object> entry = new HashMap<>();
            entry.put("question", session.getLastQuestion());
            entry.put("user", userMessage);
            entry.put("intent", intent);
            entry.put("timestamp", LocalDateTime.now().toString());
            
            history.add(entry);
            session.setConversationHistory(objectMapper.writeValueAsString(history));
        } catch (JsonProcessingException e) {
            log.error("Failed to update conversation history", e);
        }
    }
    
    /**
     * 调用Deepseek
     */
    private String callDeepseek(String prompt) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(deepseekProperties.getModel());
        request.setMessages(List.of(
            new ChatMessage("system", "你是一个专业的信息收集助手，帮助用户了解自己。"),
            new ChatMessage("user", prompt)
        ));
        request.setTemperature(0.7);
        
        ChatCompletionResponse response = deepseekClient.chat(request).block();
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new BadRequestException("AI服务暂时不可用");
        }
        
        return response.getChoices().get(0).getMessage().getContent().trim();
    }
    
    /**
     * JSON序列化
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize to JSON", e);
            throw new BadRequestException("数据序列化失败");
        }
    }
    
    /**
     * JSON反序列化为Map
     */
    private Map<String, Object> fromJson(String json) {
        try {
            if (json == null || json.trim().isEmpty() || json.equals("{}")) {
                return new HashMap<>();
            }
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON: {}", json, e);
            return new HashMap<>();
        }
    }
    
    /**
     * JSON反序列化为List
     */
    private List<String> fromJsonList(String json) {
        try {
            if (json == null || json.trim().isEmpty() || json.equals("[]")) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON list: {}", json, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Integer getProgress(String sessionId, Long userId) {
        UserConversationSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("会话不存在"));
        
        if (!session.getUserId().equals(userId)) {
            throw new BadRequestException("无权访问此会话");
        }
        
        return calculateProgress(session);
    }
    
    @Override
    @Transactional
    public void abandonSession(String sessionId, Long userId) {
        UserConversationSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("会话不存在"));
        
        if (!session.getUserId().equals(userId)) {
            throw new BadRequestException("无权访问此会话");
        }
        
        session.setStatus(SessionStatus.ABANDONED);
        sessionRepository.save(session);
        
        log.info("Session {} abandoned by user {}", sessionId, userId);
    }
}

