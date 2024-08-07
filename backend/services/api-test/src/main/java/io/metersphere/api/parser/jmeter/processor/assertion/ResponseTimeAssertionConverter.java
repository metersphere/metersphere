package io.metersphere.api.parser.jmeter.processor.assertion;

import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.assertion.MsResponseTimeAssertion;
import io.metersphere.sdk.dto.api.result.ResponseAssertionResult;
import org.apache.jmeter.assertions.DurationAssertion;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.DURATION_ASSERTION_GUI;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-27  21:01
 */
public class ResponseTimeAssertionConverter extends AssertionConverter<MsResponseTimeAssertion> {


    @Override
    public void parse(HashTree hashTree, MsResponseTimeAssertion msAssertion, ParameterConfig config, boolean isIgnoreStatus) {
        if (!needParse(msAssertion, config) || !isValid(msAssertion)) {
            return;
        }
        hashTree.add(parse2DurationAssertion(msAssertion));
    }

    private DurationAssertion parse2DurationAssertion(MsResponseTimeAssertion msAssertion) {
        DurationAssertion assertion = new DurationAssertion();
        assertion.setEnabled(msAssertion.getEnable());
        assertion.setName("Response In Time: " + msAssertion.getExpectedValue());
        assertion.setProperty(TestElement.TEST_CLASS, DurationAssertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(DURATION_ASSERTION_GUI));
        assertion.setAllowedDuration(msAssertion.getExpectedValue());
        setMsAssertionInfoProperty(assertion, ResponseAssertionResult.AssertionResultType.RESPONSE_TIME.name(), String.valueOf(msAssertion.getExpectedValue()), null, String.valueOf(msAssertion.getExpectedValue()));
        return assertion;
    }

    public boolean isValid(MsResponseTimeAssertion msAssertion) {
        return msAssertion.getExpectedValue() != null && msAssertion.getExpectedValue() > 0;
    }


}
