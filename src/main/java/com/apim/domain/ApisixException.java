package com.apim.domain;

/**
 * Thrown when communication with APISIX Admin API fails.
 */
public class ApisixException extends RuntimeException {

    private final int statusCode;

    public ApisixException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ApisixException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
