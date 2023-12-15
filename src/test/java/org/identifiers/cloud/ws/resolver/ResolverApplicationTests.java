package org.identifiers.cloud.ws.resolver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest (classes = {TestRedisServer.class})
class ResolverApplicationTests {
	@Autowired
	ResolverApplication application;

	@Test
	void contextLoads() {
		assertNotNull(application);
	}

}
