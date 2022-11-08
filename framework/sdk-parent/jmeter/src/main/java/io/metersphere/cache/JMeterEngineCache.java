package io.metersphere.cache;

import org.apache.jmeter.engine.StandardJMeterEngine;

import java.util.concurrent.ConcurrentHashMap;

public class JMeterEngineCache {
    /**
     * 执行中的线程池
     */
    public static ConcurrentHashMap<String, StandardJMeterEngine> runningEngine = new ConcurrentHashMap<>();

}
