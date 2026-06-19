package com.apim.infra.persistence;

import com.apim.domain.Service;
import com.apim.domain.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter implementing {@link ServiceRepository}.
 * Converts between domain {@link Service} and JPA {@link ServiceEntity}.
 */
@Repository
public class JpaServiceRepository implements ServiceRepository {

    private static final Logger log = LoggerFactory.getLogger(JpaServiceRepository.class);

    private final SpringDataServiceRepository springDataRepo;

    public JpaServiceRepository(SpringDataServiceRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public List<Service> findAll() {
        return springDataRepo.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Service> findById(UUID id) {
        return springDataRepo.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Service save(Service service) {
        var entity = toEntity(service);
        var saved = springDataRepo.save(entity);
        log.info("Service persisted: id={}, serviceCode={}, name={}",
                saved.getId(), saved.getServiceCode(), saved.getName());
        return toDomain(saved);
    }

    @Override
    public boolean existsByServiceCode(String serviceCode) {
        return springDataRepo.existsByServiceCode(serviceCode);
    }

    @Override
    public boolean existsByServiceCodeAndIdNot(String serviceCode, UUID id) {
        return springDataRepo.existsByServiceCodeAndIdNot(serviceCode, id);
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepo.deleteById(id);
        log.info("Service deleted: id={}", id);
    }

    // ---- Mapping ----

    private Service toDomain(ServiceEntity entity) {
        return new Service(
                entity.getId(),
                entity.getServiceCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ServiceEntity toEntity(Service service) {
        ServiceEntity entity;
        if (service.id() != null) {
            entity = springDataRepo.findById(service.id())
                    .orElseGet(ServiceEntity::new);
        } else {
            entity = new ServiceEntity();
        }
        entity.setServiceCode(service.serviceCode());
        entity.setName(service.name());
        entity.setDescription(service.description());
        entity.setStatus(service.status());
        return entity;
    }
}
