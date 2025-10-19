package com.ulog.backend.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulog.backend.ai.dto.AiGoalStrategyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GoalAiService {

    private static final Logger log = LoggerFactory.getLogger(GoalAiService.class);

    private final DeepseekService deepseekService;
    private final ObjectMapper objectMapper;

    public GoalAiService(DeepseekService deepseekService, ObjectMapper objectMapper) {
        this.deepseekService = deepseekService;
        this.objectMapper = objectMapper;
    }

    public Mono<AiGoalStrategyResponse> generateGoalStrategy(String contactInfo, String userInfo, String goalDescription) {
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(contactInfo, userInfo, goalDescription);

        return deepseekService.askReasoner(systemPrompt, userPrompt)
            .map(this::parseAiResponse)
            .doOnError(error -> log.error("Failed to generate goal strategy: {}", error.getMessage()));
    }

    private String buildSystemPrompt() {
        return "你是一个人际关系管理专家，专门帮助用户改善和维护人际关系。" +
               "请根据用户提供的关系目标，生成一个详细的策略和具体的行动计划。\n\n" +
               "你的回复必须是一个JSON格式，包含以下字段：\n" +
               "1. strategy: 一段简洁的策略说明（200-300字）\n" +
               "2. actionPlans: 一个数组，包含3-5个具体的行动计划，每个计划包含：\n" +
               "   - title: 行动标题（简短明确）\n" +
               "   - description: 行动描述（详细具体）\n" +
               "   - scheduledDays: 距今多少天后执行（整数，0表示立即，7表示一周后等）\n\n" +
               "JSON格式示例：\n" +
               "{\n" +
               "  \"strategy\": \"为了改善与该联系人的关系，建议采取循序渐进的方式...\",\n" +
               "  \"actionPlans\": [\n" +
               "    {\n" +
               "      \"title\": \"发送问候消息\",\n" +
               "      \"description\": \"主动发送一条关心的消息，询问对方近况\",\n" +
               "      \"scheduledDays\": 0\n" +
               "    },\n" +
               "    {\n" +
               "      \"title\": \"约饭聊天\",\n" +
               "      \"description\": \"邀请对方共进午餐或晚餐，面对面交流\",\n" +
               "      \"scheduledDays\": 7\n" +
               "    }\n" +
               "  ]\n" +
               "}\n\n" +
               "请确保返回的是纯JSON格式，不要包含任何其他文字说明。";
    }

    private String buildUserPrompt(String contactInfo, String userInfo, String goalDescription) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("联系人信息：\n");
        prompt.append(contactInfo != null ? contactInfo : "无");
        prompt.append("\n\n");
        
        prompt.append("我的信息：\n");
        prompt.append(userInfo != null ? userInfo : "无");
        prompt.append("\n\n");
        
        prompt.append("我的关系目标：\n");
        prompt.append(goalDescription);
        prompt.append("\n\n");
        
        prompt.append("请基于以上双方的信息和我的目标，为我生成个性化的策略和行动计划。");
        return prompt.toString();
    }

    private AiGoalStrategyResponse parseAiResponse(String aiResponse) {
        try {
            // 尝试从响应中提取JSON
            String jsonContent = extractJson(aiResponse);
            AiGoalStrategyResponse response = objectMapper.readValue(jsonContent, AiGoalStrategyResponse.class);
            
            // 验证响应
            if (response.getStrategy() == null || response.getStrategy().isBlank()) {
                throw new IllegalArgumentException("AI response missing strategy");
            }
            if (response.getActionPlans() == null || response.getActionPlans().isEmpty()) {
                throw new IllegalArgumentException("AI response missing action plans");
            }
            
            return response;
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
            log.debug("AI response content: {}", aiResponse);
            throw new RuntimeException("Failed to parse AI response: " + e.getMessage(), e);
        }
    }

    private String extractJson(String content) {
        // 查找第一个 { 和最后一个 }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        
        // 如果没找到，返回原始内容
        return content;
    }
}

