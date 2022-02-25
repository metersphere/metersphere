package io.metersphere.api.dto.automation.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.automation.ApiScenarioModuleDTO;
import io.metersphere.api.dto.definition.ApiDefinitionRequest;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.definition.SaveApiTestCaseRequest;
import io.metersphere.api.dto.definition.parse.ms.NodeTree;
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
import io.metersphere.service.ProjectService;
import org.apache.commons.collections.CollectionUtils;
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

    public static boolean checkWorkSpace(String projectId, String currentProjectId) {
        if(!Objects.equals(projectId, currentProjectId)){
            ProjectService projectService = CommonBeanFactory.getBean(ProjectService.class);
            Project project = projectService.getProjectById(projectId);
            return Objects.equals(project.getWorkspaceId(), SessionUtils.getCurrentWorkspaceId());
        }
        return true;
    }

    private static ApiDefinitionResult getApiDefinitionResult(JSONObject object, ApiDefinitionService apiDefinitionService) {
        ApiDefinitionRequest apiDefinitionRequest = new ApiDefinitionRequest();
        apiDefinitionRequest.setPath(object.getString("path"));
        apiDefinitionRequest.setMethod(object.getString("method"));
        apiDefinitionRequest.setPath(object.getString("protocol"));
        return apiDefinitionService.getApiDefinitionResult(apiDefinitionRequest);
    }

    private static ApiTestCaseWithBLOBs getApiTestCase(JSONObject object, ApiTestCaseService testCaseService, ApiDefinitionResult apiDefinitionResult) {
        SaveApiTestCaseRequest request = new SaveApiTestCaseRequest();
        request.setName(object.getString("name"));
        request.setApiDefinitionId(apiDefinitionResult.getId());
        return testCaseService.getSameCaseWithBLOBs(request);
    }

    public static void checkCase(JSONObject object, String versionId, String projectId, ApiTestCaseMapper apiTestCaseMapper, ApiDefinitionMapper apiDefinitionMapper) {
        ApiTestCaseService testCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
        ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
        ApiTestCaseWithBLOBs bloBs = testCaseService.get(object.getString("id"));
        if (bloBs != null) {
            boolean isSameWorkSpace = checkWorkSpace(bloBs.getProjectId(),projectId);
            if(!isSameWorkSpace){
                ApiDefinitionResult apiDefinition = apiDefinitionService.getById(bloBs.getApiDefinitionId());
                String apiDefinitionId = checkDefinition(apiDefinitionService,apiDefinition, versionId,projectId,apiDefinitionMapper);
                structureCaseData(bloBs,apiDefinitionId,versionId,projectId,apiTestCaseMapper);
                object.put("projectId", projectId);
                object.put("id", bloBs.getId());
            }
        }else{
            ApiDefinitionResult apiDefinition = getApiDefinitionResult(object,apiDefinitionService);
            ApiTestCaseWithBLOBs testCase;
            if(apiDefinition!=null){
                testCase= getApiTestCase(object, testCaseService, apiDefinition);
                if (testCase != null) {
                    boolean isSameWorkSpace = checkWorkSpace(testCase.getProjectId(),projectId);
                    if(!isSameWorkSpace){
                        String apiDefinitionId = checkDefinition(apiDefinitionService, apiDefinition, versionId,projectId,apiDefinitionMapper);
                        structureCaseData(testCase,apiDefinitionId,versionId,projectId,apiTestCaseMapper);
                    }
                }else{
                    String apiDefinitionId = checkDefinition(apiDefinitionService, apiDefinition, versionId,projectId,apiDefinitionMapper);
                    testCase= structureCaseByJson(object, versionId, projectId, apiDefinitionId,apiTestCaseMapper);
                }
            }else{
                ApiDefinitionResult apiDefinitionResult = structureApiDefinitionByJson(apiDefinitionService, object, versionId, projectId, apiDefinitionMapper);
                testCase = structureCaseByJson(object, versionId, projectId, apiDefinitionResult.getId(),apiTestCaseMapper);
            }
            object.put("projectId", projectId);
            object.put("id", testCase.getId());
        }
    }

    private static String checkDefinition(ApiDefinitionService apiDefinitionService, ApiDefinitionResult apiDefinition, String versionId, String projectId,ApiDefinitionMapper apiDefinitionMapper) {
        boolean isSameWorkspace = checkWorkSpace(apiDefinition.getProjectId(),projectId);
        if(!isSameWorkspace){
            String requestStr = apiDefinition.getRequest();
            JSONObject objectDefinition = JSONObject.parseObject(requestStr);
            ApiDefinitionResult apiDefinitionResult1 = insertDefinitionByApiDefinition(apiDefinitionService,apiDefinition, objectDefinition, versionId, projectId,apiDefinitionMapper);
            return apiDefinitionResult1.getId();
        }else{
            return apiDefinition.getId();
        }
    }

    private static void structureCaseData(ApiTestCaseWithBLOBs testCase, String apiDefinitionId, String versionId, String projectId,ApiTestCaseMapper apiTestCaseMapper) {
        String requestStr = testCase.getRequest();
        JSONObject objectCase = JSONObject.parseObject(requestStr);
        testCase.setApiDefinitionId(apiDefinitionId);
        insertCaseByApiTestCase(testCase,objectCase,versionId,projectId,apiTestCaseMapper);
    }

    public static ApiDefinitionResult structureApiDefinitionByJson(ApiDefinitionService apiDefinitionService,JSONObject object, String versionId, String projectId,ApiDefinitionMapper apiDefinitionMapper) {
        ApiDefinitionResult test = new ApiDefinitionResult();
        apiDefinitionService.checkQuota();
        String protocal = object.getString("protocal");
        if (StringUtils.equals(protocal, "DUBBO")) {
            test.setMethod("dubbo://");
        }else{
            test.setMethod(protocal);
        }
        apiDefinitionService.initModulePathAndId(projectId, test);
        String id = UUID.randomUUID().toString();
        test.setId(id);
        test.setName(object.getString("name"));
        test.setPath(object.getString("path"));
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
        object.put("useEnvironment","");
        test.setRequest(object.toJSONString());
        test.setUserId(SessionUtils.getUserId());
        test.setLatest(true);
        test.setOrder(apiDefinitionService.getImportNextOrder(projectId));
        apiDefinitionMapper.insert(test);
        return test;
    }

    public static ApiDefinitionResult insertDefinitionByApiDefinition(ApiDefinitionService apiDefinitionService, ApiDefinitionResult apiDefinitionResult, JSONObject objectDefinition, String versionId, String projectId,ApiDefinitionMapper apiDefinitionMapper){
        String id = UUID.randomUUID().toString();
        apiDefinitionResult.setId(id);
        apiDefinitionResult.setProjectId(projectId);
        apiDefinitionResult.setVersionId(versionId);
        apiDefinitionResult.setRefId(id);
        apiDefinitionResult.setLatest(true);
        objectDefinition.put("id", id);
        objectDefinition.put("resourceId", id);
        objectDefinition.put("projectId", projectId);
        objectDefinition.put("useEnvironment","");
        apiDefinitionService.initModulePathAndId(projectId, apiDefinitionResult);
        apiDefinitionResult.setRequest(JSON.toJSONString(objectDefinition));
        apiDefinitionResult.setOrder(apiDefinitionService.getImportNextOrder(projectId));
        apiDefinitionMapper.insert(apiDefinitionResult);
        return apiDefinitionResult;
    }

    public static ApiTestCaseWithBLOBs structureCaseByJson(JSONObject object, String versionId, String projectId, String apiDefinitionId,ApiTestCaseMapper apiTestCaseMapper) {
        ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
        ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
        String id = UUID.randomUUID().toString();
        apiTestCase.setId(id);
        apiTestCase.setName(object.getString("name"));
        apiTestCase.setCaseStatus(APITestStatus.Underway.name());
        apiTestCase.setApiDefinitionId(apiDefinitionId);
        apiTestCase.setCreateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        apiTestCase.setUpdateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        apiTestCase.setProjectId(projectId);
        apiTestCase.setCreateTime(System.currentTimeMillis());
        apiTestCase.setUpdateTime(System.currentTimeMillis());
        apiTestCase.setVersionId(versionId);
        object.put("id", apiTestCase.getId());
        object.put("resourceId", apiTestCase.getId());
        object.put("projectId", projectId);
        object.put("useEnvironment","");
        apiTestCase.setRequest(object.toJSONString());
        apiTestCase.setOrder(apiDefinitionService.getImportNextCaseOrder(projectId));
        apiTestCaseMapper.insert(apiTestCase);
        return  apiTestCase;
    }

    public static void insertCaseByApiTestCase(ApiTestCaseWithBLOBs bloBs, JSONObject objectCase, String versionId, String projectId,ApiTestCaseMapper apiTestCaseMapper){
        ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
        String id = UUID.randomUUID().toString();
        bloBs.setId(id);
        bloBs.setProjectId(projectId);
        bloBs.setVersionId(versionId);
        objectCase.put("id", id);
        objectCase.put("resourceId", id);
        objectCase.put("projectId", projectId);
        objectCase.put("useEnvironment","");
        bloBs.setRequest(JSON.toJSONString(objectCase));
        bloBs.setOrder(apiDefinitionService.getImportNextCaseOrder(projectId));
        apiTestCaseMapper.insert(bloBs);
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
