package io.metersphere.api.parser.jmeter;


import io.metersphere.api.constants.ApiScenarioStepRefType;
import io.metersphere.api.dto.ApiScenarioParamConfig;
import io.metersphere.api.dto.request.MsScenario;
import io.metersphere.api.dto.request.processors.MsProcessorConfig;
import io.metersphere.api.dto.scenario.CsvVariable;
import io.metersphere.api.dto.scenario.ScenarioConfig;
import io.metersphere.api.dto.scenario.ScenarioStepConfig;
import io.metersphere.api.dto.scenario.ScenarioVariable;
import io.metersphere.api.parser.jmeter.child.MsCsvChildPreConverter;
import io.metersphere.api.parser.jmeter.constants.JmeterAlias;
import io.metersphere.api.parser.jmeter.constants.JmeterProperty;
import io.metersphere.api.parser.jmeter.processor.MsProcessorConverter;
import io.metersphere.api.parser.jmeter.processor.MsProcessorConverterFactory;
import io.metersphere.api.parser.jmeter.processor.assertion.AssertionConverterFactory;
import io.metersphere.plugin.api.dto.ParameterConfig;
import io.metersphere.plugin.api.spi.AbstractJmeterElementConverter;
import io.metersphere.project.api.assertion.MsAssertion;
import io.metersphere.project.api.assertion.MsResponseCodeAssertion;
import io.metersphere.project.api.processor.MsProcessor;
import io.metersphere.project.api.processor.SQLProcessor;
import io.metersphere.project.dto.environment.EnvironmentConfig;
import io.metersphere.project.dto.environment.EnvironmentInfoDTO;
import io.metersphere.project.dto.environment.processors.EnvProcessorConfig;
import io.metersphere.project.dto.environment.processors.EnvScenarioSqlProcessor;
import io.metersphere.project.dto.environment.variables.CommonVariables;
import io.metersphere.sdk.util.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.control.CriticalSectionController;
import org.apache.jmeter.modifiers.UserParameters;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.metersphere.api.constants.ApiConstants.ASSOCIATE_RESULT_PROCESSOR_PREFIX;
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
        // 获取生效的环境配置
        ApiScenarioParamConfig config = getEnableConfig(msScenario, (ApiScenarioParamConfig) msParameter);
        EnvironmentInfoDTO envInfo = config.getEnvConfig(msScenario.getProjectId());

        if (isRef(msScenario.getRefType())) {
            // 引用的场景中可能包含变量，场景包一层临界控制器，解决变量冲突
            tree = addCriticalSectionController(tree, msScenario);
        }

        if (isRootScenario(msScenario.getRefType()) && msScenario.getScenarioConfig().getOtherConfig().getEnableGlobalCookie()) {
            // 根场景，设置共享cookie
            tree.add(getCookieManager());
        }

        // 添加 csv 数据集
        addCsvDataSet(tree, msScenario.getScenarioConfig());

        // 添加场景和环境变量
        addUserParameters(tree, msScenario, envInfo, config);

        // 添加环境的前置
        addEnvScenarioProcessor(tree, msScenario, config, envInfo, true);
        // 添加场景前置
        addScenarioProcessor(tree, msScenario, config, true);

        // 解析子步骤
        parseChild(tree, msScenario, config);

        // 添加场景后置
        addScenarioProcessor(tree, msScenario, config, false);
        // 添加环境的后置
        addEnvScenarioProcessor(tree, msScenario, config, envInfo, false);

        // 添加场景断言
        addScenarioAssertions(tree, msScenario, config);
    }

    private void addCsvDataSet(HashTree tree, ScenarioConfig scenarioConfig) {
        if (scenarioConfig == null || scenarioConfig.getVariable() == null || CollectionUtils.isEmpty(scenarioConfig.getVariable().getCsvVariables())) {
            return;
        }
        List<CsvVariable> csvVariables = scenarioConfig.getVariable().getCsvVariables()
                .stream()
                .filter(csvVariable -> StringUtils.equals(csvVariable.getScope(), CsvVariable.CsvVariableScope.SCENARIO.name()))
                .toList();
        MsCsvChildPreConverter.addCsvDataSet(tree, JmeterProperty.CSVDataSetProperty.SHARE_MODE_GROUP, csvVariables);
    }

    /**
     * 添加临界控制器，解决变量冲突
     *
     * @param tree
     * @param msScenario
     * @return
     */
    public HashTree addCriticalSectionController(HashTree tree, MsScenario msScenario) {
        String name = msScenario.getName();
        boolean enable = msScenario.getEnable();
        CriticalSectionController criticalSectionController = new CriticalSectionController();
        criticalSectionController.setName(StringUtils.isNotEmpty(name) ? "Csc_" + name : "Scenario Critical Section Controller");
        criticalSectionController.setLockName(UUID.randomUUID().toString());
        criticalSectionController.setEnabled(enable);
        criticalSectionController.setProperty(TestElement.TEST_CLASS, CriticalSectionController.class.getName());
        criticalSectionController.setProperty(TestElement.GUI_CLASS, JmeterAlias.CRITICAL_SECTION_CONTROLLER_GUI);
        return tree.add(criticalSectionController);
    }

    /**
     * 添加场景和环境变量
     *
     * @param tree
     * @param msScenario
     * @param envInfo
     */
    private void addUserParameters(HashTree tree, MsScenario msScenario, EnvironmentInfoDTO envInfo, ApiScenarioParamConfig config) {
        // 如果是根场景获取场景变量
        List<CommonVariables> commonVariables = getCommonVariables(msScenario.getScenarioConfig());

        if (!isRootScenario(msScenario.getRefType())) {
            // 不是根场景，从步骤配置中获取是否使用场景变量
            ScenarioStepConfig scenarioStepConfig = msScenario.getScenarioStepConfig();
            if (scenarioStepConfig != null) {
                if (BooleanUtils.isTrue(scenarioStepConfig.getUseOriginScenarioParam())) {
                    if (BooleanUtils.isNotTrue(scenarioStepConfig.getUseOriginScenarioParamPreferential())) {
                        // 如果不是源场景变量优先，则跟根场景比较，去掉重名的变量
                        Set<String> rootVariableKeys = getCommonVariables(config.getRootScenarioConfig())
                                .stream()
                                .filter(CommonVariables::getEnable)
                                .filter(variable -> variable.getEnable() && (variable.isListValid() ? variable.isListValid() : variable.isConstantValid()))
                                .map(variable -> variable.getParamType() + variable.getKey())
                                .collect(Collectors.toSet());

                        commonVariables = commonVariables.stream().filter(variable -> {
                            if (rootVariableKeys.contains(variable.getParamType() + variable.getKey())) {
                                // 当前场景变量优先，过滤掉当前场景变量中存在的变量
                                return false;
                            }
                            return true;
                        }).collect(Collectors.toList());
                    }
                } else {
                    // 如果没有启用源场景环境，则置空
                    commonVariables = List.of();
                }
            }
        }

        List<CommonVariables> envCommonVariables = List.of();
        if (envInfo != null && needParseEnv(msScenario) && envInfo.getConfig() != null) {
            // 获取环境变量
            envCommonVariables = envInfo.getConfig().getCommonVariables();
            // 获取后，将环境变量置空，避免请求重复设置
            envInfo.getConfig().setCommonVariables(List.of());
        }

        List<CommonVariables> constantVariables = mergeEnvVariables(commonVariables, envCommonVariables, CommonVariables::isConstantValid);
        List<CommonVariables> listVariables = mergeEnvVariables(commonVariables, envCommonVariables, CommonVariables::isListValid);

        if (CollectionUtils.isEmpty(commonVariables) && CollectionUtils.isEmpty(envCommonVariables)) {
            return;
        }

        UserParameters arguments = JmeterTestElementParserHelper.getUserParameters(constantVariables, listVariables);

        tree.add(arguments);
    }

    private List<CommonVariables> getCommonVariables(ScenarioConfig scenarioConfig) {
        ScenarioVariable scenarioVariable = scenarioConfig == null ? new ScenarioVariable() : scenarioConfig.getVariable();
        scenarioVariable = scenarioVariable == null ? new ScenarioVariable() : scenarioVariable;
        List<CommonVariables> commonVariables = scenarioVariable.getCommonVariables();
        return commonVariables;
    }

    /**
     * 合并环境变量和场景变量
     *
     * @param scenarioVariables
     * @param envCommonVariables
     * @param filter
     * @return
     */
    private List<CommonVariables> mergeEnvVariables(List<CommonVariables> scenarioVariables, List<CommonVariables> envCommonVariables, Predicate<CommonVariables> filter) {
        List<CommonVariables> variables = scenarioVariables
                .stream()
                .filter(CommonVariables::getEnable)
                .filter(filter::test)
                .collect(Collectors.toList());

        List<CommonVariables> envConstantVariables = envCommonVariables
                .stream()
                .filter(CommonVariables::getEnable)
                .filter(filter::test)
                .collect(Collectors.toList());

        Map<String, String> scenarioVariableMap = variables
                .stream()
                .collect(Collectors.toMap(CommonVariables::getKey, CommonVariables::getValue));

        for (CommonVariables globalConstantVariable : envConstantVariables) {
            String key = globalConstantVariable.getKey();
            if (!scenarioVariableMap.containsKey(key)) {
                variables.add(globalConstantVariable);
            }
        }
        return variables;
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

        for (int i = 0; i < assertions.size(); i++) {
            MsAssertion assertion = assertions.get(i);

            // 只给第一个响应码断言设置忽略状态
            boolean isIgnoreStatus = i == 0 && assertion instanceof MsResponseCodeAssertion;

            AssertionConverterFactory.getConverter(assertion.getClass()).parse(tree, assertion, config, isIgnoreStatus);
        }
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

        if (!needParseEnv(msScenario)) {
            return;
        }

        ScenarioConfig scenarioConfig = msScenario.getScenarioConfig();
        MsProcessorConfig scenarioProcessorConfig = isPre ? scenarioConfig.getPreProcessorConfig() : scenarioConfig.getPostProcessorConfig();
        if (scenarioProcessorConfig == null || BooleanUtils.isFalse(scenarioProcessorConfig.getEnableGlobal()) || envInfo == null) {
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

        // 处理环境场景级别的SQL处理器
        for (int i = 0; i < envScenarioProcessors.size(); i++) {
            MsProcessor msProcessor = envScenarioProcessors.get(i);
            if (msProcessor instanceof SQLProcessor) {
                EnvScenarioSqlProcessor envScenarioSqlProcessor = BeanUtils.copyBean(new EnvScenarioSqlProcessor(), msProcessor);
                envScenarioSqlProcessor.setName(ASSOCIATE_RESULT_PROCESSOR_PREFIX + false);
                envScenarioProcessors.set(i, envScenarioSqlProcessor);
            }
        }

        Function<Class<?>, MsProcessorConverter<MsProcessor>> getConverterFunc =
                isPre ? MsProcessorConverterFactory::getPreConverter : MsProcessorConverterFactory::getPostConverter;

        // 添加前后置
        envScenarioProcessors.stream()
                .filter(MsProcessor::getEnable)
                .forEach(processor -> {
                    processor.setProjectId(msScenario.getProjectId());
                    getConverterFunc.apply(processor.getClass()).parse(tree, processor, config);
                });
    }

    /**
     * 是否需要解析环境
     *
     * @param msScenario
     * @return
     */
    private boolean needParseEnv(MsScenario msScenario) {
        if (isRef(msScenario.getRefType())) {
            ScenarioStepConfig scenarioStepConfig = msScenario.getScenarioStepConfig();
            if (scenarioStepConfig == null || BooleanUtils.isFalse(scenarioStepConfig.getEnableScenarioEnv())) {
                // 引用的场景，如果没有开启源场景环境，不解析环境
                return false;
            }
            return true;
        } else if (isCopy(msScenario.getRefType())) {
            // 复制场景，不解析环境
            return false;
        } else {
            // 当前场景，解析环境
            return true;
        }
    }

    private void addScenarioProcessor(HashTree tree, MsScenario msScenario, ParameterConfig config, boolean isPre) {
        // 获取场景前后置
        ScenarioConfig scenarioConfig = msScenario.getScenarioConfig();
        if (isCopy(msScenario.getRefType()) || scenarioConfig == null) {
            // 复制的场景，没有前后置
            return;
        }
        MsProcessorConfig processorConfig = isPre ? scenarioConfig.getPreProcessorConfig() : scenarioConfig.getPostProcessorConfig();

        if (processorConfig == null || CollectionUtils.isEmpty(processorConfig.getProcessors())) {
            return;
        }
        List<MsProcessor> scenarioPreProcessors = processorConfig.getProcessors();

        Function<Class<?>, MsProcessorConverter<MsProcessor>> getConverterFunc =
                isPre ? MsProcessorConverterFactory::getPreConverter : MsProcessorConverterFactory::getPostConverter;

        // 添加场景前置处理器
        scenarioPreProcessors.stream()
                .filter(MsProcessor::getEnable)
                .forEach(processor -> {
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
     * 获取生效的环境
     * 如果使用源场景环境，则使用源场景的环境信息
     *
     * @param msScenario
     * @param config
     * @return
     */
    private ApiScenarioParamConfig getEnableConfig(MsScenario msScenario, ApiScenarioParamConfig config) {
        ApiScenarioParamConfig enableConfig = config;
        if (!isRef(msScenario.getRefType())) {
            if (isRootScenario(msScenario.getRefType())) {
                setScenarioConfig(msScenario, enableConfig);
            }
            // 非引用的场景，使用当前环境参数
            return enableConfig;
        }
        ScenarioStepConfig scenarioStepConfig = msScenario.getScenarioStepConfig();
        if (scenarioStepConfig != null && BooleanUtils.isTrue(scenarioStepConfig.getEnableScenarioEnv())) {
            // 使用源场景环境
            enableConfig = BeanUtils.copyBean(new ApiScenarioParamConfig(), config);
            enableConfig.setGrouped(msScenario.getGrouped());
            // 清空环境信息
            enableConfig.setEnvConfig(null);
            enableConfig.setProjectEnvMap(null);
            if (BooleanUtils.isTrue(msScenario.getGrouped())) {
                // 环境组设置环境Map
                Map<String, EnvironmentInfoDTO> projectEnvMap = msScenario.getProjectEnvMap();
                enableConfig.setProjectEnvMap(projectEnvMap);
            } else {
                // 设置环境信息
                EnvironmentInfoDTO environmentInfo = msScenario.getEnvironmentInfo();
                enableConfig.setEnvConfig(environmentInfo);
            }
        }

        setScenarioConfig(msScenario, enableConfig);

        return enableConfig;
    }

    private void setScenarioConfig(MsScenario msScenario, ApiScenarioParamConfig enableConfig) {
        ScenarioConfig scenarioConfig = msScenario.getScenarioConfig();
        if (scenarioConfig != null) {
            // 设置是否使用全局cookie
            enableConfig.setEnableGlobalCookie(scenarioConfig.getOtherConfig().getEnableCookieShare());

            ScenarioVariable variable = scenarioConfig.getVariable();
            if (variable != null && variable.getCsvVariables() != null) {
                for (CsvVariable csvVariable : variable.getCsvVariables()) {
                    enableConfig.getCsvMap().put(csvVariable.getId(), csvVariable);
                }
            }
        }
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
