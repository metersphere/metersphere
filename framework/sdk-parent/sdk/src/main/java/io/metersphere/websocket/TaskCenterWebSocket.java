package io.metersphere.websocket;

import com.mchange.lang.IntegerUtils;
import io.metersphere.commons.utils.JSON;
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

@ServerEndpoint("/websocket/task/running/{userId}/{triggerMode}/{hasLicense}")
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
                       @PathParam("hasLicense") boolean hasLicense, Session session) {
        Timer timer = new Timer(true);
        TaskCenter task = new TaskCenter(session, userId, triggerMode, hasLicense);
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
                          @PathParam("hasLicense") boolean hasLicense, Session session, String message) {
        try {
            Timer timer = refreshTasks.get(session);
            timer.cancel();
            int refreshTime = IntegerUtils.parseInt(message, 10);
            Timer newTimer = new Timer(true);
            newTimer.schedule(new TaskCenter(session, userId, triggerMode, hasLicense), 0, refreshTime * 1000L);
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

        TaskCenter(Session session, String userId, String triggerMode, boolean hasLicense) {
            this.session = session;
            TaskCenterRequest request = new TaskCenterRequest();
            if (!StringUtils.equals(triggerMode, ALL)) {
                request.setTriggerMode(triggerMode);
            }
            request.setUserId(userId);
            request.setExecutor(userId);
            request.setHasLicense(hasLicense);
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
