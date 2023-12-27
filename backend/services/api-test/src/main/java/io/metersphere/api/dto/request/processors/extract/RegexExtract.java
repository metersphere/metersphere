package io.metersphere.api.dto.request.processors.extract;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

@Data
@JsonTypeName("REGEX")
public class RegexExtract extends ResultMatchingExtract {
    /**
     * 表达式匹配规则
     * @see ExpressionRuleType
     */
    private String expressionMatchingRule;
    /**
     * 提取范围
     * @see ExtractScope
     */
    private String extractScope;

    public enum ExpressionRuleType {
        /**
         * 匹配表达式
         */
        EXPRESSION("$1$"),
        /**
         * 匹配组
         */
        GROUP("$0$");

        private String value;

        ExpressionRuleType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum ExtractScope {
        BODY("false"),
        REQUEST_HEADERS("request_headers"),
        UNESCAPED_BODY("unescaped"),
        BODY_AS_DOCUMENT("as_document"),
        RESPONSE_HEADERS("true"),
        URL("URL"),
        RESPONSE_CODE("code"),
        RESPONSE_MESSAGE("message");

        private String value;

        ExtractScope(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
