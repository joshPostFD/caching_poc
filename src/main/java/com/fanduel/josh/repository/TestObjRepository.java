package com.fanduel.josh.repository;

import com.fanduel.josh.model.TestObj;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

public interface TestObjRepository extends CrudRepository<TestObj, String> {
}
