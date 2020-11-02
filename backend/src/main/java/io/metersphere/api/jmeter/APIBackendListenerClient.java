package io.metersphere.api.jmeter;

import io.metersphere.api.service.APIReportService;
import io.metersphere.api.service.APITestService;
import io.metersphere.base.domain.ApiTestReport;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.BaseSystemConfigDTO;
import io.metersphere.notice.domain.MessageDetail;
import io.metersphere.notice.domain.MessageSettingDetail;
import io.metersphere.notice.service.DingTaskService;
import io.metersphere.notice.service.MailService;
import io.metersphere.notice.service.NoticeService;
import io.metersphere.notice.service.WxChatTaskService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.service.TestPlanTestCaseService;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;
import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.util.*;

/**
 * JMeter BackendListener扩展, jmx脚本中使用
 */
public class APIBackendListenerClient extends AbstractBackendListenerClient implements Serializable {

    public final static String TEST_ID = "ms.test.id";

    private final static String THREAD_SPLIT = " ";

    private final static String ID_SPLIT = "-";

    private final List<SampleResult> queue = new ArrayList<>();

    private APITestService apiTestService;

    private APIReportService apiReportService;

    private TestPlanTestCaseService testPlanTestCaseService;

    private NoticeService noticeService;

    private MailService mailService;

    public String runMode = ApiRunMode.RUN.name();

    // 测试ID
    private String testId;

    private String debugReportId;

    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        setParam(context);
        apiTestService = CommonBeanFactory.getBean(APITestService.class);
        if (apiTestService == null) {
            LogUtil.error("apiTestService is required");
        }

        apiReportService = CommonBeanFactory.getBean(APIReportService.class);
        if (apiReportService == null) {
            LogUtil.error("apiReportService is required");
        }
        testPlanTestCaseService = CommonBeanFactory.getBean(TestPlanTestCaseService.class);
        if (testPlanTestCaseService == null) {
            LogUtil.error("testPlanTestCaseService is required");
        }
        noticeService = CommonBeanFactory.getBean(NoticeService.class);
        if (noticeService == null) {
            LogUtil.error("noticeService is required");
        }
        mailService = CommonBeanFactory.getBean(MailService.class);
        if (mailService == null) {
            LogUtil.error("mailService is required");
        }
        super.setupTest(context);
    }


    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        queue.addAll(sampleResults);
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        TestResult testResult = new TestResult();
        testResult.setTestId(testId);
        testResult.setTotal(queue.size());

        // 一个脚本里可能包含多个场景(ThreadGroup)，所以要区分开，key: 场景Id
        final Map<String, ScenarioResult> scenarios = new LinkedHashMap<>();
        queue.forEach(result -> {
            // 线程名称: <场景名> <场景Index>-<请求Index>, 例如：Scenario 2-1
            String scenarioName = StringUtils.substringBeforeLast(result.getThreadName(), THREAD_SPLIT);
            String index = StringUtils.substringAfterLast(result.getThreadName(), THREAD_SPLIT);
            String scenarioId = StringUtils.substringBefore(index, ID_SPLIT);
            ScenarioResult scenarioResult;
            if (!scenarios.containsKey(scenarioId)) {
                scenarioResult = new ScenarioResult();
                scenarioResult.setId(scenarioId);
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

            scenarioResult.addPassAssertions(requestResult.getPassAssertions());
            scenarioResult.addTotalAssertions(requestResult.getTotalAssertions());
        });

        testResult.getScenarios().addAll(scenarios.values());
        testResult.getScenarios().sort(Comparator.comparing(ScenarioResult::getId));
        ApiTestReport report;
        if (StringUtils.equals(this.runMode, ApiRunMode.DEBUG.name())) {
            report = apiReportService.get(debugReportId);
        } else {
            apiTestService.changeStatus(testId, APITestStatus.Completed);
            report = apiReportService.getRunningReport(testResult.getTestId());
        }
        apiReportService.complete(testResult, report);
        queue.clear();
        super.teardownTest(context);

        TestPlanTestCaseService testPlanTestCaseService = CommonBeanFactory.getBean(TestPlanTestCaseService.class);
        List<String> ids = testPlanTestCaseService.getTestPlanTestCaseIds(testResult.getTestId());
        if (ids.size() > 0) {
            try {
                if (StringUtils.equals(APITestStatus.Success.name(), report.getStatus())) {
                    testPlanTestCaseService.updateTestCaseStates(ids, TestPlanTestCaseStatus.Pass.name());
                } else {
                    testPlanTestCaseService.updateTestCaseStates(ids, TestPlanTestCaseStatus.Failure.name());
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
        try {
            sendTask(report, testResult);
        } catch (Exception e) {
            LogUtil.error(e);
        }

    }

    private static void sendTask(ApiTestReport report, TestResult testResult) {
        NoticeService noticeService = CommonBeanFactory.getBean(NoticeService.class);
        MailService mailService = CommonBeanFactory.getBean(MailService.class);
        DingTaskService dingTaskService = CommonBeanFactory.getBean(DingTaskService.class);
        WxChatTaskService wxChatTaskService = CommonBeanFactory.getBean(WxChatTaskService.class);
        SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
        if (StringUtils.equals(NoticeConstants.API, report.getTriggerMode()) || StringUtils.equals(NoticeConstants.SCHEDULE, report.getTriggerMode())) {
            List<String> userIds = new ArrayList<>();
            List<MessageDetail> taskList = new ArrayList<>();
            String successContext = "";
            String failedContext = "";
            BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
            String url = baseSystemConfigDTO.getUrl() + "/#/api/report/view/" + report.getId();
            if (StringUtils.equals(NoticeConstants.API, report.getTriggerMode())) {
                MessageSettingDetail messageSettingDetail = noticeService.searchMessage();
                taskList = messageSettingDetail.getJenkinsTask();
                successContext = "ApiJenkins任务通知:'" + report.getName() + "'执行成功" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
                failedContext = "ApiJenkins任务通知:'" + report.getName() + "'执行失败" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
            }
            if (StringUtils.equals(NoticeConstants.SCHEDULE, report.getTriggerMode())) {
                taskList = noticeService.searchMessageSchedule(testResult.getTestId());
                successContext = "Api定时任务通知:'" + report.getName() + "'执行成功" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
                failedContext = "Api定时任务通知:'" + report.getName() + "'执行失败" + "\n" + "请点击下面链接进入测试报告页面" + "\n" + url;
            }
            String finalSuccessContext = successContext;
            String finalFailedContext = failedContext;
            taskList.forEach(r -> {
                switch (r.getType()) {
                    case NoticeConstants.NAIL_ROBOT:
                        if (StringUtils.equals(NoticeConstants.EXECUTE_SUCCESSFUL, r.getEvent()) && StringUtils.equals(report.getStatus(), "Success")) {
                            dingTaskService.sendNailRobot(r, userIds, finalSuccessContext, NoticeConstants.EXECUTE_SUCCESSFUL);
                        }
                        if (StringUtils.equals(NoticeConstants.EXECUTE_FAILED, r.getEvent()) && StringUtils.equals(report.getStatus(), "Error")) {
                            dingTaskService.sendNailRobot(r, userIds, finalFailedContext, NoticeConstants.EXECUTE_FAILED);
                        }
                        break;
                    case NoticeConstants.WECHAT_ROBOT:
                        if (StringUtils.equals(NoticeConstants.EXECUTE_SUCCESSFUL, r.getEvent()) && StringUtils.equals(report.getStatus(), "Success")) {
                            wxChatTaskService.sendWechatRobot(r, userIds, finalSuccessContext, NoticeConstants.EXECUTE_SUCCESSFUL);
                        }
                        if (StringUtils.equals(NoticeConstants.EXECUTE_FAILED, r.getEvent()) && StringUtils.equals(report.getStatus(), "Error")) {
                            wxChatTaskService.sendWechatRobot(r, userIds, finalFailedContext, NoticeConstants.EXECUTE_FAILED);
                        }
                        break;
                    case NoticeConstants.EMAIL:
                        if (StringUtils.equals(NoticeConstants.EXECUTE_SUCCESSFUL, r.getEvent()) && StringUtils.equals(report.getStatus(), "Success")) {
                            mailService.sendApiNotification(r, report, NoticeConstants.EXECUTE_SUCCESSFUL);
                        }
                        if (StringUtils.equals(NoticeConstants.EXECUTE_FAILED, r.getEvent()) && StringUtils.equals(report.getStatus(), "Error")) {
                            mailService.sendApiNotification(r, report, NoticeConstants.EXECUTE_FAILED);
                        }
                        break;
                }

            });
        }
    }

    private RequestResult getRequestResult(SampleResult result) {
        RequestResult requestResult = new RequestResult();
        requestResult.setName(result.getSampleLabel());
        requestResult.setUrl(result.getUrlAsString());
        requestResult.setMethod(getMethod(result));
        requestResult.setBody(result.getSamplerData());
        requestResult.setHeaders(result.getRequestHeaders());
        requestResult.setRequestSize(result.getSentBytes());
        requestResult.setStartTime(result.getStartTime());
        requestResult.setTotalAssertions(result.getAssertionResults().length);
        requestResult.setSuccess(result.isSuccessful());
        requestResult.setError(result.getErrorCount());
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

        if (JMeterVars.get(result.hashCode()) != null) {
            List<String> vars = new LinkedList<>();
            JMeterVars.get(result.hashCode()).entrySet().parallelStream().reduce(vars, (first, second) -> {
                first.add(second.getKey() + "：" + second.getValue());
                return first;
            }, (first, second) -> {
                if (first == second) {
                    return first;
                }
                first.addAll(second);
                return first;
            });
            responseResult.setVars(StringUtils.join(vars, "\n"));
            JMeterVars.remove(result.hashCode());
        }
        for (AssertionResult assertionResult : result.getAssertionResults()) {
            ResponseAssertionResult responseAssertionResult = getResponseAssertionResult(assertionResult);
            if (responseAssertionResult.isPass()) {
                requestResult.addPassAssertions();
            }
            responseResult.getAssertions().add(responseAssertionResult);
        }
        return requestResult;
    }

    private String getMethod(SampleResult result) {
        String body = result.getSamplerData();
        // Dubbo Protocol
        String start = "RPC Protocol: ";
        String end = "://";
        if (StringUtils.contains(body, start)) {
            return StringUtils.substringBetween(body, start, end).toUpperCase();
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

    private void setParam(BackendListenerContext context) {
        this.testId = context.getParameter(TEST_ID);
        this.runMode = context.getParameter("runMode");
        this.debugReportId = context.getParameter("debugReportId");
        if (StringUtils.isBlank(this.runMode)) {
            this.runMode = ApiRunMode.RUN.name();
        }
    }

    private ResponseAssertionResult getResponseAssertionResult(AssertionResult assertionResult) {
        ResponseAssertionResult responseAssertionResult = new ResponseAssertionResult();
        responseAssertionResult.setMessage(assertionResult.getFailureMessage());
        responseAssertionResult.setName(assertionResult.getName());
        responseAssertionResult.setPass(!assertionResult.isFailure() && !assertionResult.isError());
        return responseAssertionResult;
    }

}
