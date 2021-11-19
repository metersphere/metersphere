package io.metersphere.api.jmeter;

import io.metersphere.base.domain.ApiDefinitionExecResult;

import javax.websocket.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageCache {
    public static Map<String, ReportCounter> concurrencyCounter = new HashMap<>();

    public static ConcurrentHashMap<String, Session> reportCache = new ConcurrentHashMap<>();

    // 用例并发锁
    public static ConcurrentHashMap<String, ApiDefinitionExecResult> caseExecResourceLock = new ConcurrentHashMap<>();

    public static Map<String, Long> jmeterLogTask = new HashMap<>();
}
