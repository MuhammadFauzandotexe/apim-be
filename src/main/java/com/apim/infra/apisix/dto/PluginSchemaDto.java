package com.apim.infra.apisix.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * APISIX Plugin schema representation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PluginSchemaDto(
        String name,
        Map<String, Object> schema
) {
}
