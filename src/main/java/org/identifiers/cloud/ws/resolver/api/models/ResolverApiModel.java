package org.identifiers.cloud.ws.resolver.api.models;

import lombok.extern.slf4j.Slf4j;
import org.identifiers.cloud.ws.resolver.api.ApiCentral;
import org.identifiers.cloud.ws.resolver.api.responses.ResponseResolvePayload;
import org.identifiers.cloud.ws.resolver.api.responses.ServiceResponseResolve;
import org.identifiers.cloud.ws.resolver.data.models.Namespace;
import org.identifiers.cloud.ws.resolver.data.repositories.NamespaceRespository;
import org.identifiers.cloud.ws.resolver.models.*;
import org.identifiers.cloud.ws.resolver.services.ResolutionService;
import org.identifiers.cloud.ws.resolver.services.ResolutionServiceResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.math.NumberUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.models
 * Timestamp: 2018-01-26 10:31
 * ---
 */
// Well, when using Spring Boot, you have to fit in a particular way of doing things. Is it the right way?
@Component
@Scope("prototype")
@Slf4j
public class ResolverApiModel {
    // External Helpers
    @Autowired
    private CompactIdParsingHelper compactIdParsingHelper;

    @Autowired
    private ResolutionService resolutionService;

    @Autowired
    private NamespaceRespository namespaceRespository;

    @Value("${org.identifiers.cloud.ws.resolver.mirid.resolution.url_format}")
    private String miridResolutionFormat;

    // Helpers
    private ServiceResponseResolve createDefaultResponse() {
        return (ServiceResponseResolve)
                new ServiceResponseResolve()
                .setApiVersion(ApiCentral.apiVersion)
                .setPayload(new ResponseResolvePayload().setResolvedResources(new ArrayList<>()));
    }

    @Deprecated
    private CompactId getCompactIdentifier(String compactId, ServiceResponseResolve response) {
        try {
            return new CompactId(compactId);
        } catch (CompactIdException e) {
            response.setErrorMessage(e.getMessage());
            response.setHttpStatus(HttpStatus.BAD_REQUEST);
        }
        return null;
    }
    // END - Helpers

    // --- Resolution API ---
    public ServiceResponseResolve resolveRawCompactId(String rawCompactId) {
        // This is the only entry method right now
        ServiceResponseResolve response = createDefaultResponse();
        ParsedCompactIdentifier parsedCompactIdentifier = compactIdParsingHelper.parseCompactIdRequest(rawCompactId);
        ResolutionServiceResult resolutionServiceResult = resolutionService.resolve(parsedCompactIdentifier);
        if (resolutionServiceResult.isResolved()) {
            if (resolutionServiceResult.getResolvedResources().isEmpty()) {
                // Resolved but there are no resources
                response.setErrorMessage(resolutionServiceResult.getErrorMessage());
                response.setHttpStatus(HttpStatus.NOT_FOUND);
            } else {
                // Resolved with resources
                response.getPayload()
                        .setResolvedResources(resolutionServiceResult.getResolvedResources())
                        .setParsedCompactIdentifier(parsedCompactIdentifier);
            }
            // Return response from resolver
            return response;
        }
        // Default error response
        response.setHttpStatus(HttpStatus.BAD_REQUEST);
        response.setErrorMessage(resolutionServiceResult.getErrorMessage());
        return response;
    }

    public URI resolveMirId(String mirId) throws URISyntaxException {
        log.debug("MIRID resolution request for {}", mirId);
        if (NumberUtils.isCreatable(mirId)) {
            mirId = String.format("MIR:%08d", Integer.valueOf(mirId));
        }

        Namespace namespace = namespaceRespository.findByMirId(mirId);
        if (namespace != null) {
            log.debug("Found namespace {} for MIRID {}", namespace.getPrefix(), mirId);
        } else {
            namespace = namespaceRespository.findByResourcesMirId(mirId);
            if (namespace != null) {
                log.debug("Found namespace {} for MIRID {} via resource", namespace.getPrefix(), mirId);
            }
        }
        if (namespace != null) {
            return new URI(String.format(miridResolutionFormat, namespace.getPrefix()));
        }
        return null;
    }
}
