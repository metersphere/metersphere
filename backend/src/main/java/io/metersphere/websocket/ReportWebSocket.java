package io.metersphere.websocket;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.performance.service.PerformanceReportService;
import io.metersphere.performance.service.PerformanceTestService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/performance/report/{reportId}")
@Component
public class ReportWebSocket {

    private static PerformanceReportService performanceReportService;
    private static PerformanceTestService performanceTestService;
    private static ConcurrentHashMap<Session, Timer> refreshTasks = new ConcurrentHashMap<>();

    @Resource
    public void setReportService(PerformanceReportService performanceReportService) {
        ReportWebSocket.performanceReportService = performanceReportService;
    }

    @Resource
    public void setPerformanceTestService(PerformanceTestService performanceTestService) {
        ReportWebSocket.performanceTestService = performanceTestService;
    }

    /**
     * 开启连接的操作
     */
    @OnOpen
    public void onOpen(@PathParam("reportId") String reportId, Session session) {
        Timer timer = new Timer(true);
        ReportTask task = new ReportTask(session, reportId);
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
     * 给服务器发送消息告知数据库发生变化
     */
    @OnMessage
    public void onMessage(@PathParam("reportId") String reportId, Session session, String message) {
        int refreshTime = 10;
        try {
            refreshTime = Integer.parseInt(message);
        } catch (Exception e) {
        }
        try {
            Timer timer = refreshTasks.get(session);
            timer.cancel();

            Timer newTimer = new Timer(true);
            newTimer.schedule(new ReportTask(session, reportId), 0, refreshTime * 1000L);
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

    public static class ReportTask extends TimerTask {
        private Session session;
        private String reportId;

        ReportTask(Session session, String reportId) {
            this.session = session;
            this.reportId = reportId;
        }

        @Override
        public void run() {
            try {
                LoadTestReportWithBLOBs report = performanceReportService.getReport(reportId);
                if (report == null || StringUtils.equalsAny(report.getStatus(), PerformanceTestStatus.Completed.name())) {
                    session.close();
                }
                if (StringUtils.equals(report.getStatus(), PerformanceTestStatus.Error.name())) {
                    session.getBasicRemote().sendText("Error: " + report.getDescription());
                    performanceTestService.stopErrorTest(reportId);
                    session.close();
                }
                if (!session.isOpen()) {
                    return;
                }
                if (StringUtils.equalsAny(report.getStatus(),
                        PerformanceTestStatus.Starting.name(),
                        PerformanceTestStatus.Running.name(),
                        PerformanceTestStatus.Reporting.name())
                ) {
                    session.getBasicRemote().sendText("refresh-" + Math.random());
                }
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        }
    }
}