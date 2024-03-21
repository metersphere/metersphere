package io.metersphere.api.dto.request.controller;

import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.sdk.constants.MsAssertionCondition;
import io.metersphere.system.valid.EnumValue;
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

    @EnumValue(enumClass = io.metersphere.sdk.constants.MsAssertionCondition.class)
    private String condition;
    /**
     * 值 ${value} 长度255
     */
    private String value;

    public boolean isValid() {
        if (StringUtils.contains(condition, MsAssertionCondition.EMPTY.name())) {
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

        MsAssertionCondition msAssertionCondition = MsAssertionCondition.valueOf(operator);
        return switch (msAssertionCondition) {
            case EMPTY ->
                    buildExpression(variable + "==" + "\"\\" + this.variable + "\"" + "|| empty(" + variable + ")");
            case NOT_EMPTY ->
                    buildExpression(variable + "!=" + "\"\\" + this.variable + "\"" + "&& !empty(" + variable + ")");
            case GT ->
                    buildExpression(StringUtils.isNumeric(value) ? variable + ">" + value : variable + ">" + "\"" + value + "\"");
            case LT ->
                    buildExpression(StringUtils.isNumeric(value) ? variable + "<" + value : variable + "<" + "\"" + value + "\"");
            case GT_OR_EQUALS ->
                    buildExpression(StringUtils.isNumeric(value) ? variable + ">=" + value : variable + ">=" + "\"" + value + "\"");
            case LT_OR_EQUALS ->
                    buildExpression(StringUtils.isNumeric(value) ? variable + "<=" + value : variable + "<=" + "\"" + value + "\"");
            case CONTAINS -> buildExpression("\"(\\n|.)*" + value + "(\\n|.)*\"=~" + variable);
            case NOT_CONTAINS -> buildExpression("\"(\\n|.)*" + value + "(\\n|.)*\"!~" + variable);
            case EQUALS ->
                    buildExpression(StringUtils.isNumeric(value) ? variable + "==" + value : variable + "==" + "\"" + value + "\"");
            case NOT_EQUALS ->
                    buildExpression(StringUtils.isNumeric(value) ? variable + "!=" + value : variable + "!=" + "\"" + value + "\"");
            default -> buildExpression("\"" + condition + value + "\"");
        };
}

    private String buildExpression(String expression) {
        return "${__jexl3(" + expression + ")}";
    }

}
