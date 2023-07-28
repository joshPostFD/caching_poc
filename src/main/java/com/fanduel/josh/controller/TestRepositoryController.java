package com.fanduel.josh.controller;

import com.fanduel.josh.model.TestObj;
import com.fanduel.josh.repository.TestObjRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
