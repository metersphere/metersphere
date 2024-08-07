package io.metersphere.api.parser.jmeter.processor.assertion;

import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.assertion.MsAssertion;
import io.metersphere.sdk.constants.MsAssertionCondition;
import io.metersphere.sdk.util.EnumValidator;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.ASSERTION_GUI;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-27  10:32
 */
public abstract class AssertionConverter<T extends MsAssertion> {
    /**
     * 解析对应的提取器
     *
     * @param hashTree
     * @param extract
     * @param config
     */
    public abstract void parse(HashTree hashTree, T extract, ParameterConfig config, boolean isIgnoreStatus);

    protected boolean needParse(MsAssertion msAssertion, ParameterConfig config) {
        // 如果组件是启用的，或者设置了解析禁用的组件，则返回 true
        if (BooleanUtils.isTrue(msAssertion.getEnable()) || config.getParseDisabledElement()) {
            return true;
        }
        return false;
    }

    public static ResponseAssertion createResponseAssertion() {
        ResponseAssertion assertion = new ResponseAssertion();
        assertion.setProperty(TestElement.TEST_CLASS, ResponseAssertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(ASSERTION_GUI));
        return assertion;
    }

    protected String generateRegexExpression(String condition, String text) {
        Map<MsAssertionCondition, Function<String, String>> regexgenerateMap = new HashMap<>();
        // 只支持以下条件
        regexgenerateMap.put(MsAssertionCondition.CONTAINS, value -> StringUtils.join(".*", value, ".*"));
        regexgenerateMap.put(MsAssertionCondition.NOT_CONTAINS, value -> StringUtils.join("(?s)^((?!", value, ").)*$"));
        regexgenerateMap.put(MsAssertionCondition.END_WITH, value -> StringUtils.join(value, "$"));
        regexgenerateMap.put(MsAssertionCondition.START_WITH, value -> StringUtils.join("^", value));
        regexgenerateMap.put(MsAssertionCondition.EQUALS, value -> StringUtils.join("^", value, "$"));
        regexgenerateMap.put(MsAssertionCondition.NOT_EQUALS, value -> StringUtils.join("^(?!", value, "$).*$"));
        regexgenerateMap.put(MsAssertionCondition.EMPTY, value -> StringUtils.join("^$", value));
        regexgenerateMap.put(MsAssertionCondition.NOT_EMPTY, value -> StringUtils.join("^(?!^$).*$", value));
        regexgenerateMap.put(MsAssertionCondition.REGEX, value -> value);
        MsAssertionCondition msAssertionCondition = EnumValidator.validateEnum(MsAssertionCondition.class, condition);
        if (msAssertionCondition != null && regexgenerateMap.get(msAssertionCondition) != null) {
            return regexgenerateMap.get(msAssertionCondition).apply(text);
        }
        return text;
    }

    public static void setMsAssertionInfoProperty(AbstractTestElement assertion, String assertionType, String name, String condition, String expectedValue) {
        // 保存断言信息
        assertion.setProperty("name", name);
        assertion.setProperty("assertionType", assertionType);
        assertion.setProperty("condition", condition);
        assertion.setProperty("expectedValue", expectedValue);
    }

    public static void setMsAssertionInfoProperty(AbstractTestElement assertion, String assertionType, String name) {
        AssertionConverter.setMsAssertionInfoProperty(assertion, assertionType, name, null, null);
    }
}
