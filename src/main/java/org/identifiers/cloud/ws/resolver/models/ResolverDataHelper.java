package org.identifiers.cloud.ws.resolver.models;

import org.identifiers.cloud.libapi.models.resourcerecommender.ResourceRecommendation;
import org.identifiers.cloud.ws.resolver.data.models.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.models
 * Timestamp: 2018-05-16 15:39
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 * <p>
 * Helper for working with resolution data
 */
@Component
@Scope("prototype")
public class ResolverDataHelper {
    private static final Logger logger = LoggerFactory.getLogger(ResolverDataHelper.class);

    // Services
    @Autowired
    private ResourceRecommenderStrategy resourceRecommender;
    @Autowired
    private ResolverDataFetcher resolverDataFetcher;
    // Parsing Helpers
    @Autowired
    private CompactIdParsingHelper compactIdParsingHelper;

    // Helpers
    /**
     * Given a URL with the placeholder "{$id}", and a local ID, it will returned the resolved form of the URL
     *
     * @param url     the URL pattern
     * @param localId the ID to embed in the URL
     * @return the resolved URL
     */
    private static String resolveUrlForLocalId(String url, String localId) {
        return url.replace("{$id}", localId);
    }

    /**
     * This helper will translate from Resource data model to Resolved Resource data model, with two missing pieces:
     * Recommendation scoring and Resolved URL, so that needs to be added later by the calling client
     *
     * @param resource the source Resource data model
     * @return the translated ResolvedResource data model
     */
    private static ResolvedResource getResolvedResourceFrom(Resource resource) {
        return new ResolvedResource()
                .setId(resource.getId())
                .setProviderCode(resource.getProviderCode())
                .setDescription(resource.getDescription())
                .setInstitution(resource.getInstitution())
                .setLocation(resource.getLocation())
                .setOfficial(resource.isOfficial())
                .setResourceHomeUrl(resource.getResourceHomeUrl())
                .setDeprecatedResource(resource.isDeprecated())
                .setResourceDeprecationDate(resource.getDeprecationDate())
                .setMirId(resource.getMirId())
                .setAuthHelpDescription(resource.getAuthHelpDescription())
                .setAuthHelpUrl(resource.getAuthHelpUrl())
                .setProtectedUrls(resource.isProtectedUrls())
                .setRenderProtectedLanding(resource.isRenderProtectedLanding());
    }

    private Map<String, ResourceRecommendation> getRecommendationsByResourceId(List<ResolvedResource> resolvedResources) {
        try {
            return resourceRecommender
                    .getRecommendations(resolvedResources)
                    .parallelStream()
                    .collect(Collectors.toMap(ResourceRecommendation::getId,
                            recommendedResource -> recommendedResource,
                            (oldValue, newValue) -> oldValue));
        } catch (ResourceRecommenderStrategyException e) {
            logger.error("The following ERROR occurred while trying to get recommendations for the given resources," +
                    " ERROR '{}'", e.getMessage());
        }
        return new HashMap<>();
    }
    // END - Helpers

    // TODO - Maybe, refactor out the logic for resolving a resource given an ID, i.e. the URL substring substitution
    // This code may be refactored out later on
    public List<ResolvedResource> resolveResourcesForCompactId(ParsedCompactIdentifier parsedCompactIdentifier,
                                                               List<Resource> resources) {
        // Resolve the URLs
        List<ResolvedResource> resolvedResources =
                resources.parallelStream()
                        .map(resource -> getResolvedResourceFrom(resource)
                                .setDeprecatedNamespace(parsedCompactIdentifier.isDeprecatedNamespace())
                                .setNamespaceDeprecationDate(parsedCompactIdentifier.getNamespaceDeprecationDate())
                                .setRenderDeprecatedLanding(parsedCompactIdentifier.isRenderDeprecatedLanding() | resource.isRenderProtectedLanding())
                                .setNamespacePrefix(parsedCompactIdentifier.getNamespace())
                                .setCompactIdentifierResolvedUrl(resolveUrlForLocalId(resource.getUrlPattern(),
                                        (parsedCompactIdentifier.isNamespaceEmbeddedInLui() ? trimOutEmbeddedNamespacePrefixFromLui(parsedCompactIdentifier.getLocalId()) : parsedCompactIdentifier.getLocalId())))
                                .setRecommendation(new Recommendation())).collect(Collectors.toList());
        // Get their recommendation scoring information
        Map<String, ResourceRecommendation> recommendationById = getRecommendationsByResourceId(resolvedResources);
        resolvedResources.parallelStream().forEach(resolvedResource -> {
            if (recommendationById.containsKey(Long.toString(resolvedResource.getId()))) {
                resolvedResource.getRecommendation()
                        .setRecommendationExplanation(recommendationById.get(Long.toString(resolvedResource.getId())).getRecommendationExplanation())
                        .setRecommendationIndex(recommendationById.get(Long.toString(resolvedResource.getId())).getRecommendationIndex());
            }

        });
        return resolvedResources;
    }

    public List<ResolvedResource> resolveAllResourcesWithTheirSampleId() {
        List<ResolvedResource> resolvedResources = new ArrayList<>();
        // TODO Review this in the future to find out whether it's the best way of doing this or not
        resolverDataFetcher.findAllNamespaces().forEach(namespace -> resolvedResources.addAll(namespace
                .getResources().parallelStream().map(resource -> getResolvedResourceFrom(resource)
                        .setDeprecatedNamespace(namespace.isDeprecated())
                        .setNamespaceDeprecationDate(namespace.getDeprecationDate())
                        .setNamespacePrefix(namespace.getPrefix())
                        .setCompactIdentifierResolvedUrl(resolveUrlForLocalId(resource.getUrlPattern(),
                                resource.getSampleId()))
                        .setRecommendation(new Recommendation())).toList()));
        return resolvedResources;
    }

    // TODO - This could potentially be removed in the future?
    public List<ResolvedResource> getAllResolvedResourcesHomes() {
        return resolveAllResourcesWithTheirSampleId();
    }

    private String trimOutEmbeddedNamespacePrefixFromLui(String lui) {
        if (lui.contains(":")) {
            return lui.substring(lui.indexOf(":") + 1);
        }
        return "";
    }
}
