package io.metersphere.api.parser.jmeter;


import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.processors.MsProcessorConfig;
import io.metersphere.api.parser.jmeter.processor.MsProcessorConverterFactory;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * 脚本解析器
 */
public class MsCommonElementConverter extends AbstractJmeterElementConverter<MsCommonElement> {


    @Override
    public void toHashTree(HashTree tree, MsCommonElement element, ParameterConfig config) {
        //        todo 开关默认开启，关闭则运行该接口时不执行全局前置
        //        preProcessorConfig.getEnableGlobal();

        // 解析前置处理器
        handlePreProcessor(tree, element.getPreProcessorConfig(), config);
        // 解析后置处理器
        handlePostProcessor(tree, element.getPostProcessorConfig(), config);
    }

    private void handlePreProcessor(HashTree tree, MsProcessorConfig preProcessorConfig, ParameterConfig config) {
        if (preProcessorConfig == null || preProcessorConfig.getProcessors() == null) {
            return;
        }
        preProcessorConfig.getProcessors()
                .forEach(processor -> MsProcessorConverterFactory.getPreConverter(processor.getClass()).parse(tree, processor, config));
    }

    private void handlePostProcessor(HashTree tree, MsProcessorConfig postProcessorConfig , ParameterConfig config) {
        if (postProcessorConfig == null || postProcessorConfig.getProcessors() == null) {
            return;
        }
        postProcessorConfig.getProcessors()
                .forEach(processor -> MsProcessorConverterFactory.getPostConverter(processor.getClass()).parse(tree, processor, config));
    }
}
