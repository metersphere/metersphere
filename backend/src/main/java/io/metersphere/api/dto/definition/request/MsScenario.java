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
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.RandomVariableConfig;
import org.apache.jmeter.modifiers.CounterConfig;
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

    private static final String BODY_FILE_DIR = "/opt/metersphere/data/body";

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        if (!this.isEnable()) {
            return;
        }
        config.setStep(this.name);
        config.setStepType("");
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
        if (this.getReferenced() != null && this.getReferenced().equals("Deleted")) {
            return;
        } else if (this.getReferenced() != null && this.getReferenced().equals("REF")) {
            try {
                ApiAutomationService apiAutomationService = CommonBeanFactory.getBean(ApiAutomationService.class);
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                ApiScenarioWithBLOBs scenario = apiAutomationService.getApiScenario(this.getId());
                JSONObject element = JSON.parseObject(scenario.getScenarioDefinition());
                LinkedList<MsTestElement> elements = mapper.readValue(element.getString("hashTree"), new TypeReference<LinkedList<MsTestElement>>() {
                });
                if (hashTree == null) {
                    hashTree = elements;
                } else {
                    hashTree.addAll(elements);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // 场景变量和环境变量
        tree.add(arguments(config));
        addCsvDataSet(tree);
        addCounter(tree);
        addRandom(tree);
        if (CollectionUtils.isNotEmpty(hashTree)) {
            for (MsTestElement el : hashTree) {
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
        }
        if (config != null && config.getConfig() != null && config.getConfig().getCommonConfig() != null
                && CollectionUtils.isNotEmpty(config.getConfig().getCommonConfig().getVariables())) {
            config.getConfig().getCommonConfig().getVariables().stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                    arguments.addArgument(keyValue.getName(), keyValue.getValue(), "=")
            );
        }
        return arguments;
    }

    private void addCsvDataSet(HashTree tree) {
        if (CollectionUtils.isNotEmpty(this.getVariables())) {
            List<ScenarioVariable> list = variables.stream().filter(ScenarioVariable::isCSVValid).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(item -> {
                    CSVDataSet csvDataSet = new CSVDataSet();
                    csvDataSet.setEnabled(true);
                    csvDataSet.setProperty(TestElement.TEST_CLASS, CSVDataSet.class.getName());
                    csvDataSet.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
                    csvDataSet.setName(item.getName());
                    csvDataSet.setProperty("fileEncoding", item.getEncoding());
                    csvDataSet.setProperty("variableNames", item.getName());
                    if (CollectionUtils.isNotEmpty(item.getFiles())) {
                        csvDataSet.setProperty("filename", BODY_FILE_DIR + "/" + item.getFiles().get(0).getId() + "_" + item.getFiles().get(0).getName());
                    }
                    csvDataSet.setIgnoreFirstLine(false);
                    csvDataSet.setProperty("delimiter", item.getDelimiter());
                    csvDataSet.setComment(item.getDescription());
                    tree.add(csvDataSet);
                });
            }
        }
    }

    private void addCounter(HashTree tree) {
        if (CollectionUtils.isNotEmpty(this.getVariables())) {
            List<ScenarioVariable> list = variables.stream().filter(ScenarioVariable::isCounterValid).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(item -> {
                    CounterConfig counterConfig = new CounterConfig();
                    counterConfig.setEnabled(true);
                    counterConfig.setProperty(TestElement.TEST_CLASS, CounterConfig.class.getName());
                    counterConfig.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("CounterConfigGui"));
                    counterConfig.setName(item.getName());
                    counterConfig.setStart(item.getStartNumber());
                    counterConfig.setEnd(item.getEndNumber());
                    counterConfig.setVarName(item.getName());
                    counterConfig.setIncrement(item.getIncrement());
                    counterConfig.setFormat(item.getValue());
                    counterConfig.setComment(item.getDescription());
                    tree.add(counterConfig);
                });
            }
        }
    }

    private void addRandom(HashTree tree) {
        if (CollectionUtils.isNotEmpty(this.getVariables())) {
            List<ScenarioVariable> list = variables.stream().filter(ScenarioVariable::isRandom).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(item -> {
                    RandomVariableConfig randomVariableConfig = new RandomVariableConfig();
                    randomVariableConfig.setEnabled(true);
                    randomVariableConfig.setProperty(TestElement.TEST_CLASS, RandomVariableConfig.class.getName());
                    randomVariableConfig.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
                    randomVariableConfig.setName(item.getName());
                    randomVariableConfig.setProperty("variableName", item.getName());
                    randomVariableConfig.setProperty("outputFormat", item.getValue());
                    randomVariableConfig.setProperty("minimumValue", item.getMinNumber());
                    randomVariableConfig.setProperty("maximumValue", item.getMaxNumber());
                    randomVariableConfig.setComment(item.getDescription());
                    tree.add(randomVariableConfig);
                });
            }
        }
    }

}
