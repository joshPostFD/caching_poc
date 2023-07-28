package com.fanduel.josh.controller;

import com.fanduel.josh.cache.CacheConfig;
import com.fanduel.josh.cache.CacheLoader;
import com.fanduel.josh.model.TestObj2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.MultiValueMap;
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

@RequestMapping("test2_2")
@RequiredArgsConstructor
@RestController
@Slf4j
public class TestController2_2 {

    private final CacheLoader cacheLoader;

    @GetMapping
    public Mono<TestObj2> get() {
        return cacheLoader.loadOrFetch(TestObj2.class, this::newTestObj2);
    }

    @GetMapping("{id}")
    public Mono<TestObj2> getById(@PathVariable("id") String id) {
        return cacheLoader.loadOrFetchById(TestObj2.class, id, () -> this.newTestObj2(id));
    }


    @GetMapping("many")
    public Mono<Map<String, TestObj2>> getById(@RequestParam MultiValueMap<String, String> params) {
        // Collection of IDs to grab
        List<String> ids = params.get("id");
        // Supply cache loader with Class of the return type, and the collection of IDs you want
        return cacheLoader.loadOrFetchManyById(TestObj2.class, ids,
                // Our typical Set -> Map service method.
                // This will only be called to grab the IDs that do not exist in the cache
                () -> ids.parallelStream().collect(Collectors.toMap(
                        Function.identity(),
                        this::newTestObj2
                )));
    }

    public TestObj2 newTestObj2(String id) {
        log.info("creating newTestObj2 {}", id);
        TestObj2 TestObj2 = new TestObj2();
        TestObj2.setTestString1(id);
        TestObj2.setTestString2(String.valueOf((int) (Math.random() * 100)));
        TestObj2.setTestInteger1((int) (Math.random() * 100));
        TestObj2.setTestInteger2((int) (Math.random() * 100));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return TestObj2;
    }

    public TestObj2 newTestObj2() {
        return newTestObj2("Generated - " + String.valueOf((int) (Math.random() * 100)));
    }

    static class KeyGenerator {
        static String generateKey(@NonNull String... keys) {
            return String.join(CacheConfig.KEY_DELIMITER, keys);
        }
    }

}
