package com.fanduel.josh.repository.custom;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

public interface ReactiveCrudRepository {
    <T> Mono<T> find(Class<T> tClass);

    <T, ID> Mono<T> findOne(Class<T> tClass, ID id);

    <T, ID> Mono<Map<ID, T>> findMany(Class<T> tClass, Collection<ID> idCollection);

    <T> Mono<T> save(@NonNull T obj);

    <T, ID> Mono<T> save(@NonNull T obj, @NonNull ID id);

    <T, ID> Mono<Boolean> saveMany(@NonNull Map<ID, T> idValueMap);

    <T> Mono<Boolean> delete(Class<T> tClass);

    <T, ID> Mono<Boolean> delete(Class<T> tClass, ID id);
}
