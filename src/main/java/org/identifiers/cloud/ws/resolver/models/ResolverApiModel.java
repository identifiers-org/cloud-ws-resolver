package org.identifiers.cloud.ws.resolver.models;

import org.identifiers.cloud.libapi.models.resourcerecommender.ResourceRecommendation;
import org.identifiers.cloud.ws.resolver.data.models.ResourceEntry;
import org.identifiers.cloud.ws.resolver.models.api.responses.Recommendation;
import org.identifiers.cloud.ws.resolver.models.api.responses.ResolvedResource;
import org.identifiers.cloud.ws.resolver.models.api.responses.ResponseResolvePayload;
import org.identifiers.cloud.ws.resolver.models.api.responses.ServiceResponseResolve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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
public class ResolverApiModel {
    private static final String apiVersion = "1.0";
    private static Logger logger = LoggerFactory.getLogger(ResolverApiModel.class);
    private static String runningSessionId = UUID.randomUUID().toString();

    @Autowired
    private ResolverDataFetcher resolverDataFetcher;

    @Autowired
    private ResourceRecommenderStrategy resourceRecommender;

    // This code may be refactored out later on
    private List<ResolvedResource> resolveResourcesForCompactId(CompactId compactId,
                                                                List<ResourceEntry> resourceEntries,
                                                                Map<String, ResourceRecommendation> recommendationById) {
        // TODO - I need to add whether the resource is official or not
        return resourceEntries
                .parallelStream()
                .map(resourceEntry -> {
                    ResolvedResource resolverApiResponseResource = new ResolvedResource();
                    resolverApiResponseResource.setId(resourceEntry.getId());
                    resolverApiResponseResource.setResourcePrefix(resourceEntry.getResourcePrefix());
                    resolverApiResponseResource.setInfo(resourceEntry.getInfo());
                    resolverApiResponseResource.setInstitution(resourceEntry.getInstitution());
                    resolverApiResponseResource.setLocation(resourceEntry.getLocation());
                    resolverApiResponseResource.setAccessUrl(resourceEntry
                            .getAccessURL().replace("{$id}", compactId.getId()));
                    resolverApiResponseResource.setOfficial(resourceEntry.isOfficial());
                    // Embed Recommendation
                    Recommendation recommendation = new Recommendation();
                    if (recommendationById.containsKey(resourceEntry.getId())) {
                        recommendation
                                .setRecommendationExplanation(recommendationById.get(resourceEntry.getId()).getRecommendationExplanation())
                                .setRecommendationIndex(recommendationById.get(resourceEntry.getId()).getRecommendationIndex());
                    }
                    resolverApiResponseResource.setRecommendation(recommendation);
                    return resolverApiResponseResource;
                }).collect(Collectors.toList());
    }

    private Map<String, ResourceRecommendation> getRecommendationsByResourceId(List<ResourceEntry> resourceEntries) {
        try {
            return resourceRecommender
                    .getRecommendations(resourceEntries)
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
    // TODO - Document this API method
    public ServiceResponseResolve resolveCompactId(String compactIdParameter) throws ResolverApiException {
        CompactId compactId = null;
        try {
            compactId = new CompactId(compactIdParameter);
        } catch (CompactIdException e) {
            throw new ResolverApiException(e.getMessage());
        }
        // Prepare default answer
        ServiceResponseResolve resolverApiResponse = new ServiceResponseResolve();
        resolverApiResponse.setPayload(new ResponseResolvePayload().setResolvedResources(new ArrayList<>()));
        // TODO - Check if prefix is null, as we may want to perform a more sofisticated search on the resolver data
        // Locate resource providers
        logger.debug("Looking up resources for compact ID '{}', prefix '{}' and ID '{}'", compactId.getOriginal(), compactId.getPrefix(), compactId.getId());
        List<ResourceEntry> resourceEntries = resolverDataFetcher.findResourcesByPrefix(compactId.getPrefix());
        logger.info("CompactId '{}', with prefix '{}' got #{} resources back from the data backend", compactId
                .getOriginal(), compactId.getPrefix(), resourceEntries.size());
        // Default behaviour for the Resolver Web Service is to return all the possible options, we may want to include
        // information regarding availability of every possible resource providing information on the given compact ID
        if (resourceEntries.isEmpty()) {
            // If no providers, produce error response
            resolverApiResponse.setErrorMessage(String.format("No providers found for Compact ID '%s'", compactId.getOriginal()));
            resolverApiResponse.setHttpStatus(HttpStatus.NOT_FOUND);
        } else {
            // Resolve the links
            resolverApiResponse.getPayload().setResolvedResources(resolveResourcesForCompactId(compactId,
                    resourceEntries,
                    getRecommendationsByResourceId(resourceEntries)));
            resolverApiResponse.setHttpStatus(HttpStatus.OK);
        }
        // NOTE - This code may be refactored later
        resolverApiResponse.setApiVersion(apiVersion);
        return resolverApiResponse;
    }

    public ServiceResponseResolve resolveCompactId(String compactIdParameter, String selector) throws ResolverApiException {
        logger.info("Resolve Compact ID '{}', with selector '{}'", compactIdParameter, selector);
        CompactId compactId = null;
        try {
            compactId = new CompactId(compactIdParameter);
        } catch (CompactIdException e) {
            throw new ResolverApiException(e.getMessage());
        }
        // Prepare default answer
        ServiceResponseResolve resolverApiResponse = new ServiceResponseResolve();
        resolverApiResponse.setPayload(new ResponseResolvePayload().setResolvedResources(new ArrayList<>()));
        // TODO - I do need to do something about prefix being 'null', otherwise it will break this first implementation
        // Locate resource providers
        // TODO - Refactor this later to make sure that 'selector' is always used lower case
        if ((compactId.getPrefix() == null) || (compactId.getPrefix().equals(selector.toLowerCase()))) {
            // This is normal resolution
            // TODO - Don't worry intrepid optimizer, this model will get refactored later on
            return resolveCompactId(CompactId.getCompactIdString(selector, compactId.getId()));
        } else {
            // We need to locate resources for the given compact ID and filter by the given selector
            // So... it turns out that I don't need anything more complex as a decider right now
            logger.debug("Looking up resources for compact ID '{}', selector '{}' and ID '{}'",
                    compactId.getOriginal(),
                    selector);
            List<ResourceEntry> resourceEntries = resolverDataFetcher.findResourcesByPrefix(compactId.getPrefix())
                    .stream()
                    .filter(resourceEntry -> {
                        if (resourceEntry.getResourcePrefix() != null) {
                            return resourceEntry.getResourcePrefix().equals(selector.toLowerCase());
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
            logger.info("CompactId '{}', with selector '{}' got #{} resources back from the data backend",
                    compactId.getOriginal(),
                    selector,
                    resourceEntries.size());
            if (resourceEntries.isEmpty()) {
                // If no providers, produce error response
                resolverApiResponse.setErrorMessage(String.format("No providers found for Compact ID '%s', selector '%s'",
                        compactId.getOriginal(),
                        selector));
                resolverApiResponse.setHttpStatus(HttpStatus.NOT_FOUND);
            } else {
                // Resolve the links
                resolverApiResponse.getPayload().setResolvedResources(resolveResourcesForCompactId(compactId,
                        resourceEntries,
                        getRecommendationsByResourceId(resourceEntries)));
                resolverApiResponse.setHttpStatus(HttpStatus.OK);
            }
        }
        resolverApiResponse.setApiVersion(apiVersion);
        return resolverApiResponse;
    }

    public String livenessCheck() {
        return runningSessionId;
    }

    public String readinessCheck() {
        return runningSessionId;
    }
}
