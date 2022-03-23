package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.datacount.ApiMethodUrlDTO;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.definition.ApiTestCaseInfo;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.base.domain.ApiDefinition;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
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
    private ExtApiScenarioMapper extApiScenarioMapper;

    private static final String CASE = "CASE";
    private static final String REFERENCED = "referenced";
    private static final String REF = "REF";
    private static final String REF_TYPE = "refType";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String SCENARIO = "scenario";
    private static final String TYPE = "type";
    private static final String HASH_TREE = "hashTree";
    private static final String PATH = "path";
    private static final String METHOD = "method";
    private static final String ENABLE = "enable";
    private static final String NUM = "num";
    private static final String ENV_ENABLE = "environmentEnable";
    private static final String VARIABLE_ENABLE = "variableEnable";
    private static final String DISABLED = "disabled";
    private static final String VERSION_NAME = "versionName";
    private static final String VERSION_ENABLE = "versionEnable";
    private static final String URL = "url";
    private static final String HEADERS = "headers";
    private static final String REST = "rest";
    private static final String BODY = "body";
    private static final String ARGUMENTS = "arguments";
    private static final String AUTH_MANAGER = "authManager";

    public void setHashTree(JSONArray hashTree) {
        // 将引用转成复制
        if (CollectionUtils.isEmpty(hashTree)) {
            return;
        }
        for (int i = 0; i < hashTree.size(); i++) {
            JSONObject object = (JSONObject) hashTree.get(i);
            String referenced = object.getString(REFERENCED);
            if (StringUtils.isNotBlank(referenced) && StringUtils.equals(referenced, REF)) {
                // 检测引用对象是否存在，若果不存在则改成复制对象
                String refType = object.getString(REF_TYPE);
                if (StringUtils.isNotEmpty(refType)) {
                    if (refType.equals(CASE)) {
                        ApiTestCaseWithBLOBs bloBs = apiTestCaseService.get(object.getString(ID));
                        if (bloBs != null) {
                            object = JSON.parseObject(bloBs.getRequest());
                            object.put(ID, bloBs.getId());
                            object.put(NAME, bloBs.getName());
                            hashTree.set(i, object);
                        }
                    } else {
                        ApiScenarioWithBLOBs bloBs = apiScenarioMapper.selectByPrimaryKey(object.getString(ID));
                        if (bloBs != null) {
                            object = JSON.parseObject(bloBs.getScenarioDefinition());
                            hashTree.set(i, object);
                        }
                    }
                } else if (SCENARIO.equals(object.getString(TYPE))) {
                    ApiScenarioWithBLOBs bloBs = apiScenarioMapper.selectByPrimaryKey(object.getString(ID));
                    if (bloBs != null) {
                        object = JSON.parseObject(bloBs.getScenarioDefinition());
                        hashTree.set(i, object);
                    }
                }
            }
            if (object != null && CollectionUtils.isNotEmpty(object.getJSONArray(HASH_TREE))) {
                setHashTree(object.getJSONArray(HASH_TREE));
            }
        }
    }

    public List<ApiMethodUrlDTO> getMethodUrlDTOByHashTreeJsonObj(JSONObject obj) {
        List<ApiMethodUrlDTO> returnList = new ArrayList<>();
        if (obj != null && obj.containsKey(HASH_TREE)) {
            JSONArray hashArr = obj.getJSONArray(HASH_TREE);
            for (int i = 0; i < hashArr.size(); i++) {
                JSONObject elementObj = hashArr.getJSONObject(i);
                if (elementObj == null) {
                    continue;
                }
                if (elementObj.containsKey(URL) && elementObj.containsKey(METHOD)) {
                    String url = elementObj.getString(URL);
                    String method = elementObj.getString(METHOD);
                    ApiMethodUrlDTO dto = new ApiMethodUrlDTO(url, method);
                    if (!returnList.contains(dto)) {
                        returnList.add(dto);
                    }
                }
                if (elementObj.containsKey(PATH) && elementObj.containsKey(METHOD)) {
                    String path = elementObj.getString(PATH);
                    String method = elementObj.getString(METHOD);
                    ApiMethodUrlDTO dto = new ApiMethodUrlDTO(path, method);
                    if (!returnList.contains(dto)) {
                        returnList.add(dto);
                    }
                }

                if (elementObj.containsKey(ID) && elementObj.containsKey(REF_TYPE)) {
                    String refType = elementObj.getString(REF_TYPE);
                    String id = elementObj.getString(ID);
                    if (StringUtils.equals(CASE, refType)) {
                        ApiDefinition apiDefinition = apiTestCaseService.findApiUrlAndMethodById(id);
                        if (apiDefinition != null) {
                            ApiMethodUrlDTO dto = new ApiMethodUrlDTO(apiDefinition.getPath(), apiDefinition.getMethod());
                            if (!returnList.contains(dto)) {
                                returnList.add(dto);
                            }
                        }
                    } else if (StringUtils.equals("API", refType)) {
                        ApiDefinition apiDefinition = apiDefinitionService.selectUrlAndMethodById(id);
                        if (apiDefinition != null) {
                            ApiMethodUrlDTO dto = new ApiMethodUrlDTO(apiDefinition.getPath(), apiDefinition.getMethod());
                            if (!returnList.contains(dto)) {
                                returnList.add(dto);
                            }
                        }
                    }
                }

                List<ApiMethodUrlDTO> stepUrlList = this.getMethodUrlDTOByHashTreeJsonObj(elementObj);
                if (CollectionUtils.isNotEmpty(stepUrlList)) {
                    Collection unionList = CollectionUtils.union(returnList, stepUrlList);
                    returnList = new ArrayList<>(unionList);
                }
            }
        }
        return returnList;
    }

    private final static List<String> requests = new ArrayList<String>() {{
        this.add("HTTPSamplerProxy");
        this.add("DubboSampler");
        this.add("JDBCSampler");
        this.add("TCPSampler");
    }};

    private void setElement(JSONObject element, Integer num, Boolean enable, String versionName, Boolean versionEnable) {
        element.put(NUM, num);
        element.put(ENABLE, enable == null ? false : enable);
        element.put(VERSION_NAME, versionName);
        element.put(VERSION_ENABLE, versionEnable == null ? false : versionEnable);
    }

    private JSONObject setRequest(JSONObject element) {
        boolean enable = element.getBoolean(ENABLE);
        boolean isExist = false;
        if (StringUtils.equalsIgnoreCase(element.getString(REF_TYPE), CASE)) {
            ApiTestCaseInfo apiTestCase = apiTestCaseService.get(element.getString(ID));
            if (apiTestCase != null) {
                if (StringUtils.equalsIgnoreCase(element.getString(REFERENCED), REF)) {
                    JSONObject refElement = JSON.parseObject(apiTestCase.getRequest());
                    ElementUtil.dataFormatting(refElement);
                    JSONArray array = refElement.getJSONArray(HASH_TREE);
                    ElementUtil.copyBean(element, refElement);
                    element.put(HEADERS, refElement.get(HEADERS));
                    element.put(REST, refElement.get(REST));
                    element.put(PATH, refElement.get(PATH));
                    element.put(BODY, refElement.get(BODY));
                    element.put("active", false);
                    element.put(AUTH_MANAGER, refElement.get(AUTH_MANAGER));
                    element.put(ARGUMENTS, refElement.get(ARGUMENTS));
                    if (array != null) {
                        JSONArray sourceHashTree = element.getJSONArray("hashTree");
                        Map<String, List<JSONObject>> groupMap = ElementUtil.group(sourceHashTree);
                        Map<String, List<JSONObject>> targetGroupMap = ElementUtil.group(refElement.getJSONArray(HASH_TREE));

                        List<JSONObject> pre = ElementUtil.mergeHashTree(groupMap.get("PRE"), targetGroupMap.get("PRE"));
                        List<JSONObject> post = ElementUtil.mergeHashTree(groupMap.get("POST"), targetGroupMap.get("POST"));
                        List<JSONObject> rules = ElementUtil.mergeHashTree(groupMap.get("ASSERTIONS"), targetGroupMap.get("ASSERTIONS"));
                        JSONArray step = new JSONArray();
                        if (CollectionUtils.isNotEmpty(pre)) {
                            step.addAll(pre);
                        }
                        if (CollectionUtils.isNotEmpty(post)) {
                            step.addAll(post);
                        }
                        if (CollectionUtils.isNotEmpty(rules)) {
                            step.addAll(rules);
                        }
                        element.put("hashTree", step);
                    }
                    element.put(REFERENCED, REF);
                    element.put(DISABLED, true);
                    element.put(NAME, apiTestCase.getName());
                }
                element.put(ID, apiTestCase.getId());
                isExist = true;
                this.setElement(element, apiTestCase.getNum(), enable, apiTestCase.getVersionName(), apiTestCase.getVersionEnable());
            }
        } else {
            if (StringUtils.equalsIgnoreCase(element.getString(REFERENCED), "Copy")) {
                ApiDefinitionResult definitionWithBLOBs = apiDefinitionService.getById(element.getString(ID));
                if (definitionWithBLOBs != null) {
                    element.put(ID, definitionWithBLOBs.getId());
                    this.setElement(element, definitionWithBLOBs.getNum(), enable, definitionWithBLOBs.getVersionName(), definitionWithBLOBs.getVersionEnable());
                    isExist = true;
                }
            }
        }
        if (!isExist) {
            if (StringUtils.equalsIgnoreCase(element.getString(REFERENCED), REF)) {
                element.put(ENABLE, false);
            }
            element.put(NUM, "");
        }
        return element;
    }

    private JSONObject setRefScenario(JSONObject element) {
        boolean enable = element.containsKey(ENABLE) ? element.getBoolean(ENABLE) : true;
        if (!element.containsKey(VARIABLE_ENABLE)) {
            element.put(VARIABLE_ENABLE, true);
        }

        ApiScenarioDTO scenarioWithBLOBs = extApiScenarioMapper.selectById(element.getString(ID));
        if (scenarioWithBLOBs != null && StringUtils.isNotEmpty(scenarioWithBLOBs.getScenarioDefinition())) {
            boolean environmentEnable = element.containsKey(ENV_ENABLE)
                    ? element.getBoolean(ENV_ENABLE) : false;
            boolean variableEnable = element.containsKey(VARIABLE_ENABLE)
                    ? element.getBoolean(VARIABLE_ENABLE) : true;

            if (StringUtils.equalsIgnoreCase(element.getString(REFERENCED), REF)) {
                element = JSON.parseObject(scenarioWithBLOBs.getScenarioDefinition());
                element.put(REFERENCED, REF);
                element.put(NAME, scenarioWithBLOBs.getName());
            }
            element.put(ID, scenarioWithBLOBs.getId());
            element.put(ENV_ENABLE, environmentEnable);
            if (!element.containsKey(VARIABLE_ENABLE)) {
                element.put(VARIABLE_ENABLE, variableEnable);
            }
            this.setElement(element, scenarioWithBLOBs.getNum(), enable, scenarioWithBLOBs.getVersionName(), scenarioWithBLOBs.getVersionEnable());
        } else {
            if (StringUtils.equalsIgnoreCase(element.getString(REFERENCED), REF)) {
                element.put(ENABLE, false);
            }
            element.put(NUM, "");
        }
        return element;
    }

    public void dataFormatting(JSONArray hashTree) {
        for (int i = 0; i < hashTree.size(); i++) {
            JSONObject element = hashTree.getJSONObject(i);
            if (element != null && StringUtils.equalsIgnoreCase(element.getString(TYPE), SCENARIO)) {
                element = this.setRefScenario(element);
                hashTree.set(i, element);
            } else if (element != null && requests.contains(element.getString(TYPE))) {
                element = this.setRequest(element);
                hashTree.set(i, element);
            }
            if (element.containsKey(HASH_TREE)) {
                JSONArray elementJSONArray = element.getJSONArray(HASH_TREE);
                dataFormatting(elementJSONArray);
            }
        }
    }

    public void dataFormatting(JSONObject element) {
        if (element != null && StringUtils.equalsIgnoreCase(element.getString(TYPE), SCENARIO)) {
            element = this.setRefScenario(element);
        } else if (element != null && requests.contains(element.getString(TYPE))) {
            element = this.setRequest(element);
        }
        if (element != null && element.containsKey(HASH_TREE)) {
            JSONArray elementJSONArray = element.getJSONArray(HASH_TREE);
            dataFormatting(elementJSONArray);
        }
    }
}
