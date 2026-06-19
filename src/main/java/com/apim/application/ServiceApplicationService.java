package com.apim.application;

import com.apim.domain.Service;
import com.apim.domain.ServiceRepository;
import com.apim.web.ServiceNotFoundException;
import com.apim.web.dto.CreateServiceRequest;
import com.apim.web.dto.UpdateServiceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Application service facade for Service management.
 * Orchestrates all service use cases: create, update, delete, get, list.
 * Controllers should depend on this class.
 */
@Component
public class ServiceApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ServiceApplicationService.class);

    private final ServiceRepository serviceRepository;

    public ServiceApplicationService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    // ---- Commands ----

    public Service createService(CreateServiceRequest request) {
        if (serviceRepository.existsByServiceCode(request.serviceCode())) {
            throw new IllegalArgumentException("Service code already exists: " + request.serviceCode());
        }
        var service = Service.create(request.serviceCode(), request.name(), request.description());
        var saved = serviceRepository.save(service);
        log.info("Service created: id={}, serviceCode={}, name={}", saved.id(), saved.serviceCode(), saved.name());
        return saved;
    }

    public Service updateService(UUID id, UpdateServiceRequest request) {
        var existing = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));

        if (serviceRepository.existsByServiceCodeAndIdNot(request.serviceCode(), id)) {
            throw new IllegalArgumentException("Service code already exists: " + request.serviceCode());
        }

        var updated = existing.withUpdate(
                request.serviceCode(), request.name(), request.description(), request.status());
        var saved = serviceRepository.save(updated);
        log.info("Service updated: id={}, serviceCode={}, status={}", saved.id(), saved.serviceCode(), saved.status());
        return saved;
    }

    public void deleteService(UUID id) {
        serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));
        serviceRepository.deleteById(id);
        log.info("Service deleted: id={}", id);
    }

    // ---- Queries ----

    public Service getService(UUID id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));
    }

    public List<Service> listServices() {
        return serviceRepository.findAll();
    }
}
