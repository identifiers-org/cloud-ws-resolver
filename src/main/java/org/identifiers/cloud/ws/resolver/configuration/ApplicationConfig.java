package org.identifiers.cloud.ws.resolver.configuration;

import lombok.extern.slf4j.Slf4j;
import org.identifiers.cloud.libapi.services.ApiServicesFactory;
import org.identifiers.cloud.ws.resolver.daemons.models.ResolverDataSourcer;
import org.identifiers.cloud.ws.resolver.daemons.models.ResolverDataSourcerFromWs;
import org.identifiers.cloud.ws.resolver.models.ResolverDataFetcher;
import org.identifiers.cloud.ws.resolver.models.ResolverDataFetcherFromDataBackend;
import org.identifiers.cloud.ws.resolver.services.ResolutionService;
import org.identifiers.cloud.ws.resolver.services.ResolveFirstResolutionStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.web.client.RestTemplate;
import org.identifiers.cloud.libapi.services.ResourceRecommenderService;

/**
 * @author Manuel Bernal Llinares <mbdebian@gmail.com>
 * Project: resolver
 * Package: org.identifiers.org.cloud.ws.resolver.configuration
 * Timestamp: 2018-01-17 16:25
 * ---
 */
@Configuration
@EnableRedisRepositories
@Slf4j
public class ApplicationConfig {
    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.host}")
    private String redisHost;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info(String.format("Using Redis on host '%s' port '%d'", redisHost, redisPort));
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost,
                redisPort);
        return new JedisConnectionFactory(redisStandaloneConfiguration);    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate() {
        RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }

    // TODO - I should probably use @Component for this
    @Bean
    public ResolverDataSourcer resolverDataSourcer() {
        return new ResolverDataSourcerFromWs();
    }

    // Resolver Data Fetcher
    @Bean
    public ResolverDataFetcher resolverDataFetcher() {
        return new ResolverDataFetcherFromDataBackend();
    }

    @Bean
    public ResolutionService resolutionService() {
        log.info("[CONFIG] Resolution Strategy - Resolve First");
        return new ResolveFirstResolutionStrategy();
    }

    @Bean
    public RestTemplate getRestTempalte() {
        return new RestTemplate();
    }

    @Bean
    public ResourceRecommenderService getResourceRecommenderService(
            @Value("${org.identifiers.cloud.ws.resolver.service.recommender.host}")
            String resourceRecommenderServiceHost,
            @Value("${org.identifiers.cloud.ws.resolver.service.recommender.port}")
            String resourceRecommenderServicePort
    ) {
        return ApiServicesFactory.getResourceRecommenderService
                (resourceRecommenderServiceHost, resourceRecommenderServicePort);
    }
}
