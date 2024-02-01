package io.metersphere.api.parser.jmeter.processor;

import io.metersphere.project.api.processor.SQLProcessor;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-26  14:49
 */
public class SqlPreProcessorConverter extends SqlProcessorConverter {
    @Override
    public void parse(HashTree hashTree, SQLProcessor scriptProcessor, ParameterConfig config) {
        if (!needParse(scriptProcessor, config)) {
            return;
        }
        // todo 等环境开发完之后，补充
    }
}
