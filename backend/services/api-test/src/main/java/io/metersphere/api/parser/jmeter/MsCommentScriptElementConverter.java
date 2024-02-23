package io.metersphere.api.parser.jmeter;

import io.metersphere.api.dto.request.controller.MsCommentScriptElement;
import io.metersphere.project.api.KeyValueParam;
import io.metersphere.project.api.processor.ScriptProcessor;
import io.metersphere.api.parser.jmeter.processor.ScriptProcessorConverter;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.jmeter.modifiers.UserParameters;
import org.apache.jmeter.protocol.java.sampler.BeanShellSampler;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.USER_PARAMETERS_GUI;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-18  22:04
 */
public class MsCommentScriptElementConverter extends AbstractJmeterElementConverter<MsCommentScriptElement> {

    @Override
    public void toHashTree(HashTree hashTree, MsCommentScriptElement msElement, ParameterConfig config) {

        if (CollectionUtils.isNotEmpty(msElement.getParams())) {
            // 添加变量
            List<KeyValueParam> params = msElement.getParams()
                    .stream()
                    .filter(KeyValueParam::isValid)
                    .collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(params)) {
                UserParameters userParameters = getUserParameters(params);
                hashTree.add(userParameters);
            }
        }

        // 添加脚本
        ScriptProcessor scriptProcessor = new ScriptProcessor();
        scriptProcessor.setScriptLanguage(msElement.getScriptLanguage());
        scriptProcessor.setScript(msElement.getScript());
        TestElement scriptElement;
        if (ScriptProcessorConverter.isJSR233(scriptProcessor)) {
            scriptElement = new JSR223Sampler();
        } else {
            scriptElement = new BeanShellSampler();
        }
        ScriptProcessorConverter.parse(scriptElement, scriptProcessor);
        // 添加公共脚本的参数
        Optional.ofNullable(ScriptProcessorConverter.getScriptArguments(scriptProcessor))
                .ifPresent(hashTree::add);
        hashTree.add(scriptElement);
    }

    public static UserParameters getUserParameters(List<KeyValueParam> params) {
        UserParameters processor = new UserParameters();
        processor.setEnabled(true);
        processor.setName("User Defined Variables");
        processor.setPerIteration(true);
        processor.setProperty(TestElement.TEST_CLASS, UserParameters.class.getName());
        processor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(USER_PARAMETERS_GUI));
        if (CollectionUtils.isNotEmpty(params)) {
            List<String> names = new LinkedList<>();
            List<Object> values = new LinkedList<>();
            List<Object> threadValues = new LinkedList<>();
            for (KeyValueParam param : params) {
                String name = param.getKey();
                String value = param.getValue();
                names.add(name);
                values.add(value);
            }
            processor.setNames(names);
            threadValues.add(values);
            processor.setThreadLists(threadValues);
        }
        return processor;
    }
}
