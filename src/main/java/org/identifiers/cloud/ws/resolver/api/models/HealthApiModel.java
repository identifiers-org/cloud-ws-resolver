package org.identifiers.cloud.ws.resolver.api.models;

import org.identifiers.cloud.ws.resolver.api.controllers.HealthApiController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.models
 * Timestamp: 2018-05-15 13:34
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * Health Check API Controller model.
 */
@Component
@Deprecated
@ConditionalOnBean(HealthApiController.class)
public class HealthApiModel {
    private static final String runningSessionId = UUID.randomUUID().toString();

    public String livenessCheck() {
        return runningSessionId;
    }

    public String readinessCheck() {
        return runningSessionId;
    }

}
