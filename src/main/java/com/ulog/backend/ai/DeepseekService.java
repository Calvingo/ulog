package com.ulog.backend.ai;

import com.ulog.backend.ai.dto.ChatCompletionRequest;
import com.ulog.backend.ai.dto.ChatCompletionResponse;
import com.ulog.backend.ai.dto.ChatMessage;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DeepseekService {

    private final DeepseekClient client;

    public DeepseekService(DeepseekClient client) {
        this.client = client;
    }

    public Mono<String> ask(String systemPrompt, String userPrompt) {
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
                if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                    return choice.getMessage().getContent();
                }
                if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                    return choice.getDelta().getContent();
                }
                return "";
            });
    }
}
