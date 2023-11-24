package io.metersphere.sdk.dto.api.request.processors.extract;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

@Data
@JsonTypeName("REGEX")
public class RegexExtract extends ResultMatchingExtract {
    /**
     * 表达式匹配规则
     * 值为 ExpressionRuleType
     */
    private String expressionMatchingRule;

    public enum ExpressionRuleType {
        /**
         * 匹配表达式
         */
        EXPRESSION,
        /**
         * 匹配组
         */
        GROUP
    }
}
