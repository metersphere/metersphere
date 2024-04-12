package io.metersphere.api.parser.jmeter.processor.assertion;

import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.assertion.MsResponseHeaderAssertion;
import io.metersphere.sdk.constants.MsAssertionCondition;
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
        String testString = switch (msAssertionCondition) {
            case CONTAINS -> StringUtils.join("\\b", header,": .*", expectedValue, ".*\\b");
            case NOT_CONTAINS -> StringUtils.join("\\b", header,": (?!.*", expectedValue, ").*\\b");
            case EQUALS -> StringUtils.join("\\b", header,": ",expectedValue, "\\b");
            case NOT_EQUALS -> StringUtils.join("\\b", header,": (?!", expectedValue,"\\b)\\d+");
            default -> expectedValue;
        };
        assertion.setName(String.format("Response header %s %s %s", header, condition.toLowerCase().replace("_", ""), expectedValue));
        assertion.addTestString(testString);
        assertion.setToContainsType();

        assertion.setTestFieldResponseHeaders();
        return assertion;
    }
}
