package io.metersphere.api.dto.request.controller;

import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.valid.EnumValue;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsIfController extends AbstractMsTestElement {
    /**
     * 变量名称 ${variable} 长度255
     */
    private String variable;

    @EnumValue(enumClass = io.metersphere.sdk.constants.MsAssertionCondition.class)
    private String condition;
    /**
     * 值 ${value} 长度255
     */
    private String value;

    public String getConditionValue() {
        ConditionUtils conditionUtils = new ConditionUtils();
        return buildExpression(conditionUtils.getConditionValue(this.variable, this.condition, value));
    }

    private String buildExpression(String expression) {
        return "${__jexl3(" + expression + ")}";
    }

}
