package com.apim.infra.apisix;

import com.apim.domain.ApisixException;
import com.apim.domain.gateway.PluginGateway;
import com.apim.infra.apisix.dto.PluginSchemaDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * APISIX implementation of {@link PluginGateway}.
 * Communicates with the APISIX Admin API at /apisix/admin/schema/plugins.
 */
@Component
public class ApisixPluginGateway implements PluginGateway {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ApisixPluginGateway(WebClient apisixWebClient, ObjectMapper objectMapper) {
        this.webClient = apisixWebClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Map<String, PluginSchemaDto>> listSchemas() {
        return webClient.get()
                .uri("/apisix/admin/schema/plugins")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .map(body -> new ApisixException(
                                        "Failed to list plugin schemas: " + body,
                                        response.statusCode().value())))
                .bodyToMono(JsonNode.class)
                .map(this::parsePluginSchemas);
    }

    @Override
    public Mono<PluginSchemaDto> getSchema(String pluginName) {
        return webClient.get()
                .uri("/apisix/admin/schema/plugins/{name}", pluginName)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .map(body -> new ApisixException(
                                        "Failed to get plugin schema " + pluginName + ": " + body,
                                        response.statusCode().value())))
                .bodyToMono(JsonNode.class)
                .map(node -> new PluginSchemaDto(pluginName, objectMapper.convertValue(node, Map.class)));
    }

    private Map<String, PluginSchemaDto> parsePluginSchemas(JsonNode root) {
        var result = new LinkedHashMap<String, PluginSchemaDto>();
        var fields = root.fields();
        while (fields.hasNext()) {
            var entry = fields.next();
            var name = entry.getKey();
            var schemaMap = objectMapper.convertValue(entry.getValue(), Map.class);
            result.put(name, new PluginSchemaDto(name, schemaMap));
        }
        return result;
    }
}
