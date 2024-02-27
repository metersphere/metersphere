package io.metersphere.api.dto.request.controller.loop;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class MsWhileVariable {
    /**
     * 变量 255
     */
    private String variable;
    /**
     * 操作符 == ,!=, < ,<=, >, >=, contains (=~),not contains (!~), is empty, is not empty
     */
    private String condition;
    /**
     * 值 255
     */
    private String value;

    public String getConditionValue() {
        String variable = "\"" + this.getVariable() + "\"";
        String operator = this.getCondition();
        String value = null;
        switch (operator) {
            case "is empty":
                variable = "(" + variable + "==" + "\"\\" + this.getVariable() + "\"" + "|| empty(" + variable + "))";
                operator = "";
                break;
            case "is not empty":
                variable = "(" + variable + "!=" + "\"\\" + this.getVariable() + "\"" + "&& !empty(" + variable + "))";
                operator = "";
                break;
            case "<":
            case ">":
            case "<=":
            case ">=":
                if (StringUtils.isNumeric(this.getValue())) {
                    value = this.getValue();
                } else {
                    value = "\"" + this.getValue() + "\"";
                }
                break;
            case "=~":
            case "!~":
                value = "\"(\\n|.)*" + this.getVariable() + "(\\n|.)*\"";
                break;
            default:
                value = "\"" + this.getValue() + "\"";
                break;
        }

        return variable + operator + value;
    }
}

