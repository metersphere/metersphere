package io.metersphere.api.parser.jmeter;

import io.metersphere.api.dto.request.controller.MsScriptElement;
import io.metersphere.api.parser.jmeter.processor.ScriptProcessorConverter;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.metersphere.sdk.util.BeanUtils;
import org.apache.jmeter.protocol.java.sampler.BeanShellSampler;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-18  22:04
 */
public class MsScriptElementConverter extends AbstractJmeterElementConverter<MsScriptElement> {

    @Override
    public void toHashTree(HashTree hashTree, MsScriptElement msScriptElement, ParameterConfig config) {

        // 添加脚本
        ScriptProcessor scriptProcessor = BeanUtils.copyBean(new ScriptProcessor(), msScriptElement);
        TestElement scriptElement;
        if (ScriptProcessorConverter.isJSR233(scriptProcessor)) {
            scriptElement = new JSR223Sampler();
        } else {
            scriptElement = new BeanShellSampler();
        }
        ScriptProcessorConverter.parse(scriptElement, scriptProcessor, config);
        hashTree.add(scriptElement);
    }
}
