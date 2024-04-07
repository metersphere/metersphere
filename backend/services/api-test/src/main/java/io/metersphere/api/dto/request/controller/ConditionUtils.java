package io.metersphere.api.dto.request.controller;

import io.metersphere.sdk.constants.MsAssertionCondition;
import org.apache.commons.lang3.StringUtils;

public class ConditionUtils {


    public String getConditionValue(String variable, String condition, String value) {
        variable = "\"" + variable + "\"";
        MsAssertionCondition msAssertionCondition = MsAssertionCondition.valueOf(condition);
        return switch (msAssertionCondition) {
            case EMPTY -> String.format("(%s==\"\"|| empty(%s))", variable, variable);
            case NOT_EMPTY ->
                    String.format("(%s!=\"\"&& !empty(%s))", variable, variable);
            case GT -> StringUtils.isNumeric(value) ? variable + ">" + value : variable + ">\"" + value + "\"";
            case LT -> StringUtils.isNumeric(value) ? variable + "<" + value : variable + "<\"" + value + "\"";
            case LT_OR_EQUALS ->
                    StringUtils.isNumeric(value) ? variable + "<=" + value : variable + "<=\"" + value + "\"";
            case GT_OR_EQUALS ->
                    StringUtils.isNumeric(value) ? variable + ">=" + value : variable + ">=\"" + value + "\"";
            case CONTAINS -> String.format("%s.contains(%s)", variable,  "\"" + value + "\"");
            case NOT_CONTAINS -> String.format("!%s.contains(%s)", variable,  "\"" + value + "\"");
            case EQUALS ->
                    StringUtils.isNumeric(value) ? variable + "==" + value : variable + "==" + "\"" + value + "\"";
            case NOT_EQUALS ->
                    StringUtils.isNumeric(value) ? variable + "!=" + value : variable + "!=" + "\"" + value + "\"";
            default -> "\"" + value + "\"";
        };
    }
}
