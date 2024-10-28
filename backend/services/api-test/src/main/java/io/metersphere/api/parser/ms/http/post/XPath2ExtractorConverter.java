package io.metersphere.api.parser.ms.http.post;

import io.metersphere.api.utils.ConverterUtils;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.processor.extract.ResultMatchingExtract;
import io.metersphere.project.api.processor.extract.XPathExtract;
import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jorphan.collections.HashTree;

public class XPath2ExtractorConverter extends AbstractMsElementConverter<XPath2Extractor> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, XPath2Extractor element, HashTree hashTree) {
        XPathExtract xPathExtract = new XPathExtract();
        xPathExtract.setEnable(element.isEnabled());
        xPathExtract.setResponseFormat(XPathExtract.ResponseFormat.XML.name());
        xPathExtract.setVariableName(element.getRefName());
        xPathExtract.setVariableType("TEMPORARY");
        xPathExtract.setExpression(element.getXPathQuery());
        if (element.getMatchNumber() == -1) {
            xPathExtract.setResultMatchingRule(ResultMatchingExtract.ResultMatchingRuleType.ALL.name());
            xPathExtract.setResultMatchingRuleNum(-1);
        } else if (element.getMatchNumber() == 0) {
            xPathExtract.setResultMatchingRule(ResultMatchingExtract.ResultMatchingRuleType.RANDOM.name());
            xPathExtract.setResultMatchingRuleNum(0);
        } else {
            xPathExtract.setResultMatchingRule(ResultMatchingExtract.ResultMatchingRuleType.SPECIFIC.name());
            xPathExtract.setResultMatchingRuleNum(element.getMatchNumber());
        }
        ConverterUtils.addPostExtract(parent, xPathExtract);
    }
}
