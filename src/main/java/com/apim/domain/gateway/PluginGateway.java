package com.apim.domain.gateway;

import com.apim.infra.apisix.dto.PluginSchemaDto;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Gateway abstraction for APISIX Plugin operations.
 */
public interface PluginGateway {

    Mono<Map<String, PluginSchemaDto>> listSchemas();

    Mono<PluginSchemaDto> getSchema(String pluginName);
}
