package com.fanduel.josh.repository.custom;

import com.fanduel.josh.cache.CacheConfig;
import com.fanduel.josh.cache.CacheDetailsConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public <T> Mono<T> find(@NonNull Class<T> tClass) {
        final ClassKey classKey = getClassKey(tClass);
        checkMultipleAllowed(classKey, false);
        final String key = classKey.generateKey();
        return fetchKey(key, tClass);
    }

    @Override
    public <T, ID> Mono<T> findOne(@NonNull Class<T> tClass, @NonNull ID id) {
        final ClassKey classKey = getClassKey(tClass);
        checkMultipleAllowed(classKey, true);
        final String key = classKey.generateKey(idKeyExtractor.extractKey(id));
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
    public <T, ID> Mono<Map<ID, T>> findMany(@NonNull Class<T> tClass, @NonNull Collection<ID> idCollection) {
        List<ID> ids = new ArrayList<>(idCollection);
        final ClassKey classKey = getClassKey(tClass);
        checkMultipleAllowed(classKey, true);
        ids.removeIf(Objects::isNull);
        final List<String> keys = ids.stream()
                .map(id -> classKey.generateKey(idKeyExtractor.extractKey(id)))
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
        final ClassKey classKey = getClassKey(obj.getClass());
        checkMultipleAllowed(classKey, false);
        try {
            return reactiveStringRedisTemplate.opsForValue().set(
                            classKey.generateKey(),
                            objectMapper.writeValueAsString(obj),
                            Duration.ofSeconds(classKey.getTtlInSeconds(cacheDetailsConfig)))
                    .map((set) -> obj)
                    .onErrorResume(this::handleError);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.empty();
        }
    }

    @Override
    public <T, ID> Mono<T> save(@NonNull T obj, @NonNull ID id) {
        final ClassKey classKey = getClassKey(obj.getClass());
        checkMultipleAllowed(classKey, true);
        if (!classKey.isMultipleItems()) {
            throw new RuntimeException("Only one instance of " + classKey.getType().getName()
                    + " can exist and therefore no ID should be provided."
                    + " Use overloaded save method without ID included.");
        }
        try {
            return reactiveStringRedisTemplate.opsForValue().set(
                            classKey.generateKey(idKeyExtractor.extractKey(id)),
                            objectMapper.writeValueAsString(obj),
                            Duration.ofSeconds(classKey.getTtlInSeconds(cacheDetailsConfig))
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
        final ClassKey classKey = getClassKey(idValueMap.values().stream().findAny().get().getClass());
        checkMultipleAllowed(classKey, true);
        final long ttlInSeconds = classKey.getTtlInSeconds(cacheDetailsConfig);
        final Map<String, String> keyValueMap = idValueMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> classKey.generateKey(idKeyExtractor.extractKey(entry.getKey())),
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
        ClassKey classKey = getClassKey(tClass);
        checkMultipleAllowed(classKey, true);
        return reactiveStringRedisTemplate.opsForValue().delete(
                        classKey.generateKey(idKeyExtractor.extractKey(id)))
                .onErrorResume(this::handleError);
    }

    @Override
    public <T> Mono<Boolean> delete(Class<T> tClass) {
        ClassKey classKey = getClassKey(tClass);
        checkMultipleAllowed(classKey, false);
        return reactiveStringRedisTemplate.opsForValue().delete(classKey.getKey())
                .onErrorResume(this::handleError);
    }

    public <T> Mono<Long> deleteAllOfType(Class<T> tClass) {
        return deleteAllByKeyName(getClassKey(tClass).getKey());
    }

    public Mono<Long> deleteAllByKeyName(String keyName) {
        ClassKey classKey = Arrays.stream(ClassKey.values())
                .filter(ck -> ck.name().equals(keyName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid key provided."));

        if (classKey.isMultipleItems()) {
            return reactiveStringRedisTemplate.scan(
                            ScanOptions.scanOptions()
                                    .match(classKey.getKey() + CacheConfig.KEY_DELIMITER + "*")
                                    .build())
                    .buffer(100)
                    .flatMap(keyList ->
                            reactiveStringRedisTemplate.delete(keyList.toArray(String[]::new)))
                    .reduce(Long::sum);
        } else {
            return reactiveStringRedisTemplate.delete(classKey.getKey());
        }
    }

    private ClassKey getClassKey(Class<?> type) {
        return ClassKey.fromClass(type)
                .orElseThrow(() ->
                        new RuntimeException("Type " + type.getName() + " is not registered in ClassKey enum."));
    }

    private <T> Mono<T> handleError(Throwable throwable) {
        return Mono.empty();
    }

    private void checkMultipleAllowed(ClassKey classKey, boolean multipleAllowed) {
        if (multipleAllowed != classKey.isMultipleItems()) {
            if (classKey.isMultipleItems()) {
                throw new RuntimeException("Multiple instances of " + classKey.getType().getName()
                        + " can exist and therefore only methods including an ID should be used for this type.");
            } else {
                throw new RuntimeException("Only one instance of " + classKey.getType().getName()
                        + " can exist and therefore no methods including an ID should be used for this type.");
            }
        }
    }

}
