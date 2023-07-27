package com.fanduel.josh.controller;

import com.fanduel.josh.model.TestObj;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequestMapping("test")
@RequiredArgsConstructor
@RestController
@Slf4j
public class TestController {

    private final ReactiveRedisTemplate<String, TestObj> reactiveRedisTemplate;
    private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("{id}")
    public Mono<TestObj> getById(@PathVariable("id") String id) {
        return reactiveRedisTemplate.opsForHash().get("testKey", id)
                .map(string -> {
                    try {
                        return objectMapper.readValue((String)string, TestObj.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // No TTL available for hash
    @PostMapping("{id}")
    public Mono<Boolean> post(@PathVariable("id") String id,
                              @RequestBody TestObj test) throws JsonProcessingException {
        return reactiveStringRedisTemplate.opsForHash().put(
                "testKey",
                id,
                objectMapper.writeValueAsString(test));
    }

    @DeleteMapping("{id}")
    public Mono<Long> delete(@PathVariable("id") String id) {
        return reactiveStringRedisTemplate.opsForHash().remove("testKey", id);
    }


}
