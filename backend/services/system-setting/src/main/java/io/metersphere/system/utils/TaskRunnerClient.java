package io.metersphere.system.utils;

import com.bastiaanjansen.otp.TOTPGenerator;
import io.metersphere.sdk.constants.MsHttpHeaders;
import io.metersphere.sdk.dto.api.task.TaskRequestDTO;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.controller.handler.result.MsHttpResultCode;
import jakarta.annotation.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Component
public class TaskRunnerClient {
    private static TOTPGenerator totpGenerator;

    private static final String API_DEBUG = "/api/debug";
    private static final String HTTP_BATH = "http://%s:%s";

    private static final RestTemplate restTemplateWithTimeOut = new RestTemplate();
    private static final int retryCount = 3;

    static {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(2000);
        httpRequestFactory.setConnectTimeout(2000);
        restTemplateWithTimeOut.setRequestFactory(httpRequestFactory);
    }

    public static void debugApi(String endpoint, TaskRequestDTO taskRequest) throws Exception {
        post(endpoint + API_DEBUG, taskRequest);
    }

    public static String getEndpoint(String ip, String port) {
        return String.format(HTTP_BATH, ip, port);
    }

    public static ResultHolder get(String url,  Object... uriVariables) throws Exception {
        // 定义action
        Action action = (u, body) -> {
            String token = totpGenerator.now();
            HttpHeaders headers = new HttpHeaders();
            headers.add(MsHttpHeaders.OTP_TOKEN, token);
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<ResultHolder> entity = restTemplateWithTimeOut.exchange(u, HttpMethod.GET, httpEntity, ResultHolder.class, uriVariables);
            return entity.getBody();
        };

        return retry(url, null, action);
    }

    public static ResultHolder post(String url, Object requestBody, Object... uriVariables) throws Exception {
        // 定义action
        Action action = (u, b) -> {
            String token = totpGenerator.now();
            HttpHeaders headers = new HttpHeaders();
            headers.add(MsHttpHeaders.OTP_TOKEN, token);
            HttpEntity<Object> httpEntity = new HttpEntity<>(b, headers);
            ResponseEntity<ResultHolder> entity = restTemplateWithTimeOut.exchange(u, HttpMethod.POST, httpEntity, ResultHolder.class, uriVariables);
            return entity.getBody();
        };

        return retry(url, requestBody, action);
    }

    private static ResultHolder retry(String url, Object requestBody, Action action) throws Exception {
        // 首次调用
        ResultHolder body = action.execute(url, requestBody);
        if (body != null && body.getCode() == MsHttpResultCode.SUCCESS.getCode()) {
            return body;
        }

        // 增加token失败重试
        for (int i = 0; i < retryCount; i++) {
            TimeUnit.MILLISECONDS.sleep(300);

            body = action.execute(url, requestBody);
            // 重试后检查是否成功
            if (body != null && body.getCode() == MsHttpResultCode.SUCCESS.getCode()) {
                return body;
            }
        }
        return body;
    }

    @Resource
    public void setTotpGenerator(TOTPGenerator totpGenerator) {
        TaskRunnerClient.totpGenerator = totpGenerator;
    }

    @FunctionalInterface
    private interface Action {
        ResultHolder execute(String url, Object body);
    }

}
