package org.identifiers.cloud.ws.resolver.periodictasks.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.identifiers.cloud.ws.resolver.data.models.Namespace;

import java.io.Serializable;
import java.util.List;

/**
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.daemons.models
 * Timestamp: 2019-04-02 13:32
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * This data model represents the payload that comes embedded in the service response from the HQ Registry API Service
 * when queried about the resolver dataset.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResolverDatasetPayload implements Serializable {
    private List<Namespace> namespaces;
}
