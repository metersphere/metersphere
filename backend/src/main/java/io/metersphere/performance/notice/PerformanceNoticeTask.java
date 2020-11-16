package io.metersphere.performance.notice;

import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.MessageSettingDetail;
import io.metersphere.notice.service.DingTaskService;
import io.metersphere.notice.service.MailService;
import io.metersphere.notice.service.NoticeService;
import io.metersphere.notice.service.WxChatTaskService;
import io.metersphere.service.SystemParameterService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class PerformanceNoticeTask {
    @Resource
    private NoticeService noticeService;
    @Resource
    private DingTaskService dingTaskService;
    @Resource
    private WxChatTaskService wxChatTaskService;
    @Resource
    private MailService mailService;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;

    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    private boolean isRunning=false;

    /*@PreDestroy
    public void preDestroy() {
        isRunning = false;
    }*/

    public void registerNoticeTask(LoadTestReportWithBLOBs loadTestReport) {
        isRunning=true;
        executorService.submit(() -> {
            LogUtil.info("性能测试定时任务");
            while (isRunning) {
                LoadTestReportWithBLOBs loadTestReportFromDatabase = loadTestReportMapper.selectByPrimaryKey(loadTestReport.getId());
                if (StringUtils.equals(loadTestReportFromDatabase.getStatus(), PerformanceTestStatus.Completed.name())) {
                    sendSuccessNotice(loadTestReportFromDatabase);
                    isRunning=false;
                }
                if (StringUtils.equals(loadTestReportFromDatabase.getStatus(), PerformanceTestStatus.Error.name())) {
                    sendFailNotice(loadTestReportFromDatabase);
                    isRunning=false;
                }
                try {
                    //查询定时任务是否关闭
                    Thread.sleep(1000 * 30);// 每分钟检查 loadtest 的状态
                } catch (InterruptedException e) {
                    LogUtil.error(e);
                }
            }
        });
    }

    public void sendSuccessNotice(LoadTestReportWithBLOBs loadTestReport) {
        List<String> userIds = new ArrayList<>();
        List<MessageDetail> taskList = new ArrayList<>();
        String successContext = "";
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String url = baseSystemConfigDTO.getUrl() + "/#/performance/report/view/" + loadTestReport.getId();
        if (StringUtils.equals(NoticeConstants.API, loadTestReport.getTriggerMode())) {
            MessageSettingDetail messageSettingDetail = noticeService.searchMessage();
            taskList = messageSettingDetail.getJenkinsTask();
            successContext = "LoadJenkins任务通知:" + loadTestReport.getName() + "执行成功" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
        }
        if (StringUtils.equals(NoticeConstants.SCHEDULE, loadTestReport.getTriggerMode())) {
            taskList = noticeService.searchMessageSchedule(loadTestReport.getTestId());
            successContext = "Load定时任务通知:" + loadTestReport.getName() + "执行成功" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
        }
        String finalSuccessContext = successContext;
        taskList.forEach(r -> {
            switch (r.getType()) {
                case NoticeConstants.NAIL_ROBOT:
                    if (StringUtils.equals(NoticeConstants.EXECUTE_SUCCESSFUL, r.getEvent()) && StringUtils.equals(loadTestReport.getStatus(), PerformanceTestStatus.Completed.name())) {
                        dingTaskService.sendNailRobot(r, userIds, finalSuccessContext, NoticeConstants.EXECUTE_SUCCESSFUL);
                    }
                    break;
                case NoticeConstants.WECHAT_ROBOT:
                    if (StringUtils.equals(NoticeConstants.EXECUTE_SUCCESSFUL, r.getEvent()) && StringUtils.equals(loadTestReport.getStatus(), PerformanceTestStatus.Completed.name())) {
                        wxChatTaskService.sendWechatRobot(r, userIds, finalSuccessContext, NoticeConstants.EXECUTE_SUCCESSFUL);
                    }
                    break;
                case NoticeConstants.EMAIL:
                    if (StringUtils.equals(NoticeConstants.EXECUTE_SUCCESSFUL, r.getEvent()) && StringUtils.equals(loadTestReport.getStatus(), PerformanceTestStatus.Completed.name())) {
                        mailService.sendLoadNotification(r, loadTestReport, NoticeConstants.EXECUTE_SUCCESSFUL);
                    }
                    break;
            }

        });
    }


    public void sendFailNotice(LoadTestReportWithBLOBs loadTestReport) {
        List<String> userIds = new ArrayList<>();
        List<MessageDetail> taskList = new ArrayList<>();
        String failedContext = "";
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String url = baseSystemConfigDTO.getUrl() + "/#/performance/report/view/" + loadTestReport.getId();
        if (StringUtils.equals(NoticeConstants.API, loadTestReport.getTriggerMode())) {
            MessageSettingDetail messageSettingDetail = noticeService.searchMessage();
            taskList = messageSettingDetail.getJenkinsTask();
            failedContext = "LoadJenkins任务通知:" + loadTestReport.getName() + "执行失败" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
        }
        if (StringUtils.equals(NoticeConstants.SCHEDULE, loadTestReport.getTriggerMode())) {
            taskList = noticeService.searchMessageSchedule(loadTestReport.getTestId());
            failedContext = "Load定时任务通知:" + loadTestReport.getName() + "执行失败" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
        }
        String finalFailedContext = failedContext;
        taskList.forEach(r -> {
            switch (r.getType()) {
                case NoticeConstants.NAIL_ROBOT:
                    if (StringUtils.equals(NoticeConstants.EXECUTE_FAILED, r.getEvent()) && StringUtils.equals(loadTestReport.getStatus(), PerformanceTestStatus.Error.name())) {
                        dingTaskService.sendNailRobot(r, userIds, finalFailedContext, NoticeConstants.EXECUTE_FAILED);
                    }
                    break;
                case NoticeConstants.WECHAT_ROBOT:
                    if (StringUtils.equals(NoticeConstants.EXECUTE_FAILED, r.getEvent()) && StringUtils.equals(loadTestReport.getStatus(), PerformanceTestStatus.Error.name())) {
                        wxChatTaskService.sendWechatRobot(r, userIds, finalFailedContext, NoticeConstants.EXECUTE_FAILED);
                    }
                    break;
                case NoticeConstants.EMAIL:
                    if (StringUtils.equals(NoticeConstants.EXECUTE_FAILED, r.getEvent()) && StringUtils.equals(loadTestReport.getStatus(), PerformanceTestStatus.Error.name())) {
                        mailService.sendLoadNotification(r, loadTestReport, NoticeConstants.EXECUTE_FAILED);
                    }
                    break;
            }

        });
    }

}