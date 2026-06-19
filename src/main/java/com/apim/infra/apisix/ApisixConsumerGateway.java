package com.apim.infra.apisix;

import com.apim.domain.ApisixException;
import com.apim.domain.gateway.ConsumerGateway;
import com.apim.infra.apisix.dto.ApisixNode;
import com.apim.infra.apisix.dto.ConsumerDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * APISIX implementation of {@link ConsumerGateway}.
 * Communicates with the APISIX Admin API at /apisix/admin/consumers.
 */
@Component
public class ApisixConsumerGateway implements ConsumerGateway {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ApisixConsumerGateway(WebClient apisixWebClient, ObjectMapper objectMapper) {
        this.webClient = apisixWebClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<List<ApisixNode<ConsumerDto>>> list() {
        return webClient.get()
                .uri("/apisix/admin/consumers")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .map(body -> new ApisixException(
                                        "Failed to list consumers: " + body,
                                        response.statusCode().value())))
                .bodyToMono(JsonNode.class)
                .map(this::parseNodeList);
    }

    @Override
    public Mono<ConsumerDto> findById(String username) {
        return webClient.get()
                .uri("/apisix/admin/consumers/{username}", username)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .map(body -> new ApisixException(
                                        "Failed to get consumer " + username + ": " + body,
                                        response.statusCode().value())))
                .bodyToMono(JsonNode.class)
                .flatMap(node -> {
                    try {
                        return Mono.just(objectMapper.treeToValue(node.get("value"), ConsumerDto.class));
                    } catch (Exception e) {
                        return Mono.error(new ApisixException("Failed to parse consumer response", e));
                    }
                });
    }

    private List<ApisixNode<ConsumerDto>> parseNodeList(JsonNode root) {
        var listNode = root.get("list");
        if (listNode == null || !listNode.isArray()) {
            return List.of();
        }
        var result = new ArrayList<ApisixNode<ConsumerDto>>();
        for (JsonNode node : listNode) {
            var key = node.has("key") ? node.get("key").asText() : null;
            var value = objectMapper.convertValue(node.get("value"), ConsumerDto.class);
            result.add(new ApisixNode<>(key, value));
        }
        return result;
    }
}
