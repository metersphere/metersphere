package io.metersphere.performance.notice;

import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.domain.Project;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.consumer.LoadTestFinishEvent;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.service.ProjectService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.UserService;
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
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private UserService userService;

    public void sendNotice(LoadTestReport loadTestReport) {

        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String reportUrl = baseSystemConfigDTO.getUrl() + "/#/performance/report/view/" + loadTestReport.getId();
        String subject = "";
        String event = "";
        String successContext = "${operator}执行性能测试成功: ${name}, 报告: ${reportUrl}";
        String failedContext = "${operator}执行性能测试失败: ${name}, 报告: ${reportUrl}";
        if (StringUtils.equals(ReportTriggerMode.API.name(), loadTestReport.getTriggerMode())) {
            subject = Translator.get("task_notification_jenkins");
        }
        if (StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), loadTestReport.getTriggerMode())) {
            subject = Translator.get("task_notification");
        }

        if (PerformanceTestStatus.Completed.name().equals(loadTestReport.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
        }
        if (PerformanceTestStatus.Error.name().equals(loadTestReport.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_FAILED;
        }

        LoadTestDTO loadTestDTO = performanceTestService.get(loadTestReport.getTestId());
        UserDTO userDTO = userService.getUserDTO(loadTestReport.getUserId());

        Map paramMap = new HashMap<>();
        paramMap.put("operator", userDTO.getName());
        paramMap.put("type", "performance");
        paramMap.put("url", baseSystemConfigDTO.getUrl());
        paramMap.put("reportUrl", reportUrl);
        paramMap.putAll(new BeanMap(loadTestDTO));


        if (StringUtils.equals(ReportTriggerMode.API.name(), loadTestReport.getTriggerMode())
                || StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), loadTestReport.getTriggerMode())) {
            NoticeModel noticeModel = NoticeModel.builder()
                    .operator(loadTestReport.getUserId())
                    .successContext(successContext)
                    .successMailTemplate("PerformanceSuccessNotification")
                    .failedContext(failedContext)
                    .failedMailTemplate("PerformanceFailedNotification")
                    .testId(loadTestReport.getTestId())
                    .status(loadTestReport.getStatus())
                    .subject(subject)
                    .event(event)
                    .paramMap(paramMap)
                    .build();
            noticeSendService.send(loadTestReport.getTriggerMode(), NoticeConstants.TaskType.PERFORMANCE_TEST_TASK, noticeModel);
        } else {
            Project project = projectService.getProjectById(loadTestReport.getProjectId());
            String context = "${operator}执行性能测试完成: ${name}";
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
            noticeSendService.send(project, NoticeConstants.TaskType.PERFORMANCE_TEST_TASK, noticeModel2);
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