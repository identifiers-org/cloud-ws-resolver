package org.identifiers.cloud.ws.resolver.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.identifiers.cloud.ws.resolver.data.repositories.NamespaceRespository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Project: resolver
 * Package: org.identifiers.cloud.ws.resolver.models
 * Timestamp: 2024-11-01
 *
 * @author Renato Caminha Juacaba Neto <rjuacaba@ebi.ac.uk>
 * ---
 *
 * This is a rewrite of the first version of the parser helper that is a bit inconsistent
 * when namespaces don't exist or have the embedded in lui flag. This parser also allows
 * local ids to contain slashes and colons.
 */
@Slf4j
@Primary
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "org.identifiers.cloud.ws.resolver.parser.version", havingValue = "2", matchIfMissing = true)
public class CompactIdParsingHelperV2 implements CompactIdParsingHelper {

    private final NamespaceRespository namespaceRespository;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows(JsonProcessingException.class)
    public ParsedCompactIdentifier parseCompactIdRequest(String rawCompactIdentifier) {
        ParsedCompactIdentifier parsedCompactIdentifier = new ParsedCompactIdentifier().setRawRequest(rawCompactIdentifier);

        int firstColonIdx = rawCompactIdentifier.indexOf(':');
        int firstSlashIdx = rawCompactIdentifier.indexOf('/');

        if (firstColonIdx == -1 && firstSlashIdx == -1) {
            // No colons or slashes, no local ID or provider ID, can only be a prefix
            if (prefixExistsLowercase(rawCompactIdentifier)) {
                parsedCompactIdentifier.setNamespace(rawCompactIdentifier);
            }
        }
        else if (firstSlashIdx == -1) {
            // Only colons, no provider id, so it can only be namespace:id
            takeNamespaceAndLocalIdIfValid(rawCompactIdentifier,
                    firstColonIdx, parsedCompactIdentifier);
        }
        else if (firstColonIdx == -1) {
            // Only slashes, could be provider/namespace/id or namespace/id, id may include slashes
            parseSlashOnlyRequest(rawCompactIdentifier,
                    firstSlashIdx, parsedCompactIdentifier);
        } else {
            // Request has both colons and slashes, so any CURIE scheme is valid
            String providerCode = null;
            var remainingRequest = rawCompactIdentifier;
            var possibleNamespace = rawCompactIdentifier.substring(0, firstSlashIdx);
            if (firstSlashIdx < firstColonIdx && !prefixExistsLowercase(possibleNamespace)) {
                // Since the colon cannot be part of the provider code and prefix,
                // the request is very likely to be of type provider/namespace...
                //   But it's also possible that it is namespace/id with the id containing a colon
                providerCode = rawCompactIdentifier.substring(0, firstSlashIdx);
                remainingRequest = rawCompactIdentifier.substring(firstSlashIdx+1);
            }
            findValidNamespaceAndLocalId(remainingRequest, parsedCompactIdentifier);
            if (parsedCompactIdentifier.getNamespace() != null) {
                parsedCompactIdentifier.setProviderCode(providerCode);
            }
        }

        log.info("Parsed Compact Identifier {}", objectMapper.writeValueAsString(parsedCompactIdentifier));
        return parsedCompactIdentifier;
    }


    private boolean prefixExistsLowercase(String prefix) {
        return namespaceRespository.existsByPrefix(prefix.toLowerCase());
    }

    private void parseSlashOnlyRequest(String rawCompactIdentifier, int firstSlashIdx,
                                       ParsedCompactIdentifier parsedCompactIdentifier) {
        var secondSlashIdx = rawCompactIdentifier.indexOf('/', firstSlashIdx +1);
        if (secondSlashIdx != -1) { // There might be a provider id
            var firstSection = rawCompactIdentifier.substring(0, firstSlashIdx);
            var secondSection = rawCompactIdentifier.substring(firstSlashIdx +1, secondSlashIdx);
            if (prefixExistsLowercase(firstSection)) {
                takeNamespaceAndLocalId(rawCompactIdentifier,
                        firstSlashIdx, parsedCompactIdentifier);
            } else if (prefixExistsLowercase(secondSection)) {
                parsedCompactIdentifier.setProviderCode(firstSection);
                var remaining = rawCompactIdentifier.substring(firstSlashIdx+1);
                takeNamespaceAndLocalId(remaining, secondSlashIdx - firstSlashIdx -1, parsedCompactIdentifier);
            }
        } else { // Only one slash found, so it can only be namespace/id
            takeNamespaceAndLocalIdIfValid(rawCompactIdentifier,
                    firstSlashIdx, parsedCompactIdentifier);
        }
    }

    private void findValidNamespaceAndLocalId(String source, ParsedCompactIdentifier parsedCompactIdentifier) {
        int colonIdx = source.indexOf(':');
        int slashIdx = source.indexOf('/');

        if (colonIdx == -1 && slashIdx == -1)
            log.error("COULD NOT FIND expected local ID separator. Source: {}", source);
        else if (colonIdx == -1) // if no colon, use slash as local id separator
            takeNamespaceAndLocalIdIfValid(source, slashIdx, parsedCompactIdentifier);
        else if (slashIdx == -1) // if no slash, use colon as local id separator
            takeNamespaceAndLocalIdIfValid(source, colonIdx, parsedCompactIdentifier);
        else { // Both separators found, gotta check repository for matching prefixes
            var possibleNamespaceWithColon = source.substring(0, colonIdx);
            if (prefixExistsLowercase(possibleNamespaceWithColon)) {
                takeNamespaceAndLocalId(source, colonIdx, parsedCompactIdentifier);
            } else {
                var possibleNamespaceWithSlash = source.substring(0, slashIdx);
                if (prefixExistsLowercase(possibleNamespaceWithSlash)) {
                    takeNamespaceAndLocalId(source, slashIdx, parsedCompactIdentifier);
                }
            }
        }
    }

    private void takeNamespaceAndLocalIdIfValid(String source, int separatorIdx,
                                                ParsedCompactIdentifier parsedCompactIdentifier) {
        var possibleNamespace = source.substring(0, separatorIdx);
        if (prefixExistsLowercase(possibleNamespace)) {
            takeNamespaceAndLocalId(source, separatorIdx, parsedCompactIdentifier);
        } else {
            log.debug("{} IS NOT A NAMESPACE. Could not parse namespace and local id. Source: {}", possibleNamespace, source);
        }
    }

    private void takeNamespaceAndLocalId(String source, int separatorIdx,
                                         ParsedCompactIdentifier parsedCompactIdentifier) {
        var prefix = source.substring(0, separatorIdx);

        var namespace = namespaceRespository.findByPrefix(prefix.toLowerCase());
        parsedCompactIdentifier.setNamespace(namespace.getPrefix());
        parsedCompactIdentifier.setNamespaceEmbeddedInLui(namespace.isNamespaceEmbeddedInLui());
        parsedCompactIdentifier.setRenderDeprecatedLanding(namespace.isRenderDeprecatedLanding());
        if (namespace.isNamespaceEmbeddedInLui()) {
            log.debug("Namespace '{}' has LUIs with embedded namespace prefix", prefix);
            parsedCompactIdentifier.setLocalId(source);
        } else {
            log.debug("Namespace '{}' DOES NOT HAVE LUIs with embedded namespace prefix", prefix);
            var localId = source.substring(separatorIdx + 1);
            parsedCompactIdentifier.setLocalId(localId);
        }
        if (namespace.isDeprecated()) {
            parsedCompactIdentifier.setDeprecatedNamespace(true);
            parsedCompactIdentifier.setNamespaceDeprecationDate(namespace.getDeprecationDate());
            parsedCompactIdentifier.setRenderDeprecatedLanding(namespace.isRenderDeprecatedLanding());
        }
    }
}
