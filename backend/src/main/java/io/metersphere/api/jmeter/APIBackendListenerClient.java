package io.metersphere.api.jmeter;

import io.metersphere.api.service.APIReportService;
import io.metersphere.api.service.APITestService;
import io.metersphere.base.domain.ApiTestReport;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.notice.domain.NoticeDetail;
import io.metersphere.notice.service.MailService;
import io.metersphere.notice.service.NoticeService;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;

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
        ApiTestReport report = null;
        if (StringUtils.equals(this.runMode, ApiRunMode.DEBUG.name())) {
            report = apiReportService.get(debugReportId);
        } else {
            apiTestService.changeStatus(testId, APITestStatus.Completed);
            report = apiReportService.getRunningReport(testResult.getTestId());
        }
        apiReportService.complete(testResult, report);
        queue.clear();
        super.teardownTest(context);
        NoticeService noticeService = CommonBeanFactory.getBean(NoticeService.class);
        try {
            List<NoticeDetail> noticeList = noticeService.queryNotice(testResult.getTestId());
            MailService mailService = CommonBeanFactory.getBean(MailService.class);
            mailService.sendApiNotification(report, noticeList);
        } catch (Exception e) {
            LogUtil.error(e);
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
            return StringUtils.substringBefore(body, " ");
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
        responseAssertionResult.setPass(!assertionResult.isFailure());
        return responseAssertionResult;
    }

}
