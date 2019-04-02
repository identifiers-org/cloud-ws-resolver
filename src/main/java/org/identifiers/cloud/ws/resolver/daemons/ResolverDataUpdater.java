package org.identifiers.cloud.ws.resolver.daemons;

import org.identifiers.cloud.ws.resolver.daemons.models.ResolverDataSourcer;
import org.identifiers.cloud.ws.resolver.daemons.models.ResolverDataSourcerException;
import org.identifiers.cloud.ws.resolver.data.models.Namespace;
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
    private Logger logger = LoggerFactory.getLogger(ResolverDataUpdater.class);

    private boolean shutdown = false;

    @Autowired
    private ResolverDataSourcer resolverDataSourcer;

    @Autowired
    private PidEntryRepository pidEntryRepository;

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
            // TODO - Do your stuff
            logger.info("---> Creating instance of Namespace ---");
            List<Namespace> pidEntries = new ArrayList<>();
            try {
                pidEntries = resolverDataSourcer.getResolverData();
            } catch (ResolverDataSourcerException e) {
                logger.error("Failed to obtained resolver data update because '{}'", e.getMessage());
            }
            if (pidEntries.size() > 0) {
                logger.info("Resolver data update, #{} PID entries", pidEntries.size());
                // Update data backend
                pidEntryRepository.saveAll(pidEntries);
            } else {
                logger.warn("EMPTY resolver data update!");
            }
            // TODO - Somewhere I need to test how to access the data I put on the backend
            // TODO - Once data access has been tested, the next step is the algorithm that solves contexts and prefixes
            // Wait for a predefined period of time before the next announcement
            try {
                long waitTime = random.nextInt(WAIT_TIME_LIMIT_SECONDS) * 1000;
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
