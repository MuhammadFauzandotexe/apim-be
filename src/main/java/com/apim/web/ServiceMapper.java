package com.apim.web;

import com.apim.domain.Service;
import com.apim.web.dto.ServiceResponse;
import org.springframework.stereotype.Component;

/**
 * Maps between domain {@link Service} and web DTOs.
 */
@Component
public class ServiceMapper {

    public ServiceResponse toResponse(Service service) {
        return new ServiceResponse(
                service.id(),
                service.serviceCode(),
                service.name(),
                service.description(),
                service.status(),
                service.createdAt(),
                service.updatedAt()
        );
    }
}
