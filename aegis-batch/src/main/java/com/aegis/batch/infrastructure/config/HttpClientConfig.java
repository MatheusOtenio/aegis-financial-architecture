package com.aegis.batch.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class HttpClientConfig {

    @Value("${aegis.backend.url}")
    private String backendUrl;

    @Value("${aegis.backend.token}")
    private String serviceToken;

    @Bean
    public WebClient backendWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(backendUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-SERVICE-TOKEN", serviceToken)
                // Aumentar buffer para lidar com payloads grandes se necessário,
                // mas o streaming deve mitigar uso de memória.
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB buffer segurança
                .build();
    }
}
