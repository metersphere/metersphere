package io.metersphere.api.dto.definition.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.utils.CommonBeanFactory;
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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "scenario")
public class MsScenario extends MsTestElement {

    private String type = "scenario";
    @JSONField(ordinal = 20)
    private String name;

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

    private static final String BODY_FILE_DIR = "/opt/metersphere/data/body";

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        if (!this.isEnable()) {
            return;
        }
        if (this.getReferenced() != null && this.getReferenced().equals("Deleted")) {
            return;
        } else if (this.getReferenced() != null && this.getReferenced().equals("REF")) {
            try {
                ApiAutomationService apiAutomationService = CommonBeanFactory.getBean(ApiAutomationService.class);
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                ApiScenarioWithBLOBs scenario = apiAutomationService.getApiScenario(this.getId());
                JSONObject element = JSON.parseObject(scenario.getScenarioDefinition());
                hashTree = mapper.readValue(element.getString("hashTree"), new TypeReference<LinkedList<MsTestElement>>() {
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        config.setStep(this.name);
        config.setStepType("SCENARIO");
        config.setEnableCookieShare(enableCookieShare);
        if (StringUtils.isNotEmpty(environmentId)) {
            ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
            ApiTestEnvironmentWithBLOBs environment = environmentService.get(environmentId);
            if (environment != null && environment.getConfig() != null) {
                config.setConfig(JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class));
            }
        }
        if (CollectionUtils.isNotEmpty(this.getVariables())) {
            config.setVariables(this.variables);
        }
        // 场景变量和环境变量
        tree.add(arguments(config));
        this.addCsvDataSet(tree, variables);
        this.addCounter(tree, variables);
        this.addRandom(tree, variables);
        if (CollectionUtils.isNotEmpty(hashTree)) {
            for (MsTestElement el : hashTree) {
                // 给所有孩子加一个父亲标志
                el.setParent(this);
                el.toHashTree(tree, el.getHashTree(), config);
            }
        }
        if (CollectionUtils.isNotEmpty(this.headers)) {
            setHeader(tree, this.headers);
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
            tree.add(headerManager);
        }
    }

    private Arguments arguments(ParameterConfig config) {
        Arguments arguments = new Arguments();
        arguments.setEnabled(true);
        arguments.setName(name + "Variables");
        arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
        arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));
        if (CollectionUtils.isNotEmpty(this.getVariables())) {
            variables.stream().filter(ScenarioVariable::isConstantValid).forEach(keyValue ->
                    arguments.addArgument(keyValue.getName(), keyValue.getValue(), "=")
            );

            List<ScenarioVariable> variableList = variables.stream().filter(ScenarioVariable::isListValid).collect(Collectors.toList());
            variableList.forEach(item -> {
                String[] arrays = item.getValue().split(",");
                for (int i = 0; i < arrays.length; i++) {
                    arguments.addArgument(item.getName() + "_" + (i + 1), arrays[i], "=");
                }
            });
        }
        if (config != null && config.getConfig() != null && config.getConfig().getCommonConfig() != null
                && CollectionUtils.isNotEmpty(config.getConfig().getCommonConfig().getVariables())) {
            config.getConfig().getCommonConfig().getVariables().stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                    arguments.addArgument(keyValue.getName(), keyValue.getValue(), "=")
            );
        }

        return arguments;
    }


}
