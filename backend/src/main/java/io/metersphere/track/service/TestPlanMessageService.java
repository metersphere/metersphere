package io.metersphere.track.service;

import io.metersphere.api.service.ShareInfoService;
import io.metersphere.base.domain.TestPlan;
import io.metersphere.base.domain.TestPlanReport;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.TestPlanReportStatus;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.ProjectService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.service.UserService;
import io.metersphere.track.dto.TestPlanDTOWithMetric;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class TestPlanMessageService {
    @Resource
    private ProjectService projectService;
    @Lazy
    @Resource
    private  TestPlanService testPlanService;
    @Resource
    private UserService userService;
    @Resource
    private ShareInfoService shareInfoService;

    @Async
    public void sendMessage(TestPlan testPlan, TestPlanReport testPlanReport, String projectId) {
        assert testPlan != null;
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
        assert systemParameterService != null;
        assert noticeSendService != null;
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String url = baseSystemConfigDTO.getUrl() + "/#/track/testPlan/reportList";
        String subject = "";
        String successContext = "${operator}执行的 ${name} 测试计划运行成功, 报告: ${planShareUrl}";
        String failedContext = "${operator}执行的 ${name} 测试计划运行失败, 报告: ${planShareUrl}";
        String context = "${operator}完成了测试计划: ${name}, 报告: ${planShareUrl}";
        if (StringUtils.equals(testPlanReport.getTriggerMode(), ReportTriggerMode.API.name())) {
            subject = Translator.get("task_notification_jenkins");
        } else {
            subject = Translator.get("task_notification");
        }
        // 计算通过率
        TestPlanDTOWithMetric testPlanDTOWithMetric = BeanUtils.copyBean(new TestPlanDTOWithMetric(), testPlan);
        testPlanService.calcTestPlanRate(Collections.singletonList(testPlanDTOWithMetric));

        String creator = testPlanReport.getCreator();
        UserDTO userDTO = userService.getUserDTO(creator);

        Map paramMap = new HashMap();
        paramMap.put("type", "testPlan");
        paramMap.put("url", url);
        paramMap.put("projectId", projectId);
        if (userDTO != null) {
            paramMap.put("operator", userDTO.getName());
        }
        paramMap.putAll(new BeanMap(testPlanDTOWithMetric));

        String testPlanShareUrl = shareInfoService.getTestPlanShareUrl(testPlanReport.getId(), creator);
        paramMap.put("planShareUrl", baseSystemConfigDTO.getUrl() + "/sharePlanReport" + testPlanShareUrl);

        /**
         * 测试计划的消息通知配置包括 完成、成功、失败
         * 所以发送通知时必定会有"完成"状态的通知
         */
        Map<String, String> execStatusEventMap = new HashMap<>();
        execStatusEventMap.put(TestPlanReportStatus.COMPLETED.name(), NoticeConstants.Event.COMPLETE);
        if (StringUtils.equalsIgnoreCase(testPlanReport.getStatus(), TestPlanReportStatus.SUCCESS.name())) {
            execStatusEventMap.put(testPlanReport.getStatus(), NoticeConstants.Event.EXECUTE_SUCCESSFUL);
        } else if (StringUtils.equalsIgnoreCase(testPlanReport.getStatus(), TestPlanReportStatus.FAILED.name())) {
            execStatusEventMap.put(testPlanReport.getStatus(), NoticeConstants.Event.EXECUTE_FAILED);
        } else if (!StringUtils.equalsIgnoreCase(testPlanReport.getStatus(), TestPlanReportStatus.COMPLETED.name())) {
            execStatusEventMap.put(testPlanReport.getStatus(), NoticeConstants.Event.COMPLETE);
        }
        for (Map.Entry<String, String> entry : execStatusEventMap.entrySet()) {
            String status = entry.getKey();
            String event = entry.getValue();
            NoticeModel noticeModel = NoticeModel.builder()
                    .operator(creator)
                    .context(context)
                    .successContext(successContext)
                    .failedContext(failedContext)
                    .testId(testPlan.getId())
                    .status(status)
                    .event(event)
                    .subject(subject)
                    .paramMap(paramMap)
                    .build();

            if (StringUtils.equals(testPlanReport.getTriggerMode(), ReportTriggerMode.MANUAL.name())) {
                noticeSendService.send(projectService.getProjectById(projectId), NoticeConstants.TaskType.TEST_PLAN_TASK, noticeModel);
            }

            if (StringUtils.equalsAny(testPlanReport.getTriggerMode(), ReportTriggerMode.SCHEDULE.name(), ReportTriggerMode.API.name())) {
                noticeSendService.send(testPlanReport.getTriggerMode(), NoticeConstants.TaskType.TEST_PLAN_TASK, noticeModel);
            }
        }
    }
}
