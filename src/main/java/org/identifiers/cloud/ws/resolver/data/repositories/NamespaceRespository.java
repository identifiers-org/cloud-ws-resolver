package org.identifiers.cloud.ws.resolver.data.repositories;

import org.identifiers.cloud.ws.resolver.data.models.Namespace;
import org.springframework.data.repository.CrudRepository;

/**
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.data.repositories
 * Timestamp: 2019-04-02 11:33
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
public interface NamespaceRespository extends CrudRepository<Namespace, String> {

    Namespace findByPrefix(String prefix);

    Namespace findByMirId(String mirId);

    Namespace findByResourcesMirId(String mirId);
}
