package com.apim.application;

import com.apim.domain.Service;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case: Get a Service by id.
 * Delegates to {@link ServiceApplicationService}.
 */
@Component
public class GetServiceUseCase {

    private final ServiceApplicationService applicationService;

    public GetServiceUseCase(ServiceApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public Service execute(UUID id) {
        return applicationService.getService(id);
    }
}
