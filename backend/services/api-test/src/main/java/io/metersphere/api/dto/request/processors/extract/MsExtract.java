package io.metersphere.api.dto.request.processors.extract;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "extractType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegexExtract.class),
        @JsonSubTypes.Type(value = JSONPathExtract.class),
        @JsonSubTypes.Type(value = XPathExtract.class)
})
public abstract class MsExtract {
    /**
     * 变量名
     */
    private String variableName;
    /**
     * 参数类型
     */
    private String variableType;
    /**
     * 表达式
     */
    private String expression;

    public boolean isValid() {
        return StringUtils.isNotBlank(variableName) && StringUtils.isNotBlank(expression);
    }
}
