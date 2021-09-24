package io.metersphere.api.jmeter;

import io.metersphere.base.domain.ApiDefinitionExecResult;
import org.apache.jmeter.engine.StandardJMeterEngine;

import javax.websocket.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MessageCache {
    public static Map<String, ReportCounter> cache = new HashMap<>();

    public static ConcurrentHashMap<String, Session> reportCache = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, StandardJMeterEngine> runningEngine = new ConcurrentHashMap<>();

    public static ConcurrentLinkedDeque<String> terminationOrderDeque = new ConcurrentLinkedDeque<>();

    public static ConcurrentHashMap<String, ApiDefinitionExecResult> batchTestCases = new ConcurrentHashMap<>();

}
