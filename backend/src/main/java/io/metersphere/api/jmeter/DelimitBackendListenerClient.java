package io.metersphere.api.jmeter;

import io.metersphere.api.service.ApiDelimitExecResultService;
import io.metersphere.api.service.ApiDelimitService;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
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
public class DelimitBackendListenerClient extends AbstractBackendListenerClient implements Serializable {

    public final static String TEST_ID = "ms.test.id";

    private final List<SampleResult> queue = new ArrayList<>();

    public String runMode = ApiRunMode.RUN.name();
    private ApiDelimitService apiDelimitService;
    private ApiDelimitExecResultService apiDelimitExecResultService;
    // 测试ID
    private String testId;
    // 运行结果报告ID
    private String reportId;

    @Override
    public void setupTest(BackendListenerContext context) throws Exception {
        setParam(context);
        apiDelimitService = CommonBeanFactory.getBean(ApiDelimitService.class);
        if (apiDelimitService == null) {
            LogUtil.error("apiDelimitService is required");
        }
        apiDelimitExecResultService = CommonBeanFactory.getBean(ApiDelimitExecResultService.class);
        if (apiDelimitExecResultService == null) {
            LogUtil.error("apiDelimitExecResultService is required");
        }
        super.setupTest(context);
    }


    @Override
    public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
        queue.addAll(sampleResults);
    }

    @Override
    public void teardownTest(BackendListenerContext context) throws Exception {
        ApiTestResult testResult = new ApiTestResult();
        testResult.setId(testId);
        queue.forEach(result -> {
            RequestResult reqResult = getRequestResult(result);
            testResult.addRequestResult(reqResult);
            testResult.addPassAssertions(reqResult.getPassAssertions());
            testResult.addTotalAssertions(reqResult.getTotalAssertions());
        });
        // 调试操作，不需要存储结果
        if (StringUtils.isBlank(reportId)) {
            apiDelimitService.addResult(testResult);
        } else {
            apiDelimitExecResultService.saveApiResult(testResult);
        }
        queue.clear();
        super.teardownTest(context);
    }


    private RequestResult getRequestResult(SampleResult result) {
        RequestResult reqResult = new RequestResult();
        reqResult.setName(result.getSampleLabel());
        reqResult.setUrl(result.getUrlAsString());
        reqResult.setMethod(getMethod(result));
        reqResult.setBody(result.getSamplerData());
        reqResult.setHeaders(result.getRequestHeaders());
        reqResult.setRequestSize(result.getSentBytes());
        reqResult.setStartTime(result.getStartTime());
        reqResult.setTotalAssertions(result.getAssertionResults().length);
        reqResult.setSuccess(result.isSuccessful());
        reqResult.setError(result.getErrorCount());
        for (SampleResult subResult : result.getSubResults()) {
            reqResult.getSubRequestResults().add(getRequestResult(subResult));
        }

        ResponseResult responseResult = reqResult.getResponseResult();
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
                reqResult.addPassAssertions();
            }
            responseResult.getAssertions().add(responseAssertionResult);
        }
        return reqResult;
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
        this.reportId = context.getParameter("reportId");
        if (StringUtils.isBlank(this.runMode)) {
            this.runMode = ApiRunMode.RUN.name();
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
