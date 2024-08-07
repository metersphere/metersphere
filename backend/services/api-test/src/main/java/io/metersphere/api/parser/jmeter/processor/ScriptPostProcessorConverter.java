package io.metersphere.api.parser.jmeter.processor;

import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.processor.ScriptProcessor;
import org.apache.jmeter.extractor.BeanShellPostProcessor;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.Optional;

/**
 * @Author: jianxing
 * @CreateTime: 2023-12-26  14:49
 */
public class ScriptPostProcessorConverter extends ScriptProcessorConverter {
    @Override
    public void parse(HashTree hashTree, ScriptProcessor scriptProcessor, ParameterConfig config) {
        if (!needParse(scriptProcessor, config) || !scriptProcessor.isValid()) {
            return;
        }
        TestElement processor;
        if (isJSR233(scriptProcessor)) {
            processor = new JSR223PostProcessor();
        } else {
            processor = new BeanShellPostProcessor();
        }
        parse(processor, scriptProcessor, config);

        // 添加公共脚本的参数
        Optional.ofNullable(getScriptArguments(scriptProcessor))
                .ifPresent(hashTree::add);

        hashTree.add(processor);
    }
}
