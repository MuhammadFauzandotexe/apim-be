package com.apim.infra.apisix.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * APISIX Upstream resource representation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UpstreamDto(
        String id,
        String name,
        String desc,
        String type,
        List<Map<String, Object>> nodes,
        Integer timeout,
        Integer retries
) {
}
