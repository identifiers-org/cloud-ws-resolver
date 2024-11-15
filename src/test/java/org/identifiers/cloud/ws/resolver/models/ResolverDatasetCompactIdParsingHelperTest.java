package org.identifiers.cloud.ws.resolver.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.identifiers.cloud.ws.resolver.TestRedisServer;
import org.identifiers.cloud.ws.resolver.data.models.Namespace;
import org.identifiers.cloud.ws.resolver.data.repositories.NamespaceRespository;
import org.identifiers.cloud.ws.resolver.periodictasks.ResolverDataUpdater;
import org.identifiers.cloud.ws.resolver.periodictasks.models.HqServiceResponseGetResolverDataset;
import org.identifiers.cloud.ws.resolver.periodictasks.models.ResolverDataSourcer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.util.ResourceUtils;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import org.testcontainers.shaded.org.hamcrest.text.IsEqualIgnoringCase;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@SpringBootTest(properties = {
        "org.identifiers.cloud.ws.resolver.parser.version=2",
        "logging.level.org.identifiers.cloud.ws.resolver.models=WARN"
})
@Import(TestRedisServer.class)
class ResolverDatasetCompactIdParsingHelperTest {
    @Autowired
    CompactIdParsingHelper helper;

    @TestConfiguration
    public static class ResolverDataSourcerMockConfig {
        static List<Namespace> namespaces;

        @Bean @Primary
        public ResolverDataSourcer mockedDatasourcer() throws IOException {
            var sourcer = mock(ResolverDataSourcer.class);

            getTestNamespaces();
            doReturn(namespaces).when(sourcer).getResolverData();

            return sourcer;
        }

        public static List<Namespace> getTestNamespaces() throws IOException {
            if (namespaces == null) {
                var namespaceFile = ResourceUtils.getFile("classpath:testResolutionDataset.json");
                var objectMapper = new ObjectMapper();
                objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

                namespaces = objectMapper.readValue(namespaceFile, HqServiceResponseGetResolverDataset.class).getPayload().getNamespaces();
            }
            return namespaces;
        }
    }

    public static Stream<Arguments> getTestArguments() throws IOException {
        // These have been disabled because they don't behave as other namespaces
        final Set<String> problematicNamespaces = Set.of(
                "mmmp:biomaps"// Prefix string shouldn't contain colons
        );

        var namespaces = ResolverDataSourcerMockConfig.getTestNamespaces();
        var prefixes = namespaces.stream().map(Namespace::getPrefix).collect(Collectors.toSet());
        Stream.Builder<Arguments> argumentsBuilder = Stream.builder();
        for (var namespace : namespaces) {
            var prefix = namespace.getPrefix();
            if (problematicNamespaces.contains(prefix.toLowerCase())) continue;

            var sampleId = namespace.getSampleId();
            for (var resource : namespace.getResources()) {
                var providerCode = resource.getProviderCode();
                var rSampleId = sampleId;
                if (StringUtils.isNotBlank(resource.getSampleId()) &&
                        !StringUtils.equals(sampleId, resource.getSampleId())) {
                    rSampleId = resource.getSampleId();
                }
                if (StringUtils.isNotBlank(providerCode) &&
                        !StringUtils.equals(providerCode, "CURATOR_REVIEW") &&
                        !StringUtils.equalsIgnoreCase(providerCode, prefix) &&
                        prefixes.stream().noneMatch(providerCode::equalsIgnoreCase)) {

                    var rawRequest = String.format("%s/%s:%s", providerCode, prefix, rSampleId);
                    argumentsBuilder.add(Arguments.of(rawRequest, providerCode, prefix,
                            namespace.isNamespaceEmbeddedInLui() ? prefix + ":" + rSampleId : rSampleId));
                    rawRequest = String.format("%s/%s:%s", providerCode, prefix.toUpperCase(), rSampleId);
                    argumentsBuilder.add(Arguments.of(rawRequest, providerCode, prefix.toUpperCase(),
                            namespace.isNamespaceEmbeddedInLui() ? prefix.toUpperCase() + ":" + rSampleId : rSampleId));

                    rawRequest = String.format("%s/%s/%s", providerCode, prefix, rSampleId);
                    argumentsBuilder.add(Arguments.of(rawRequest, providerCode, prefix,
                            namespace.isNamespaceEmbeddedInLui() ? prefix + "/" + rSampleId : rSampleId));
                    rawRequest = String.format("%s/%s/%s", providerCode, prefix.toUpperCase(), rSampleId);
                    argumentsBuilder.add(Arguments.of(rawRequest, providerCode, prefix.toUpperCase(),
                            namespace.isNamespaceEmbeddedInLui() ? prefix.toUpperCase() + "/" + rSampleId : rSampleId));
                }
                var rawRequest = String.format("%s:%s", prefix, rSampleId);
                argumentsBuilder.add(Arguments.of(rawRequest, null, prefix,
                        namespace.isNamespaceEmbeddedInLui() ? prefix + ":" + rSampleId : rSampleId));
                rawRequest = String.format("%s:%s", prefix.toUpperCase(), rSampleId);
                argumentsBuilder.add(Arguments.of(rawRequest, null, prefix.toUpperCase(),
                        namespace.isNamespaceEmbeddedInLui() ? prefix.toUpperCase() + ":" + rSampleId : rSampleId));

                rawRequest = String.format("%s/%s", prefix, rSampleId);
                argumentsBuilder.add(Arguments.of(rawRequest, null, prefix,
                        namespace.isNamespaceEmbeddedInLui() ? prefix + "/" + rSampleId : rSampleId));
                rawRequest = String.format("%s/%s", prefix.toUpperCase(), rSampleId);
                argumentsBuilder.add(Arguments.of(rawRequest, null, prefix.toUpperCase(),
                        namespace.isNamespaceEmbeddedInLui() ? prefix.toUpperCase() + "/" + rSampleId : rSampleId));
            }
        }
        return argumentsBuilder.build();
    }

    @BeforeAll
    public static void resetDatabase(@Autowired ResolverDataUpdater updater,
                                     @Autowired NamespaceRespository namespaceRespository) {
        namespaceRespository.deleteAll();
        updater.run();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getTestArguments")
    void check(String rawRequest, String expectedProviderCode,
               String expectedNamespace, String expectedLocalId) {
        log.info("RawRequest: {}", rawRequest);
        ParsedCompactIdentifier pci = helper.parseCompactIdRequest(rawRequest);
        assertEquals(rawRequest, pci.getRawRequest(), "Different requests");
        assertEquals(expectedProviderCode, pci.getProviderCode(), "Different provider");
        assertThat("Different prefixes", pci.getNamespace(), equalToIgnoringCase(expectedNamespace));
        assertEquals(expectedLocalId, pci.getLocalId(), "Different local ID");
    }
}