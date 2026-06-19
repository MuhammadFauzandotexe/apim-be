package com.apim.infra.apisix;

import com.apim.domain.ApisixException;
import com.apim.domain.gateway.RouteGateway;
import com.apim.infra.apisix.dto.ApisixNode;
import com.apim.infra.apisix.dto.RouteDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * APISIX implementation of {@link RouteGateway}.
 * Communicates with the APISIX Admin API at /apisix/admin/routes.
 */
@Component
public class ApisixRouteGateway implements RouteGateway {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ApisixRouteGateway(WebClient apisixWebClient, ObjectMapper objectMapper) {
        this.webClient = apisixWebClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<List<ApisixNode<RouteDto>>> list() {
        return webClient.get()
                .uri("/apisix/admin/routes")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .map(body -> new ApisixException(
                                        "Failed to list routes: " + body,
                                        response.statusCode().value())))
                .bodyToMono(JsonNode.class)
                .map(this::parseNodeList);
    }

    @Override
    public Mono<RouteDto> findById(String id) {
        return webClient.get()
                .uri("/apisix/admin/routes/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .map(body -> new ApisixException(
                                        "Failed to get route " + id + ": " + body,
                                        response.statusCode().value())))
                .bodyToMono(JsonNode.class)
                .flatMap(node -> {
                    try {
                        return Mono.just(objectMapper.treeToValue(node.get("value"), RouteDto.class));
                    } catch (Exception e) {
                        return Mono.error(new ApisixException("Failed to parse route response", e));
                    }
                });
    }

    private List<ApisixNode<RouteDto>> parseNodeList(JsonNode root) {
        var nodesNode = root.get("list");
        if (nodesNode == null || !nodesNode.isArray()) {
            return List.of();
        }
        var result = new ArrayList<ApisixNode<RouteDto>>();
        for (JsonNode node : nodesNode) {
            var key = node.has("key") ? node.get("key").asText() : null;
            var value = objectMapper.convertValue(node.get("value"), RouteDto.class);
            result.add(new ApisixNode<>(key, value));
        }
        return result;
    }
}
