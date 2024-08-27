package io.metersphere.api.parser.ms.http.post;

import io.metersphere.api.utils.ConverterUtils;
import io.metersphere.plugin.api.spi.AbstractMsElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.processor.extract.JSONPathExtract;
import io.metersphere.project.api.processor.extract.RegexExtract;
import io.metersphere.project.api.processor.extract.ResultMatchingExtract;
import io.metersphere.project.api.processor.extract.XPathExtract;
import org.apache.jmeter.extractor.XPathExtractor;
import org.apache.jorphan.collections.HashTree;

public class XPathExtractorConverter extends AbstractMsElementConverter<XPathExtractor> {
    @Override
    public void toMsElement(AbstractMsTestElement parent, XPathExtractor element, HashTree hashTree) {

        JSONPathExtract jsonPathExtract = new JSONPathExtract();
        RegexExtract regexExtract = new RegexExtract();

        XPathExtract xPathExtract = new XPathExtract();
        xPathExtract.setEnable(element.isEnabled());
        xPathExtract.setResponseFormat(XPathExtract.ResponseFormat.HTML.name());
        xPathExtract.setVariableName(element.getRefName());
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
