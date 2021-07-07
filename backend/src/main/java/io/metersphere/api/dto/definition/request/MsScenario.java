package io.metersphere.api.dto.definition.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.mockconfig.MockConfigStaticData;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.constants.RunModeConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.FileUtils;
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

    @JSONField(ordinal = 24)
    private Boolean onSampleError;

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
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        // 非导出操作，且不是启用状态则跳过执行
        if (!config.isOperating() && !this.isEnable()) {
            return;
        }
        if (this.getReferenced() != null && this.getReferenced().equals(MsTestElementConstants.Deleted.name())) {
            return;
        } else if (this.getReferenced() != null && MsTestElementConstants.REF.name().equals(this.getReferenced())) {
            try {
                ApiAutomationService apiAutomationService = CommonBeanFactory.getBean(ApiAutomationService.class);
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                ApiScenarioWithBLOBs scenario = apiAutomationService.getApiScenario(this.getId());
                if (scenario != null && StringUtils.isNotEmpty(scenario.getScenarioDefinition())) {
                    JSONObject element = JSON.parseObject(scenario.getScenarioDefinition());
                    this.setName(scenario.getName());
                    this.setProjectId(scenario.getProjectId());
                    hashTree = mapper.readValue(element.getString("hashTree"), new TypeReference<LinkedList<MsTestElement>>() {
                    });
                    // 场景变量
                    if (StringUtils.isNotEmpty(element.getString("variables"))) {
                        LinkedList<ScenarioVariable> variables = mapper.readValue(element.getString("variables"),
                                new TypeReference<LinkedList<ScenarioVariable>>() {
                                });
                        this.setVariables(variables);
                    }
                    // 场景请求头
                    if (StringUtils.isNotEmpty(element.getString("headers"))) {
                        LinkedList<KeyValue> headers = mapper.readValue(element.getString("headers"),
                                new TypeReference<LinkedList<KeyValue>>() {
                                });
                        this.setHeaders(headers);
                    }

                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
                this.environmentMap.keySet().forEach(projectId -> {
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
                config.setConfig(envConfig);
            }
        } else {
            Map<String, EnvironmentConfig> map = config.getConfig();
            for (EnvironmentConfig evnConfig :
                    map.values()) {
                if (evnConfig.getHttpConfig() != null) {
                    this.setMockEnvironment(evnConfig.getHttpConfig().isMock());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(this.getVariables())) {
            config.setVariables(this.variables);
        }
        // 场景变量和环境变量
        Arguments arguments = arguments(config);
        if (arguments != null) {
            tree.add(ParameterConfig.valueSupposeMock(arguments));
        }
        this.addCsvDataSet(tree, variables, config, "shareMode.group");
        this.addCounter(tree, variables);
        this.addRandom(tree, variables);
        if (CollectionUtils.isNotEmpty(this.headers)) {
            setHeader(tree, this.headers);
        }
        if (CollectionUtils.isNotEmpty(hashTree)) {
            for (MsTestElement el : hashTree) {
                // 给所有孩子加一个父亲标志
                el.setParent(this);
                el.setMockEnvironment(this.isMockEnvironment());
                el.toHashTree(tree, el.getHashTree(), config);
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


}
