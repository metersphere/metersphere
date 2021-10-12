package io.metersphere.websocket;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.Notification;
import io.metersphere.commons.constants.NotificationConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.service.NotificationService;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/notification/count/{userId}")
@Component
public class NotificationWebSocket {
    private static NotificationService notificationService;
    private static ConcurrentHashMap<Session, Timer> refreshTasks = new ConcurrentHashMap<>();

    @Resource
    public void setNotificationService(NotificationService notificationService) {
        NotificationWebSocket.notificationService = notificationService;
    }

    /**
     * 开启连接的操作
     */
    @OnOpen
    public void onOpen(@PathParam("userId") String userId, Session session) {
        Timer timer = new Timer(true);
        NotificationCenter task = new NotificationCenter(session, userId);
        timer.schedule(task, 0, 10 * 1000);
        refreshTasks.putIfAbsent(session, timer);
    }

    /**
     * 连接关闭的操作
     */
    @OnClose
    public void onClose(Session session) {
        Timer timer = refreshTasks.get(session);
        if (timer != null) {
            timer.cancel();
            refreshTasks.remove(session);
        }
    }

    /**
     * 推送消息
     */
    @OnMessage
    public void onMessage(@PathParam("userId") String userId, Session session, String message) {
        int refreshTime = 10;
        try {
            refreshTime = Integer.parseInt(message);
        } catch (Exception e) {
        }
        try {
            Timer timer = refreshTasks.get(session);
            timer.cancel();

            Timer newTimer = new Timer(true);
            newTimer.schedule(new NotificationCenter(session, userId), 0, refreshTime * 1000L);
            refreshTasks.put(session, newTimer);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 出错的操作
     */
    @OnError
    public void onError(Throwable error) {
        System.out.println(error);
        error.printStackTrace();
    }

    public static class NotificationCenter extends TimerTask {
        private Session session;
        private String userId;

        NotificationCenter(Session session, String userId) {
            this.session = session;
            this.userId = userId;
        }

        @Override
        public void run() {
            try {
                if (!session.isOpen()) {
                    return;
                }
                Notification notification = new Notification();
                notification.setReceiver(userId);
                notification.setStatus(NotificationConstants.Status.UNREAD.name());
                int count = notificationService.countNotification(notification);
                NotificationMessage message = NotificationMessage.builder()
                        .count(count)
                        .now(System.currentTimeMillis())
                        .build();
                session.getBasicRemote().sendText(JSON.toJSONString(message));
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        }
    }

    @Data
    @Builder
    public static class NotificationMessage {
        private Integer count;
        private Long now;
    }
}
