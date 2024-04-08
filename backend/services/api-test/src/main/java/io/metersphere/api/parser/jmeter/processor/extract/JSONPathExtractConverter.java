package io.metersphere.api.parser.jmeter.processor.extract;

import io.metersphere.project.api.processor.extract.JSONPathExtract;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.JSON_POST_PROCESSOR_GUI;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-27  10:32
 */
public class JSONPathExtractConverter extends ExtractConverter<JSONPathExtract> {

    @Override
    public void parse(HashTree hashTree, JSONPathExtract msExtract, ParameterConfig config) {
        JSONPostProcessor extractor = new JSONPostProcessor();
        extractor.setName(msExtract.getVariableName());
        extractor.setProperty(TestElement.TEST_CLASS, JSONPostProcessor.class.getName());
        extractor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(JSON_POST_PROCESSOR_GUI));
        extractor.setRefNames(msExtract.getVariableName());
        extractor.setJsonPathExpressions(msExtract.getExpression());
        extractor.setProperty("JSONPostProcessor.compute_concat", true);
        // 处理匹配多条等匹配规则
        extractor.setMatchNumbers(parseResultMatchingRule(msExtract).toString());
        extractor.setEnabled(msExtract.getEnable());
        hashTree.add(extractor);
    }
}
