package org.identifiers.cloud.ws.resolver.services;

import lombok.extern.slf4j.Slf4j;
import org.identifiers.cloud.ws.resolver.data.models.Namespace;
import org.identifiers.cloud.ws.resolver.data.models.Resource;
import org.identifiers.cloud.ws.resolver.models.ParsedCompactIdentifier;
import org.identifiers.cloud.ws.resolver.models.ResolverDataFetcher;
import org.identifiers.cloud.ws.resolver.models.ResolverDataHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.services
 * Timestamp: 2019-06-26 15:57
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 */
@Slf4j
@Component
@Qualifier("CompactIdentifierResolutionService")
public class CompactIdentifierResolutionService implements ResolutionService {
    private final ResolverDataFetcher resolverDataFetcher;
    private final ResolverDataHelper resolverDataHelper;
    public CompactIdentifierResolutionService(ResolverDataFetcher resolverDataFetcher, ResolverDataHelper resolverDataHelper) {
        this.resolverDataFetcher = resolverDataFetcher;
        this.resolverDataHelper = resolverDataHelper;
    }

    static class CompactIdentifierVerification {
        String errorMessage;
        boolean valid = true;
    }

    // Helpers
    private CompactIdentifierVerification verifyCompactIdentifier(String namespace, String localId) {
        CompactIdentifierVerification verification = new CompactIdentifierVerification();
        Namespace registryNamespace = resolverDataFetcher.findNamespaceByPrefix(namespace);
        if (registryNamespace == null) {
            String errorMessage = String.format("UNKNOWN namespace '%s' when verifying local ID '%s'", namespace,
                    localId);
            log.error(errorMessage);
            verification.errorMessage = errorMessage;
            verification.valid = false;
        } else {
            // Verify regular expression
            if (!localId.matches(registryNamespace.getPattern())) {
                String errorMessage = String.format("For namespace '%s', provided local ID '%s' DOES NOT MATCH local IDs definition pattern '%s'", namespace, localId, registryNamespace.getPattern());
                log.error(errorMessage);
                verification.errorMessage = errorMessage;
                verification.valid = false;
            }
        }
        return verification;
    }
    // END - Helpers


    @Override
    public ResolutionServiceResult resolve(ParsedCompactIdentifier parsedCompactIdentifier) {
        // Base result
        ResolutionServiceResult resolutionServiceResult = new ResolutionServiceResult().setResolved(true);
        // This if we need to look into the registry
        if ((parsedCompactIdentifier.getLocalId() != null && (parsedCompactIdentifier.getNamespace() != null))) {
            // Verify compact identifier
            CompactIdentifierVerification ciVerification = verifyCompactIdentifier(parsedCompactIdentifier.getNamespace(), parsedCompactIdentifier.getLocalId());
            if (!ciVerification.valid) {
                resolutionServiceResult.setErrorMessage(ciVerification.errorMessage);
                resolutionServiceResult.setResolved(false);
                return resolutionServiceResult;
            }
            // Get the resources
            List<Resource> resources = resolverDataFetcher.findResourcesByPrefix(parsedCompactIdentifier.getNamespace());
            log.info(String.format("CompactId '%s', with prefix '%s' got #%d resources back from the data backend",
                    parsedCompactIdentifier.getRawRequest(), parsedCompactIdentifier.getNamespace(), resources.size()));
            // Have we found any resources?
            if (resources.isEmpty()) {
                // If no providers, produce error response
                resolutionServiceResult.setErrorMessage(String.format("No providers found for Compact ID '%s'", parsedCompactIdentifier.getRawRequest()));
                // We have, indeed, resolved the compact identifier to an empty provider space
                return resolutionServiceResult;
            }
            // Check for provider
            if (parsedCompactIdentifier.getProviderCode() != null) {
                // Look for the one with the given provider code
                List<Resource> filteredResources = resources.stream()
                        .filter(resource -> {
                            if (resource.getProviderCode() != null) {
                                return resource.getProviderCode().equals(parsedCompactIdentifier.getProviderCode());
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
                log.info(String.format("CompactId '%s', with prefix '%s' got #%d resources with provider code '%s'",
                        parsedCompactIdentifier.getRawRequest(),
                        parsedCompactIdentifier.getNamespace(),
                        filteredResources.size(),
                        parsedCompactIdentifier.getProviderCode()));
                if (filteredResources.isEmpty()) {
                    String errorMessage = String.format("For Compact ID '%s', prefix '%s', " +
                                    "NO RESOURCES FOUND with PROVIDER CODE '%s'",
                            parsedCompactIdentifier.getRawRequest(),
                            parsedCompactIdentifier.getNamespace(),
                            parsedCompactIdentifier.getProviderCode());
                    resolutionServiceResult.setErrorMessage(errorMessage);
                    log.error(errorMessage);
                    return resolutionServiceResult;
                }
                // Resolve the selected resources
                resolutionServiceResult.setResolvedResources(resolverDataHelper.resolveResourcesForCompactId(parsedCompactIdentifier, filteredResources));
                return resolutionServiceResult;
            }
            // Resolve all the resources
            resolutionServiceResult.setResolvedResources(resolverDataHelper.resolveResourcesForCompactId(parsedCompactIdentifier, resources));
            return resolutionServiceResult;
        }
        String errorMessage = String.format("INVALID Compact Identifier resolution request for '%s'", parsedCompactIdentifier.getRawRequest());
        resolutionServiceResult.setErrorMessage(errorMessage);
        resolutionServiceResult.setResolved(false);
        return resolutionServiceResult;
    }
}
