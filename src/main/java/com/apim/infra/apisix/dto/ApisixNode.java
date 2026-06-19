package com.apim.infra.apisix.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Generic APISIX node wrapper.
 * APISIX returns list results as nodes containing key (path/id) and value (resource data).
 *
 * @param id    the resource key path (e.g., "/apisix/routes/1")
 * @param value the resource data
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApisixNode<T>(String id, T value) {
}
