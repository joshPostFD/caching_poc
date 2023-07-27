package com.fanduel.josh.controller;

import com.fanduel.josh.cache.CacheConfig;
import com.fanduel.josh.model.TestObj;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RequestMapping("test2")
@RequiredArgsConstructor
@RestController
@Slf4j
public class TestController2 {

    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("{id}")
    public Mono<TestObj> getById(@PathVariable("id") String id) {
        return reactiveStringRedisTemplate.opsForValue().get(id)
                .map(string -> {
                    try {
                        return objectMapper.readValue(string, TestObj.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @PostMapping("{id}")
    public Mono<Boolean> post(@PathVariable("id") String id,
                              @RequestBody TestObj test) throws JsonProcessingException {
        return reactiveStringRedisTemplate.opsForValue().set(
                KeyGenerator.generateKey(id, test.getTestString1()),
                objectMapper.writeValueAsString(test));
//                Duration.ofSeconds(10));
    }

    @DeleteMapping("{id}")
    public Mono<Boolean> delete(@PathVariable("id") String id) {
        return reactiveStringRedisTemplate.opsForValue().delete(id);
    }

    static class KeyGenerator {
        static String generateKey(@NonNull String... keys) {
            return String.join(CacheConfig.KEY_DELIMITER, keys);
        }
    }

}
