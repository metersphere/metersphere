package io.metersphere.api.parser.jmeter.processor;

import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.processor.SQLProcessor;
import org.apache.jmeter.protocol.jdbc.processor.JDBCPreProcessor;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-26  14:49
 */
public class SqlPreProcessorConverter extends SqlProcessorConverter {
    @Override
    public void parse(HashTree hashTree, SQLProcessor sqlProcessor, ParameterConfig config) {
        parse(hashTree, sqlProcessor, config, JDBCPreProcessor.class);
    }
}
