package io.metersphere.websocket.c.to.c;

import com.alibaba.fastjson.JSON;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.websocket.c.to.c.util.MsgDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@Slf4j
@Component // 注册到spring
@ServerEndpoint("/ws/{reportId}") // 创建websocket服务
public class IndexWebSocket {

    /**
     * 连接成功响应
     */
    @OnOpen
    public void openSession(@PathParam("reportId") String reportId, Session session) {
        WebSocketUtils.ONLINE_USER_SESSIONS.put(reportId, session);
        RemoteEndpoint.Async async = session.getAsyncRemote();
        if (async != null) {
            async.sendText("CONN_SUCCEEDED");
        }
        LoggerUtil.info("客户端: [" + reportId + "] : 连接成功！" + WebSocketUtils.ONLINE_USER_SESSIONS.size(), reportId);
    }

    /**
     * 收到消息响应
     */
    @OnMessage
    public void onMessage(@PathParam("reportId") String reportId, String message) {
        LoggerUtil.info("服务器收到：[" + reportId + "] : " + message);
        MsgDto dto = JSON.parseObject(message, MsgDto.class);
        dto.setContent(dto.getContent());
        WebSocketUtils.sendMessageSingle(dto);
    }

    /**
     * 连接关闭响应
     */
    @OnClose
    public void onClose(@PathParam("reportId") String reportId, Session session) throws IOException {
        //当前的Session 移除
        WebSocketUtils.ONLINE_USER_SESSIONS.remove(reportId);
        LoggerUtil.info("[" + reportId + "] : 断开连接！" + WebSocketUtils.ONLINE_USER_SESSIONS.size());
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
