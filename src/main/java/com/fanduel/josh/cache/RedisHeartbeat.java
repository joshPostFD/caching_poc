package com.fanduel.josh.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class RedisHeartbeat {

    private boolean isAlive = false;

    private final RedisConnectionFactory connectionFactory;
    private final SimpleCacheManager simpleCacheManager;

    @PostConstruct
    protected void init() {
        pingRedis();
    }

    @Scheduled(fixedRate = 5000)
    public void pingRedis() {
        RedisConnection redisConnection = null;
        try {
            redisConnection = connectionFactory.getConnection();
            if (!isAlive & (isAlive = "PONG".equals(redisConnection.ping()))) {
                log.info("Redis connection obtained. Wiping in-memory cache.");
                simpleCacheManager.getCacheNames().parallelStream()
                        .forEach(
                                name ->
                                        Optional.ofNullable(simpleCacheManager.getCache(name))
                                                .ifPresent(Cache::clear));
            }
        } catch (RedisConnectionFailureException e) {
            if (isAlive) {
                log.error(
                        "Redis connection failed. Falling back to in-memory cache: {}",
                        e.getMessage());
                e.printStackTrace();
            }
            isAlive = false;
        }
        if (redisConnection != null) {
            redisConnection.close();
        }
    }

    public boolean isAlive() {
        return isAlive;
    }
}
