package com.apim.application;

import com.apim.domain.Service;
import com.apim.domain.ServiceRepository;
import com.apim.domain.ServiceStatus;
import com.apim.web.ServiceNotFoundException;
import com.apim.web.dto.CreateServiceRequest;
import com.apim.web.dto.UpdateServiceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceApplicationServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceApplicationService serviceApplicationService;

    private static final UUID SERVICE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final Instant NOW = Instant.parse("2026-06-20T00:00:00Z");

    private Service existingService;

    @BeforeEach
    void setUp() {
        existingService = new Service(SERVICE_ID, "payment-svc", "Payment Service",
                "Handles payments", ServiceStatus.ACTIVE, NOW, NOW);
    }

    // ---- createService ----

    @Test
    void createService_shouldPersistAndReturn() {
        var request = new CreateServiceRequest("payment-svc", "Payment Service", "Handles payments");
        when(serviceRepository.existsByServiceCode("payment-svc")).thenReturn(false);
        when(serviceRepository.save(any(Service.class))).thenReturn(existingService);

        var result = serviceApplicationService.createService(request);

        assertThat(result.serviceCode()).isEqualTo("payment-svc");
        assertThat(result.name()).isEqualTo("Payment Service");

        var captor = ArgumentCaptor.forClass(Service.class);
        verify(serviceRepository).save(captor.capture());
        assertThat(captor.getValue().serviceCode()).isEqualTo("payment-svc");
    }

    @Test
    void createService_duplicateCode_shouldThrow() {
        var request = new CreateServiceRequest("payment-svc", "Payment Service", "desc");
        when(serviceRepository.existsByServiceCode("payment-svc")).thenReturn(true);

        assertThatThrownBy(() -> serviceApplicationService.createService(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Service code already exists");

        verify(serviceRepository, never()).save(any());
    }

    // ---- updateService ----

    @Test
    void updateService_shouldPersistAndReturn() {
        var request = new UpdateServiceRequest("payment-v2", "Payment V2", "Updated", ServiceStatus.INACTIVE);
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(existingService));
        when(serviceRepository.existsByServiceCodeAndIdNot("payment-v2", SERVICE_ID)).thenReturn(false);
        when(serviceRepository.save(any(Service.class))).thenAnswer(inv -> {
            var svc = inv.getArgument(0, Service.class);
            return new Service(SERVICE_ID, svc.serviceCode(), svc.name(), svc.description(),
                    svc.status(), NOW, NOW);
        });

        var result = serviceApplicationService.updateService(SERVICE_ID, request);

        assertThat(result.serviceCode()).isEqualTo("payment-v2");
        assertThat(result.status()).isEqualTo(ServiceStatus.INACTIVE);
    }

    @Test
    void updateService_notFound_shouldThrow() {
        var request = new UpdateServiceRequest("code", "Name", null, ServiceStatus.ACTIVE);
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceApplicationService.updateService(SERVICE_ID, request))
                .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void updateService_duplicateCode_shouldThrow() {
        var request = new UpdateServiceRequest("existing-code", "Name", null, ServiceStatus.ACTIVE);
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(existingService));
        when(serviceRepository.existsByServiceCodeAndIdNot("existing-code", SERVICE_ID)).thenReturn(true);

        assertThatThrownBy(() -> serviceApplicationService.updateService(SERVICE_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Service code already exists");
    }

    // ---- deleteService ----

    @Test
    void deleteService_existing_shouldDelete() {
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(existingService));

        serviceApplicationService.deleteService(SERVICE_ID);

        verify(serviceRepository).deleteById(SERVICE_ID);
    }

    @Test
    void deleteService_notFound_shouldThrow() {
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceApplicationService.deleteService(SERVICE_ID))
                .isInstanceOf(ServiceNotFoundException.class);

        verify(serviceRepository, never()).deleteById(any());
    }

    // ---- getService ----

    @Test
    void getService_existing_shouldReturn() {
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.of(existingService));

        var result = serviceApplicationService.getService(SERVICE_ID);

        assertThat(result.id()).isEqualTo(SERVICE_ID);
        assertThat(result.serviceCode()).isEqualTo("payment-svc");
    }

    @Test
    void getService_notFound_shouldThrow() {
        when(serviceRepository.findById(SERVICE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceApplicationService.getService(SERVICE_ID))
                .isInstanceOf(ServiceNotFoundException.class);
    }

    // ---- listServices ----

    @Test
    void listServices_shouldReturnAll() {
        when(serviceRepository.findAll()).thenReturn(List.of(existingService));

        var result = serviceApplicationService.listServices();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().serviceCode()).isEqualTo("payment-svc");
    }

    @Test
    void listServices_empty_shouldReturnEmptyList() {
        when(serviceRepository.findAll()).thenReturn(List.of());

        var result = serviceApplicationService.listServices();

        assertThat(result).isEmpty();
    }
}
