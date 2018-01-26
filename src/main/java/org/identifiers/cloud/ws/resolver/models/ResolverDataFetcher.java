package org.identifiers.cloud.ws.resolver.models;

import org.identifiers.cloud.ws.resolver.data.models.PidEntry;
import org.identifiers.cloud.ws.resolver.data.models.ResourceEntry;
import org.identifiers.cloud.ws.resolver.data.repositories.PidEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.models
 * Timestamp: 2018-01-26 10:40
 * ---
 */
public class ResolverDataFetcher {
    private static Logger logger = LoggerFactory.getLogger(ResolverDataFetcher.class);

    @Autowired
    private PidEntryRepository pidEntryRepository;

    public List<ResourceEntry> findResourcesByPrefix(String prefix) {
        List<ResourceEntry> result = new ArrayList<>();
        List<PidEntry> pidEntries = pidEntryRepository.findByPrefix(prefix);
        if (!pidEntries.isEmpty()) {
            if (pidEntries.size() > 1) {
                logger.error("MULTIPLE PID entries for prefix '{}'", prefix);
            }
            result = pidEntries.parallelStream().flatMap(pidEntry -> Arrays.stream(pidEntry.getResources())).collect(Collectors
                    .toList());
        } else {
            logger.warn("NO PID entry for prefix '{}'", prefix);
        }
        return result;
    }
}
