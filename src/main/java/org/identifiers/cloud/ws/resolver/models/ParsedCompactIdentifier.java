package org.identifiers.cloud.ws.resolver.models;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.models
 * Timestamp: 2019-05-16 13:20
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 * <p>
 * This models the elements presented in a Compact ID request
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class ParsedCompactIdentifier implements Serializable {
    private String providerCode;
    // In fact, the 'prefix' value of a namespace
    private String namespace;
    private String localId;
    private String rawRequest;
    // This will flag the clients on whether this compact identifier has the namespace embedded in LUI or not.
    private boolean namespaceEmbeddedInLui = false;
    // Deprecation Information
    private boolean deprecatedNamespace = false;
    private boolean renderDeprecatedLanding = false;
    private Date namespaceDeprecationDate;
}
