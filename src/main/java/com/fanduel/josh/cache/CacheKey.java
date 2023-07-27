package com.fanduel.josh.cache;

import com.fanduel.josh.model.TestObj;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Optional;

public enum CacheKey {

    testObj(TestObj.class);

    @Getter
    final Class<?> type;

    CacheKey(Class<?> clazz) {
        type = clazz;
    }

    public String getKey() {
        return name();
    }

    public long getTtlInSeconds(CacheDetailsConfig cacheDetailsConfig) {
        return Optional.ofNullable(cacheDetailsConfig.get(getKey()))
                .map(CacheDetailsConfig.CacheDetails::getTtlInSeconds)
                .orElse(-1L);
    }

    public String generateKey(@Nullable String... keys) {
        if (keys == null || keys.length == 0) {
            return getKey();
        }
        return getKey() + CacheConfig.KEY_DELIMITER + String.join(CacheConfig.KEY_DELIMITER, keys);
    }

    public static Optional<CacheKey> fromClass(Class<?> type) {
        return Arrays.stream(values())
                .filter(cacheKey -> cacheKey.type.equals(type))
                .findFirst();
    }
}
