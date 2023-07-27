package com.fanduel.josh.cache;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class RedisHeartbeat {

    private boolean isAlive = false;

    private final RedisConnectionFactory connectionFactory;

    @PostConstruct
    protected void init() {
        pingRedis();
    }

    @Scheduled(fixedRate = 5000)
    public void pingRedis() {
        RedisConnection redisConnection = null;
        try {
            redisConnection = connectionFactory.getConnection();
            isAlive = "PONG".equals(redisConnection.ping());
        } catch (RedisConnectionFailureException e) {
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
