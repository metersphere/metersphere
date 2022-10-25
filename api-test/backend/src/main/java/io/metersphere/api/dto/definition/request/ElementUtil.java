package io.metersphere.api.dto.definition.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.definition.request.assertions.MsAssertions;
import io.metersphere.api.dto.definition.request.auth.MsAuthManager;
import io.metersphere.api.dto.definition.request.controller.MsIfController;
import io.metersphere.api.dto.definition.request.controller.MsLoopController;
import io.metersphere.api.dto.definition.request.controller.MsTransactionController;
import io.metersphere.api.dto.definition.request.dns.MsDNSCacheManager;
import io.metersphere.api.dto.definition.request.extract.MsExtract;
import io.metersphere.api.dto.definition.request.processors.MsJSR223Processor;
import io.metersphere.api.dto.definition.request.processors.post.MsJDBCPostProcessor;
import io.metersphere.api.dto.definition.request.processors.post.MsJSR223PostProcessor;
import io.metersphere.api.dto.definition.request.processors.pre.MsJDBCPreProcessor;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.definition.request.sampler.*;
import io.metersphere.api.dto.definition.request.timer.MsConstantTimer;
import io.metersphere.api.dto.definition.request.unknown.MsJmeterElement;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.scenario.DatabaseConfig;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.scenario.environment.item.EnvAssertions;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestEnvironmentWithBLOBs;
import io.metersphere.base.domain.FileMetadata;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.enums.StorageEnums;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.i18n.Translator;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.request.BodyFile;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.RandomVariableConfig;
import org.apache.jmeter.modifiers.CounterConfig;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.modifiers.UserParameters;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ElementUtil {
    private static final String PRE = "PRE";
    private static final String POST = "POST";
    private static final String ASSERTIONS = ElementConstants.ASSERTIONS;
    private static final String BODY_FILE_DIR = FileUtils.BODY_FILE_DIR;

    public static Arguments addArguments(ParameterConfig config, String projectId, String name) {
        if (config.isEffective(projectId) && config.getConfig().get(projectId).getCommonConfig() != null && CollectionUtils.isNotEmpty(config.getConfig().get(projectId).getCommonConfig().getVariables())) {
            Arguments arguments = new Arguments();
            arguments.setEnabled(true);
            arguments.setName(StringUtils.isNoneBlank(name) ? name : "Arguments");
            arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
            arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));
            config.getConfig().get(projectId).getCommonConfig().getVariables().stream().filter(ScenarioVariable::isConstantValid).filter(ScenarioVariable::isEnable).forEach(keyValue -> arguments.addArgument(keyValue.getName(), keyValue.getValue(), "="));
            if (arguments.getArguments().size() > 0) {
                return arguments;
            }
        }
        return null;
    }

    public static Map<String, EnvironmentConfig> getEnvironmentConfig(String environmentId, String projectId) {
        BaseEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(BaseEnvironmentService.class);
        ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentService.get(environmentId);
        if (environment != null && environment.getConfig() != null) {
            if (StringUtils.isEmpty(projectId)) {
                projectId = environment.getProjectId();
            }
            // 单独接口执行
            if (StringUtils.isEmpty(projectId)) {
                projectId = environment.getProjectId();
            }
            Map<String, EnvironmentConfig> map = new HashMap<>();
            map.put(projectId, JSONUtil.parseObject(environment.getConfig(), EnvironmentConfig.class));
            return map;
        }


        return null;
    }

    public static void addCsvDataSet(HashTree tree, List<ScenarioVariable> variables, ParameterConfig config, String shareMode) {
        if (CollectionUtils.isNotEmpty(variables)) {
            List<ScenarioVariable> list = variables.stream().filter(ScenarioVariable::isCSVValid).filter(ScenarioVariable::isEnable).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(list) && CollectionUtils.isNotEmpty(config.getTransferVariables())) {
                list = config.getTransferVariables().stream().filter(ScenarioVariable::isCSVValid).filter(ScenarioVariable::isEnable).collect(Collectors.toList());
            }
            if (CollectionUtils.isNotEmpty(list)) {
                FileMetadataService fileMetadataService = CommonBeanFactory.getBean(FileMetadataService.class);
                list.forEach(item -> {
                    CSVDataSet csvDataSet = new CSVDataSet();
                    csvDataSet.setEnabled(true);
                    csvDataSet.setProperty(TestElement.TEST_CLASS, CSVDataSet.class.getName());
                    csvDataSet.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
                    csvDataSet.setName(StringUtils.isEmpty(item.getName()) ? "CSVDataSet" : item.getName());
                    csvDataSet.setProperty("fileEncoding", StringUtils.isEmpty(item.getEncoding()) ? StandardCharsets.UTF_8.name() : item.getEncoding());
                    if (CollectionUtils.isEmpty(item.getFiles())) {
                        MSException.throwException(StringUtils.isEmpty(item.getName()) ? "CSVDataSet" : item.getName() + "：[ " + Translator.get("csv_no_exist") + " ]");
                    } else {
                        boolean isRef = false;
                        String fileId = null;
                        boolean isRepository = false;
                        BodyFile file = item.getFiles().get(0);
                        String path = BODY_FILE_DIR + "/" + item.getFiles().get(0).getId() + "_" + item.getFiles().get(0).getName();
                        if (StringUtils.equalsIgnoreCase(file.getStorage(), StorageConstants.FILE_REF.name())) {
                            isRef = true;
                            fileId = file.getFileId();
                            if (fileMetadataService != null) {
                                FileMetadata fileMetadata = fileMetadataService.getFileMetadataById(fileId);
                                if (fileMetadata != null && !StringUtils.equals(fileMetadata.getStorage(), StorageConstants.LOCAL.name())) {
                                    isRepository = true;
                                }
                            }
                            path = FileUtils.getFilePath(file);
                        }
                        if (!config.isOperating() && !isRepository && !new File(path).exists()) {
                            MSException.throwException(StringUtils.isEmpty(item.getName()) ? "CSVDataSet" : item.getName() + "：[ " + Translator.get("csv_no_exist") + " ]");
                        }
                        csvDataSet.setProperty("filename", path);
                        csvDataSet.setProperty("isRef", isRef);
                        csvDataSet.setProperty("fileId", fileId);
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

    public static void addApiCsvDataSet(HashTree tree, List<ScenarioVariable> variables, ParameterConfig config, String shareMode) {
        if (CollectionUtils.isNotEmpty(variables)) {
            List<ScenarioVariable> list = variables.stream().filter(ScenarioVariable::isCSVValid).filter(ScenarioVariable::isEnable).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(item -> {
                    CSVDataSet csvDataSet = new CSVDataSet();
                    csvDataSet.setEnabled(true);
                    csvDataSet.setProperty(TestElement.TEST_CLASS, CSVDataSet.class.getName());
                    csvDataSet.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TestBeanGUI"));
                    csvDataSet.setName(StringUtils.isEmpty(item.getName()) ? "CSVDataSet" : item.getName());
                    csvDataSet.setProperty("fileEncoding", StringUtils.isEmpty(item.getEncoding()) ? StandardCharsets.UTF_8.name() : item.getEncoding());
                    if (CollectionUtils.isEmpty(item.getFiles())) {
                        MSException.throwException(StringUtils.isEmpty(item.getName()) ? "CSVDataSet" : item.getName() + "：[ " + Translator.get("csv_no_exist") + " ]");
                    } else {
                        BodyFile file = item.getFiles().get(0);
                        String fileId = item.getId();
                        boolean isRef = false;
                        String path = null;
                        if (StringUtils.equalsIgnoreCase(file.getStorage(), StorageConstants.FILE_REF.name())) {
                            isRef = true;
                            fileId = file.getFileId();
                            path = FileUtils.getFilePath(file);
                        } else {
                            path = BODY_FILE_DIR + "/" + item.getFiles().get(0).getId() + "_" + item.getFiles().get(0).getName();
                            if (StringUtils.equalsIgnoreCase(file.getStorage(), StorageEnums.FILE_REF.name())) {
                                path = ApiFileUtil.getFilePath(file);
                            }
                            if (!config.isOperating() && !new File(path).exists()) {
                                MSException.throwException(StringUtils.isEmpty(item.getName()) ? "CSVDataSet" : item.getName() + "：[ " + Translator.get("csv_no_exist") + " ]");
                            }
                        }
                        csvDataSet.setProperty("filename", path);
                        csvDataSet.setProperty("isRef", isRef);
                        csvDataSet.setProperty("fileId", fileId);
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
            List<ScenarioVariable> list = variables.stream().filter(ScenarioVariable::isCounterValid).filter(ScenarioVariable::isEnable).collect(Collectors.toList());
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
            List<ScenarioVariable> list = variables.stream().filter(ScenarioVariable::isRandom).filter(ScenarioVariable::isEnable).collect(Collectors.toList());
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
            put(ElementConstants.SCENARIO, MsScenario.class.getCanonicalName());
            put(ElementConstants.HTTP_SAMPLER, MsHTTPSamplerProxy.class.getCanonicalName());
            put(ElementConstants.DUBBO_SAMPLER, MsDubboSampler.class.getCanonicalName());
            put(ElementConstants.JDBC_SAMPLER, MsJDBCSampler.class.getCanonicalName());
            put(ElementConstants.TCP_SAMPLER, MsTCPSampler.class.getCanonicalName());
            put(ElementConstants.IF_CONTROLLER, MsIfController.class.getCanonicalName());
            put(ElementConstants.TRANSACTION_CONTROLLER, MsTransactionController.class.getCanonicalName());
            put(ElementConstants.LOOP_CONTROLLER, MsLoopController.class.getCanonicalName());
            put(ElementConstants.CONSTANT_TIMER, MsConstantTimer.class.getCanonicalName());
            put(ElementConstants.JSR223, MsJSR223Processor.class.getCanonicalName());
            put(ElementConstants.JSR223_PRE, MsJSR223PreProcessor.class.getCanonicalName());
            put(ElementConstants.JSR223_POST, MsJSR223PostProcessor.class.getCanonicalName());
            put(ElementConstants.JDBC_PRE, MsJDBCPreProcessor.class.getCanonicalName());
            put(ElementConstants.JDBC_POST, MsJDBCPostProcessor.class.getCanonicalName());
            put(ElementConstants.ASSERTIONS, MsAssertions.class.getCanonicalName());
            put(ElementConstants.EXTRACT, MsExtract.class.getCanonicalName());
            put(ElementConstants.JMETER_ELE, MsJmeterElement.class.getCanonicalName());
            put(ElementConstants.TEST_PLAN, MsTestPlan.class.getCanonicalName());
            put(ElementConstants.THREAD_GROUP, MsThreadGroup.class.getCanonicalName());
            put(ElementConstants.DNS_CACHE, MsDNSCacheManager.class.getCanonicalName());
            put(ElementConstants.DEBUG_SAMPLER, MsDebugSampler.class.getCanonicalName());
            put(ElementConstants.AUTH_MANAGER, MsAuthManager.class.getCanonicalName());
        }
    };

    private static void formatSampler(JSONObject element) {
        if (element == null || !element.has(PropertyConstant.TYPE)) {
            return;
        }
        if (!element.has(ElementConstants.CLAZZ_NAME) && element.has(PropertyConstant.TYPE) && element.optString(PropertyConstant.TYPE).equals(ElementConstants.TCP_SAMPLER)) {
            if (element.has("tcpPreProcessor")) {
                JSONObject tcpPreProcessor = JSONUtil.parseObject(element.optString("tcpPreProcessor"));
                if (tcpPreProcessor != null && !tcpPreProcessor.has(ElementConstants.CLAZZ_NAME)) {
                    tcpPreProcessor.put(ElementConstants.CLAZZ_NAME, clazzMap.get(tcpPreProcessor.optString(PropertyConstant.TYPE)));
                    element.put("tcpPreProcessor", tcpPreProcessor);
                }
            }
        } else if (element.optString(PropertyConstant.TYPE).equals(ElementConstants.HTTP_SAMPLER)) {
            if (element.has("authManager")) {
                JSONObject authManager = JSONUtil.parseObject(element.optString("authManager"));
                if (authManager != null && !authManager.has(ElementConstants.CLAZZ_NAME)) {
                    authManager.put(ElementConstants.CLAZZ_NAME, clazzMap.get(authManager.optString(PropertyConstant.TYPE)));
                    element.put("authManager", authManager);
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
        for (int i = 0; i < hashTree.length(); i++) {
            JSONObject element = hashTree.optJSONObject(i);
            if (element != null && StringUtils.equals(element.get(PropertyConstant.TYPE).toString(), ElementConstants.SCENARIO) && StringUtils.equals(element.get("referenced").toString(), "REF")) {
                if (!referenceRelationships.contains(element.get("id").toString())) {
                    referenceRelationships.add(element.get("id").toString());
                }
            } else {
                if (element.has(ElementConstants.HASH_TREE)) {
                    JSONArray elementJSONArray = element.optJSONArray(ElementConstants.HASH_TREE);
                    relationships(elementJSONArray, referenceRelationships);
                }
            }
        }
    }

    public static void dataFormatting(JSONArray hashTree) {
        for (int i = 0; i < hashTree.length(); i++) {
            JSONObject element = hashTree.optJSONObject(i);
            if (element == null) {
                continue;
            }
            formatSampler(element);
            if (!element.has(ElementConstants.CLAZZ_NAME) && clazzMap.containsKey(element.optString(PropertyConstant.TYPE))) {
                element.put(ElementConstants.CLAZZ_NAME, clazzMap.get(element.optString(PropertyConstant.TYPE)));
            }
            if (element.has(ElementConstants.HASH_TREE)) {
                JSONArray elementJSONArray = element.optJSONArray(ElementConstants.HASH_TREE);
                dataFormatting(elementJSONArray);
            }
        }
    }

    public static void dataFormatting(JSONObject element) {
        if (element != null && !element.has(ElementConstants.CLAZZ_NAME) && (element.has(PropertyConstant.TYPE) && clazzMap.containsKey(element.optString(PropertyConstant.TYPE)))) {
            element.put(ElementConstants.CLAZZ_NAME, clazzMap.get(element.optString(PropertyConstant.TYPE)));
        }
        formatSampler(element);
        if (element != null && element.has(ElementConstants.HASH_TREE)) {
            JSONArray elementJSONArray = element.optJSONArray(ElementConstants.HASH_TREE);
            dataFormatting(elementJSONArray);
        }
    }

    public static void dataSetDomain(JSONArray hashTree, MsParameter msParameter) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            for (int i = 0; i < hashTree.length(); i++) {
                JSONObject element = hashTree.optJSONObject(i);
                boolean isScenarioEnv = false;
                ParameterConfig config = new ParameterConfig();
                if (element != null && element.get(PropertyConstant.TYPE).toString().equals(ElementConstants.SCENARIO)) {
                    MsScenario scenario = JSON.parseObject(element.toString(), MsScenario.class);
                    if (scenario.isEnvironmentEnable()) {
                        isScenarioEnv = true;
                        Map<String, String> environmentMap = new HashMap<>();
                        ApiScenarioMapper apiScenarioMapper = CommonBeanFactory.getBean(ApiScenarioMapper.class);
                        BaseEnvGroupProjectService environmentGroupProjectService = CommonBeanFactory.getBean(BaseEnvGroupProjectService.class);
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
                                BaseEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(BaseEnvironmentService.class);
                                ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentService.get(finalEnvironmentMap.get(projectId));
                                if (environment != null && environment.getConfig() != null) {
                                    EnvironmentConfig env = JSONUtil.parseObject(environment.getConfig(), EnvironmentConfig.class);
                                    env.setEnvironmentId(environment.getId());
                                    envConfig.put(projectId, env);
                                }
                            });
                            config.setConfig(envConfig);
                        }
                    }
                } else if (element != null && element.get(PropertyConstant.TYPE).toString().equals(ElementConstants.HTTP_SAMPLER)) {
                    MsHTTPSamplerProxy httpSamplerProxy = JSON.parseObject(element.toString(), MsHTTPSamplerProxy.class);
                    if (httpSamplerProxy != null && (!httpSamplerProxy.isCustomizeReq() || (httpSamplerProxy.isCustomizeReq() && BooleanUtils.isTrue(httpSamplerProxy.getIsRefEnvironment())))) {
                        // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
                        if (element != null && element.has(ElementConstants.HASH_TREE)) {
                            LinkedList<MsTestElement> elements = mapper.readValue(element.optString(ElementConstants.HASH_TREE), new TypeReference<LinkedList<MsTestElement>>() {
                            });
                            httpSamplerProxy.setHashTree(elements);
                        }
                        HashTree tmpHashTree = new HashTree();
                        httpSamplerProxy.toHashTree(tmpHashTree, null, msParameter);
                        if (tmpHashTree != null && tmpHashTree.getArray().length > 0) {
                            HTTPSamplerProxy object = (HTTPSamplerProxy) tmpHashTree.getArray()[0];
                            // 清空Domain
                            element.put("domain", "");
                            if (object != null && StringUtils.isNotEmpty(object.getDomain())) {
                                element.put("domain", StringUtils.isNotEmpty(object.getProtocol()) ? object.getProtocol() + "://" + object.getDomain() : object.getDomain());
                            }
                        }
                    }
                }
                if (element.has(ElementConstants.HASH_TREE)) {
                    JSONArray elementJSONArray = element.optJSONArray(ElementConstants.HASH_TREE);
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
                    if (StringUtils.isNotEmpty(item.optString("id"))) {
                        updateMap.put(item.optString("id"), item);
                    }
                }
            }
            // 找出待更新内容和源已经被删除的内容
            if (CollectionUtils.isNotEmpty(sourceHashTree)) {
                for (int i = 0; i < sourceHashTree.size(); i++) {
                    JSONObject source = sourceHashTree.get(i);
                    if (source != null) {
                        sourceIds.add(source.optString("id"));
                        if (!StringUtils.equals(source.optString("label"), "SCENARIO-REF-STEP") && StringUtils.isNotEmpty(source.optString("id"))) {
                            if (updateMap.containsKey(source.optString("id"))) {
                                sourceHashTree.set(i, updateMap.get(source.optString("id")));
                            } else {
                                delIds.add(source.optString("id"));
                            }
                        }
                        // 历史数据兼容
                        if (!source.has("id") && !StringUtils.equals(source.optString("label"), "SCENARIO-REF-STEP") && i < targetHashTree.size()) {
                            sourceHashTree.set(i, targetHashTree.get(i));
                        }
                    }
                }
            }

            // 删除多余的步骤
            for (int i = 0; i < sourceHashTree.size(); i++) {
                JSONObject source = sourceHashTree.get(i);
                if (delIds.contains(source.optString("id"))) {
                    sourceHashTree.remove(i);
                }
            }
            // 补充新增的源引用步骤
            if (CollectionUtils.isNotEmpty(targetHashTree)) {
                for (int i = 0; i < targetHashTree.size(); i++) {
                    JSONObject item = sourceHashTree.get(i);
                    if (!sourceIds.contains(item.optString("id"))) {
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
        // 跳过失败重试层
        if (parent != null && StringUtils.equalsAnyIgnoreCase("RetryLoopController", parent.getType())) {
            parent = parent.getParent();
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
                script.append("vars.put(\"" + arguments.getArgument(i).getName() + "\",\"" + argValue + "\");").append(StringUtils.LF);
            }
            processor.setProperty("script", script.toString());
        }
        return processor;
    }

    public static UserParameters argumentsToUserParameters(Arguments arguments) {
        UserParameters processor = new UserParameters();
        processor.setEnabled(true);
        processor.setName("User Defined Variables");
        processor.setPerIteration(true);
        processor.setProperty(TestElement.TEST_CLASS, UserParameters.class.getName());
        processor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("UserParametersGui"));
        if (arguments != null && arguments.getArguments().size() > 0) {
            List<String> names = new LinkedList<>();
            List<Object> values = new LinkedList<>();
            List<Object> threadValues = new LinkedList<>();
            for (int i = 0; i < arguments.getArguments().size(); ++i) {
                String argValue = arguments.getArgument(i).getValue();
                String name = arguments.getArgument(i).getName();
                names.add(name);
                values.add(argValue);
            }
            processor.setNames(names);
            threadValues.add(values);
            processor.setThreadLists(threadValues);
        }
        return processor;
    }

    public static void setBaseParams(AbstractTestElement sampler, MsTestElement parent, ParameterConfig config, String id, String indexPath) {
        sampler.setProperty("MS-ID", id);
        sampler.setProperty("MS-RESOURCE-ID", ElementUtil.getResourceId(id, config, parent, indexPath));
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
        this.add(ElementConstants.JSR223_PRE);
        this.add(ElementConstants.JDBC_PRE);
        this.add(ElementConstants.CONSTANT_TIMER);
    }};
    private static final List<String> postOperates = new ArrayList<String>() {{
        this.add(ElementConstants.JSR223_POST);
        this.add(ElementConstants.JDBC_POST);
        this.add(ElementConstants.EXTRACT);
    }};


    public static void copyBean(JSONObject target, JSONObject source) {
        if (source == null || target == null) {
            return;
        }
        for (String key : target.keySet()) {
            if (source.has(key) && !StringUtils.equalsIgnoreCase(key, ElementConstants.HASH_TREE)) {
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
        }
        return list;
    }

    public static Map<String, List<JSONObject>> group(JSONArray elements) {
        Map<String, List<JSONObject>> groupMap = new LinkedHashMap<>();
        if (elements != null) {
            for (int i = 0; i < elements.length(); i++) {
                JSONObject item = elements.optJSONObject(i);
                if (ElementConstants.ASSERTIONS.equals(item.optString(PropertyConstant.TYPE))) {
                    if (groupMap.containsKey(ASSERTIONS)) {
                        groupMap.get(ASSERTIONS).add(item);
                    } else {
                        groupMap.put(ASSERTIONS, new LinkedList<JSONObject>() {{
                            this.add(item);
                        }});
                    }
                } else if (preOperates.contains(item.optString(PropertyConstant.TYPE))) {
                    if (groupMap.containsKey(PRE)) {
                        groupMap.get(PRE).add(item);
                    } else {
                        groupMap.put(PRE, new LinkedList<JSONObject>() {{
                            this.add(item);
                        }});
                    }
                } else if (postOperates.contains(item.optString(PropertyConstant.TYPE))) {
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
                if (ElementConstants.ASSERTIONS.equals(item.getType())) {
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

    public static String getEvlValue(String evlValue) {
        try {
            if (StringUtils.startsWith(evlValue, "@")) {
                return ScriptEngineUtils.calculate(evlValue);
            } else {
                return ScriptEngineUtils.buildFunctionCallString(evlValue);
            }
        } catch (Exception e) {
            return evlValue;
        }
    }

    public static Arguments getConfigArguments(ParameterConfig config, String name, String projectId, List<ScenarioVariable> variables) {
        Arguments arguments = new Arguments();
        arguments.setEnabled(true);
        arguments.setName(StringUtils.isNotEmpty(name) ? name : "Arguments");
        arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
        arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));

        // 场景变量
        if (CollectionUtils.isNotEmpty(variables)) {
            variables.stream()
                    .filter(ScenarioVariable::isConstantValid).forEach(keyValue ->
                            arguments.addArgument(keyValue.getName(),
                                    keyValue.getValue(), "="));
            List<ScenarioVariable> variableList = variables.stream()
                    .filter(ScenarioVariable::isListValid).collect(Collectors.toList());
            variableList.forEach(item -> {
                String[] arrays = item.getValue().split(",");
                for (int i = 0; i < arrays.length; i++) {
                    arguments.addArgument(item.getName() + "_" + (i + 1), arrays[i], "=");
                }
            });
        }
        // 环境通用变量
        if (config.isEffective(projectId)
                && config.getConfig().get(projectId).getCommonConfig() != null
                && CollectionUtils.isNotEmpty(config.getConfig().get(projectId).getCommonConfig().getVariables())) {
            //常量
            List<ScenarioVariable> constants = config.getConfig().get(projectId).getCommonConfig().getVariables().stream()
                    .filter(ScenarioVariable::isConstantValid)
                    .filter(ScenarioVariable::isEnable)
                    .collect(Collectors.toList());
            constants.forEach(keyValue ->
                    arguments.addArgument(keyValue.getName(),
                            keyValue.getValue() != null && keyValue.getValue().startsWith("@")
                                    ? ScriptEngineUtils.buildFunctionCallString(keyValue.getValue())
                                    : keyValue.getValue(), "="));
            // List类型的变量
            List<ScenarioVariable> variableList = config.getConfig().get(projectId).getCommonConfig().getVariables().stream()
                    .filter(ScenarioVariable::isListValid)
                    .filter(ScenarioVariable::isEnable)
                    .collect(Collectors.toList());
            variableList.forEach(item -> {
                String[] arrays = item.getValue().split(",");
                for (int i = 0; i < arrays.length; i++) {
                    arguments.addArgument(item.getName() + "_" + (i + 1), arrays[i], "=");
                }
            });
            // 清空变量，防止重复添加
            config.getConfig().get(projectId).getCommonConfig().getVariables().removeAll(constants);
            config.getConfig().get(projectId).getCommonConfig().getVariables().removeAll(variableList);
        }

        if (arguments.getArguments() != null && arguments.getArguments().size() > 0) {
            return arguments;
        }
        return null;
    }

    public static void addApiVariables(ParameterConfig config, HashTree httpSamplerTree, String projectId) {
        if (config.isEffective(projectId) && config.getConfig().get(projectId).getCommonConfig() != null && CollectionUtils.isNotEmpty(config.getConfig().get(projectId).getCommonConfig().getVariables())) {
            ElementUtil.addApiCsvDataSet(httpSamplerTree,
                    config.getConfig().get(projectId).getCommonConfig().getVariables(),
                    config, "shareMode.group");
            ElementUtil.addCounter(httpSamplerTree,
                    config.getConfig().get(projectId).getCommonConfig().getVariables(), false);
            ElementUtil.addRandom(httpSamplerTree,
                    config.getConfig().get(projectId).getCommonConfig().getVariables());
        }
    }

    public static DatabaseConfig dataSource(String projectId, String dataSourceId, EnvironmentConfig envConfig) {
        try {
            BaseEnvironmentService environmentService = CommonBeanFactory.getBean(BaseEnvironmentService.class);
            List<ApiTestEnvironmentWithBLOBs> environment = environmentService.list(projectId);
            EnvironmentConfig dataConfig = null;
            List<String> dataName = new ArrayList<>();
            List<ApiTestEnvironmentWithBLOBs> orgDataSource = environment.stream()
                    .filter(ApiTestEnvironmentWithBLOBs ->
                            ApiTestEnvironmentWithBLOBs.getConfig().contains(dataSourceId)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(orgDataSource)) {
                dataConfig = JSONUtil.parseObject(orgDataSource.get(0).getConfig(), EnvironmentConfig.class);
                if (CollectionUtils.isNotEmpty(dataConfig.getDatabaseConfigs())) {
                    dataName = dataConfig.getDatabaseConfigs().stream()
                            .filter(DatabaseConfig ->
                                    DatabaseConfig.getId().equals(dataSourceId))
                            .map(DatabaseConfig::getName)
                            .collect(Collectors.toList());
                }
            }
            List<String> finalDataName = dataName;
            List<DatabaseConfig> collect = envConfig.getDatabaseConfigs().stream()
                    .filter(DatabaseConfig ->
                            DatabaseConfig.getName().equals(finalDataName.get(0)))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
                return collect.get(0);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }

    public static void replaceFileMetadataId(MsTestElement testElement, String newFileMetadataId, String oldFileMetadataId) {
        if (testElement != null && testElement instanceof MsHTTPSamplerProxy) {
            if (((MsHTTPSamplerProxy) testElement).getBody() != null && CollectionUtils.isNotEmpty(((MsHTTPSamplerProxy) testElement).getBody().getKvs())) {
                for (KeyValue keyValue : ((MsHTTPSamplerProxy) testElement).getBody().getKvs()) {
                    if (CollectionUtils.isNotEmpty(keyValue.getFiles())) {
                        for (BodyFile bodyFile : keyValue.getFiles()) {
                            if (StringUtils.equals(bodyFile.getFileId(), oldFileMetadataId)) {
                                bodyFile.setFileId(newFileMetadataId);
                            }
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(testElement.getHashTree())) {
                for (MsTestElement childElement : testElement.getHashTree()) {
                    replaceFileMetadataId(childElement, newFileMetadataId, oldFileMetadataId);

                }
            }
        }
    }

    public static List<MsAssertions> copyAssertion(List<EnvAssertions> envAssertions) {
        List<MsAssertions> assertions = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(envAssertions)) {
            envAssertions.forEach(item -> {
                if (item != null) {
                    MsAssertions env = new MsAssertions();
                    BeanUtils.copyBean(env, item);
                    assertions.add(env);
                }
            });
        }
        return assertions;
    }
}
