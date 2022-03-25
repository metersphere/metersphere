package io.metersphere.config;

import io.metersphere.commons.utils.LogUtil;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.net.ConnectException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public RestTemplate restTemplate() {
        return getTimeOutTemplate(10 * 1000, 10 * 1000, 30 * 1000);
    }

    @Bean
    public RestTemplate restTemplateWithTimeOut() {
        return getTimeOutTemplate(4000, 4000, 10 * 1000);
    }

    private RestTemplate getTimeOutTemplate(int requestTimeout, int connectTimeout, int readTimeout) {
        RestTemplate restTemplate = new RestTemplate();
        HttpComponentsClientHttpRequestFactory httpRequestFactory =
                new HttpComponentsClientHttpRequestFactory(getHttpClientBuilder().build());
        httpRequestFactory.setConnectionRequestTimeout(requestTimeout);
        httpRequestFactory.setConnectTimeout(connectTimeout);
        httpRequestFactory.setReadTimeout(readTimeout);
        restTemplate.setRequestFactory(httpRequestFactory);
        return restTemplate;
    }

    private HttpClientBuilder getHttpClientBuilder() {
        final int retryTimes = 3;
        final long retryIntervalTime = 1000L;

        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        // 只有io异常才会触发重试
        httpClientBuilder.setRetryHandler((IOException exception, int curRetryCount, HttpContext context) -> {
            // curRetryCount 每一次都会递增，从1开始
            if (curRetryCount > retryTimes) return false;
            try {
                //重试延迟
                Thread.sleep(curRetryCount * retryIntervalTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
                LogUtil.error(e);
            }
            if (exception instanceof ConnectTimeoutException ||
                    exception instanceof NoHttpResponseException ||
                    exception instanceof ConnectException) {
                LogUtil.error("重试次数: " + curRetryCount);
                return true;
            }
            return false;
        });
        return httpClientBuilder;
    }

}
