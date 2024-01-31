package io.metersphere.api.service;

import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.mapper.ApiReportMapper;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ApiScenarioReportMapper;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ApiReportStatus;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.dto.api.notice.ApiNoticeDTO;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.BaseSystemConfigDTO;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.NoticeSendService;
import io.metersphere.system.service.SystemParameterService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ApiReportSendNoticeService {

    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiReportMapper apiReportMapper;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private ProjectMapper projectMapper;
    private static final String API_SCENARIO = "API_SCENARIO";

    public void sendNotice(ApiNoticeDTO noticeDTO) {
        String noticeType;
        String reportUrl;
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        assert systemParameterService != null;
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        BeanMap beanMap;
        String event;
        String status;
        ApiScenarioReport report = new ApiScenarioReport();
        if (API_SCENARIO.equals(noticeDTO.getResourceType())) {
            ApiScenario scenario = apiScenarioMapper.selectByPrimaryKey(noticeDTO.getResourceId());
            beanMap = new BeanMap(scenario);
            noticeType = NoticeConstants.TaskType.API_SCENARIO_TASK;
            reportUrl = baseSystemConfigDTO.getUrl() + "/#/api/automation/report/view/" + noticeDTO.getReportId();
            report = apiScenarioReportMapper.selectByPrimaryKey(noticeDTO.getReportId());
            if (StringUtils.endsWithIgnoreCase(noticeDTO.getReportStatus(), ApiReportStatus.SUCCESS.name())) {
                event = NoticeConstants.Event.SCENARIO_EXECUTE_SUCCESSFUL;
                status = "成功";
            } else if (StringUtils.endsWithIgnoreCase(noticeDTO.getReportStatus(), ApiReportStatus.FAKE_ERROR.name())) {
                event = NoticeConstants.Event.SCENARIO_EXECUTE_FAKE_ERROR;
                status = "误报";
            } else {
                event = NoticeConstants.Event.SCENARIO_EXECUTE_FAILED;
                status = "失败";
            }
        } else {
            ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(noticeDTO.getResourceId());
            beanMap = new BeanMap(testCase);

            // TODO 是否需要区分场景和用例
            noticeType = NoticeConstants.TaskType.API_DEFINITION_TASK;
            reportUrl = baseSystemConfigDTO.getUrl() + "/#/api/automation/report/view/" + noticeDTO.getReportId();

            ApiReport apiReport = apiReportMapper.selectByPrimaryKey(noticeDTO.getReportId());
            BeanUtils.copyBean(report, apiReport);
            if (StringUtils.endsWithIgnoreCase(noticeDTO.getReportStatus(), ApiReportStatus.SUCCESS.name())) {
                event = NoticeConstants.Event.CASE_EXECUTE_SUCCESSFUL;
                status = "成功";
            } else if (StringUtils.endsWithIgnoreCase(noticeDTO.getReportStatus(), ApiReportStatus.FAKE_ERROR.name())) {
                event = NoticeConstants.Event.CASE_EXECUTE_FAKE_ERROR;
                status = "误报";
            } else {
                event = NoticeConstants.Event.CASE_EXECUTE_FAILED;
                status = "失败";
            }
        }

        String userId = noticeDTO.getUserId();
        User user = userMapper.selectByPrimaryKey(userId);

        Map<String, Object> paramMap = new HashMap(beanMap);
        paramMap.put("operator", user != null ? user.getName() : "");
        paramMap.put("status", noticeDTO.getReportStatus());

        paramMap.put("reportName", report.getName());
        paramMap.put("startTime", report.getStartTime());
        paramMap.put("endTime", report.getEndTime());
        paramMap.put("requestDuration", report.getRequestDuration());
        paramMap.put("reportStatus", report.getStatus());
        paramMap.put("errorCount", report.getErrorCount());
        paramMap.put("fakeErrorCount", report.getFakeErrorCount());
        paramMap.put("pendingCount", report.getPendingCount());
        paramMap.put("successCount", report.getSuccessCount());
        paramMap.put("assertionCount", report.getAssertionCount());
        paramMap.put("assertionSuccessCount", report.getAssertionSuccessCount());
        paramMap.put("requestErrorRate", report.getRequestErrorRate());
        paramMap.put("requestPendingRate", report.getRequestPendingRate());
        paramMap.put("requestFakeErrorRate", report.getRequestFakeErrorRate());
        paramMap.put("requestPassRate", report.getRequestPassRate());
        paramMap.put("assertionPassRate", report.getAssertionPassRate());

        // TODO 这里状态是否是国际化  还有分享链接需要补充

        // TODO 暂时取一个环境处理
        if (StringUtils.isNotEmpty(noticeDTO.getEnvironmentId())) {
            Environment environment = environmentMapper.selectByPrimaryKey(noticeDTO.getEnvironmentId());
            if (environment != null) {
                paramMap.put("environment", environment.getName());
            }
        } else {
            paramMap.put("environment", "未配置");
        }
        paramMap.put("reportUrl", reportUrl);


        // TODO: 缺少生成分享链接
        String shareUrl = null;
        paramMap.put("scenarioShareUrl", baseSystemConfigDTO.getUrl() + "/api/share-api-report" + shareUrl);
        String context = "${operator}执行接口测试" + status + ": ${name}";
        NoticeModel noticeModel = NoticeModel.builder().operator(userId)
                .context(context).subject("执行通知").paramMap(paramMap).event(event).build();

        Project project = projectMapper.selectByPrimaryKey(noticeDTO.getProjectId());

        noticeSendService.send(project, noticeType, noticeModel);
    }

}
