package io.metersphere.sdk.dto.api.request.processors.extract;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "extractType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegexExtract.class),
        @JsonSubTypes.Type(value = JSONPathExtract.class),
        @JsonSubTypes.Type(value = XPathExtract.class)
})
public abstract class MsExtract {
    /**
     * 参数名
     */
    private String paramName;
    /**
     * 参数类型
     */
    private String paramType;
    /**
     * 提取范围
     */
    private String extractScope;
    /**
     * 表达式
     */
    private String expression;
}
