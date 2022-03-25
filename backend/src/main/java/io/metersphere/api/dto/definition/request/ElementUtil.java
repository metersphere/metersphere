package io.metersphere.api.dto.definition.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.definition.request.controller.MsLoopController;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.mockconfig.MockConfigStaticData;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.service.ApiTestEnvironmentService;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.commons.constants.DelimiterConstants;
import io.metersphere.commons.constants.LoopConstants;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.FileUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.EnvironmentGroupProjectService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.RandomVariableConfig;
import org.apache.jmeter.modifiers.CounterConfig;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ElementUtil {
    private static final String PRE = "PRE";
    private static final String POST = "POST";
    private static final String ASSERTIONS = "ASSERTIONS";
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
            if (StringUtils.isEmpty(projectId)) {
                projectId = environment.getProjectId();
            }
            if (StringUtils.equals(environment.getName(), MockConfigStaticData.MOCK_EVN_NAME)) {
                isMockEnvironment = true;
            }
            // 单独接口执行
            if (StringUtils.isEmpty(projectId)) {
                projectId = environment.getProjectId();
            }
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

    public static void addCounter(HashTree tree, List<ScenarioVariable> variables, boolean isInternal) {
        if (CollectionUtils.isNotEmpty(variables)) {
            List<ScenarioVariable> list = variables.stream().filter(ScenarioVariable::isCounterValid).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(item -> {
                    CounterConfig counterConfig = new CounterConfig();
                    counterConfig.setEnabled(true);
                    counterConfig.setProperty(TestElement.TEST_CLASS, CounterConfig.class.getName());
                    counterConfig.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("CounterConfigGui"));
                    counterConfig.setName(item.getName());
                    if (isInternal) {
                        counterConfig.setStart((item.getStartNumber() + 1));
                    } else {
                        counterConfig.setStart(item.getStartNumber());
                    }
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

    public final static List<String> requests = new ArrayList<String>() {{
        this.add("HTTPSamplerProxy");
        this.add("DubboSampler");
        this.add("JDBCSampler");
        this.add("TCPSampler");
        this.add("JSR223Processor");
        this.add("JSR223PreProcessor");
        this.add("JSR223PostProcessor");
        this.add("JDBCPreProcessor");
        this.add("JDBCPostProcessor");
        this.add("JmeterElement");
        this.add("TestPlan");
        this.add("ThreadGroup");
        this.add("DNSCacheManager");
        this.add("DebugSampler");
        this.add("AuthManager");
    }};

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

    /**
     * 只找出场景直接依赖
     *
     * @param hashTree
     * @param referenceRelationships
     */
    public static void relationships(JSONArray hashTree, List<String> referenceRelationships) {
        for (int i = 0; i < hashTree.size(); i++) {
            JSONObject element = hashTree.getJSONObject(i);
            if (element != null && StringUtils.equals(element.get("type").toString(), "scenario") && StringUtils.equals(element.get("referenced").toString(), "REF")) {
                if (!referenceRelationships.contains(element.get("id").toString())) {
                    referenceRelationships.add(element.get("id").toString());
                }
            } else {
                if (element.containsKey("hashTree")) {
                    JSONArray elementJSONArray = element.getJSONArray("hashTree");
                    relationships(elementJSONArray, referenceRelationships);
                }
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
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            for (int i = 0; i < hashTree.size(); i++) {
                JSONObject element = hashTree.getJSONObject(i);
                boolean isScenarioEnv = false;
                ParameterConfig config = new ParameterConfig();
                if (element != null && element.get("type").toString().equals("scenario")) {
                    MsScenario scenario = JSONObject.toJavaObject(element, MsScenario.class);
                    if (scenario.isEnvironmentEnable()) {
                        isScenarioEnv = true;
                        Map<String, String> environmentMap = new HashMap<>();
                        ApiScenarioMapper apiScenarioMapper = CommonBeanFactory.getBean(ApiScenarioMapper.class);
                        EnvironmentGroupProjectService environmentGroupProjectService = CommonBeanFactory.getBean(EnvironmentGroupProjectService.class);
                        ApiScenarioWithBLOBs apiScenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(scenario.getId());
                        String environmentType = apiScenarioWithBLOBs.getEnvironmentType();
                        String environmentGroupId = apiScenarioWithBLOBs.getEnvironmentGroupId();
                        String environmentJson = apiScenarioWithBLOBs.getEnvironmentJson();
                        if (StringUtils.equals(environmentType, EnvironmentType.GROUP.name())) {
                            environmentMap = environmentGroupProjectService.getEnvMap(environmentGroupId);
                        } else if (StringUtils.equals(environmentType, EnvironmentType.JSON.name())) {
                            environmentMap = JSON.parseObject(environmentJson, Map.class);
                        }
                        Map<String, EnvironmentConfig> envConfig = new HashMap<>(16);
                        if (environmentMap != null && !environmentMap.isEmpty()) {
                            Map<String, String> finalEnvironmentMap = environmentMap;
                            environmentMap.keySet().forEach(projectId -> {
                                ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
                                ApiTestEnvironmentWithBLOBs environment = environmentService.get(finalEnvironmentMap.get(projectId));
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
                        // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
                        if (element != null && StringUtils.isNotEmpty(element.getString("hashTree"))) {
                            LinkedList<MsTestElement> elements = mapper.readValue(element.getString("hashTree"),
                                    new TypeReference<LinkedList<MsTestElement>>() {
                                    });
                            httpSamplerProxy.setHashTree(elements);
                        }
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

    public static void mergeHashTree(MsTestElement element, LinkedList<MsTestElement> targetHashTree) {
        try {
            LinkedList<MsTestElement> sourceHashTree = element.getHashTree();
            if (CollectionUtils.isNotEmpty(sourceHashTree) && CollectionUtils.isNotEmpty(targetHashTree) && sourceHashTree.size() < targetHashTree.size()) {
                element.setHashTree(targetHashTree);
                return;
            }
            List<String> sourceIds = new ArrayList<>();
            List<String> delIds = new ArrayList<>();
            Map<String, MsTestElement> updateMap = new HashMap<>();
            if (CollectionUtils.isEmpty(sourceHashTree)) {
                if (CollectionUtils.isNotEmpty(targetHashTree)) {
                    element.setHashTree(targetHashTree);
                }
                return;
            }
            if (CollectionUtils.isNotEmpty(targetHashTree)) {
                for (MsTestElement item : targetHashTree) {
                    if (StringUtils.isNotEmpty(item.getId())) {
                        updateMap.put(item.getId(), item);
                    }
                }
            }
            // 找出待更新内容和源已经被删除的内容
            if (CollectionUtils.isNotEmpty(sourceHashTree)) {
                for (int i = 0; i < sourceHashTree.size(); i++) {
                    MsTestElement source = sourceHashTree.get(i);
                    if (source != null) {
                        sourceIds.add(source.getId());
                        if (!StringUtils.equals(source.getLabel(), "SCENARIO-REF-STEP") && StringUtils.isNotEmpty(source.getId())) {
                            if (updateMap.containsKey(source.getId())) {
                                sourceHashTree.set(i, updateMap.get(source.getId()));
                            } else {
                                delIds.add(source.getId());
                            }
                        }
                        // 历史数据兼容
                        if (StringUtils.isEmpty(source.getId()) && !StringUtils.equals(source.getLabel(), "SCENARIO-REF-STEP") && i < targetHashTree.size()) {
                            sourceHashTree.set(i, targetHashTree.get(i));
                        }
                    }
                }
            }

            // 删除多余的步骤
            sourceHashTree.removeIf(item -> item != null && delIds.contains(item.getId()));
            // 补充新增的源引用步骤
            if (CollectionUtils.isNotEmpty(targetHashTree)) {
                for (MsTestElement item : targetHashTree) {
                    if (!sourceIds.contains(item.getId())) {
                        sourceHashTree.add(item);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(sourceHashTree)) {
                element.setHashTree(sourceHashTree);
            }
        } catch (Exception e) {
            element.setHashTree(targetHashTree);
        }
    }

    public static List<JSONObject> mergeHashTree(List<JSONObject> sourceHashTree, List<JSONObject> targetHashTree) {
        try {
            List<String> sourceIds = new ArrayList<>();
            List<String> delIds = new ArrayList<>();
            Map<String, JSONObject> updateMap = new HashMap<>();

            if (CollectionUtils.isNotEmpty(targetHashTree)) {
                for (int i = 0; i < targetHashTree.size(); i++) {
                    JSONObject item = targetHashTree.get(i);
                    item.put("disabled", true);
                    if (StringUtils.isNotEmpty(item.getString("id"))) {
                        updateMap.put(item.getString("id"), item);
                    }
                }
            }
            // 找出待更新内容和源已经被删除的内容
            if (CollectionUtils.isNotEmpty(sourceHashTree)) {
                for (int i = 0; i < sourceHashTree.size(); i++) {
                    JSONObject source = sourceHashTree.get(i);
                    if (source != null) {
                        sourceIds.add(source.getString("id"));
                        if (!StringUtils.equals(source.getString("label"), "SCENARIO-REF-STEP") && StringUtils.isNotEmpty(source.getString("id"))) {
                            if (updateMap.containsKey(source.getString("id"))) {
                                sourceHashTree.set(i, updateMap.get(source.getString("id")));
                            } else {
                                delIds.add(source.getString("id"));
                            }
                        }
                        // 历史数据兼容
                        if (StringUtils.isEmpty(source.getString("id")) && !StringUtils.equals(source.getString("label"), "SCENARIO-REF-STEP") && i < targetHashTree.size()) {
                            sourceHashTree.set(i, targetHashTree.get(i));
                        }
                    }
                }
            }

            // 删除多余的步骤
            for (int i = 0; i < sourceHashTree.size(); i++) {
                JSONObject source = sourceHashTree.get(i);
                if (delIds.contains(source.getString("id"))) {
                    sourceHashTree.remove(i);
                }
            }
            // 补充新增的源引用步骤
            if (CollectionUtils.isNotEmpty(targetHashTree)) {
                for (int i = 0; i < targetHashTree.size(); i++) {
                    JSONObject item = sourceHashTree.get(i);
                    if (!sourceIds.contains(item.getString("id"))) {
                        sourceHashTree.add(item);
                    }
                }
            }
        } catch (Exception e) {
            return targetHashTree;
        }
        return sourceHashTree;
    }

    public static String hashTreeToString(HashTree hashTree) {
        try (ByteArrayOutputStream bas = new ByteArrayOutputStream()) {
            SaveService.saveTree(hashTree, bas);
            return bas.toString();
        } catch (Exception e) {
            e.printStackTrace();
            io.metersphere.plugin.core.utils.LogUtil.warn("HashTree error, can't log jmx scenarioDefinition");
        }
        return null;
    }

    public static String getResourceId(String resourceId, ParameterConfig config, MsTestElement parent, String indexPath) {
        if (StringUtils.isNotEmpty(config.getScenarioId()) && StringUtils.equals(config.getReportType(), RunModeConstants.SET_REPORT.toString())) {
            resourceId = config.getScenarioId() + "=" + resourceId;
        }
        return resourceId + "_" + ElementUtil.getFullIndexPath(parent, indexPath);
    }

    public static JSR223PreProcessor argumentsToProcessor(Arguments arguments) {
        JSR223PreProcessor processor = new JSR223PreProcessor();
        processor.setEnabled(true);
        processor.setName("User Defined Variables");
        processor.setProperty("scriptLanguage", "beanshell");
        processor.setProperty(TestElement.TEST_CLASS, JSR223PreProcessor.class.getName());
        processor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
        StringBuffer script = new StringBuffer();
        if (arguments != null) {
            for (int i = 0; i < arguments.getArguments().size(); ++i) {
                String argValue = arguments.getArgument(i).getValue();
                script.append("vars.put(\"" + arguments.getArgument(i).getName() + "\",\"" + argValue + "\");").append("\n");
            }
            processor.setProperty("script", script.toString());
        }
        return processor;
    }

    public static void setBaseParams(AbstractTestElement sampler, MsTestElement parent, ParameterConfig config, String id, String indexPath) {
        sampler.setProperty("MS-ID", id);
        sampler.setProperty("MS-RESOURCE-ID", ElementUtil.getResourceId(id, config, parent, indexPath));
        LoggerUtil.debug("mqtt sampler resourceId :" + sampler.getPropertyAsString("MS-RESOURCE-ID"));
    }

    public static void accuracyHashTree(HashTree hashTree) {
        Map<Object, HashTree> objects = new LinkedHashMap<>();
        Object groupHashTree = hashTree;
        if (hashTree != null && hashTree.size() > 0) {
            for (Object key : hashTree.keySet()) {
                if (key instanceof TestPlan) {
                    for (Object node : hashTree.get(key).keySet()) {
                        if (node instanceof ThreadGroup) {
                            groupHashTree = hashTree.get(key).get(node);
                        }
                    }
                } else {
                    objects.put(key, hashTree.get(key));
                }
            }
        }
        if (!objects.isEmpty() && groupHashTree instanceof HashTree) {
            for (Object key : objects.keySet()) {
                hashTree.remove(key);
                ((HashTree) groupHashTree).add(key, objects.get(key));
            }
        }
    }

    private static final List<String> preOperates = new ArrayList<String>() {{
        this.add("JSR223PreProcessor");
        this.add("JDBCPreProcessor");
        this.add("ConstantTimer");
    }};
    private static final List<String> postOperates = new ArrayList<String>() {{
        this.add("JSR223PostProcessor");
        this.add("JDBCPostProcessor");
        this.add("Extract");
    }};


    public static void copyBean(JSONObject target, JSONObject source) {
        if (source == null || target == null) {
            return;
        }
        for (String key : target.keySet()) {
            if (source.containsKey(key) && !StringUtils.equalsIgnoreCase(key, "hashTree")) {
                target.put(key, source.get(key));
            }
        }
    }

    private static List<MsTestElement> orderList(List<MsTestElement> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                if (StringUtils.isEmpty(list.get(i).getIndex())) {
                    list.get(i).setIndex(String.valueOf(i));
                }
            }
            return list.stream().sorted(Comparator.comparing(MsTestElement::getIndex)).collect(Collectors.toList());
        }
        return list;
    }

    public static Map<String, List<JSONObject>> group(JSONArray elements) {
        Map<String, List<JSONObject>> groupMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(elements)) {
            for (int i = 0; i < elements.size(); i++) {
                JSONObject item = elements.getJSONObject(i);
                if ("Assertions".equals(item.getString("type"))) {
                    if (groupMap.containsKey(ASSERTIONS)) {
                        groupMap.get(ASSERTIONS).add(item);
                    } else {
                        groupMap.put(ASSERTIONS, new LinkedList<JSONObject>() {{
                            this.add(item);
                        }});
                    }
                } else if (preOperates.contains(item.getString("type"))) {
                    if (groupMap.containsKey(PRE)) {
                        groupMap.get(PRE).add(item);
                    } else {
                        groupMap.put(PRE, new LinkedList<JSONObject>() {{
                            this.add(item);
                        }});
                    }
                } else if (postOperates.contains(item.getString("type"))) {
                    if (groupMap.containsKey(POST)) {
                        groupMap.get(POST).add(item);
                    } else {
                        groupMap.put(POST, new LinkedList<JSONObject>() {{
                            this.add(item);
                        }});
                    }
                }
            }
        }
        return groupMap;
    }

    public static List<MsTestElement> order(List<MsTestElement> elements) {
        List<MsTestElement> elementList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(elements)) {
            Map<String, List<MsTestElement>> groupMap = new LinkedHashMap<>();
            elements.forEach(item -> {
                if ("Assertions".equals(item.getType())) {
                    if (groupMap.containsKey(ASSERTIONS)) {
                        groupMap.get(ASSERTIONS).add(item);
                    } else {
                        groupMap.put(ASSERTIONS, new LinkedList<MsTestElement>() {{
                            this.add(item);
                        }});
                    }
                } else if (preOperates.contains(item.getType())) {
                    if (groupMap.containsKey(PRE)) {
                        groupMap.get(PRE).add(item);
                    } else {
                        groupMap.put(PRE, new LinkedList<MsTestElement>() {{
                            this.add(item);
                        }});
                    }
                } else if (postOperates.contains(item.getType())) {
                    if (groupMap.containsKey(POST)) {
                        groupMap.get(POST).add(item);
                    } else {
                        groupMap.put(POST, new LinkedList<MsTestElement>() {{
                            this.add(item);
                        }});
                    }
                } else {
                    elementList.add(item);
                }
            });
            if (CollectionUtils.isNotEmpty(groupMap.get(PRE))) {
                elementList.addAll(orderList(groupMap.get(PRE)));
            }
            if (CollectionUtils.isNotEmpty(groupMap.get(POST))) {
                elementList.addAll(orderList(groupMap.get(POST)));
            }
            if (CollectionUtils.isNotEmpty(groupMap.get(ASSERTIONS))) {
                elementList.addAll(groupMap.get(ASSERTIONS));
            }
        }
        return elementList;
    }
}
