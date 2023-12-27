package io.metersphere.api.parser.jmeter.processor.extract;

import io.metersphere.api.dto.request.processors.extract.MsExtract;
import io.metersphere.api.dto.request.processors.extract.ResultMatchingExtract;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-27  10:32
 */
public abstract class ExtractConverter<T extends MsExtract> {
    /**
     * 解析对应的提取器
     * @param hashTree
     * @param extract
     * @param config
     */
    public abstract void parse(HashTree hashTree, T extract, ParameterConfig config);
 
    /**
     * 处理匹配多条等匹配规则
     * @param extract
     * @return
     */
    protected Integer parseResultMatchingRule(ResultMatchingExtract extract) {
        String resultMatchingRule = extract.getResultMatchingRule();
        if (StringUtils.equals(resultMatchingRule, ResultMatchingExtract.ResultMatchingRuleType.ALL.name())) {
            return -1;
        } else if (StringUtils.equals(resultMatchingRule, ResultMatchingExtract.ResultMatchingRuleType.RANDOM.name())) {
            return 0;
        } else if (StringUtils.equals(resultMatchingRule, ResultMatchingExtract.ResultMatchingRuleType.SPECIFIC.name())
                && extract.getResultMatchingRuleNum() != null) {
            return extract.getResultMatchingRuleNum();
        }
        return 1;
    }
}
