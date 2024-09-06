package io.metersphere.project.dto.environment.http;

import io.metersphere.sdk.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * @Author: jianxing
 * @CreateTime: 2024-02-05  18:53
 */
@Data
public class HttpConfigPathMatchRule  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 匹配规则 CONTAINS/EQUALS
     * {@link MatchRuleCondition}
     */
    @Schema(description = "匹配条件 CONTAINS/EQUALS")
    @EnumValue(enumClass = MatchRuleCondition.class)
    private String condition;
    @Schema(description = "路径")
    private String path;

    public enum MatchRuleCondition {
        /**
         * 包含
         */
        CONTAINS((envPath, path) -> StringUtils.contains(path, envPath)),
        /**
         * 等于
         */
        EQUALS((envPath, path) -> StringUtils.equals(path, envPath));

        MatchRuleCondition(BiFunction<String, String, Boolean> matchFunc) {
            this.matchFunc = matchFunc;
        }

        private BiFunction<String, String, Boolean> matchFunc;

        public boolean match(String value, String expect) {
            return matchFunc.apply(value, expect);
        }
    }
}
