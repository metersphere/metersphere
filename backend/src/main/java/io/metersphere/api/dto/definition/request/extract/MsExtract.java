package io.metersphere.api.dto.definition.request.extract;

import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "Extract")
public class MsExtract extends MsTestElement {
    private List<MsExtractRegex> regex;
    private List<MsExtractJSONPath> json;
    private List<MsExtractXPath> xpath;
    private String type = "Extract";

    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        if (!this.isEnable()) {
            return;
        }
        addRequestExtractors(tree);
    }

    private void addRequestExtractors(HashTree samplerHashTree) {
        if (CollectionUtils.isNotEmpty(this.getRegex())) {
            this.getRegex().stream().filter(MsExtractRegex::isValid).forEach(extractRegex ->
                    samplerHashTree.add(regexExtractor(extractRegex))
            );
        }
        if (CollectionUtils.isNotEmpty(this.getXpath())) {
            this.getXpath().stream().filter(MsExtractCommon::isValid).forEach(extractXPath ->
                    samplerHashTree.add(xPath2Extractor(extractXPath))
            );
        }
        if (CollectionUtils.isNotEmpty(this.getJson())) {
            this.getJson().stream().filter(MsExtractCommon::isValid).forEach(extractJSONPath ->
                    samplerHashTree.add(jsonPostProcessor(extractJSONPath))
            );
        }
    }

    private RegexExtractor regexExtractor(MsExtractRegex extractRegex) {
        RegexExtractor extractor = new RegexExtractor();
        extractor.setEnabled(true);
        extractor.setName(extractRegex.getVariable() + " RegexExtractor");
        extractor.setProperty(TestElement.TEST_CLASS, RegexExtractor.class.getName());
        extractor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("RegexExtractorGui"));
        extractor.setRefName(extractRegex.getVariable());
        extractor.setRegex(extractRegex.getExpression());
        extractor.setUseField(extractRegex.getUseHeaders());
        if (extractRegex.isMultipleMatching()) {
            extractor.setMatchNumber(-1);
        }
        extractor.setTemplate("$1$");
        return extractor;
    }

    private XPath2Extractor xPath2Extractor(MsExtractXPath extractXPath) {
        XPath2Extractor extractor = new XPath2Extractor();
        extractor.setEnabled(true);
        extractor.setName(extractXPath.getVariable() + " XPath2Extractor");
        extractor.setProperty(TestElement.TEST_CLASS, XPath2Extractor.class.getName());
        extractor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("XPath2ExtractorGui"));
        extractor.setRefName(extractXPath.getVariable());
        extractor.setXPathQuery(extractXPath.getExpression());
        if (extractXPath.isMultipleMatching()) {
            extractor.setMatchNumber(-1);
        }
        return extractor;
    }

    private JSONPostProcessor jsonPostProcessor(MsExtractJSONPath extractJSONPath) {
        JSONPostProcessor extractor = new JSONPostProcessor();
        extractor.setEnabled(true);
        extractor.setName(extractJSONPath.getVariable() + " JSONExtractor");
        extractor.setProperty(TestElement.TEST_CLASS, JSONPostProcessor.class.getName());
        extractor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("JSONPostProcessorGui"));
        extractor.setRefNames(extractJSONPath.getVariable());
        extractor.setJsonPathExpressions(extractJSONPath.getExpression());
        if (extractJSONPath.isMultipleMatching()) {
            extractor.setMatchNumbers("-1");
        }
        return extractor;
    }
}
