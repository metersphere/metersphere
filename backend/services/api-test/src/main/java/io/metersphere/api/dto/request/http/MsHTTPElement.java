package io.metersphere.api.dto.request.http;

import io.metersphere.api.dto.request.http.auth.HTTPAuth;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@Data
@EqualsAndHashCode(callSuper = true)
public class MsHTTPElement extends AbstractMsTestElement {
    // todo 完善字段校验
    /**
     * 完整请求地址
     */
    private String url;
    /**
     * 接口定义和用例的请求路径
     */
    private String path;
    /**
     * 请求方法
     */
    private String method;
    /**
     * 请求体
     */
    private Body body;
    /**
     * 请求头
     */
    private List<Header> headers;
    /**
     * rest参数
     */
    private List<RestParam> rest;
    /**
     * query参数
     */
    private List<QueryParam> query;
    /**
     * 其他配置
     */
    private MsHTTPConfig otherConfig;
    /**
     * 认证配置
     */
    private HTTPAuth authConfig;
}