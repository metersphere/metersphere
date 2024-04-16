package io.metersphere.api.parser.jmeter.processor;

import io.metersphere.api.dto.ApiParamConfig;
import io.metersphere.api.parser.jmeter.constants.JmeterAlias;
import io.metersphere.api.parser.jmeter.constants.JmeterProperty;
import io.metersphere.api.parser.jmeter.processor.extract.ExtractConverterFactory;
import io.metersphere.plugin.api.constants.ElementProperty;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.project.api.processor.ExtractPostProcessor;
import io.metersphere.project.api.processor.extract.MsExtract;
import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;


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

        //提取参数应用到环境变量
        List<MsExtract> list = processor.getExtractors().stream()
                .filter(extract -> StringUtils.equals(extract.getVariableType(), MsExtract.MsExtractType.ENVIRONMENT.name())
                        && extract.isValid() && extract.getEnable()).toList();

        ApiParamConfig apiParamConfig = (ApiParamConfig) config;
        EnvironmentInfoDTO envConfig = apiParamConfig.getEnvConfig(processor.getProjectId());
        if (CollectionUtils.isNotEmpty(list) && envConfig != null) {
            //需要生成一个后置脚本
            String envId = envConfig.getId();
            JSR223PostProcessor jsr223PostProcessor = new JSR223PostProcessor();
            jsr223PostProcessor.setName("Set Environment Variable");
            jsr223PostProcessor.setProperty(TestElement.TEST_CLASS, jsr223PostProcessor.getClass().getSimpleName());
            jsr223PostProcessor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(JmeterAlias.TEST_BEAN_GUI));
            jsr223PostProcessor.setProperty(ElementProperty.PROJECT_ID.name(), processor.getProjectId());
            jsr223PostProcessor.setProperty(JmeterProperty.SCRIPT_LANGUAGE, ScriptLanguageType.BEANSHELL.name().toLowerCase());
            StringBuilder scriptBuilder = new StringBuilder();
            list.forEach(extract -> {
                String script = """
                        vars.put("MS.ENV.%s.%s","${%s}");
                        vars.put("%s","${%s}");
                        """;
                scriptBuilder.append(String.format(script, envId, extract.getVariableName(), extract.getVariableName(), extract.getVariableName(), extract.getVariableName()));
            });
            jsr223PostProcessor.setProperty(JmeterProperty.SCRIPT, scriptBuilder.toString());
            hashTree.add(jsr223PostProcessor);
        }
    }
}
