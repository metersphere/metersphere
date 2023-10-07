package io.metersphere.jmeter;

import com.alibaba.fastjson.JSON;
import io.metersphere.constants.BackendListenerConstants;
import io.metersphere.constants.HttpMethodConstants;
import io.metersphere.dto.*;
import io.metersphere.enums.ApiReportStatus;
import io.metersphere.utils.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.visualizers.backend.BackendListener;
import org.apache.jorphan.collections.HashTree;

import java.lang.reflect.Field;
import java.util.*;

public class JMeterBase {
    private final static String THREAD_SPLIT = " ";
    private final static String TRANSACTION = "Transaction=";
    private final static String SPLIT_EQ = "split==";
    private final static String SPLIT_AND = "split&&";

    private static final List<String> imageList = Arrays.asList("image/png", "image/jpeg", "image/gif", "image/bmp", "image/webp", "image/svg+xml", "image/apng", "image/avif");

    public static HashTree getHashTree(Object scriptWrapper) throws Exception {
        Field field = scriptWrapper.getClass().getDeclaredField("testPlan");
        field.setAccessible(true);
        return (HashTree) field.get(scriptWrapper);
    }

    public static void addBackendListener(JmeterRunRequestDTO request, HashTree hashTree, String listenerClazz) {
        LoggerUtil.info("开始为报告添加BackendListener", request.getReportId());

        BackendListener backendListener = new BackendListener();
        backendListener.setName(request.getReportId() + "_" + request.getTestId());
        Arguments arguments = new Arguments();
        arguments.addArgument(BackendListenerConstants.NAME.name(), request.getReportId() + "_" + request.getTestId());
        arguments.addArgument(BackendListenerConstants.REPORT_ID.name(), request.getReportId());
        arguments.addArgument(BackendListenerConstants.TEST_ID.name(), request.getTestId());
        arguments.addArgument(BackendListenerConstants.RUN_MODE.name(), request.getRunMode());
        arguments.addArgument(BackendListenerConstants.REPORT_TYPE.name(), request.getReportType());
        arguments.addArgument(BackendListenerConstants.MS_TEST_PLAN_REPORT_ID.name(), request.getTestPlanReportId());
        arguments.addArgument(BackendListenerConstants.CLASS_NAME.name(), listenerClazz);
        arguments.addArgument(BackendListenerConstants.QUEUE_ID.name(), request.getQueueId());
        arguments.addArgument(BackendListenerConstants.RUN_TYPE.name(), request.getRunType());
        arguments.addArgument(BackendListenerConstants.RETRY_ENABLE.name(), String.valueOf(request.isRetryEnable()));
        if (MapUtils.isNotEmpty(request.getFakeErrorMap())) {
            arguments.addArgument(BackendListenerConstants.FAKE_ERROR.name(), JSON.toJSONString(request.getFakeErrorMap()));
        }
        if (MapUtils.isNotEmpty(request.getExtendedParameters())) {
            arguments.addArgument(BackendListenerConstants.EPT.name(), JsonUtils.toJSONString(request.getExtendedParameters()));
        }
        if (request.getKafkaConfig() != null && request.getKafkaConfig().size() > 0) {
            arguments.addArgument(BackendListenerConstants.KAFKA_CONFIG.name(), JsonUtils.toJSONString(request.getKafkaConfig()));
        }
        backendListener.setArguments(arguments);
        backendListener.setClassname(listenerClazz);
        if (hashTree != null) {
            hashTree.add(hashTree.getArray()[0], backendListener);
        }
        LoggerUtil.info("报告添加BackendListener 结束", request.getTestId());
    }

    public static RequestResult getRequestResult(SampleResult result, Map<String, List<MsRegexDTO>> fakeErrorMap) {
        LoggerUtil.debug("开始处理结果资源【" + result.getSampleLabel() + "】");
        String threadName = StringUtils.substringBeforeLast(result.getThreadName(), THREAD_SPLIT);
        RequestResult requestResult = new RequestResult();
        requestResult.setThreadName(threadName);
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
        requestResult.setFakeErrorMessage(result.getFakeError());
        if (result instanceof HTTPSampleResult) {
            HTTPSampleResult res = (HTTPSampleResult) result;
            requestResult.setCookies(res.getCookies());
        }

        for (SampleResult subResult : result.getSubResults()) {
            requestResult.getSubRequestResults().add(getRequestResult(subResult, fakeErrorMap));
        }
        ResponseResult responseResult = requestResult.getResponseResult();
        // 超过20M的文件不入库
        long size = 1024 * 1024 * 20;
        if (StringUtils.equals(ContentType.APPLICATION_OCTET_STREAM.getMimeType(), result.getContentType())
                && StringUtils.isNotEmpty(result.getResponseDataAsString())
                && result.getResponseDataAsString().length() > size) {
            requestResult.setBody("");
        } else {
            //判断返回的类型是否是图片
            if (StringUtils.isNotEmpty(result.getContentType()) && imageList.contains(result.getContentType())) {
                responseResult.setContentType(result.getContentType());
                responseResult.setImageUrl(result.getResponseData());
           }
            responseResult.setBody(result.getResponseDataAsString());
        }
        responseResult.setHeaders(result.getResponseHeaders());
        responseResult.setLatency(result.getLatency());
        responseResult.setResponseCode(result.getResponseCode());
        responseResult.setResponseSize(result.getResponseData().length);
        responseResult.setResponseTime(result.getTime());
        responseResult.setResponseMessage(result.getResponseMessage());
        JMeterVariables variables = JMeterVars.get(result.getResourceId());
        if (StringUtils.isNotEmpty(result.getExtVars())) {
            responseResult.setVars(result.getExtVars());
            JMeterVars.remove(result.getResourceId());
        } else if (variables != null && CollectionUtils.isNotEmpty(variables.entrySet())) {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                builder.append(entry.getKey()).append("：").append(entry.getValue()).append("\n");
            }
            if (StringUtils.isNotEmpty(builder)) {
                responseResult.setVars(builder.toString());
            }
            JMeterVars.remove(result.getResourceId());
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

        LoggerUtil.debug("处理结果资源【" + result.getSampleLabel() + "】结束");
        // 误报处理
        FakeErrorLibraryDTO errorCodeDTO = FakeErrorUtils.parseAssertions(requestResult, fakeErrorMap);
        if (CollectionUtils.isNotEmpty(errorCodeDTO.getErrorCodeList())) {
            requestResult.setFakeErrorCode(errorCodeDTO.getErrorCodeStr());
        }
        String status = requestResult.getError() == 0 ? "SUCCESS" : "ERROR";
        if (StringUtils.equalsIgnoreCase(errorCodeDTO.getRequestStatus(), ApiReportStatus.FAKE_ERROR.name())) {
            status = ApiReportStatus.FAKE_ERROR.name();
        }
        requestResult.setStatus(status);
        return requestResult;
    }

    private static ResponseAssertionResult getResponseAssertionResult(AssertionResult assertionResult) {
        ResponseAssertionResult responseAssertionResult = new ResponseAssertionResult();

        responseAssertionResult.setName(assertionResult.getName());
        if (StringUtils.isNotEmpty(assertionResult.getName()) && assertionResult.getName().indexOf(SPLIT_EQ) != -1) {
            if (assertionResult.getName().indexOf("JSR223") != -1) {
                String[] array = assertionResult.getName().split(SPLIT_EQ, 3);
                if (array.length > 2 && "JSR223".equals(array[0])) {
                    responseAssertionResult.setName(array[1]);
                    if (array[2].indexOf(SPLIT_AND) != -1) {
                        String[] content = array[2].split(SPLIT_AND);
                        responseAssertionResult.setContent(content[0]);
                        if (content.length > 1) {
                            responseAssertionResult.setScript(content[1]);
                        }
                    } else {
                        responseAssertionResult.setContent(array[2]);
                    }
                }
            } else {
                String[] array = assertionResult.getName().split(SPLIT_EQ);
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

    private static String getMethod(SampleResult result) {
        String body = result.getSamplerData();
        String start = "RPC Protocol: ";
        String end = "://";
        if (StringUtils.contains(body, start)) {
            String protocol = StringUtils.substringBetween(body, start, end);
            if (StringUtils.isNotEmpty(protocol)) {
                return protocol.toUpperCase();
            }
            return "DUBBO";
        } else if (StringUtils.contains(result.getResponseHeaders(), "url:jdbc")) {
            return "SQL";
        } else {
            String method = StringUtils.substringBefore(body, " ");
            for (HttpMethodConstants value : HttpMethodConstants.values()) {
                if (StringUtils.equals(method, value.name())) {
                    return method;
                }
            }
            return "Request";
        }
    }

    /**
     * 执行结果数据转化
     *
     * @param sampleResults
     * @param dto
     */
    public static void resultFormatting(List<SampleResult> sampleResults, ResultDTO dto) {
        try {
            List<RequestResult> requestResults = new LinkedList<>();
            List<String> environmentList = new ArrayList<>();
            sampleResults.forEach(result -> {
                ListenerUtil.setVars(result);
                RequestResult requestResult = JMeterBase.getRequestResult(result, dto.getFakeErrorMap());
                if (StringUtils.equals(result.getSampleLabel(), ListenerUtil.RUNNING_DEBUG_SAMPLER_NAME)) {
                    String evnStr = result.getResponseDataAsString();
                    environmentList.add(evnStr);
                } else {
                    //检查是否有关系到最终执行结果的全局前后置脚本。
                    boolean resultNotFilterOut = ListenerUtil.checkResultIsNotFilterOut(requestResult);
                    if (resultNotFilterOut) {
                        if (StringUtils.isNotEmpty(requestResult.getName()) && requestResult.getName().startsWith(TRANSACTION)) {
                            transactionFormat(requestResult.getSubRequestResults(), requestResults);
                        } else {
                            requestResults.add(requestResult);
                        }
                    }
                }
            });
            dto.setRequestResults(requestResults);
            ListenerUtil.setEev(dto, environmentList);
        } catch (Exception e) {
            LoggerUtil.error("JMETER-调用存储方法失败", dto.getReportId(), e);
        }
    }

    private static void transactionFormat(List<RequestResult> requestResults, List<RequestResult> refRes) {
        for (RequestResult requestResult : requestResults) {
            if (CollectionUtils.isEmpty(requestResult.getSubRequestResults())) {
                refRes.add(requestResult);
            } else {
                transactionFormat(requestResult.getSubRequestResults(), refRes);
            }
        }
    }
}
