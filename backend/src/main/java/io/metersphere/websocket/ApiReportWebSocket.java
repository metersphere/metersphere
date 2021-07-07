package io.metersphere.websocket;

import com.alibaba.nacos.client.utils.StringUtils;
import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.commons.utils.LogUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/api/definition/run/report/{reportId}/{runMode}")
@Component
public class ApiReportWebSocket {

    private static ApiDefinitionService apiDefinitionService;
    private ConcurrentHashMap<Session, Timer> refreshTasks = new ConcurrentHashMap<>();

    @Resource
    public void setReportService(ApiDefinitionService apiDefinitionService) {
        ApiReportWebSocket.apiDefinitionService = apiDefinitionService;
    }

    /**
     * 开启连接的操作
     */
    @OnOpen
    public void onOpen(@PathParam("reportId") String reportId, @PathParam("runMode") String runMode, Session session) {
        Timer timer = new Timer(true);
        ApiReportTask task = new ApiReportTask(session, reportId, runMode);
        timer.schedule(task, 0, 1000);
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
     * 给服务器发送消息告知数据库发生变化
     */
    @OnMessage
    public void onMessage(@PathParam("reportId") String reportId, @PathParam("runMode") String runMode, Session session, String message) {
        try {
            LogUtil.info(message);
            Timer timer = refreshTasks.get(session);
            if (timer != null) {
                timer.cancel();
            }
            Timer newTimer = new Timer(true);
            newTimer.schedule(new ApiReportTask(session, reportId, runMode), 0, 1000L);
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

    public static class ApiReportTask extends TimerTask {
        private Session session;
        private String reportId;
        private String runMode;

        ApiReportTask(Session session, String reportId, String runMode) {
            this.session = session;
            this.reportId = reportId;
            this.runMode = runMode;
        }

        @Override
        public void run() {
            try {
                APIReportResult report = null;
                if (StringUtils.equals(runMode, "debug")) {
                    report = apiDefinitionService.getResult(reportId, runMode);
                } else {
                    report = apiDefinitionService.getReportById(reportId);
                }
                if (!session.isOpen()) {
                    return;
                }
                if (report != null) {
                    session.getBasicRemote().sendText(report.getContent());
                    session.close();
                }
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        }
    }
}