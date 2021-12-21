package io.metersphere.api.jmeter;

import javax.websocket.Session;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageCache {
    public final static ConcurrentHashMap<String, Session> reportCache = new ConcurrentHashMap<>();
    
    public final static Map<String, Long> jmeterLogTask = new HashMap<>();

    // 定时任务报告
    public final static List<String> jobReportCache = new LinkedList<>();

    public static int corePoolSize = 10;
}
