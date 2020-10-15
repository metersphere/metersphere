package io.metersphere.api.dto.scenario.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
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
    @JSONField(ordinal = 2)
    private String url;
    @JSONField(ordinal = 3)
    private String method;
    @JSONField(ordinal = 4)
    private String path;
    @JSONField(ordinal = 5)
    private Boolean useEnvironment;
    @JSONField(ordinal = 6)
    private List<KeyValue> parameters;
    @JSONField(ordinal = 7)
    private List<KeyValue> headers;
    @JSONField(ordinal = 8)
    private Body body;
    @JSONField(ordinal = 14)
    private Long connectTimeout;
    @JSONField(ordinal = 15)
    private Long responseTimeout;
    @JSONField(ordinal = 16)
    private Boolean followRedirects;
    @JSONField(ordinal = 17)
    private Boolean doMultipartPost;
}
