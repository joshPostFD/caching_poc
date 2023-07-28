package com.fanduel.josh.repository.custom;

import com.fanduel.josh.cache.CacheConfig;
import com.fanduel.josh.cache.CacheDetailsConfig;
import com.fanduel.josh.model.TestObj;
import com.fanduel.josh.model.TestObj2;
import com.fanduel.josh.model.TestObj3;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Optional;

public enum ClassKey {

    testObj(TestObj.class, false),
    testObj2(TestObj2.class, true),
    testObj3(TestObj3.class, true),
    ;

    @Getter
    private final Class<?> type;
    @Getter
    private final boolean multipleItems;

    ClassKey(Class<?> clazz, boolean multipleItems) {
        type = clazz;
        this.multipleItems = multipleItems;
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

    public static Optional<ClassKey> fromClass(Class<?> type) {
        return Arrays.stream(values())
                .filter(cacheKey -> cacheKey.type.equals(type))
                .findFirst();
    }
}
