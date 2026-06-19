package com.apim.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceTest {

    private static final UUID ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final Instant NOW = Instant.parse("2026-06-20T00:00:00Z");

    @Test
    void create_shouldCreateServiceWithActiveStatus() {
        var service = Service.create("payment-svc", "Payment Service", "Handles payments");

        assertThat(service.id()).isNull();
        assertThat(service.serviceCode()).isEqualTo("payment-svc");
        assertThat(service.name()).isEqualTo("Payment Service");
        assertThat(service.description()).isEqualTo("Handles payments");
        assertThat(service.status()).isEqualTo(ServiceStatus.ACTIVE);
        assertThat(service.createdAt()).isNull();
        assertThat(service.updatedAt()).isNull();
    }

    @Test
    void constructor_shouldSetAllFields() {
        var service = new Service(ID, "payment-svc", "Payment Service", "desc",
                ServiceStatus.INACTIVE, NOW, NOW);

        assertThat(service.id()).isEqualTo(ID);
        assertThat(service.serviceCode()).isEqualTo("payment-svc");
        assertThat(service.name()).isEqualTo("Payment Service");
        assertThat(service.description()).isEqualTo("desc");
        assertThat(service.status()).isEqualTo(ServiceStatus.INACTIVE);
        assertThat(service.createdAt()).isEqualTo(NOW);
        assertThat(service.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void constructor_nullStatus_shouldDefaultToActive() {
        var service = new Service(ID, "svc", "Name", null, null, null, null);

        assertThat(service.status()).isEqualTo(ServiceStatus.ACTIVE);
    }

    @Test
    void constructor_nullServiceCode_shouldThrow() {
        assertThatThrownBy(() -> new Service(ID, null, "Name", null, null, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("serviceCode");
    }

    @Test
    void constructor_blankServiceCode_shouldThrow() {
        assertThatThrownBy(() -> new Service(ID, "  ", "Name", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("serviceCode");
    }

    @Test
    void constructor_nullName_shouldThrow() {
        assertThatThrownBy(() -> new Service(ID, "svc", null, null, null, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("name");
    }

    @Test
    void constructor_blankName_shouldThrow() {
        assertThatThrownBy(() -> new Service(ID, "svc", "", null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void withUpdate_shouldReturnNewServiceWithUpdatedFields() {
        var original = new Service(ID, "old-code", "Old Name", "Old desc",
                ServiceStatus.ACTIVE, NOW, NOW);

        var updated = original.withUpdate("new-code", "New Name", "New desc", ServiceStatus.INACTIVE);

        assertThat(updated.id()).isEqualTo(ID);
        assertThat(updated.serviceCode()).isEqualTo("new-code");
        assertThat(updated.name()).isEqualTo("New Name");
        assertThat(updated.description()).isEqualTo("New desc");
        assertThat(updated.status()).isEqualTo(ServiceStatus.INACTIVE);
        assertThat(updated.createdAt()).isEqualTo(NOW);
        assertThat(updated.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void withUpdate_shouldPreserveOriginalId() {
        var original = new Service(ID, "svc", "Name", null, ServiceStatus.ACTIVE, NOW, NOW);

        var updated = original.withUpdate("svc", "Updated", null, ServiceStatus.ACTIVE);

        assertThat(updated).isNotSameAs(original);
        assertThat(updated.id()).isEqualTo(original.id());
    }

    @Test
    void create_nullDescription_shouldBeAllowed() {
        var service = Service.create("svc", "Name", null);

        assertThat(service.description()).isNull();
    }
}
