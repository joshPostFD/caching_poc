package com.fanduel.josh.controller;

import com.fanduel.josh.cache.CacheConfig;
import com.fanduel.josh.cache.CacheLoader;
import com.fanduel.josh.repository.custom.ReactiveRedisRepository;
import com.fanduel.josh.model.ComplexId3;
import com.fanduel.josh.model.TestObj3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequestMapping("test2_3")
@RequiredArgsConstructor
@RestController
@Slf4j
public class TestController2_3 {

    private final CacheLoader cacheLoader;
    private final ReactiveRedisRepository reactiveRedisRepository;

    @GetMapping
    public Mono<TestObj3> get() {
        return cacheLoader.loadOrFetch(TestObj3.class,
                this::newTestObj3);
    }

    @GetMapping("{id}")
    public Mono<TestObj3> getById(@PathVariable("id") String id) {
        return cacheLoader.loadOrFetchById(TestObj3.class, new ComplexId3(id),
                () -> this.newTestObj3(new ComplexId3(id)));
    }

    @GetMapping("many")
    public Mono<Map<ComplexId3, TestObj3>> getManyById(@RequestParam MultiValueMap<String, String> params) {
        List<ComplexId3> ids = params.get("id").stream().map(ComplexId3::new).collect(Collectors.toList());
        return cacheLoader.loadOrFetchManyById(TestObj3.class, ids,
                () -> ids.parallelStream()
                        .collect(Collectors.toMap(
                                Function.identity(),
                                this::newTestObj3
                        )));
    }

    @DeleteMapping()
    public Mono<Long> delete() {
        return reactiveRedisRepository.deleteAllOfType(TestObj3.class);
    }

    @DeleteMapping("{id}")
    public Mono<Long> deleteById(@PathVariable("id") String id) {
        return reactiveRedisRepository.deleteAllByKeyName(id);
    }

    public TestObj3 newTestObj3(ComplexId3 id) {
        log.info("creating newTestObj3 {}", id);
        TestObj3 TestObj3 = new TestObj3();
        TestObj3.setTestString1(id.getTestString1());
        TestObj3.setTestString2(id.getTestString2());
        TestObj3.setTestInteger1((int) (Math.random() * 100));
        TestObj3.setTestInteger2((int) (Math.random() * 100));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return TestObj3;
    }

    public TestObj3 newTestObj3() {
        return newTestObj3(new ComplexId3("Generated - " + String.valueOf((int) (Math.random() * 100))));
    }

    static class KeyGenerator {
        static String generateKey(@NonNull String... keys) {
            return String.join(CacheConfig.KEY_DELIMITER, keys);
        }
    }

}
