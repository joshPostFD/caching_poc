package com.fanduel.josh.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Data
public class TestObj2 {

    private String testString1;
    private String testString2;
    private Integer testInteger1;
    private Integer testInteger2;

}
