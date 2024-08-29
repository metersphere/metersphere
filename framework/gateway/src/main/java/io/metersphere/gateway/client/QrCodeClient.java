package io.metersphere.gateway.client;

import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class QrCodeClient {

    private static final RestTemplate restTemplate;
    private final static int CONNECT_TIMEOUT = 10000;

    static {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 设置连接超时时间（单位：毫秒）
        factory.setConnectTimeout(CONNECT_TIMEOUT);

        // 设置读取超时时间（单位：毫秒）
        factory.setReadTimeout(CONNECT_TIMEOUT);

        restTemplate = new RestTemplate(factory);
    }

    public String get(String url){
        ResponseEntity<Object> forEntity = restTemplate.getForEntity(url, Object.class);

        try {
            return getResponseStr(forEntity);
        } catch (Exception e) {
            LogUtil.error("HttpClient查询失败", e);
            throw new RuntimeException("HttpClient查询失败: " + e.getMessage());
        }

    }
    private static String getResponseStr( ResponseEntity<Object> forEntity) throws Exception {
        if (forEntity.getStatusCode().value() >= 400) {
            throw new Exception("StatusCode: " + forEntity.getStatusCode());
        }
        return JSON.toJSONString(forEntity.getBody());
    }

    public String exchange(String url, String headerParam, String paramName, MediaType mediaType, MediaType AcceptMediaType){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        if (AcceptMediaType != null) {
            headers.setAccept(Collections.singletonList(AcceptMediaType));
        }
        if (StringUtils.isNotBlank(paramName)) {
            headers.set(paramName, headerParam);
        }
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<Object> forEntity = restTemplate.exchange(url, HttpMethod.GET, request, Object.class);
        try {
            return getResponseStr(forEntity);
        } catch (Exception e) {
            LogUtil.error("HttpClient查询失败", e);
            throw new RuntimeException("HttpClient查询失败: " + e.getMessage());
        }
    }

    public String exchangeString(String url, String headerParam, String paramName, MediaType mediaType, MediaType acceptMediaType){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        if (acceptMediaType != null) {
            headers.setAccept(Collections.singletonList(acceptMediaType));
        }
        if (StringUtils.isNotBlank(paramName)) {
            headers.set(paramName, headerParam);
        }
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<String> forEntity = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
        try {
            if (forEntity.getStatusCode().value() >= 400) {
                throw new Exception("StatusCode: " + forEntity.getStatusCode());
            }
            return JSON.toJSONString(forEntity.getBody());
        } catch (Exception e) {
            LogUtil.error("HttpClient查询失败", e);
            throw new RuntimeException("HttpClient查询失败: " + e.getMessage());
        }
    }

    public String postExchange(String url, String headerParam, String paramName, Object body, MediaType mediaType, MediaType AcceptMediaType){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        if (AcceptMediaType != null) {
            headers.setAccept(Collections.singletonList(AcceptMediaType));
        }
        if (StringUtils.isNotBlank(paramName)) {
            headers.set(paramName, headerParam);
        }
        HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Object> forEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Object.class);
        try {
            return getResponseStr(forEntity);
        } catch (Exception e) {
            LogUtil.error("HttpClient查询失败", e);
            throw new RuntimeException("HttpClient查询失败: " + e.getMessage());
        }
    }
}
