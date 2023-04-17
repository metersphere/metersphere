package io.metersphere.api.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class FixedCapacityUtils {
    private static Map<String, StringBuffer> fixedCapacityCache = Collections.synchronizedMap(new LRUHashMap<>());

    public static StringBuffer get(String key) {
        return fixedCapacityCache.get(key);
    }

    public static boolean containsKey(String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        return fixedCapacityCache.containsKey(key);
    }

    public static void put(String key, StringBuffer value) {
        if (!fixedCapacityCache.containsKey(key)) {
            fixedCapacityCache.put(key, value);
        }
    }

    public static void remove(String key) {
        if (fixedCapacityCache.containsKey(key)) {
            fixedCapacityCache.remove(key);
        }
    }

    public static int size() {
        return fixedCapacityCache.size();
    }


    static class LRUHashMap<K, V> extends LinkedHashMap<K, V> {
        private int capacity = 3000;

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }
}
