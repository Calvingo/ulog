package com.ulog.backend.conversation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulog.backend.ai.DeepseekService;
import com.ulog.backend.conversation.dto.SelfValue;
import com.ulog.backend.conversation.util.PromptTemplates;
import com.ulog.backend.contact.service.ContactService;
import com.ulog.backend.user.service.UserService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SelfValueCalculationServiceImpl implements SelfValueCalculationService {

    private static final Logger log = LoggerFactory.getLogger(SelfValueCalculationServiceImpl.class);

    private final DeepseekService deepseekService;
    private final ContactService contactService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public SelfValueCalculationServiceImpl(
            DeepseekService deepseekService,
            ContactService contactService,
            UserService userService,
            ObjectMapper objectMapper) {
        this.deepseekService = deepseekService;
        this.contactService = contactService;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    public SelfValue calculateSelfValue(Map<String, Object> collectedData) {
        try {
            log.info("Starting synchronous self value calculation for data: {}", collectedData);

            // 构建 AI 评估 prompt
            String prompt = PromptTemplates.buildSelfValueEvaluationPrompt(collectedData);
            
            // 调用 Deepseek AI
            String aiResponse = deepseekService.askReasoner("", prompt)
                .timeout(java.time.Duration.ofSeconds(10))
                .block();

            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                log.warn("AI returned empty response for self value calculation");
                return SelfValue.getDefaultSelfValue();
            }

            // 解析 AI 响应
            SelfValue selfValue = parseAiResponse(aiResponse);
            log.info("Successfully calculated self value: {}", selfValue);
            
            return selfValue;

        } catch (Exception e) {
            log.error("Failed to calculate self value: {}", e.getMessage(), e);
            return SelfValue.getDefaultSelfValue();
        }
    }

    @Override
    @Async("selfValueTaskExecutor")
    public void calculateAndUpdateContactAsync(Long contactId, Map<String, Object> collectedData) {
        try {
            log.info("Starting async self value calculation for contact {} with data: {}", 
                contactId, collectedData);

            // 计算 self value
            SelfValue selfValue = calculateSelfValue(collectedData);
            String selfValueStr = SelfValue.format(selfValue);

            // 更新联系人
            contactService.updateSelfValue(contactId, selfValueStr);
            
            log.info("Successfully updated self value for contact {}: {}", contactId, selfValueStr);

        } catch (Exception e) {
            log.error("Failed to calculate and update self value for contact {}: {}", 
                contactId, e.getMessage(), e);
            
            // 失败时使用默认值
            try {
                String defaultSelfValue = SelfValue.format(SelfValue.getDefaultSelfValue());
                contactService.updateSelfValue(contactId, defaultSelfValue);
                log.info("Applied default self value for contact {}: {}", contactId, defaultSelfValue);
            } catch (Exception updateEx) {
                log.error("Failed to apply default self value for contact {}: {}", 
                    contactId, updateEx.getMessage(), updateEx);
            }
        }
    }

    @Override
    @Async("selfValueTaskExecutor")
    public void calculateAndUpdateUserAsync(Long userId, Map<String, Object> collectedData) {
        try {
            log.info("Starting async self value calculation for user {} with data: {}", 
                userId, collectedData);

            // 计算 self value
            SelfValue selfValue = calculateSelfValue(collectedData);
            String selfValueStr = SelfValue.format(selfValue);

            // 更新用户
            userService.updateUserSelfValue(userId, selfValueStr);
            
            log.info("Successfully updated self value for user {}: {}", userId, selfValueStr);

        } catch (Exception e) {
            log.error("Failed to calculate and update self value for user {}: {}", 
                userId, e.getMessage(), e);
            
            // 失败时使用默认值
            try {
                String defaultSelfValue = SelfValue.format(SelfValue.getDefaultSelfValue());
                userService.updateUserSelfValue(userId, defaultSelfValue);
                log.info("Applied default self value for user {}: {}", userId, defaultSelfValue);
            } catch (Exception updateEx) {
                log.error("Failed to apply default self value for user {}: {}", 
                    userId, updateEx.getMessage(), updateEx);
            }
        }
    }

    /**
     * 解析 AI 响应为 SelfValue 对象
     */
    private SelfValue parseAiResponse(String aiResponse) {
        try {
            // 尝试解析 JSON 格式的响应
            String cleanResponse = aiResponse.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();

            // 解析为 JSON
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(cleanResponse, Map.class);
            
            Double selfEsteem = parseDoubleValue(responseMap.get("selfEsteem"));
            Double selfAcceptance = parseDoubleValue(responseMap.get("selfAcceptance"));
            Double selfEfficacy = parseDoubleValue(responseMap.get("selfEfficacy"));
            Double existentialValue = parseDoubleValue(responseMap.get("existentialValue"));
            Double selfConsistency = parseDoubleValue(responseMap.get("selfConsistency"));

            SelfValue selfValue = new SelfValue(selfEsteem, selfAcceptance, selfEfficacy, 
                existentialValue, selfConsistency);

            if (selfValue.isValid()) {
                return selfValue;
            } else {
                log.warn("Invalid self value calculated: {}", selfValue);
                return SelfValue.getDefaultSelfValue();
            }

        } catch (JsonProcessingException e) {
            log.warn("Failed to parse AI response as JSON: {}", aiResponse);
            
            // 尝试从文本中提取数字
            return extractSelfValueFromText(aiResponse);
        } catch (Exception e) {
            log.error("Unexpected error parsing AI response: {}", e.getMessage(), e);
            return SelfValue.getDefaultSelfValue();
        }
    }

    /**
     * 从文本中提取 self value（备用方案）
     */
    private SelfValue extractSelfValueFromText(String text) {
        try {
            // 简单的文本解析逻辑
            String[] lines = text.split("\n");
            Double[] values = new Double[5];
            
            for (String line : lines) {
                if (line.contains("自尊") || line.contains("selfEsteem")) {
                    values[0] = extractNumber(line);
                } else if (line.contains("自我接纳") || line.contains("selfAcceptance")) {
                    values[1] = extractNumber(line);
                } else if (line.contains("自我效能") || line.contains("selfEfficacy")) {
                    values[2] = extractNumber(line);
                } else if (line.contains("存在价值") || line.contains("existentialValue")) {
                    values[3] = extractNumber(line);
                } else if (line.contains("自我一致") || line.contains("selfConsistency")) {
                    values[4] = extractNumber(line);
                }
            }

            // 检查是否有足够的有效值
            int validCount = 0;
            for (Double value : values) {
                if (value != null && value >= 1.0 && value <= 5.0) {
                    validCount++;
                }
            }

            if (validCount >= 3) {
                return new SelfValue(
                    values[0] != null ? values[0] : 3.0,
                    values[1] != null ? values[1] : 3.0,
                    values[2] != null ? values[2] : 3.0,
                    values[3] != null ? values[3] : 3.0,
                    values[4] != null ? values[4] : 3.0
                );
            } else {
                log.warn("Insufficient valid values extracted from text: {}", text);
                return SelfValue.getDefaultSelfValue();
            }

        } catch (Exception e) {
            log.error("Failed to extract self value from text: {}", e.getMessage(), e);
            return SelfValue.getDefaultSelfValue();
        }
    }

    private Double parseDoubleValue(Object value) {
        if (value == null) {
            return 3.0;
        }
        if (value instanceof Number) {
            double num = ((Number) value).doubleValue();
            return (num >= 1.0 && num <= 5.0) ? num : 3.0;
        }
        try {
            double num = Double.parseDouble(value.toString());
            return (num >= 1.0 && num <= 5.0) ? num : 3.0;
        } catch (NumberFormatException e) {
            return 3.0;
        }
    }

    private Double extractNumber(String text) {
        try {
            // 简单的数字提取
            String[] parts = text.split("[^0-9.]");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    double num = Double.parseDouble(part);
                    if (num >= 1.0 && num <= 5.0) {
                        return num;
                    }
                }
            }
        } catch (NumberFormatException e) {
            // 忽略解析错误
        }
        return 3.0;
    }
}
