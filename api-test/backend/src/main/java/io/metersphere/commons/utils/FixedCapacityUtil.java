package io.metersphere.commons.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class FixedCapacityUtil {
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

    public static String getJmeterLogger(String reportId, boolean isClear) {
        try {
            StringBuffer console = fixedCapacityCache.get(reportId);
            if (FileUtils.isFolderExists(reportId)) {
                console.append(StringUtils.LF)
                        .append(DateUtils.getTimeString(new Date()))
                        .append(" INFO ").append("Tmp folder  ")
                        .append(FileUtils.BODY_FILE_DIR)
                        .append(File.separator)
                        .append(reportId)
                        .append(" has deleted.");
            }
            if (FileUtils.isFolderExists("tmp" + File.separator + reportId)) {
                console.append(StringUtils.LF)
                        .append(DateUtils.getTimeString(new Date()))
                        .append(" INFO ")
                        .append("Tmp folder  ")
                        .append(FileUtils.BODY_FILE_DIR)
                        .append(File.separator)
                        .append("tmp")
                        .append(File.separator)
                        .append(reportId)
                        .append(" has deleted.");
            }
            return console.toString();
        } catch (Exception e) {
            return StringUtils.EMPTY;
        } finally {
            if (isClear && fixedCapacityCache.containsKey(reportId)) {
                fixedCapacityCache.remove(reportId);
            }
        }
    }
}
