package io.metersphere.api.dto.automation.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.automation.ApiScenarioModuleDTO;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.definition.SaveApiTestCaseRequest;
import io.metersphere.api.dto.definition.parse.ms.NodeTree;
import io.metersphere.api.dto.definition.response.HttpResponse;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.parse.ApiImportAbstractParser;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiScenarioModuleService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.service.CheckPermissionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ApiScenarioImportUtil {

    public static ApiScenarioModule getSelectModule(String moduleId) {
        ApiScenarioModuleService apiScenarioModuleService = CommonBeanFactory.getBean(ApiScenarioModuleService.class);
        if (StringUtils.isNotBlank(moduleId) && !StringUtils.equals("root", moduleId)) {
            ApiScenarioModule module = new ApiScenarioModule();
            ApiScenarioModuleDTO moduleDTO = apiScenarioModuleService.getNode(moduleId);
            if (moduleDTO != null) {
                BeanUtils.copyBean(module, moduleDTO);
            }
            return module;
        }
        return null;
    }

    public static String getSelectModulePath(String path, String pid) {
        ApiScenarioModuleService apiScenarioModuleService = CommonBeanFactory.getBean(ApiScenarioModuleService.class);
        if (StringUtils.isNotBlank(pid)) {
            ApiScenarioModuleDTO moduleDTO = apiScenarioModuleService.getNode(pid);
            if (moduleDTO != null) {
                return getSelectModulePath(moduleDTO.getName() + "/" + path, moduleDTO.getParentId());
            }
        }
        return "/" + path;
    }

    public static ApiScenarioModule buildModule(ApiScenarioModule parentModule, String name, String projectId) {
        ApiScenarioModuleService apiModuleService = CommonBeanFactory.getBean(ApiScenarioModuleService.class);
        ApiScenarioModule module;
        if (parentModule != null) {
            module = apiModuleService.getNewModule(name, projectId, parentModule.getLevel() + 1);
            module.setParentId(parentModule.getId());
        } else {
            module = apiModuleService.getNewModule(name, projectId, 1);
        }
        createModule(module);
        return module;
    }

    public static void createModule(ApiScenarioModule module) {
        ApiScenarioModuleService apiModuleService = CommonBeanFactory.getBean(ApiScenarioModuleService.class);
        if (module.getName().length() > 64) {
            module.setName(module.getName().substring(0, 64));
        }
        List<ApiScenarioModule> apiModules = apiModuleService.selectSameModule(module);
        if (CollectionUtils.isEmpty(apiModules)) {
            apiModuleService.addNode(module);
        } else {
            module.setId(apiModules.get(0).getId());
        }
    }

    private static void createNodeTree(NodeTree nodeTree, String pid, String projectId,
                                       ApiScenarioModuleService apiModuleService, String path, int baseLevel) {
        ApiScenarioModule module = new ApiScenarioModule();
        BeanUtils.copyBean(module, nodeTree);
        apiModuleService.buildNewModule(module);
        module.setProjectId(projectId);
        module.setParentId(pid);
        module.setLevel(module.getLevel() + baseLevel);
        createModule(module);
        nodeTree.setNewId(module.getId());
        path = path + nodeTree.getName();
        nodeTree.setPath(path);
        List<NodeTree> children = nodeTree.getChildren();
        if (CollectionUtils.isNotEmpty(children)) {
            String finalPath = path;
            children.forEach(item -> {
                createNodeTree(item, module.getId(), projectId, apiModuleService, finalPath + "/", baseLevel);
            });
        }
    }

    /**
     * 根据导出的模块树，创建新的模块树
     *
     * @param nodeTree
     * @param projectId
     */
    public static void createNodeTree(List<NodeTree> nodeTree, String projectId, String moduleId) {
        ApiScenarioModuleService apiModuleService = CommonBeanFactory.getBean(ApiScenarioModuleService.class);
        Iterator<NodeTree> iterator = nodeTree.iterator();
        boolean hasModuleSelected = false;
        ApiScenarioModuleDTO selectModule = null;
        if (StringUtils.isNotBlank(moduleId) && !"root".equals(moduleId)) {
            selectModule = apiModuleService.getNode(moduleId);
            hasModuleSelected = true;
        }
        while (iterator.hasNext()) {
            NodeTree node = iterator.next();
            createNodeTree(node, hasModuleSelected ? selectModule.getId() : null,
                    projectId, apiModuleService, "/", hasModuleSelected ? selectModule.getLevel() : 0);
        }
    }

    private static ApiDefinition getApiDefinitionResult(JSONObject object, ApiDefinitionService apiDefinitionService, Map<String, ApiDefinition> definitionMap, Set<String> userRelatedProjectIds) {
        List<String> projectIds = new ArrayList<>(userRelatedProjectIds);
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        if (object.getString("path") != null && object.getString("method") != null) {
            apiDefinitionExample.createCriteria()
                    .andPathEqualTo(object.getString("path"))
                    .andMethodEqualTo(object.getString("method"))
                    .andProtocolEqualTo(object.getString("protocol"))
                    .andProjectIdIn(projectIds)
                    .andStatusNotEqualTo("Trash");
        }
        ApiDefinition apiDefinition = apiDefinitionService.getApiDefinition(apiDefinitionExample);
        if (apiDefinition == null) {
            if (MapUtils.isEmpty(definitionMap)) {
                return null;
            } else {
                return definitionMap.get(object.getString("path") + object.getString("method") + object.getString("protocol"));
            }
        } else {
            if (MapUtils.isEmpty(definitionMap)) {
                definitionMap.put(object.getString("path") + object.getString("method") + object.getString("protocol"), apiDefinition);
                return apiDefinition;
            } else {
                ApiDefinition apiDefinition1 = definitionMap.get(object.getString("path") + object.getString("method") + object.getString("protocol"));
                if (apiDefinition1 == null) {
                    definitionMap.put(object.getString("path") + object.getString("method") + object.getString("protocol"), apiDefinition);
                    return apiDefinition;
                } else {
                    return apiDefinition1;
                }
            }
        }
    }

    public static void checkCase(int i, JSONObject object, String versionId, String projectId, ApiTestCaseMapper apiTestCaseMapper, ApiDefinitionMapper apiDefinitionMapper, Map<String, ApiDefinition> definitionMap) {
        ApiTestCaseService testCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
        ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
        ApiTestCaseWithBLOBs bloBs = testCaseService.get(object.getString("id"));
        CheckPermissionService checkPermissionService = CommonBeanFactory.getBean(CheckPermissionService.class);
        Set<String> userRelatedProjectIds = checkPermissionService.getUserRelatedProjectIds();
        if (bloBs == null || !userRelatedProjectIds.contains(bloBs.getProjectId())) {
            ApiDefinition apiDefinition = getApiDefinitionResult(object, apiDefinitionService, definitionMap, userRelatedProjectIds);
            if (apiDefinition != null) {
                if (MapUtils.isNotEmpty(definitionMap) || definitionMap.size() == 0) {
                    SaveApiTestCaseRequest request = new SaveApiTestCaseRequest();
                    request.setApiDefinitionId(apiDefinition.getId());
                    request.setName(object.getString("name"));
                    ApiTestCase sameCase = testCaseService.getSameCase(request);
                    if (sameCase == null) {
                        structureCaseByJson(i, object, testCaseService, apiDefinition, apiTestCaseMapper);
                    } else {
                        object.put("id", sameCase.getId());
                        object.put("resourceId", sameCase.getId());
                        object.put("projectId", projectId);
                        object.put("useEnvironment", "");
                        object.put("environmentId", "");
                    }
                }
            } else {
                ApiDefinitionResult apiDefinitionResult = structureApiDefinitionByJson(i, apiDefinitionService, object, versionId, projectId, apiDefinitionMapper, definitionMap);
                structureCaseByJson(i, object, testCaseService, apiDefinitionResult, apiTestCaseMapper);
            }
        }
    }

    public static ApiDefinitionResult structureApiDefinitionByJson(int i, ApiDefinitionService apiDefinitionService, JSONObject object, String versionId, String projectId, ApiDefinitionMapper apiDefinitionMapper, Map<String, ApiDefinition> definitionMap) {
        ApiDefinitionResult test = new ApiDefinitionResult();
        apiDefinitionService.checkQuota(projectId);
        String protocal = object.getString("protocal");
        if (StringUtils.equals(protocal, "DUBBO")) {
            test.setMethod("dubbo://");
        } else {
            test.setMethod(object.getString("method"));
        }
        test.setProtocol(object.getString("protocol"));
        if (StringUtils.isNotBlank(object.getString("modulePath")) && StringUtils.isNotBlank(object.getString("moduleId"))) {
            test.setModulePath(object.getString("modulePath"));
            test.setModuleId(object.getString("moduleId"));
        } else {
            apiDefinitionService.initModulePathAndId(projectId, test);
        }
        String id = UUID.randomUUID().toString();
        test.setId(id);
        test.setNum(apiDefinitionService.getNextNum(projectId) + i);
        test.setName(object.getString("name"));
        if (StringUtils.isBlank(object.getString("path"))) {
            if (StringUtils.isNotBlank(object.getString("url"))) {
                ApiImportAbstractParser apiImportAbstractParser = CommonBeanFactory.getBean(ApiImportAbstractParser.class);
                String path = apiImportAbstractParser.formatPath(object.getString("url"));
                test.setPath(path);
            }
        } else {
            test.setPath(object.getString("path"));
        }
        test.setCreateUser(SessionUtils.getUserId());
        test.setProjectId(projectId);
        test.setCreateTime(System.currentTimeMillis());
        test.setUpdateTime(System.currentTimeMillis());
        test.setStatus(APITestStatus.Underway.name());
        test.setRefId(id);
        test.setLatest(true);
        test.setVersionId(versionId);
        object.put("id", test.getId());
        object.put("resourceId", test.getId());
        object.put("projectId", projectId);
        object.put("useEnvironment", "");
        object.put("environmentId", "");
        object.put("url", "");
        JSONObject objectNew = JSONObject.parseObject(object.toJSONString(), Feature.DisableSpecialKeyDetect);
        objectNew.remove("refType");
        objectNew.remove("referenced");
        test.setRequest(objectNew.toJSONString());
        HttpResponse httpResponse = new HttpResponse();
        KeyValue keyValue = new KeyValue();
        List<KeyValue> list = new ArrayList<>();
        list.add(keyValue);
        httpResponse.setHeaders(list);
        httpResponse.setStatusCode(list);
        httpResponse.setBody(new Body());
        test.setResponse(JSON.toJSONString(httpResponse));
        test.setUserId(SessionUtils.getUserId());
        test.setLatest(true);
        test.setOrder(apiDefinitionService.getImportNextOrder(projectId));
        if (test.getName().length() > 255) {
            test.setName(test.getName().substring(0, 255));
        }
        apiDefinitionMapper.insert(test);
        definitionMap.put(object.getString("path") + object.getString("method") + object.getString("protocol"), test);
        return test;
    }

    public static void structureCaseByJson(int i, JSONObject object, ApiTestCaseService testCaseService, ApiDefinition apiDefinition, ApiTestCaseMapper apiTestCaseMapper) {
        String projectId = apiDefinition.getProjectId();
        ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
        ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
        String id = UUID.randomUUID().toString();
        apiTestCase.setId(id);
        apiTestCase.setName(object.getString("name"));
        apiTestCase.setCaseStatus(APITestStatus.Underway.name());
        apiTestCase.setApiDefinitionId(apiDefinition.getId());
        apiTestCase.setCreateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        apiTestCase.setUpdateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        apiTestCase.setProjectId(projectId);
        apiTestCase.setCreateTime(System.currentTimeMillis());
        apiTestCase.setUpdateTime(System.currentTimeMillis());
        apiTestCase.setVersionId(apiDefinition.getVersionId());
        apiTestCase.setPriority("P0");
        apiTestCase.setNum(testCaseService.getNextNum(apiTestCase.getApiDefinitionId(), apiDefinition.getNum() + i, projectId));
        object.put("id", apiTestCase.getId());
        object.put("resourceId", apiTestCase.getId());
        object.put("projectId", projectId);
        object.put("useEnvironment", "");
        object.put("environmentId", "");
        JSONObject objectNew = JSONObject.parseObject(object.toJSONString(), Feature.DisableSpecialKeyDetect);
        objectNew.remove("refType");
        objectNew.remove("referenced");
        apiTestCase.setRequest(objectNew.toJSONString());
        apiTestCase.setOrder(apiDefinitionService.getImportNextCaseOrder(projectId));
        if (apiTestCase.getName().length() > 255) {
            apiTestCase.setName(apiTestCase.getName().substring(0, 255));
        }
        apiTestCaseMapper.insert(apiTestCase);
    }

    public static void formatHashTree(JSONArray hashTree) {
        if (CollectionUtils.isNotEmpty(hashTree)) {
            for (int i = 0; i < hashTree.size(); i++) {
                JSONObject object = (JSONObject) hashTree.get(i);
                object.put("index", i + 1);
                object.put("resourceId", UUID.randomUUID().toString());
                hashTree.set(i, object);
                if (CollectionUtils.isNotEmpty(object.getJSONArray("hashTree"))) {
                    formatHashTree(object.getJSONArray("hashTree"));
                }
            }
        }
    }

}
