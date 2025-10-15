package com.ulog.backend.ai;

import com.ulog.backend.config.DeepseekProperties;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiSummaryServiceImpl implements AiSummaryService {

    private static final Logger log = LoggerFactory.getLogger(AiSummaryServiceImpl.class);
    private static final String SYSTEM_PROMPT = "你是联系人信息整理助手。请将自由描述归纳为 3-6 条要点：基本画像、偏好、兴趣、近期计划/合作机会、可能的禁忌。不要编造。";

    private final DeepseekService deepseekService;
    private final DeepseekProperties properties;

    public AiSummaryServiceImpl(DeepseekService deepseekService, DeepseekProperties properties) {
        this.deepseekService = deepseekService;
        this.properties = properties;
    }

    @Override
    public String generateAiSummary(String description) {
        if (!StringUtils.hasText(description)) {
            return "";
        }
        if (!StringUtils.hasText(properties.getApiKey()) || !StringUtils.hasText(properties.getBaseUrl())) {
            return "";
        }
        try {
            Duration timeout = Duration.ofMillis(Math.max(1000, properties.getTimeoutMs()));
            return deepseekService.ask(SYSTEM_PROMPT, "根据以下描述生成要点：\n" + description)
                .timeout(timeout)
                .blockOptional(timeout)
                .orElse("");
        } catch (Exception ex) {
            log.warn("AI summary generation failed: {}", ex.getMessage());
            log.debug("AI summary generation error", ex);
            return "";
        }
    }
}
