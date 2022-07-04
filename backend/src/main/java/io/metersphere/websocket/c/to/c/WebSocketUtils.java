package io.metersphere.websocket.c.to.c;

import io.metersphere.utils.LoggerUtil;
import io.metersphere.websocket.c.to.c.util.MsgDto;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketUtils {
    public static final Map<String, Session> ONLINE_USER_SESSIONS = new ConcurrentHashMap<>();

    // 单用户推送
    public static void sendMessage(Session session, String message) {
        if (session == null) {
            return;
        }
        // 替换了web容器后 jetty没有设置永久有效的参数，这里暂时设置超时时间为一天
        session.setMaxIdleTimeout(86400000l);
        RemoteEndpoint.Async async = session.getAsyncRemote();
        if (async == null) {
            return;
        }
        async.sendText(message);
    }

    // 单用户推送
    public static void sendMessageSingle(MsgDto dto) {
        sendMessage(ONLINE_USER_SESSIONS.get(Optional.ofNullable(dto.getReportId()).orElse("")), dto.getContent());
        sendMessage(ONLINE_USER_SESSIONS.get(Optional.ofNullable(dto.getToReport()).orElse("")), dto.getContent());
    }

    // 全用户推送
    public static void sendMessageAll(String message) {
        ONLINE_USER_SESSIONS.forEach((sessionId, session) -> {
            sendMessage(session, message);
        });
    }

    //当前的Session 移除
    public static void onClose(String reportId) {
        try {
            if (WebSocketUtils.ONLINE_USER_SESSIONS.containsKey(reportId)) {
                WebSocketUtils.ONLINE_USER_SESSIONS.get(reportId).close();
                WebSocketUtils.ONLINE_USER_SESSIONS.remove(reportId);
            }
            if (WebSocketUtils.ONLINE_USER_SESSIONS.containsKey(("send." + reportId))) {
                WebSocketUtils.ONLINE_USER_SESSIONS.get(("send." + reportId)).close();
                WebSocketUtils.ONLINE_USER_SESSIONS.remove(("send." + reportId));
            }
        } catch (Exception e) {
            LoggerUtil.error("关闭socket失败：", e);
        }
    }
}
