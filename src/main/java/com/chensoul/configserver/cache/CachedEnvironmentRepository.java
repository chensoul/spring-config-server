package com.chensoul.configserver.cache;

import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.environment.CompositeEnvironmentRepository;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;

public class CachedEnvironmentRepository extends CompositeEnvironmentRepository {
    private static final Logger LOG = LoggerFactory.getLogger(CachedEnvironmentRepository.class);

    public CachedEnvironmentRepository(List<EnvironmentRepository> environmentRepositories, ObservationRegistry observationRegistry, boolean failOnError) {
        super(environmentRepositories, observationRegistry, failOnError);
    }

    public Environment findOne(String application, String profile, String label) {
        return this.findOne(application, profile, label, false);
    }

    @Cacheable(cacheNames = {"environment"}, key = "{#root.targetClass, #application, #profile, #label, #includeOrigin}")
    public Environment findOne(String application, String profile, String label, boolean includeOrigin) {
        LOG.info("Fetching environment from Config Server");
        return super.findOne(application, profile, label, includeOrigin);
    }

    @CacheEvict(cacheNames = {"environment"}, allEntries = true)
    public void clearCache() {
        LOG.info("Clearing Config Server cache");
    }
}
