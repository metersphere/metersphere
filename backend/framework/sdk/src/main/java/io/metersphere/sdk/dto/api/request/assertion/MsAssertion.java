package io.metersphere.sdk.dto.api.request.assertion;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * 断言
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "assertionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ResponseCodeAssertion.class),
        @JsonSubTypes.Type(value = ResponseHeaderAssertion.class),
        @JsonSubTypes.Type(value = ResponseBodyAssertion.class),
        @JsonSubTypes.Type(value = ResponseTimeAssertion.class),
        @JsonSubTypes.Type(value = ScriptAssertion.class),
})
public abstract class MsAssertion {
    /**
     * 是否启用
     */
    private Boolean enable = true;
    /**
     * 断言名称
     */
    private String name;
}
