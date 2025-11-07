package com.er.zoo.config;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration class for caching in the Zoo API using Caffeine.
 * <p>
 * Features:
 *  <ul>
 *       <li>Time-based eviction: caches expire after a fixed duration.</li>
 *       <li>Maximum size: prevents excessive memory usage by limiting cache entries.</li>
 *       <li>Separate caches for Animals, Rooms, and AnimalsInRoom lists.</li>
 *  </ul>
 *  </p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(5))
                .maximumSize(10_000);
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager manager = new CaffeineCacheManager("animals", "rooms", "animalsInRoom");
        manager.setCaffeine(caffeine);
        return manager;
    }
}

