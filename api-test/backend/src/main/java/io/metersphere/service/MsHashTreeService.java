package io.metersphere.service;

import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.definition.ApiTestCaseInfo;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.base.domain.ApiTestCase;
import io.metersphere.base.domain.Project;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioMapper;
import io.metersphere.commons.constants.CommonConstants;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.service.definition.ApiDefinitionService;
import io.metersphere.service.definition.ApiTestCaseService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MsHashTreeService {
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private ProjectMapper projectMapper;

    public static final String CASE = CommonConstants.CASE;
    public static final String API = "API";
    public static final String REFERENCED = "referenced";
    public static final String REF = "REF";
    public static final String COPY = "Copy";
    public static final String REF_TYPE = "refType";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String SCENARIO = "scenario";
    public static final String TYPE = PropertyConstant.TYPE;
    public static final String HASH_TREE = "hashTree";
    public static final String PATH = "path";
    public static final String METHOD = "method";
    public static final String ENABLE = "enable";
    public static final String NUM = "num";
    public static final String ENV_ENABLE = "environmentEnable";
    public static final String VARIABLE_ENABLE = "variableEnable";
    public static final String MIX_ENABLE = "mixEnable";
    public static final String DISABLED = "disabled";
    public static final String VERSION_NAME = "versionName";
    public static final String VERSION_ENABLE = "versionEnable";
    public static final String URL = "url";
    public static final String HEADERS = "headers";
    public static final String REST = "rest";
    public static final String BODY = "body";
    public static final String ARGUMENTS = "arguments";
    public static final String AUTH_MANAGER = "authManager";
    public static final String PROJECT_ID = "projectId";
    public static final String ACTIVE = "active";
    public static final String ENV_MAP = "environmentMap";
    public static final String REF_ENABLE = "refEnable";
    private static final String PRE = "PRE";
    private static final String POST = "POST";
    private static final String ASSERTIONS = ElementConstants.ASSERTIONS;
    public static final String CUSTOM_NUM = "customNum";
    public static final String SHOW_CUSTOM_NUM = "showCustomNum";
    public static final String VERSION_ID = "versionId";
    private static final String CONNECT_TIMEOUT = "connectTimeout";
    private static final String RESPONSE_TIMEOUT = "responseTimeout";
    private static final String FOLLOW_REDIRECTS = "followRedirects";
    private static final String AUTO_REDIRECTS = "autoRedirects";
    private static final String ALIAS = "alias";
    public static final String INDEX = "index";
    private static final String QUERY = "query";
    private static final String VARIABLE_NAMES = "variableNames";
    private static final String DATA_SOURCE_ID = "dataSourceId";
    private static final String RESULT_VARIABLE = "resultVariable";
    private static final String ENV_Id = "environmentId";


    private final static String JSON_PATH = "jsonPath";
    private final static String JSR223 = "jsr223";
    private final static String XPATH = "xpath2";
    private final static String REGEX = "regex";
    private final static String DURATION = "duration";
    private final static String DOCUMENT = "document";
    private final static String LABEL = "label";
    private final static String SCENARIO_REF = "SCENARIO-REF-STEP";

    private void setElement(JSONObject element, Integer num,
                            Boolean enable, String versionName,
                            Boolean versionEnable, ParameterConfig msParameter) {
        element.put(NUM, num);
        element.put(ENABLE, enable == null ? false : enable);
        element.put(VERSION_NAME, versionName);
        element.put(VERSION_ENABLE, versionEnable == null ? false : versionEnable);
        if (msParameter != null) {
            ElementUtil.setDomain(element, msParameter);
        }
    }

    private void handleCopyReference(ApiTestCaseInfo apiCase, JSONObject element) {
        JSONObject refElement = JSONUtil.parseObject(apiCase.getRequest());
        refElement.remove(INDEX);
        ElementUtil.dataFormatting(refElement);
        ElementUtil.copyBean(element, refElement);
        element.put(HEADERS, refElement.opt(HEADERS));
        element.put(REST, refElement.opt(REST));
        element.put(PATH, refElement.opt(PATH));
        element.put(BODY, refElement.opt(BODY));
        element.put(ACTIVE, false);
        element.put(AUTH_MANAGER, refElement.opt(AUTH_MANAGER));
        element.put(ARGUMENTS, refElement.opt(ARGUMENTS));
        element.put(PROJECT_ID, apiCase.getProjectId());
        element.put(CONNECT_TIMEOUT, refElement.opt(CONNECT_TIMEOUT));
        element.put(RESPONSE_TIMEOUT, refElement.opt(RESPONSE_TIMEOUT));
        element.put(FOLLOW_REDIRECTS, refElement.opt(FOLLOW_REDIRECTS));
        element.put(AUTO_REDIRECTS, refElement.opt(AUTO_REDIRECTS));
        element.put(ALIAS, refElement.opt(ALIAS));

        if (StringUtils.equals(refElement.optString(TYPE), ElementConstants.JDBC_SAMPLER)) {
            element.put(QUERY, refElement.opt(QUERY));
            element.put(VARIABLE_NAMES, refElement.opt(VARIABLE_NAMES));
            element.put(DATA_SOURCE_ID, refElement.opt(DATA_SOURCE_ID));
            element.put(RESULT_VARIABLE, refElement.opt(RESULT_VARIABLE));
            element.put(ENV_Id, refElement.opt(ENV_Id));
        }

        JSONArray array = refElement.optJSONArray(HASH_TREE);
        if (array != null) {
            JSONArray sourceHashTree = element.optJSONArray(HASH_TREE);
            Map<String, List<JSONObject>> groupMap = ElementUtil.group(sourceHashTree);
            Map<String, List<JSONObject>> targetGroupMap = ElementUtil.group(refElement.optJSONArray(HASH_TREE));

            List<JSONObject> pre = ElementUtil.mergeHashTree(groupMap.get(PRE), targetGroupMap.get(PRE));
            List<JSONObject> post = ElementUtil.mergeHashTree(groupMap.get(POST), targetGroupMap.get(POST));
            List<JSONObject> rules = mergeAssertions(groupMap.get(ASSERTIONS), targetGroupMap.get(ASSERTIONS));
            List<JSONObject> step = new LinkedList<>();
            if (CollectionUtils.isNotEmpty(pre)) {
                step.addAll(pre);
            }
            if (CollectionUtils.isNotEmpty(post)) {
                step.addAll(post);
            }
            if (CollectionUtils.isNotEmpty(rules)) {
                step.addAll(rules);
            }
            element.put(HASH_TREE, step);
        }
        element.put(REFERENCED, REF);
        element.put(DISABLED, true);
        element.put(NAME, apiCase.getName());
    }

    private JSONObject setRequest(JSONObject element, Map<String, ApiTestCaseInfo> caseMap,
                                  Map<String, ApiDefinitionResult> apiMap, ParameterConfig msParameter) {
        boolean enable = element.optBoolean(ENABLE);
        boolean isExist = false;
        if (StringUtils.equalsIgnoreCase(element.optString(REF_TYPE), CASE) && MapUtils.isNotEmpty(caseMap)) {
            ApiTestCaseInfo apiCase = caseMap.get(element.optString(ID));
            if (apiCase != null) {
                if (StringUtils.equalsIgnoreCase(element.optString(REFERENCED), REF)) {
                    handleCopyReference(apiCase, element);
                }
                element.put(ID, apiCase.getId());
                isExist = true;
                this.setElement(element, apiCase.getNum(), enable, apiCase.getVersionName(), apiCase.getVersionEnable(), msParameter);
            }
        } else if (StringUtils.equalsIgnoreCase(element.optString(REFERENCED), COPY) && MapUtils.isNotEmpty(apiMap)) {
            ApiDefinitionResult definition = apiMap.get(element.optString(ID));
            if (definition != null) {
                Project project = projectMapper.selectByPrimaryKey(definition.getProjectId());
                element.put(ID, definition.getId());
                element.put(VERSION_ID, definition.getVersionId());
                this.setElement(element, definition.getNum(), enable,
                        definition.getVersionName(), project.getVersionEnable(), msParameter);
                isExist = true;
            }
        }
        if (!isExist) {
            if (StringUtils.equalsIgnoreCase(element.optString(REFERENCED), REF)) {
                element.put(ENABLE, false);
            }
            element.put(NUM, StringUtils.EMPTY);
        }
        return element;
    }

    public List<JSONObject> mergeAssertions(List<JSONObject> sourceHashTree, List<JSONObject> targetHashTree) {
        try {
            if (CollectionUtils.isNotEmpty(targetHashTree)) {
                JSONObject target = targetHashTree.get(0);

                if (CollectionUtils.isNotEmpty(sourceHashTree)) {
                    JSONObject source = sourceHashTree.get(0);
                    //jsonPath
                    JSONArray jsonPathTar = target.optJSONArray(JSON_PATH);
                    JSONArray jsonPathSource = source.optJSONArray(JSON_PATH);
                    mergeArray(target, jsonPathTar, jsonPathSource, JSON_PATH);
                    //jsr223
                    JSONArray jsr223Tar = target.optJSONArray(JSR223);
                    JSONArray jsr223Source = source.optJSONArray(JSR223);
                    mergeArray(target, jsr223Tar, jsr223Source, JSR223);
                    //xpath
                    JSONArray xpathTar = target.optJSONArray(XPATH);
                    JSONArray xpathSource = source.optJSONArray(XPATH);
                    mergeArray(target, xpathTar, xpathSource, XPATH);
                    //regex
                    JSONArray regexTar = target.optJSONArray(REGEX);
                    JSONArray regexSource = source.optJSONArray(REGEX);
                    mergeArray(target, regexTar, regexSource, REGEX);
                    //duration
                    JSONObject durationTar = target.optJSONObject(DURATION);
                    JSONObject durationSource = source.optJSONObject(DURATION);
                    mergeObject(target, durationTar, durationSource, DURATION);
                    //document
                    JSONObject documentTar = target.optJSONObject(DOCUMENT);
                    JSONObject documentSource = source.optJSONObject(DOCUMENT);
                    mergeObject(target, documentTar, documentSource, DOCUMENT);
                    sourceHashTree.remove(0);
                    sourceHashTree.add(target);
                }
            }
        } catch (Exception e) {
            LogUtil.error("mergeAssertions error", e);
        }
        return sourceHashTree;
    }

    private static void mergeObject(JSONObject target, JSONObject durationTar, JSONObject durationSource, String type) {
        if (durationSource != null && durationSource.has(LABEL) &&
                StringUtils.equals(durationSource.optString(LABEL), SCENARIO_REF)) {
            durationTar = durationSource;
        }
        target.remove(type);
        target.put(type, durationTar);
    }

    private static void mergeArray(JSONObject target, JSONArray jsonArray, JSONArray source, String type) {
        if (!source.isEmpty()) {
            source.forEach(obj -> {
                JSONObject jsonObject = (JSONObject) obj;
                if (StringUtils.equals(jsonObject.optString(LABEL), SCENARIO_REF)) {
                    jsonArray.put(jsonObject);
                }
            });
        }
        target.remove(type);
        target.put(type, jsonArray);
    }

    private void addCaseIds(JSONObject element, List<String> caseIds) {
        if (StringUtils.equalsAnyIgnoreCase(element.optString(REF_TYPE), CASE, API) && element.has(ID)) {
            caseIds.add(element.optString(ID));
        }
    }

    private JSONObject setRefScenario(JSONObject element) {
        boolean enable = element.has(ENABLE) ? element.optBoolean(ENABLE) : true;
        if (!element.has(MIX_ENABLE)) {
            element.put(MIX_ENABLE, false);
        }

        ApiScenarioDTO scenario = extApiScenarioMapper.selectById(element.optString(ID));
        if (scenario != null && StringUtils.isNotEmpty(scenario.getScenarioDefinition())) {
            boolean environmentEnable = element.has(ENV_ENABLE) ? element.optBoolean(ENV_ENABLE) : false;
            boolean variableEnable = element.has(VARIABLE_ENABLE) ? element.optBoolean(VARIABLE_ENABLE) : false;
            boolean mixEnable = element.has(MIX_ENABLE)
                    ? element.getBoolean(MIX_ENABLE) : false;

            if (environmentEnable && StringUtils.isNotEmpty(scenario.getEnvironmentJson())) {
                element.put(ENV_MAP, JSON.parseObject(scenario.getEnvironmentJson(), Map.class));
            }
            if (StringUtils.equalsIgnoreCase(element.optString(REFERENCED), REF)) {
                JSONObject object = JSONUtil.parseObject(scenario.getScenarioDefinition());
                object.put(INDEX, element.optString(INDEX));
                element = object;
                element.put(REFERENCED, REF);
                element.put(NAME, scenario.getName());
            }
            element.put(ID, scenario.getId());
            element.put(ENV_ENABLE, environmentEnable);
            if (!element.has(VARIABLE_ENABLE)) {
                element.put(VARIABLE_ENABLE, variableEnable);
            }
            if (!element.has(MIX_ENABLE) && !variableEnable) {
                element.put(MIX_ENABLE, mixEnable);
            }
            //获取场景的当前项目是否开启了自定义id
            ProjectConfig project = baseProjectApplicationService.getSpecificTypeValue(scenario.getProjectId(), "SCENARIO_CUSTOM_NUM");
            if (project != null) {
                element.put(SHOW_CUSTOM_NUM, project.getScenarioCustomNum());
            }
            element.put(CUSTOM_NUM, scenario.getCustomNum());
            this.setElement(element, scenario.getNum(), enable,
                    scenario.getVersionName(), scenario.getVersionEnable(), null);
        } else {
            if (StringUtils.equalsIgnoreCase(element.optString(REFERENCED), REF)) {
                element.put(ENABLE, false);
            }
            if (element.has(ENABLE) && BooleanUtils.isFalse(element.optBoolean(ENABLE))) {
                element.put(REF_ENABLE, true);
            }
            element.put(NUM, StringUtils.EMPTY);
        }
        return element;
    }

    public void dataFormatting(JSONArray hashTree, List<String> caseIds) {
        for (int i = 0; i < hashTree.length(); i++) {
            JSONObject element = hashTree.optJSONObject(i);
            if (element != null && StringUtils.equalsIgnoreCase(element.optString(TYPE), SCENARIO)) {
                element = this.setRefScenario(element);
                hashTree.put(i, element);
            } else if (element != null && ElementConstants.REQUESTS.contains(element.optString(TYPE))) {
                this.addCaseIds(element, caseIds);
                hashTree.put(i, element);
            }
            if (element.has(HASH_TREE)) {
                JSONArray elementJSONArray = element.optJSONArray(HASH_TREE);
                dataFormatting(elementJSONArray, caseIds);
            }
        }
    }

    public void dataFormatting(JSONObject element, List<String> caseIds) {
        if (element == null) {
            return;
        }
        if (StringUtils.equalsIgnoreCase(element.optString(TYPE), SCENARIO)) {
            element = this.setRefScenario(element);
        } else if (ElementConstants.REQUESTS.contains(element.optString(TYPE))) {
            this.addCaseIds(element, caseIds);
        }

        if (element.has(HASH_TREE)) {
            JSONArray elementJSONArray = element.optJSONArray(HASH_TREE);
            dataFormatting(elementJSONArray, caseIds);
        }
    }

    public void caseFormatting(JSONArray hashTree, Map<String, ApiTestCaseInfo> caseMap,
                               Map<String, ApiDefinitionResult> apiMap, ParameterConfig msParameter) {
        for (int i = 0; i < hashTree.length(); i++) {
            JSONObject element = hashTree.optJSONObject(i);
            if (element != null && ElementConstants.REQUESTS.contains(element.optString(TYPE))) {
                element = this.setRequest(element, caseMap, apiMap, msParameter);
                hashTree.put(i, element);
            }
            if (element.has(HASH_TREE)) {
                JSONArray elementJSONArray = element.optJSONArray(HASH_TREE);
                caseFormatting(elementJSONArray, caseMap, apiMap, msParameter);
            }
        }
    }

    public void caseFormatting(JSONObject element, List<String> caseIds, ParameterConfig msParameter) {
        List<ApiTestCaseInfo> caseInfos = apiTestCaseService.selectByCaseIds(caseIds);
        Map<String, ApiTestCaseInfo> caseMap = caseInfos.stream()
                .collect(Collectors.toMap(ApiTestCase::getId, a -> a, (k1, k2) -> k1));

        caseIds = caseIds.stream()
                .filter(n -> !caseMap.containsKey(n))
                .collect(Collectors.toList());
        // 接口
        List<ApiDefinitionResult> apis = apiDefinitionService.getApiByIds(caseIds);
        Map<String, ApiDefinitionResult> apiMap = apis.stream()
                .collect(Collectors.toMap(ApiDefinitionResult::getId, a -> a, (k1, k2) -> k1));

        if (element != null && ElementConstants.REQUESTS.contains(element.optString(TYPE))) {
            element = this.setRequest(element, caseMap, apiMap, msParameter);
        }
        if (element != null && element.has(HASH_TREE)) {
            JSONArray array = element.optJSONArray(HASH_TREE);
            caseFormatting(array, caseMap, apiMap, msParameter);
        }
    }
}
