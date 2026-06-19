package com.apim.application;

import com.apim.domain.Service;
import com.apim.web.dto.CreateServiceRequest;
import org.springframework.stereotype.Component;

/**
 * Use case: Create a new Service.
 * Delegates to {@link ServiceApplicationService}.
 */
@Component
public class CreateServiceUseCase {

    private final ServiceApplicationService applicationService;

    public CreateServiceUseCase(ServiceApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public Service execute(CreateServiceRequest request) {
        return applicationService.createService(request);
    }
}
