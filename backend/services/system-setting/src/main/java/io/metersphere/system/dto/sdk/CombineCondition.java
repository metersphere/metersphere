package io.metersphere.system.dto.sdk;

import io.metersphere.system.valid.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: jianxing
 * @CreateTime: 2024-08-28  17:31
 */
@Data
public class CombineCondition {

    @Schema(description = "参数key")
    private String key;

    @Schema(description = "期望值, BETWEEN,IN,NOT_IN 时为数组, 其他为单值")
    private Object value;

    @Schema(description = "操作符",
            allowableValues = {"IN", "NOT_IN", "BETWEEN", "GT", "LT", "EQUALS", "NOT_EQUALS", "CONTAINS", "NOT_CONTAINS", "EMPTY", "NOT_EMPTY", "CURRENT_USER"})
    @EnumValue(enumClass = CombineConditionOperator.class)
    private String operator;

    public boolean valid() {
        return StringUtils.isNotBlank(key) && StringUtils.isNotBlank(operator) && value != null;
    }

    public enum CombineConditionOperator {
        /**
         * 属于
         */
        IN,
        /**
         * 不属于
         */
        NOT_IN,
        /**
         * 区间
         */
        BETWEEN,
        /**
         * 大于
         */
        GT,
        /**
         * 小于
         */
        LT,
        /**
         * 等于
         */
        EQUALS,
        /**
         * 不等于
         */
        NOT_EQUALS,
        /**
         * 包含
         */
        CONTAINS,
        /**
         * 不包含
         */
        NOT_CONTAINS,
        /**
         * 为空
         */
        EMPTY,
        /**
         * 不为空
         */
        NOT_EMPTY,
        /**
         * 当前用户
         */
        CURRENT_USER
    }
}
