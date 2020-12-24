package io.metersphere.performance.notice;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.SystemParameterService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class PerformanceNoticeTask {
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private NoticeSendService noticeSendService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    private boolean isRunning = false;

    @PreDestroy
    public void preDestroy() {
        isRunning = false;
    }

    public void registerNoticeTask(LoadTestReportWithBLOBs loadTestReport) {
        isRunning = true;
        executorService.submit(() -> {
            LogUtil.info("性能测试定时任务");
            while (isRunning) {
                LoadTestReportWithBLOBs loadTestReportFromDatabase = loadTestReportMapper.selectByPrimaryKey(loadTestReport.getId());
                if (StringUtils.equalsAny(loadTestReportFromDatabase.getStatus(),
                        PerformanceTestStatus.Completed.name(), PerformanceTestStatus.Error.name())) {
                    sendNotice(loadTestReportFromDatabase);
                    isRunning = false;
                }
                try {
                    //查询定时任务是否关闭
                    Thread.sleep(1000 * 10);// 检查 loadtest 的状态
                } catch (InterruptedException e) {
                    LogUtil.error(e.getMessage(), e);
                }
            }
        });
    }

    public void sendNotice(LoadTestReportWithBLOBs loadTestReport) {
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String url = baseSystemConfigDTO.getUrl() + "/#/performance/report/view/" + loadTestReport.getId();
        String successContext = "";
        String failedContext = "";
        String subject = "";
        String event = "";
        if (StringUtils.equals(NoticeConstants.Mode.API, loadTestReport.getTriggerMode())) {
            successContext = "性能测试 API任务通知:" + loadTestReport.getName() + "执行成功" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
            failedContext = "性能测试 API任务通知:" + loadTestReport.getName() + "执行失败" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
            subject = Translator.get("task_notification_jenkins");
        }
        if (StringUtils.equals(NoticeConstants.Mode.SCHEDULE, loadTestReport.getTriggerMode())) {
            successContext = "性能测试定时任务通知:" + loadTestReport.getName() + "执行成功" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
            failedContext = "性能测试定时任务通知:" + loadTestReport.getName() + "执行失败" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
            subject = Translator.get("task_notification");
        }

        if (PerformanceTestStatus.Completed.name().equals(loadTestReport.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
        }
        if (PerformanceTestStatus.Error.name().equals(loadTestReport.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_FAILED;
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("testName", loadTestReport.getName());
        paramMap.put("id", loadTestReport.getId());
        paramMap.put("type", "performance");
        paramMap.put("status", loadTestReport.getStatus());
        paramMap.put("url", baseSystemConfigDTO.getUrl());
        NoticeModel noticeModel = NoticeModel.builder()
                .successContext(successContext)
                .successMailTemplate("PerformanceApiSuccessNotification")
                .failedContext(failedContext)
                .failedMailTemplate("PerformanceFailedNotification")
                .testId(loadTestReport.getTestId())
                .status(loadTestReport.getStatus())
                .subject(subject)
                .event(event)
                .paramMap(paramMap)
                .build();
        noticeSendService.send(loadTestReport.getTriggerMode(), noticeModel);
    }
}