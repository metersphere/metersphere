package io.metersphere.api.parser.jmeter.processor.extract;

import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.processor.extract.XPathExtract;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jmeter.extractor.XPathExtractor;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.X_PATH2_EXTRACTOR_GUI;
import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.X_PATH_EXTRACTOR_GUI;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-27  10:32
 */
public class XPathExtractConverter extends ExtractConverter<XPathExtract> {

    /**
     * 这里标记下 XPathExtractor 的 name，表达式错误时会添加一条断言
     * 处理结果是根据这个 name 来过滤这条断言
     */
    public static final String X_PATH2_EXTRACTOR_NAME = "X_PATH2_EXTRACTOR_NAME";

    @Override
    public void parse(HashTree hashTree, XPathExtract msExtract, ParameterConfig config) {
        if (StringUtils.equals(msExtract.getResponseFormat(), XPathExtract.ResponseFormat.HTML.name())) {
            hashTree.add(parseXPathExtract(msExtract));
        } else {
            hashTree.add(parseXPath2Extract(msExtract));
        }
    }

    private XPathExtractor parseXPathExtract(XPathExtract msExtract) {
        XPathExtractor extractor = new XPathExtractor();
        extractor.setTolerant(true);
        extractor.setName(X_PATH2_EXTRACTOR_NAME);
        extractor.setProperty(TestElement.TEST_CLASS, XPathExtractor.class.getName());
        extractor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(X_PATH_EXTRACTOR_GUI));
        extractor.setRefName(msExtract.getVariableName());
        extractor.setXPathQuery(msExtract.getExpression());
        extractor.setEnabled(msExtract.getEnable());
        // 处理匹配多条等匹配规则
        extractor.setMatchNumber(parseResultMatchingRule(msExtract));
        setMsExtractInfoProperty(extractor, msExtract.getVariableType(), msExtract.getExpression());
        return extractor;
    }

    private XPath2Extractor parseXPath2Extract(XPathExtract msExtract) {
        XPath2Extractor extractor = new XPath2Extractor();
        extractor.setName(X_PATH2_EXTRACTOR_NAME);
        extractor.setProperty(TestElement.TEST_CLASS, XPathExtractor.class.getName());
        extractor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(X_PATH2_EXTRACTOR_GUI));
        extractor.setRefName(msExtract.getVariableName());
        extractor.setXPathQuery(msExtract.getExpression());
        extractor.setEnabled(msExtract.getEnable());
        // 处理匹配多条等匹配规则
        extractor.setMatchNumber(parseResultMatchingRule(msExtract));
        setMsExtractInfoProperty(extractor, msExtract.getVariableType(), msExtract.getExpression());
        return extractor;
    }
}
