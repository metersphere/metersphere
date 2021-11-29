package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.jmeter.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterVariables;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.security.util.Cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class MsResultService {
    // 零时存放实时结果
    private final Cache cache = Cache.newHardMemoryCache(0, 3600 * 2);
    private final ConcurrentHashMap<String, List<SampleResult>> processCache = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, List<SampleResult>> getProcessCache() {
        return processCache;
    }

    private final static String THREAD_SPLIT = " ";

    private final static String ID_SPLIT = "-";

    public TestResult getResult(String key) {
        if (this.cache.get(key) != null) {
            return (TestResult) this.cache.get(key);
        }
        return null;
    }

    public List<SampleResult> procResult(String key) {
        if (this.processCache.get(key) != null) {
            return this.processCache.get(key);
        }
        return new LinkedList<>();
    }

    public void setCache(String key, SampleResult result) {
        if (key.startsWith("[") && key.endsWith("]")) {
            key = JSON.parseArray(key).get(0).toString();
        }
        List<SampleResult> testResult = this.procResult(key);
        testResult.add(result);
        this.processCache.put(key, testResult);
    }

    public TestResult synSampleResult(String key) {
        if (key.startsWith("[") && key.endsWith("]")) {
            key = JSON.parseArray(key).get(0).toString();
        }
        String logs = getJmeterLogger(key, false);
        List<SampleResult> results = this.processCache.get(key);
        boolean isRemove = false;
        TestResult testResult = (TestResult) cache.get(key);
        if (testResult == null) {
            testResult = new TestResult();
        }
        if (CollectionUtils.isNotEmpty(results)) {
            final Map<String, ScenarioResult> scenarios = new LinkedHashMap<>();
            for (SampleResult result : results) {
                if (result.getResponseCode().equals(MsResultCollector.TEST_END)) {
                    testResult.setEnd(true);
                    this.cache.put(key, testResult);
                    isRemove = true;
                    break;
                }
                testResult.setTestId(key);
                if (StringUtils.isNotEmpty(logs)) {
                    testResult.setConsole(logs);
                }
                testResult.setTotal(0);
                this.formatTestResult(testResult, scenarios, result);
            }
            testResult.getScenarios().clear();
            testResult.getScenarios().addAll(scenarios.values());
            testResult.getScenarios().sort(Comparator.comparing(ScenarioResult::getId));
            this.cache.put(key, testResult);

            if (isRemove) {
                this.processCache.remove(key);
            }
        }
        return (TestResult) cache.get(key);
    }

    public void delete(String testId) {
        this.cache.remove(testId);
        MessageCache.reportCache.remove(testId);
    }

    public void formatTestResult(TestResult testResult, Map<String, ScenarioResult> scenarios, SampleResult result) {
        String scenarioName = StringUtils.substringBeforeLast(result.getThreadName(), THREAD_SPLIT);
        if (StringUtils.equals(scenarioName, "parallel")) {
            scenarioName = testResult.getTestId();
        }
        ScenarioResult scenarioResult;
        if (!scenarios.containsKey(scenarioName)) {
            scenarioResult = new ScenarioResult();
            scenarioResult.setId(1);
            if (StringUtils.equals(testResult.getTestId(), scenarioName)) {
                scenarioResult.setName(scenarioName);
            } else {
                scenarioResult.setName(testResult.getTestId());
            }
            scenarios.put(scenarioName, scenarioResult);
        } else {
            scenarioResult = scenarios.get(scenarioName);
        }
        if (result.isSuccessful()) {
            scenarioResult.addSuccess();
            testResult.addSuccess();
        } else {
            scenarioResult.addError(result.getErrorCount());
            testResult.addError(result.getErrorCount());
        }
        RequestResult requestResult = this.getRequestResult(result);
        scenarioResult.getRequestResults().add(requestResult);
        scenarioResult.addResponseTime(result.getTime());

        testResult.addPassAssertions(requestResult.getPassAssertions());
        testResult.addTotalAssertions(requestResult.getTotalAssertions());
        testResult.setTotal(testResult.getTotal() + 1);
        scenarioResult.addPassAssertions(requestResult.getPassAssertions());
        scenarioResult.addTotalAssertions(requestResult.getTotalAssertions());
    }

    public String getJmeterLogger(String testId, boolean removed) {
        try {
            Long startTime = FixedTask.tasks.get(testId);
            if (startTime == null) {
                startTime = FixedTask.tasks.get("[" + testId + "]");
            }
            if (startTime == null) {
                startTime = System.currentTimeMillis();
            }
            Long endTime = System.currentTimeMillis();
            Long finalStartTime = startTime;
            String logMessage = FixedCapacityUtils.fixedCapacityCache.entrySet().stream()
                    .filter(map -> map.getKey() > finalStartTime && map.getKey() < endTime)
                    .map(map -> map.getValue()).collect(Collectors.joining());
            if (removed) {
                if (processCache.get(testId) != null) {
                    try {
                        Thread.sleep(2000);
                    } catch (Exception e) {
                    }
                }
                FixedTask.tasks.remove(testId);
            }
            return logMessage;
        } catch (Exception e) {
            return "";
        }
    }

    public RequestResult getRequestResult(SampleResult result) {
        RequestResult requestResult = new RequestResult();
        requestResult.setId(result.getSamplerId());
        requestResult.setResourceId(result.getResourceId());
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
        JMeterVariables variables = JMeterVars.get(result.hashCode());
        if (StringUtils.isNotEmpty(result.getExtVars())) {
            responseResult.setVars(result.getExtVars());
            JMeterVars.remove(result.hashCode());
        } else if (variables != null && CollectionUtils.isNotEmpty(variables.entrySet())) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
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
                    (StringUtils.isNotBlank(responseAssertionResult.getName()) && !responseAssertionResult.getName().endsWith("XPath2Extractor"))
                    || (StringUtils.isNotBlank(responseAssertionResult.getContent()) && !responseAssertionResult.getContent().endsWith("XPath2Extractor"))
            ) {
                responseResult.getAssertions().add(responseAssertionResult);
            }
        }
        return requestResult;
    }

    private String getMethod(SampleResult result) {
        String body = result.getSamplerData();
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
        if (StringUtils.isNotEmpty(assertionResult.getName()) && assertionResult.getName().indexOf("==") != -1) {
            if (assertionResult.getName().indexOf("JSR223") != -1) {
                String[] array = assertionResult.getName().split("==", 3);
                if ("JSR223".equals(array[0])) {
                    responseAssertionResult.setName(array[1]);
                    if (array[2].indexOf("&&") != -1) {
                        String[] content = array[2].split("&&");
                        responseAssertionResult.setContent(content[0]);
                        if (content.length > 1) {
                            responseAssertionResult.setScript(content[1]);
                        }
                    } else {
                        responseAssertionResult.setContent(array[2]);
                    }
                }
            } else {
                String[] array = assertionResult.getName().split("==");
                responseAssertionResult.setName(array[0]);
                StringBuffer content = new StringBuffer();
                for (int i = 1; i < array.length; i++) {
                    content.append(array[i]);
                }
                responseAssertionResult.setContent(content.toString());
            }
        }
        responseAssertionResult.setPass(!assertionResult.isFailure() && !assertionResult.isError());
        if (!responseAssertionResult.isPass()) {
            responseAssertionResult.setMessage(assertionResult.getFailureMessage());
        }
        return responseAssertionResult;
    }
}
