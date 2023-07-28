package com.fanduel.josh.repository;

import com.fanduel.josh.model.TestObj;
import org.springframework.data.repository.CrudRepository;

public interface TestObjRepository extends CrudRepository<TestObj, String> {
}
