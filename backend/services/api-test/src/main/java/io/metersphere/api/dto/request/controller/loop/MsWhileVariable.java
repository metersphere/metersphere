package io.metersphere.api.dto.request.controller.loop;

import io.metersphere.api.dto.request.controller.ConditionUtils;
import io.metersphere.sdk.valid.EnumValue;
import lombok.Data;

@Data
public class MsWhileVariable {
    /**
     * 变量 255
     */
    private String variable;
    /**
     * 操作符
     */
    @EnumValue(enumClass = io.metersphere.sdk.constants.MsAssertionCondition.class)
    private String condition;
    /**
     * 值 255
     */
    private String value;

    public String getConditionValue() {
        ConditionUtils conditionUtils = new ConditionUtils();
        return conditionUtils.getConditionValue(variable, condition, value);
    }
}

