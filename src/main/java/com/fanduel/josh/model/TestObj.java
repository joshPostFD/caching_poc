package com.fanduel.josh.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Data
@RedisHash
public class TestObj {

    @Id
    private String testString1;
    private String testString2;
    private Integer testInteger1;
    private Integer testInteger2;

    @TimeToLive
    private Integer ttl = 60;

}
