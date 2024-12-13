package io.metersphere.project.api.processor.extract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.metersphere.sdk.valid.EnumValue;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 提取处理器
 * <pre>
 * 该参数传参时，需要传入 extractType 字段，用于区分是哪种提取
 * extractType 取值为:
 *   REGEX {@link RegexExtract}
 *   JSON_PATH {@link JSONPathExtract}
 *   X_PATH {@link XPathExtract}
 * <pre>
 */
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
    @Size(max = 100)
    private String variableName;
    /**
     * 参数类型
     * 取值参考 {@link MsExtractType}
     */
    @Size(max = 100)
    @EnumValue(enumClass = MsExtractType.class)
    private String variableType;
    /**
     * 表达式
     */
    @Size(max = 200)
    private String expression;
    /**
     * 是否启用
     */
    private Boolean enable = true;
    /**
     * 描述
     */
    private String description;

    @JsonIgnore
    public boolean isValid() {
        return StringUtils.isNotBlank(variableName) && StringUtils.isNotBlank(expression);
    }

    public enum MsExtractType {
        /**
         * 临时参数
         */
        TEMPORARY,
        /**
         * 环境参数
         */
        ENVIRONMENT,
//        /**
//         * 全局参数 暂时不上
//         */
//        GLOBAL;
    }
}
