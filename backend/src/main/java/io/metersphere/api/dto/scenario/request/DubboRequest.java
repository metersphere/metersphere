package io.metersphere.api.dto.scenario.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.request.dubbo.ConfigCenter;
import io.metersphere.api.dto.scenario.request.dubbo.ConsumerAndService;
import io.metersphere.api.dto.scenario.request.dubbo.RegistryCenter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = RequestType.DUBBO)
public class DubboRequest extends Request {
    // type 必须放最前面，以便能够转换正确的类
    private String type = RequestType.DUBBO;
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
}
