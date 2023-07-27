package com.fanduel.josh.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRepository {

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CacheDetailsConfig cacheDetailsConfig;
    private final IdKeyExtractor idKeyExtractor;

    public <T> Mono<T> find(Class<T> tClass) {
        final CacheKey cacheKey = getCacheKey(tClass);
        final String key = cacheKey.generateKey();
        return reactiveStringRedisTemplate.opsForValue()
                .get(key)
                .map(string -> {
                    try {
                        return objectMapper.readValue(string, tClass);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        reactiveStringRedisTemplate.opsForValue().delete(key);
                        return null;
                    }
                }).onErrorResume(this::handleError);
    }

    public <T, ID> Mono<T> findOne(Class<T> tClass, ID id) {
        final CacheKey cacheKey = getCacheKey(tClass);
        final String key = cacheKey.generateKey(idKeyExtractor.extractKey(id));
        return reactiveStringRedisTemplate.opsForValue()
                .get(key)
                .map(string -> {
                    try {
                        return objectMapper.readValue(string, tClass);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        reactiveStringRedisTemplate.opsForValue().delete(key);
                        return null;
                    }
                }).onErrorResume(this::handleError);
    }

    public <T, ID> Mono<Map<ID, T>> findMany(Class<T> tClass, Collection<ID> idCollection) {
        List<ID> ids = new ArrayList<>(idCollection);
        final CacheKey cacheKey = getCacheKey(tClass);
        ids.removeIf(Objects::isNull);
        final List<String> keys = ids.stream()
                .map(id -> cacheKey.generateKey(idKeyExtractor.extractKey(id)))
                .collect(Collectors.toList());
        return reactiveStringRedisTemplate.opsForValue()
                .multiGet(keys)
                .map(stringValues -> {
                    Map<ID, T> keyValueMap = new HashMap<>();
                    for (int index = 0; index < stringValues.size(); index++) {
                        try {
                            String value = stringValues.get(index);
                            if (StringUtils.hasText(value)) {
                                keyValueMap.put(
                                        ids.get(index),
                                        objectMapper.readValue(value, tClass)
                                );
                            }
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                    return keyValueMap;
                }).onErrorResume(this::handleError);
    }

    public <T> Mono<T> save(@NonNull T obj) {
        final CacheKey cacheKey = getCacheKey(obj.getClass());
        try {
            return reactiveStringRedisTemplate.opsForValue().set(
                            cacheKey.generateKey(),
                            objectMapper.writeValueAsString(obj),
                            Duration.ofSeconds(cacheKey.getTtlInSeconds(cacheDetailsConfig)))
                    .map((set) -> obj)
                    .onErrorResume(this::handleError);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.empty();
        }
    }

    public <T, ID> Mono<T> save(@NonNull T obj, @NonNull ID id) {
        final CacheKey cacheKey = getCacheKey(obj.getClass());
        try {
            return reactiveStringRedisTemplate.opsForValue().set(
                            cacheKey.generateKey(idKeyExtractor.extractKey(id)),
                            objectMapper.writeValueAsString(obj))
                    .map((set) -> obj)
                    .onErrorResume(this::handleError);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.empty();
        }
    }


    public <T, ID> Mono<Boolean> saveMany(@NonNull Map<ID, T> idValueMap) {
        if (idValueMap.isEmpty()) {
            return Mono.empty();
        }
        final CacheKey cacheKey = getCacheKey(idValueMap.values().stream().findAny().get().getClass());
        final long ttlInSeconds = cacheKey.getTtlInSeconds(cacheDetailsConfig);
        final Map<String, String> keyValueMap = idValueMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> cacheKey.generateKey(idKeyExtractor.extractKey(entry.getKey())),
                        entry -> {
                            try {
                                return objectMapper.writeValueAsString(entry.getValue());
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                ));
        if (ttlInSeconds > 0) {
            keyValueMap.entrySet()
                    .parallelStream()
                    .forEach(entry -> reactiveStringRedisTemplate.opsForValue().set(
                            entry.getKey(), entry.getValue(), Duration.ofSeconds(ttlInSeconds)));
            return Mono.just(true);
        } else {
            return reactiveStringRedisTemplate.opsForValue().multiSet(keyValueMap)
                    .onErrorResume(this::handleError);
        }
    }

    public <T, ID> Mono<Boolean> delete(Class<T> tClass, ID id) {
        return reactiveStringRedisTemplate.opsForValue().delete(
                        getCacheKey(tClass).generateKey(idKeyExtractor.extractKey(id)))
                .onErrorResume(this::handleError);
    }

    private CacheKey getCacheKey(Class<?> type) {
        return CacheKey.fromClass(type)
                .orElseThrow(() ->
                        new RuntimeException("Type " + type.getName() + " is not registered in CacheKey enum."));
    }

    private <T> Mono<T> handleError(Throwable throwable) {
        return Mono.empty();
    }

}
