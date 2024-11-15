package org.identifiers.cloud.ws.resolver.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.identifiers.cloud.ws.resolver.data.models.Namespace;
import org.identifiers.cloud.ws.resolver.data.repositories.NamespaceRespository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "org.identifiers.cloud.ws.resolver.parser.version", havingValue = "1")
public class CompactIdParsingHelperV1 implements CompactIdParsingHelper {

    private final NamespaceRespository namespaceRespository;

    @Override
    public ParsedCompactIdentifier parseCompactIdRequest(String rawCompactIdentifier) {
        ParsedCompactIdentifier parsedCompactIdentifier = new ParsedCompactIdentifier().setRawRequest(rawCompactIdentifier);
        // Check if we got just a namespace
        Namespace namespace = namespaceRespository.findByPrefix(rawCompactIdentifier.toLowerCase());
        if (namespace != null) {
            // It's just a namespace, the whole thing
            parsedCompactIdentifier.setNamespace(namespace.getPrefix());
            parsedCompactIdentifier.setRenderDeprecatedLanding(namespace.isRenderDeprecatedLanding());
            return parsedCompactIdentifier;
        }
        // Look for the first '/'
        if (rawCompactIdentifier.contains("/")) {
            // Possible provider code
            String[] splitBySlash = rawCompactIdentifier.split("/");
            if (splitBySlash[0].contains(":")) {
                // ':' is NOT ALLOWED for namespaces, so the whole thing is a compact identifier
                parsedCompactIdentifier.setNamespace(rawCompactIdentifier.split(":")[0].toLowerCase());
                // Check if namespace is a special case where the LUI has the namespace embedded, before prefix and LUI can actually be taken apart
                if (isNamespaceEmbeddedInLui(parsedCompactIdentifier.getNamespace())) {
                    parsedCompactIdentifier.setNamespaceEmbeddedInLui(true);
                    parsedCompactIdentifier.setLocalId(rawCompactIdentifier);
                } else {
                    parsedCompactIdentifier.setLocalId(rawCompactIdentifier.substring(rawCompactIdentifier.indexOf(":") + 1));
                }
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
                            parsedCompactIdentifier.setNamespace(possibleNamespace);
                            // check if the namespace is a special case where the LUI has it embedded, before prefix and LUI can actually be taken apart
                            if (isNamespaceEmbeddedInLui(possibleNamespace)) {
                                parsedCompactIdentifier.setNamespaceEmbeddedInLui(true);
                                parsedCompactIdentifier.setLocalId(rightSide);
                            } else {
                                parsedCompactIdentifier.setLocalId(rightSide.substring(rightSide.indexOf(":") + 1));
                            }
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
                    if (isNamespaceEmbeddedInLui(parsedCompactIdentifier.getNamespace())) {
                        parsedCompactIdentifier.setNamespaceEmbeddedInLui(true);
                    }
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
                // Another place to check whether it belongs to the special case where the LUI has the namespace embedded
                if (isNamespaceEmbeddedInLui(parsedCompactIdentifier.getNamespace())) {
                    parsedCompactIdentifier.setNamespaceEmbeddedInLui(true);
                    parsedCompactIdentifier.setLocalId(rawCompactIdentifier);
                } else {
                    parsedCompactIdentifier.setLocalId(rawCompactIdentifier.substring(rawCompactIdentifier.indexOf(":") + 1));
                }
            }
        }
        if (parsedCompactIdentifier.getNamespace() != null) {
            log.info(String.format("Collecting deprecation information on namespace '%s'", parsedCompactIdentifier.getNamespace()));
            Namespace foundNamespace = namespaceRespository.findByPrefix(parsedCompactIdentifier.getNamespace());
            if ((foundNamespace != null) && (foundNamespace.isDeprecated())) {
                parsedCompactIdentifier.setDeprecatedNamespace(true);
                parsedCompactIdentifier.setNamespaceDeprecationDate(foundNamespace.getDeprecationDate());
                parsedCompactIdentifier.setRenderDeprecatedLanding(foundNamespace.isRenderDeprecatedLanding());
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            log.info(String.format("Parsed Compact Identifier '%s'", objectMapper.writeValueAsString(parsedCompactIdentifier)));
        } catch (JsonProcessingException e) {
            // Let's do nothing
            log.error(String.format("Exception when serializing parsed compact identifier for RAW compact identifier '%s'", rawCompactIdentifier));
        }
        return parsedCompactIdentifier;
    }

    private boolean isNamespaceEmbeddedInLui(String namespace) {
        Namespace namespaceRecord = namespaceRespository.findByPrefix(namespace);
        if (namespaceRecord != null) {
            if (namespaceRecord.isNamespaceEmbeddedInLui())
                log.info(String.format("Namespace '%s' has LUIs with embedded namespace prefix", namespace));
            else
                log.info(String.format("Namespace '%s' DOES NOT HAVE LUIs with embedded namespace prefix", namespace));
            return namespaceRecord.isNamespaceEmbeddedInLui();
        }
        log.info(String.format("Namespace '%s' NOT FOUND", namespace));
        return false;
    }
}
