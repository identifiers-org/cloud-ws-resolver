package org.identifiers.cloud.ws.resolver.daemons.models;

import org.identifiers.cloud.ws.resolver.data.models.PidEntry;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.daemons.models
 * Timestamp: 2018-01-29 10:16
 * ---
 */
public class ResolverDataSourcerFromWs implements ResolverDataSourcer {
    @Value("${org.identifiers.cloud.ws.resolver.data.source.url}")
    private String resolverDataDumpWsEndpoint;
    @Override
    public List<PidEntry> getResolverData() throws ResolverDataSourcerException {
        return null;
    }
}
