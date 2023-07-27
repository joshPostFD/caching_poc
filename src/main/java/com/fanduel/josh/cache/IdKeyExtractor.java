package com.fanduel.josh.cache;

import com.fanduel.josh.model.TestObj;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

@Component
public class IdKeyExtractor {

    private final Map<Class<?>, Function<?, String>> keyExtractors = new HashMap<>();

    public IdKeyExtractor() {
        registerExtractor(String.class, Function.identity());
        registerExtractor(Integer.class, Object::toString);
        registerExtractor(Double.class, Object::toString);
        registerExtractor(Long.class, Object::toString);
        registerExtractor(TestObj.class, (TestObj test) -> test.getTestInteger1().toString());
    }

    private String combineStrings(String... strings) {
        return String.join(CacheConfig.KEY_DELIMITER, strings);
    }

    private <T> void registerExtractor(Class<T> tClass, Function<T, String> func) {
        if (keyExtractors.containsKey(tClass)) {
            throw new DuplicateKeyException(tClass.getName() + " is already registered in " + getClass().getName());
        }
        keyExtractors.put(tClass, func);
    }

    @SuppressWarnings("unchecked")
    public <T> String extractKey(T obj) {
        Function<T, String> extractor = (Function<T, String>) keyExtractors.get(obj.getClass());
        if (extractor == null) {
            throw new NoSuchElementException(obj.getClass().getName() + " is not registered in " + getClass().getName());
        }
        return extractor.apply(obj);
    }

}
