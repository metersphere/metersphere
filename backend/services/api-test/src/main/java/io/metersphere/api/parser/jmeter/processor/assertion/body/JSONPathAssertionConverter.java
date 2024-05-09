package io.metersphere.api.parser.jmeter.processor.assertion.body;

import io.metersphere.assertions.JSONPathAssertion;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.assertion.body.MsJSONPathAssertion;
import io.metersphere.project.api.assertion.body.MsJSONPathAssertionItem;
import io.metersphere.sdk.dto.api.result.ResponseAssertionResult;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-03  10:05
 */
public class JSONPathAssertionConverter extends ResponseBodyTypeAssertionConverter<MsJSONPathAssertion> {
    @Override
    public void parse(HashTree hashTree, MsJSONPathAssertion msAssertion, ParameterConfig config, boolean isIgnoreStatus, boolean globalEnable) {
        if (msAssertion == null || msAssertion.getAssertions() == null) {
            return;
        }
        msAssertion.getAssertions().stream()
                .filter(MsJSONPathAssertionItem::isValid)
                .forEach(msJSONPathAssertionItem -> {
                    if (needParse(msJSONPathAssertionItem, config)) {
                        hashTree.add(parse2JSONPathAssertion(msJSONPathAssertionItem, globalEnable));
                    }
                });
    }

    private JSONPathAssertion parse2JSONPathAssertion(MsJSONPathAssertionItem msAssertion, Boolean globalEnable) {
        // 使用定制的 JSONPath 断言组件
        JSONPathAssertion assertion = new JSONPathAssertion();
        assertion.setEnabled(msAssertion.getEnable());
        String condition = msAssertion.getCondition();
        String expression = msAssertion.getExpression();
        String expectedValue = msAssertion.getExpectedValue();

        setMsAssertionInfoProperty(assertion, ResponseAssertionResult.AssertionResultType.JSON_PATH.name(), expression, condition, expectedValue);

        assertion.setName(String.format("Response data JSONPath expect %s %s %s", expression, condition.toLowerCase().replace("_", ""), expectedValue));
        assertion.setEnabled(msAssertion.getEnable());
        if (BooleanUtils.isFalse(globalEnable)) {
            // 如果整体禁用，则禁用
            assertion.setEnabled(false);
        }
        assertion.setProperty(TestElement.TEST_CLASS, JSONPathAssertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, "io.metersphere.assertions.gui.JSONPathAssertionGui");
        assertion.setJsonValidationBool(true);
        assertion.setCondition(condition);
        assertion.setJsonPath(expression);
        assertion.setExpectedValue(expectedValue);
        return assertion;
    }

}
