package io.metersphere.api.dto.request.assertion.body;

import io.metersphere.system.valid.EnumValue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * JSONPath断言
 *
 * @Author: jianxing
 * @CreateTime: 2023-11-23  14:04
 */
@Data
public class MsJSONPathAssertionItem extends MsBodyAssertionItem {
    /**
     * 表达式
     */
    private String expression;
    /**
     * 匹配条件
     * 取值参考 {@link io.metersphere.sdk.constants.MsAssertionCondition}
     */
    @NotBlank
    @EnumValue(enumClass = io.metersphere.sdk.constants.MsAssertionCondition.class)
    private String condition;
    /**
     * 匹配值
     */
    private String expectedValue;

    public boolean isValid() {
        return StringUtils.isNotBlank(expression) && StringUtils.isNotBlank(condition) && StringUtils.isNotBlank(expectedValue);
    }
}

