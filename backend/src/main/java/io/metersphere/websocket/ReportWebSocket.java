package io.metersphere.websocket;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.performance.service.ReportService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint("/performance/report/{reportId}")
@Component
public class ReportWebSocket {

    private static ReportService reportService;
    private static PerformanceTestService performanceTestService;

    @Resource
    public void setReportService(ReportService reportService) {
        ReportWebSocket.reportService = reportService;
    }

    @Resource
    public void setPerformanceTestService(PerformanceTestService performanceTestService) {
        ReportWebSocket.performanceTestService = performanceTestService;
    }

    /**
     * 开启连接的操作
     */
    @OnOpen
    public void onOpen(@PathParam("reportId") String reportId, Session session) throws IOException {
        //开启一个线程对数据库中的数据进行轮询
        ReportThread reportThread = new ReportThread(session, reportId);
        Thread thread = new Thread(reportThread);
        thread.start();
    }

    /**
     * 连接关闭的操作
     */
    @OnClose
    public void onClose(Session session) {

    }

    /**
     * 给服务器发送消息告知数据库发生变化
     */
    @OnMessage
    public void onMessage(Session session, String message) {
    }

    /**
     * 出错的操作
     */
    @OnError
    public void onError(Throwable error) {
        System.out.println(error);
        error.printStackTrace();
    }

    public static class ReportThread implements Runnable {
        private boolean stopMe = true;
        private final String reportId;
        private final Session session;
        private int refresh;

        public ReportThread(Session session, String reportId) {
            this.session = session;
            this.reportId = reportId;
            this.refresh = 0;
        }

        public void stopMe() {
            stopMe = false;
        }

        public void run() {
            while (stopMe) {
                try {
                    LoadTestReportWithBLOBs report = reportService.getReport(reportId);
                    if (report == null || StringUtils.equalsAny(report.getStatus(), PerformanceTestStatus.Completed.name())) {
                        this.stopMe();
                        session.close();
                        break;
                    }
                    if (StringUtils.equals(report.getStatus(), PerformanceTestStatus.Error.name())) {
                        this.stopMe();
                        session.getBasicRemote().sendText("Error: " + report.getDescription());
                        performanceTestService.stopErrorTest(reportId);
                        session.close();
                        break;
                    }
                    if (!session.isOpen()) {
                        return;
                    }
                    if (StringUtils.equalsAny(report.getStatus(), PerformanceTestStatus.Running.name(), PerformanceTestStatus.Reporting.name())) {
                        session.getBasicRemote().sendText("refresh-" + this.refresh++);
                    }
                    Thread.sleep(20 * 1000L);
                } catch (Exception e) {
                    LogUtil.error(e.getMessage(), e);
                }
            }
        }
    }
}