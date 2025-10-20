package com.ulog.backend.ai;

import com.ulog.backend.ai.dto.ChatCompletionRequest;
import com.ulog.backend.ai.dto.ChatCompletionResponse;
import com.ulog.backend.ai.dto.ChatMessage;
import com.ulog.backend.compliance.dto.ModerationResult;
import com.ulog.backend.compliance.service.ContentModerationService;
import com.ulog.backend.config.DeepseekProperties;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DeepseekService {

    private static final Logger log = LoggerFactory.getLogger(DeepseekService.class);

    private final DeepseekClient client;
    private final DeepseekProperties properties;
    private final ContentModerationService contentModerationService;

    public DeepseekService(DeepseekClient client, DeepseekProperties properties,
                          ContentModerationService contentModerationService) {
        this.client = client;
        this.properties = properties;
        this.contentModerationService = contentModerationService;
    }

    public Mono<String> ask(String systemPrompt, String userPrompt) {
        return ask(systemPrompt, userPrompt, null);
    }

    public Mono<String> ask(String systemPrompt, String userPrompt, Long userId) {
        // 审核用户输入内容
        ModerationResult inputModeration = contentModerationService.moderateContent(
            userId, "ai_input", userPrompt);
        
        if (!inputModeration.isPassed()) {
            log.warn("User input rejected by content moderation: {}", inputModeration.getRiskDetails());
            return Mono.just("抱歉，您的输入包含不当内容，请修改后重试。");
        }

        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setMessages(List.of(
            new ChatMessage("system", systemPrompt),
            new ChatMessage("user", userPrompt)
        ));
        request.setTemperature(0.7);
        
        return client.chat(request)
            .map(response -> {
                if (response.getChoices() == null || response.getChoices().isEmpty()) {
                    return "";
                }
                ChatCompletionResponse.Choice choice = response.getChoices().get(0);
                String content = "";
                if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                    content = choice.getMessage().getContent();
                } else if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                    content = choice.getDelta().getContent();
                }
                
                // 审核AI输出内容
                if (!content.isEmpty()) {
                    ModerationResult outputModeration = contentModerationService.moderateContent(
                        userId, "ai_output", content);
                    
                    if (!outputModeration.isPassed()) {
                        log.warn("AI output rejected by content moderation: {}", outputModeration.getRiskDetails());
                        return "抱歉，AI生成的内容未通过审核，请重新提问。";
                    }
                }
                
                return content;
            });
    }

    public Mono<String> askReasoner(String systemPrompt, String userPrompt) {
        return askReasoner(systemPrompt, userPrompt, null);
    }

    public Mono<String> askReasoner(String systemPrompt, String userPrompt, Long userId) {
        // 审核用户输入内容
        ModerationResult inputModeration = contentModerationService.moderateContent(
            userId, "ai_input", userPrompt);
        
        if (!inputModeration.isPassed()) {
            log.warn("User input rejected by content moderation: {}", inputModeration.getRiskDetails());
            return Mono.just("抱歉，您的输入包含不当内容，请修改后重试。");
        }

        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(properties.getReasonerModel()); // 使用 reasoner 模型
        request.setMessages(List.of(
            new ChatMessage("system", systemPrompt),
            new ChatMessage("user", userPrompt)
        ));
        request.setTemperature(0.7);
        
        return client.chat(request)
            .map(response -> {
                if (response.getChoices() == null || response.getChoices().isEmpty()) {
                    return "";
                }
                ChatCompletionResponse.Choice choice = response.getChoices().get(0);
                String content = "";
                if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                    content = choice.getMessage().getContent();
                } else if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                    content = choice.getDelta().getContent();
                }
                
                // 审核AI输出内容
                if (!content.isEmpty()) {
                    ModerationResult outputModeration = contentModerationService.moderateContent(
                        userId, "ai_output", content);
                    
                    if (!outputModeration.isPassed()) {
                        log.warn("AI output rejected by content moderation: {}", outputModeration.getRiskDetails());
                        return "抱歉，AI生成的内容未通过审核，请重新提问。";
                    }
                }
                
                return content;
            });
    }
}
