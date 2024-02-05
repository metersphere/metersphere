package io.metersphere.project.dto.environment.http;

import io.metersphere.system.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

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
        CONTAINS,
        /**
         * 等于
         */
        EQUALS
    }
}
