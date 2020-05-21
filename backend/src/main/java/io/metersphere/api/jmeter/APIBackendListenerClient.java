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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JMeter BackendListener扩展, jmx脚本中使用
 */
public class APIBackendListenerClient extends AbstractBackendListenerClient implements Serializable {

    // 与前端JMXGenerator的SPLIT对应，用于获取 测试名称 和 测试ID
    private final static String SPLIT = "@@:";
    // 测试ID作为key
    private final Map<String, List<SampleResult>> queue = new ConcurrentHashMap<>();

    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        sampleResults.forEach(result -> {
            // 将不同的测试脚本按测试ID分开
            String label = result.getSampleLabel();
            if (!label.contains(SPLIT)) {
                LogUtil.error("request name format is invalid, name: " + label);
                return;
            }
            String name = label.split(SPLIT)[0];
            String testId = label.split(SPLIT)[1];
            if (!queue.containsKey(testId)) {
                List<SampleResult> testResults = new ArrayList<>();
                queue.put(testId, testResults);
            }
            result.setSampleLabel(name);
            queue.get(testId).add(result);
        });
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        APITestService apiTestService = CommonBeanFactory.getBean(APITestService.class);
        if (apiTestService == null) {
            LogUtil.error("apiTestService is required");
            return;
        }

        APIReportService apiReportService = CommonBeanFactory.getBean(APIReportService.class);
        if (apiReportService == null) {
            LogUtil.error("apiReportService is required");
            return;
        }

        queue.forEach((id, sampleResults) -> {
            TestResult testResult = new TestResult();
            testResult.setId(id);
            testResult.setTotal(sampleResults.size());

            // key: 场景Id
            final Map<String, ScenarioResult> scenarios = new LinkedHashMap<>();

            sampleResults.forEach(result -> {
                String thread = StringUtils.substringBeforeLast(result.getThreadName(), " ");
                String order = StringUtils.substringAfterLast(result.getThreadName(), " ");
                String scenarioName = StringUtils.substringBefore(thread, SPLIT);
                String scenarioId = StringUtils.substringAfter(thread, SPLIT);
                ScenarioResult scenarioResult;
                if (!scenarios.containsKey(scenarioId)) {
                    scenarioResult = new ScenarioResult();
                    scenarioResult.setId(scenarioId);
                    scenarioResult.setName(scenarioName);
                    scenarioResult.setOrder(StringUtils.substringBefore(order, "-"));
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
            testResult.getScenarios().sort(Comparator.comparing(ScenarioResult::getOrder));
            apiTestService.changeStatus(id, APITestStatus.Completed);
            apiReportService.save(testResult);
        });
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

    private ResponseAssertionResult getResponseAssertionResult(AssertionResult assertionResult) {
        ResponseAssertionResult responseAssertionResult = new ResponseAssertionResult();
        responseAssertionResult.setMessage(assertionResult.getFailureMessage());
        responseAssertionResult.setName(assertionResult.getName());
        responseAssertionResult.setPass(!assertionResult.isFailure());
        return responseAssertionResult;
    }

}
