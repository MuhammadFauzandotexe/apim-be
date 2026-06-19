package com.apim.domain.gateway;

import com.apim.infra.apisix.dto.ApisixNode;
import com.apim.infra.apisix.dto.RouteDto;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Gateway abstraction for APISIX Route operations.
 */
public interface RouteGateway {

    Mono<List<ApisixNode<RouteDto>>> list();

    Mono<RouteDto> findById(String id);
}
