package com.fanduel.josh.controller;

import com.fanduel.josh.model.TestObj;
import com.fanduel.josh.repository.TestObjRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RequestMapping("testrepo")
@RequiredArgsConstructor
@RestController
@Slf4j
public class TestRepositoryController {

    private final TestObjRepository testObjRepository;

    @GetMapping("{id}")
    public Mono<Optional<TestObj>> getById(@PathVariable("id") String id) {
        return Mono.create((m) ->
                m.success(testObjRepository.findById(id)));
    }

    @PostMapping("{id}")
    public Mono<TestObj> post(@PathVariable("id") String id,
                              @RequestBody TestObj test) {
        return Mono.create((m) ->
                m.success(testObjRepository.save(test)));
    }

    @DeleteMapping("{id}")
    public Mono<Void> delete(@PathVariable("id") String id) {
        return Mono.create((m) -> {
            testObjRepository.deleteById(id);
            m.success();
        });
    }

}
