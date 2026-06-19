package com.apim.web.dto;

import com.apim.domain.ServiceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for updating an existing Service.
 */
public record UpdateServiceRequest(
        @NotBlank(message = "serviceCode is required")
        @Size(max = 100, message = "serviceCode must not exceed 100 characters")
        @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9-]*$", message = "serviceCode must start with a letter and contain only alphanumeric characters or hyphens")
        String serviceCode,

        @NotBlank(message = "name is required")
        @Size(max = 255, message = "name must not exceed 255 characters")
        String name,

        @Size(max = 1000, message = "description must not exceed 1000 characters")
        String description,

        @NotNull(message = "status is required")
        ServiceStatus status
) {
}
