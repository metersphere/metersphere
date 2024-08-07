package io.metersphere.api.parser.jmeter.processor.assertion.body;

import io.metersphere.api.parser.jmeter.processor.assertion.AssertionConverter;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.assertion.body.MsRegexAssertion;
import io.metersphere.project.api.assertion.body.MsRegexAssertionItem;
import io.metersphere.sdk.dto.api.result.ResponseAssertionResult;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-03  10:05
 */
public class RegexAssertionConverter extends ResponseBodyTypeAssertionConverter<MsRegexAssertion> {
    @Override
    public void parse(HashTree hashTree, MsRegexAssertion msAssertion, ParameterConfig config, boolean isIgnoreStatus, boolean globalEnable) {
        if (msAssertion == null || msAssertion.getAssertions() == null) {
            return;
        }
        msAssertion.getAssertions().stream()
                .filter(MsRegexAssertionItem::isValid)
                .forEach(regexAssertionItem -> {
                    if (needParse(regexAssertionItem, config)) {
                        ResponseAssertion responseAssertion = parse2RegexResponseAssertion(regexAssertionItem, globalEnable);
                        responseAssertion.setAssumeSuccess(isIgnoreStatus);
                        hashTree.add(responseAssertion);
                    }
                });
    }

    private ResponseAssertion parse2RegexResponseAssertion(MsRegexAssertionItem msAssertion, Boolean globalEnable) {
        ResponseAssertion assertion = AssertionConverter.createResponseAssertion();

        setMsAssertionInfoProperty(assertion, ResponseAssertionResult.AssertionResultType.REGEX.name(), msAssertion.getExpression());

        assertion.setEnabled(msAssertion.getEnable());
        assertion.setName("Response data expect regex " + msAssertion.getExpression());
        assertion.addTestString(msAssertion.getExpression());
        assertion.setTestFieldResponseData();
        assertion.setEnabled(msAssertion.getEnable());
        if (BooleanUtils.isFalse(globalEnable)) {
            // 如果整体禁用，则禁用
            assertion.setEnabled(false);
        }
        assertion.setToContainsType();

        return assertion;
    }
}
