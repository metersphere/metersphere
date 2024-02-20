package io.metersphere.api.parser.jmeter;


import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.api.dto.ApiScenarioParamConfig;
import io.metersphere.api.dto.request.MsScenario;
import io.metersphere.api.dto.request.processors.MsProcessorConfig;
import io.metersphere.api.dto.scenario.ScenarioConfig;
import io.metersphere.api.dto.scenario.ScenarioStepConfig;
import io.metersphere.api.parser.jmeter.processor.MsProcessorConverter;
import io.metersphere.api.parser.jmeter.processor.MsProcessorConverterFactory;
import io.metersphere.api.parser.jmeter.processor.assertion.AssertionConverterFactory;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.project.api.assertion.MsAssertion;
import io.metersphere.project.api.processor.MsProcessor;
import io.metersphere.project.dto.environment.EnvironmentConfig;
import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import io.metersphere.project.dto.environment.processors.EnvProcessorConfig;
import io.metersphere.sdk.util.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.metersphere.api.parser.jmeter.constants.JmeterAlias.COOKIE_PANEL;

/**
 * @Author: jianxing
 * @CreateTime: 2023-10-27  10:07
 * <p>
 * 脚本解析器
 */
public class MsScenarioConverter extends AbstractJmeterElementConverter<MsScenario> {

    @Override
    public void toHashTree(HashTree tree, MsScenario msScenario, ParameterConfig msParameter) {
        ApiScenarioParamConfig config = (ApiScenarioParamConfig) msParameter;
        EnvironmentInfoDTO envInfo = config.getEnvConfig(msScenario.getProjectId());

        if (isRootScenario(msScenario.getRefType()) && msScenario.getScenarioConfig().getOtherConfig().getEnableGlobalCookie()) {
            // 根场景，设置共享cookie
            tree.add(getCookieManager());
        }

        // 添加环境的前置
        addEnvScenarioProcessor(tree, msScenario, config, envInfo, true);
        // 添加场景前置
        addScenarioProcessor(tree, msScenario, config, true);

        // 解析子步骤
        ApiScenarioParamConfig chileConfig = getChileConfig(msScenario, config);
        parseChild(tree, msScenario, chileConfig);

        // 添加场景后置
        addScenarioProcessor(tree, msScenario, config, false);
        // 添加环境的后置
        addEnvScenarioProcessor(tree, msScenario, config, envInfo, false);

        // 添加场景断言
        addScenarioAssertions(tree, msScenario, config);
    }

    /**
     * 添加场景断言
     *
     * @param tree
     * @param msScenario
     * @param config
     */
    private void addScenarioAssertions(HashTree tree, MsScenario msScenario, ApiScenarioParamConfig config) {
        ScenarioConfig scenarioConfig = msScenario.getScenarioConfig();
        if (scenarioConfig == null) {
            return;
        }
        List<MsAssertion> assertions = scenarioConfig
                .getAssertionConfig()
                .getAssertions();

        boolean ignoreAssertStatus = MsCommonElementConverter.isIgnoreAssertStatus(assertions);

        assertions.forEach(assertion ->
                AssertionConverterFactory.getConverter(assertion.getClass()).parse(tree, assertion, config, ignoreAssertStatus));
    }

    /**
     * 添加环境的前后置
     *
     * @param tree
     * @param msScenario
     * @param config
     * @param envInfo
     * @param isPre
     */
    private void addEnvScenarioProcessor(HashTree tree,
                                         MsScenario msScenario,
                                         ApiScenarioParamConfig config,
                                         EnvironmentInfoDTO envInfo,
                                         boolean isPre) {

        if (isRef(msScenario.getRefType())) {
            ScenarioStepConfig scenarioStepConfig = msScenario.getScenarioStepConfig();
            if (scenarioStepConfig == null || BooleanUtils.isFalse(scenarioStepConfig.getEnableScenarioEnv())) {
                // 引用的场景，如果没有开启源场景环境，不添加环境的前后置
                return;
            }
        } else if (isCopy(msScenario.getRefType())) {
            // 复制场景，不添加环境的前后置
            return;
        } else {
            // 当前场景，添加环境的前后置
            // do nothing
        }

        ScenarioConfig scenarioConfig = msScenario.getScenarioConfig();
        MsProcessorConfig scenarioProcessorConfig = isPre ? scenarioConfig.getPreProcessorConfig() : scenarioConfig.getPostProcessorConfig();
        Boolean enableGlobal = scenarioProcessorConfig.getEnableGlobal();
        if (BooleanUtils.isFalse(enableGlobal) || envInfo == null) {
            // 如果场景配置没有开启全局前置，不添加环境的前后置
            return;
        }

        // 获取环境中的场景级前后置
        EnvironmentConfig envConfig = envInfo.getConfig();
        EnvProcessorConfig processorConfig = isPre ? envConfig.getPreProcessorConfig() : envConfig.getPostProcessorConfig();
        List<MsProcessor> envScenarioProcessors = processorConfig.getApiProcessorConfig()
                .getScenarioProcessorConfig()
                .getProcessors();

        if (CollectionUtils.isEmpty(envScenarioProcessors)) {
            return;
        }

        Function<Class<?>, MsProcessorConverter<MsProcessor>> getConverterFunc =
                isPre ? MsProcessorConverterFactory::getPreConverter : MsProcessorConverterFactory::getPostConverter;

        // 添加前后置
        envScenarioProcessors.forEach(processor -> {
            processor.setProjectId(msScenario.getProjectId());
            getConverterFunc.apply(processor.getClass()).parse(tree, processor, config);
        });
    }

    private void addScenarioProcessor(HashTree tree, MsScenario msScenario, ParameterConfig config, boolean isPre) {
        if (isCopy(msScenario.getRefType())) {
            // 复制的场景，没有前后置
            return;
        }
        // 获取场景前后置
        ScenarioConfig scenarioConfig = msScenario.getScenarioConfig();
        MsProcessorConfig processorConfig = isPre ? scenarioConfig.getPreProcessorConfig() : scenarioConfig.getPostProcessorConfig();
        List<MsProcessor> scenarioPreProcessors = processorConfig.getProcessors();

        if (CollectionUtils.isEmpty(scenarioPreProcessors)) {
            return;
        }

        Function<Class<?>, MsProcessorConverter<MsProcessor>> getConverterFunc =
                isPre ? MsProcessorConverterFactory::getPreConverter : MsProcessorConverterFactory::getPostConverter;

        // 添加场景前置处理器
        scenarioPreProcessors.forEach(processor -> {
            processor.setProjectId(msScenario.getProjectId());
            getConverterFunc.apply(processor.getClass()).parse(tree, processor, config);
        });
    }

    private boolean isRef(String refType) {
        return StringUtils.equalsAny(refType, ApiScenarioStepRefType.REF.name(), ApiScenarioStepRefType.PARTIAL_REF.name());
    }

    private boolean isRootScenario(String refType) {
        return StringUtils.equals(refType, ApiScenarioStepRefType.DIRECT.name());
    }

    private boolean isCopy(String refType) {
        return StringUtils.equals(refType, ApiScenarioStepRefType.COPY.name());
    }

    /**
     * 获取子步骤的配置信息
     * 如果使用源场景环境，则使用当前场景的环境信息
     *
     * @param msScenario
     * @param config
     * @return
     */
    private ApiScenarioParamConfig getChileConfig(MsScenario msScenario, ApiScenarioParamConfig config) {
        ApiScenarioParamConfig childConfig = config;
        if (!isRef(msScenario.getRefType())) {
            // 非引用的场景，使用当前环境参数
            return childConfig;
        }
        ScenarioStepConfig scenarioStepConfig = msScenario.getScenarioStepConfig();
        if (scenarioStepConfig != null && BooleanUtils.isTrue(scenarioStepConfig.getEnableScenarioEnv())) {
            // 使用源场景环境
            childConfig = BeanUtils.copyBean(new ApiScenarioParamConfig(), config);
            childConfig.setGrouped(msScenario.getGrouped());
            // 清空环境信息
            childConfig.setEnvConfig(null);
            childConfig.setProjectEnvMap(null);
            if (BooleanUtils.isTrue(msScenario.getGrouped())) {
                // 环境组设置环境Map
                Map<String, EnvironmentInfoDTO> projectEnvMap = msScenario.getProjectEnvMap();
                childConfig.setProjectEnvMap(projectEnvMap);
            } else {
                // 设置环境信息
                EnvironmentInfoDTO environmentInfo = msScenario.getEnvironmentInfo();
                childConfig.setEnvConfig(environmentInfo);
            }
        }

        ScenarioConfig scenarioConfig = msScenario.getScenarioConfig();
        if (scenarioConfig != null) {
            // 设置是否使用全局cookie
            childConfig.setEnableGlobalCookie(scenarioConfig.getOtherConfig().getEnableCookieShare());
        }

        return childConfig;
    }

    private CookieManager getCookieManager() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setProperty(TestElement.TEST_CLASS, CookieManager.class.getName());
        cookieManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(COOKIE_PANEL));
        cookieManager.setEnabled(true);
        cookieManager.setName("CookieManager");
        cookieManager.setClearEachIteration(false);
        cookieManager.setControlledByThread(false);
        return cookieManager;
    }
}
