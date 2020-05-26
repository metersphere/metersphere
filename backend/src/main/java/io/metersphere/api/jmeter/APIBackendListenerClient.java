package io.metersphere.api.jmeter;

import io.metersphere.api.service.APIReportService;
import io.metersphere.api.service.APITestService;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
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

    // 测试ID
    private String testId;

    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        this.testId = context.getParameter(TEST_ID);
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
        apiTestService.changeStatus(testId, APITestStatus.Completed);
        apiReportService.complete(testResult);

        queue.clear();
        super.teardownTest(context);
    }

    private RequestResult getRequestResult(SampleResult result) {
        String body = result.getSamplerData();
        String method = StringUtils.substringBefore(body, " ");

        RequestResult requestResult = new RequestResult();
        requestResult.setName(result.getSampleLabel());
        requestResult.setUrl(result.getUrlAsString());
        requestResult.setMethod(method);
        requestResult.setBody(body);
        requestResult.setHeaders(result.getRequestHeaders());
        requestResult.setRequestSize(result.getSentBytes());
        requestResult.setTotalAssertions(result.getAssertionResults().length);
        requestResult.setSuccess(result.isSuccessful());
        requestResult.setError(result.getErrorCount());
        for (SampleResult subResult : result.getSubResults()) {
            requestResult.getSubRequestResults().add(getRequestResult(subResult));
        }

        ResponseResult responseResult = requestResult.getResponseResult();
        responseResult.setBody(new String(result.getResponseData(), StandardCharsets.UTF_8));
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

    private ResponseAssertionResult getResponseAssertionResult(AssertionResult assertionResult) {
        ResponseAssertionResult responseAssertionResult = new ResponseAssertionResult();
        responseAssertionResult.setMessage(assertionResult.getFailureMessage());
        responseAssertionResult.setName(assertionResult.getName());
        responseAssertionResult.setPass(!assertionResult.isFailure());
        return responseAssertionResult;
    }

}
