package com.ulog.backend.ai;

import com.ulog.backend.ai.dto.ChatCompletionRequest;
import com.ulog.backend.ai.dto.ChatMessage;
import com.ulog.backend.config.DeepseekProperties;
import com.ulog.backend.conversation.util.PromptTemplates;
import com.ulog.backend.domain.contact.Contact;
import com.ulog.backend.domain.user.User;
import com.ulog.backend.repository.ContactRepository;
import com.ulog.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

/**
 * 关系分析服务实现类
 */
@Service
public class RelationshipAnalysisServiceImpl implements RelationshipAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(RelationshipAnalysisServiceImpl.class);

    private final DeepseekClient deepseekClient;
    private final DeepseekProperties deepseekProperties;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public RelationshipAnalysisServiceImpl(
            DeepseekClient deepseekClient,
            DeepseekProperties deepseekProperties,
            ContactRepository contactRepository,
            UserRepository userRepository
    ) {
        this.deepseekClient = deepseekClient;
        this.deepseekProperties = deepseekProperties;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Mono<String> generateRelationshipAnalysis(Long contactId, Long userId) {
        log.info("Starting relationship analysis generation for contact {} and user {}", contactId, userId);

        return Mono.fromCallable(() -> {
            Contact contact = contactRepository.findById(contactId)
                    .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + contactId));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

            String contactDescription = contact.getDescription();
            String contactSelfValue = contact.getSelfValue();
            String userDescription = user.getDescription();
            String userSelfValue = user.getSelfValue();

            if (contactDescription == null || contactDescription.trim().isEmpty()) {
                throw new IllegalStateException("Contact description is empty");
            }
            if (userDescription == null || userDescription.trim().isEmpty()) {
                throw new IllegalStateException("User description is empty");
            }

            return new AnalysisContext(contactDescription, contactSelfValue, userDescription, userSelfValue);
        })
        .flatMap(context -> {
            String prompt = PromptTemplates.buildRelationshipAnalysisPrompt(
                    context.contactDescription,
                    context.contactSelfValue,
                    context.userDescription,
                    context.userSelfValue
            );

            return callDeepseekWithPrompt(prompt, "生成关系分析");
        })
        .timeout(Duration.ofSeconds(180))
        .retryWhen(Retry.backoff(2, Duration.ofSeconds(5))
                .filter(throwable -> !(throwable instanceof IllegalStateException)))
        .doOnSuccess(result -> log.info("Successfully generated relationship analysis for contact {}", contactId))
        .doOnError(error -> log.error("Failed to generate relationship analysis for contact {}: {}", 
                contactId, error.getMessage()));
    }

    @Override
    public Mono<String> generateInteractionSuggestions(Long contactId) {
        log.info("Starting interaction suggestions generation for contact {}", contactId);

        return Mono.fromCallable(() -> {
            Contact contact = contactRepository.findById(contactId)
                    .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + contactId));

            String description = contact.getDescription();
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalStateException("Contact description is empty");
            }

            return description;
        })
        .flatMap(description -> {
            String prompt = PromptTemplates.buildInteractionSuggestionsPrompt(description);
            return callDeepseekWithPrompt(prompt, "生成交往建议");
        })
        .timeout(Duration.ofSeconds(180))
        .retryWhen(Retry.backoff(2, Duration.ofSeconds(5))
                .filter(throwable -> !(throwable instanceof IllegalStateException)))
        .doOnSuccess(result -> log.info("Successfully generated interaction suggestions for contact {}", contactId))
        .doOnError(error -> log.error("Failed to generate interaction suggestions for contact {}: {}", 
                contactId, error.getMessage()));
    }

    private Mono<String> callDeepseekWithPrompt(String prompt, String taskName) {
        log.debug("Calling Deepseek for {}", taskName);

        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(deepseekProperties.getReasonerModel());
        request.setMessages(List.of(
                new ChatMessage("system", "你是一个专业的关系分析助手。"),
                new ChatMessage("user", prompt)
        ));
        request.setTemperature(0.7);

        return deepseekClient.chat(request)
                .map(response -> {
                    if (response.getChoices() == null || response.getChoices().isEmpty()) {
                        throw new RuntimeException("Empty response from Deepseek");
                    }
                    return response.getChoices().get(0).getMessage().getContent().trim();
                });
    }

    private static class AnalysisContext {
        final String contactDescription;
        final String contactSelfValue;
        final String userDescription;
        final String userSelfValue;

        AnalysisContext(String contactDescription, String contactSelfValue, 
                       String userDescription, String userSelfValue) {
            this.contactDescription = contactDescription;
            this.contactSelfValue = contactSelfValue;
            this.userDescription = userDescription;
            this.userSelfValue = userSelfValue;
        }
    }
}
