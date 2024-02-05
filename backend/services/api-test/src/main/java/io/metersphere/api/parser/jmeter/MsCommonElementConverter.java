package io.metersphere.api.parser.jmeter;


import io.metersphere.api.constants.ApiConstants;
import io.metersphere.api.dto.ApiParamConfig;
import io.metersphere.api.dto.assertion.MsAssertionConfig;
import io.metersphere.api.dto.request.MsCommonElement;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.processors.MsProcessorConfig;
import io.metersphere.api.parser.jmeter.processor.MsProcessorConverter;
import io.metersphere.api.parser.jmeter.processor.MsProcessorConverterFactory;
import io.metersphere.api.parser.jmeter.processor.assertion.AssertionConverterFactory;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.assertion.MsAssertion;
import io.metersphere.project.api.assertion.MsResponseCodeAssertion;
import io.metersphere.project.api.processor.MsProcessor;
import io.metersphere.project.dto.environment.EnvironmentConfig;
import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import io.metersphere.project.dto.environment.processors.ApiEnvProcessorConfig;
import io.metersphere.project.dto.environment.processors.ApiEnvRequestProcessorConfig;
import io.metersphere.project.dto.environment.processors.EnvProcessorConfig;
import io.metersphere.project.dto.environment.processors.EnvRequestScriptProcessor;
import io.metersphere.sdk.constants.MsAssertionCondition;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jorphan.collections.HashTree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * 脚本解析器
 */
public class MsCommonElementConverter extends AbstractJmeterElementConverter<MsCommonElement> {


    @Override
    public void toHashTree(HashTree tree, MsCommonElement element, ParameterConfig config) {
        EnvironmentInfoDTO envInfo = getEnvInfo(element, config);
        // 解析前置处理器，包括环境前置
        addProcessors(tree, element, config, envInfo, true);
        // 解析后置处理器，包括环境后置
        addProcessors(tree, element, config, envInfo, false);
        // 处理断言，包括环境断言
        addAssertion(tree, element, config);
    }

    private EnvironmentInfoDTO getEnvInfo(MsCommonElement element, ParameterConfig config) {
        if (config instanceof ApiParamConfig apiParamConfig) {
            return apiParamConfig.getEnvConfig(element.getProjectId());
        }
        return null;
    }

    /**
     * 添加断言
     * @param tree
     * @param element
     * @param config
     */
    private void addAssertion(HashTree tree, MsCommonElement element, ParameterConfig config) {
        MsAssertionConfig assertionConfig = element.getAssertionConfig();
        List<MsAssertion> assertions = assertionConfig.getAssertions();

        // 添加环境断言
        if (assertionConfig.getEnableGlobal() && config instanceof ApiParamConfig apiParamConfig) {
            EnvironmentInfoDTO envConfig = apiParamConfig.getEnvConfig(element.getProjectId());
            if (envConfig != null) {
                assertions.addAll(envConfig.getConfig().getAssertionConfig().getAssertions());
            }
        }

        assertionConfig.getAssertions()
                .forEach(assertion -> AssertionConverterFactory.getConverter(assertion.getClass()).parse(tree, assertion, config, isIgnoreAssertStatus(assertions)));
    }

    /**
     * 是否忽略状态码
     * @param assertions
     * @return
     */
    public static boolean isIgnoreAssertStatus(List<MsAssertion> assertions) {
        boolean isIgnoreStatus = false;
        for (MsAssertion assertion : assertions) {
            if (assertion instanceof MsResponseCodeAssertion responseCodeAssertion) {
                // 如果状态码断言添加了不校验状态码，则所有断言忽略状态码
                if (StringUtils.equals(responseCodeAssertion.getCondition(), MsAssertionCondition.UNCHECK.name())) {
                    isIgnoreStatus = true;
                }
            }
        }
        return isIgnoreStatus;
    }

    private void addProcessors(HashTree tree, MsCommonElement msCommonElement, ParameterConfig config,
                                    EnvironmentInfoDTO envInfo, boolean isPre) {
        MsProcessorConfig processorConfig = isPre ? msCommonElement.getPreProcessorConfig() : msCommonElement.getPostProcessorConfig();
        if (processorConfig == null || processorConfig.getProcessors() == null) {
            return;
        }
        AbstractMsTestElement parent = msCommonElement.getParent();
        String protocol = null;
        if (parent instanceof MsHTTPElement) {
            protocol = ApiConstants.HTTP_PROTOCOL;
        } else {
            if (config instanceof ApiParamConfig apiParamConfig) {
                protocol = apiParamConfig.getTestElementClassProtocalMap().get(parent.getClass());
            }
        }

        List<MsProcessor> beforeStepProcessors = new ArrayList<>(0);
        List<MsProcessor> afterStepProcessors = new ArrayList<>(0);

        // 开启全局前置才处理环境前置处理器
        if (BooleanUtils.isTrue(processorConfig.getEnableGlobal()) && envInfo != null) {
            EnvironmentConfig envConfig = envInfo.getConfig();
            EnvProcessorConfig envProcessorConfig = isPre ? envConfig.getPreProcessorConfig() : envConfig.getPostProcessorConfig();
            addEnvProcessors(envProcessorConfig, beforeStepProcessors, afterStepProcessors, protocol, true);
        }

        Function<Class<?>, MsProcessorConverter<MsProcessor>> getConverterFunc =
                isPre ? MsProcessorConverterFactory::getPreConverter : MsProcessorConverterFactory::getPostConverter;

        // 处理环境中，步骤前处理器
        beforeStepProcessors.forEach(processor ->
                getConverterFunc.apply(processor.getClass()).parse(tree, processor, config));

        processorConfig.getProcessors()
                .forEach(processor -> getConverterFunc.apply(processor.getClass()).parse(tree, processor, config));

        // 处理环境中，步骤后处理器
        afterStepProcessors.forEach(processor ->
                getConverterFunc.apply(processor.getClass()).parse(tree, processor, config));
    }

    private void addEnvProcessors(EnvProcessorConfig envProcessorConfig,
                                  List<MsProcessor> beforeStepProcessors,
                                  List<MsProcessor> afterStepProcessors,
                                  String protocol,
                                  boolean isPre) {
        // 获取环境中的前后置处理器
        ApiEnvProcessorConfig apiProcessorConfig = envProcessorConfig.getApiProcessorConfig();
        ApiEnvRequestProcessorConfig requestProcessorConfig = apiProcessorConfig.getRequestProcessorConfig();
        List<MsProcessor> processors = requestProcessorConfig.getProcessors();

        for (MsProcessor processor : processors) {
            if (processor instanceof EnvRequestScriptProcessor requestScriptProcessor) {
                // 如果是脚本处理器，处理步骤内前置脚本前后配置，以及忽略的协议
                Boolean beforeStepScript = requestScriptProcessor.getBeforeStepScript();
                List<String> ignoreProtocols = requestScriptProcessor.getIgnoreProtocols();
                if (ignoreProtocols.contains(protocol)) {
                    return;
                }
                if (BooleanUtils.isTrue(beforeStepScript)) {
                    beforeStepProcessors.add(processor);
                } else {
                    afterStepProcessors.add(processor);
                }
            } else {
                // 其他处理器
                if (isPre) {
                    // 如果是前置，则在前置处理器之前执行
                    beforeStepProcessors.add(processor);
                } else {
                    // 如果是后置，则在后置处理器之后执行
                    afterStepProcessors.add(processor);
                }
            }
        }
    }
}
