package io.metersphere.api.parser.jmeter.processor.assertion;

import io.metersphere.project.api.assertion.MsResponseHeaderAssertion;
import io.metersphere.sdk.util.EnumValidator;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.sdk.constants.MsAssertionCondition;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jorphan.collections.HashTree;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
                && StringUtils.isNotBlank(headerAssertionItem.getExpectedValue());
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
        assertion.setName(String.format("Response header %s %s", condition.toLowerCase().replace("_", ""), expectedValue));
        MsAssertionCondition msAssertionCondition = EnumValidator.validateEnum(MsAssertionCondition.class, condition);
        Map<MsAssertionCondition, Function<String, String>> regexgenerateMap = new HashMap<>();
        regexgenerateMap.put(MsAssertionCondition.EQUALS, value -> StringUtils.join("\\b", msAssertion.getHeader(),": ",value, "\\b"));
        regexgenerateMap.put(MsAssertionCondition.NOT_EQUALS, value -> StringUtils.join("\\b", msAssertion.getHeader(),": (?!", value,"\\b)\\d+"));
        regexgenerateMap.put(MsAssertionCondition.CONTAINS, value -> StringUtils.join("\\b", msAssertion.getHeader(),": .*", value, ".*\\b"));
        regexgenerateMap.put(MsAssertionCondition.NOT_CONTAINS, value -> StringUtils.join("\\b", msAssertion.getHeader(),": (?!.*", value, ").*\\b"));
        if (msAssertionCondition != null && regexgenerateMap.get(msAssertionCondition) != null) {
            assertion.addTestString(regexgenerateMap.get(msAssertionCondition).apply(expectedValue));
        } else {
            assertion.addTestString(expectedValue);
        }
        assertion.setToContainsType();

        assertion.setTestFieldResponseHeaders();
        return assertion;
    }
}
