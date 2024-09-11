package io.metersphere.api.parser.ms.http.post;

import io.metersphere.api.utils.ConverterUtils;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.assertion.MsResponseBodyAssertion;
import io.metersphere.project.api.assertion.body.MsJSONPathAssertion;
import io.metersphere.project.api.assertion.body.MsRegexAssertion;
import io.metersphere.project.api.assertion.body.MsXPathAssertion;
import io.metersphere.project.api.assertion.body.MsXPathAssertionItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.assertions.XPathAssertion;
import org.apache.jorphan.collections.HashTree;

import java.util.ArrayList;
import java.util.List;

public class XPathAssertionConverter extends AbstractMsElementConverter<XPathAssertion> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, XPathAssertion element, HashTree hashTree) {
        MsResponseBodyAssertion msResponseBodyAssertion = new MsResponseBodyAssertion();
        msResponseBodyAssertion.setAssertionBodyType(MsResponseBodyAssertion.MsBodyAssertionType.JSON_PATH.name());
        msResponseBodyAssertion.setJsonPathAssertion(new MsJSONPathAssertion());
        msResponseBodyAssertion.setRegexAssertion(new MsRegexAssertion());
        msResponseBodyAssertion.setName(element.getName());
        msResponseBodyAssertion.setEnable(true);

        MsXPathAssertion msXPathAssertion = new MsXPathAssertion();
        if (element.isValidating()) {
            msXPathAssertion.setResponseFormat(MsXPathAssertion.ResponseFormat.XML.name());
        } else {
            msXPathAssertion.setResponseFormat(MsXPathAssertion.ResponseFormat.HTML.name());
        }

        MsXPathAssertionItem xpathItem = new MsXPathAssertionItem();
        xpathItem.setEnable(element.isEnabled());
        xpathItem.setExpectedValue(StringUtils.EMPTY);
        xpathItem.setExpression(element.getXPathString());

        List<MsXPathAssertionItem> assertions = new ArrayList<>();
        assertions.add(xpathItem);
        msXPathAssertion.setAssertions(assertions);
        msResponseBodyAssertion.setXpathAssertion(msXPathAssertion);
        ConverterUtils.addAssertions(parent, msResponseBodyAssertion, "XPATH");
    }
}
