package io.metersphere.api.dto.definition.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.parse.ms.NodeTree;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.parse.MsAbstractParser;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.commons.constants.ApiImportPlatform;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class MsDefinitionParser extends MsAbstractParser<ApiDefinitionImport> {

    private ApiModule selectModule;

    private String selectModulePath;

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        JSONObject testObject = JSONObject.parseObject(testStr, Feature.OrderedField);
        this.projectId = request.getProjectId();
        if (StringUtils.isNotBlank(request.getModuleId())) {
            this.selectModule = ApiDefinitionImportUtil.getSelectModule(request.getModuleId());
            if (this.selectModule != null) {
                this.selectModulePath = ApiDefinitionImportUtil.getSelectModulePath(this.selectModule.getName(), this.selectModule.getParentId());
            }
        }

        if (testObject.get("projectName") != null || testObject.get("projectId") != null ) {//  metersphere 格式导入
            return parseMsFormat(testStr, request);
        } else {    //  chrome 插件录制格式导入
            request.setPlatform(ApiImportPlatform.Plugin.name());
            ApiDefinitionImport apiImport = new ApiDefinitionImport();
            apiImport.setProtocol(RequestType.HTTP);
            apiImport.setData(parsePluginFormat(testObject, request, true));
            return apiImport;
        }
    }

    protected List<ApiDefinitionWithBLOBs> parsePluginFormat(JSONObject testObject, ApiTestImportRequest importRequest, Boolean isCreateModule) {
        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();
        testObject.keySet().forEach(tag -> {
            String moduleId = null;
            if (isCreateModule) {
                moduleId = ApiDefinitionImportUtil.buildModule(this.selectModule, tag, this.projectId).getId();
            }
            List<MsHTTPSamplerProxy> msHTTPSamplerProxies = parseMsHTTPSamplerProxy(testObject, tag);
            for (MsHTTPSamplerProxy msHTTPSamplerProxy : msHTTPSamplerProxies) {
                ApiDefinitionWithBLOBs apiDefinition = buildApiDefinition(msHTTPSamplerProxy.getId(), msHTTPSamplerProxy.getName(), msHTTPSamplerProxy.getPath(), msHTTPSamplerProxy.getMethod(), importRequest);
                apiDefinition.setModuleId(moduleId);
                apiDefinition.setProjectId(this.projectId);
                apiDefinition.setRequest(JSONObject.toJSONString(msHTTPSamplerProxy));
                apiDefinition.setName(apiDefinition.getPath() + " [" + apiDefinition.getMethod() + "]");
                results.add(apiDefinition);
            }
        });
        return results;
    }

    private ApiDefinitionImport parseMsFormat(String testStr, ApiTestImportRequest importRequest) {
        ApiDefinitionImport apiDefinitionImport = JSON.parseObject(testStr, ApiDefinitionImport.class);

        Map<String, List<ApiTestCaseWithBLOBs>> caseMap = new HashMap<>();
        if (apiDefinitionImport.getCases() != null) {
            apiDefinitionImport.getCases().forEach(item -> {
                List<ApiTestCaseWithBLOBs> caseList = caseMap.get(item.getApiDefinitionId());
                if (caseList == null) {
                    caseList = new ArrayList<>();
                    caseMap.put(item.getApiDefinitionId(), caseList);
                }
                caseList.add(item);
            });
        }

        Set<String> moduleIdSet = apiDefinitionImport.getData().stream()
                .map(ApiDefinitionWithBLOBs::getModuleId).collect(Collectors.toSet());

        Map<String, NodeTree> nodeMap = null;
        List<NodeTree> nodeTree = apiDefinitionImport.getNodeTree();
        if (CollectionUtils.isNotEmpty(nodeTree)) {
            cutDownTree(nodeTree, moduleIdSet);
            ApiDefinitionImportUtil.createNodeTree(nodeTree, projectId, importRequest.getModuleId());
            nodeMap = getNodeMap(nodeTree);
        }

        Map<String, NodeTree> finalNodeMap = nodeMap;
        apiDefinitionImport.getData().forEach(apiDefinition -> {
            parseApiDefinition(apiDefinition, importRequest, caseMap, finalNodeMap);
        });
        return apiDefinitionImport;
    }

    private void parseApiDefinition(ApiDefinitionWithBLOBs apiDefinition, ApiTestImportRequest importRequest,
                                    Map<String, List<ApiTestCaseWithBLOBs>> caseMap, Map<String, NodeTree> nodeMap) {
        String originId = apiDefinition.getId();
        String id = UUID.randomUUID().toString();

        if (nodeMap != null && nodeMap.get(apiDefinition.getModuleId()) != null) {
            NodeTree nodeTree = nodeMap.get(apiDefinition.getModuleId());
            apiDefinition.setModuleId(nodeTree.getNewId());
            apiDefinition.setModulePath(nodeTree.getPath());
        } else {
            if (StringUtils.isBlank(apiDefinition.getModulePath())) {
                apiDefinition.setModuleId(null);
            }
            // 旧版本未导出模块
            parseModule(apiDefinition.getModulePath(), importRequest, apiDefinition);
        }

        apiDefinition.setId(id);
        apiDefinition.setProjectId(this.projectId);
        String request = apiDefinition.getRequest();
        JSONObject requestObj = JSONObject.parseObject(request);
        requestObj.put("id", id);
        apiDefinition.setRequest(JSONObject.toJSONString(requestObj));
        parseCase(caseMap, apiDefinition, importRequest, originId);
    }

    private void parseCase(Map<String, List<ApiTestCaseWithBLOBs>> caseMap, ApiDefinitionWithBLOBs apiDefinition,
                           ApiTestImportRequest importRequest, String originId) {
        List<ApiTestCaseWithBLOBs> cases = caseMap.get(originId);
        if (CollectionUtils.isEmpty(cases)) {
            return;
        }
        cases.forEach(item -> {
            item.setApiDefinitionId(apiDefinition.getId());
            item.setProjectId(importRequest.getProjectId());
        });
    }

    private void parseModule(String modulePath, ApiTestImportRequest importRequest, ApiDefinitionWithBLOBs apiDefinition) {
        if (StringUtils.isEmpty(modulePath)) {
            return;
        }
        if (modulePath.startsWith("/")) {
            modulePath = modulePath.substring(1, modulePath.length());
        }
        if (modulePath.endsWith("/")) {
            modulePath = modulePath.substring(0, modulePath.length() - 1);
        }
        List<String> modules = Arrays.asList(modulePath.split("/"));
        ApiModule parent = this.selectModule;
        Iterator<String> iterator = modules.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            parent = ApiDefinitionImportUtil.buildModule(parent, item, this.projectId, importRequest.getUserId());
            if (!iterator.hasNext()) {
                apiDefinition.setModuleId(parent.getId());
                String path = apiDefinition.getModulePath() == null ? "" : apiDefinition.getModulePath();
                if (StringUtils.isNotBlank(this.selectModulePath)) {
                    apiDefinition.setModulePath(this.selectModulePath + path);
                } else if (StringUtils.isBlank(importRequest.getModuleId())){
                    apiDefinition.setModulePath("/默认模块" + path);
                }
            }
        }
    }
}
