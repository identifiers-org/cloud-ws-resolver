package org.identifiers.cloud.ws.resolver.models;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.identifiers.cloud.ws.resolver.data.models.Namespace;
import org.identifiers.cloud.ws.resolver.data.repositories.NamespaceRespository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CompactIdParsingHelperV2Test {
    final CompactIdParsingHelper helper;
    final NamespaceRespository namespaceRespository;
    public CompactIdParsingHelperV2Test() {
        namespaceRespository = mock(NamespaceRespository.class);
        helper = new CompactIdParsingHelperV2(namespaceRespository, new ObjectMapper());
    }

    @BeforeEach
    public void setupNamespaceRepository() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.WARN);

        Namespace namespace = new Namespace()
                .setPrefix("existing_namespace")
                .setPattern("\\d+").setDeprecated(false)
                .setNamespaceEmbeddedInLui(false);
        Namespace luinamespace = new Namespace()
                .setPrefix("lui_namespace")
                .setPattern("\\d+").setDeprecated(false)
                .setNamespaceEmbeddedInLui(true);
        namespace.setResources(Collections.emptyList());

        //Order is important here
        doReturn(null).when(namespaceRespository).findByPrefix(anyString());
        doReturn(namespace).when(namespaceRespository).findByPrefix("existing_namespace");
        doReturn(luinamespace).when(namespaceRespository).findByPrefix("lui_namespace");

        doReturn(false).when(namespaceRespository).existsByPrefix(anyString());
        doReturn(true).when(namespaceRespository).existsByPrefix("existing_namespace");
        doReturn(true).when(namespaceRespository).existsByPrefix("lui_namespace");
    }

    private static Stream<Arguments> CompactIdParsingHelperTestParameters() {
        return Stream.of(
            ////// Parsing namespace without embedded prefixes flag
            Arguments.of("existing_namespace", null, "existing_namespace", null),
            Arguments.of("existing_namespace:123", null, "existing_namespace", "123"),
            Arguments.of("existing_namespace:abc", null, "existing_namespace", "abc"),
            Arguments.of("existing_namespace/123", null, "existing_namespace", "123"),
            Arguments.of("provider/existing_namespace:123", "provider", "existing_namespace", "123"),
            Arguments.of("provider/existing_namespace/123", "provider", "existing_namespace", "123"),
            Arguments.of("existing_namespace/123:123", null, "existing_namespace", "123:123"),
            Arguments.of("existing_namespace:123/123", null, "existing_namespace", "123/123"),
            Arguments.of("provider/existing_namespace/123:123", "provider", "existing_namespace", "123:123"),
            Arguments.of("provider/existing_namespace:123/123", "provider", "existing_namespace", "123/123"),

            ////// Parsing non-existent namespace
            Arguments.of("non_existing_namespace", null, null, null),
            Arguments.of("non_existing_namespace:123/123", null, null, null),
            Arguments.of("non_existing_namespace/123:123", null, null, null),
            Arguments.of("provider/non_existing_namespace:123", null, null, null),
            Arguments.of("provider/non_existing_namespace:123/123", null, null, null),
            Arguments.of("provider/non_existing_namespace/123", null, null, null),
            Arguments.of("provider/non_existing_namespace/123:123", null, null, null),


            ////// Parsing namespace with embedded prefixes flag
            Arguments.of("lui_namespace:123", null, "lui_namespace", "lui_namespace:123"),
            Arguments.of("provider/lui_namespace:123", "provider", "lui_namespace", "lui_namespace:123"),
            Arguments.of("provider/lui_namespace/123", "provider", "lui_namespace", "lui_namespace/123"),
            Arguments.of("provider/lui_namespace:123/123", "provider", "lui_namespace", "lui_namespace:123/123"),
            Arguments.of("provider/lui_namespace/123:123", "provider", "lui_namespace", "lui_namespace/123:123")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("CompactIdParsingHelperTestParameters")
    void testRawRequestHelper(String rawRequest, String expectedProviderCode,
                              String expectedNamespace, String expectedLocalId) {
        ParsedCompactIdentifier pci = helper.parseCompactIdRequest(rawRequest);
        assertEquals(rawRequest, pci.getRawRequest());
        assertEquals(expectedProviderCode, pci.getProviderCode());
        assertEquals(expectedNamespace, pci.getNamespace());
        assertEquals(expectedLocalId, pci.getLocalId());

        // Checks for limited calls to repository
        verify(namespaceRespository, atMostOnce()).findByPrefix(anyString());
    }
}