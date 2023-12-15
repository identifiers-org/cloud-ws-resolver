package org.identifiers.cloud.ws.resolver.periodictasks.models;

import org.identifiers.cloud.ws.resolver.data.models.Namespace;

import java.util.List;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.daemons.models
 * Timestamp: 2018-01-18 9:46
 * ---
 */
public interface ResolverDataSourcer {
    List<Namespace> getResolverData() throws ResolverDataSourcerException;
}
