package io.metersphere.api.jmeter;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.RunningParamKeys;
import io.metersphere.api.dto.automation.ApiTestReportVariable;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.service.*;
import io.metersphere.base.domain.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.request.testcase.TrackCount;
import io.metersphere.track.service.TestPlanApiCaseService;
import io.metersphere.track.service.TestPlanReportService;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import io.metersphere.track.service.TestPlanTestCaseService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleResult;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class APIBackendListenerResultHandler {

    @Resource
    private APITestService apiTestService;
    @Resource
    private APIReportService apiReportService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    private TestPlanReportService testPlanReportService;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiAutomationService apiAutomationService;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private ApiEnvironmentRunningParamService apiEnvironmentRunningParamService;
    @Resource
    private TestPlanTestCaseService testPlanTestCaseService;

    private final static String THREAD_SPLIT = " ";

    private final static String ID_SPLIT = "-";

    public void handleTeardownTest(List<SampleResult> queue, String runMode,
                                   String testId, String debugReportId, String setReportId, String console) throws Exception {
        TestResult testResult = new TestResult();
        testResult.setTestId(testId);
        testResult.setSetReportId(setReportId);
        testResult.setConsole(console);
        testResult.setTotal(0);
        // 一个脚本里可能包含多个场景(ThreadGroup)，所以要区分开，key: 场景Id
        final Map<String, ScenarioResult> scenarios = new LinkedHashMap<>();
        queue.forEach(result -> {
            // 线程名称: <场景名> <场景Index>-<请求Index>, 例如：Scenario 2-1
            if(StringUtils.equals(result.getSampleLabel(), RunningParamKeys.RUNNING_DEBUG_SAMPLER_NAME)){
                String evnStr = result.getResponseDataAsString();
                apiEnvironmentRunningParamService.parseEvn(evnStr);
            }else {
                String scenarioName = StringUtils.substringBeforeLast(result.getThreadName(), THREAD_SPLIT);
                String index = StringUtils.substringAfterLast(result.getThreadName(), THREAD_SPLIT);
                String scenarioId = StringUtils.substringBefore(index, ID_SPLIT);
                ScenarioResult scenarioResult;
                if (!scenarios.containsKey(scenarioId)) {
                    scenarioResult = new ScenarioResult();
                    try {
                        scenarioResult.setId(Integer.parseInt(scenarioId));
                    } catch (Exception e) {
                        scenarioResult.setId(0);
                        LogUtil.error("场景ID转换异常: " + e.getMessage());
                    }
                    scenarioResult.setName(scenarioName);
                    scenarios.put(scenarioId, scenarioResult);
                } else {
                    scenarioResult = scenarios.get(scenarioId);
                }
                if (result.isSuccessful()) {
                    scenarioResult.addSuccess();
                    testResult.addSuccess();
                } else {
                    scenarioResult.addError(result.getErrorCount());
                    testResult.addError(result.getErrorCount());
                }

                RequestResult requestResult = getRequestResult(result);
                scenarioResult.getRequestResults().add(requestResult);
                scenarioResult.addResponseTime(result.getTime());

                testResult.addPassAssertions(requestResult.getPassAssertions());
                testResult.addTotalAssertions(requestResult.getTotalAssertions());
                testResult.setTotal(testResult.getTotal()+1);
                scenarioResult.addPassAssertions(requestResult.getPassAssertions());
                scenarioResult.addTotalAssertions(requestResult.getTotalAssertions());
            }
        });

        testResult.getScenarios().addAll(scenarios.values());
        testResult.getScenarios().sort(Comparator.comparing(ScenarioResult::getId));
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
                apiDefinitionExecResultService.saveApiResult(testResult, ApiRunMode.DEFINITION.name());
            }
        } else if (StringUtils.equals(runMode, ApiRunMode.JENKINS.name())) {
            apiDefinitionService.addResult(testResult);
            apiDefinitionExecResultService.saveApiResult(testResult, ApiRunMode.DEFINITION.name());
            ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = apiTestCaseService.getInfoJenkins(testResult.getTestId());
            ApiDefinitionExecResult apiResult = apiDefinitionExecResultService.getInfo(apiTestCaseWithBLOBs.getLastResultId());
            //环境
            String name = apiAutomationService.get(debugReportId).getName();
            //时间
            Long time = apiTestCaseWithBLOBs.getCreateTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String executionTime = null;
            String time_ = String.valueOf(time);
            if (!time_.equals("null")) {
                executionTime = sdf.format(new Date(Long.parseLong(time_)));
            }

            //执行人
            String userName = apiAutomationService.getUser(apiTestCaseWithBLOBs.getCreateUserId()).getName();

            //报告内容
            reportTask = new ApiTestReportVariable();
            reportTask.setStatus(apiResult.getStatus());
            reportTask.setId(apiResult.getId());
            reportTask.setTriggerMode("API");
            reportTask.setName(apiTestCaseWithBLOBs.getName());
            reportTask.setExecutor(userName);
            reportTask.setExecutionTime(executionTime);
            reportTask.setExecutionEnvironment(name);
            //用例，定时，jenkins
        } else if (StringUtils.equalsAny(runMode, ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(), ApiRunMode.JENKINS_API_PLAN.name())) {
            apiDefinitionService.addResult(testResult);

            //测试计划定时任务-接口执行逻辑的话，需要同步测试计划的报告数据
            if (StringUtils.equals(runMode, ApiRunMode.SCHEDULE_API_PLAN.name())) {
                apiDefinitionExecResultService.saveApiResultByScheduleTask(testResult, ApiRunMode.SCHEDULE_API_PLAN.name());
                List<String> testPlanReportIdList = new ArrayList<>();
                testPlanReportIdList.add(debugReportId);
                for (String testPlanReportId : testPlanReportIdList) {   //  更新每个测试计划的状态
                    testPlanReportService.checkTestPlanStatus(testPlanReportId);
                }
                testPlanReportService.updateReport(testPlanReportIdList, ApiRunMode.SCHEDULE_API_PLAN.name(), ReportTriggerMode.SCHEDULE.name());
            } else if (StringUtils.equals(runMode, ApiRunMode.JENKINS_API_PLAN.name())) {
                apiDefinitionExecResultService.saveApiResultByScheduleTask(testResult, ApiRunMode.JENKINS_API_PLAN.name());
                List<String> testPlanReportIdList = new ArrayList<>();
                testPlanReportIdList.add(debugReportId);
                for (String testPlanReportId : testPlanReportIdList) {   //  更新每个测试计划的状态
                    testPlanReportService.checkTestPlanStatus(testPlanReportId);
                }
                testPlanReportService.updateReport(testPlanReportIdList, ApiRunMode.JENKINS_API_PLAN.name(), ReportTriggerMode.API.name());
            } else {
                apiDefinitionExecResultService.saveApiResult(testResult, ApiRunMode.API_PLAN.name());
            }
        } else if (StringUtils.equalsAny(runMode, ApiRunMode.SCENARIO.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO.name(), ApiRunMode.JENKINS_SCENARIO_PLAN.name())) {
            // 执行报告不需要存储，由用户确认后在存储
            testResult.setTestId(testId);
            ApiScenarioReport scenarioReport = apiScenarioReportService.complete(testResult, runMode);
            //环境
            ApiScenarioWithBLOBs apiScenario = apiAutomationService.getDto(scenarioReport.getScenarioId());
            String name = "";
            if (apiScenario != null) {
                String executionEnvironment = apiScenario.getScenarioDefinition();
                JSONObject json = JSONObject.parseObject(executionEnvironment);
                if (json != null && json.getString("environmentMap") != null && json.getString("environmentMap").length() > 2) {
                    JSONObject environment = JSONObject.parseObject(json.getString("environmentMap"));
                    String environmentId = environment.get(apiScenario.getProjectId()).toString();
                    name = apiAutomationService.get(environmentId).getName();
                }
            }
            //时间
            Long time = scenarioReport.getUpdateTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String executionTime = null;
            String time_ = String.valueOf(time);
            if (!time_.equals("null")) {
                executionTime = sdf.format(new Date(Long.parseLong(time_)));
            }

            //报告内容
            reportTask = new ApiTestReportVariable();
            reportTask.setStatus(scenarioReport.getStatus());
            reportTask.setId(scenarioReport.getId());
            reportTask.setTriggerMode(scenarioReport.getTriggerMode());
            reportTask.setName(scenarioReport.getName());
            //执行人
            if(apiScenario != null){
                String userName = apiAutomationService.getUser(apiScenario.getUserId()).getName();
                reportTask.setExecutor(userName);
            }
            reportTask.setExecutionTime(executionTime);
            reportTask.setExecutionEnvironment(name);
            SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
            assert systemParameterService != null;
            BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
            reportUrl = baseSystemConfigDTO.getUrl() + "/#/api/automation/report";
            testResult.setTestId(scenarioReport.getScenarioId());
            planScenarioId = scenarioReport.getTestPlanScenarioId();
        } else {
            apiTestService.changeStatus(testId, APITestStatus.Completed);
            report = apiReportService.getRunningReport(testResult.getTestId());
            apiReportService.complete(testResult, report);
        }
        queue.clear();

        updateTestCaseStates(testResult, planScenarioId, runMode);

        List<String> ids = testPlanTestCaseService.getTestPlanTestCaseIds(testResult.getTestId());
        if (ids.size() > 0) {
            try {
                if (StringUtils.equals(APITestStatus.Success.name(), report.getStatus())) {
                    testPlanTestCaseService.updateTestCaseStates(ids, TestPlanTestCaseStatus.Pass.name());
                } else {
                    testPlanTestCaseService.updateTestCaseStates(ids, TestPlanTestCaseStatus.Failure.name());
                }
            } catch (Exception e) {

            }
        }
        if (reportTask != null) {
            if (StringUtils.equals(ReportTriggerMode.API.name(), reportTask.getTriggerMode()) || StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), reportTask.getTriggerMode())) {
                sendTask(reportTask, reportUrl, testResult);
            }
        }

    }

    /**
     * 更新测试计划关联接口测试的功能用例的状态
     * @param testResult
     */
    private void updateTestCaseStates(TestResult testResult, String testPlanScenarioId, String runMode) {
        try {
            if (StringUtils.equalsAny(runMode, ApiRunMode.API_PLAN.name(), ApiRunMode.SCHEDULE_API_PLAN.name(),
                    ApiRunMode.JENKINS_API_PLAN.name(), ApiRunMode.SCENARIO_PLAN.name(), ApiRunMode.SCHEDULE_SCENARIO_PLAN.name())) {
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

    private static void sendTask(ApiTestReportVariable report, String reportUrl, TestResult testResult) {
        if (report == null) {
            return;
        }
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
        assert systemParameterService != null;
        assert noticeSendService != null;
        BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
        String url = baseSystemConfigDTO.getUrl() + "/#/api/report/view/" + report.getId();
        String url2 = baseSystemConfigDTO.getUrl() + "/#/api/automation/report/view/" + report.getId();

        String successContext = "";
        String failedContext = "";
        String subject = "";
        String event = "";
        if (StringUtils.equals(ReportTriggerMode.API.name(), report.getTriggerMode())) {
            successContext = "接口测试 API任务通知:'" + report.getExecutor() + "所执行的" + report.getName() + "'执行成功" + "\n" + "执行环境:" + report.getExecutionEnvironment() + "\n" + "[接口定义暂无报告链接]" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + "（旧版）接口测试路径" + url + "\n" + "（新版）接口测试路径" + url2;
            failedContext = "接口测试 API任务通知:'" + report.getExecutor() + "所执行的" + report.getName() + "'执行失败" + "\n" + "执行环境:" + report.getExecutionEnvironment() + "\n" + "[接口定义暂无报告链接]" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + "（旧版）接口测试路径" + url + "\n" + "（新版）接口测试路径" + url2;
            subject = Translator.get("task_notification_jenkins");
        }
        if (StringUtils.equals(ReportTriggerMode.SCHEDULE.name(), report.getTriggerMode())) {
            successContext = "接口测试定时任务通知:'" + report.getExecutor() + "所执行的" + report.getName() + "'执行成功" + "\n" + "执行环境:" + report.getExecutionEnvironment() + "\n" + "[接口定义暂无报告链接]" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + "（旧版）接口测试路径" + url + "\n" + "（新版）接口测试路径" + url2;
            failedContext = "接口测试定时任务通知:'" + report.getExecutor() + "所执行的" + report.getName() + "'执行失败" + "\n" + "执行环境:" + report.getExecutionEnvironment() + "\n" + "[接口定义暂无报告链接]" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + "（旧版）接口测试路径" + url + "\n" + "（新版）接口测试路径" + url2;
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
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("testName", report.getName());
        paramMap.put("id", report.getId());
        paramMap.put("type", "api");
        paramMap.put("url", baseSystemConfigDTO.getUrl());
        paramMap.put("status", report.getStatus());
        paramMap.put("executor", report.getExecutor());
        paramMap.put("executionTime", report.getExecutionTime());
        paramMap.put("executionEnvironment", report.getExecutionEnvironment());
        NoticeModel noticeModel = NoticeModel.builder()
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
        noticeSendService.send(report.getTriggerMode(), noticeModel);
    }

    private RequestResult getRequestResult(SampleResult result) {
        RequestResult requestResult = new RequestResult();
        requestResult.setId(result.getSamplerId());
        requestResult.setName(result.getSampleLabel());
        requestResult.setUrl(result.getUrlAsString());
        requestResult.setMethod(getMethod(result));
        requestResult.setBody(result.getSamplerData());
        requestResult.setHeaders(result.getRequestHeaders());
        requestResult.setRequestSize(result.getSentBytes());
        requestResult.setStartTime(result.getStartTime());
        requestResult.setEndTime(result.getEndTime());
        requestResult.setTotalAssertions(result.getAssertionResults().length);
        requestResult.setSuccess(result.isSuccessful());
        requestResult.setError(result.getErrorCount());
        requestResult.setScenario(result.getScenario());
        if (result instanceof HTTPSampleResult) {
            HTTPSampleResult res = (HTTPSampleResult) result;
            requestResult.setCookies(res.getCookies());
        }

        for (SampleResult subResult : result.getSubResults()) {
            requestResult.getSubRequestResults().add(getRequestResult(subResult));
        }
        ResponseResult responseResult = requestResult.getResponseResult();
        responseResult.setBody(result.getResponseDataAsString());
        responseResult.setHeaders(result.getResponseHeaders());
        responseResult.setLatency(result.getLatency());
        responseResult.setResponseCode(result.getResponseCode());
        responseResult.setResponseSize(result.getResponseData().length);
        responseResult.setResponseTime(result.getTime());
        responseResult.setResponseMessage(result.getResponseMessage());
        if (JMeterVars.get(result.hashCode()) != null && CollectionUtils.isNotEmpty(JMeterVars.get(result.hashCode()).entrySet())) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, Object> entry : JMeterVars.get(result.hashCode()).entrySet()) {
                builder.append(entry.getKey()).append("：").append(entry.getValue()).append("\n");
            }
            if (StringUtils.isNotEmpty(builder)) {
                responseResult.setVars(builder.toString());
            }
            JMeterVars.remove(result.hashCode());
        }
        for (AssertionResult assertionResult : result.getAssertionResults()) {
            ResponseAssertionResult responseAssertionResult = getResponseAssertionResult(assertionResult);
            if (responseAssertionResult.isPass()) {
                requestResult.addPassAssertions();
            }
            //xpath 提取错误会添加断言错误
            if (StringUtils.isBlank(responseAssertionResult.getMessage()) ||
                    (StringUtils.isNotBlank(responseAssertionResult.getName()) && !responseAssertionResult.getName().endsWith("XPath2Extractor"))) {
                responseResult.getAssertions().add(responseAssertionResult);
            }
        }
        return requestResult;
    }

    private String getMethod(SampleResult result) {
        String body = result.getSamplerData();
        // Dubbo Protocol
        String start = "RPC Protocol: ";
        String end = "://";
        if (StringUtils.contains(body, start)) {
            String protocol = StringUtils.substringBetween(body, start, end);
            if (StringUtils.isNotEmpty(protocol)) {
                return protocol.toUpperCase();
            }
            return RequestType.DUBBO;
        } else if (StringUtils.contains(result.getResponseHeaders(), "url:jdbc")) {
            return "SQL";
        } else {
            // Http Method
            String method = StringUtils.substringBefore(body, " ");
            for (HttpMethod value : HttpMethod.values()) {
                if (StringUtils.equals(method, value.name())) {
                    return method;
                }
            }
            return "Request";
        }
    }

    private ResponseAssertionResult getResponseAssertionResult(AssertionResult assertionResult) {
        ResponseAssertionResult responseAssertionResult = new ResponseAssertionResult();
        responseAssertionResult.setName(assertionResult.getName());
        responseAssertionResult.setPass(!assertionResult.isFailure() && !assertionResult.isError());
        if (!responseAssertionResult.isPass()) {
            responseAssertionResult.setMessage(assertionResult.getFailureMessage());
        }
        return responseAssertionResult;
    }
}
