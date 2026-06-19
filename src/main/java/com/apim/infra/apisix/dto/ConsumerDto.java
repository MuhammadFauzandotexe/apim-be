package com.apim.infra.apisix.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * APISIX Consumer resource representation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConsumerDto(
        String username,
        String desc,
        Map<String, Object> plugins
) {
}
