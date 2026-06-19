# Architecture Rules

1. APISIX is traffic plane.
2. Spring Boot is management plane.
3. No APISIX business logic.
4. Domain services cannot call APISIX directly.
5. All deployments must be auditable.
6. All deployments must be rollbackable.
