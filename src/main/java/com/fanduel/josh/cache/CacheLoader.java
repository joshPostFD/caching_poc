package com.fanduel.josh.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheLoader {

    private final ReactiveRedisRepository redisRepository;

    public <T> Mono<T> loadOrFetch(Class<T> tClass, Supplier<T> supplier) {
        return redisRepository.find(tClass)
                .switchIfEmpty(Mono.defer(() -> Mono.just(supplier.get()))
                        .doOnSuccess(res -> {
                            if (res != null) {
                                redisRepository.save(res).toFuture();
                            }
                        })
                );
    }

    public <T, ID> Mono<T> loadOrFetchById(Class<T> tClass, ID id, Supplier<T> supplier) {
        return redisRepository.findOne(tClass, id)
                .switchIfEmpty(Mono.defer(() -> Mono.just(supplier.get()))
                        .doOnSuccess(res -> {
                            if (res != null) {
                                redisRepository.save(res, id).toFuture();
                            }
                        })
                );
    }

    public <T, ID> Mono<Map<ID, T>> loadOrFetchManyById(Class<T> tClass,
                                                        Collection<ID> ids,
                                                        Supplier<Map<ID, T>> supplier) {
        return redisRepository.findMany(tClass, ids)
                .map(res -> {
                    Map<ID, T> responseMap = new HashMap<>();
                    if (res != null) {
                        responseMap.putAll(res);
                        ids.removeAll(responseMap.keySet());
                    }
                    if (!ids.isEmpty()) {
                        Map<ID, T> fetchedMap = supplier.get();
                        if (fetchedMap != null) {
                            redisRepository.saveMany(fetchedMap).toFuture();
                            responseMap.putAll(fetchedMap);
                        }
                    }
                    return responseMap;
                });
    }


}
