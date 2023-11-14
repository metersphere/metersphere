package io.metersphere.websocket;


import io.metersphere.commons.utils.LogUtil;
import io.metersphere.utils.LoggerUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 推送消息更新UI列表用例状态
 */

@Component
@ServerEndpoint("/websocket/plan/load/{planId}")
public class LoadCaseStatusHandleSocket {

    public static final Map<String, Set<Session>> ONLINE_PLAN_LOAD_SESSIONS = new ConcurrentHashMap<>();

    public static void sendMessage(Session session, String message) {
        if (session == null) {
            return;
        }
        // 替换了web容器后 jetty没有设置永久有效的参数，这里暂时设置超时时间为一天
        session.setMaxIdleTimeout(86400000L);
        RemoteEndpoint.Async async = session.getAsyncRemote();
        if (async == null) {
            return;
        }
        async.sendText(message);
    }

    public static void sendMessageSingle(String planId, String message) {
        if (ONLINE_PLAN_LOAD_SESSIONS.containsKey(planId)) {
            ONLINE_PLAN_LOAD_SESSIONS.get(planId).forEach(session -> sendMessage(session, message));
        }
    }

    /**
     * 连接成功响应
     */
    @OnOpen
    public void openSession(@PathParam("planId") String planId, Session session) {
        Set<Session> sessions = ONLINE_PLAN_LOAD_SESSIONS.getOrDefault(planId, new HashSet<>());
        sessions.add(session);
        ONLINE_PLAN_LOAD_SESSIONS.put(planId, sessions);
        sendMessage(session, "CONN_SUCCEEDED");
        LoggerUtil.info("客户端: [" + planId + "] : 连接成功！" + ONLINE_PLAN_LOAD_SESSIONS.size());
    }

    /**
     * 收到消息响应
     */
    @OnMessage
    public void onMessage(@PathParam("planId") String planId, String message) {
        LoggerUtil.info("服务器收到：[" + planId + "] : " + message);
        sendMessageSingle(planId, message);
    }

    /**
     * 连接关闭响应
     */
    @OnClose
    public void onClose(@PathParam("planId") String planId, Session session) throws IOException {
        //当前的Session 移除
        Set<Session> sessions = ONLINE_PLAN_LOAD_SESSIONS.get(planId);
        if (CollectionUtils.isNotEmpty(sessions)) {
            LogUtil.info("剔除一个socket链接: {} - {}", planId, session.getId());
            sessions.remove(session);
            LogUtil.info("在线socket链接: {}, size: {}", planId, sessions.size());
        } else {
            ONLINE_PLAN_LOAD_SESSIONS.remove(planId);
            LoggerUtil.info("[" + planId + "] : 断开连接！" + ONLINE_PLAN_LOAD_SESSIONS.size());
            //并且通知其他人当前用户已经断开连接了
            session.close();
        }
    }

    /**
     * 连接异常响应
     */
    @OnError
    public void onError(Session session, Throwable throwable) throws IOException {
        session.close();
    }

    /**
     * 每一分钟群发一次心跳检查
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void heartbeatCheck() {
        for (Set<Session> sessions : ONLINE_PLAN_LOAD_SESSIONS.values()) {
            sessions.forEach(session -> sendMessage(session, "heartbeat check"));
        }
    }
}
