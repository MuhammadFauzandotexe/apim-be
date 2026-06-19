package com.apim.infra.apisix.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * APISIX Route resource representation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RouteDto(
        String id,
        String name,
        String desc,
        List<String> methods,
        String uri,
        String host,
        Integer priority,
        Map<String, Object> upstream,
        Map<String, Object> plugins,
        String status
) {
}
