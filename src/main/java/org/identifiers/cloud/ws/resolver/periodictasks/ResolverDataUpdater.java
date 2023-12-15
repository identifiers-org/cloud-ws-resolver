package org.identifiers.cloud.ws.resolver.periodictasks;

import org.identifiers.cloud.ws.resolver.periodictasks.models.ResolverDataSourcer;
import org.identifiers.cloud.ws.resolver.periodictasks.models.ResolverDataSourcerException;
import org.identifiers.cloud.ws.resolver.data.models.Namespace;
import org.identifiers.cloud.ws.resolver.data.repositories.NamespaceRespository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.time.Duration;
import java.util.List;
import java.util.Random;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.org.cloud.ws.resolver.daemons
 * Timestamp: 2018-01-16 16:34
 * ---
 */
@Component
public class ResolverDataUpdater implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(ResolverDataUpdater.class);
    private final Random random = new Random(System.currentTimeMillis());

    private boolean wasLastRunSuccessfull = true;
    @Value("${org.identifiers.cloud.ws.resolver.data.wait_upon_success}")
    private Duration waitTimeLimit;
    @Value("${org.identifiers.cloud.ws.resolver.data.wait_upon_fail}")
    private Duration waitTimeUponErrorLimit;

    private final ResolverDataSourcer resolverDataSourcer;
    private final NamespaceRespository namespaceRespository;
    public ResolverDataUpdater(ResolverDataSourcer resolverDataSourcer,
                               NamespaceRespository namespaceRespository) {
        this.resolverDataSourcer = resolverDataSourcer;
        this.namespaceRespository = namespaceRespository;
    }

    @Override
    public void run() {
        logger.info("---> Updating namespaces from registry ---");
        try {
            List<Namespace> namespaces = resolverDataSourcer.getResolverData();
            if (!namespaces.isEmpty()) {
                // Update data backend
                logger.info("Resolver data update, #{} Namespaces", namespaces.size());
                namespaceRespository.saveAll(namespaces);
                wasLastRunSuccessfull = true;
            } else {
                logger.warn("EMPTY resolver data update!");
                wasLastRunSuccessfull = false;
            }
        } catch (ResolverDataSourcerException e) {
            logger.error("Failed to obtained resolver data update because '{}'", e.getMessage());
            wasLastRunSuccessfull = false;
        }
    }

    public long getNextWait() {
        long maxWait = wasLastRunSuccessfull ?
                waitTimeLimit.getSeconds() :
                waitTimeUponErrorLimit.getSeconds();
        long waitTime = random.nextLong(maxWait);
        logger.info("Waiting {}s before we check again for the resolver data", waitTime);
        return waitTime;
    }
}
