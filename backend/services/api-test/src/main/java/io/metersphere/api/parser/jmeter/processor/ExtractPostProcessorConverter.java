package io.metersphere.api.parser.jmeter.processor;

import io.metersphere.project.api.processor.ExtractPostProcessor;
import io.metersphere.project.api.processor.extract.MsExtract;
import io.metersphere.api.parser.jmeter.processor.extract.ExtractConverterFactory;
import io.metersphere.plugin.api.dto.ParameterConfig;
import org.apache.jorphan.collections.HashTree;


/**
 * @Author: jianxing
 * @CreateTime: 2023-12-26  14:49
 */
public class ExtractPostProcessorConverter extends MsProcessorConverter<ExtractPostProcessor> {

    @Override
    public void parse(HashTree hashTree, ExtractPostProcessor processor, ParameterConfig config) {
        if (!needParse(processor, config) || processor.getExtractors() == null) {
            return;
        }
        processor.getExtractors()
                .stream()
                .filter(MsExtract::isValid)
                .forEach(extract -> {
                    // 单调提取器的 enable 跟随整体的 enable
                    extract.setEnable(processor.getEnable());
                    ExtractConverterFactory.getConverter(extract.getClass())
                            .parse(hashTree, extract, config);
                });
    }
}
