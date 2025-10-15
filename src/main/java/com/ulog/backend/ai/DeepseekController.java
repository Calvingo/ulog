package com.ulog.backend.ai;

import com.ulog.backend.ai.dto.ChatCompletionRequest;
import com.ulog.backend.ai.dto.ChatMessage;
import com.ulog.backend.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/ai")
public class DeepseekController {

    public record AskRequest(@NotBlank String prompt) {
    }

    private final DeepseekService service;
    private final DeepseekClient client;

    public DeepseekController(DeepseekService service, DeepseekClient client) {
        this.service = service;
        this.client = client;
    }

    @PostMapping("/ask")
    public Mono<ApiResponse<String>> ask(@Valid @RequestBody AskRequest request) {
        return service.ask("You are a helpful assistant.", request.prompt())
            .map(ApiResponse::success);
    }

    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askStream(@Valid @RequestBody AskRequest request) {
        ChatCompletionRequest completionRequest = new ChatCompletionRequest();
        completionRequest.setMessages(List.of(
            new ChatMessage("system", "You are a helpful assistant."),
            new ChatMessage("user", request.prompt())
        ));
        completionRequest.setTemperature(0.7);
        completionRequest.setStream(true);
        return client.chatStream(completionRequest)
            .timeout(Duration.ofSeconds(60));
    }
}
