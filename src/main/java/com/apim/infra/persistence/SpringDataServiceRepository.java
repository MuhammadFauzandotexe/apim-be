package com.apim.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for ServiceEntity.
 */
public interface SpringDataServiceRepository extends JpaRepository<ServiceEntity, UUID> {

    Optional<ServiceEntity> findByServiceCode(String serviceCode);

    boolean existsByServiceCode(String serviceCode);

    boolean existsByServiceCodeAndIdNot(String serviceCode, UUID id);
}
