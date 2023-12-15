package org.identifiers.cloud.ws.resolver.periodictasks.models;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.daemons.models
 * Timestamp: 2018-01-18 9:56
 * ---
 */
public class ResolverDataSourcerException extends RuntimeException {
    public ResolverDataSourcerException(String message) {
        super(message);
    }

    public ResolverDataSourcerException(String message, Throwable cause) {
        super(message, cause);
    }
}
