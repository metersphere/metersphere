package io.metersphere.api.jmeter;

import io.metersphere.api.dto.automation.APIScenarioReportResult;
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
    // 用例并发锁
    public static ConcurrentHashMap<String, ApiDefinitionExecResult> caseExecResourceLock = new ConcurrentHashMap<>();
    // 场景并发锁
    public static ConcurrentHashMap<String, APIScenarioReportResult> scenarioExecResourceLock = new ConcurrentHashMap<>();

    // 串行执行队列 KEY=报告ID VALUE=开始时间
    public static Map<String, Long> executionQueue = new HashMap<>();


}
