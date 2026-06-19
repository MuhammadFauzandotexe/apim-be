package com.apim.infra.apisix;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configures the WebClient used for all APISIX Admin API communication.
 */
@Configuration
@EnableConfigurationProperties(ApisixProperties.class)
public class ApisixWebClientConfig {

    @Bean
    public WebClient apisixWebClient(ApisixProperties properties) {
        var builder = WebClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("Content-Type", "application/json");

        if (!properties.apiKey().isBlank()) {
            builder.defaultHeader("X-API-KEY", properties.apiKey());
        }

        return builder.build();
    }
}
