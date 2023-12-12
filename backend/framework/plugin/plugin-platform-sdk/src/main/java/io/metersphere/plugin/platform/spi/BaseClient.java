package io.metersphere.plugin.platform.spi;

import io.metersphere.plugin.platform.utils.EnvProxySelector;
import io.metersphere.plugin.platform.utils.PluginCodingUtils;
import io.metersphere.plugin.sdk.util.MSPluginException;
import io.metersphere.plugin.sdk.util.PluginLogUtils;
import io.metersphere.plugin.sdk.util.PluginUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.List;

public abstract class BaseClient {

    protected RestTemplate restTemplate;

    public BaseClient() {
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();

            SSLConnectionSocketFactory csf = SSLConnectionSocketFactoryBuilder
                    .create()
                    .setSslContext(sslContext)
                    .build();

            CloseableHttpClient httpClient = HttpClients.custom()
                    // 可以支持设置系统代理
                    .setRoutePlanner(new SystemDefaultRoutePlanner(new EnvProxySelector()))
                    // 忽略 https
                    .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                            .setSSLSocketFactory(csf)
                            .build())
                    .build();

            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);

            restTemplate = new RestTemplate(requestFactory);
        } catch (Exception e) {
            PluginLogUtils.error(e);
        }
    }

    protected HttpHeaders getBasicHttpHeaders(String userName, String passWd) {
        String authKey = PluginCodingUtils.base64Encoding(userName + ":" + passWd);
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(authKey);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    protected HttpHeaders getBearHttpHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    protected String getResult(ResponseEntity<String> response) {
        int statusCodeValue = response.getStatusCodeValue();
        PluginLogUtils.info("responseCode: " + statusCodeValue);
        if (statusCodeValue >= 400) {
            throw new MSPluginException(response.getBody());
        }
        PluginLogUtils.info("result: " + response.getBody());
        return response.getBody();
    }

    protected <T> List<T> getResultForList(Class<T> clazz, ResponseEntity<String> response) {
        return PluginUtils.parseArray(getResult(response), clazz);
    }

    protected <T> T getResultForObject(Class<T> clazz, ResponseEntity<String> response) {
        return PluginUtils.parseObject(getResult(response), clazz);
    }

    public void validateProxyUrl(String url, String... path) {
        try {
            if (!StringUtils.containsAny(new URI(url).getPath(), path)) {
                // 只允许访问图片
                throw new MSPluginException("illegal path");
            }
        } catch (URISyntaxException e) {
            PluginLogUtils.error(e);
            throw new MSPluginException("illegal path");
        }
    }
}
