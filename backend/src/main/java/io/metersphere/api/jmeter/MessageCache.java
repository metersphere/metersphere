package io.metersphere.api.jmeter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageCache {
    public static Map<String, ReportCounter> cache = new HashMap<>();
    // 串行执行队列 KEY=报告ID VALUE=开始时间
    public static Map<String, Long> executionQueue = new ConcurrentHashMap<>();

}
