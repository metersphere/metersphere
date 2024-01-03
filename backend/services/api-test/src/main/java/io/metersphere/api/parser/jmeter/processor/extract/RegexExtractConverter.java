package io.metersphere.api.parser.jmeter.processor.extract;

import io.metersphere.api.dto.request.processors.extract.RegexExtract;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.REGEX_EXTRACTOR_GUI;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-27  10:32
 */
public class RegexExtractConverter extends ExtractConverter<RegexExtract> {
    @Override
    public void parse(HashTree hashTree, RegexExtract msExtract, ParameterConfig config) {
        RegexExtractor extractor = new RegexExtractor();
        extractor.setName(msExtract.getVariableName());
        extractor.setProperty(TestElement.TEST_CLASS, RegexExtractor.class.getName());
        extractor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(REGEX_EXTRACTOR_GUI));
        extractor.setRefName(msExtract.getVariableName());
        extractor.setRegex(msExtract.getExpression());
        extractor.setUseField(msExtract.getExtractScope());
        extractor.setEnabled(msExtract.getEnable());

        // 处理匹配多条等匹配规则
        extractor.setMatchNumber(parseResultMatchingRule(msExtract));

        // $1$提取 JSON 响应中的第一个匹配项 $0$用于提取整个 JSON 响应
        if (StringUtils.isBlank(msExtract.getExpressionMatchingRule())) {
            extractor.setTemplate(RegexExtract.ExpressionRuleType.EXPRESSION.getValue());
        } else {
            extractor.setTemplate(msExtract.getExpressionMatchingRule());
        }
        hashTree.add(extractor);
    }
}
