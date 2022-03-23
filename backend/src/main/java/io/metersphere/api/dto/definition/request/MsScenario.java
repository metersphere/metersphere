package io.metersphere.api.dto.definition.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.definition.request.controller.MsCriticalSectionController;
import io.metersphere.api.dto.definition.request.processors.MsJSR223Processor;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.mockconfig.MockConfigStaticData;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.EnvironmentGroupProjectService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "scenario")
public class MsScenario extends MsTestElement {

    private String type = "scenario";
    private String clazzName = MsScenario.class.getCanonicalName();

    @JSONField(ordinal = 21)
    private String referenced;

    @JSONField(ordinal = 22)
    private String environmentId;

    @JSONField(ordinal = 23)
    private List<ScenarioVariable> variables;

    @JSONField(ordinal = 24)
    private boolean enableCookieShare;

    @JSONField(ordinal = 26)
    private List<KeyValue> headers;

    @JSONField(ordinal = 27)
    private Map<String, String> environmentMap;

    @JSONField(ordinal = 28)
    private Boolean onSampleError;

    @JSONField(ordinal = 29)
    private boolean environmentEnable;

    @JSONField(ordinal = 30)
    private Boolean variableEnable;

    private static final String BODY_FILE_DIR = FileUtils.BODY_FILE_DIR;

    public MsScenario() {
    }

    public MsScenario(String name) {
        if (StringUtils.isEmpty(name)) {
            this.setName("Scenario");
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
        Map<String, EnvironmentConfig> envConfig = new HashMap<>(16);
        if (config.getConfig() == null) {
            // 兼容历史数据
            if (this.environmentMap == null || this.environmentMap.isEmpty()) {
                this.environmentMap = new HashMap<>(16);
                if (StringUtils.isNotBlank(environmentId)) {
                    // 兼容1.8之前 没有environmentMap但有environmentId的数据
                    this.environmentMap.put(RunModeConstants.HIS_PRO_ID.toString(), environmentId);
                }
            }
            if (this.environmentMap != null && !this.environmentMap.isEmpty()) {
                this.setEnv(this.environmentMap, envConfig);
                config.setConfig(envConfig);
            }
        } else {
            Map<String, EnvironmentConfig> map = config.getConfig();
            for (EnvironmentConfig evnConfig : map.values()) {
                if (evnConfig.getHttpConfig() != null) {
                    this.setMockEnvironment(evnConfig.getHttpConfig().isMock());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(this.getVariables())) {
            config.setVariables(this.variables);
        }
        HashTree scenarioTree = tree;
        // 取出自身场景环境
        ParameterConfig newConfig = new ParameterConfig();
        if (this.isEnvironmentEnable()) {
            this.setNewConfig(envConfig, newConfig);
        }

        if (config != null && !config.getExcludeScenarioIds().contains(this.getId())) {
            scenarioTree = MsCriticalSectionController.createHashTree(tree, this.getName(), this.isEnable());
        }
        // 环境变量
        Arguments arguments = arguments(this.isEnvironmentEnable() ? newConfig : config);
        if (arguments != null && (this.variableEnable == null || this.variableEnable)) {
            Arguments valueSupposeMock = ParameterConfig.valueSupposeMock(arguments);
            // 这里加入自定义变量解决ForEach循环控制器取值问题，循环控制器无法从vars中取值
            scenarioTree.add(valueSupposeMock);
            if (this.variableEnable != null && this.variableEnable) {
                scenarioTree.add(ElementUtil.argumentsToProcessor(valueSupposeMock));
            }
        }
        if (this.variableEnable == null || this.variableEnable) {
            ElementUtil.addCsvDataSet(scenarioTree, variables, this.isEnvironmentEnable() ? newConfig : config, "shareMode.group");
            ElementUtil.addCounter(scenarioTree, variables, false);
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

    private void setGlobProcessor(ParameterConfig config, HashTree scenarioTree, boolean isPre) {
        if (config.getConfig() != null && (this.variableEnable == null || this.variableEnable)) {
            config.getConfig().forEach((k, environmentConfig) -> {
                if (environmentConfig != null) {
                    MsJSR223Processor processor = isPre ? environmentConfig.getPreStepProcessor() : environmentConfig.getPostStepProcessor();
                    if (processor != null && StringUtils.isNotEmpty(processor.getScript())) {
                        processor.setType("JSR223Processor");
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
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ApiScenarioWithBLOBs scenario = apiAutomationService.selectByPrimaryKey(this.getId());
            if (scenario != null && StringUtils.isNotEmpty(scenario.getScenarioDefinition())) {
                JSONObject element = JSON.parseObject(scenario.getScenarioDefinition());
                // 历史数据处理
                ElementUtil.dataFormatting(element.getJSONArray("hashTree"));
                this.setName(scenario.getName());
                this.setProjectId(scenario.getProjectId());
                LinkedList<MsTestElement> sourceHashTree = mapper.readValue(element.getString("hashTree"), new TypeReference<LinkedList<MsTestElement>>() {
                });
                // 场景变量
                if (StringUtils.isNotEmpty(element.getString("variables")) && (this.variableEnable == null || this.variableEnable)) {
                    LinkedList<ScenarioVariable> variables = mapper.readValue(element.getString("variables"),
                            new TypeReference<LinkedList<ScenarioVariable>>() {
                            });
                    this.setVariables(variables);
                }
                // 场景请求头
                if (StringUtils.isNotEmpty(element.getString("headers")) && (this.variableEnable == null || this.variableEnable)) {
                    LinkedList<KeyValue> headers = mapper.readValue(element.getString("headers"),
                            new TypeReference<LinkedList<KeyValue>>() {
                            });
                    this.setHeaders(headers);
                }
                this.setHashTree(sourceHashTree);
                hashTree.clear();
                hashTree.addAll(sourceHashTree);
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void setNewConfig(Map<String, EnvironmentConfig> envConfig, ParameterConfig newConfig) {
        if (this.isEnvironmentEnable()) {
            ApiScenarioMapper apiScenarioMapper = CommonBeanFactory.getBean(ApiScenarioMapper.class);
            EnvironmentGroupProjectService environmentGroupProjectService = CommonBeanFactory.getBean(EnvironmentGroupProjectService.class);
            ApiScenarioWithBLOBs scenario = apiScenarioMapper.selectByPrimaryKey(this.getId());
            if (scenario != null) {
                String environmentType = scenario.getEnvironmentType();
                String environmentJson = scenario.getEnvironmentJson();
                String environmentGroupId = scenario.getEnvironmentGroupId();
                if (StringUtils.equals(environmentType, EnvironmentType.GROUP.name())) {
                    this.environmentMap = environmentGroupProjectService.getEnvMap(environmentGroupId);
                } else if (StringUtils.equals(environmentType, EnvironmentType.JSON.name())) {
                    this.environmentMap = JSON.parseObject(environmentJson, Map.class);
                }
            } else {
                this.setEnvironmentEnable(false);
            }

            if (this.environmentMap != null && !this.environmentMap.isEmpty()) {
                environmentMap.keySet().forEach(projectId -> {
                    ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
                    ApiTestEnvironmentWithBLOBs environment = environmentService.get(this.environmentMap.get(projectId));
                    if (environment != null && environment.getConfig() != null) {
                        EnvironmentConfig env = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
                        env.setApiEnvironmentid(environment.getId());
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

    public void setOldVariables(List<KeyValue> oldVariables) {
        if (CollectionUtils.isNotEmpty(oldVariables)) {
            String json = JSON.toJSONString(oldVariables);
            this.variables = JSON.parseArray(json, ScenarioVariable.class);
        }
    }

    public void setHeader(HashTree tree, List<KeyValue> headers) {
        if (CollectionUtils.isNotEmpty(headers)) {
            HeaderManager headerManager = new HeaderManager();
            headerManager.setEnabled(true);
            headerManager.setName(this.getName() + "场景Headers");
            headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
            headerManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HeaderPanel"));
            headers.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                    headerManager.add(new Header(keyValue.getName(), keyValue.getValue()))
            );
            if (headerManager.getHeaders().size() > 0) {
                tree.add(headerManager);
            }
        }
    }

    private Arguments arguments(ParameterConfig config) {
        Arguments arguments = new Arguments();
        arguments.setEnabled(true);
        arguments.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() : "Arguments");
        arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
        arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));
        // 场景变量
        if (CollectionUtils.isNotEmpty(this.getVariables())) {
            this.getVariables().stream().filter(ScenarioVariable::isConstantValid).forEach(keyValue ->
                    arguments.addArgument(keyValue.getName(), keyValue.getValue(), "=")
            );

            List<ScenarioVariable> variableList = this.getVariables().stream().filter(ScenarioVariable::isListValid).collect(Collectors.toList());
            variableList.forEach(item -> {
                String[] arrays = item.getValue().split(",");
                for (int i = 0; i < arrays.length; i++) {
                    arguments.addArgument(item.getName() + "_" + (i + 1), arrays[i], "=");
                }
            });
        }
        // 环境通用变量
        if (config.isEffective(this.getProjectId()) && config.getConfig().get(this.getProjectId()).getCommonConfig() != null
                && CollectionUtils.isNotEmpty(config.getConfig().get(this.getProjectId()).getCommonConfig().getVariables())) {
            config.getConfig().get(this.getProjectId()).getCommonConfig().getVariables().stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                    arguments.addArgument(keyValue.getName(), keyValue.getValue(), "=")
            );
            // 清空变量，防止重复添加
            config.getConfig().get(this.getProjectId()).getCommonConfig().getVariables().clear();
        }
        if (arguments.getArguments() != null && arguments.getArguments().size() > 0) {
            return arguments;
        }
        return null;
    }

    private void setEnv(Map<String, String> environmentMap, Map<String, EnvironmentConfig> envConfig) {
        for (String projectId : environmentMap.keySet()) {
            ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
            ApiTestEnvironmentWithBLOBs environment = environmentService.get(environmentMap.get(projectId));
            if (environment != null && StringUtils.isNotEmpty(environment.getConfig())) {
                EnvironmentConfig env = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
                env.setApiEnvironmentid(environment.getId());
                envConfig.put(projectId, env);
                if (StringUtils.equals(environment.getName(), MockConfigStaticData.MOCK_EVN_NAME)) {
                    this.setMockEnvironment(true);
                }
            }
        }
    }
}
