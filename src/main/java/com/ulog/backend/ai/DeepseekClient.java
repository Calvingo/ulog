package com.ulog.backend.ai;

import com.ulog.backend.ai.dto.ChatCompletionRequest;
import com.ulog.backend.ai.dto.ChatCompletionResponse;
import com.ulog.backend.config.DeepseekProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DeepseekClient {

    private final WebClient webClient;
    private final DeepseekProperties properties;

    public DeepseekClient(WebClient deepseekWebClient, DeepseekProperties properties) {
        this.webClient = deepseekWebClient;
        this.properties = properties;
    }

    public Mono<ChatCompletionResponse> chat(ChatCompletionRequest request) {
        if (request.getModel() == null) {
            request.setModel(properties.getModel());
        }
        request.setStream(false);
        return webClient.post()
            .uri("/v1/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .bodyToMono(ChatCompletionResponse.class);
    }

    public Flux<String> chatStream(ChatCompletionRequest request) {
        if (request.getModel() == null) {
            request.setModel(properties.getModel());
        }
        request.setStream(true);
        return webClient.post()
            .uri("/v1/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .bodyValue(request)
            .retrieve()
            .bodyToFlux(String.class);
    }
}
