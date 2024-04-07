package io.metersphere.project.api.assertion;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 断言
 * <pre>
 * 该参数传参时，需要传入 assertionType 字段，用于区分是哪种断言
 * assertionType 取值为:
 *   RESPONSE_CODE {@link MsResponseCodeAssertion}
 *   RESPONSE_HEADER {@link MsResponseHeaderAssertion}
 *   RESPONSE_BODY {@link MsResponseBodyAssertion}
 *   RESPONSE_TIME {@link MsResponseTimeAssertion}
 *   SCRIPT {@link MsScriptAssertion}
 *   VARIABLE {@link MsVariableAssertion}
 * </pre>
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
     * 默认启用
     */
    private Boolean enable = true;
    /**
     * 断言名称
     */
    @Size(max = 100)
    private String name;
    /**
     * id
     */
    private String id;
    /**
     * 项目ID
     * 执行时设置
     */
    private String projectId;
}
