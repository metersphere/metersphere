package io.metersphere.api.curl.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author wx
 */
@Data
@Builder
public class CurlEntity {
    /**
     * URL路径
     */
    private String url;

    /**
     * 请求方法类型
     */
    private Method method;

    /**
     * URL参数
     */
    private Map<String, String> queryParams;

    /**
     * header参数
     */
    private Map<String, String> headers;

    /**
     * 请求体
     */
    private Object body;

    private String bodyType;

    public enum Method {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH,
        OPTIONS,
        HEAD,
        CONNECT
    }
}
