package com.apim.domain.gateway;

import com.apim.infra.apisix.dto.ApisixNode;
import com.apim.infra.apisix.dto.UpstreamDto;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Gateway abstraction for APISIX Upstream operations.
 */
public interface UpstreamGateway {

    Mono<List<ApisixNode<UpstreamDto>>> list();

    Mono<UpstreamDto> findById(String id);
}
