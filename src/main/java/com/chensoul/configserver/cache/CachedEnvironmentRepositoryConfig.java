package com.chensoul.configserver.cache;

import io.micrometer.observation.ObservationRegistry;
import java.util.Arrays;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentProperties;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepository;
import org.springframework.cloud.config.server.environment.MultipleJGitEnvironmentRepositoryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableCaching
@Profile({"cached"})
public class CachedEnvironmentRepositoryConfig {
    @Bean
    CachedEnvironmentRepository cachedEnvironmentRepository(MultipleJGitEnvironmentRepositoryFactory gitEnvironmentRepositoryFactory, MultipleJGitEnvironmentProperties environmentProperties, ObservationRegistry observationRegistry) throws Exception {
        MultipleJGitEnvironmentRepository environmentRepository = gitEnvironmentRepositoryFactory.build(environmentProperties);
        return new CachedEnvironmentRepository(Arrays.asList(environmentRepository), observationRegistry, true);
    }

    @Bean
    CacheManager inMemoryCacheManager() {
        return new ConcurrentMapCacheManager();
    }
}