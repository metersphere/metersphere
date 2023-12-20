package io.metersphere.autoconfigure;


import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.utils.LoggerUtil;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;


public class RestTemplateConfig {
    private final static int RETRY_COUNT = 3;
    private final static long RETRY_INTERVAL_TIME = 1000L;
    private final static int MAX_TOTAL = 1000;
    private final static int MAX_PER_ROUTE = 500;
    private final static int CONN_REQUEST_TIMEOUT = 5000;
    private final static int CONNECT_TIMEOUT = 8000;
    private final static int SOCKET_TIMEOUT = 200 * 1000;

    @Bean
    public RestTemplate restTemplate() {
        return setTemplate();
    }

    @Bean
    public RestTemplate restTemplateWithTimeOut() {
        return setTemplate();
    }

    @Bean
    public HttpClient httpClient() {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        //设置整个连接池最大连接数
        connectionManager.setMaxTotal(MAX_TOTAL);
        //路由是对maxTotal的细分
        connectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);
        //服务器返回数据(response)的时间，超过该时间抛出read timeout
        connectionManager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(SOCKET_TIMEOUT, TimeUnit.MILLISECONDS).build());

        RequestConfig requestConfig = RequestConfig.custom()
                //连接上服务器(握手成功)的时间，超出该时间抛出connect timeout
                .setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                //从连接池中获取连接的超时时间，超过该时间未拿到可用连接，Timeout waiting for connection from pool
                .setConnectionRequestTimeout(CONN_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
        // 重试次数
        HttpClientBuilder client = getHttpClientBuilder();
        client.setDefaultRequestConfig(requestConfig);
        // 配置连接池
        client.setConnectionManager(connectionManager);
        return client.build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getFactory()
                .setStreamReadConstraints(StreamReadConstraints.builder().maxStringLength(JSON.DEFAULT_MAX_STRING_LEN).build());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private RestTemplate setTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient()));
        return restTemplate;
    }

    private HttpClientBuilder getHttpClientBuilder() {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        // 只有io异常才会触发重试
        httpClientBuilder.setRetryStrategy(new DefaultHttpRequestRetryStrategy() {
            @Override
            public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
                // curRetryCount 每一次都会递增，从1开始
                if (execCount > RETRY_COUNT) return false;
                try {
                    //重试延迟
                    Thread.sleep(execCount * RETRY_INTERVAL_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    LogUtil.error(e);
                }
                if (exception instanceof ConnectTimeoutException ||
                        exception instanceof NoHttpResponseException ||
                        exception instanceof ConnectException ||
                        exception instanceof SocketException) {
                    LoggerUtil.info("重试次数: " + execCount);
                    return true;
                }
                return false;
            }
        });
        return httpClientBuilder;
    }

}
