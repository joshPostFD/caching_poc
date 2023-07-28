package com.fanduel.josh.cache;

import com.fanduel.josh.model.TestObj;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Setup JedisConnectionFactory config bean for setting redis host + port RedisTemplate bean in
 * order to use RedisConnectionFactory JedisCacheConfiguration - so we can use spring
 * boot's @Cacheable etc on our service CacheManager - brings it all together to configure the cache
 */
@Configuration
@EnableCaching
@EnableRedisRepositories("com.fanduel.josh.repository")
@RequiredArgsConstructor
@Slf4j
public class CacheConfig extends CachingConfigurerSupport {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.timeout}")
    private long connectionReadTimeout;

    @Value("${spring.redis.connect-timeout}")
    private long connectionTimeout;

    private final CacheDetailsConfig cacheDetailsConfig;
    public static final String KEY_DELIMITER = ":";

    @Bean
    @Primary
    public CacheManager cacheManager(CustomMultiCacheManager cacheManager) {
        return cacheManager;
    }

    @Bean
    @Primary
    public JedisConnectionFactory redisConnectionFactory(
            JedisClientConfiguration jedisClientConfiguration) {
        RedisStandaloneConfiguration redisStandaloneConfiguration =
                new RedisStandaloneConfiguration(redisHost, redisPort);

        return new CustomJedisConnectionFactory(
                redisStandaloneConfiguration, jedisClientConfiguration);
    }

    @Bean
    public JedisClientConfiguration redisClientConfiguration() {
        return JedisClientConfiguration.builder()
                .readTimeout(Duration.ofSeconds(connectionReadTimeout))
                .connectTimeout(Duration.ofSeconds(connectionTimeout))
                .build();
    }

    @Bean
    public ReactiveRedisConnectionFactory lettuceConnectionFactory() {

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(connectionReadTimeout))
                .shutdownTimeout(Duration.ZERO)
                .build();

        return new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(redisHost, redisPort),
                clientConfig
        );
    }

    @Bean
    public ReactiveRedisTemplate<String, TestObj> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory
    ) {
        RedisSerializationContext<String, TestObj> serializationContext = RedisSerializationContext
                .<String, TestObj>newSerializationContext(new StringRedisSerializer())
                .key(new StringRedisSerializer())
                .value(new GenericToStringSerializer<>(TestObj.class))
                .hashKey(new Jackson2JsonRedisSerializer<>(Integer.class))
                .hashValue(new GenericJackson2JsonRedisSerializer())
                .build();
        ReactiveRedisTemplate template =
                new ReactiveRedisTemplate(connectionFactory,
                        serializationContext);

        return template;
    }

    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory
    ) {
        return new ReactiveStringRedisTemplate(connectionFactory);
    }

    @Bean
    public SimpleCacheManager simpleCacheManager() {
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        List<CaffeineCache> mapCaches =
                CacheKey.values().stream()
                        .map(this::buildCache)
                        .collect(Collectors.toList());
        simpleCacheManager.setCaches(mapCaches);
        simpleCacheManager.initializeCaches();
        return simpleCacheManager;
    }

    private CaffeineCache buildCache(String name) {
        return new CaffeineCache(
                name,
                Caffeine.newBuilder()
                        .expireAfterWrite(
                                Optional.ofNullable(cacheDetailsConfig.get(name))
                                        .map(CacheDetailsConfig.CacheDetails::getTtlInSeconds)
                                        .orElse(
                                                cacheDetailsConfig
                                                        .getDefaultConfig()
                                                        .getTtlInSeconds()),
                                TimeUnit.SECONDS)
                        .ticker(Ticker.systemTicker())
                        .build());
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        Map<String, RedisCacheConfiguration> redisCacheConfigurations = new HashMap<>();

        for (String key : CacheKey.values()) {
            CacheDetailsConfig.CacheDetails cacheConfig = cacheDetailsConfig.get(key);
            if (cacheConfig == null) {
                log.info("Cache config not found for {}. Using default configuration values.", key);
                redisCacheConfigurations.put(
                        key, createCacheConfiguration(cacheDetailsConfig.getDefaultConfig()));
            } else {
                redisCacheConfigurations.put(key, createCacheConfiguration(cacheConfig));
            }
        }

        RedisCacheManager redisCacheManager =
                RedisCacheManager.builder(redisConnectionFactory)
                        .cacheDefaults(
                                createCacheConfiguration(cacheDetailsConfig.getDefaultConfig()))
                        .withInitialCacheConfigurations(redisCacheConfigurations)
                        .build();
        redisCacheManager.initializeCaches();

        return redisCacheManager;
    }

    private RedisCacheConfiguration createCacheConfiguration(
            CacheDetailsConfig.CacheDetails details) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(details.getTtlInSeconds()));
    }

    // This stops the cacheable annotation throwing an error when it can't deserialize data from the
    // cache (i.e. if the
    // object has changed with a release) and instead just goes direct to the source and then
    // recaches. This should
    // allow us to do releases without having issues with stale data in the cache from the previous
    // version.
    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Error getting {} from cache {}.", key, cache.getName(), exception);
            }
        };
    }
}
