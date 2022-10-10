package io.metersphere.websocket;

import io.metersphere.commons.utils.JSON;
import io.metersphere.dto.MsgDTO;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.utils.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@Slf4j
@Component // 注册到spring
@ServerEndpoint("/websocket/plan/api/{reportId}") // 创建websocket服务
public class ExecReportWebSocket {
    /**
     * 连接成功响应
     */
    @OnOpen
    public void openSession(@PathParam("reportId") String reportId, Session session) {
        WebSocketUtil.ONLINE_USER_SESSIONS.put(reportId, session);
        RemoteEndpoint.Async async = session.getAsyncRemote();
        if (async != null) {
            async.sendText("CONN_SUCCEEDED");
        }
        LoggerUtil.info("客户端: [" + reportId + "] : 连接成功！" + WebSocketUtil.ONLINE_USER_SESSIONS.size(), reportId);
    }

    /**
     * 收到消息响应
     */
    @OnMessage
    public void onMessage(@PathParam("reportId") String reportId, String message) {
        LoggerUtil.info("服务器收到：[" + reportId + "] : " + message);
        MsgDTO dto = JSON.parseObject(message, MsgDTO.class);
        dto.setContent(dto.getContent());
        WebSocketUtil.sendMessageSingle(dto);
    }

    /**
     * 连接关闭响应
     */
    @OnClose
    public void onClose(@PathParam("reportId") String reportId, Session session) throws IOException {
        //当前的Session 移除
        WebSocketUtil.ONLINE_USER_SESSIONS.remove(reportId);
        LoggerUtil.info("[" + reportId + "] : 断开连接！" + WebSocketUtil.ONLINE_USER_SESSIONS.size());
        //并且通知其他人当前用户已经断开连接了
        session.close();
    }

    /**
     * 连接异常响应
     */
    @OnError
    public void onError(Session session, Throwable throwable) throws IOException {
        session.close();
    }
}
