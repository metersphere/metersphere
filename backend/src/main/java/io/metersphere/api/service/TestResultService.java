package io.metersphere.api.service;

import io.metersphere.api.dto.automation.ApiTestReportVariable;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResultDTO;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.request.testcase.TrackCount;
import io.metersphere.track.service.TestPlanApiCaseService;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import io.metersphere.track.service.TestPlanTestCaseService;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class TestResultService {
    @Resource
    private ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiAutomationService apiAutomationService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private UiScenarioMapper uiScenarioMapper;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanTestCaseService testPlanTestCaseService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ApiEnvironmentRunningParamService apiEnvironmentRunningParamService;
    @Resource
    private RedisTemplateService redisTemplateService;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;

    // 场景
    private static final List<String> scenarioRunModes = new ArrayList<>() {{
        this.add(ApiRunMode.SCENARIO.name());
        this.add(ApiRunMode.SCENARIO_PLAN.name());
        this.add(ApiRunMode.SCHEDULE_SCENARIO_PLAN.name());
        this.add(ApiRunMode.SCHEDULE_SCENARIO.name());
        this.add(ApiRunMode.JENKINS_SCENARIO_PLAN.name());
    }};

    // 接口测试 用例/接口
    private static final List<String> caseRunModes = new ArrayList<>() {{
        this.add(ApiRunMode.DEFINITION.name());
        this.add(ApiRunMode.JENKINS.name());
        this.add(ApiRunMode.API_PLAN.name());
    }};

    // 测试计划 用例/接口
    private static final List<String> planCaseRunModes = new ArrayList<>() {{
        this.add(ApiRunMode.SCHEDULE_API_PLAN.name());
        this.add(ApiRunMode.JENKINS_API_PLAN.name());
        this.add(ApiRunMode.MANUAL_PLAN.name());
    }};

    // ui 执行触发类型
    private static final List<String> uiRunModes = new ArrayList<>() {{
        this.add(ApiRunMode.UI_SCENARIO.name());
        this.add(ApiRunMode.UI_SCENARIO_PLAN.name());
        this.add(ApiRunMode.UI_JENKINS_SCENARIO_PLAN.name());
        this.add(ApiRunMode.UI_SCHEDULE_SCENARIO.name());
        this.add(ApiRunMode.UI_SCHEDULE_SCENARIO_PLAN.name());

    }};

    private void sendNotice(ApiDefinitionExecResult result, User user) {
        try {
            String resourceId = result.getResourceId();
            ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(resourceId);
            // 接口定义直接执行不发通知
            if (apiTestCaseWithBLOBs == null) {
                return;
            }
            BeanMap beanMap = new BeanMap(apiTestCaseWithBLOBs);

            String event;
            String status;
            if (StringUtils.equals(result.getStatus(), "success")) {
                event = NoticeConstants.Event.EXECUTE_SUCCESSFUL;
                status = "成功";
            } else {
                event = NoticeConstants.Event.EXECUTE_FAILED;
                status = "失败";
            }
            if (user == null && StringUtils.isNotBlank(result.getUserId())) {
                user = userMapper.selectByPrimaryKey(result.getUserId());
            }
            Map paramMap = new HashMap<>(beanMap);
            paramMap.put("operator", user != null ? user.getName() : result.getUserId());
            paramMap.put("status", result.getStatus());
            String context = "${operator}执行接口用例" + status + ": ${name}";
            NoticeModel noticeModel = NoticeModel.builder()
                    .operator(result.getUserId() != null ? result.getUserId() : SessionUtils.getUserId())
                    .context(context)
                    .subject("接口用例通知")
                    .paramMap(paramMap)
                    .event(event)
                    .build();

            String taskType = NoticeConstants.TaskType.API_DEFINITION_TASK;
            Project project = projectMapper.selectByPrimaryKey(apiTestCaseWithBLOBs.getProjectId());
            noticeSendService.send(project, taskType, noticeModel);
        } catch (Exception e) {
            LogUtil.error("消息发送失败：" + e.getMessage());
        }
    }

    /**
     * 执行结果存储
     *
     * @param dto 执行结果
     */
    public void saveResults(ResultDTO dto) {
        // 处理环境
        List<String> environmentList = new LinkedList<>();
        if (dto.getArbitraryData() != null && dto.getArbitraryData().containsKey("ENV")) {
            environmentList = (List<String>) dto.getArbitraryData().get("ENV");
        }
        //处理环境参数
        if (CollectionUtils.isNotEmpty(environmentList)) {
            apiEnvironmentRunningParamService.parseEnvironment(environmentList);
        }

        // 测试计划用例触发结果处理
        if (planCaseRunModes.contains(dto.getRunMode())) {
            apiDefinitionExecResultService.saveApiResultByScheduleTask(dto);
        } else if (caseRunModes.contains(dto.getRunMode())) {
            // 手动触发/批量触发 用例结果处理
            List<ApiDefinitionExecResult> results = apiDefinitionExecResultService.saveApiResult(dto);
            sendMessage(results, dto);
        } else if (scenarioRunModes.contains(dto.getRunMode())) {
            // 场景报告结果处理
            apiScenarioReportService.saveResult(dto);
        } else if (uiRunModes.contains(dto.getRunMode())) {
            // ui 结果处理
            apiScenarioReportService.saveUiResult(dto.getRequestResults(), dto);
        }
        updateTestCaseStates(dto.getRequestResults(), dto.getRunMode());
    }

    private void sendMessage(List<ApiDefinitionExecResult> results, ResultDTO dto) {
        results.forEach(result -> {
            User user = null;
            if (MapUtils.isNotEmpty(dto.getExtendedParameters())) {
                if (dto.getExtendedParameters().containsKey("userId") && dto.getExtendedParameters().containsKey("userName")) {
                    user = new User() {{
                        this.setId(dto.getExtendedParameters().get("userId").toString());
                        this.setName(dto.getExtendedParameters().get("userName").toString());
                    }};
                } else if (dto.getExtendedParameters().containsKey("userId")) {
                    result.setUserId(dto.getExtendedParameters().get("userId").toString());
                }
            }
            sendNotice(result, user);
        });
    }

    /**
     * 批量存储执行结果
     *
     * @param resultDtoMap
     */
    public void batchSaveResults(Map<String, List<ResultDTO>> resultDtoMap) {
        // 处理环境
        List<String> environmentList = new LinkedList<>();
        for (String key : resultDtoMap.keySet()) {
            for (ResultDTO dto : resultDtoMap.get(key)) {
                if (dto.getArbitraryData() != null && dto.getArbitraryData().containsKey("ENV")) {
                    environmentList = (List<String>) dto.getArbitraryData().get("ENV");
                }
                //处理环境参数
                if (CollectionUtils.isNotEmpty(environmentList)) {
                    apiEnvironmentRunningParamService.parseEnvironment(environmentList);
                }
                // 处理用例/场景和计划关系
                updateTestCaseStates(dto.getRequestResults(), dto.getRunMode());
            }
            //测试计划定时任务-接口执行逻辑的话，需要同步测试计划的报告数据
            if (StringUtils.equals(key, "schedule-task")) {
                Map<ResultDTO, List<ApiDefinitionExecResult>> results = apiDefinitionExecResultService.batchSaveApiResult(resultDtoMap.get(key), true);
                for (ResultDTO dto : results.keySet()) {
                    sendMessage(results.get(dto), dto);
                }
            } else if (StringUtils.equals(key, "api-test-case-task")) {
                Map<ResultDTO, List<ApiDefinitionExecResult>> results = apiDefinitionExecResultService.batchSaveApiResult(resultDtoMap.get(key), false);
                for (ResultDTO dto : results.keySet()) {
                    sendMessage(results.get(dto), dto);
                }
            } else if (StringUtils.equalsAny(key, "api-scenario-task")) {
                apiScenarioReportService.batchSaveResult(resultDtoMap.get(key));
            }

        }
    }

    public void testEnded(ResultDTO dto) {
        // 删除串行资源锁
        if (StringUtils.equals(dto.getRunType(), RunModeConstants.SERIAL.toString())) {
            String key = StringUtils.join(RunModeConstants.SERIAL.name(), "_", dto.getReportId());
            redisTemplateService.delete(key);
        }
        if (dto.getRequestResults() == null) {
            dto.setRequestResults(new LinkedList<>());
        }
        if (scenarioRunModes.contains(dto.getRunMode()) || dto.getRunMode().startsWith("UI")) {
            ApiScenarioReport scenarioReport = apiScenarioReportService.testEnded(dto);
            if (scenarioReport != null) {
                String environment = "";
                //执行人
                String userName = "";
                //负责人
                String principal = "";

                if (dto.getRunMode().startsWith("UI")) {
                    UiScenarioWithBLOBs uiScenario = uiScenarioMapper.selectByPrimaryKey(scenarioReport.getScenarioId());
                    if (uiScenario != null) {
                        userName = apiAutomationService.getUser(uiScenario.getUserId());
                        principal = apiAutomationService.getUser(uiScenario.getPrincipal());
                    }
                } else {
                    ApiScenarioWithBLOBs apiScenario = apiScenarioMapper.selectByPrimaryKey(scenarioReport.getScenarioId());
                    if (apiScenario != null) {
                        environment = apiScenarioReportService.getEnvironment(apiScenario);
                        userName = apiAutomationService.getUser(apiScenario.getUserId());
                        principal = apiAutomationService.getUser(apiScenario.getPrincipal());
                    }
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
        } else if (StringUtils.equalsAnyIgnoreCase(dto.getRunMode(), ApiRunMode.DEFINITION.name(), ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name())) {
            ApiDefinitionExecResult record = new ApiDefinitionExecResult();
            record.setId(dto.getReportId());
            record.setStatus("STOP");

            ApiDefinitionExecResultExample example = new ApiDefinitionExecResultExample();
            example.createCriteria().andIdEqualTo(dto.getReportId()).andStatusEqualTo(APITestStatus.Running.name());
            apiDefinitionExecResultMapper.updateByExampleSelective(record, example);
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
                ApiScenarioWithBLOBs apiScenario = apiScenarioMapper.selectByPrimaryKey(testPlanApiScenario.getApiScenarioId());
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
            subject = "Jenkins任务通知";
        }
        if (StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), report.getTriggerMode())) {
            subject = "任务通知";
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
        ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(testId);
        Map paramMap = new HashMap<>();
        paramMap.put("type", "api");
        paramMap.put("url", baseSystemConfigDTO.getUrl());
        paramMap.put("reportUrl", reportUrl);
        paramMap.put("operator", report.getExecutor());
        paramMap.putAll(new BeanMap(report));
        paramMap.putAll(new BeanMap(scenario));
        NoticeModel noticeModel = NoticeModel.builder()
                .operator(report.getUserId())
                .successContext(successContext)
                .failedContext(failedContext)
                .testId(testId)
                .status(report.getStatus())
                .event(event)
                .subject(subject)
                .paramMap(paramMap)
                .build();
        noticeSendService.send(report.getTriggerMode(), NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }
}
