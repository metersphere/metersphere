package io.metersphere.api.dto.scenario.request;

import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = HttpRequest.class, name = RequestType.HTTP),
        @JsonSubTypes.Type(value = DubboRequest.class, name = RequestType.DUBBO)
})
@JSONType(seeAlso = {HttpRequest.class, DubboRequest.class}, typeKey = "type")
public interface Request {
}
