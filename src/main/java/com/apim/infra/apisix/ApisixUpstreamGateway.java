package com.apim.infra.apisix;

import com.apim.domain.ApisixException;
import com.apim.domain.gateway.UpstreamGateway;
import com.apim.infra.apisix.dto.ApisixNode;
import com.apim.infra.apisix.dto.UpstreamDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * APISIX implementation of {@link UpstreamGateway}.
 * Communicates with the APISIX Admin API at /apisix/admin/upstreams.
 */
@Component
public class ApisixUpstreamGateway implements UpstreamGateway {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ApisixUpstreamGateway(WebClient apisixWebClient, ObjectMapper objectMapper) {
        this.webClient = apisixWebClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<List<ApisixNode<UpstreamDto>>> list() {
        return webClient.get()
                .uri("/apisix/admin/upstreams")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .map(body -> new ApisixException(
                                        "Failed to list upstreams: " + body,
                                        response.statusCode().value())))
                .bodyToMono(JsonNode.class)
                .map(this::parseNodeList);
    }

    @Override
    public Mono<UpstreamDto> findById(String id) {
        return webClient.get()
                .uri("/apisix/admin/upstreams/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .map(body -> new ApisixException(
                                        "Failed to get upstream " + id + ": " + body,
                                        response.statusCode().value())))
                .bodyToMono(JsonNode.class)
                .flatMap(node -> {
                    try {
                        return Mono.just(objectMapper.treeToValue(node.get("value"), UpstreamDto.class));
                    } catch (Exception e) {
                        return Mono.error(new ApisixException("Failed to parse upstream response", e));
                    }
                });
    }

    private List<ApisixNode<UpstreamDto>> parseNodeList(JsonNode root) {
        var listNode = root.get("list");
        if (listNode == null || !listNode.isArray()) {
            return List.of();
        }
        var result = new ArrayList<ApisixNode<UpstreamDto>>();
        for (JsonNode node : listNode) {
            var key = node.has("key") ? node.get("key").asText() : null;
            var value = objectMapper.convertValue(node.get("value"), UpstreamDto.class);
            result.add(new ApisixNode<>(key, value));
        }
        return result;
    }
}
