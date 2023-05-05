package org.identifiers.cloud.ws.resolver.data.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.Date;

/**
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.data.models
 * Timestamp: 2019-04-02 11:29
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class Resource implements Serializable {
    // identifiers.org internal ID for this resource
    private long id;
    @Indexed
    private String mirId;
    private String urlPattern;
    // This is known as 'info' in the old data model from the EBI platform
    private String name;
    // Now you can optionally provide a description
    private String description;
    private boolean official;
    // We should require every provider to have a code
    private String providerCode;
    // This is a sample ID at provider level
    private String sampleId;
    // This is a home URL for this resource within the context of the namespace it belongs to
    private String resourceHomeUrl;
    private Institution institution;
    private Location location;
    // Deprecation / deactivation
    private boolean deprecated;
    private Date deprecationDate;
    private boolean protectedUrls;
    private boolean renderProtectedLanding;
    private String authHelpUrl;
    private String authHelpDescription;
}