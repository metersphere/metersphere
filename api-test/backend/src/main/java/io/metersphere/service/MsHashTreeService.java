package io.metersphere.service;

import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.definition.ApiTestCaseInfo;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.Project;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioMapper;
import io.metersphere.commons.constants.ElementConstants;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.JSONUtil;
import io.metersphere.service.definition.ApiDefinitionService;
import io.metersphere.service.definition.ApiTestCaseService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class MsHashTreeService {
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;

    @Resource
    private ProjectMapper projectMapper;

    public static final String CASE = "CASE";
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
    private static final String PRE = "PRE";
    private static final String POST = "POST";
    private static final String ASSERTIONS = ElementConstants.ASSERTIONS;

    public void setHashTree(JSONArray hashTree) {
        // 将引用转成复制
        if (hashTree == null) {
            return;
        }
        for (int i = 0; i < hashTree.length(); i++) {
            JSONObject object = (JSONObject) hashTree.opt(i);
            String referenced = object.optString(REFERENCED);
            if (StringUtils.isNotBlank(referenced) && StringUtils.equals(referenced, REF)) {
                // 检测引用对象是否存在，若果不存在则改成复制对象
                String refType = object.optString(REF_TYPE);
                if (StringUtils.isNotEmpty(refType)) {
                    if (refType.equals(CASE)) {
                        ApiTestCaseWithBLOBs bloBs = apiTestCaseService.get(object.optString(ID));
                        if (bloBs != null) {
                            object = JSONUtil.parseObject(bloBs.getRequest());
                            object.put(ID, bloBs.getId());
                            object.put(NAME, bloBs.getName());
                            hashTree.put(i, object);
                        }
                    } else {
                        ApiScenarioWithBLOBs bloBs = apiScenarioMapper.selectByPrimaryKey(object.optString(ID));
                        if (bloBs != null) {
                            object = JSONUtil.parseObject(bloBs.getScenarioDefinition());
                            hashTree.put(i, object);
                        }
                    }
                } else if (SCENARIO.equals(object.optString(TYPE))) {
                    ApiScenarioWithBLOBs bloBs = apiScenarioMapper.selectByPrimaryKey(object.optString(ID));
                    if (bloBs != null) {
                        object = JSONUtil.parseObject(bloBs.getScenarioDefinition());
                        hashTree.put(i, object);
                    }
                }
            }
            if (object != null && object.optJSONArray(HASH_TREE) != null) {
                setHashTree(object.optJSONArray(HASH_TREE));
            }
        }
    }

    private void setElement(JSONObject element, Integer num, Boolean enable, String versionName, Boolean versionEnable) {
        element.put(NUM, num);
        element.put(ENABLE, enable == null ? false : enable);
        element.put(VERSION_NAME, versionName);
        element.put(VERSION_ENABLE, versionEnable == null ? false : versionEnable);
    }

    private JSONObject setRequest(JSONObject element) {
        boolean enable = element.optBoolean(ENABLE);
        boolean isExist = false;
        if (StringUtils.equalsIgnoreCase(element.optString(REF_TYPE), CASE)) {
            ApiTestCaseInfo apiTestCase = apiTestCaseService.get(element.optString(ID));
            if (apiTestCase != null) {
                if (StringUtils.equalsIgnoreCase(element.optString(REFERENCED), REF)) {
                    JSONObject refElement = JSONUtil.parseObject(apiTestCase.getRequest());
                    ElementUtil.dataFormatting(refElement);
                    JSONArray array = refElement.optJSONArray(HASH_TREE);
                    ElementUtil.copyBean(element, refElement);
                    element.put(HEADERS, refElement.opt(HEADERS));
                    element.put(REST, refElement.opt(REST));
                    element.put(PATH, refElement.opt(PATH));
                    element.put(BODY, refElement.opt(BODY));
                    element.put(ACTIVE, false);
                    element.put(AUTH_MANAGER, refElement.opt(AUTH_MANAGER));
                    element.put(ARGUMENTS, refElement.opt(ARGUMENTS));
                    element.put(PROJECT_ID, apiTestCase.getProjectId());
                    if (array != null) {
                        JSONArray sourceHashTree = element.optJSONArray(HASH_TREE);
                        Map<String, List<JSONObject>> groupMap = ElementUtil.group(sourceHashTree);
                        Map<String, List<JSONObject>> targetGroupMap = ElementUtil.group(refElement.optJSONArray(HASH_TREE));

                        List<JSONObject> pre = ElementUtil.mergeHashTree(groupMap.get(PRE), targetGroupMap.get(PRE));
                        List<JSONObject> post = ElementUtil.mergeHashTree(groupMap.get(POST), targetGroupMap.get(POST));
                        List<JSONObject> rules = ElementUtil.mergeHashTree(groupMap.get(ASSERTIONS), targetGroupMap.get(ASSERTIONS));
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
                    element.put(NAME, apiTestCase.getName());
                }
                element.put(ID, apiTestCase.getId());
                isExist = true;
                this.setElement(element, apiTestCase.getNum(), enable, apiTestCase.getVersionName(), apiTestCase.getVersionEnable());
            }
        } else if (StringUtils.equalsIgnoreCase(element.optString(REFERENCED), COPY)) {
            ApiDefinitionResult definition = apiDefinitionService.getById(element.optString(ID));
            if (definition != null) {
                Project project = projectMapper.selectByPrimaryKey(definition.getProjectId());
                element.put(ID, definition.getId());
                this.setElement(element, definition.getNum(), enable, definition.getVersionName(), project.getVersionEnable());
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

    private JSONObject setRefScenario(JSONObject element) {
        boolean enable = element.has(ENABLE) ? element.optBoolean(ENABLE) : true;
        if (!element.has(VARIABLE_ENABLE)) {
            element.put(VARIABLE_ENABLE, true);
        }

        ApiScenarioDTO scenarioWithBLOBs = extApiScenarioMapper.selectById(element.optString(ID));
        if (scenarioWithBLOBs != null && StringUtils.isNotEmpty(scenarioWithBLOBs.getScenarioDefinition())) {
            boolean environmentEnable = element.has(ENV_ENABLE) ? element.optBoolean(ENV_ENABLE) : false;
            boolean variableEnable = element.has(VARIABLE_ENABLE) ? element.optBoolean(VARIABLE_ENABLE) : true;
            if (environmentEnable && StringUtils.isNotEmpty(scenarioWithBLOBs.getEnvironmentJson())) {
                element.put(ENV_MAP, JSON.parseObject(scenarioWithBLOBs.getEnvironmentJson(), Map.class));
            }
            if (StringUtils.equalsIgnoreCase(element.optString(REFERENCED), REF)) {
                element = JSONUtil.parseObject(scenarioWithBLOBs.getScenarioDefinition());
                element.put(REFERENCED, REF);
                element.put(NAME, scenarioWithBLOBs.getName());
            }
            element.put(ID, scenarioWithBLOBs.getId());
            element.put(ENV_ENABLE, environmentEnable);
            if (!element.has(VARIABLE_ENABLE)) {
                element.put(VARIABLE_ENABLE, variableEnable);
            }
            this.setElement(element, scenarioWithBLOBs.getNum(), enable, scenarioWithBLOBs.getVersionName(), scenarioWithBLOBs.getVersionEnable());
        } else {
            if (StringUtils.equalsIgnoreCase(element.optString(REFERENCED), REF)) {
                element.put(ENABLE, false);
            }
            element.put(NUM, StringUtils.EMPTY);
        }
        return element;
    }

    public void dataFormatting(JSONArray hashTree) {
        for (int i = 0; i < hashTree.length(); i++) {
            JSONObject element = hashTree.optJSONObject(i);
            if (element != null && StringUtils.equalsIgnoreCase(element.optString(TYPE), SCENARIO)) {
                element = this.setRefScenario(element);
                hashTree.put(i, element);
            } else if (element != null && ElementConstants.REQUESTS.contains(element.optString(TYPE))) {
                element = this.setRequest(element);
                hashTree.put(i, element);
            }
            if (element.has(HASH_TREE)) {
                JSONArray elementJSONArray = element.optJSONArray(HASH_TREE);
                dataFormatting(elementJSONArray);
            }
        }
    }

    public void dataFormatting(JSONObject element) {
        if (element != null && StringUtils.equalsIgnoreCase(element.optString(TYPE), SCENARIO)) {
            element = this.setRefScenario(element);
        } else if (element != null && ElementConstants.REQUESTS.contains(element.optString(TYPE))) {
            element = this.setRequest(element);
        }
        if (element != null && element.has(HASH_TREE)) {
            JSONArray elementJSONArray = element.optJSONArray(HASH_TREE);
            dataFormatting(elementJSONArray);
        }
    }
}
