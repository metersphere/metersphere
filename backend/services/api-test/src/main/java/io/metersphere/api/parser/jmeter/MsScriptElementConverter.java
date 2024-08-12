package io.metersphere.api.parser.jmeter;

import io.metersphere.api.dto.request.controller.MsScriptElement;
import io.metersphere.api.parser.jmeter.constants.JmeterAlias;
import io.metersphere.api.parser.jmeter.constants.JmeterProperty;
import io.metersphere.api.parser.jmeter.processor.ScriptProcessorConverter;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.metersphere.sdk.util.BeanUtils;
import org.apache.jmeter.protocol.java.sampler.BeanShellSampler;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.Optional;

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

        if (!ScriptProcessorConverter.isJSR233(scriptProcessor)) {
            // beanshell 参数名有区别，替换一下
            scriptElement.setProperty(JmeterProperty.BEAN_SHELL_SAMPLER_QUERY, scriptElement.getProperty(JmeterProperty.SCRIPT).getStringValue());
            scriptElement.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(JmeterAlias.BEAN_SHELL_SAMPLER_GUI));
        }

        setStepIdentification(msScriptElement, config, scriptElement);


        // 添加公共脚本的参数
        Optional.ofNullable(ScriptProcessorConverter.getScriptArguments(scriptProcessor))
                .ifPresent(hashTree::add);

        HashTree scriptTree = hashTree.add(scriptElement);

        parseChild(scriptTree, msScriptElement, config);
    }
}
