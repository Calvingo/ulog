package com.ulog.backend.contact.service;

import com.ulog.backend.ai.DeepseekService;
import com.ulog.backend.contact.dto.AibookDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AibookServiceImpl implements AibookService {
    
    private static final Logger log = LoggerFactory.getLogger(AibookServiceImpl.class);
    
    private final DeepseekService deepseekService;
    private final ObjectMapper objectMapper;

    public AibookServiceImpl(DeepseekService deepseekService, ObjectMapper objectMapper) {
        this.deepseekService = deepseekService;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT_ZH = """
你是"人际关系洞察助理"。我会给你两段文本：
A) 联系人自由描述（关于对方）
B) 当前用户的自我描述（关于我）
请仅根据文本中的明确信息与合理推断，产出一个 JSON 对象 aibook，包含 5 个维度：
1) behaviorTendencies（行为倾向推断）
2) valuesAndEmotions（情绪与价值观暗示）
3) latentNeeds（潜在需求推理）
4) taboos（可能的禁忌雷区）
5) relationshipOpportunities（人际关系机会点）
每个维度返回 2-5 条，每条包含：point（结论，≤40字），reason（依据简述），source（contact|user|both）。严禁编造，信息不足可为空数组。输出仅限 JSON，无额外文字。
""";

    private static final String SYSTEM_PROMPT_EN = """
You are a relationship-insight assistant. You'll receive two texts:
A) Contact's free-form description (about them)
B) Current user's self-description (about me)
Return a JSON object `aibook` with 5 dimensions: behaviorTendencies, valuesAndEmotions, latentNeeds, taboos, relationshipOpportunities. Each dimension has 2-5 items with: point, reason, source (contact|user|both). Be concise, avoid fabrication; empty arrays are allowed. Output JSON only.
""";

    @Override
    public AibookDto generate(String contactDesc, String userDesc, String language) {
        if ((contactDesc == null || contactDesc.isBlank()) && (userDesc == null || userDesc.isBlank())) {
            throw new IllegalArgumentException("Both descriptions are empty");
        }
        
        String systemPrompt = "zh".equalsIgnoreCase(language) ? SYSTEM_PROMPT_ZH : SYSTEM_PROMPT_EN;
        String userPrompt = String.format("""
【联系人描述 A】
%s

【用户自述 B】
%s

请按 system 指令返回 JSON。
""", nullToEmpty(contactDesc), nullToEmpty(userDesc));

        try {
            String rawResponse = deepseekService.ask(systemPrompt, userPrompt)
                .timeout(Duration.ofSeconds(120))
                .block();
            
            if (rawResponse == null || rawResponse.trim().isEmpty()) {
                throw new RuntimeException("Empty AI response");
            }
            
            return parseAibookResponse(rawResponse);
            
        } catch (Exception e) {
            log.error("Failed to generate aibook: {}", e.getMessage(), e);
            throw new RuntimeException("AI service error: " + e.getMessage(), e);
        }
    }

    private AibookDto parseAibookResponse(String rawResponse) {
        try {
            // 允许 DeepSeek 直接返回 {"aibook":{...}} 或仅返回 {...}
            if (rawResponse.trim().startsWith("{")) {
                var node = objectMapper.readTree(rawResponse);
                if (node.has("aibook")) {
                    return objectMapper.treeToValue(node.get("aibook"), AibookDto.class);
                } else {
                    return objectMapper.treeToValue(node, AibookDto.class);
                }
            }
            throw new RuntimeException("Invalid AI response format: " + rawResponse);
        } catch (Exception e) {
            log.error("Failed to parse aibook response: {}", rawResponse, e);
            throw new RuntimeException("Parse aibook failed: " + e.getMessage(), e);
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
