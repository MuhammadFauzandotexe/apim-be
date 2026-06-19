package com.apim.infra.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing for automatic population of
 * createdAt and updatedAt timestamp fields.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {
}
