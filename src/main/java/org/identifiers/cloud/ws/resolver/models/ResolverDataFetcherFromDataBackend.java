package org.identifiers.cloud.ws.resolver.models;

import lombok.extern.slf4j.Slf4j;
import org.identifiers.cloud.ws.resolver.data.models.Namespace;
import org.identifiers.cloud.ws.resolver.data.models.Resource;
import org.identifiers.cloud.ws.resolver.data.repositories.NamespaceRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.models
 * Timestamp: 2018-01-27 9:02
 * ---
 */
@Component
@Slf4j
public class ResolverDataFetcherFromDataBackend implements ResolverDataFetcher {

    @Autowired
    private NamespaceRespository namespaceRespository;

    @Override
    public List<Resource> findResourcesByPrefix(String prefix) {
        log.info(String.format("Find resources by prefix for '%s'", prefix));
        if (prefix != null) {
            Namespace namespace = namespaceRespository.findByPrefix(prefix);
            if (namespace != null) {
                return namespace.getResources();
            } else {
                log.warn("NO PID entry for prefix '{}'", prefix);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public Namespace findNamespaceByPrefix(String prefix) {
        log.info(String.format("Searching for namespace '%s'", prefix));
        return namespaceRespository.findByPrefix(prefix);
    }

    @Override
    public Iterable<Namespace> findAllNamespaces() {
        return namespaceRespository.findAll();
    }
}
