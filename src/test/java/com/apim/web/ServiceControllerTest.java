package com.apim.web;

import com.apim.application.ServiceApplicationService;
import com.apim.domain.Service;
import com.apim.domain.ServiceStatus;
import com.apim.web.dto.CreateServiceRequest;
import com.apim.web.dto.ServiceResponse;
import com.apim.web.dto.UpdateServiceRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceController.class)
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ServiceApplicationService serviceApplicationService;

    @MockitoBean
    private ServiceMapper serviceMapper;

    private static final UUID SERVICE_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final Instant NOW = Instant.parse("2026-06-20T00:00:00Z");

    @Test
    void listServices_returnsAllServices() throws Exception {
        var service = createDomainService("payment-svc", "Payment Service");
        when(serviceApplicationService.listServices()).thenReturn(List.of(service));
        when(serviceMapper.toResponse(service)).thenReturn(createResponse(service));

        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serviceCode").value("payment-svc"))
                .andExpect(jsonPath("$[0].name").value("Payment Service"));
    }

    @Test
    void getById_returnsService() throws Exception {
        var service = createDomainService("payment-svc", "Payment Service");
        when(serviceApplicationService.getService(SERVICE_ID)).thenReturn(service);
        when(serviceMapper.toResponse(service)).thenReturn(createResponse(service));

        mockMvc.perform(get("/api/services/{id}", SERVICE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SERVICE_ID.toString()))
                .andExpect(jsonPath("$.serviceCode").value("payment-svc"))
                .andExpect(jsonPath("$.name").value("Payment Service"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(serviceApplicationService.getService(SERVICE_ID))
                .thenThrow(new ServiceNotFoundException(SERVICE_ID));

        mockMvc.perform(get("/api/services/{id}", SERVICE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Service Not Found"));
    }

    @Test
    void create_returnsCreatedService() throws Exception {
        var request = new CreateServiceRequest("payment-svc", "Payment Service", "Handles payments");
        var created = new Service(SERVICE_ID, "payment-svc", "Payment Service",
                "Handles payments", ServiceStatus.ACTIVE, NOW, NOW);

        when(serviceApplicationService.createService(any(CreateServiceRequest.class))).thenReturn(created);
        when(serviceMapper.toResponse(created)).thenReturn(createResponse(created));

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/services/" + SERVICE_ID)))
                .andExpect(jsonPath("$.serviceCode").value("payment-svc"))
                .andExpect(jsonPath("$.name").value("Payment Service"));
    }

    @Test
    void create_invalidRequest_returns400() throws Exception {
        var request = new CreateServiceRequest("", "", null);

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"));
    }

    @Test
    void create_invalidServiceCodePattern_returns400() throws Exception {
        var request = new CreateServiceRequest("123-invalid", "Name", null);

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"));
    }

    @Test
    void create_duplicateCode_returns409() throws Exception {
        var request = new CreateServiceRequest("payment-svc", "Payment", null);
        when(serviceApplicationService.createService(any(CreateServiceRequest.class)))
                .thenThrow(new IllegalArgumentException("Service code already exists: payment-svc"));

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    void update_returnsUpdatedService() throws Exception {
        var request = new UpdateServiceRequest("payment-v2", "Payment V2", "Updated", ServiceStatus.INACTIVE);
        var updated = new Service(SERVICE_ID, "payment-v2", "Payment V2",
                "Updated", ServiceStatus.INACTIVE, NOW, NOW);

        when(serviceApplicationService.updateService(eq(SERVICE_ID), any(UpdateServiceRequest.class)))
                .thenReturn(updated);
        when(serviceMapper.toResponse(updated)).thenReturn(createResponse(updated));

        mockMvc.perform(put("/api/services/{id}", SERVICE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serviceCode").value("payment-v2"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/services/{id}", SERVICE_ID))
                .andExpect(status().isNoContent());

        verify(serviceApplicationService).deleteService(SERVICE_ID);
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new ServiceNotFoundException(SERVICE_ID))
                .when(serviceApplicationService).deleteService(SERVICE_ID);

        mockMvc.perform(delete("/api/services/{id}", SERVICE_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Service Not Found"));
    }

    // ---- Helpers ----

    private Service createDomainService(String serviceCode, String name) {
        return new Service(SERVICE_ID, serviceCode, name, "desc",
                ServiceStatus.ACTIVE, NOW, NOW);
    }

    private ServiceResponse createResponse(Service service) {
        return new ServiceResponse(
                service.id(), service.serviceCode(), service.name(),
                service.description(), service.status(),
                service.createdAt(), service.updatedAt()
        );
    }
}
