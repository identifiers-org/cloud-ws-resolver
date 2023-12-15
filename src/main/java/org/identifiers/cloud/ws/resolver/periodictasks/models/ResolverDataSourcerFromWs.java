package org.identifiers.cloud.ws.resolver.periodictasks.models;

import org.identifiers.cloud.ws.resolver.data.models.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.daemons.models
 * Timestamp: 2018-01-29 10:16
 * ---
 */
@Component
@EnableRetry
public class ResolverDataSourcerFromWs implements ResolverDataSourcer {
    public static final int WS_REQUEST_RETRY_MAX_ATTEMPTS = 12;
    public static final int WS_REQUEST_RETRY_BACK_OFF_PERIOD = 1500; // 1.5 seconds

    private static final Logger logger = LoggerFactory.getLogger(ResolverDataSourcerFromWs.class);

    // Re-try pattern, externalize this later if needed
    // TODO refactor this to do it in a more modern way
    private static final RetryTemplate retryTemplate;

    @Autowired
    RestTemplate restTemplate;

    static {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(WS_REQUEST_RETRY_MAX_ATTEMPTS);

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(WS_REQUEST_RETRY_BACK_OFF_PERIOD);

        retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);
    }

    @Value("${org.identifiers.cloud.ws.resolver.data.source.url}")
    private String resolverDataDumpWsEndpoint;

    @Override
    public List<Namespace> getResolverData() throws ResolverDataSourcerException {
        List<Namespace> result = new ArrayList<>();
        logger.info("Try to get Resolver data dump from '{}'", resolverDataDumpWsEndpoint);
        // Run it with multiple tries
        try {
            result = retryTemplate.execute(retryContext -> restTemplate
                    .getForObject(resolverDataDumpWsEndpoint, HqServiceResponseGetResolverDataset.class)
                    .getPayload().getNamespaces()
            );
        } catch (RuntimeException e) {
            // NOTE - Yes, according to best practices, I should not be catching such a top level exception, but here it
            // makes sense
            logger.error("COULD NOT RETRIVE Resolver Data Dump from '{}' because '{}'",
                    resolverDataDumpWsEndpoint,
                    e.getMessage());
        }
        return result;
    }
}
