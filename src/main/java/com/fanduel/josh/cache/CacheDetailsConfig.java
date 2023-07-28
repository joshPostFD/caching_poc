package com.fanduel.josh.cache;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(value = "cache")
@Data
@NoArgsConstructor
public class CacheDetailsConfig {

    private Map<String, CacheDetails> map;
    private CacheDetails defaultConfig;

    public CacheDetails get(String key) {
        return Optional.ofNullable(map).map(m -> m.get(key)).orElse(null);
    }

    @Data
    public static class CacheDetails {
        private Long ttlInSeconds;
    }
}
