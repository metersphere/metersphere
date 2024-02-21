package io.metersphere.sdk.util;

import io.metersphere.sdk.dto.SocketMsgDTO;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketUtils {
    public static final Map<String, Session> ONLINE_USER_SESSIONS = new ConcurrentHashMap<>();

    // 单用户推送
    public static void sendMessage(Session session, SocketMsgDTO message) {
        if (session == null) {
            return;
        }
        // 替换了web容器后 jetty没有设置永久有效的参数，这里暂时设置超时时间为一天
        session.setMaxIdleTimeout(86400000L);
        RemoteEndpoint.Async async = session.getAsyncRemote();
        if (async == null) {
            return;
        }
        async.sendText(JSON.toJSONString(message));
    }

    // 单用户推送
    public static void sendMessageSingle(SocketMsgDTO dto) {
        sendMessage(ONLINE_USER_SESSIONS.get(Optional.ofNullable(dto.getReportId())
                .orElse(StringUtils.EMPTY)), dto);
    }

    // 全用户推送
    public static void sendMessageAll(SocketMsgDTO message) {
        ONLINE_USER_SESSIONS.forEach((sessionId, session) -> {
            sendMessage(session, message);
        });
    }

    public static boolean has(String key) {
        return StringUtils.isNotEmpty(key) && ONLINE_USER_SESSIONS.containsKey(key);
    }

    //当前的Session 移除
    public static void onClose(String reportId) {
        try {
            if (WebSocketUtils.ONLINE_USER_SESSIONS.containsKey(reportId)) {
                WebSocketUtils.ONLINE_USER_SESSIONS.get(reportId).close();
                WebSocketUtils.ONLINE_USER_SESSIONS.remove(reportId);
            }
        } catch (Exception e) {
            LogUtils.error("关闭socket失败：{}", e);
        }
    }
}
