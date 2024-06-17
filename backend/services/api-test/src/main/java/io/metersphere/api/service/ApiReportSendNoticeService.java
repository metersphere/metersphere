package io.metersphere.api.service;

import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.constants.ApiScenarioStatus;
import io.metersphere.api.domain.ApiReport;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioReport;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.share.ApiReportShareRequest;
import io.metersphere.api.dto.share.ShareInfoDTO;
import io.metersphere.api.mapper.ApiReportMapper;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.api.mapper.ApiScenarioReportMapper;
import io.metersphere.api.mapper.ApiTestCaseMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ApiExecuteResourceType;
import io.metersphere.sdk.constants.ReportStatus;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.dto.api.notice.ApiNoticeDTO;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.BaseSystemConfigDTO;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.NoticeModel;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.service.NoticeSendService;
import io.metersphere.system.service.SystemParameterService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
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

    public void sendNotice(ApiNoticeDTO noticeDTO) {
        String noticeType = null;
        if (StringUtils.equalsAnyIgnoreCase(noticeDTO.getResourceType(),
                ApiExecuteResourceType.PLAN_RUN_API_CASE.name(), ApiExecuteResourceType.PLAN_RUN_API_SCENARIO.name())) {
            return;
        }
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        assert systemParameterService != null;
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        BeanMap beanMap = null;
        String event = null;
        ApiReportShareService shareService = CommonBeanFactory.getBean(ApiReportShareService.class);
        ApiReportShareRequest shareRequest = new ApiReportShareRequest();
        shareRequest.setReportId(noticeDTO.getReportId());
        shareRequest.setProjectId(noticeDTO.getProjectId());
        assert shareService != null;
        ShareInfoDTO url = shareService.gen(shareRequest, noticeDTO.getUserId());
        Project project = projectMapper.selectByPrimaryKey(noticeDTO.getProjectId());
        String reportUrl = baseSystemConfigDTO.getUrl() + "/#/api-test/report?orgId=%s&pId=%s&type=%s&reportId=%s";
        String shareUrl = baseSystemConfigDTO.getUrl() + "/#/share/%s?shareId=" + url.getId();
        ApiScenarioReport report = new ApiScenarioReport();
        if (StringUtils.equalsAnyIgnoreCase(noticeDTO.getResourceType(),
                ApiExecuteResourceType.API_SCENARIO.name(), ApiExecuteResourceType.TEST_PLAN_API_SCENARIO.name())) {
            ApiScenario scenario = apiScenarioMapper.selectByPrimaryKey(noticeDTO.getResourceId());
            beanMap = new BeanMap(scenario);
            noticeType = NoticeConstants.TaskType.API_SCENARIO_TASK;
            report = apiScenarioReportMapper.selectByPrimaryKey(noticeDTO.getReportId());
            reportUrl = String.format(reportUrl, project.getOrganizationId(), project.getId(), ApiExecuteResourceType.API_SCENARIO.name(), report.getId());
            if (StringUtils.endsWithIgnoreCase(noticeDTO.getReportStatus(), ReportStatus.SUCCESS.name())) {
                event = NoticeConstants.Event.SCENARIO_EXECUTE_SUCCESSFUL;
            } else if (StringUtils.endsWithIgnoreCase(noticeDTO.getReportStatus(), ReportStatus.FAKE_ERROR.name())) {
                event = NoticeConstants.Event.SCENARIO_EXECUTE_FAKE_ERROR;
            } else if (StringUtils.endsWithIgnoreCase(noticeDTO.getReportStatus(), ReportStatus.ERROR.name())) {
                event = NoticeConstants.Event.SCENARIO_EXECUTE_FAILED;
            }
            shareUrl = String.format(shareUrl, "shareReportScenario");
        } else if (StringUtils.equalsAnyIgnoreCase(noticeDTO.getResourceType(),
                ApiExecuteResourceType.API_CASE.name(), ApiExecuteResourceType.TEST_PLAN_API_CASE.name())) {
            ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(noticeDTO.getResourceId());
            beanMap = new BeanMap(testCase);

            // TODO 是否需要区分场景和用例
            noticeType = NoticeConstants.TaskType.API_DEFINITION_TASK;
            ApiReport apiReport = apiReportMapper.selectByPrimaryKey(noticeDTO.getReportId());
            reportUrl = String.format(reportUrl, project.getOrganizationId(), project.getId(), ApiExecuteResourceType.API_CASE.name(), apiReport.getId());
            BeanUtils.copyBean(report, apiReport);
            if (StringUtils.endsWithIgnoreCase(noticeDTO.getReportStatus(), ReportStatus.SUCCESS.name())) {
                event = NoticeConstants.Event.CASE_EXECUTE_SUCCESSFUL;
            } else if (StringUtils.endsWithIgnoreCase(noticeDTO.getReportStatus(), ReportStatus.FAKE_ERROR.name())) {
                event = NoticeConstants.Event.CASE_EXECUTE_FAKE_ERROR;
            } else if (StringUtils.endsWithIgnoreCase(noticeDTO.getReportStatus(), ReportStatus.ERROR.name())) {
                event = NoticeConstants.Event.CASE_EXECUTE_FAILED;
            }
            shareUrl = String.format(shareUrl, "shareReportCase");
        }

        String userId = noticeDTO.getUserId();
        User user = userMapper.selectByPrimaryKey(userId);

        Map paramMap = new HashMap<>(beanMap);
        noticeSendService.setLanguage(user.getLanguage());
        paramMap.put(NoticeConstants.RelatedUser.OPERATOR, user != null ? user.getName() : "");
        // TODO 是否需要国际化   根据状态判断给不同的key
        String status = paramMap.containsKey("status") ? paramMap.get("status").toString() : null;
        if (StringUtils.isNotBlank(status)) {
            if (List.of(ApiScenarioStatus.UNDERWAY.name(), ApiDefinitionStatus.PROCESSING.name()).contains(status)) {
                status = Translator.get("api_definition.status.ongoing");
            } else if (List.of(ApiScenarioStatus.COMPLETED.name(), ApiDefinitionStatus.DONE.name()).contains(status)) {
                status = Translator.get("api_definition.status.completed");
            } else if (StringUtils.equals(ApiScenarioStatus.DEPRECATED.name(), status)) {
                status = Translator.get("api_definition.status.abandoned");
            } else if (StringUtils.equals(ApiDefinitionStatus.DEBUGGING.name(), status)) {
                status = Translator.get("api_definition.status.continuous");
            }
        }


        String reportStatus = report.getStatus();
        if (StringUtils.endsWithIgnoreCase(reportStatus, ReportStatus.SUCCESS.name())) {
            reportStatus = Translator.get("report.status.success");
        } else if (StringUtils.endsWithIgnoreCase(reportStatus, ReportStatus.FAKE_ERROR.name())) {
            reportStatus = Translator.get("report.status.fake_error");
        } else {
            reportStatus = Translator.get("report.status.error");
        }

        paramMap.put("status", status);
        paramMap.put("reportName", report.getName());
        paramMap.put("startTime", report.getStartTime());
        paramMap.put("endTime", report.getEndTime());
        paramMap.put("requestDuration", report.getRequestDuration());
        paramMap.put("reportStatus", reportStatus);
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
        String environmentId = noticeDTO.getRunModeConfig().getEnvironmentId();
        if (StringUtils.isNotEmpty(environmentId)) {
            Environment environment = environmentMapper.selectByPrimaryKey(environmentId);
            if (environment != null) {
                paramMap.put("environment", environment.getName());
            }
        } else {
            paramMap.put("environment", "未配置");
        }
        paramMap.put("reportUrl", reportUrl);

        paramMap.put("scenarioShareUrl", shareUrl);

        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        String template = defaultTemplateMap.get(noticeType + "_" + event);
        Map<String, String> defaultSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        String subject = defaultSubjectMap.get(noticeType + "_" + event);
        NoticeModel noticeModel = NoticeModel.builder().operator(userId)
                .context(template).subject(subject).paramMap(paramMap).event(event).build();

        noticeSendService.send(project, noticeType, noticeModel);
    }
}
