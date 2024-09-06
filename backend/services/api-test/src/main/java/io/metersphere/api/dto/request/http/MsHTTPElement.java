package io.metersphere.api.dto.request.http;

import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.plugin.api.spi.AbstractMsProtocolTestElement;
import io.metersphere.project.dto.environment.auth.HTTPAuthConfig;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.valid.EnumValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Http接口详情
 * <pre>
 * 其中包括：接口调试、接口定义、接口用例、场景的自定义请求 的详情
 * 接口协议插件的接口详情也类似
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MsHTTPElement extends AbstractMsProtocolTestElement {
    /**
     * 接口定义和用例的请求路径，或者完整路径
     */
    private String path;
    /**
     * 请求方法
     * 取值参考：{@link HttpMethodConstants}
     */
    @NotBlank
    @EnumValue(enumClass = HttpMethodConstants.class)
    private String method;
    /**
     * 请求体
     */
    @Valid
    private Body body;
    /**
     * 请求头
     */
    @Valid
    private List<MsHeader> headers;
    /**
     * rest参数
     */
    @Valid
    private List<RestParam> rest;
    /**
     * query参数
     */
    @Valid
    private List<QueryParam> query;
    /**
     * 其他配置
     */
    @Valid
    private MsHTTPConfig otherConfig = new MsHTTPConfig();
    /**
     * 认证配置
     */
    @Valid
    private HTTPAuthConfig authConfig = new HTTPAuthConfig();
    /**
     * 模块ID
     * 运行时参数，接口无需设置
     */
    private String moduleId;
    /**
     * mock执行需要的接口编号
     */
    private Long num;
    /**
     * mock调试过来的mock编号
     */
    private String mockNum;
}