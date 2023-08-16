package io.metersphere.api.dto.definition.request;

import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.RunningParamKeys;
import io.metersphere.api.dto.definition.request.assertions.MsAssertions;
import io.metersphere.api.dto.definition.request.auth.MsAuthManager;
import io.metersphere.api.dto.definition.request.controller.MsIfController;
import io.metersphere.api.dto.definition.request.controller.MsLoopController;
import io.metersphere.api.dto.definition.request.controller.MsRetryLoopController;
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
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.MsHashTreeConstants;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.constants.StorageConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.commons.vo.JDBCProcessorVO;
import io.metersphere.commons.vo.ScriptProcessorVO;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.i18n.Translator;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.plugin.core.MsParameter;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.request.BodyFile;
import io.metersphere.service.MsHashTreeService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.RandomVariableConfig;
import org.apache.jmeter.modifiers.CounterConfig;
import org.apache.jmeter.modifiers.UserParameters;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.java.sampler.BeanShellSampler;
import org.apache.jmeter.protocol.jdbc.AbstractJDBCTestElement;
import org.apache.jmeter.protocol.jdbc.config.DataSourceElement;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ElementUtil {
    private static final String PRE = "PRE";
    private static final String POST = "POST";
    private static final String ASSERTIONS = ElementConstants.ASSERTIONS;
    private static final String BODY_FILE_DIR = FileUtils.BODY_FILE_DIR;
    private static final String TEST_BEAN_GUI = "TestBeanGUI";
    private final static String SCENARIO_REF = "SCENARIO-REF-STEP";
    private final static String MS_DEFAULT = "ms-default-data-source";
    public final static List<String> scriptList = new ArrayList<String>() {{
        this.add(ElementConstants.JSR223);
        this.add(ElementConstants.JSR223_PRE);
        this.add(ElementConstants.JSR223_POST);
    }};
    public static final String JSR = "jsr223";
    public static final String CLAZZ = "clazzName";


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
                addCsv(tree, config, shareMode, list);
            }
        }
    }

    private static void addCsv(HashTree tree, ParameterConfig config, String shareMode, List<ScenarioVariable> list) {
        FileMetadataService fileMetadataService = CommonBeanFactory.getBean(FileMetadataService.class);
        list.forEach(item -> {
            CSVDataSet csvDataSet = new CSVDataSet();
            csvDataSet.setEnabled(true);
            csvDataSet.setProperty(TestElement.TEST_CLASS, CSVDataSet.class.getName());
            csvDataSet.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(TEST_BEAN_GUI));
            csvDataSet.setName(StringUtils.isEmpty(item.getName()) ? "CSVDataSet" : item.getName());
            csvDataSet.setProperty("fileEncoding", StringUtils.isEmpty(item.getEncoding()) ? StandardCharsets.UTF_8.name() : item.getEncoding());
            if (CollectionUtils.isEmpty(item.getFiles())) {
                MSException.throwException(StringUtils.isEmpty(item.getName()) ? "CSVDataSet" : item.getName() + "：[ " + Translator.get("csv_no_exist") + " ]");
            } else {
                String fileId = null;
                boolean isRepository = false;
                BodyFile file = item.getFiles().get(0);
                boolean isRef = StringUtils.equalsIgnoreCase(file.getStorage(), StorageConstants.FILE_REF.name());
                String path = StringUtils.join(BODY_FILE_DIR, File.separator, item.getFiles().get(0).getId(), "_", item.getFiles().get(0).getName());
                if (StringUtils.equalsIgnoreCase(file.getStorage(), StorageConstants.FILE_REF.name())) {
                    fileId = file.getFileId();
                    FileMetadata fileMetadata = fileMetadataService.getFileMetadataById(fileId);
                    if (fileMetadata != null
                            && !StringUtils.equals(fileMetadata.getStorage(), StorageConstants.LOCAL.name())) {
                        isRepository = true;
                    }
                    path = FileUtils.getFilePath(file);
                }
                if (!config.isOperating() && !isRepository && !new File(path).exists()) {
                    // 从MinIO下载
                    ApiFileUtil.downloadFile(file.getId(), path);
                    if (!new File(path).exists()) {
                        MSException.throwException(StringUtils.isEmpty(item.getName()) ? "CSVDataSet" : item.getName() + "：[ " + Translator.get("csv_no_exist") + " ]");
                    }
                }
                csvDataSet.setProperty(ElementConstants.FILENAME, path);
                csvDataSet.setProperty(ElementConstants.IS_REF, isRef);
                csvDataSet.setProperty(ElementConstants.FILE_ID, fileId);
                csvDataSet.setProperty(ElementConstants.RESOURCE_ID, file.getId());
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

    public static void addApiCsvDataSet(HashTree tree, List<ScenarioVariable> variables, ParameterConfig config, String shareMode) {
        if (CollectionUtils.isNotEmpty(variables)) {
            List<ScenarioVariable> list = variables.stream().filter(ScenarioVariable::isCSVValid).filter(ScenarioVariable::isEnable).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                addCsv(tree, config, shareMode, list);
            }
        }
    }

    public static void addCounter(HashTree tree, List<ScenarioVariable> variables) {
        if (CollectionUtils.isNotEmpty(variables)) {
            List<ScenarioVariable> list = variables.stream().filter(ScenarioVariable::isCounterValid).filter(ScenarioVariable::isEnable).collect(Collectors.toList());
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
            List<ScenarioVariable> list = variables.stream().filter(ScenarioVariable::isRandom).filter(ScenarioVariable::isEnable).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(list)) {
                list.forEach(item -> {
                    RandomVariableConfig randomVariableConfig = new RandomVariableConfig();
                    randomVariableConfig.setEnabled(true);
                    randomVariableConfig.setProperty(TestElement.TEST_CLASS, RandomVariableConfig.class.getName());
                    randomVariableConfig.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(TEST_BEAN_GUI));
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

    public static String getFullIndexPath(MsTestElement element, String path) {
        if (element == null || element.getParent() == null) {
            return path;
        }
        path = element.getIndex() + "_" + path;
        return getFullIndexPath(element.getParent(), path);
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

    public static void dataSetDomain(JSONArray hashTree, ParameterConfig msParameter) {
        try {
            ApiScenarioMapper apiScenarioMapper = CommonBeanFactory.getBean(ApiScenarioMapper.class);
            BaseEnvGroupProjectService environmentGroupProjectService = CommonBeanFactory.getBean(BaseEnvGroupProjectService.class);
            BaseEnvironmentService apiTestEnvironmentService = CommonBeanFactory.getBean(BaseEnvironmentService.class);

            for (int i = 0; i < hashTree.length(); i++) {
                JSONObject element = hashTree.optJSONObject(i);
                boolean isScenarioEnv = false;
                ParameterConfig config = new ParameterConfig(msParameter.getCurrentProjectId(), false);
                if (element != null && element.get(PropertyConstant.TYPE).toString().equals(ElementConstants.SCENARIO)) {
                    MsScenario scenario = JSON.parseObject(element.toString(), MsScenario.class);
                    if (scenario.isEnvironmentEnable()) {
                        isScenarioEnv = true;
                        Map<String, String> environmentMap = new HashMap<>();
                        ApiScenarioWithBLOBs apiScenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(scenario.getId());
                        if (apiScenarioWithBLOBs == null) {
                            continue;
                        }
                        if (StringUtils.equals(apiScenarioWithBLOBs.getEnvironmentType(), EnvironmentType.GROUP.name())) {
                            environmentMap = environmentGroupProjectService.getEnvMap(apiScenarioWithBLOBs.getEnvironmentGroupId());
                        } else if (StringUtils.equals(apiScenarioWithBLOBs.getEnvironmentType(), EnvironmentType.JSON.name())) {
                            environmentMap = JSON.parseObject(apiScenarioWithBLOBs.getEnvironmentJson(), Map.class);
                        }
                        Map<String, EnvironmentConfig> envConfig = new HashMap<>();
                        if (environmentMap != null && !environmentMap.isEmpty()) {
                            for (String projectId : environmentMap.keySet()) {
                                ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentService.get(environmentMap.get(projectId));
                                if (environment != null && environment.getConfig() != null) {
                                    EnvironmentConfig env = JSONUtil.parseObject(environment.getConfig(), EnvironmentConfig.class);
                                    env.setEnvironmentId(environment.getId());
                                    envConfig.put(projectId, env);
                                }
                            }
                            config.setConfig(envConfig);
                        }
                    }
                } else if (element != null && element.get(PropertyConstant.TYPE).toString().equals(ElementConstants.HTTP_SAMPLER)) {
                    setDomain(element, msParameter);
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

    public static void setDomain(JSONObject element, MsParameter msParameter) {
        if (!StringUtils.equals(element.optString(MsHashTreeService.TYPE), ElementConstants.HTTP_SAMPLER) || !element.has(CLAZZ)) {
            return;
        }
        MsHTTPSamplerProxy httpSamplerProxy = JSON.parseObject(element.toString(), MsHTTPSamplerProxy.class);
        ParameterConfig config = (ParameterConfig) msParameter;
        if (httpSamplerProxy != null
                && (!httpSamplerProxy.isCustomizeReq() || BooleanUtils.isTrue(httpSamplerProxy.getIsRefEnvironment()))
                && MapUtils.isNotEmpty(config.getConfig())) {
            try {
                if (element.has(ElementConstants.HASH_TREE)) {
                    httpSamplerProxy.setHashTree(JSONUtil.readValue(element.optString(ElementConstants.HASH_TREE)));
                }
                HashTree testPlan = new HashTree();
                httpSamplerProxy.toHashTree(testPlan, null, msParameter);
                if (testPlan.getArray().length > 0) {
                    HTTPSamplerProxy object = (HTTPSamplerProxy) testPlan.getArray()[0];
                    if (object != null && StringUtils.isNotEmpty(object.getDomain())) {
                        element.put("domain", StringUtils.isNotEmpty(object.getProtocol()) ?
                                object.getProtocol() + "://" + object.getDomain() : object.getDomain());
                    }
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
    }

    public static void mergeHashTree(MsTestElement element, LinkedList<MsTestElement> targetHashTree) {
        try {
            Map<String, LinkedList<MsTestElement>> source = groupCase(element.getHashTree());
            Map<String, LinkedList<MsTestElement>> target = groupCase(targetHashTree);
            List<MsTestElement> step = new LinkedList<>();
            List<MsTestElement> pre = ElementUtil.mergeCaseHashTree(source.get(PRE), target.get(PRE));
            List<MsTestElement> post = ElementUtil.mergeCaseHashTree(source.get(POST), target.get(POST));
            List<MsTestElement> rules = MsHashTreeService.mergeCaseAssertions(source.get(ASSERTIONS), target.get(ASSERTIONS));
            if (CollectionUtils.isNotEmpty(pre)) {
                step.addAll(pre);
            }
            if (CollectionUtils.isNotEmpty(post)) {
                step.addAll(post);
            }
            if (CollectionUtils.isNotEmpty(rules)) {
                step.addAll(rules);
            }
            element.getHashTree().clear();
            element.getHashTree().addAll(step);
        } catch (Exception e) {
            element.setHashTree(targetHashTree);
        }
    }

    public static Map<String, LinkedList<MsTestElement>> groupCase(List<MsTestElement> elements) {
        Map<String, LinkedList<MsTestElement>> groupMap = new LinkedHashMap<>();
        if (elements != null) {
            for (int i = 0; i < elements.size(); i++) {
                MsTestElement item = elements.get(i);
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
                }
            }
        }
        return groupMap;
    }

    public static List<MsTestElement> mergeCaseHashTree(List<MsTestElement> targets, List<MsTestElement> sources) {
        try {
            List<String> sourceIds = new ArrayList<>();
            List<String> delIds = new ArrayList<>();
            Map<String, MsTestElement> updateMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(sources)) {
                for (int i = 0; i < sources.size(); i++) {
                    MsTestElement msTestElement = sources.get(i);
                    if (StringUtils.isNotEmpty(msTestElement.getId())) {
                        updateMap.put(msTestElement.getId(), msTestElement);
                    }
                }
            }
            // 找出待更新内容和源已经被删除的内容
            if (CollectionUtils.isNotEmpty(targets)) {
                for (int i = 0; i < targets.size(); i++) {
                    MsTestElement msTestElement = targets.get(i);
                    if (msTestElement == null) {
                        continue;
                    }
                    sourceIds.add(msTestElement.getId());
                    if (!StringUtils.equals(msTestElement.getLabel(), SCENARIO_REF)
                            && StringUtils.isNotEmpty(msTestElement.getId())) {
                        if (updateMap.containsKey(msTestElement.getId())) {
                            targets.set(i, updateMap.get(msTestElement.getId()));
                            updateMap.remove(msTestElement.getId());
                        } else {
                            delIds.add(msTestElement.getId());
                        }
                    }
                }
            }

            // 删除多余的步骤 delIds中包含的全都干掉
            targets.removeIf(msTestElement -> delIds.contains(msTestElement.getId()));

            // 补充新增的源引用步骤
            if (CollectionUtils.isNotEmpty(sources)) {
                for (int i = 0; i < sources.size(); i++) {
                    MsTestElement msTestElement = sources.get(i);
                    if (!sourceIds.contains(msTestElement.getId())) {
                        targets.add(msTestElement);
                    }
                }
            }
        } catch (Exception e) {
            return sources;
        }
        //根据index排序
        targets.sort(Comparator.comparing(MsTestElement::getIndex));
        return targets;
    }

    public static List<JSONObject> mergeHashTree(List<JSONObject> targets, List<JSONObject> sources) {
        try {
            List<String> sourceIds = new ArrayList<>();
            List<String> delIds = new ArrayList<>();
            Map<String, JSONObject> updateMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(sources)) {
                for (int i = 0; i < sources.size(); i++) {
                    JSONObject item = sources.get(i);
                    item.put(MsHashTreeConstants.DISABLED, true);
                    item.put(ElementConstants.REF_ENABLE, true);
                    if (StringUtils.isNotEmpty(item.optString(ElementConstants.ID))) {
                        updateMap.put(item.optString(ElementConstants.ID), item);
                    }
                }
            }
            // 找出待更新内容和源已经被删除的内容
            if (CollectionUtils.isNotEmpty(targets)) {
                for (int i = 0; i < targets.size(); i++) {
                    JSONObject object = targets.get(i);
                    if (object == null) {
                        continue;
                    }
                    sourceIds.add(object.optString(ElementConstants.ID));
                    if (!StringUtils.equals(object.optString("label"), SCENARIO_REF)
                            && StringUtils.isNotEmpty(object.optString(ElementConstants.ID))) {
                        if (updateMap.containsKey(object.optString(ElementConstants.ID))) {
                            targets.set(i, updateMap.get(object.optString(ElementConstants.ID)));
                            updateMap.remove(object.optString(ElementConstants.ID));
                        } else {
                            delIds.add(object.optString(ElementConstants.ID));
                        }
                    }
                    // 历史数据兼容
                    if (!object.has(ElementConstants.ID)
                            && !StringUtils.equals(object.optString("label"), SCENARIO_REF)
                            && i < sources.size()) {
                        targets.set(i, sources.get(i));
                    }
                }
            }

            // 删除多余的步骤
            targets.removeIf(msTestElement -> delIds.contains(msTestElement.optString(ElementConstants.ID)));

            // 补充新增的源引用步骤
            if (CollectionUtils.isNotEmpty(sources)) {
                for (int i = 0; i < sources.size(); i++) {
                    JSONObject item = sources.get(i);
                    if (!sourceIds.contains(item.optString(ElementConstants.ID))) {
                        targets.add(item);
                    }
                }
            }
        } catch (Exception e) {
            return sources;
        }
        return targets;
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

    public static void setBaseParams(TestElement sampler, MsTestElement parent, ParameterConfig config, String id, String indexPath) {
        sampler.setProperty("MS-ID", id);
        sampler.setProperty("MS-RESOURCE-ID", ElementUtil.getResourceId(id, config, parent, indexPath));
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
                                    StringUtils.isNotBlank(keyValue.getValue())
                                            ? keyValue.getValue().replaceAll("[\r\n]", "") : keyValue.getValue(), "="));
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
                && config.get(projectId).getCommonConfig() != null
                && CollectionUtils.isNotEmpty(config.get(projectId).getCommonConfig().getVariables())) {
            //常量
            List<ScenarioVariable> constants = config.get(projectId).getCommonConfig().getVariables().stream()
                    .filter(ScenarioVariable::isConstantValid)
                    .filter(ScenarioVariable::isEnable)
                    .collect(Collectors.toList());
            constants.forEach(keyValue ->
                    arguments.addArgument(keyValue.getName(),
                            keyValue.getValue() != null && keyValue.getValue().startsWith("@")
                                    ? ScriptEngineUtils.buildFunctionCallString(keyValue.getValue())
                                    : (StringUtils.isNotBlank(keyValue.getValue())
                                    ? keyValue.getValue().replaceAll("[\r\n]", "")
                                    : keyValue.getValue()), "="));
            // List类型的变量
            List<ScenarioVariable> variableList = config.get(projectId).getCommonConfig().getVariables().stream()
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
            config.get(projectId).getCommonConfig().getVariables().removeAll(constants);
            config.get(projectId).getCommonConfig().getVariables().removeAll(variableList);
        }

        if (arguments.getArguments() != null && arguments.getArguments().size() > 0) {
            return arguments;
        }
        return null;
    }

    public static void addApiVariables(ParameterConfig config, HashTree httpSamplerTree, String projectId) {
        if (config.isEffective(projectId) && config.get(projectId).getCommonConfig() != null && CollectionUtils.isNotEmpty(config.get(projectId).getCommonConfig().getVariables())) {
            ElementUtil.addApiCsvDataSet(httpSamplerTree,
                    config.get(projectId).getCommonConfig().getVariables(),
                    config, "shareMode.group");
            ElementUtil.addCounter(httpSamplerTree,
                    config.get(projectId).getCommonConfig().getVariables());
            ElementUtil.addRandom(httpSamplerTree,
                    config.get(projectId).getCommonConfig().getVariables());
        }
    }

    public static DatabaseConfig dataSource(String projectId, String dataSourceId, EnvironmentConfig envConfig) {
        try {
            if (StringUtils.isBlank(dataSourceId)) {
                return null;
            }
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

    public static String getDataSourceName(String name) {
        return StringUtils.isNotBlank(name) ? name : "default-pool";
    }


    public static void initScript(TestElement testElement, ScriptProcessorVO vo) {
        testElement.setEnabled(vo.isEnabled());
        if (StringUtils.isNotEmpty(vo.getName())) {
            testElement.setName(vo.getName());
        } else {
            testElement.setName(testElement.getClass().getSimpleName());
        }
        //替换环境变量
        if (StringUtils.isNotEmpty(vo.getScript())) {
            vo.setScript(StringUtils.replace(vo.getScript(), RunningParamKeys.API_ENVIRONMENT_ID, "\"" + RunningParamKeys.RUNNING_PARAMS_PREFIX + vo.getEnvironmentId() + ".\""));
        }
        testElement.setProperty("cacheKey", false);
        testElement.setProperty(TestElement.TEST_CLASS, testElement.getClass().getSimpleName());
        testElement.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(TEST_BEAN_GUI));
        String scriptLanguage = vo.getScriptLanguage();
        if (StringUtils.equals(scriptLanguage, "nashornScript")) {
            scriptLanguage = "nashorn";
        }
        if (StringUtils.equalsAny(scriptLanguage, "rhinoScript", "javascript")) {
            scriptLanguage = "rhino";
        }
        testElement.setProperty("scriptLanguage", scriptLanguage);

        if (testElement instanceof BeanShellSampler) {
            testElement.setProperty("BeanShellSampler.query", vo.getScript());
            testElement.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("BeanShellSamplerGui"));
        } else {
            testElement.setProperty("scriptLanguage", vo.getScriptLanguage());
            testElement.setProperty(ElementConstants.SCRIPT, vo.getScript());
        }
    }

    public static String getScriptEnv(String environmentId, ParameterConfig config, String projectId) {
        if (StringUtils.isEmpty(environmentId)) {
            if (config.getConfig() != null) {
                if (StringUtils.isNotBlank(projectId) && config.getConfig().containsKey(projectId)) {
                    return config.get(projectId).getEnvironmentId();
                } else {
                    if (CollectionUtils.isNotEmpty(config.getConfig().values())) {
                        Optional<EnvironmentConfig> values = config.getConfig().entrySet().stream().findFirst().map(Map.Entry::getValue);
                        return values.get().getEnvironmentId();
                    }
                }
            }
        }
        return environmentId;
    }

    public static void jdbcArguments(String name, List<KeyValue> variables, HashTree tree) {
        if (CollectionUtils.isNotEmpty(variables)) {
            Arguments arguments = new Arguments();
            arguments.setEnabled(true);
            name = StringUtils.isNotEmpty(name) ? name : "Arguments";
            arguments.setName(name + "JDBC_Argument");
            arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
            arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));
            variables.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
                    arguments.addArgument(keyValue.getName(), ElementUtil.getEvlValue(keyValue.getValue()), "=")
            );
            if (!arguments.getArguments().isEmpty()) {
                tree.add(arguments);
            }
        }
    }

    public static void jdbcProcessor(AbstractJDBCTestElement jdbcProcessor, ParameterConfig config, JDBCProcessorVO vo) {
        jdbcProcessor.setEnabled(vo.isEnable());
        jdbcProcessor.setName(vo.getName() == null ? jdbcProcessor.getClass().getSimpleName() : vo.getName());
        jdbcProcessor.setProperty(TestElement.TEST_CLASS, jdbcProcessor.getClass().getSimpleName());
        jdbcProcessor.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(TEST_BEAN_GUI));

        ElementUtil.setBaseParams(jdbcProcessor, vo.getParent(), config, vo.getId(), vo.getIndex());
        if (ObjectUtils.isNotEmpty(vo.getDataSource())) {
            jdbcProcessor.setDataSource(ElementUtil.getDataSourceName(vo.getDataSource().getName()));
        }
        jdbcProcessor.setProperty("dataSource", jdbcProcessor.getDataSource());
        jdbcProcessor.setProperty("query", vo.getQuery());
        jdbcProcessor.setProperty("queryTimeout", String.valueOf(vo.getQueryTimeout()));
        jdbcProcessor.setProperty("resultVariable", vo.getResultVariable());
        jdbcProcessor.setProperty("variableNames", vo.getVariableNames());
        jdbcProcessor.setProperty("resultSetHandler", "Store as String");
        jdbcProcessor.setProperty("queryType", "Callable Statement");
    }

    public static DataSourceElement jdbcDataSource(String sourceName, DatabaseConfig dataSource) {
        if (dataSource == null) {
            dataSource = new DatabaseConfig(MS_DEFAULT, MS_DEFAULT, 1, 1L, MS_DEFAULT, MS_DEFAULT, MS_DEFAULT, MS_DEFAULT);
        }
        DataSourceElement dataSourceElement = new DataSourceElement();
        dataSourceElement.setEnabled(true);
        dataSourceElement.setName(sourceName + " JDBCDataSource");
        dataSourceElement.setProperty(TestElement.TEST_CLASS, DataSourceElement.class.getName());
        dataSourceElement.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass(TEST_BEAN_GUI));
        dataSourceElement.setProperty("autocommit", true);
        dataSourceElement.setProperty("keepAlive", true);
        dataSourceElement.setProperty("preinit", false);
        dataSourceElement.setProperty("dataSource", StringUtils.defaultIfBlank(sourceName, MS_DEFAULT));
        dataSourceElement.setProperty("dbUrl", dataSource.getDbUrl());
        dataSourceElement.setProperty("driver", dataSource.getDriver());
        dataSourceElement.setProperty("username", dataSource.getUsername());
        dataSourceElement.setProperty("password", dataSource.getPassword());
        dataSourceElement.setProperty("poolMax", dataSource.getPoolMax());
        dataSourceElement.setProperty("timeout", String.valueOf(dataSource.getTimeout()));
        dataSourceElement.setProperty("connectionAge", 5000);
        dataSourceElement.setProperty("trimInterval", 6000);
        dataSourceElement.setProperty("transactionIsolation", "DEFAULT");
        return dataSourceElement;
    }


    public static DatabaseConfig initDataSource(String environmentId, String dataSourceId) {
        if (StringUtils.isNotBlank(environmentId) && StringUtils.isNotBlank(dataSourceId)) {
            BaseEnvironmentService service = CommonBeanFactory.getBean(BaseEnvironmentService.class);
            ApiTestEnvironmentWithBLOBs environment = service.get(environmentId);
            if (environment != null && environment.getConfig() != null) {
                EnvironmentConfig envConfig = JSONUtil.parseObject(environment.getConfig(), EnvironmentConfig.class);
                if (CollectionUtils.isNotEmpty(envConfig.getDatabaseConfigs())) {
                    List<DatabaseConfig> configs = envConfig.getDatabaseConfigs().stream().filter(item ->
                            StringUtils.equals(item.getId(), dataSourceId)).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(configs)) {
                        return configs.get(0);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 过程变量覆盖处理
     *
     * @param tree
     */
    public static void coverArguments(HashTree tree) {
        Map<String, String> process = new HashMap<>();
        coverArguments(tree, process);
    }

    public static void coverArguments(HashTree tree, Map<String, String> process) {
        for (Object key : tree.keySet()) {
            HashTree node = tree.get(key);
            if (key instanceof Arguments) {
                Arguments arguments = (Arguments) key;
                if (arguments.getProperty(ElementConstants.COVER) == null) {
                    continue;
                }
                for (int i = 0; i < arguments.getArguments().size(); ++i) {
                    String argKey = arguments.getArgument(i).getName();
                    String argValue = arguments.getArgument(i).getValue();
                    if (process.containsKey(argKey)) {
                        arguments.getArgument(i).setValue(process.get(argKey));
                    } else {
                        process.put(argKey, argValue);
                    }
                }
            }
            if (node != null) {
                coverArguments(node, process);
            }
        }
    }

    public static void setHeader(HashTree tree, List<KeyValue> headers, String name) {
        // 合并header
        HeaderManager headerManager = new HeaderManager();
        headerManager.setEnabled(true);
        headerManager.setName(StringUtils.isNotEmpty(name) ? name + "HeaderManager" : "HeaderManager");
        headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        headerManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HeaderPanel"));
        boolean isAdd = true;
        for (Object key : tree.keySet()) {
            if (key instanceof HeaderManager) {
                headerManager = (HeaderManager) key;
                isAdd = false;
            }
        }
        //  header 也支持 mock 参数
        List<KeyValue> keyValues = headers.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).collect(Collectors.toList());
        for (KeyValue keyValue : keyValues) {
            boolean hasHead = false;
            //检查是否已经有重名的Head。如果Header重复会导致执行报错
            if (headerManager.getHeaders() != null) {
                for (int i = 0; i < headerManager.getHeaders().size(); i++) {
                    Header header = headerManager.getHeader(i);
                    String headName = header.getName();
                    if (StringUtils.equals(headName, keyValue.getName()) && !StringUtils.equals(headName, "Cookie")) {
                        hasHead = true;
                        break;
                    }
                }
            }
            if (!hasHead) {
                headerManager.add(new Header(keyValue.getName(), ScriptEngineUtils.buildFunctionCallString(keyValue.getValue())));
            }
        }
        if (headerManager.getHeaders().size() > 0 && isAdd) {
            tree.add(headerManager);
        }
    }

    public static HashTree retryHashTree(String name, long retryNum, HashTree tree) {
        if (StringUtils.isNotBlank(name) &&
                (name.startsWith(ResultParseUtil.POST_PROCESS_SCRIPT) ||
                        name.startsWith(ResultParseUtil.PRE_PROCESS_SCRIPT))) {
            return tree;
        }
        MsRetryLoopController loopController = new MsRetryLoopController();
        loopController.setClazzName(MsRetryLoopController.class.getCanonicalName());
        loopController.setRetryNum(retryNum);
        loopController.setEnable(true);
        return loopController.controller(tree, name);
    }

    public static boolean isLoop(MsTestElement sampler) {
        if (sampler != null) {
            if (StringUtils.equals(sampler.getType(), "LoopController")) {
                return true;
            }
            if (sampler.getParent() != null) {
                return isLoop(sampler.getParent());
            }
        }
        return false;
    }

    public static DatabaseConfig getDataSource(String processorName, String environmentId, String dataSourceId, String projectId, ParameterConfig config) {
        if (config == null) {
            return null;
        }
        DatabaseConfig dataSource = null;
        // 自选了数据源
        if (config.isEffective(projectId) && CollectionUtils.isNotEmpty(config.get(projectId).getDatabaseConfigs())
                && isDataSource(dataSourceId, config.get(projectId).getDatabaseConfigs())) {
            EnvironmentConfig environmentConfig = config.get(projectId);
            if (environmentConfig.getDatabaseConfigs() != null && StringUtils.isNotEmpty(environmentConfig.getEnvironmentId())) {
                environmentId = environmentConfig.getEnvironmentId();
            }
            dataSource = ElementUtil.initDataSource(environmentId, dataSourceId);
            if (dataSource == null && CollectionUtils.isNotEmpty(environmentConfig.getDatabaseConfigs())) {
                dataSource = environmentConfig.getDatabaseConfigs().get(0);
            }
        } else {
            // 取当前环境下默认的一个数据源
            if (config.isEffective(projectId) && CollectionUtils.isNotEmpty(config.get(projectId).getDatabaseConfigs())) {
                LoggerUtil.info(processorName + "：开始获取当前环境下默认数据源");
                DatabaseConfig dataSourceOrg = ElementUtil.dataSource(projectId, dataSourceId, config.get(projectId));
                if (dataSourceOrg != null) {
                    dataSource = dataSourceOrg;
                } else {
                    LoggerUtil.info(processorName + "：获取当前环境下默认数据源结束！未查找到默认数据源");
                    dataSource = config.get(projectId).getDatabaseConfigs().get(0);
                }
            }
        }
        return dataSource;
    }

    private static boolean isDataSource(String dataSourceId, List<DatabaseConfig> databaseConfigs) {
        List<String> ids = databaseConfigs.stream().map(DatabaseConfig::getId).collect(Collectors.toList());
        if (StringUtils.isNotEmpty(dataSourceId) && ids.contains(dataSourceId)) {
            return true;
        }
        return false;
    }

    public static List<String> scriptList(String request) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isBlank(request)) {
            return list;
        }
        JSONObject element = JSONUtil.parseObject(request);
        toList(element.getJSONArray(ElementConstants.HASH_TREE), scriptList, list);
        return list;
    }

    private static void toList(JSONArray hashTree, List<String> scriptList, List<String> list) {
        for (int i = 0; i < hashTree.length(); i++) {
            JSONObject element = hashTree.optJSONObject(i);
            if (element == null) {
                continue;
            }
            if (scriptList.contains(element.optString(ElementConstants.TYPE))) {
                JSONObject elementTarget = JSONUtil.parseObject(element.toString());
                if (elementTarget.has(ElementConstants.HASH_TREE)) {
                    elementTarget.remove(ElementConstants.HASH_TREE);
                }
                elementTarget.remove(MsHashTreeService.ACTIVE);
                elementTarget.remove(MsHashTreeService.INDEX);
                list.add(elementTarget.toString());
            }
            JSONArray jsrArray = element.optJSONArray(JSR);
            if (jsrArray != null) {
                for (int j = 0; j < jsrArray.length(); j++) {
                    JSONObject jsr223 = jsrArray.optJSONObject(j);
                    if (jsr223 != null) {
                        list.add(jsr223.toString());
                    }
                }
            }
            if (element.has(ElementConstants.HASH_TREE)) {
                JSONArray elementJSONArray = element.optJSONArray(ElementConstants.HASH_TREE);
                toList(elementJSONArray, scriptList, list);
            }
        }
    }

    public static boolean isSend(List<String> org, List<String> target) {
        if (org.size() != target.size()) {
            if (CollectionUtils.isEmpty(org)) {
                return true;
            }
            if (CollectionUtils.isEmpty(target)) {
                return false;
            }
            return true;

        }
        List<String> diff = target.stream()
                .filter(s -> !org.contains(s))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(diff)) {
            return true;
        }
        return false;
    }

    @NotNull
    public static List<String> getProjectIds(String scenarioDefinition) {
        Pattern pattern = Pattern.compile("\"projectId\"\\s*:\\s*\"?([^\"]*)\"?,");
        Matcher matcher = pattern.matcher(scenarioDefinition);
        List<String> projectIdLists = new ArrayList<>();
        while (matcher.find()) {
            if (!projectIdLists.contains(matcher.group(1))) {
                projectIdLists.add(matcher.group(1));
            }
        }
        return projectIdLists;
    }

    public static Map<String, String> getProjectEnvMap(List<String> projectIdLists, Map<String, String> projectEnvMap) {
        if (CollectionUtils.isNotEmpty(projectIdLists)) {
            projectEnvMap = projectEnvMap.entrySet().stream()
                    .filter(entry -> projectIdLists.contains(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return projectEnvMap;
    }
}
