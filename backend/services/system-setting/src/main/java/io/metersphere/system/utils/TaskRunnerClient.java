package io.metersphere.system.utils;

import com.bastiaanjansen.otp.TOTPGenerator;
import io.metersphere.sdk.constants.MsHttpHeaders;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.controller.handler.result.MsHttpResultCode;
import jakarta.annotation.Resource;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.SocketException;

@Component
public class TaskRunnerClient {
    private static TOTPGenerator totpGenerator;

    private static final String HTTP_BATH = "http://%s:%s";

    private static final RestTemplate restTemplateWithTimeOut = new RestTemplate();
    private final static int RETRY_COUNT = 3;
    private final static long RETRY_INTERVAL_TIME = 1000L;

    static {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(2000);
        httpRequestFactory.setConnectTimeout(2000);
        restTemplateWithTimeOut.setRequestFactory(httpRequestFactory);
    }

    public static String getEndpoint(String ip, String port) {
        return String.format(HTTP_BATH, ip, port);
    }

    public static ResultHolder get(String url, Object... uriVariables) throws Exception {
        // 定义action
        Action action = (u, body) -> {
            String token = totpGenerator.now();
            HttpHeaders headers = new HttpHeaders();
            headers.add(MsHttpHeaders.OTP_TOKEN, token);
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            headers.add(HttpHeaders.ACCEPT, "application/json");
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<ResultHolder> entity = restTemplateWithTimeOut.exchange(u, HttpMethod.GET, httpEntity, ResultHolder.class, uriVariables);
            return entity.getBody();
        };

        return retry(url, null, action);
    }

    public static ResultHolder post(String url, Object param, Object... uriVariables) throws Exception {
        // 定义action
        Action action = (u, body) -> {
            String token = totpGenerator.now();
            HttpHeaders headers = new HttpHeaders();
            headers.add(MsHttpHeaders.OTP_TOKEN, token);
            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
            headers.add(HttpHeaders.ACCEPT, "application/json");
            HttpEntity<Object> httpEntity = new HttpEntity<>(param, headers);
            ResponseEntity<ResultHolder> entity = restTemplateWithTimeOut.exchange(u, HttpMethod.POST, httpEntity, ResultHolder.class, uriVariables);
            return entity.getBody();
        };

        return retry(url, null, action);
    }

    private static ResultHolder retry(String url, Object requestBody, Action action) throws Exception {
        ResultHolder body;
        try {
            // 首次调用
            body = action.execute(url, requestBody);
            if (body != null && body.getCode() == MsHttpResultCode.SUCCESS.getCode()) {
                return body;
            }
        } catch (NoHttpResponseException | ConnectTimeoutException | SocketException | ResourceAccessException e) {
            return doRetry(url, requestBody, action, e);
        }
        return body;
    }

    private static ResultHolder doRetry(String url, Object requestBody, Action action, Exception e) throws Exception {
        ResultHolder body;
        // 增加token失败重试
        for (int i = 1; i <= RETRY_COUNT; i++) {
            LogUtils.error(e);
            LogUtils.error("retry count {}", i);

            try {
                //重试延迟
                Thread.sleep(i * RETRY_INTERVAL_TIME);
            } catch (InterruptedException interruptedException) {
                LogUtils.error(interruptedException);
            }

            try {
                body = action.execute(url, requestBody);
                // 重试后检查是否成功
                if (body != null && body.getCode() == MsHttpResultCode.SUCCESS.getCode()) {
                    return body;
                }
            } catch (NoHttpResponseException | ConnectTimeoutException | SocketException retryException) {
                LogUtils.error("retry count {} fail", i);
            }
        }
        throw e;
    }

    @Resource
    public void setTotpGenerator(TOTPGenerator totpGenerator) {
        TaskRunnerClient.totpGenerator = totpGenerator;
    }

    @FunctionalInterface
    private interface Action {
        ResultHolder execute(String url, Object body) throws IOException;
    }

}
