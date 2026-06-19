-- V3__refactor_services.sql
-- Refactor services table: add service_code, drop owner and audit-user columns

ALTER TABLE services ADD COLUMN service_code VARCHAR(100) NOT NULL DEFAULT '';
UPDATE services SET service_code = name WHERE service_code = '';
ALTER TABLE services ADD CONSTRAINT uk_services_service_code UNIQUE (service_code);

ALTER TABLE services DROP COLUMN IF EXISTS owner;
ALTER TABLE services DROP COLUMN IF EXISTS created_by;
ALTER TABLE services DROP COLUMN IF EXISTS updated_by;

DROP INDEX IF EXISTS idx_services_owner;
CREATE INDEX idx_services_service_code ON services (service_code);
