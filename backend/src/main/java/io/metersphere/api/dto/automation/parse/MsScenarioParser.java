package io.metersphere.api.dto.automation.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.definition.parse.ms.NodeTree;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.base.domain.*;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.api.parse.MsAbstractParser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class MsScenarioParser extends MsAbstractParser<ScenarioImport> {

    private ApiScenarioModule selectModule;

    private String selectModulePath;

    @Override
    public ScenarioImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        this.projectId = request.getProjectId();
        JSONObject testObject = JSONObject.parseObject(testStr, Feature.OrderedField);

        if (StringUtils.isNotBlank(request.getModuleId())) {
            this.selectModule = ApiScenarioImportUtil.getSelectModule(request.getModuleId());
            if (this.selectModule != null) {
                this.selectModulePath = ApiScenarioImportUtil.getSelectModulePath(this.selectModule.getName(), this.selectModule.getParentId());
            }
        }

        if (testObject.get("projectName") != null || testObject.get("projectId") != null) {
            return parseMsFormat(testStr, request);
        } else {
            ScenarioImport apiImport = new ScenarioImport();
            ArrayList<ApiScenarioWithBLOBs> apiScenarioWithBLOBs = new ArrayList<>();
            apiScenarioWithBLOBs.add(parsePluginFormat(testObject, request));
            apiImport.setData(apiScenarioWithBLOBs);
            return apiImport;
        }
    }

    protected ApiScenarioWithBLOBs parsePluginFormat(JSONObject testObject, ApiTestImportRequest importRequest) {
        LinkedList<MsTestElement> results = new LinkedList<>();
        testObject.keySet().forEach(tag -> {
            results.addAll(parseMsHTTPSamplerProxy(testObject, tag, true));
        });
        MsScenario msScenario = new MsScenario();
        msScenario.setName(importRequest.getFileName());
        msScenario.setHashTree(results);
        ApiScenarioWithBLOBs scenarioWithBLOBs = parseScenario(msScenario);
        scenarioWithBLOBs.setApiScenarioModuleId(importRequest.getModuleId());
        return scenarioWithBLOBs;
    }

    private ScenarioImport parseMsFormat(String testStr, ApiTestImportRequest importRequest) {
        ScenarioImport scenarioImport = JSON.parseObject(testStr, ScenarioImport.class);
        List<ApiScenarioWithBLOBs> data = scenarioImport.getData();

        Set<String> moduleIdSet = scenarioImport.getData().stream()
                .map(ApiScenarioWithBLOBs::getApiScenarioModuleId).collect(Collectors.toSet());

        Map<String, NodeTree> nodeMap = null;
        List<NodeTree> nodeTree = scenarioImport.getNodeTree();
        if (CollectionUtils.isNotEmpty(nodeTree)) {
            cutDownTree(nodeTree, moduleIdSet);
            ApiScenarioImportUtil.createNodeTree(nodeTree, projectId, importRequest.getModuleId());
            nodeMap = getNodeMap(nodeTree);
        }

        if (CollectionUtils.isNotEmpty(data)) {
            Map<String, NodeTree> finalNodeMap = nodeMap;
            data.forEach(item -> {
                String scenarioDefinitionStr = item.getScenarioDefinition();
                if (StringUtils.isNotBlank(scenarioDefinitionStr)) {
                    JSONObject scenarioDefinition = JSONObject.parseObject(scenarioDefinitionStr);
                    if (scenarioDefinition != null) {
                        JSONObject environmentMap = scenarioDefinition.getJSONObject("environmentMap");
                        if (environmentMap != null) {
                            scenarioDefinition.put("environmentMap", new HashMap<>());
                        }
                        item.setEnvironmentType(EnvironmentType.JSON.name());
                        item.setEnvironmentJson(null);
                        item.setEnvironmentGroupId(null);
                        item.setScenarioDefinition(JSONObject.toJSONString(scenarioDefinition));
                    }
                }

                if (finalNodeMap != null && finalNodeMap.get(item.getApiScenarioModuleId()) != null) {
                    NodeTree node = finalNodeMap.get(item.getApiScenarioModuleId());
                    item.setApiScenarioModuleId(node.getNewId());
                    item.setModulePath(node.getPath());
                } else {
                    if (StringUtils.isBlank(item.getModulePath())) {
                        item.setApiScenarioModuleId(null);
                    }
                    // 旧版本未导出模块
                    parseModule(item.getModulePath(), importRequest, item);
                }
                item.setProjectId(this.projectId);
            });
        }
        return scenarioImport;
    }

    protected void parseModule(String modulePath, ApiTestImportRequest importRequest, ApiScenarioWithBLOBs apiScenarioWithBLOBs) {
        if (StringUtils.isEmpty(modulePath)) {
            return;
        }
        if (modulePath.startsWith("/")) {
            modulePath = modulePath.substring(1);
        }
        if (modulePath.endsWith("/")) {
            modulePath = modulePath.substring(0, modulePath.length() - 1);
        }
        List<String> modules = Arrays.asList(modulePath.split("/"));
        ApiScenarioModule parent = this.selectModule;
        Iterator<String> iterator = modules.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            parent = ApiScenarioImportUtil.buildModule(parent, item, this.projectId);
            if (!iterator.hasNext()) {
                apiScenarioWithBLOBs.setApiScenarioModuleId(parent.getId());
                String path = apiScenarioWithBLOBs.getModulePath() == null ? "" : apiScenarioWithBLOBs.getModulePath();
                if (StringUtils.isNotBlank(this.selectModulePath)) {
                    apiScenarioWithBLOBs.setModulePath(this.selectModulePath + path);
                } else if (StringUtils.isBlank(importRequest.getModuleId())) {
                    apiScenarioWithBLOBs.setModulePath("/未规划场景" + path);
                }
            }
        }
    }
}
