package com.apim.domain.gateway;

import com.apim.infra.apisix.dto.ApisixNode;
import com.apim.infra.apisix.dto.ConsumerDto;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Gateway abstraction for APISIX Consumer operations.
 */
public interface ConsumerGateway {

    Mono<List<ApisixNode<ConsumerDto>>> list();

    Mono<ConsumerDto> findById(String username);
}
