package io.metersphere.api.parser.jmeter.processor;


import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.processor.MsProcessor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * body 解析器
 */
public abstract class MsProcessorConverter<T extends MsProcessor> {

    /**
     * 解析对应的前后置处理器
     * @param hashTree
     * @param processor
     * @param config
     */
    public abstract void parse(HashTree hashTree, T processor, ParameterConfig config);

    protected boolean needParse(MsProcessor processor, ParameterConfig config) {
        // 如果组件是启用的，或者设置了解析禁用的组件，则返回 true
        if (BooleanUtils.isTrue(processor.getEnable()) || config.getParseDisabledElement()) {
            return true;
        }
        return false;
    }
}
