package io.metersphere.project.api.processor.extract;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.sdk.valid.EnumValue;
import lombok.Data;

@Data
@JsonTypeName("REGEX")
public class RegexExtract extends ResultMatchingExtract {
    /**
     * 表达式匹配规则
     * 取值参考 {@link ExpressionRuleType} 中的 value
     * 默认为表达式匹配
     */
    @EnumValue(enumClass = ExpressionRuleType.class)
    private String expressionMatchingRule = ExpressionRuleType.EXPRESSION.name();
    /**
     * 提取范围
     * 取值参考 {@link ExtractScope}
     */
    @EnumValue(enumClass = ExtractScope.class)
    private String extractScope;

    /**
     * 表达式匹配规则
     */
    public enum ExpressionRuleType {
        /**
         * 匹配表达式
         */
        EXPRESSION,
        /**
         * 匹配组
         */
        GROUP;
    }

    /**
     * 提取对象
     */
    public enum ExtractScope {
        BODY,
        REQUEST_HEADERS,
        UNESCAPED_BODY,
        BODY_AS_DOCUMENT,
        RESPONSE_HEADERS,
        URL,
        RESPONSE_CODE,
        RESPONSE_MESSAGE;
    }
}
