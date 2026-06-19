package com.apim.application;

import com.apim.domain.Service;
import com.apim.web.dto.UpdateServiceRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case: Update an existing Service.
 * Delegates to {@link ServiceApplicationService}.
 */
@Component
public class UpdateServiceUseCase {

    private final ServiceApplicationService applicationService;

    public UpdateServiceUseCase(ServiceApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public Service execute(UUID id, UpdateServiceRequest request) {
        return applicationService.updateService(id, request);
    }
}
