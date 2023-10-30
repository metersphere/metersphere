package io.metersphere.system.utils;

import com.bastiaanjansen.otp.TOTPGenerator;
import io.metersphere.sdk.constants.MsHttpHeaders;
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

    private static final RestTemplate restTemplateWithTimeOut = new RestTemplate();
    private static final int retryCount = 3;

    static {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(2000);
        httpRequestFactory.setConnectTimeout(2000);
        restTemplateWithTimeOut.setRequestFactory(httpRequestFactory);
    }


    public static ResultHolder get(String url) throws Exception {
        // 定义action
        Action action = (u, body) -> {
            String token = totpGenerator.now();
            HttpHeaders headers = new HttpHeaders();
            headers.add(MsHttpHeaders.OTP_TOKEN, token);
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<ResultHolder> entity = restTemplateWithTimeOut.exchange(u, HttpMethod.GET, httpEntity, ResultHolder.class);
            return entity.getBody();
        };
        // 首次调用
        ResultHolder body = action.execute(url, null);
        return retry(url, body, action);
    }

    public static ResultHolder post(String url, Object requestBody) throws Exception {
        // 定义action
        Action action = (u, b) -> {
            String token = totpGenerator.now();
            HttpHeaders headers = new HttpHeaders();
            headers.add(MsHttpHeaders.OTP_TOKEN, token);
            HttpEntity<Object> httpEntity = new HttpEntity<>(b, headers);
            ResponseEntity<ResultHolder> entity = restTemplateWithTimeOut.exchange(u, HttpMethod.POST, httpEntity, ResultHolder.class);
            restTemplateWithTimeOut.postForEntity(url, httpEntity, ResultHolder.class);
            return entity.getBody();
        };
        // 首次调用
        ResultHolder body = action.execute(url, requestBody);
        return retry(url, body, action);
    }

    private static ResultHolder retry(String url, ResultHolder body, Action action) throws Exception {
        if (body != null && body.getCode() == MsHttpResultCode.SUCCESS.getCode()) {
            return body;
        }
        // 增加token失败重试
        for (int i = 0; i < retryCount; i++) {
            TimeUnit.MILLISECONDS.sleep(300);

            body = action.execute(url, null);
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
