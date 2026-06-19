package com.apim.web;

import java.util.UUID;

/**
 * Thrown when a Service is not found by id.
 */
public class ServiceNotFoundException extends RuntimeException {

    public ServiceNotFoundException(UUID id) {
        super("Service not found: " + id);
    }
}
