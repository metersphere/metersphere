package io.metersphere.api.service;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.automation.ApiTestReportVariable;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.BaseSystemConfigDTO;
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
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestResultService {

    @Resource
    private APITestService apiTestService;
    @Resource
    private APIReportService apiReportService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiAutomationService apiAutomationService;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanTestCaseService testPlanTestCaseService;

    public void saveResult(TestResult testResult, String runMode, String debugReportId, String testId) {
        try {
            ApiTestReport report = null;
            ApiTestReportVariable reportTask = null;
            String reportUrl = null;
            String planScenarioId = null;
            // 这部分后续优化只留 DEFINITION 和 SCENARIO 两部分
            if (StringUtils.equals(runMode, ApiRunMode.DEBUG.name())) {
                report = apiReportService.get(debugReportId);
                apiReportService.complete(testResult, report);
            } else if (StringUtils.equals(runMode, ApiRunMode.DEFINITION.name())) {
                // 调试操作，不需要存储结果
                apiDefinitionService.addResult(testResult);
                if (StringUtils.isBlank(debugReportId)) {
                    apiDefinitionExecResultService.saveApiResult(testResult, ApiRunMode.DEFINITION.name(), TriggerMode.MANUAL.name());
                }
                //jenkins单接口执行
            } else if (StringUtils.equals(runMode, ApiRunMode.JENKINS.name())) {
                apiDefinitionExecResultService.saveApiResult(testResult, ApiRunMode.DEFINITION.name(), TriggerMode.API.name());
                ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = apiTestCaseService.getInfoJenkins(testResult.getTestId());
                ApiDefinitionExecResult apiResult = apiDefinitionExecResultService.getInfo(apiTestCaseWithBLOBs.getLastResultId());
                //环境
                String name = apiAutomationService.get(debugReportId).getName();
                //执行人
                String userName = apiAutomationService.getUser(apiTestCaseWithBLOBs.getCreateUserId());
                //报告内容
                reportTask = new ApiTestReportVariable();
                reportTask.setStatus(apiResult.getStatus());
                reportTask.setId(apiResult.getId());
                reportTask.setTriggerMode(TriggerMode.API.name());
                reportTask.setName(apiTestCaseWithBLOBs.getName());
                reportTask.setExecutor(userName);
                reportTask.setExecutionTime(DateUtils.getTimeString(apiTestCaseWithBLOBs.getCreateTime()));
                reportTask.setExecutionEnvironment(name);
                //测试计划用例，定时，jenkins
            } else if (StringUtils.equalsAny(runMode, ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name(), ApiRunMode.MANUAL_PLAN.name())) {
                //测试计划定时任务-接口执行逻辑的话，需要同步测试计划的报告数据
                if (StringUtils.equalsAny(runMode, ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name(), ApiRunMode.MANUAL_PLAN.name())) {
                    apiDefinitionExecResultService.saveApiResultByScheduleTask(testResult, debugReportId, runMode);
                } else {
                    apiDefinitionExecResultService.saveApiResult(testResult, ApiRunMode.API_PLAN.name(), TriggerMode.MANUAL.name());
                }
            } else if (StringUtils.equalsAny(runMode, ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                // 执行报告不需要存储，由用户确认后在存储
                testResult.setTestId(testId);
                ApiScenarioReport scenarioReport = apiScenarioReportService.complete(testResult, runMode);
                //环境
                if (scenarioReport != null) {
                    ApiScenarioWithBLOBs apiScenario = apiAutomationService.getDto(scenarioReport.getScenarioId());
                    String name = "";
                    //执行人
                    String userName = "";
                    //负责人
                    String principal = "";
                    if (apiScenario != null) {
                        String executionEnvironment = apiScenario.getScenarioDefinition();
                        JSONObject json = JSONObject.parseObject(executionEnvironment);
                        if (json != null && json.getString("environmentMap") != null && json.getString("environmentMap").length() > 2) {
                            JSONObject environment = JSONObject.parseObject(json.getString("environmentMap"));
                            String environmentId = environment.get(apiScenario.getProjectId()).toString();
                            name = apiAutomationService.get(environmentId).getName();
                        }
                        userName = apiAutomationService.getUser(apiScenario.getUserId());
                        principal = apiAutomationService.getUser(apiScenario.getPrincipal());
                    }
                    //报告内容
                    reportTask = new ApiTestReportVariable();
                    if (StringUtils.equalsAny(runMode, ApiRunMode.SCHEDULE_SCENARIO.name())) {
                        reportTask.setStatus(scenarioReport.getStatus());
                        reportTask.setId(scenarioReport.getId());
                        reportTask.setTriggerMode(scenarioReport.getTriggerMode());
                        reportTask.setName(scenarioReport.getName());
                        reportTask.setExecutor(userName);
                        reportTask.setPrincipal(principal);
                        reportTask.setExecutionTime(DateUtils.getTimeString(scenarioReport.getUpdateTime()));
                        reportTask.setExecutionEnvironment(name);
                        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
                        assert systemParameterService != null;
                        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
                        reportUrl = baseSystemConfigDTO.getUrl() + "/#/api/automation/report";

                    }
                    testResult.setTestId(scenarioReport.getScenarioId());
                    planScenarioId = scenarioReport.getTestPlanScenarioId();
                }
            } else {
                apiTestService.changeStatus(testId, APITestStatus.Completed);
                report = apiReportService.getRunningReport(testResult.getTestId());
                apiReportService.complete(testResult, report);
            }
            updateTestCaseStates(testResult, planScenarioId, runMode);
            List<String> ids = testPlanTestCaseService.getTestPlanTestCaseIds(testResult.getTestId());
            if (CollectionUtils.isNotEmpty(ids)) {
                if (StringUtils.equals(APITestStatus.Success.name(), report.getStatus())) {
                    testPlanTestCaseService.updateTestCaseStates(ids, TestPlanTestCaseStatus.Pass.name());
                } else {
                    testPlanTestCaseService.updateTestCaseStates(ids, TestPlanTestCaseStatus.Failure.name());
                }
            }
            if (reportTask != null) {
                if (StringUtils.equals(ReportTriggerMode.API.name(), reportTask.getTriggerMode())
                        || StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), reportTask.getTriggerMode())) {
                    sendTask(reportTask, testResult);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 更新测试计划关联接口测试的功能用例的状态
     *
     * @param testResult
     */
    private void updateTestCaseStates(TestResult testResult, String testPlanScenarioId, String runMode) {
        try {
            if (StringUtils.equalsAny(runMode, ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(),
                    ApiRunMode.JENKINS_API_PLAN.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
                testResult.getScenarios().forEach(scenarioResult -> {
                    if (scenarioResult != null && CollectionUtils.isNotEmpty(scenarioResult.getRequestResults())) {
                        scenarioResult.getRequestResults().forEach(item -> {
                            if (StringUtils.equalsAny(runMode, ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(),
                                    ApiRunMode.JENKINS_API_PLAN.name())) {
                                TestPlanApiCase testPlanApiCase = testPlanApiCaseService.getById(item.getName());
                                ApiTestCaseWithBLOBs apiTestCase = apiTestCaseService.get(testPlanApiCase.getApiCaseId());
                                testPlanTestCaseService.updateTestCaseStates(apiTestCase.getId(), apiTestCase.getName(), testPlanApiCase.getTestPlanId(), TrackCount.TESTCASE);
                            } else {
                                TestPlanScenarioCaseService testPlanScenarioCaseService = CommonBeanFactory.getBean(TestPlanScenarioCaseService.class);
                                TestPlanApiScenario testPlanApiScenario = testPlanScenarioCaseService.get(testPlanScenarioId);
                                ApiScenarioWithBLOBs apiScenario = apiAutomationService.getApiScenario(testPlanApiScenario.getApiScenarioId());
                                testPlanTestCaseService.updateTestCaseStates(apiScenario.getId(), apiScenario.getName(), testPlanApiScenario.getTestPlanId(), TrackCount.AUTOMATION);
                            }
                        });
                    }
                });
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    private void sendTask(ApiTestReportVariable report, TestResult testResult) {
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
        String successContext = "${operator}执行接口测成功: ${name}" + ", 报告: ${reportUrl}";
        String failedContext = "${operator}执行接口测试失败: ${name}" + ", 报告: ${reportUrl}";

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
                .testId(testResult.getTestId())
                .status(report.getStatus())
                .event(event)
                .subject(subject)
                .paramMap(paramMap)
                .build();
        noticeSendService.send(report.getTriggerMode(), NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }
}
