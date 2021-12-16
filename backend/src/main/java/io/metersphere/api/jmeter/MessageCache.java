package io.metersphere.api.jmeter;

import io.metersphere.base.domain.ApiDefinitionExecResult;

import javax.websocket.Session;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageCache {
    public final static Map<String, ReportCounter> concurrencyCounter = new HashMap<>();

    public final static ConcurrentHashMap<String, Session> reportCache = new ConcurrentHashMap<>();
    // 用例并发锁
    public final static ConcurrentHashMap<String, ApiDefinitionExecResult> caseExecResourceLock = new ConcurrentHashMap<>();

    public final static Map<String, Long> jmeterLogTask = new HashMap<>();

    // 定时任务报告
    public final static List<String> jobReportCache = new LinkedList<>();
}
