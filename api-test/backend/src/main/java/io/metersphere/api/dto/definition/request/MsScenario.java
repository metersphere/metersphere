package io.metersphere.api.dto.definition.request;

import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.definition.request.controller.MsCriticalSectionController;
import io.metersphere.api.dto.definition.request.processors.MsJSR223Processor;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.mock.config.MockConfigStaticData;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.scenario.environment.item.EnvJSR223Processor;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.utils.*;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.utils.LoggerUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class MsScenario extends MsTestElement {
    private String type = ElementConstants.SCENARIO;
    private String clazzName = MsScenario.class.getCanonicalName();
    private String referenced;
    private String environmentId;
    private List<ScenarioVariable> variables;
    private boolean enableCookieShare;
    private List<KeyValue> headers;
    private Map<String, String> environmentMap;
    private Boolean onSampleError;
    private boolean environmentEnable;
    private Boolean variableEnable;
    private static final String BODY_FILE_DIR = FileUtils.BODY_FILE_DIR;
    private Boolean mixEnable;

    public MsScenario() {
    }

    public MsScenario(String name) {
        if (StringUtils.isEmpty(name)) {
            this.setName(ElementConstants.SCENARIO);
        }
        this.setName(name);
    }

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, MsParameter msParameter) {
        ParameterConfig config = (ParameterConfig) msParameter;
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        if (this.getReferenced() != null && this.getReferenced().equals(MsTestElementConstants.Deleted.name())) {
            return;
        } else if (this.getReferenced() != null && MsTestElementConstants.REF.name().equals(this.getReferenced())
                && !this.setRefScenario(hashTree)) {
            return;
        }

        // 设置共享cookie
        config.setEnableCookieShare(enableCookieShare);
        if (MapUtils.isNotEmpty(config.getConfig())) {
            Map<String, EnvironmentConfig> map = config.getConfig();
            for (EnvironmentConfig evnConfig : map.values()) {
                if (evnConfig.getHttpConfig() != null) {
                    this.setMockEnvironment(evnConfig.getHttpConfig().isMock());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(this.getVariables()) && (this.variableEnable == null || this.variableEnable)) {
            config.setVariables(this.variables);
        }
        HashTree scenarioTree = tree;
        // 取出自身场景环境
        ParameterConfig newConfig = new ParameterConfig(this.getProjectId(), false);
        if (this.isEnvironmentEnable()) {
            this.setNewConfig(newConfig);
            newConfig.setRetryNum(config.getRetryNum());
        }
        if (StringUtils.equals(this.getId(), config.getScenarioId())) {
            config.setTransferVariables(this.variables);
            if (CollectionUtils.isNotEmpty(this.headers)) {
                ElementUtil.setHeader(scenarioTree, this.headers, this.getName());
            }
        }
        if (!config.getExcludeScenarioIds().contains(this.getId())) {
            scenarioTree = MsCriticalSectionController.createHashTree(tree, this.getName(), this.isEnable());
        }
        // 启用当前场景变量优先选择
        if (isMixEnable() || isAllEnable()) {
            config.margeVariables(this.variables, config.getTransferVariables());
        }
        // 环境变量
        Arguments arguments = ElementUtil.getConfigArguments(this.isEnvironmentEnable() ?
                newConfig : config, this.getName(), this.getProjectId(), this.getVariables());
        if (arguments != null && !arguments.getArguments().isEmpty()) {
            Arguments valueSupposeMock = ParameterConfig.valueSupposeMock(arguments);
            // 这里加入自定义变量解决ForEach循环控制器取值问题，循环控制器无法从vars中取值
            if (BooleanUtils.isTrue(this.variableEnable) || BooleanUtils.isTrue(this.mixEnable)) {
                scenarioTree.add(ElementUtil.argumentsToUserParameters(valueSupposeMock));
            } else if (this.isAllEnable() || config.isApi()) {
                valueSupposeMock.setProperty(ElementConstants.COVER, true);
                scenarioTree.add(valueSupposeMock);
            }
        }
        if ((this.variableEnable == null && this.mixEnable == null)
                || BooleanUtils.isTrue(this.variableEnable) || BooleanUtils.isTrue(this.mixEnable)) {
            ElementUtil.addCsvDataSet(scenarioTree, variables, this.isEnvironmentEnable() ? newConfig : config, "shareMode.group");
            ElementUtil.addCounter(scenarioTree, variables);
            ElementUtil.addRandom(scenarioTree, variables);
            if (CollectionUtils.isNotEmpty(this.headers)) {
                if (this.isEnvironmentEnable()) {
                    newConfig.setHeaders(this.headers);
                } else {
                    config.setHeaders(this.headers);
                }
            }
        }
        // 添加全局前置
        this.setGlobProcessor(this.isEnvironmentEnable() ? newConfig : config, scenarioTree, true);
        if (CollectionUtils.isNotEmpty(hashTree)) {
            for (MsTestElement el : hashTree) {
                if (el != null) {
                    el.setParent(this);
                    el.setMockEnvironment(this.isMockEnvironment());
                    if (this.isEnvironmentEnable()) {
                        newConfig.setScenarioId(config.getScenarioId());
                        newConfig.setReportType(config.getReportType());
                        el.toHashTree(scenarioTree, el.getHashTree(), newConfig);
                    } else {
                        el.toHashTree(scenarioTree, el.getHashTree(), config);
                    }
                }
            }
        }
        // 添加全局后置
        this.setGlobProcessor(this.isEnvironmentEnable() ? newConfig : config, scenarioTree, false);
    }

    private boolean isMixEnable() {
        return (mixEnable == null || BooleanUtils.isTrue(mixEnable))
                && (this.variableEnable == null || BooleanUtils.isFalse(this.variableEnable));
    }

    private boolean isAllEnable() {
        return (this.variableEnable == null || BooleanUtils.isFalse(this.variableEnable))
                && (this.mixEnable == null || BooleanUtils.isFalse(this.mixEnable));
    }

    private void setGlobProcessor(ParameterConfig config, HashTree scenarioTree, boolean isPre) {
        if (config.getConfig() != null && (this.variableEnable == null || this.variableEnable)) {
            config.getConfig().forEach((k, environmentConfig) -> {
                if (environmentConfig != null) {
                    EnvJSR223Processor envProcessor = isPre ? environmentConfig.getPreStepProcessor() : environmentConfig.getPostStepProcessor();
                    MsJSR223Processor processor = new MsJSR223Processor();
                    if (envProcessor != null) {
                        BeanUtils.copyBean(processor, envProcessor);
                    }
                    if (StringUtils.isNotEmpty(processor.getScript())) {
                        processor.setType(ElementConstants.JSR223);
                        processor.setClazzName(MsJSR223Processor.class.getCanonicalName());
                        boolean isConnScenarioPre = false;
                        if (environmentConfig.getGlobalScriptConfig() != null) {
                            isConnScenarioPre = isPre ? environmentConfig.getGlobalScriptConfig().isConnScenarioPreScript() :
                                    environmentConfig.getGlobalScriptConfig().isConnScenarioPostScript();
                        }
                        String name = isPre ? "PRE_PROCESSOR_ENV_" : "POST_PROCESSOR_ENV_";
                        processor.setName(name + isConnScenarioPre);
                        processor.toHashTree(scenarioTree, processor.getHashTree(), config);
                    }
                }
            });
        }
    }

    private boolean setRefScenario(List<MsTestElement> hashTree) {
        try {
            ApiScenarioMapper apiAutomationService = CommonBeanFactory.getBean(ApiScenarioMapper.class);
            ApiScenarioWithBLOBs scenario = apiAutomationService.selectByPrimaryKey(this.getId());
            if (scenario != null && StringUtils.isNotEmpty(scenario.getScenarioDefinition())) {
                JSONObject element = JSONUtil.parseObject(scenario.getScenarioDefinition());
                // 历史数据处理
                ElementUtil.dataFormatting(element.optJSONArray(ElementConstants.HASH_TREE));
                this.setName(scenario.getName());
                this.setProjectId(scenario.getProjectId());
                LinkedList<MsTestElement> sourceHashTree = JSONUtil.readValue(element.optString(ElementConstants.HASH_TREE));
                // 场景变量
                if (StringUtils.isNotEmpty(element.optString("variables")) && (this.variableEnable == null || this.variableEnable)) {
                    this.setVariables(JSONUtil.parseArray(element.optString("variables"), ScenarioVariable.class));
                }
                // 场景请求头
                if (StringUtils.isNotEmpty(element.optString("headers")) && (this.variableEnable == null || this.variableEnable)) {
                    this.setHeaders(JSONUtil.parseArray(element.optString("headers"), KeyValue.class));
                }
                this.setHashTree(sourceHashTree);
                hashTree.clear();
                hashTree.addAll(sourceHashTree);
                return true;
            }
        } catch (Exception ex) {
            LoggerUtil.error(ex);
        }
        return false;
    }

    private void setNewConfig( ParameterConfig newConfig) {
        Map<String, EnvironmentConfig> envConfig = new HashMap<>();
        if (this.isEnvironmentEnable()) {
            ApiScenarioMapper apiScenarioMapper = CommonBeanFactory.getBean(ApiScenarioMapper.class);
            BaseEnvGroupProjectService environmentGroupProjectService = CommonBeanFactory.getBean(BaseEnvGroupProjectService.class);
            ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(this.getId());
            if (scenario != null) {
                String environmentType = scenario.getEnvironmentType();
                String environmentJson = scenario.getEnvironmentJson();
                String environmentGroupId = scenario.getEnvironmentGroupId();
                if (StringUtils.equals(environmentType, EnvironmentType.GROUP.name())) {
                    this.environmentMap = environmentGroupProjectService.getEnvMap(environmentGroupId);
                } else if (StringUtils.isNotEmpty(environmentJson) && StringUtils.equals(environmentType, EnvironmentType.JSON.name())) {
                    this.environmentMap = JSON.parseObject(environmentJson, Map.class);
                }
            } else {
                this.setEnvironmentEnable(false);
            }
            if (this.environmentMap != null && !this.environmentMap.isEmpty()) {
                environmentMap.keySet().forEach(projectId -> {
                    BaseEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(BaseEnvironmentService.class);
                    ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentService.get(this.environmentMap.get(projectId));
                    if (environment != null && environment.getConfig() != null) {
                        EnvironmentConfig env = JSONUtil.parseObject(environment.getConfig(), EnvironmentConfig.class);
                        env.setEnvironmentId(environment.getId());
                        envConfig.put(projectId, env);
                        if (StringUtils.equals(environment.getName(), MockConfigStaticData.MOCK_EVN_NAME)) {
                            this.setMockEnvironment(true);
                        }
                    }
                });
                newConfig.setConfig(envConfig);
            }
        }
    }
}
