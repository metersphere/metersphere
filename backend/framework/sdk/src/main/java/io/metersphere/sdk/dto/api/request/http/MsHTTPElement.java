package io.metersphere.sdk.dto.api.request.http;

import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.dto.api.request.http.auth.HTTPAuth;
import io.metersphere.sdk.dto.api.request.http.body.Body;
import io.metersphere.sdk.dto.api.request.processors.MsProcessor;
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
    /**
     * 前置处理器
     */
    private List<MsProcessor> preProcessors;
    /**
     * 后置处理器
     */
    private List<MsProcessor> postProcessors;
    // todo 断言和提取 待设计
}