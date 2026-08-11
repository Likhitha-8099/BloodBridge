package com.bloodbridge.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Spring Configuration for the Groq AI HTTP client.
 * Uses Spring Boot 3.3 RestClient with configured timeouts.
 */
@Configuration
public class GroqConfig {

    @Value("${groq.timeout-seconds:15}")
    private int timeoutSeconds;

    @Bean
    public RestClient groqRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(timeoutSeconds).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(timeoutSeconds).toMillis());

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
