package org.identifiers.cloud.ws.resolver.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.identifiers.cloud.ws.resolver.data.models.Institution;
import org.identifiers.cloud.ws.resolver.data.models.Location;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: libapi
 * Package: org.identifiers.cloud.ws.resolver.models.api.responses
 * Timestamp: 2018-03-07 7:28
 * ---
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class ResolvedResource implements Serializable {
    // identifiers.org internal ID for this resource
    private long id;
    // Resource MIR ID
    private String mirId;
    // Provider code for this resource
    private String providerCode;
    // This is the resolved URL for the given compact identifier
    private String compactIdentifierResolvedUrl;
    // A description of this resource
    private String description;
    // The institution this resource belongs to
    private Institution institution;
    // Location information for this resource
    private Location location;
    // Whether this resource is official or not
    private boolean official;
    // Home URL for this resource within the context of the namespace where it's registered
    private String resourceHomeUrl;
    // Recommendation scoring information
    private Recommendation recommendation;
    // Deprecation information
    private String namespacePrefix;
    private boolean deprecatedNamespace = false;
    private Date namespaceDeprecationDate;
    private boolean deprecatedResource = false;
    private Date resourceDeprecationDate;
    private boolean protectedUrls;
    private boolean renderProtectedLanding;
    private String authHelpUrl;
    private String authHelpDescription;
}
