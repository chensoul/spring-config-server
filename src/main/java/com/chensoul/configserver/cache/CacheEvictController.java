package com.chensoul.configserver.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile({"cached"})
public class CacheEvictController {
    private static final Logger LOG = LoggerFactory.getLogger(CacheEvictController.class);
    private final CachedEnvironmentRepository repository;

    public CacheEvictController(CachedEnvironmentRepository repository) {
        this.repository = repository;
    }

    @PostMapping({"/cache/evict"})
    public void evict() {
        LOG.info("Evicting Config Server cache");
        this.repository.clearCache();
    }
}