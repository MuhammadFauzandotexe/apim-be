package com.apim.application;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case: Delete a Service by id.
 * Delegates to {@link ServiceApplicationService}.
 */
@Component
public class DeleteServiceUseCase {

    private final ServiceApplicationService applicationService;

    public DeleteServiceUseCase(ServiceApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public void execute(UUID id) {
        applicationService.deleteService(id);
    }
}
