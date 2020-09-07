package io.metersphere.api.dto.scenario.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.assertions.Assertions;
import io.metersphere.api.dto.scenario.extract.Extract;
import io.metersphere.api.dto.scenario.processor.BeanShellPostProcessor;
import io.metersphere.api.dto.scenario.processor.BeanShellPreProcessor;
import io.metersphere.api.dto.scenario.processor.JSR223PostProcessor;
import io.metersphere.api.dto.scenario.processor.JSR223PreProcessor;
import lombok.Data;

import java.util.List;

@Data
@JSONType(typeName = RequestType.HTTP)
public class HttpRequest implements Request {
    // type 必须放最前面，以便能够转换正确的类
    private String type = RequestType.HTTP;
    @JSONField(ordinal = 1)
    private String id;
    @JSONField(ordinal = 1)
    private String name;
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
    @JSONField(ordinal = 9)
    private Assertions assertions;
    @JSONField(ordinal = 10)
    private Extract extract;
    @JSONField(ordinal = 11)
    private BeanShellPreProcessor beanShellPreProcessor;
    @JSONField(ordinal = 12)
    private BeanShellPostProcessor beanShellPostProcessor;
    @JSONField(ordinal = 13)
    private Boolean enable;
    @JSONField(ordinal = 14)
    private Long connectTimeout;
    @JSONField(ordinal = 15)
    private Long responseTimeout;
    @JSONField(ordinal = 16)
    private Boolean followRedirects;
    @JSONField(ordinal = 17)
    private JSR223PreProcessor jsr223PreProcessor;
    @JSONField(ordinal = 18)
    private JSR223PostProcessor jsr223PostProcessor;
}
