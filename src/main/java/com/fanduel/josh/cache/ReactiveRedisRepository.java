package com.fanduel.josh.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReactiveRedisRepository implements ReactiveCrudRepository {

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CacheDetailsConfig cacheDetailsConfig;
    private final ObjectKeyExtractor idKeyExtractor;

    @Override
    public <T> Mono<T> find(Class<T> tClass) {
        final ClassKey cacheKey = getCacheKey(tClass);
        final String key = cacheKey.generateKey();
        return fetchKey(key, tClass);
    }

    @Override
    public <T, ID> Mono<T> findOne(Class<T> tClass, ID id) {
        final ClassKey cacheKey = getCacheKey(tClass);
        final String key = cacheKey.generateKey(idKeyExtractor.extractKey(id));
        return fetchKey(key, tClass);
    }

    private <T> Mono<T> fetchKey(String key, Class<T> tClass) {
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

    @Override
    public <T, ID> Mono<Map<ID, T>> findMany(Class<T> tClass, Collection<ID> idCollection) {
        List<ID> ids = new ArrayList<>(idCollection);
        final ClassKey cacheKey = getCacheKey(tClass);
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
                        }
                    }
                    return keyValueMap;
                }).onErrorResume(this::handleError);
    }

    @Override
    public <T> Mono<T> save(@NonNull T obj) {
        final ClassKey cacheKey = getCacheKey(obj.getClass());
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

    @Override
    public <T, ID> Mono<T> save(@NonNull T obj, @NonNull ID id) {
        final ClassKey cacheKey = getCacheKey(obj.getClass());
        try {
            return reactiveStringRedisTemplate.opsForValue().set(
                            cacheKey.generateKey(idKeyExtractor.extractKey(id)),
                            objectMapper.writeValueAsString(obj),
                            Duration.ofSeconds(cacheKey.getTtlInSeconds(cacheDetailsConfig))
                    )
                    .map((set) -> obj)
                    .onErrorResume(this::handleError);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.empty();
        }
    }

    @Override
    public <T, ID> Mono<Boolean> saveMany(@NonNull Map<ID, T> idValueMap) {
        if (idValueMap.isEmpty()) {
            return Mono.empty();
        }
        final ClassKey cacheKey = getCacheKey(idValueMap.values().stream().findAny().get().getClass());
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
            return Flux.fromIterable(keyValueMap.entrySet())
                    .flatMap(entry -> reactiveStringRedisTemplate.opsForValue().set(
                            entry.getKey(), entry.getValue(), Duration.ofSeconds(ttlInSeconds)))
                    .reduce((a, b) -> a || b);
        } else {
            return reactiveStringRedisTemplate.opsForValue().multiSet(keyValueMap)
                    .onErrorResume(this::handleError);
        }
    }

    @Override
    public <T, ID> Mono<Boolean> delete(Class<T> tClass, ID id) {
        return reactiveStringRedisTemplate.opsForValue().delete(
                        getCacheKey(tClass).generateKey(idKeyExtractor.extractKey(id)))
                .onErrorResume(this::handleError);
    }

    private ClassKey getCacheKey(Class<?> type) {
        return ClassKey.fromClass(type)
                .orElseThrow(() ->
                        new RuntimeException("Type " + type.getName() + " is not registered in CacheKey enum."));
    }

    private <T> Mono<T> handleError(Throwable throwable) {
        return Mono.empty();
    }

}
