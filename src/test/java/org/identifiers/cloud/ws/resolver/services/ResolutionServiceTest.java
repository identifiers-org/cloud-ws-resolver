package org.identifiers.cloud.ws.resolver.services;

import org.identifiers.cloud.ws.resolver.TestRedisServer;
import org.identifiers.cloud.ws.resolver.data.models.Location;
import org.identifiers.cloud.ws.resolver.data.models.Namespace;
import org.identifiers.cloud.ws.resolver.data.models.Resource;
import org.identifiers.cloud.ws.resolver.data.repositories.NamespaceRespository;
import org.identifiers.cloud.ws.resolver.models.CompactIdParsingHelper;
import org.identifiers.cloud.ws.resolver.models.ParsedCompactIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(classes = {TestRedisServer.class})
class ResolutionServiceTest {
    @Autowired ResolutionService resolutionService;
    @Autowired CompactIdParsingHelper helper;

    @MockBean
    NamespaceRespository namespaceRespository;



    @BeforeEach
    public void setupNamespaceRepository() {
        Namespace namespace = new Namespace()
                .setPrefix("existing_namespace")
                .setPattern("\\d+").setDeprecated(false);
        Resource resource = new Resource()
                .setId(1).setName("valid resource").setDeprecated(false)
                .setUrlPattern("https://mockedurl.com?q={$id}")
                .setLocation(new Location().setCountryCode("GB").setCountryName("UK"));
        namespace.setResources(Collections.singletonList(resource));

        //Order is important here
        doReturn(null).when(namespaceRespository).findByPrefix(anyString());
        doReturn(namespace).when(namespaceRespository).findByPrefix("existing_namespace");
    }

    @Test
    void testResolveExistingNamespace() {
        ParsedCompactIdentifier pci = helper.parseCompactIdRequest("existing_namespace");
        ResolutionServiceResult result = resolutionService.resolve(pci);
        assertTrue(result.isResolved());
        assertEquals(1, result.getResolvedResources().size());
        assertNull(result.getResolvedResources().get(0).getNamespacePrefix());
    }

    @Test
    void testResolveNonExistingNamespace() {
        ParsedCompactIdentifier pci = helper.parseCompactIdRequest("non_namespace");
        ResolutionServiceResult result = resolutionService.resolve(pci);
        assertFalse(result.isResolved());
        assertEquals(0, result.getResolvedResources().size());
    }

    @Test
    void testResolveValidCID() {
        ParsedCompactIdentifier pci = helper.parseCompactIdRequest("existing_namespace:123");
        ResolutionServiceResult result = resolutionService.resolve(pci);
        assertTrue(result.isResolved());
        assertEquals(1, result.getResolvedResources().size());
        String resolvedURL = result.getResolvedResources().get(0).getCompactIdentifierResolvedUrl();
        assertTrue(resolvedURL.startsWith("https://mockedurl.com"));
        assertFalse(resolvedURL.contains("{$id}"));
    }

    @Test
    void testResolveNonValidCID() {
        ParsedCompactIdentifier pci = helper.parseCompactIdRequest("existing_namespace:abc");
        ResolutionServiceResult result = resolutionService.resolve(pci);
        assertFalse(result.isResolved());
        assertTrue(result.getResolvedResources().isEmpty());
    }
}