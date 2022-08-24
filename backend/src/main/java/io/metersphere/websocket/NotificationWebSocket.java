package io.metersphere.websocket;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.Notification;
import io.metersphere.commons.constants.NotificationConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.service.NotificationService;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/notification/count/{userId}")
@Component
public class NotificationWebSocket {
    private static NotificationService notificationService;
    private static final ConcurrentHashMap<String, Timer> refreshTasks = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Set<Session>> sessionMap = new ConcurrentHashMap<>();

    @Resource
    public void setNotificationService(NotificationService notificationService) {
        NotificationWebSocket.notificationService = notificationService;
    }

    /**
     * 开启连接的操作
     */
    @OnOpen
    public void onOpen(@PathParam("userId") String userId, Session session) {
        LogUtil.info("建立一个socket链接: {} - {}", userId, session.getId());
        Timer timer = refreshTasks.get(userId);
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer(true);
        NotificationCenter task = new NotificationCenter(userId);
        timer.schedule(task, 0, 10 * 1000);
        refreshTasks.put(userId, timer);

        Set<Session> sessions = sessionMap.getOrDefault(userId, new HashSet<>());
        sessions.add(session);
        sessionMap.put(userId, sessions);

        LogUtil.info("在线socket链接: {}, size: {}", userId, sessions.size());
    }

    /**
     * 连接关闭的操作
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId, Session session) {
        // 删除当前session
        Set<Session> sessions = sessionMap.get(userId);
        if (CollectionUtils.isNotEmpty(sessions)) {
            LogUtil.info("剔除一个socket链接: {} - {}", userId, session.getId());
            sessions.remove(session);
        }

        // 没有在线用户了
        if (CollectionUtils.isEmpty(sessions)) {
            LogUtil.info("关闭socket: {} ", userId);

            Timer timer = refreshTasks.get(userId);
            if (timer != null) {
                timer.cancel();
                refreshTasks.remove(userId);
            }
        }
    }

    /**
     * 推送消息
     */
    @OnMessage
    public void onMessage(@PathParam("userId") String userId, Session session, String message) {
        LogUtil.info(message);
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
        private String userId;

        NotificationCenter(String userId) {
            this.userId = userId;
        }

        @Override
        public void run() {
            try {
                Notification notification = new Notification();
                notification.setReceiver(userId);
                notification.setStatus(NotificationConstants.Status.UNREAD.name());
                int count = notificationService.countNotification(notification);
                NotificationMessage message = NotificationMessage.builder()
                        .count(count)
                        .now(System.currentTimeMillis())
                        .build();

                Set<Session> sessions = sessionMap.get(userId);
                if (CollectionUtils.isNotEmpty(sessions)) {
                    sessions.forEach(session -> {
                        try {
                            session.getBasicRemote().sendText(JSON.toJSONString(message));
                        } catch (IOException e) {
                            LogUtil.error(e.getMessage(), e);
                        }
                    });
                }
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
