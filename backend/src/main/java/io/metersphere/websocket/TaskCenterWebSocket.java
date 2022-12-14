package io.metersphere.websocket;

import com.alibaba.fastjson.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.task.dto.TaskCenterRequest;
import io.metersphere.task.dto.TaskStatisticsDTO;
import io.metersphere.task.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/task/center/count/running/{userId}/{triggerMode}")
@Component
public class TaskCenterWebSocket {
    private static TaskService taskService;
    private static ConcurrentHashMap<Session, Timer> refreshTasks = new ConcurrentHashMap<>();
    private final static String ALL = "ALL";

    @Resource
    public void setTaskService(TaskService taskService) {
        TaskCenterWebSocket.taskService = taskService;
    }

    /**
     * 开启连接的操作
     */
    @OnOpen
    public void onOpen(@PathParam("userId") String userId,
                       @PathParam("triggerMode") String triggerMode,
                       Session session) {
        Timer timer = new Timer(true);
        TaskCenterWebSocket.TaskCenter task = new TaskCenterWebSocket.TaskCenter(session, userId,triggerMode);
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
    public void onMessage(@PathParam("userId") String userId,
                          @PathParam("triggerMode") String triggerMode,
                          Session session, String message) {
        int refreshTime = 10;
        try {
            refreshTime = Integer.parseInt(message);
        } catch (Exception e) {
        }
        try {
            Timer timer = refreshTasks.get(session);
            timer.cancel();

            Timer newTimer = new Timer(true);
            newTimer.schedule(new TaskCenter(session, userId, triggerMode), 0, refreshTime * 1000L);
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

    public static class TaskCenter extends TimerTask {
        private Session session;
        private TaskCenterRequest request;

        TaskCenter(Session session, String userId, String triggerMode) {
            this.session = session;
            TaskCenterRequest request = new TaskCenterRequest();
            if (!StringUtils.equals(triggerMode, ALL)) {
                request.setTriggerMode(triggerMode);
            }
            request.setUserId(userId);
            request.setExecutor(userId);
            this.request = request;
        }

        @Override
        public void run() {
            try {
                TaskStatisticsDTO task = taskService.getRunningTasks(request);
                if (!session.isOpen()) {
                    return;
                }
                session.getBasicRemote().sendText(JSON.toJSONString(task));
                if (task.getTotal() == 0) {
                    session.getBasicRemote().sendText(JSON.toJSONString(task));
                    session.close();
                }
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        }
    }
}
