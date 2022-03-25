package io.metersphere.api.jmeter;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FixedCapacityUtils {
    public static Map<Long, StringBuffer> fixedCapacityCache = Collections.synchronizedMap(new LRUHashMap<>());
    public final static Map<String, Long> jmeterLogTask = new HashMap<>();

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


    public static String getJmeterLogger(String testId) {
        try {
            Long startTime = FixedCapacityUtils.jmeterLogTask.get(testId);
            if (startTime == null) {
                startTime = FixedCapacityUtils.jmeterLogTask.get("[" + testId + "]");
            }
            if (startTime == null) {
                startTime = System.currentTimeMillis();
            }
            Long endTime = System.currentTimeMillis();
            Long finalStartTime = startTime;
            String logMessage = FixedCapacityUtils.fixedCapacityCache.entrySet().stream()
                    .filter(map -> map.getKey() > finalStartTime && map.getKey() <= endTime)
                    .map(map -> map.getValue()).collect(Collectors.joining());
            return logMessage;
        } catch (Exception e) {
            return "";
        }
    }
}
