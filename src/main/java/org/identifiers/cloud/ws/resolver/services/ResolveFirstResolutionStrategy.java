package org.identifiers.cloud.ws.resolver.services;

import org.identifiers.cloud.ws.resolver.models.ParsedCompactIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.services
 * Timestamp: 2019-06-26 15:40
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * This resolution service implements a "resolve first" strategy for resolving a compact identifier request, which means
 * it will go throw a list of resolution services and, the first one able to resolve the request, is the one used for
 * sending back the results.
 */
@Component
@Qualifier("ResolveFirstResolutionStrategy")
@Primary
public class ResolveFirstResolutionStrategy implements ResolutionService {
    @Autowired
    private MultiResolverBuilder builder;

    @Override
    public ResolutionServiceResult resolve(ParsedCompactIdentifier parsedCompactIdentifier) {
        List<String> errors = new ArrayList<>();
        for (ResolutionService resolutionService : builder.getResolverChain()) {
            ResolutionServiceResult resolutionServiceResult =
                    resolutionService.resolve(parsedCompactIdentifier);
            if (resolutionServiceResult.isResolved()) {
                return resolutionServiceResult;
            }
            errors.add(resolutionServiceResult.getErrorMessage());
        }
        // None of them could resolve the request
        return new ResolutionServiceResult().setErrorMessage(String.join("; ", errors));
    }
}
