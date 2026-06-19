package com.apim.application;

import com.apim.domain.Service;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Use case: List all Services.
 * Delegates to {@link ServiceApplicationService}.
 */
@Component
public class ListServicesUseCase {

    private final ServiceApplicationService applicationService;

    public ListServicesUseCase(ServiceApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public List<Service> execute() {
        return applicationService.listServices();
    }
}
