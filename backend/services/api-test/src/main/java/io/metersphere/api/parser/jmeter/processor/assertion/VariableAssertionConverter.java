package io.metersphere.api.parser.jmeter.processor.assertion;

import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.api.dto.request.assertion.MsVariableAssertion;
import io.metersphere.api.dto.request.processors.ScriptProcessor;
import io.metersphere.api.parser.jmeter.processor.ScriptProcessorConverter;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.sdk.constants.MsAssertionCondition;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.JSR223Assertion;
import org.apache.jorphan.collections.HashTree;

import java.util.HashMap;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-27  21:01
 */
public class VariableAssertionConverter extends AssertionConverter<MsVariableAssertion> {
    @Override
    public void parse(HashTree hashTree, MsVariableAssertion msAssertion, ParameterConfig config, boolean isIgnoreStatus) {
        if (!needParse(msAssertion, config)) {
            return;
        }
        Boolean globalEnable = msAssertion.getEnable();
        msAssertion.getVariableAssertionItems()
                .stream()
                .filter(this::isValid)
                .forEach(variableAssertionItem -> {
                    if (needParse(variableAssertionItem, config)) {
                        JSR223Assertion jsr223Assertion = parse2JSR233Assertion(variableAssertionItem);
                        jsr223Assertion.setEnabled(variableAssertionItem.getEnable());
                        if (BooleanUtils.isFalse(globalEnable)) {
                            // 如果整体禁用，则禁用
                            jsr223Assertion.setEnabled(false);
                        }
                        hashTree.add(jsr223Assertion);
                    }
                });
    }

    protected boolean needParse(MsVariableAssertion.VariableAssertionItem variableAssertionItem, ParameterConfig config) {
        // 如果组件是启用的，或者设置了解析禁用的组件，则返回 true
        return BooleanUtils.isTrue(variableAssertionItem.getEnable()) || config.getParseDisabledElement();
    }

    private static JSR223Assertion parse2JSR233Assertion(MsVariableAssertion.VariableAssertionItem variableAssertionItem) {
        ScriptProcessor scriptProcessor = new ScriptProcessor();
        scriptProcessor.setScript(parse2BeanshellJSR233Script(variableAssertionItem));

        String variableName = variableAssertionItem.getVariableName();
        String condition = variableAssertionItem.getCondition();
        String expectedValue = variableAssertionItem.getExpectedValue();
        String name = String.format("Variable '%s' expect %s %s", variableName, condition.toLowerCase().replace("_", ""), expectedValue);
        scriptProcessor.setName(name);

        scriptProcessor.setScriptLanguage(ScriptLanguageType.BEANSHELL_JSR233.getValue());
        JSR223Assertion jsr223Assertion = new JSR223Assertion();
        ScriptProcessorConverter.parse(jsr223Assertion, scriptProcessor);
        return jsr223Assertion;
    }

    public boolean isValid(MsVariableAssertion.VariableAssertionItem variableAssertionItem) {
        return StringUtils.isNotBlank(variableAssertionItem.getVariableName()) && StringUtils.isNotBlank(variableAssertionItem.getExpectedValue())
                && StringUtils.isNotBlank(variableAssertionItem.getCondition());
    }

    private static String parse2BeanshellJSR233Script(MsVariableAssertion.VariableAssertionItem variableAssertionItem) {
        HashMap<String, String> handleMap = new HashMap<>();
        String script = String.format(
                """
                variableValue = vars.get("%s");
                expectation = "%s";
                flag = true;
                """, variableAssertionItem.getVariableName(), variableAssertionItem.getExpectedValue());

        handleMap.put(MsAssertionCondition.EQUALS.name(),
                """
                result = expectation.equals(variableValue);"
                msg = "value == " + expectation;
                """);

        handleMap.put(MsAssertionCondition.NOT_EQUALS.name(),
                """
                result = !expectation.equals(variableValue);
                msg = "value != " + expectation;
                """);

        handleMap.put(MsAssertionCondition.CONTAINS.name(),
                """
                result = expectation.contains(variableValue);
                msg = "value contains " + expectation;
                """);

        handleMap.put(MsAssertionCondition.NOT_CONTAINS.name(),
                """
                result = !expectation.contains(variableValue);
                msg = "value not contains " + expectation;
                """);

        handleMap.put(MsAssertionCondition.GT.name(),
                """
                number = Integer.parseInt(expectation);
                result = number > expectation;
                msg = "value > " + expectation;
                """);

        handleMap.put(MsAssertionCondition.GT_OR_EQUALS.name(),
                """
                number = Integer.parseInt(expectation);
                result = number >= expectation;
                msg = "value >= " + expectation;
                """);

        handleMap.put(MsAssertionCondition.LT.name(),
                """
                number = Integer.parseInt(expectation);
                result = number < expectation;
                msg = "value < " + expectation;
                """);

        handleMap.put(MsAssertionCondition.LT_OR_EQUALS.name(),
                """
                number = Integer.parseInt(expectation);
                result = number <= expectation;
                msg = "value <= " + expectation;
                """);

        handleMap.put(MsAssertionCondition.START_WITH.name(),
                """
                result = expectation.startsWith(variableValue);
                msg = "value start with " + expectation;
                """);

        handleMap.put(MsAssertionCondition.END_WITH.name(),
                """
                result = expectation.endWith(variableValue);
                msg = "value end with " + expectation;
                """);

        handleMap.put(MsAssertionCondition.LENGTH_EQUALS.name(),
                """
                number = Integer.parseInt(expectation);
                result = variableValue.length() == number;
                msg = "value length == " + expectation;
                """);

        handleMap.put(MsAssertionCondition.LENGTH_GT.name(),
                """
                number = Integer.parseInt(expectation);
                result = variableValue.length() > number;
                msg = "value length > " + expectation;
                """);

        handleMap.put(MsAssertionCondition.LENGTH_GT.name(),
                """
                number = Integer.parseInt(expectation);
                result = variableValue.length() >= number;
                msg = "value length >= " + expectation;
                """);

        handleMap.put(MsAssertionCondition.LENGTH_LT.name(),
                """
                number = Integer.parseInt(expectation);
                result = variableValue.length() < number;
                msg = "value length < " + expectation;
                """);

        handleMap.put(MsAssertionCondition.LENGTH_LT_OR_EQUALS.name(),
                """
                number = Integer.parseInt(expectation);
                result = variableValue.length() <= number;
                msg = "value length <= " + expectation;
                """);

        handleMap.put(MsAssertionCondition.EMPTY.name(),
                """
                result = variableValue == void || variableValue.length() == 0;
                msg = "value is empty";
                flag = false;
                """);

        handleMap.put(MsAssertionCondition.NOT_EMPTY.name(),
                """
                result = variableValue != void && variableValue.length() > 0;
                msg = "value is not empty";
                flag = false;
                """);

        String condition = variableAssertionItem.getCondition();
        String handleScript = handleMap.get(condition);
        if (StringUtils.isNotBlank(handleScript)) {
            script += handleMap.get(MsAssertionCondition.EQUALS.name());
        }

        script += """
                if (!result) {
                    if (flag) {
                        msg = "assertion [" + msg + "]: false;";
                    }
                    AssertionResult.setFailureMessage(msg);
                    AssertionResult.setFailure(true);
                }
                """;
        return script;
    }
}
