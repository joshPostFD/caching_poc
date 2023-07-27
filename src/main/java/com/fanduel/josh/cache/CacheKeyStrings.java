package com.fanduel.josh.cache;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CacheKeyStrings {

    private CacheKeyStrings() {}
    public static final String PARTNER_PLAYERS_MAPPING_CACHE = "partnerplayersmap";

    public static List<String> values() {
        final CacheKeyStrings instance = new CacheKeyStrings();
        return Arrays.stream(CacheKeyStrings.class.getDeclaredFields())
                .filter(field -> Modifier.isPublic(field.getModifiers()))
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> Modifier.isFinal(field.getModifiers()))
                .map(
                        field -> {
                            try {
                                return field.get(instance);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
