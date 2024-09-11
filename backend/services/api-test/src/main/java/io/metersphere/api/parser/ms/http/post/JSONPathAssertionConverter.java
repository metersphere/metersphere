package io.metersphere.api.parser.ms.http.post;

import io.metersphere.api.utils.ConverterUtils;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.assertion.MsResponseBodyAssertion;
import io.metersphere.project.api.assertion.body.MsJSONPathAssertion;
import io.metersphere.project.api.assertion.body.MsJSONPathAssertionItem;
import io.metersphere.project.api.assertion.body.MsRegexAssertion;
import io.metersphere.project.api.assertion.body.MsXPathAssertion;
import io.metersphere.sdk.dto.CombineCondition;
import org.apache.jmeter.assertions.JSONPathAssertion;
import org.apache.jorphan.collections.HashTree;

import java.util.ArrayList;
import java.util.List;

public class JSONPathAssertionConverter extends AbstractMsElementConverter<JSONPathAssertion> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, JSONPathAssertion element, HashTree hashTree) {
        MsResponseBodyAssertion msResponseBodyAssertion = new MsResponseBodyAssertion();
        msResponseBodyAssertion.setAssertionBodyType(MsResponseBodyAssertion.MsBodyAssertionType.JSON_PATH.name());
        msResponseBodyAssertion.setXpathAssertion(new MsXPathAssertion());
        msResponseBodyAssertion.setRegexAssertion(new MsRegexAssertion());
        msResponseBodyAssertion.setName(element.getName());
        msResponseBodyAssertion.setEnable(true);

        MsJSONPathAssertionItem jsonPathAssertionItem = new MsJSONPathAssertionItem();
        jsonPathAssertionItem.setCondition(CombineCondition.CombineConditionOperator.EQUALS.name());
        jsonPathAssertionItem.setEnable(element.isEnabled());
        jsonPathAssertionItem.setExpectedValue(element.getExpectedValue());
        jsonPathAssertionItem.setExpression(element.getJsonPath());

        List<MsJSONPathAssertionItem> assertions = new ArrayList<>();
        assertions.add(jsonPathAssertionItem);

        msResponseBodyAssertion.setJsonPathAssertion(new MsJSONPathAssertion() {{
            this.setAssertions(assertions);
        }});

        ConverterUtils.addAssertions(parent, msResponseBodyAssertion, "JSON_PATH");
    }
}
