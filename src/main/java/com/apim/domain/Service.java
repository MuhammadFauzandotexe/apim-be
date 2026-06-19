package com.apim.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Service aggregate root.
 * Represents business service ownership within the API management platform.
 *
 * Fields: id, serviceCode, name, description, status, createdAt, updatedAt.
 */
public final class Service {

    private final UUID id;
    private final String serviceCode;
    private final String name;
    private final String description;
    private final ServiceStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Service(UUID id, String serviceCode, String name, String description,
                   ServiceStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.serviceCode = requireNotBlank(serviceCode, "serviceCode");
        this.name = requireNotBlank(name, "name");
        this.description = description;
        this.status = status != null ? status : ServiceStatus.ACTIVE;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory for creating a new Service (no id or audit timestamps yet).
     */
    public static Service create(String serviceCode, String name, String description) {
        return new Service(null, serviceCode, name, description, ServiceStatus.ACTIVE, null, null);
    }

    /**
     * Returns a new Service with updated fields, preserving id and createdAt.
     */
    public Service withUpdate(String serviceCode, String name, String description, ServiceStatus status) {
        return new Service(this.id, serviceCode, name, description, status, this.createdAt, this.updatedAt);
    }

    // ---- Accessors ----

    public UUID id() {
        return id;
    }

    public String serviceCode() {
        return serviceCode;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public ServiceStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    // ---- Validation ----

    private static String requireNotBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
