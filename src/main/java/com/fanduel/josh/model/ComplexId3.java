package com.fanduel.josh.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ComplexId3 {
    private String testString1;
    private String testString2;

    public ComplexId3(String id) {
        testString1 = id;
        testString2 = new StringBuilder(id).reverse().toString();
    }

}
