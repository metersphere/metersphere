package io.metersphere.api.dto.request.processors.extract;

import lombok.Data;

@Data
public abstract class ResultMatchingExtract extends MsExtract {
    /**
     * 结果匹配规则
     * 值为 ResultMatchingRuleType
     */
    private String resultMatchingRule;
    /**
     * 匹配第几条结果
     */
    private Integer resultMatchingRuleNum;

    public enum ResultMatchingRuleType {
        /**
         * 随机匹配
         */
        RANDOM,
        /**
         * 指定匹配
         */
        SPECIFIC,
        /**
         * 全部匹配
         */
        ALL
    }
}
