package io.metersphere.api.dto.scenario.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.assertions.Assertions;
import io.metersphere.api.dto.scenario.extract.Extract;
import io.metersphere.api.dto.scenario.processor.BeanShellPostProcessor;
import io.metersphere.api.dto.scenario.processor.BeanShellPreProcessor;
import io.metersphere.api.dto.scenario.processor.JSR223PostProcessor;
import io.metersphere.api.dto.scenario.processor.JSR223PreProcessor;
import io.metersphere.api.dto.scenario.request.dubbo.ConfigCenter;
import io.metersphere.api.dto.scenario.request.dubbo.ConsumerAndService;
import io.metersphere.api.dto.scenario.request.dubbo.RegistryCenter;
import lombok.Data;

import java.util.List;

@Data
@JSONType(typeName = RequestType.DUBBO)
public class DubboRequest implements Request {
    // type 必须放最前面，以便能够转换正确的类
    private String type = RequestType.DUBBO;
    @JSONField(ordinal = 1)
    private String id;
    @JSONField(ordinal = 1)
    private String name;
    @JSONField(ordinal = 2)
    private String protocol;
    @JsonProperty(value = "interface")
    @JSONField(ordinal = 3, name = "interface")
    private String _interface;
    @JSONField(ordinal = 4)
    private String method;

    @JSONField(ordinal = 5)
    private ConfigCenter configCenter;
    @JSONField(ordinal = 6)
    private RegistryCenter registryCenter;
    @JSONField(ordinal = 7)
    private ConsumerAndService consumerAndService;

    @JSONField(ordinal = 8)
    private List<KeyValue> args;
    @JSONField(ordinal = 9)
    private List<KeyValue> attachmentArgs;
    @JSONField(ordinal = 10)
    private Assertions assertions;
    @JSONField(ordinal = 11)
    private Extract extract;
    @JSONField(ordinal = 12)
    private BeanShellPreProcessor beanShellPreProcessor;
    @JSONField(ordinal = 13)
    private BeanShellPostProcessor beanShellPostProcessor;
    @JSONField(ordinal = 14)
    private Boolean enable;
    @JSONField(ordinal = 15)
    private JSR223PreProcessor jsr223PreProcessor;
    @JSONField(ordinal = 16)
    private JSR223PostProcessor jsr223PostProcessor;
}
