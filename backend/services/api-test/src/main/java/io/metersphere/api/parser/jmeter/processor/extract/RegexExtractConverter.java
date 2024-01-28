package io.metersphere.api.parser.jmeter.processor.extract;

import io.metersphere.api.dto.request.processors.extract.RegexExtract;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.HashMap;

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
        extractor.setEnabled(msExtract.getEnable());
        extractor.setUseField(getUseField(msExtract.getExtractScope()));
        extractor.setTemplate(getTemplate(msExtract.getExpressionMatchingRule()));
        // 处理匹配多条等匹配规则
        extractor.setMatchNumber(parseResultMatchingRule(msExtract));
        hashTree.add(extractor);
    }

    private String getTemplate(String expressionMatchingRule) {
        // $1$提取 JSON 响应中的第一个匹配项 $0$用于提取整个 JSON 响应
        HashMap<String, String> ruleValueMap = new HashMap<>() {{
            put(RegexExtract.ExpressionRuleType.EXPRESSION.name(), "$1$");
            put(RegexExtract.ExpressionRuleType.GROUP.name(), "$0$");
        }};
        if (StringUtils.isBlank(expressionMatchingRule)) {
            return ruleValueMap.get(RegexExtract.ExpressionRuleType.EXPRESSION.name());
        } else {
            return ruleValueMap.get(expressionMatchingRule);
        }
    }

    private String getUseField(String extractScope) {
        HashMap<String, String> extractScopeMap = new HashMap<>() {{
            put(RegexExtract.ExtractScope.BODY.name(), "false");
            put(RegexExtract.ExtractScope.REQUEST_HEADERS.name(), "request_headers");
            put(RegexExtract.ExtractScope.UNESCAPED_BODY.name(), "unescaped");
            put(RegexExtract.ExtractScope.BODY_AS_DOCUMENT.name(), "as_document");
            put(RegexExtract.ExtractScope.RESPONSE_HEADERS.name(), "true");
            put(RegexExtract.ExtractScope.URL.name(), "URL");
            put(RegexExtract.ExtractScope.RESPONSE_CODE.name(), "code");
            put(RegexExtract.ExtractScope.RESPONSE_MESSAGE.name(), "message");
        }};
        if (StringUtils.isNotBlank(extractScope)) {
            return extractScopeMap.get(extractScope);
        } else {
            return RegexExtract.ExtractScope.BODY.name();
        }
    }
}
