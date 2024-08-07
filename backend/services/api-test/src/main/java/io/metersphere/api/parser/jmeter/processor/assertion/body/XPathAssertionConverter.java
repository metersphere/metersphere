package io.metersphere.api.parser.jmeter.processor.assertion.body;

import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.assertion.body.MsXPathAssertion;
import io.metersphere.project.api.assertion.body.MsXPathAssertionItem;
import io.metersphere.project.api.processor.extract.XPathExtract;
import io.metersphere.sdk.dto.api.result.ResponseAssertionResult;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.XPath2Assertion;
import org.apache.jmeter.assertions.XPathAssertion;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.XPATH_ASSERTION_GUI;
import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.X_PATH_2_ASSERTION_GUI;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-03  10:05
 */
public class XPathAssertionConverter extends ResponseBodyTypeAssertionConverter<MsXPathAssertion> {
    @Override
    public void parse(HashTree hashTree, MsXPathAssertion msAssertion, ParameterConfig config, boolean isIgnoreStatus, boolean globalEnable) {
        if (msAssertion == null || msAssertion.getAssertions() == null) {
            return;
        }
        String responseFormat = msAssertion.getResponseFormat();
        msAssertion.getAssertions().stream()
                .filter(MsXPathAssertionItem::isValid)
                .forEach(msXPathAssertionItem -> {
                    if (needParse(msXPathAssertionItem, config)) {
                        if (StringUtils.equals(responseFormat, XPathExtract.ResponseFormat.HTML.name())) {
                            XPathAssertion xPathAssertion = parse2XPathAssertion(msXPathAssertionItem, globalEnable);
                            hashTree.add(xPathAssertion);
                        } else {
                            XPath2Assertion xPath2Assertion = parse2XPath2Assertion(msXPathAssertionItem, globalEnable);
                            hashTree.add(xPath2Assertion);
                        }
                    }
                });
    }

    private XPathAssertion parse2XPathAssertion(MsXPathAssertionItem msAssertion, Boolean globalEnable) {
        XPathAssertion assertion = new XPathAssertion();
        String expression = msAssertion.getExpression();
        setMsAssertionInfoProperty(assertion, ResponseAssertionResult.AssertionResultType.XPATH.name(), expression);
        assertion.setEnabled(msAssertion.getEnable());
        assertion.setTolerant(true);
        assertion.setValidating(false);
        assertion.setName("Response data expect xpath " + expression);
        assertion.setProperty(TestElement.TEST_CLASS, XPathAssertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(XPATH_ASSERTION_GUI));
        assertion.setXPathString(expression);
        assertion.setNegated(false);
        assertion.setEnabled(msAssertion.getEnable());
        if (BooleanUtils.isFalse(globalEnable)) {
            // 如果整体禁用，则禁用
            assertion.setEnabled(false);
        }
        return assertion;
    }

    private XPath2Assertion parse2XPath2Assertion(MsXPathAssertionItem msAssertion, Boolean globalEnable) {
        XPath2Assertion assertion = new XPath2Assertion();
        String expression = msAssertion.getExpression();
        setMsAssertionInfoProperty(assertion, ResponseAssertionResult.AssertionResultType.XPATH.name(), expression);
        assertion.setEnabled(msAssertion.getEnable());
        assertion.setName("Response date expect xpath " +expression);
        assertion.setProperty(TestElement.TEST_CLASS, XPath2Assertion.class.getName());
        assertion.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(X_PATH_2_ASSERTION_GUI));
        assertion.setXPathString(expression);
        assertion.setNegated(false);
        assertion.setEnabled(msAssertion.getEnable());
        if (BooleanUtils.isFalse(globalEnable)) {
            // 如果整体禁用，则禁用
            assertion.setEnabled(false);
        }
        return assertion;
    }
}
