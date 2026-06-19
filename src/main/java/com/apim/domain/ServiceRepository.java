package com.apim.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for the Service aggregate.
 * Implemented by the infrastructure layer (JPA adapter).
 */
public interface ServiceRepository {

    List<Service> findAll();

    Optional<Service> findById(UUID id);

    Service save(Service service);

    boolean existsByServiceCode(String serviceCode);

    boolean existsByServiceCodeAndIdNot(String serviceCode, UUID id);

    void deleteById(UUID id);
}
