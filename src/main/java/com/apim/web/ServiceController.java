package com.apim.web;

import com.apim.application.ServiceApplicationService;
import com.apim.web.dto.CreateServiceRequest;
import com.apim.web.dto.ServiceResponse;
import com.apim.web.dto.UpdateServiceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Service management.
 * Delegates to {@link ServiceApplicationService}; does not access repositories directly.
 */
@RestController
@RequestMapping("/api/services")
@Tag(name = "Services", description = "Service management operations")
public class ServiceController {

    private final ServiceApplicationService serviceApplicationService;
    private final ServiceMapper mapper;

    public ServiceController(ServiceApplicationService serviceApplicationService,
                             ServiceMapper mapper) {
        this.serviceApplicationService = serviceApplicationService;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "List all services", description = "Returns all registered services")
    public List<ServiceResponse> list() {
        return serviceApplicationService.listServices().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service by ID", description = "Returns a single service by its UUID")
    public ServiceResponse getById(@PathVariable UUID id) {
        var service = serviceApplicationService.getService(id);
        return mapper.toResponse(service);
    }

    @PostMapping
    @Operation(summary = "Create a service", description = "Creates a new service with the provided details")
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody CreateServiceRequest request,
                                                  UriComponentsBuilder uriBuilder) {
        var created = serviceApplicationService.createService(request);
        var location = uriBuilder.path("/api/services/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a service", description = "Updates an existing service by its UUID")
    public ServiceResponse update(@PathVariable UUID id,
                                  @Valid @RequestBody UpdateServiceRequest request) {
        var updated = serviceApplicationService.updateService(id, request);
        return mapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a service", description = "Deletes a service by its UUID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        serviceApplicationService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
