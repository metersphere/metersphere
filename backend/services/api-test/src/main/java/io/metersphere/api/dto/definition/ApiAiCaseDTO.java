package io.metersphere.api.dto.definition;

import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.request.http.RestParam;
import io.metersphere.api.dto.request.http.body.Body;
import io.metersphere.sdk.constants.HttpMethodConstants;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ApiAiCaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接口定义和用例的请求路径，或者完整路径
     */
    private String path;
    /**
     * 请求方法
     * 取值参考：{@link HttpMethodConstants}
     */
    private String method;
    /**
     * 请求体
     */
    private Body body;
    /**
     * 请求头
     */
    private List<MsHeader> headers;
    /**
     * rest参数
     */
    private List<RestParam> rest;
    /**
     * query参数
     */
    private List<QueryParam> query;
}
