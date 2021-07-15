package io.metersphere.api.dto.definition.request;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.metersphere.api.dto.definition.request.assertions.MsAssertions;
import io.metersphere.api.dto.definition.request.auth.MsAuthManager;
import io.metersphere.api.dto.definition.request.configurations.MsHeaderManager;
import io.metersphere.api.dto.definition.request.controller.MsIfController;
import io.metersphere.api.dto.definition.request.controller.MsLoopController;
import io.metersphere.api.dto.definition.request.controller.MsTransactionController;
import io.metersphere.api.dto.definition.request.extract.MsExtract;
import io.metersphere.api.dto.definition.request.processors.MsJSR223Processor;
import io.metersphere.api.dto.definition.request.processors.post.MsJDBCPostProcessor;
import io.metersphere.api.dto.definition.request.processors.post.MsJSR223PostProcessor;
import io.metersphere.api.dto.definition.request.processors.pre.MsJDBCPreProcessor;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.definition.request.sampler.MsDubboSampler;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.dto.definition.request.timer.MsConstantTimer;
import io.metersphere.api.dto.definition.request.unknown.MsJmeterElement;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.mockconfig.MockConfigStaticData;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.commons.constants.DelimiterConstants;
import io.metersphere.commons.constants.LoopConstants;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.RandomVariableConfig;
import org.apache.jmeter.modifiers.CounterConfig;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MsHTTPSamplerProxy.class, name = "HTTPSamplerProxy"),
        @JsonSubTypes.Type(value = MsHeaderManager.class, name = "HeaderManager"),
        @JsonSubTypes.Type(value = MsJSR223Processor.class, name = "JSR223Processor"),
        @JsonSubTypes.Type(value = MsJSR223PostProcessor.class, name = "JSR223PostProcessor"),
        @JsonSubTypes.Type(value = MsJSR223PreProcessor.class, name = "JSR223PreProcessor"),
        @JsonSubTypes.Type(value = MsJDBCPreProcessor.class, name = "JDBCPreProcessor"),
        @JsonSubTypes.Type(value = MsJDBCPostProcessor.class, name = "JDBCPostProcessor"),
        @JsonSubTypes.Type(value = MsTestPlan.class, name = "TestPlan"),
        @JsonSubTypes.Type(value = MsThreadGroup.class, name = "ThreadGroup"),
        @JsonSubTypes.Type(value = MsAuthManager.class, name = "AuthManager"),
        @JsonSubTypes.Type(value = MsAssertions.class, name = "Assertions"),
        @JsonSubTypes.Type(value = MsExtract.class, name = "Extract"),
        @JsonSubTypes.Type(value = MsTCPSampler.class, name = "TCPSampler"),
        @JsonSubTypes.Type(value = MsDubboSampler.class, name = "DubboSampler"),
        @JsonSubTypes.Type(value = MsJDBCSampler.class, name = "JDBCSampler"),
        @JsonSubTypes.Type(value = MsConstantTimer.class, name = "ConstantTimer"),
        @JsonSubTypes.Type(value = MsIfController.class, name = "IfController"),
        @JsonSubTypes.Type(value = MsTransactionController.class, name = "TransactionController"),
        @JsonSubTypes.Type(value = MsScenario.class, name = "scenario"),
        @JsonSubTypes.Type(value = MsLoopController.class, name = "LoopController"),
        @JsonSubTypes.Type(value = MsJmeterElement.class, name = "JmeterElement"),

})
@JSONType(seeAlso = {MsHTTPSamplerProxy.class, MsHeaderManager.class, MsJSR223Processor.class, MsJSR223PostProcessor.class, MsJDBCPreProcessor.class, MsJDBCPostProcessor.class,
        MsJSR223PreProcessor.class, MsTestPlan.class, MsThreadGroup.class, MsAuthManager.class, MsAssertions.class,
        MsExtract.class, MsTCPSampler.class, MsDubboSampler.class, MsJDBCSampler.class, MsConstantTimer.class, MsIfController.class, MsTransactionController.class, MsScenario.class, MsLoopController.class, MsJmeterElement.class}, typeKey = "type")
@Data
public abstract class MsTestElement {
    private String type;
    @JSONField(ordinal = 1)
    private String id;
    @JSONField(ordinal = 2)
    private String name;
    @JSONField(ordinal = 3)
    private String label;
    @JSONField(ordinal = 4)
    private String resourceId;
    @JSONField(ordinal = 5)
    private String referenced;
    @JSONField(ordinal = 6)
    private boolean active;
    @JSONField(ordinal = 7)
    private String index;
    @JSONField(ordinal = 8)
    private boolean enable = true;
    @JSONField(ordinal = 9)
    private String refType;
    @JSONField(ordinal = 10)
    private LinkedList<MsTestElement> hashTree;
    @JSONField(ordinal = 11)
    private boolean customizeReq;
    @JSONField(ordinal = 12)
    private String projectId;
    @JSONField(ordinal = 13)
    private boolean isMockEnvironment;
    @JSONField(ordinal = 14)
    private String environmentId;
    private MsTestElement parent;

    private static final String BODY_FILE_DIR = FileUtils.BODY_FILE_DIR;

    /**
     * todo 公共环境逐层传递，如果自身有环境 以自身引用环境为准否则以公共环境作为请求环境
     */
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        if (CollectionUtils.isNotEmpty(hashTree)) {
            for (MsTestElement el : hashTree) {
                el.toHashTree(tree, el.hashTree, config);
            }
        }
    }

    /**
     * 转换JMX
     *
     * @param hashTree
     * @return
     */
    public String getJmx(HashTree hashTree) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            SaveService.saveTree(hashTree, baos);
            return baos.toString();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.warn("HashTree error, can't log jmx scenarioDefinition");
        }
        return null;
    }

    public HashTree generateHashTree(ParameterConfig config) {
        HashTree jmeterTestPlanHashTree = new ListedHashTree();
        this.toHashTree(jmeterTestPlanHashTree, this.hashTree, config);
        return jmeterTestPlanHashTree;
    }

    public Arguments addArguments(ParameterConfig config) {
        if (config.isEffective(this.getProjectId()) && config.getConfig().get(this.getProjectId()).getCommonConfig() != null
                && CollectionUtils.isNotEmpty(config.getConfig().get(this.getProjectId()).getCommonConfig().getVariables())) {
            Arguments arguments = new Arguments();
            arguments.setEnabled(true);
            arguments.setName(StringUtils.isNoneBlank(this.getName()) ? this.getName() : "Arguments");
            arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
            arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));
            config.getConfig().get(this.getProjectId()).getCommonConfig().getVariables().stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                    arguments.addArgument(keyValue.getName(), keyValue.getValue(), "=")
            );
            if (arguments.getArguments().size() > 0) {
                return arguments;
            }
        }
        return null;
    }

    protected Map<String, EnvironmentConfig> getEnvironmentConfig(String environmentId) {
        ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
        ApiTestEnvironmentWithBLOBs environment = environmentService.get(environmentId);
        if (environment != null && environment.getConfig() != null) {
            if (StringUtils.equals(environment.getName(), MockConfigStaticData.MOCK_EVN_NAME)) {
                isMockEnvironment = true;
            }
            // 单独接口执行
            Map<String, EnvironmentConfig> map = new HashMap<>();
            map.put(this.getProjectId(), JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class));
            return map;
        }


        return null;
    }

    protected void addCsvDataSet(HashTree tree, List<ScenarioVariable> variables, ParameterConfig config, String shareMode) {
        if (CollectionUtils.isNotEmpty(variables)) {
            List<ScenarioVariable> list = variables.stream().filter(ScenarioVariable::isCSVValid).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(item -> {
                    CSVDataSet csvDataSet = new CSVDataSet();
                    csvDataSet.setEnabled(true);
                    csvDataSet.setProperty(TestElement.TEST_CLASS, CSVDataSet.class.getName());
                    csvDataSet.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
                    csvDataSet.setName(StringUtils.isEmpty(item.getName()) ? "CSVDataSet" : item.getName());
                    csvDataSet.setProperty("fileEncoding", StringUtils.isEmpty(item.getEncoding()) ? "UTF-8" : item.getEncoding());
                    if (CollectionUtils.isNotEmpty(item.getFiles())) {
                        if (!config.isOperating() && !new File(BODY_FILE_DIR + "/" + item.getFiles().get(0).getId() + "_" + item.getFiles().get(0).getName()).exists()) {
                            MSException.throwException(StringUtils.isEmpty(item.getName()) ? "CSVDataSet" : item.getName() + "：[ CSV文件不存在 ]");
                        }
                        csvDataSet.setProperty("filename", BODY_FILE_DIR + "/" + item.getFiles().get(0).getId() + "_" + item.getFiles().get(0).getName());
                    }
                    csvDataSet.setIgnoreFirstLine(false);
                    csvDataSet.setProperty("shareMode", shareMode);
                    csvDataSet.setProperty("recycle", true);
                    csvDataSet.setProperty("delimiter", item.getDelimiter());
                    csvDataSet.setProperty("quotedData", item.isQuotedData());
                    csvDataSet.setComment(StringUtils.isEmpty(item.getDescription()) ? "" : item.getDescription());
                    tree.add(csvDataSet);
                });
            }
        }
    }

    protected void addCounter(HashTree tree, List<ScenarioVariable> variables) {
        if (CollectionUtils.isNotEmpty(variables)) {
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
                    counterConfig.setComment(StringUtils.isEmpty(item.getDescription()) ? "" : item.getDescription());
                    tree.add(counterConfig);
                });
            }
        }
    }

    protected void addRandom(HashTree tree, List<ScenarioVariable> variables) {
        if (CollectionUtils.isNotEmpty(variables)) {
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
                    randomVariableConfig.setComment(StringUtils.isEmpty(item.getDescription()) ? "" : item.getDescription());
                    tree.add(randomVariableConfig);
                });
            }
        }
    }

    public String getFullPath(MsTestElement element, String path) {
        if (element.getParent() == null) {
            return path;
        }
        if (MsTestElementConstants.LoopController.name().equals(element.getType())) {
            MsLoopController loopController = (MsLoopController) element;
            if (StringUtils.equals(loopController.getLoopType(), LoopConstants.WHILE.name()) && loopController.getWhileController() != null) {
                path = "While 循环" + DelimiterConstants.STEP_DELIMITER.toString() + "While 循环-" + "${MS_LOOP_CONTROLLER_CONFIG}";
            }
            if (StringUtils.equals(loopController.getLoopType(), LoopConstants.FOREACH.name()) && loopController.getForEachController() != null) {
                path = "ForEach 循环" + DelimiterConstants.STEP_DELIMITER.toString() + " ForEach 循环-" + "${MS_LOOP_CONTROLLER_CONFIG}";
            }
            if (StringUtils.equals(loopController.getLoopType(), LoopConstants.LOOP_COUNT.name()) && loopController.getCountController() != null) {
                path = "次数循环" + DelimiterConstants.STEP_DELIMITER.toString() + "次数循环-" + "${MS_LOOP_CONTROLLER_CONFIG}";
            }
        } else {
            path = StringUtils.isEmpty(element.getName()) ? element.getType() : element.getName() + DelimiterConstants.STEP_DELIMITER.toString() + path;
        }
        return getFullPath(element.getParent(), path);
    }

    public void getScenarioSet(MsTestElement element, List<String> id_names) {
        if (StringUtils.equals(element.getType(), "scenario")) {
            id_names.add(element.getResourceId() + "_" + element.getName());
        }
        if (element.getParent() == null) {
            return;
        }
        getScenarioSet(element.getParent(), id_names);
    }

    protected String getParentName(MsTestElement parent) {
        if (parent != null) {
            if (MsTestElementConstants.TransactionController.name().equals(parent.getType())) {
                MsTransactionController transactionController = (MsTransactionController) parent;
                if (StringUtils.isNotEmpty(transactionController.getName())) {
                    return transactionController.getName();
                } else if (StringUtils.isNotEmpty(transactionController.getLabelName())) {
                    return transactionController.getLabelName();
                } else {
                    return "TransactionController";
                }
            }
            // 获取全路径以备后面使用
            String fullPath = getFullPath(parent, new String());
            return fullPath + DelimiterConstants.SEPARATOR.toString() + parent.getName();
        }
        return "";
    }

    public boolean isURL(String str) {
        try {
            if (StringUtils.isEmpty(str)) {
                return false;
            }
            new URL(str);
            return true;
        } catch (Exception e) {
            // 支持包含变量的url
            if (str.matches("^(http|https|ftp)://.*$") && str.matches(".*://\\$\\{.*$")) {
                return true;
            }
            return false;
        }
    }

    public static <T> List<T> findFromHashTreeByType(MsTestElement hashTree, Class<T> clazz, List<T> requests) {
        if (requests == null) {
            requests = new ArrayList<>();
        }
        if (clazz.isInstance(hashTree)) {
            requests.add((T) hashTree);
        } else {
            if (hashTree != null) {
                LinkedList<MsTestElement> childHashTree = hashTree.getHashTree();
                if (CollectionUtils.isNotEmpty(childHashTree)) {
                    for (MsTestElement item : childHashTree) {
                        findFromHashTreeByType(item, clazz, requests);
                    }
                }
            }
        }
        return requests;
    }
}





