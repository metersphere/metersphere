package io.metersphere.api.service;

import io.metersphere.api.dto.automation.ApiTestReportVariable;
import io.metersphere.api.jmeter.ExecutedHandleSingleton;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResultDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.request.testcase.TrackCount;
import io.metersphere.track.service.TestPlanApiCaseService;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import io.metersphere.track.service.TestPlanTestCaseService;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestResultService {
    @Resource
    private ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiAutomationService apiAutomationService;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanTestCaseService testPlanTestCaseService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;

    public void saveResults(ResultDTO dto) {
        // 处理环境
        List<String> environmentList = new LinkedList<>();
        if (dto.getArbitraryData() != null && dto.getArbitraryData().containsKey("ENV")) {
            environmentList = (List<String>) dto.getArbitraryData().get("ENV");
        }
        List<RequestResult> requestResults = dto.getRequestResults();
        //处理环境参数
        if (CollectionUtils.isNotEmpty(environmentList)) {
            ExecutedHandleSingleton.parseEnvironment(environmentList);
        }
        //测试计划定时任务-接口执行逻辑的话，需要同步测试计划的报告数据
        if (StringUtils.equalsAny(dto.getRunMode(), ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name(), ApiRunMode.MANUAL_PLAN.name())) {
            apiDefinitionExecResultService.saveApiResultByScheduleTask(requestResults, dto);
        } else if (StringUtils.equalsAny(dto.getRunMode(), ApiRunMode.DEFINITION.name(), ApiRunMode.JENKINS.name(), ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name(), ApiRunMode.MANUAL_PLAN.name())) {
            apiDefinitionExecResultService.saveApiResult(requestResults, dto);
        } else if (StringUtils.equalsAny(dto.getRunMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            apiScenarioReportService.saveResult(requestResults, dto);
        }
        updateTestCaseStates(requestResults, dto.getRunMode());
    }

    public void editReportTime(ResultDTO dto) {
        ApiScenarioReport report = apiScenarioReportMapper.selectByPrimaryKey(dto.getReportId());
        if (report != null) {
            report.setUpdateTime(System.currentTimeMillis());
            apiScenarioReportMapper.updateByPrimaryKey(report);
        }
    }

    public void testEnded(ResultDTO dto) {
        if (StringUtils.equalsAny(dto.getRunMode(), ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            ApiScenarioReport scenarioReport = apiScenarioReportService.testEnded(dto);
            if (scenarioReport != null) {
                ApiScenarioWithBLOBs apiScenario = apiAutomationService.getApiScenario(scenarioReport.getScenarioId());
                String environment = "";
                //执行人
                String userName = "";
                //负责人
                String principal = "";
                if (apiScenario != null) {
                    environment = apiScenarioReportService.getEnvironment(apiScenario);
                    userName = apiAutomationService.getUser(apiScenario.getUserId());
                    principal = apiAutomationService.getUser(apiScenario.getPrincipal());
                }
                //报告内容
                ApiTestReportVariable reportTask = new ApiTestReportVariable();
                reportTask.setStatus(scenarioReport.getStatus());
                reportTask.setId(scenarioReport.getId());
                reportTask.setTriggerMode(scenarioReport.getTriggerMode());
                reportTask.setName(scenarioReport.getName());
                reportTask.setExecutor(userName);
                reportTask.setUserId(scenarioReport.getUserId());
                reportTask.setPrincipal(principal);
                reportTask.setExecutionTime(DateUtils.getTimeString(scenarioReport.getUpdateTime()));
                reportTask.setEnvironment(environment);
                reportTask.setProjectId(scenarioReport.getProjectId());

                updateScenarioTestCaseStates(dto.getTestId(), dto.getRunMode());
                if (reportTask != null) {
                    if (StringUtils.equals(ReportTriggerMode.API.name(), reportTask.getTriggerMode())
                            || StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), reportTask.getTriggerMode())) {
                        sendTask(reportTask, dto.getTestId());
                    }
                }
            }
        }
    }

    /**
     * 更新测试计划关联接口测试的功能用例的状态
     */
    private void updateScenarioTestCaseStates(String testPlanScenarioId, String runMode) {
        try {
            if (StringUtils.equalsAny(runMode, ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                TestPlanScenarioCaseService testPlanScenarioCaseService = CommonBeanFactory.getBean(TestPlanScenarioCaseService.class);
                TestPlanApiScenario testPlanApiScenario = testPlanScenarioCaseService.get(testPlanScenarioId);
                ApiScenarioWithBLOBs apiScenario = CommonBeanFactory.getBean(ApiAutomationService.class).getApiScenario(testPlanApiScenario.getApiScenarioId());
                testPlanTestCaseService.updateTestCaseStates(apiScenario.getId(), apiScenario.getName(), testPlanApiScenario.getTestPlanId(), TrackCount.AUTOMATION);
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    private void updateTestCaseStates(List<RequestResult> requestResults, String runMode) {
        try {
            if (StringUtils.equalsAny(runMode, ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(),
                    ApiRunMode.JENKINS_API_PLAN.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                if (CollectionUtils.isNotEmpty(requestResults)) {
                    requestResults.forEach(item -> {
                        if (StringUtils.equalsAny(runMode, ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(),
                                ApiRunMode.JENKINS_API_PLAN.name())) {
                            TestPlanApiCase testPlanApiCase = testPlanApiCaseService.getById(item.getName());
                            ApiTestCaseWithBLOBs apiTestCase = apiTestCaseService.get(testPlanApiCase.getApiCaseId());
                            testPlanTestCaseService.updateTestCaseStates(apiTestCase.getId(), apiTestCase.getName(), testPlanApiCase.getTestPlanId(), TrackCount.TESTCASE);
                        }
                    });
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }


    private void sendTask(ApiTestReportVariable report, String testId) {
        if (report == null) {
            return;
        }
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
        assert systemParameterService != null;
        assert noticeSendService != null;
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String reportUrl = baseSystemConfigDTO.getUrl() + "/#/api/automation/report/view/" + report.getId();

        String subject = "";
        String event = "";
        String successContext = "${operator}执行接口自动化成功: ${name}" + ", 报告: ${reportUrl}";
        String failedContext = "${operator}执行接口自动化失败: ${name}" + ", 报告: ${reportUrl}";

        if (StringUtils.equals(ReportTriggerMode.API.name(), report.getTriggerMode())) {
            subject = Translator.get("task_notification_jenkins");
        }
        if (StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), report.getTriggerMode())) {
            subject = Translator.get("task_notification");
        }
        if (StringUtils.equals("Success", report.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
        }
        if (StringUtils.equals("success", report.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
        }
        if (StringUtils.equals("Error", report.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_FAILED;
        }
        if (StringUtils.equals("error", report.getStatus())) {
            event = NoticeConstants.Event.EXECUTE_FAILED;
        }
        Map paramMap = new HashMap<>();
        paramMap.put("type", "api");
        paramMap.put("url", baseSystemConfigDTO.getUrl());
        paramMap.put("reportUrl", reportUrl);
        paramMap.put("operator", report.getExecutor());
        paramMap.putAll(new BeanMap(report));
        NoticeModel noticeModel = NoticeModel.builder()
                .operator(report.getUserId())
                .successContext(successContext)
                .successMailTemplate("ApiSuccessfulNotification")
                .failedContext(failedContext)
                .failedMailTemplate("ApiFailedNotification")
                .testId(testId)
                .status(report.getStatus())
                .event(event)
                .subject(subject)
                .paramMap(paramMap)
                .build();
        noticeSendService.send(report.getTriggerMode(), NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }
}
