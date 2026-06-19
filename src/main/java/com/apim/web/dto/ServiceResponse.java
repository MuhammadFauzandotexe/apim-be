package com.apim.web.dto;

import com.apim.domain.ServiceStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Response body representing a Service.
 */
public record ServiceResponse(
        UUID id,
        String serviceCode,
        String name,
        String description,
        ServiceStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
