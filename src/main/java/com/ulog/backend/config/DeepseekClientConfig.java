package com.ulog.backend.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
public class DeepseekClientConfig {

    @Bean
    public WebClient deepseekWebClient(DeepseekProperties props) {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getTimeoutMs())
            .responseTimeout(Duration.ofMillis(props.getTimeoutMs()))
            .doOnConnected(conn -> {
                int timeoutSeconds = Math.max(1, props.getTimeoutMs() / 1000);
                conn.addHandlerLast(new ReadTimeoutHandler(timeoutSeconds));
                conn.addHandlerLast(new WriteTimeoutHandler(timeoutSeconds));
            });

        WebClient.Builder builder = WebClient.builder()
            .baseUrl(props.getBaseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(httpClient));

        if (props.isEnableLogging()) {
            builder.filter(logRequest()).filter(logResponse());
        }

        return builder.build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            System.out.println("[DeepSeek][REQ] " + request.method() + " " + request.url());
            return Mono.just(request);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            System.out.println("[DeepSeek][RES] status=" + response.statusCode());
            return Mono.just(response);
        });
    }
}
