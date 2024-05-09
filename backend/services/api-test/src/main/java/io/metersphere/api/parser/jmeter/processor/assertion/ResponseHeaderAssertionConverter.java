package io.metersphere.api.parser.jmeter.processor.assertion;

import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.assertion.MsResponseHeaderAssertion;
import io.metersphere.sdk.constants.MsAssertionCondition;
import io.metersphere.sdk.dto.api.result.ResponseAssertionResult;
import io.metersphere.sdk.util.EnumValidator;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-27  21:01
 */
public class ResponseHeaderAssertionConverter extends AssertionConverter<MsResponseHeaderAssertion> {
    @Override
    public void parse(HashTree hashTree, MsResponseHeaderAssertion msAssertion, ParameterConfig config, boolean isIgnoreStatus) {
        if (!needParse(msAssertion, config)) {
            return;
        }
        Boolean globalEnable = msAssertion.getEnable();
        msAssertion.getAssertions()
                .stream()
                .filter(this::isHeaderAssertionValid)
                .forEach(headerAssertionItem -> {
                    ResponseAssertion responseAssertion = parse2ResponseAssertion(headerAssertionItem, globalEnable);
                    responseAssertion.setAssumeSuccess(isIgnoreStatus);
                    hashTree.add(responseAssertion);
                });
    }


    public boolean isHeaderAssertionValid(MsResponseHeaderAssertion.ResponseHeaderAssertionItem headerAssertionItem) {
        return StringUtils.isNotBlank(headerAssertionItem.getHeader())
                && StringUtils.isNotBlank(headerAssertionItem.getCondition())
                && StringUtils.isNotBlank(headerAssertionItem.getExpectedValue())
                && BooleanUtils.isTrue(headerAssertionItem.getEnable());
    }

    private ResponseAssertion parse2ResponseAssertion(MsResponseHeaderAssertion.ResponseHeaderAssertionItem msAssertion,
                                                      Boolean globalEnable) {
        ResponseAssertion assertion = createResponseAssertion();
        assertion.setEnabled(msAssertion.getEnable());
        if (BooleanUtils.isFalse(globalEnable)) {
            // 如果整体禁用，则禁用
            assertion.setEnabled(false);
        }
        String expectedValue = msAssertion.getExpectedValue();
        String condition = msAssertion.getCondition();
        MsAssertionCondition msAssertionCondition = EnumValidator.validateEnum(MsAssertionCondition.class, condition);
        String header = msAssertion.getHeader();

        setMsAssertionInfoProperty(assertion, ResponseAssertionResult.AssertionResultType.RESPONSE_HEADER.name(), header, condition, expectedValue);

        String regexTemplate = switch (msAssertionCondition) {
            case NOT_CONTAINS, CONTAINS -> "((?:[\\r\\n]%key|^%key):.*%value)";
            case EQUALS, NOT_EQUALS -> "((?:[\\r\\n]%key|^%key):\\s*(?:%value[\\r\\n]|%value$))";
            default -> null;
        };

        String testString = expectedValue;
        if (StringUtils.isNotEmpty(regexTemplate)) {
            testString = regexTemplate
                    .replace("%key", header)
                    .replace("%value", expectedValue);
        }

        if (StringUtils.startsWith(msAssertionCondition.name(), "NOT")) {
            // 如果是 not 则结果取反
            assertion.setToNotType();
        }

        assertion.setName(String.format("Response header %s %s %s", header, condition.toLowerCase().replace("_", " "), expectedValue));
        assertion.addTestString(testString);
        assertion.setToContainsType();

        assertion.setTestFieldResponseHeaders();
        return assertion;
    }
}
