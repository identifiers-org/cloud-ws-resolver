package org.identifiers.cloud.ws.resolver.daemons;

import org.identifiers.cloud.ws.resolver.daemons.models.ResolverDataSourcer;
import org.identifiers.cloud.ws.resolver.daemons.models.ResolverDataSourcerException;
import org.identifiers.cloud.ws.resolver.data.models.Namespace;
import org.identifiers.cloud.ws.resolver.data.repositories.NamespaceRespository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
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
public class ResolverDataUpdater extends Thread {
    private static final int WAIT_TIME_LIMIT_SECONDS = 1800;
    private static final int WAIT_TIME_UPON_ERROR_LIMIT_SECONDS = 300;
    private Logger logger = LoggerFactory.getLogger(ResolverDataUpdater.class);

    private boolean shutdown = false;

    @Autowired
    private ResolverDataSourcer resolverDataSourcer;

    @Autowired
    private NamespaceRespository namespaceRespository;

    public synchronized boolean isShutdown() {
        return shutdown;
    }

    public synchronized void setShutdown() {
        this.shutdown = true;
    }

    @Override
    public void run() {
        logger.info("--- Resolver Data Update Daemon Start ---");
        Random random = new Random(System.currentTimeMillis());
        while (!isShutdown()) {
            logger.info("---> Creating instance of Namespace ---");
            List<Namespace> namespaces = new ArrayList<>();
            int nextTimeLimitSeconds = WAIT_TIME_LIMIT_SECONDS;
            try {
                namespaces = resolverDataSourcer.getResolverData();
            } catch (ResolverDataSourcerException e) {
                logger.error("Failed to obtained resolver data update because '{}'", e.getMessage());
                nextTimeLimitSeconds = WAIT_TIME_UPON_ERROR_LIMIT_SECONDS;
            }
            if (namespaces.size() > 0) {
                logger.info("Resolver data update, #{} Namespaces", namespaces.size());
                // Update data backend
                namespaceRespository.saveAll(namespaces);
            } else {
                logger.warn("EMPTY resolver data update!");
                nextTimeLimitSeconds = WAIT_TIME_UPON_ERROR_LIMIT_SECONDS;
            }
            // TODO - Somewhere I need to test how to access the data I put on the backend
            // TODO - Once data access has been tested, the next step is the algorithm that solves contexts and prefixes
            // Wait for a predefined period of time before the next announcement
            try {
                long waitTime = random.nextInt(nextTimeLimitSeconds) * 1000;
                logger.info("Waiting {}s before we check again for the resolver data", waitTime / 1000);
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                logger.warn("The Resolver Data Update Daemon has been interrupted while waiting for " +
                        "another iteration. Stopping the service, no more updates will be submitted");
                shutdown = true;
            }
        }
    }

    @PostConstruct
    public void autoStartThread() {
        start();
    }

    @PreDestroy
    public void stopDaemon() {
        logger.info("--- [STOPPING] Resolver Data Update Daemon ---");
        setShutdown();
    }
}
