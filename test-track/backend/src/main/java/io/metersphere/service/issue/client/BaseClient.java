package io.metersphere.service.issue.client;

import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import java.util.Arrays;

public abstract class BaseClient {

    protected RestTemplate restTemplate;

    {
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

            HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(csf)
                    .build();

            CloseableHttpClient httpClient = HttpClients.custom()
                    // 可以支持设置系统代理
                    .setRoutePlanner(new SystemDefaultRoutePlanner(new EnvProxySelector()))
                    .setConnectionManager(cm)
                    .build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);

            restTemplate = new RestTemplate(requestFactory);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    protected HttpHeaders getBasicHttpHeaders(String userName, String passWd) {
        String authKey = EncryptUtils.base64Encoding(userName + ":" + passWd);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + authKey);
        headers.add("Accept", "application/json");
        return headers;
    }

    protected String getResult(ResponseEntity<String> response) {
        int statusCodeValue = response.getStatusCodeValue();
        LogUtil.info("responseCode: " + statusCodeValue);
        if (statusCodeValue >= 400) {
            MSException.throwException(response.getBody());
        }
        LogUtil.info("result: " + response.getBody());
        return response.getBody();
    }

    protected Object getResultForList(Class clazz, ResponseEntity<String> response) {
        return Arrays.asList(JSON.parseArray(getResult(response), clazz).toArray());
    }

    protected Object getResultForObject(Class clazz, ResponseEntity<String> response) {
        return JSON.parseObject(getResult(response), clazz);
    }
}
