package io.metersphere.api.dto.request.controller.loop;

import io.metersphere.sdk.constants.MsAssertionCondition;
import io.metersphere.system.valid.EnumValue;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

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
        String variable = "\"" + this.getVariable() + "\"";
        String value = this.getValue();
        MsAssertionCondition msAssertionCondition = MsAssertionCondition.valueOf(this.getCondition());
        return switch (msAssertionCondition) {
            case EMPTY -> String.format("(%s==\"\\\"%s\\\"\"|| empty(%s))", variable, this.getVariable(), variable);
            case NOT_EMPTY ->
                    String.format("(%s!=\"\\\"%s\\\"\"&& !empty(%s))", variable, this.getVariable(), variable);
            case GT -> StringUtils.isNumeric(value) ? variable + ">" + value : variable + ">\"" + value + "\"";
            case LT -> StringUtils.isNumeric(value) ? variable + "<" + value : variable + "<\"" + value + "\"";
            case LT_OR_EQUALS ->
                    StringUtils.isNumeric(value) ? variable + "<=" + value : variable + "<=\"" + value + "\"";
            case GT_OR_EQUALS ->
                    StringUtils.isNumeric(value) ? variable + ">=" + value : variable + ">=\"" + value + "\"";
            case CONTAINS -> String.format("\"(\\n|.)*%s(\\n|.)*\"=~", variable);
            case NOT_CONTAINS -> String.format("\"(\\n|.)*%s(\\n|.)*\"!~", variable);
            case EQUALS ->
                    StringUtils.isNumeric(value) ? variable + "==" + value : variable + "==" + "\"" + value + "\"";
            case NOT_EQUALS ->
                    StringUtils.isNumeric(value) ? variable + "!=" + value : variable + "!=" + "\"" + value + "\"";
            default -> "\"" + value + "\"";
        };
    }
}

