package com.apim.infra.apisix;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Generic APISIX single-resource response wrapper.
 * APISIX returns single resources wrapped in {"key": "...", "value": {...}}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApisixResponse<T>(String key, T value) {
}
