package io.metersphere.api.dto.definition.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.definition.request.controller.MsLoopController;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
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
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.RandomVariableConfig;
import org.apache.jmeter.modifiers.CounterConfig;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ElementUtil {

    private static final String BODY_FILE_DIR = FileUtils.BODY_FILE_DIR;

    public static Arguments addArguments(ParameterConfig config, String projectId, String name) {
        if (config.isEffective(projectId) && config.getConfig().get(projectId).getCommonConfig() != null
                && CollectionUtils.isNotEmpty(config.getConfig().get(projectId).getCommonConfig().getVariables())) {
            Arguments arguments = new Arguments();
            arguments.setEnabled(true);
            arguments.setName(StringUtils.isNoneBlank(name) ? name : "Arguments");
            arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
            arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));
            config.getConfig().get(projectId).getCommonConfig().getVariables().stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                    arguments.addArgument(keyValue.getName(), keyValue.getValue(), "=")
            );
            if (arguments.getArguments().size() > 0) {
                return arguments;
            }
        }
        return null;
    }

    public static Map<String, EnvironmentConfig> getEnvironmentConfig(String environmentId, String projectId, boolean isMockEnvironment) {
        ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
        ApiTestEnvironmentWithBLOBs environment = environmentService.get(environmentId);
        if (environment != null && environment.getConfig() != null) {
            if (StringUtils.equals(environment.getName(), MockConfigStaticData.MOCK_EVN_NAME)) {
                isMockEnvironment = true;
            }
            // 单独接口执行
            Map<String, EnvironmentConfig> map = new HashMap<>();
            map.put(projectId, JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class));
            return map;
        }


        return null;
    }

    public static void addCsvDataSet(HashTree tree, List<ScenarioVariable> variables, ParameterConfig config, String shareMode) {
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

    public static void addCounter(HashTree tree, List<ScenarioVariable> variables) {
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

    public static void addRandom(HashTree tree, List<ScenarioVariable> variables) {
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

    public static String getFullPath(MsTestElement element, String path) {
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

    public static void getScenarioSet(MsTestElement element, List<String> id_names) {
        if (StringUtils.equals(element.getType(), "scenario")) {
            id_names.add(element.getResourceId() + "_" + element.getName());
        }
        if (element.getParent() == null) {
            return;
        }
        getScenarioSet(element.getParent(), id_names);
    }

    public static String getParentName(MsTestElement parent) {
        if (parent != null) {
            // 获取全路径以备后面使用
            String fullPath = getFullPath(parent, new String());
            return fullPath + DelimiterConstants.SEPARATOR.toString() + parent.getName();
        }
        return "";
    }

    public static String getFullIndexPath(MsTestElement element, String path) {
        if (element == null || element.getParent() == null) {
            return path;
        }
        path = element.getIndex() + "_" + path;
        return getFullIndexPath(element.getParent(), path);
    }

    public static boolean isURL(String str) {
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


    public final static HashMap<String, String> clazzMap = new HashMap<String, String>() {
        {
            put("scenario", "io.metersphere.api.dto.definition.request.MsScenario");
            put("HTTPSamplerProxy", "io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy");
            put("DubboSampler", "io.metersphere.api.dto.definition.request.sampler.MsDubboSampler");
            put("JDBCSampler", "io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler");
            put("TCPSampler", "io.metersphere.api.dto.definition.request.sampler.MsTCPSampler");
            put("IfController", "io.metersphere.api.dto.definition.request.controller.MsIfController");
            put("TransactionController", "io.metersphere.api.dto.definition.request.controller.MsTransactionController");
            put("LoopController", "io.metersphere.api.dto.definition.request.controller.MsLoopController");
            put("ConstantTimer", "io.metersphere.api.dto.definition.request.timer.MsConstantTimer");
            put("JSR223Processor", "io.metersphere.api.dto.definition.request.processors.MsJSR223Processor");
            put("JSR223PreProcessor", "io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor");
            put("JSR223PostProcessor", "io.metersphere.api.dto.definition.request.processors.post.MsJSR223PostProcessor");
            put("JDBCPreProcessor", "io.metersphere.api.dto.definition.request.processors.pre.MsJDBCPreProcessor");
            put("JDBCPostProcessor", "io.metersphere.api.dto.definition.request.processors.post.MsJDBCPostProcessor");
            put("Assertions", "io.metersphere.api.dto.definition.request.assertions.MsAssertions");
            put("Extract", "io.metersphere.api.dto.definition.request.extract.MsExtract");
            put("JmeterElement", "io.metersphere.api.dto.definition.request.unknown.MsJmeterElement");
            put("TestPlan", "io.metersphere.api.dto.definition.request.MsTestPlan");
            put("ThreadGroup", "io.metersphere.api.dto.definition.request.MsThreadGroup");
            put("DNSCacheManager", "io.metersphere.api.dto.definition.request.dns.MsDNSCacheManager");
            put("DebugSampler", "io.metersphere.api.dto.definition.request.sampler.MsDebugSampler");
            put("AuthManager", "io.metersphere.api.dto.definition.request.auth.MsAuthManager");
        }
    };

    private static void formatSampler(JSONObject element) {
        if (element == null || StringUtils.isEmpty(element.getString("type"))) {
            return;
        }
        if (element.get("clazzName") == null && element.getString("type").equals("TCPSampler")) {
            if (element.getString("tcpPreProcessor") != null) {
                JSONObject tcpPreProcessor = JSON.parseObject(element.getString("tcpPreProcessor"));
                if (tcpPreProcessor != null && tcpPreProcessor.get("clazzName") == null) {
                    tcpPreProcessor.fluentPut("clazzName", clazzMap.get(tcpPreProcessor.getString("type")));
                    element.fluentPut("tcpPreProcessor", tcpPreProcessor);
                }
            }
        } else if (element.getString("type").equals("HTTPSamplerProxy")) {
            if (element.getString("authManager") != null) {
                JSONObject authManager = JSON.parseObject(element.getString("authManager"));
                if (authManager != null && authManager.get("clazzName") == null) {
                    authManager.fluentPut("clazzName", clazzMap.get(authManager.getString("type")));
                    element.fluentPut("authManager", authManager);
                }
            }
        }
    }

    public static void relationships(JSONArray hashTree, List<String> referenceRelationships) {
        for (int i = 0; i < hashTree.size(); i++) {
            JSONObject element = hashTree.getJSONObject(i);
            if (element != null && StringUtils.equals(element.get("type").toString(), "scenario") && StringUtils.equals(element.get("referenced").toString(), "REF")) {
                if (!referenceRelationships.contains(element.get("id").toString())) {
                    referenceRelationships.add(element.get("id").toString());
                }
            }
            if (element.containsKey("hashTree")) {
                JSONArray elementJSONArray = element.getJSONArray("hashTree");
                relationships(elementJSONArray, referenceRelationships);
            }
        }
    }

    public static void dataFormatting(JSONArray hashTree) {
        for (int i = 0; i < hashTree.size(); i++) {
            JSONObject element = hashTree.getJSONObject(i);
            formatSampler(element);
            if (element != null && element.get("clazzName") == null && clazzMap.containsKey(element.getString("type"))) {
                element.fluentPut("clazzName", clazzMap.get(element.getString("type")));
            }
            if (element.containsKey("hashTree")) {
                JSONArray elementJSONArray = element.getJSONArray("hashTree");
                dataFormatting(elementJSONArray);
            }
        }
    }

    public static void dataFormatting(JSONObject element) {
        if (element != null && element.get("clazzName") == null && clazzMap.containsKey(element.getString("type"))) {
            element.fluentPut("clazzName", clazzMap.get(element.getString("type")));
        }
        formatSampler(element);
        if (element != null && element.containsKey("hashTree")) {
            JSONArray elementJSONArray = element.getJSONArray("hashTree");
            dataFormatting(elementJSONArray);
        }
    }

    public static void dataSetDomain(JSONArray hashTree, MsParameter msParameter) {
        try {
            for (int i = 0; i < hashTree.size(); i++) {
                JSONObject element = hashTree.getJSONObject(i);
                boolean isScenarioEnv = false;
                ParameterConfig config = new ParameterConfig();
                if (element != null && element.get("type").toString().equals("scenario")) {
                    MsScenario scenario = JSONObject.toJavaObject(element, MsScenario.class);
                    if (scenario.isEnvironmentEnable()) {
                        isScenarioEnv = true;
                        Map<String, EnvironmentConfig> envConfig = new HashMap<>(16);
                        Map<String, String> environmentMap = (Map<String, String>) element.get("environmentMap");
                        if (environmentMap != null && !environmentMap.isEmpty()) {
                            environmentMap.keySet().forEach(projectId -> {
                                ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
                                ApiTestEnvironmentWithBLOBs environment = environmentService.get(environmentMap.get(projectId));
                                if (environment != null && environment.getConfig() != null) {
                                    EnvironmentConfig env = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
                                    env.setApiEnvironmentid(environment.getId());
                                    envConfig.put(projectId, env);
                                }
                            });
                            config.setConfig(envConfig);
                        }
                    }
                } else if (element != null && element.get("type").toString().equals("HTTPSamplerProxy")) {
                    MsHTTPSamplerProxy httpSamplerProxy = JSON.toJavaObject(element, MsHTTPSamplerProxy.class);
                    if (httpSamplerProxy != null
                            && (!httpSamplerProxy.isCustomizeReq() || (httpSamplerProxy.isCustomizeReq() && httpSamplerProxy.getIsRefEnvironment()))) {
                        HashTree tmpHashTree = new HashTree();
                        httpSamplerProxy.toHashTree(tmpHashTree, null, msParameter);
                        if (tmpHashTree != null && tmpHashTree.getArray().length > 0) {
                            HTTPSamplerProxy object = (HTTPSamplerProxy) tmpHashTree.getArray()[0];
                            if (object != null && StringUtils.isNotEmpty(object.getDomain())) {
                                element.fluentPut("domain", StringUtils.isNotEmpty(object.getProtocol()) ? object.getProtocol() + "://" + object.getDomain() : object.getDomain());
                            }
                        }
                    }
                }
                if (element.containsKey("hashTree")) {
                    JSONArray elementJSONArray = element.getJSONArray("hashTree");
                    if (isScenarioEnv) {
                        dataSetDomain(elementJSONArray, config);
                    } else {
                        dataSetDomain(elementJSONArray, msParameter);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
    }
}
