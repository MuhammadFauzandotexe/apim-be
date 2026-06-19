package com.apim.infra.apisix;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for APISIX Admin API connection.
 */
@ConfigurationProperties(prefix = "apisix")
public record ApisixProperties(
        String baseUrl,
        String apiKey
) {
    public ApisixProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:9180";
        }
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = "";
        }
    }
}
