package io.metersphere.api.dto.request.controller;

import io.metersphere.sdk.constants.MsAssertionCondition;

public class ConditionUtils {


    public String getConditionValue(String variable, String condition, String value) {
        String wrappedVariable = "\"" + variable + "\"";
        String wrappedValue = "\"" + value + "\"";
        MsAssertionCondition msAssertionCondition = MsAssertionCondition.valueOf(condition);
        return switch (msAssertionCondition) {
            case EMPTY -> String.format("(%s==\"\" || %s==%s || empty(%s))", wrappedVariable, wrappedVariable,
                    wrappedVariable.replace("$", "\\$"), wrappedVariable);
            case NOT_EMPTY -> String.format("(%s!=\"\" && %s!=%s && !empty(%s))", wrappedVariable, wrappedVariable,
                    wrappedVariable.replace("$", "\\$"), wrappedVariable);
            case GT -> variable + ">" + value;
            case LT -> variable + "<" + value;
            case LT_OR_EQUALS -> variable + "<=" + value;
            case GT_OR_EQUALS -> variable + ">=" + value;
            case CONTAINS -> String.format("%s.contains(%s)", wrappedVariable,  wrappedValue);
            case NOT_CONTAINS -> String.format("!%s.contains(%s)", wrappedVariable,  wrappedValue);
            case EQUALS -> isNumeric(value) ? wrappedVariable + "==" + value : wrappedVariable + "==" + wrappedValue;
            case NOT_EQUALS -> isNumeric(value) ? wrappedVariable + "!=" + value : wrappedVariable + "!=" + wrappedValue;
            default -> wrappedValue;
        };
    }

    public boolean isNumeric(String strNum) {
        try {
            Double.parseDouble(strNum);
            return true;
        } catch (NumberFormatException e2) {
            return false;
        }
    }
}
