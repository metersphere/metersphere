package io.metersphere.api.dto.request.controller;

import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsIfController extends AbstractMsTestElement {
    /**
     * 变量名称 ${variable} 长度255
     */
    private String variable;
    /**
     * 操作符 == ,!=, < ,<=, >, >=, contains (=~),not contains (!~), is empty, is not empty
     */
    private String condition;
    /**
     * 值 ${value} 长度255
     */
    private String value;

    public boolean isValid() {
        if (StringUtils.contains(condition, "is empty")) {
            return StringUtils.isNotBlank(variable);
        }
        return StringUtils.isNotBlank(variable) && StringUtils.isNotBlank(condition) && StringUtils.isNotBlank(value);
    }

    public String getContentValue() {
        try {
            String content = this.variable;
            String pattern = "\\$\\{([^}]*)\\}";
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(content);
            StringBuilder stringBuilder = new StringBuilder();
            while (matcher.find()) {
                stringBuilder.append(matcher.group(1)).append(",");
            }
            if (!stringBuilder.isEmpty()) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            if (StringUtils.isEmpty(stringBuilder.toString())) {
                return this.variable;
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public String getConditionValue() {
        String key = getContentValue();

        String variable = (StringUtils.isEmpty(key) || key.equals(this.variable)) || key.startsWith("__")
                ? StringUtils.join("\"", this.variable, "\"")
                : StringUtils.join("vars.get('", key, "')");

        String operator = this.condition;
        String value;

        switch (operator) {
            case "is empty":
                variable = variable + "==" + "\"\\" + this.variable + "\"" + "|| empty(" + variable + ")";
                operator = "";
                value = "";
                break;
            case "is not empty":
                variable = variable + "!=" + "\"\\" + this.variable + "\"" + "&& !empty(" + variable + ")";
                operator = "";
                value = "";
                break;
            case "<":
            case ">":
            case "<=":
            case ">=":
                value = this.value;
                break;
            case "=~":
            case "!~":
                value = "\"(\\n|.)*" + this.value + "(\\n|.)*\"";
                break;
            default:
                value = "\"" + this.value + "\"";
                break;
        }

        return "${__jexl3(" + variable + operator + value + ")}";
    }

}
