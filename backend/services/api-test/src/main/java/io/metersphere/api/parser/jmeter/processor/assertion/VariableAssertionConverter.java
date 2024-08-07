package io.metersphere.api.parser.jmeter.processor.assertion;

import io.metersphere.api.parser.jmeter.processor.ScriptProcessorConverter;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.assertion.MsVariableAssertion;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.sdk.constants.MsAssertionCondition;
import io.metersphere.sdk.dto.api.result.ResponseAssertionResult;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
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
                        JSR223Assertion jsr223Assertion = parse2JSR233Assertion(variableAssertionItem, config);
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

    private static JSR223Assertion parse2JSR233Assertion(MsVariableAssertion.VariableAssertionItem variableAssertionItem, ParameterConfig config) {
        ScriptProcessor scriptProcessor = new ScriptProcessor();
        scriptProcessor.setScript(parse2BeanshellJSR233Script(variableAssertionItem));

        String variableName = variableAssertionItem.getVariableName();
        String condition = variableAssertionItem.getCondition();
        String expectedValue = variableAssertionItem.getExpectedValue();
        String name = String.format("Variable '%s' expect %s %s", variableName, condition.toLowerCase().replace("_", ""), expectedValue);
        scriptProcessor.setName(name);

        scriptProcessor.setScriptLanguage(ScriptLanguageType.GROOVY.name());
        JSR223Assertion jsr223Assertion = new JSR223Assertion();
        ScriptProcessorConverter.parse(jsr223Assertion, scriptProcessor, config);

        setMsAssertionInfoProperty(jsr223Assertion, ResponseAssertionResult.AssertionResultType.VARIABLE.name(), variableName, condition, expectedValue);
        return jsr223Assertion;
    }

    public boolean isValid(MsVariableAssertion.VariableAssertionItem variableAssertionItem) {
        return StringUtils.isNotBlank(variableAssertionItem.getVariableName())
                && StringUtils.isNotBlank(variableAssertionItem.getCondition())
                && BooleanUtils.isTrue(variableAssertionItem.getEnable())
                && !StringUtils.equals(variableAssertionItem.getCondition(), MsAssertionCondition.UNCHECK.name());
    }

    private static String parse2BeanshellJSR233Script(MsVariableAssertion.VariableAssertionItem variableAssertionItem) {
        HashMap<String, String> handleMap = new HashMap<>();
        String script = String.format(
                """
                variableValue = vars.get("%s");
                expectation = "%s";
                flag = true;
                """, StringEscapeUtils.escapeJava(variableAssertionItem.getVariableName()), StringEscapeUtils.escapeJava(variableAssertionItem.getExpectedValue())); // 转义一下再填充

        handleMap.put(MsAssertionCondition.REGEX.name(),
                """
                import java.util.regex.Pattern;
                if (variableValue != null) {
                   result = Pattern.matches(expectation, variableValue);
                } else {
                   result = false;
                }
                msg = variableValue + " not matching " + expectation;
                """);

        handleMap.put(MsAssertionCondition.EQUALS.name(),
                """
                result = variableValue.equals(expectation);
                msg = variableValue + " == " + expectation;
                """);

        handleMap.put(MsAssertionCondition.NOT_EQUALS.name(),
                """
                result = !variableValue.equals(expectation);
                msg = variableValue + " != " + expectation;
                """);

        handleMap.put(MsAssertionCondition.CONTAINS.name(),
                """
                result = variableValue.contains(expectation);
                msg = variableValue + " contains " + expectation;
                """);

        handleMap.put(MsAssertionCondition.NOT_CONTAINS.name(),
                """
                result = !variableValue.contains(expectation);
                msg = variableValue + " not contains " + expectation;
                """);

        handleMap.put(MsAssertionCondition.GT.name(),
                """
                result = Double.parseDouble(variableValue) > Double.parseDouble(expectation);
                msg = variableValue + " > " + expectation;
                """);

        handleMap.put(MsAssertionCondition.GT_OR_EQUALS.name(),
                """
                result = Double.parseDouble(variableValue) >= Double.parseDouble(expectation);
                msg = variableValue + " >= " + expectation;
                """);

        handleMap.put(MsAssertionCondition.LT.name(),
                """
                result = Double.parseDouble(variableValue) < Double.parseDouble(expectation);
                msg = variableValue + " < " + expectation;
                """);

        handleMap.put(MsAssertionCondition.LT_OR_EQUALS.name(),
                """
                result = Double.parseDouble(variableValue) <= Double.parseDouble(expectation);
                msg = variableValue + " <= " + expectation;
                """);

        handleMap.put(MsAssertionCondition.START_WITH.name(),
                """
                result = variableValue.startsWith(expectation);
                msg = variableValue + " start with " + expectation;
                """);

        handleMap.put(MsAssertionCondition.END_WITH.name(),
                """
                result = variableValue.endsWith(expectation);
                msg = variableValue + " end with " + expectation;
                """);

        handleMap.put(MsAssertionCondition.LENGTH_EQUALS.name(),
                """
                number = Double.parseDouble(expectation);
                result = variableValue.length() == number;
                msg = variableValue + " length == " + expectation;
                """);

        handleMap.put(MsAssertionCondition.LENGTH_GT.name(),
                """
                number = Double.parseDouble(expectation);
                result = variableValue.length() > number;
                msg = variableValue + " length > " + expectation;
                """);

        handleMap.put(MsAssertionCondition.LENGTH_GT_OR_EQUALS.name(),
                """
                number = Double.parseDouble(expectation);
                result = variableValue.length() >= number;
                msg = variableValue + " length >= " + expectation;
                """);

        handleMap.put(MsAssertionCondition.LENGTH_LT.name(),
                """
                number = Double.parseDouble(expectation);
                result = variableValue.length() < number;
                msg = variableValue + " length < " + expectation;
                """);

        handleMap.put(MsAssertionCondition.LENGTH_LT_OR_EQUALS.name(),
                """
                number = Double.parseDouble(expectation);
                result = variableValue.length() <= number;
                msg = variableValue + " length <= " + expectation;
                """);

        handleMap.put(MsAssertionCondition.EMPTY.name(),
                """
                result = variableValue == void || variableValue.length() == 0;
                msg = variableValue + " is empty";
                flag = false;
                """);

        handleMap.put(MsAssertionCondition.NOT_EMPTY.name(),
                """
                result = variableValue != void && variableValue.length() > 0;
                msg = variableValue + " is not empty";
                flag = false;
                """);

        String condition = variableAssertionItem.getCondition();
        String handleScript = handleMap.get(condition);
        if (StringUtils.isBlank(handleScript)) {
            script += handleMap.get(MsAssertionCondition.EQUALS.name());
        } else {
            script += handleScript;
        }

        script += """
                if (!result){
                    if (flag) {
                        msg = "assertion [" + msg + "]: false;";
                    }
                    AssertionResult.setFailure(true);
                }
                AssertionResult.setFailureMessage(msg + "&&&" + variableValue);
                """;
        return script;
    }
}
