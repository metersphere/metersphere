package io.metersphere.api.dto.scenario.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.metersphere.api.dto.scenario.assertions.Assertions;
import io.metersphere.api.dto.scenario.controller.IfController;
import io.metersphere.api.dto.scenario.extract.Extract;
import io.metersphere.api.dto.scenario.processor.JSR223PostProcessor;
import io.metersphere.api.dto.scenario.processor.JSR223PreProcessor;
import io.metersphere.api.dto.scenario.timer.ConstantTimer;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HttpRequest.class, name = RequestType.HTTP),
        @JsonSubTypes.Type(value = DubboRequest.class, name = RequestType.DUBBO),
        @JsonSubTypes.Type(value = SqlRequest.class, name = RequestType.SQL),
        @JsonSubTypes.Type(value = TCPRequest.class, name = RequestType.TCP)
})
@JSONType(seeAlso = {HttpRequest.class, DubboRequest.class, SqlRequest.class, TCPRequest.class}, typeKey = "type")
@Data
public abstract class Request {
    @JSONField(ordinal = 1)
    private String id;
    @JSONField(ordinal = 2)
    private String name;
    @JSONField(ordinal = 3)
    private boolean enable = true;
    @JSONField(ordinal = 4)
    private boolean useEnvironment;
    @JSONField(ordinal = 5)
    private Assertions assertions;
    @JSONField(ordinal = 6)
    private Extract extract;
    @JSONField(ordinal = 7)
    private JSR223PreProcessor jsr223PreProcessor;
    @JSONField(ordinal = 8)
    private JSR223PostProcessor jsr223PostProcessor;
    @JSONField(ordinal = 9)
    private IfController controller;
    @JSONField(ordinal = 10)
    private ConstantTimer timer;
}
