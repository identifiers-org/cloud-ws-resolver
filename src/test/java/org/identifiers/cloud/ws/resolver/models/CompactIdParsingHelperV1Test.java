package org.identifiers.cloud.ws.resolver.models;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class CompactIdParsingHelperV1Test {
    final CompactIdParsingHelperV1 helper;
    final NamespaceRespository namespaceRespository;
    public CompactIdParsingHelperV1Test() {
        namespaceRespository = mock(NamespaceRespository.class);
        helper = new CompactIdParsingHelperV1(namespaceRespository);
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
    }

    private static Stream<Arguments> CompactIdParsingHelperTestParameters() {
        return Stream.of(
            ////// Tests with existing namespace
            Arguments.of("Namespace only",
                    "existing_namespace", null, "existing_namespace", null),
            Arguments.of("Namespace + lui",
                    "existing_namespace:123", null, "existing_namespace", "123"),
            Arguments.of("Namespace + invalid lui",
                    "existing_namespace:abc", null, "existing_namespace", "abc"),
            Arguments.of("Namespace + lui using slash",
                    "existing_namespace/123", null, "existing_namespace", "123"),
            Arguments.of("Namespace + lui + provider",
                    "provider/existing_namespace:123", "provider", "existing_namespace", "123"),
            //The parsed result below should be the same as the previous
            Arguments.of("Namespace + lui + provider using slash",
                    "provider/existing_namespace/123", "provider", null, null),

            ////// Tests with non existing namespace
            // The helper is inconsistent when the namespace doesn't exist
            Arguments.of("Non existing namespace",
                    "non_existing_namespace", null, null, null),
            Arguments.of("Non existing namespace + lui",
                    "non_existing_namespace:123", null, "non_existing_namespace", "123"),
            Arguments.of("Non existing namespace + lui using slash",
                    "non_existing_namespace/123", "non_existing_namespace", null, null),
            Arguments.of("Non existing namespace + lui + provider",
                    "provider/non_existing_namespace:123", "provider", null, null),
            Arguments.of("Non existing namespace + lui + provider using slash",
                    "provider/non_existing_namespace/123", "provider", null, null),

            ////// Tests with embedded lui namespace
            Arguments.of("Embedded lui namespace + lui ",
                    "lui_namespace:123", null, "lui_namespace", "lui_namespace:123"),
            Arguments.of("Embedded lui namespace + lui + provider",
                    "provider/lui_namespace:123", "provider", "lui_namespace", "lui_namespace:123"),
            // The below is inconsistent
            Arguments.of("Embedded lui namespace + lui + provider using slash",
                    "provider/lui_namespace/123", "provider", null, null)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("CompactIdParsingHelperTestParameters")
    void testRawRequest(String msg,
                        String expectedRawRequest, String expectedProviderCode,
                        String expectedNamespace, String expectedLocalId) {
        ParsedCompactIdentifier pci = helper.parseCompactIdRequest(expectedRawRequest);
        assertEquals(expectedRawRequest, pci.getRawRequest(), msg);
        assertEquals(expectedProviderCode, pci.getProviderCode(), msg);
        assertEquals(expectedNamespace, pci.getNamespace(), msg);
        assertEquals(expectedLocalId, pci.getLocalId(), msg);
    }
}