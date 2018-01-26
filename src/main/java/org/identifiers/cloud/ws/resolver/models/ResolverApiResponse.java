package org.identifiers.cloud.ws.resolver.models;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.models
 * Timestamp: 2018-01-26 11:35
 * ---
 *
 * This may be the main Resolver API response object, let's see how it evolves over time, as the Resolver WS is being
 * build
 */
public class ResolverApiResponse {
    private String errorMsg;
    private List<ResolverApiResponseResource> resolvedResources = new ArrayList<>();

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public List<ResolverApiResponseResource> getResolvedResources() {
        return resolvedResources;
    }

    public void setResolvedResources(List<ResolverApiResponseResource> resolvedResources) {
        this.resolvedResources = resolvedResources;
    }
}