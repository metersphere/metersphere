package io.metersphere.api.jmeter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class FixedCapacityUtils {
    public static Map<Long, StringBuffer> fixedCapacityCache = Collections.synchronizedMap(new LRUHashMap<>());

    public static StringBuffer get(Long key) {
        return fixedCapacityCache.get(key);
    }

    public static void put(Long key, StringBuffer value) {
        fixedCapacityCache.put(key, value);
    }

    public static int size() {
        return fixedCapacityCache.size();
    }


    static class LRUHashMap<K, V> extends LinkedHashMap<K, V> {
        private int capacity = 100;

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }
}
