package io.metersphere.project.api.processor.extract;

import io.metersphere.sdk.valid.EnumValue;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public abstract class ResultMatchingExtract extends MsExtract {
    /**
     * 结果匹配规则
     * 取值参考 {@link ResultMatchingRuleType}
     * 默认随机匹配
     */
    @Size(max = 100)
    @EnumValue(enumClass = ResultMatchingRuleType.class)
    private String resultMatchingRule = ResultMatchingRuleType.RANDOM.name();
    /**
     * 匹配第几条结果
     */
    private Integer resultMatchingRuleNum;

    /**
     * 结果匹配规则
     */
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
