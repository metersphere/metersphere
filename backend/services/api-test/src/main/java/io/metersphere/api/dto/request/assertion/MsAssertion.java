package io.metersphere.api.dto.request.assertion;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * 断言
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "assertionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MsResponseCodeAssertion.class),
        @JsonSubTypes.Type(value = MsResponseHeaderAssertion.class),
        @JsonSubTypes.Type(value = MsResponseBodyAssertion.class),
        @JsonSubTypes.Type(value = MsResponseTimeAssertion.class),
        @JsonSubTypes.Type(value = MsScriptAssertion.class),
        @JsonSubTypes.Type(value = MsVariableAssertion.class),
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
