package io.metersphere.api.dto.scenario.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.scenario.AuthConfig;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = RequestType.HTTP)
public class HttpRequest extends Request {
    // type 必须放最前面，以便能够转换正确的类
    private String type = RequestType.HTTP;
    @JSONField(ordinal = 50)
    private String url;
    @JSONField(ordinal = 51)
    private String method;
    @JSONField(ordinal = 52)
    private String path;
    @JSONField(ordinal = 53)
    private List<KeyValue> parameters;
    @JSONField(ordinal = 54)
    private List<KeyValue> headers;
    @JSONField(ordinal = 55)
    private Body body;
    @JSONField(ordinal = 56)
    private String connectTimeout;
    @JSONField(ordinal = 57)
    private String responseTimeout;
    @JSONField(ordinal = 58)
    private boolean followRedirects;
    @JSONField(ordinal = 59)
    private boolean doMultipartPost;
    @JSONField(ordinal = 60)
    private List<KeyValue> rest;
    @JSONField(ordinal = 61)
    private AuthConfig authConfig;
    // 和接口定义模块用途区分
    @JSONField(ordinal = 62)
    private boolean isDefinition;

}
