package org.identifiers.cloud.ws.resolver.models;

import lombok.extern.slf4j.Slf4j;
import org.identifiers.cloud.libapi.models.resourcerecommender.Location;
import org.identifiers.cloud.libapi.models.resourcerecommender.ResolvedResource;
import org.identifiers.cloud.libapi.models.resourcerecommender.ResourceRecommendation;
import org.identifiers.cloud.libapi.services.ApiServicesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.models
 * Timestamp: 2018-02-27 21:34
 * ---
 */
@Component
@Scope("prototype")
@Slf4j
public class ResourceRecommenderService implements ResourceRecommenderStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ResourceRecommenderService.class);

    @Value("${org.identifiers.cloud.ws.resolver.service.recommender.host}")
    private String resourceRecommenderServiceHost;
    @Value("${org.identifiers.cloud.ws.resolver.service.recommender.port}")
    private String resourceRecommenderServicePort;

    @Override
    public List<ResourceRecommendation> getRecommendations(List<org.identifiers.cloud.ws.resolver.models.ResolvedResource> resources) throws ResourceRecommenderStrategyException {
        // Whatever happens, the client library will always return a default empty answer that is valid
        return ApiServicesFactory
                .getResourceRecommenderService(resourceRecommenderServiceHost, resourceRecommenderServicePort)
                .requestRecommendations(resources.parallelStream()
                        .map(resolvedResource ->
                                new ResolvedResource()
                                        .setOfficial(resolvedResource.isOfficial())
                                        .setCompactIdentifierResolvedUrl(resolvedResource.getCompactIdentifierResolvedUrl())
                                        .setId(Long.toString(resolvedResource.getId()))
                                        .setResourceHomeUrl(resolvedResource.getResourceHomeUrl())
                                        .setDeprecatedNamespace(resolvedResource.isDeprecatedNamespace())
                                        .setNamespaceDeprecationDate(resolvedResource.getNamespaceDeprecationDate())
                                        .setDeprecatedResource(resolvedResource.isDeprecatedResource())
                                        .setResourceDeprecationDate(resolvedResource.getResourceDeprecationDate())
                                        .setMirId(resolvedResource.getMirId())
                                        .setNamespacePrefix(resolvedResource.getNamespacePrefix())
                                        .setProtectedUrls(resolvedResource.isProtectedUrls())
                                        .setAuthHelpUrl(resolvedResource.getAuthHelpUrl())
                                        .setAuthHelpDescription(resolvedResource.getAuthHelpDescription())
                                        .setRenderProtectedLanding(resolvedResource.isRenderProtectedLanding())
                                        .setLocation(
                                                new Location()
                                                        .setCountryName(resolvedResource.getLocation().getCountryName())
                                                        .setCountryCode(resolvedResource.getLocation().getCountryCode())))
                        .collect(Collectors.toList()))
                .getPayload()
                .getResourceRecommendations();

    }
}
