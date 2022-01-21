package io.metersphere.api.dto.definition.request.extract;

import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.XPathExtractor;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "Extract")
public class MsExtract extends MsTestElement {
    private String clazzName = MsExtract.class.getCanonicalName();

    private List<MsExtractRegex> regex;
    private List<MsExtractJSONPath> json;
    private List<MsExtractXPath> xpath;
    private String type = "Extract";

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        addRequestExtractors(tree, config);
    }

    private void addRequestExtractors(HashTree samplerHashTree, ParameterConfig config) {
        StringJoiner extract = new StringJoiner(";");

        if (CollectionUtils.isNotEmpty(this.getRegex())) {
            this.getRegex().stream().filter(MsExtractRegex::isValid).forEach(extractRegex ->
                    samplerHashTree.add(regexExtractor(extractRegex, extract))
            );
        }
        if (CollectionUtils.isNotEmpty(this.getXpath())) {
            this.getXpath().stream().filter(MsExtractCommon::isValid).forEach(extractXPath ->
                    samplerHashTree.add(xPath2Extractor(extractXPath, extract))
            );
        }
        if (CollectionUtils.isNotEmpty(this.getJson())) {
            this.getJson().stream().filter(MsExtractCommon::isValid).forEach(extractJSONPath ->
                    samplerHashTree.add(jsonPostProcessor(extractJSONPath, extract))
            );
        }
        if (Optional.ofNullable(extract).orElse(extract).length() > 0 && !config.isOperating()) {
            JSR223PostProcessor shell = new JSR223PostProcessor();
            shell.setEnabled(true);
            shell.setName(StringUtils.isEmpty(this.getName()) ? "JSR223PostProcessor" : this.getName());
            shell.setProperty(TestElement.TEST_CLASS, JSR223PostProcessor.class.getName());
            shell.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
            shell.setProperty("cacheKey", false);
            shell.setProperty("script", "io.metersphere.utils.JMeterVars.addVars(prev.getResourceId(),vars," + "\"" + extract.toString() + "\"" + ");");
            samplerHashTree.add(shell);
        }
    }

    private RegexExtractor regexExtractor(MsExtractRegex extractRegex, StringJoiner extract) {

        RegexExtractor extractor = new RegexExtractor();
        extractor.setEnabled(this.isEnable());
        extractor.setName(StringUtils.isNotEmpty(extractRegex.getVariable()) ? extractRegex.getVariable() : this.getName());
        if(StringUtils.isEmpty(extractor.getName())){
            extractor.setName("RegexExtractor");
        }
        /*extractor.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() : " RegexExtractor");*/
        extractor.setProperty(TestElement.TEST_CLASS, RegexExtractor.class.getName());
        extractor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("RegexExtractorGui"));
        extractor.setRefName(extractRegex.getVariable());
        extractor.setRegex(extractRegex.getExpression());
        extractor.setUseField(extractRegex.getUseHeaders());
        if (extractRegex.isMultipleMatching()) {
            extractor.setMatchNumber(-1);
        }
        extractor.setTemplate("$1$");
        extract.add(extractor.getRefName());

        return extractor;
    }

    private XPathExtractor xPath2Extractor(MsExtractXPath extractXPath, StringJoiner extract) {
        XPathExtractor extractor = new XPathExtractor();
        extractor.setEnabled(this.isEnable());
        extractor.setTolerant(true);
        extractor.setName(StringUtils.isNotEmpty(extractXPath.getVariable()) ? extractXPath.getVariable() : this.getName());
        if(StringUtils.isEmpty(extractor.getName())){
            extractor.setName("XPath2Extractor");
        }
        extractor.setProperty(TestElement.TEST_CLASS, XPathExtractor.class.getName());
        extractor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("XPathExtractorGui"));
        extractor.setRefName(extractXPath.getVariable());
        extractor.setXPathQuery(extractXPath.getExpression());
        if (extractXPath.isMultipleMatching()) {
            extractor.setMatchNumber(-1);
        }
        extract.add(extractor.getRefName());
        return extractor;
    }

    private JSONPostProcessor jsonPostProcessor(MsExtractJSONPath extractJSONPath, StringJoiner extract) {
        JSONPostProcessor extractor = new JSONPostProcessor();
        extractor.setEnabled(this.isEnable());
        extractor.setName(StringUtils.isNotEmpty(extractJSONPath.getVariable()) ? extractJSONPath.getVariable() : this.getName());
        if(StringUtils.isEmpty(extractor.getName())){
            extractor.setName("JSONPostProcessor");
        }
        /*extractor.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() : " JSONExtractor");*/
        extractor.setProperty(TestElement.TEST_CLASS, JSONPostProcessor.class.getName());
        extractor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("JSONPostProcessorGui"));
        extractor.setRefNames(extractJSONPath.getVariable());
        extractor.setJsonPathExpressions(extractJSONPath.getExpression());
        extractor.setComputeConcatenation(true);
        if (extractJSONPath.isMultipleMatching()) {
            extractor.setMatchNumbers("-1");
        }
        extract.add(extractor.getRefNames());
        return extractor;
    }
}
