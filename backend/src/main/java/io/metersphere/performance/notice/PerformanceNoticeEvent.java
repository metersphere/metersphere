package io.metersphere.performance.notice;

import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.domain.Organization;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.consumer.LoadTestFinishEvent;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.ProjectService;
import io.metersphere.service.SystemParameterService;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class PerformanceNoticeEvent implements LoadTestFinishEvent {
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private ProjectService projectService;

    public void sendNotice(LoadTestReport loadTestReport) {

        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String url = baseSystemConfigDTO.getUrl() + "/#/performance/report/view/" + loadTestReport.getId();
        String successContext = "";
        String failedContext = "";
        String subject = "";
        String event = "";
        if (StringUtils.equals(ReportTriggerMode.API.name(), loadTestReport.getTriggerMode())) {
            successContext = "性能测试 API任务通知:" + loadTestReport.getName() + "执行成功" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
            failedContext = "性能测试 API任务通知:" + loadTestReport.getName() + "执行失败" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
            subject = Translator.get("task_notification_jenkins");
        }
        if (StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), loadTestReport.getTriggerMode())) {
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
        Map paramMap = new HashMap<>();
        paramMap.put("operator", loadTestReport.getUserId());
        paramMap.put("type", "performance");
        paramMap.put("url", baseSystemConfigDTO.getUrl());
        paramMap.putAll(new BeanMap(loadTestReport));
        NoticeModel noticeModel = NoticeModel.builder()
                .operator(loadTestReport.getUserId())
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

        Organization organization = projectService.getOrganizationByProjectId(loadTestReport.getProjectId());

        if (StringUtils.equals(ReportTriggerMode.API.name(), loadTestReport.getTriggerMode())
                || StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), loadTestReport.getTriggerMode())) {
            noticeSendService.send(loadTestReport.getTriggerMode(), noticeModel);
        } else {
            String context = "${operator}执行了性能测试: ${name}, 执行结果: ${status}";
            NoticeModel noticeModel2 = NoticeModel.builder()
                    .operator(loadTestReport.getUserId())
                    .context(context)
                    .mailTemplate("performance/TestResult")
                    .testId(loadTestReport.getTestId())
                    .status(loadTestReport.getStatus())
                    .subject(subject)
                    .event(NoticeConstants.Event.EXECUTE_COMPLETED)
                    .paramMap(paramMap)
                    .build();
            noticeSendService.send(organization, NoticeConstants.TaskType.PERFORMANCE_TEST_TASK, noticeModel2);
        }
    }

    @Override
    public void execute(LoadTestReport loadTestReport) {
        LogUtil.info("PerformanceNoticeEvent OVER:" + loadTestReport.getTriggerMode() + ";" + loadTestReport.getStatus() + ";" + loadTestReport.getName());
        if (StringUtils.equalsAny(loadTestReport.getStatus(),
                PerformanceTestStatus.Completed.name(), PerformanceTestStatus.Error.name())) {
            sendNotice(loadTestReport);
        }
    }
}