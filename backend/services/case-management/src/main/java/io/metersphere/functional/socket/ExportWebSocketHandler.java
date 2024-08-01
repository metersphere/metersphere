package io.metersphere.functional.socket;

import io.metersphere.sdk.constants.MsgType;
import io.metersphere.sdk.dto.SocketMsgDTO;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/export/{fileId}")
public class ExportWebSocketHandler {

    public static final Map<String, Session> ONLINE_EXPORT_EXCEL_SESSIONS = new ConcurrentHashMap<>();

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

    public static void sendMessageSingle(SocketMsgDTO dto) {
        sendMessage(ONLINE_EXPORT_EXCEL_SESSIONS.get(Optional.ofNullable(dto.getReportId())
                .orElse(StringUtils.EMPTY)), dto);
    }


    /**
     * 连接成功响应
     */
    @OnOpen
    public void openSession(@PathParam("fileId") String fileId, Session session) {
        ONLINE_EXPORT_EXCEL_SESSIONS.put(fileId, session);
        RemoteEndpoint.Async async = session.getAsyncRemote();
        if (async != null) {
            async.sendText(JSON.toJSONString(new SocketMsgDTO(fileId, "", MsgType.CONNECT.name(), MsgType.CONNECT.name())));
            session.setMaxIdleTimeout(180000);
        }
        LogUtils.info("客户端: [" + fileId + "] : 连接成功！" + ExportWebSocketHandler.ONLINE_EXPORT_EXCEL_SESSIONS.size(), fileId);
    }

    /**
     * 收到消息响应
     */
    @OnMessage
    public void onMessage(@PathParam("fileId") String fileId, String message) {
        LogUtils.info("服务器收到：[" + fileId + "] : " + message);
        SocketMsgDTO dto = JSON.parseObject(message, SocketMsgDTO.class);
        ExportWebSocketHandler.sendMessageSingle(dto);
    }

    /**
     * 连接关闭响应
     */
    @OnClose
    public void onClose(@PathParam("fileId") String fileId, Session session) throws IOException {
        //当前的Session 移除
        ExportWebSocketHandler.ONLINE_EXPORT_EXCEL_SESSIONS.remove(fileId);
        LogUtils.info("[" + fileId + "] : 断开连接！" + ExportWebSocketHandler.ONLINE_EXPORT_EXCEL_SESSIONS.size());
        //并且通知其他人当前用户已经断开连接了
        session.close();
    }

    /**
     * 连接异常响应
     */
    @OnError
    public void onError(Session session, Throwable throwable) throws IOException {
        LogUtils.error("连接异常响应", throwable);
        session.close();
    }

    /**
     * 每一分钟群发一次心跳检查
     */
    @Scheduled(fixedRate = 60000)
    public void heartbeatCheck() {
        ExportWebSocketHandler.sendMessageSingle(
                new SocketMsgDTO(MsgType.HEARTBEAT.name(), MsgType.HEARTBEAT.name(), MsgType.HEARTBEAT.name(), "heartbeat check")
        );
    }
}
