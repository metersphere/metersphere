package io.metersphere.api.parser.ms.http.post;

import io.metersphere.api.utils.ConverterUtils;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.processor.extract.JSONPathExtract;
import io.metersphere.project.api.processor.extract.ResultMatchingExtract;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jorphan.collections.HashTree;

public class JSONPostProcessorConverter extends AbstractMsElementConverter<JSONPostProcessor> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, JSONPostProcessor element, HashTree hashTree) {
        JSONPathExtract jsonPathExtract = new JSONPathExtract();
        jsonPathExtract.setVariableName(element.getRefNames());
        jsonPathExtract.setExpression(element.getJsonPathExpressions());
        jsonPathExtract.setEnable(element.isEnabled());
        jsonPathExtract.setVariableType("TEMPORARY");
        if (StringUtils.equalsIgnoreCase(element.getMatchNumbers(), "-1")) {
            jsonPathExtract.setResultMatchingRule(ResultMatchingExtract.ResultMatchingRuleType.ALL.name());
            jsonPathExtract.setResultMatchingRuleNum(-1);
        } else if (StringUtils.equalsIgnoreCase(element.getMatchNumbers(), "0")) {
            jsonPathExtract.setResultMatchingRule(ResultMatchingExtract.ResultMatchingRuleType.RANDOM.name());
            jsonPathExtract.setResultMatchingRuleNum(0);
        } else {
            jsonPathExtract.setResultMatchingRule(ResultMatchingExtract.ResultMatchingRuleType.SPECIFIC.name());
            try {
                jsonPathExtract.setResultMatchingRuleNum(Integer.parseInt(element.getMatchNumbers()));
            } catch (Exception ignore) {
            }
        }
        ConverterUtils.addPostExtract(parent, jsonPathExtract);
    }
}
