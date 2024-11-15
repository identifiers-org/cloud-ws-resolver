package org.identifiers.cloud.ws.resolver;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import jakarta.annotation.PreDestroy;

@TestConfiguration
@SuppressWarnings("resource")
public class TestRedisServer {
    static GenericContainer<?> redis;

    static {
        DockerImageName img = DockerImageName.parse("redis");
        redis = new GenericContainer<>(img)
                .withExposedPorts(6379)
                .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1));
        redis.start();
        System.setProperty("spring.redis.host", redis.getHost());
        System.setProperty("spring.redis.port", redis.getMappedPort(6379).toString());
    }

    @PreDestroy
    void stopServer() {
        redis.stop();
    }
}
