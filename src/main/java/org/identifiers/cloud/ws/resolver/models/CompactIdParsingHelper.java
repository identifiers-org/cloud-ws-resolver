package org.identifiers.cloud.ws.resolver.models;

import lombok.extern.slf4j.Slf4j;
import org.identifiers.cloud.ws.resolver.data.models.Namespace;
import org.identifiers.cloud.ws.resolver.data.repositories.NamespaceRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.models
 * Timestamp: 2019-05-16 13:18
 *
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * ---
 *
 * This code helps parse Compact Identifiers
 */
@Component
@Slf4j
public class CompactIdParsingHelper {

    @Autowired
    private NamespaceRespository namespaceRespository;

    // Helpers
    private boolean isNamespaceEmbeddedInLui(String namespace) {
        Namespace namespaceRecord = namespaceRespository.findByPrefix(namespace);
        if (namespaceRecord != null) {
            return namespaceRecord.isNamespaceEmbeddedInLui();
        }
        return false;
    }

    // END - Helpers

    public ParsedCompactIdentifier parseCompactIdRequest(String rawCompactIdentifier) {
        ParsedCompactIdentifier parsedCompactIdentifier = new ParsedCompactIdentifier().setRawRequest(rawCompactIdentifier);
        // Look for the first '/'
        if (rawCompactIdentifier.contains("/")) {
            // Possible provider code
            String[] splitBySlash = rawCompactIdentifier.split("/");
            if (splitBySlash[0].contains(":")) {
                // ':' is NOT ALLOWED for namespaces, so the whole thing is a compact identifier
                parsedCompactIdentifier.setNamespace(rawCompactIdentifier.split(":")[0]);
                // TODO Check if namespace is a special case where the LUI has the namespace embedded, before prefix and LUI can actually be taken apart
                parsedCompactIdentifier.setLocalId(rawCompactIdentifier.substring(rawCompactIdentifier.indexOf(":") + 1));
            } else {
                // We have a provider code (possibly)
                String possibleProviderCode = splitBySlash[0].toLowerCase();
                String rightSide = rawCompactIdentifier.substring(rawCompactIdentifier.indexOf("/") + 1);
                if (namespaceRespository.findByPrefix(possibleProviderCode) == null) {
                    // It is a provider code
                    parsedCompactIdentifier.setProviderCode(possibleProviderCode);
                    // Test whether we have a local ID or a compact identifier on the right side
                    if (rightSide.contains(":")) {
                        // Check if it is a namespace
                        String possibleNamespace = rightSide.split(":")[0].toLowerCase();
                        if (namespaceRespository.findByPrefix(possibleNamespace) != null) {
                            // TODO check if the namespace is a special case where the LUI has it embedded, before prefix and LUI can actually be taken apart
                            parsedCompactIdentifier.setNamespace(possibleNamespace).setLocalId(rightSide.substring(rightSide.indexOf(":") + 1));
                        } else {
                            // ERROR We have a provider code but the namespace does not exist
                            log.error(String.format("For RAW compact identifier parsing '%s', " +
                                    "we have provider code '%s' " +
                                    "but the namespace '%s' " +
                                    "DOES NOT EXIST, so it could be considered a local ID, which makes no sense in this context",
                                    rawCompactIdentifier,
                                    possibleProviderCode,
                                    possibleNamespace));
                        }
                    } else {
                        // ERROR We have a provider code but no information on a namespace for what it looks like a local ID
                        log.error(String.format("For RAW compact identifier parsing '%s', " +
                                        "we have provider code '%s' " +
                                        "but no information regarding namespace, for what it looks like a local ID",
                                rawCompactIdentifier,
                                possibleProviderCode));
                    }
                } else {
                    // It's a namespace
                    parsedCompactIdentifier.setNamespace(possibleProviderCode);
                    // WHEN USING NAMESPACE ON THE LEFT SIDE, A LOCAL IDENTIFIER IS REQUIRED ON THE RIGHT SIDE
                    // AND THIS IS THE ONLY DAMN use case that doesn't require special processing for those LUIs that
                    // have the namespace embedded, and are allowed to omit that when hitting the resolver
                    parsedCompactIdentifier.setLocalId(rightSide);
                }
            }
        } else {
            // If there is no '/', it means there is no provider code, just a compact identifier
            if (rawCompactIdentifier.contains(":")) {
                parsedCompactIdentifier.setNamespace(rawCompactIdentifier.split(":")[0].toLowerCase());
                // TODO Another place to check whether it belongs to the special case where the LUI has the namespace embedded
                parsedCompactIdentifier.setLocalId(rawCompactIdentifier.substring(rawCompactIdentifier.indexOf(":") + 1));
            }
        }
        return parsedCompactIdentifier;
    }
}
