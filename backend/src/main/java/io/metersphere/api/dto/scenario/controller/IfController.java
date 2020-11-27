package io.metersphere.api.dto.scenario.controller;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class IfController {
    private String type;
    private String id;
    private boolean enable = true;
    private String variable;
    private String operator;
    private String value;

    public boolean isValid() {
        if (StringUtils.contains(operator, "empty")) {
            return StringUtils.isNotBlank(variable);
        }
        return StringUtils.isNotBlank(variable) && StringUtils.isNotBlank(operator) && StringUtils.isNotBlank(value);
    }

    public String getLabel() {
        if (isValid()) {
            String label = variable + " " + operator;
            if (StringUtils.isNotBlank(value)) {
                label += " " + this.value;
            }
            return label;
        }
        return "";
    }

    public String getCondition() {
        String variable = "\"" + this.variable + "\"";
        String operator = this.operator;
        String value = "\"" + this.value + "\"";

        if (StringUtils.contains(operator, "~")) {
            value = "\".*" + this.value + ".*\"";
        }

        if (StringUtils.equals(operator, "is empty")) {
            variable = "empty(" + variable + ")";
            operator = "";
            value = "";
        }

        if (StringUtils.equals(operator, "is not empty")) {
            variable = "!empty(" + variable + ")";
            operator = "";
            value = "";
        }

        return "${__jexl3(" + variable + operator + value + ")}";
    }
}
