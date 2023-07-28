package com.fanduel.josh.repository.custom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ReactiveCaffeineRepository implements ReactiveCrudRepository {


    @Override
    public <T> Mono<T> find(Class<T> tClass) {
        throw new RuntimeException("Not Implemented Yet");
    }

    @Override
    public <T, ID> Mono<T> findOne(Class<T> tClass, ID id) {
        throw new RuntimeException("Not Implemented Yet");
    }

    @Override
    public <T, ID> Mono<Map<ID, T>> findMany(Class<T> tClass, Collection<ID> ids) {
        throw new RuntimeException("Not Implemented Yet");
    }

    @Override
    public <T> Mono<T> save(T obj) {
        throw new RuntimeException("Not Implemented Yet");
    }

    @Override
    public <T, ID> Mono<T> save(T obj, ID id) {
        throw new RuntimeException("Not Implemented Yet");
    }

    @Override
    public <T, ID> Mono<Boolean> saveMany(Map<ID, T> idValueMap) {
        throw new RuntimeException("Not Implemented Yet");
    }

    @Override
    public <T> Mono<Boolean> delete(Class<T> tClass) {
        throw new RuntimeException("Not Implemented Yet");
    }

    @Override
    public <T, ID> Mono<Boolean> delete(Class<T> tClass, ID id) {
        throw new RuntimeException("Not Implemented Yet");
    }
}
