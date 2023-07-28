package com.fanduel.josh.controller;

import com.fanduel.josh.cache.CacheConfig;
import com.fanduel.josh.cache.CacheLoader;
import com.fanduel.josh.model.TestObj;
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

@RequestMapping("test2_1")
@RequiredArgsConstructor
@RestController
@Slf4j
public class TestController2_1 {

    private final CacheLoader cacheLoader;

    @GetMapping
    public Mono<TestObj> get() {
        return cacheLoader.loadOrFetch(TestObj.class, this::newTestObj);
    }

    @GetMapping("{id}")
    public Mono<TestObj> getById(@PathVariable("id") String id) {
        return cacheLoader.loadOrFetchById(TestObj.class, id, () -> this.newTestObj(id));
    }


    @GetMapping("many")
    public Mono<Map<String, TestObj>> getById(@RequestParam MultiValueMap<String, String> params) {
        List<String> ids = params.get("id");
        return cacheLoader.loadOrFetchManyById(TestObj.class, ids,
                () -> ids.stream().collect(Collectors.toMap(
                        Function.identity(),
                        this::newTestObj
                )));
    }

    public TestObj newTestObj(String id) {
        log.info("creating newTestObj {}", id);
        TestObj testObj = new TestObj();
        testObj.setTestString1(id);
        testObj.setTestString2(String.valueOf((int) (Math.random() * 100)));
        testObj.setTestInteger1((int) (Math.random() * 100));
        testObj.setTestInteger2((int) (Math.random() * 100));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return testObj;
    }

    public TestObj newTestObj() {
        return newTestObj("Generated - " + String.valueOf((int) (Math.random() * 100)));
    }

    static class KeyGenerator {
        static String generateKey(@NonNull String... keys) {
            return String.join(CacheConfig.KEY_DELIMITER, keys);
        }
    }

}
