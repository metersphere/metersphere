package io.metersphere.api.socket;

import io.metersphere.sdk.constants.MsgType;
import io.metersphere.sdk.dto.SocketMsgDTO;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.sdk.util.WebSocketUtils;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ServerEndpoint("/ws/api/{reportId}")
public class WebSocketHandler {
    /**
     * 连接成功响应
     */
    @OnOpen
    public void openSession(@PathParam("reportId") String reportId, Session session) {
        WebSocketUtils.ONLINE_USER_SESSIONS.put(reportId, session);
        RemoteEndpoint.Async async = session.getAsyncRemote();
        if (async != null) {
            async.sendText(JSON.toJSONString(new SocketMsgDTO(reportId, "", MsgType.CONNECT.name(), MsgType.CONNECT.name())));
            session.setMaxIdleTimeout(180000);
        }
        LogUtils.info("客户端: [" + reportId + "] : 连接成功！" + WebSocketUtils.ONLINE_USER_SESSIONS.size(), reportId);
    }

    /**
     * 收到消息响应
     */
    @OnMessage
    public void onMessage(@PathParam("reportId") String reportId, String message) {
        LogUtils.info("服务器收到：[" + reportId + "] : " + message);
        SocketMsgDTO dto = JSON.parseObject(message, SocketMsgDTO.class);
        WebSocketUtils.sendMessageSingle(dto);
    }

    /**
     * 连接关闭响应
     */
    @OnClose
    public void onClose(@PathParam("reportId") String reportId, Session session) throws IOException {
        //当前的Session 移除
        WebSocketUtils.ONLINE_USER_SESSIONS.remove(reportId);
        LogUtils.info("[" + reportId + "] : 断开连接！" + WebSocketUtils.ONLINE_USER_SESSIONS.size());
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
        WebSocketUtils.sendMessageAll(
                new SocketMsgDTO(MsgType.HEARTBEAT.name(), MsgType.HEARTBEAT.name(), MsgType.HEARTBEAT.name(), "heartbeat check")
        );
    }
}
