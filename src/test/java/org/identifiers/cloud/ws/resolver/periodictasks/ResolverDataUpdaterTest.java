package org.identifiers.cloud.ws.resolver.periodictasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.identifiers.cloud.ws.resolver.TestRedisServer;
import org.identifiers.cloud.ws.resolver.periodictasks.models.HqServiceResponseGetResolverDataset;
import org.identifiers.cloud.ws.resolver.periodictasks.models.ResolverDatasetPayload;
import org.identifiers.cloud.ws.resolver.periodictasks.models.ServiceResponse;
import org.identifiers.cloud.ws.resolver.data.models.Namespace;
import org.identifiers.cloud.ws.resolver.data.models.Resource;
import org.identifiers.cloud.ws.resolver.data.repositories.NamespaceRespository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@Slf4j
@SpringBootTest(classes = TestRedisServer.class)
class ResolverDataUpdaterTest {

    @Autowired
    NamespaceRespository namespaceRespository;
    @Autowired
    ResolverDataUpdater updater;
    @Autowired
    RestTemplate restTemplate;

    @Value("${org.identifiers.cloud.ws.resolver.data.source.url}")
    private String resolverDataDumpWsEndpoint;

    final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setupRegistryServer() throws JsonProcessingException {
        List<Namespace> mockedNamespaces = Collections.singletonList(
                new Namespace().setPrefix("mock_prefix").setPattern("\\d+").setName("Mocked Prefix")
                        .setResources(Collections.singletonList(new Resource()
                                .setName("Mocked Resource").setSampleId("123")
                                .setUrlPattern("https://google.com?q={$id}"))));

        ServiceResponse<ResolverDatasetPayload> response = new HqServiceResponseGetResolverDataset()
                .setApiVersion("1.0")
                .setPayload(new ResolverDatasetPayload().setNamespaces(mockedNamespaces));
        String responseStr = mapper.writeValueAsString(response);

        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        server.expect(once(), requestTo(resolverDataDumpWsEndpoint))
                .andRespond(withStatus(HttpStatus.OK).body(responseStr).contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void updateNamespace() {
        updater.run();
        assertEquals(1, namespaceRespository.count());
        assertNotNull(namespaceRespository.findByPrefix("mock_prefix"));
        assertEquals(1, namespaceRespository.findByPrefix("mock_prefix").getResources().size());
    }
}