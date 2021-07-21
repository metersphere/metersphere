package io.metersphere.api.dto.automation.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.parse.ms.NodeTree;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.parse.MsAbstractParser;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.ApiScenarioModule;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.commons.utils.CommonBeanFactory;
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
            results.addAll(parseMsHTTPSamplerProxy(testObject, tag));
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
                        JSONArray hashTree = scenarioDefinition.getJSONArray("hashTree");
                        setCopy(hashTree);
                        JSONObject environmentMap = scenarioDefinition.getJSONObject("environmentMap");
                        if (environmentMap != null) {
                            scenarioDefinition.put("environmentMap", new HashMap<>());
                        }
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

                item.setId(UUID.randomUUID().toString());
                item.setProjectId(this.projectId);
            });
        }
        return scenarioImport;
    }

    private void setCopy(JSONArray hashTree) {
        // 将引用转成复制
        if (CollectionUtils.isNotEmpty(hashTree)) {
            for (int i = 0; i < hashTree.size(); i++) {
                JSONObject object = (JSONObject) hashTree.get(i);
                String referenced = object.getString("referenced");
                if (StringUtils.isNotBlank(referenced) && StringUtils.equals(referenced, "REF")) {
                    // 检测引用对象是否存在，若果不存在则改成复制对象
                    String refType = object.getString("refType");
                    boolean isCopy = true;
                    if (StringUtils.isNotEmpty(refType)) {
                        if (refType.equals("CASE")) {
                            ApiTestCaseService testCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
                            ApiTestCaseWithBLOBs bloBs = testCaseService.get(object.getString("id"));
                            if (bloBs != null) {
                                isCopy = false;
                            }
                        } else {
                            ApiAutomationService apiAutomationService = CommonBeanFactory.getBean(ApiAutomationService.class);
                            ApiScenarioWithBLOBs bloBs = apiAutomationService.getDto(object.getString("id"));
                            if (bloBs != null) {
                                isCopy = false;
                            }
                        }
                    }
                    if (isCopy) {
                        object.put("referenced", "Copy");
                    }
                }
                object.put("projectId", "");
                JSONObject environmentMap = object.getJSONObject("environmentMap");
                if (environmentMap != null) {
                    object.put("environmentMap", new HashMap<>());
                }
                if (CollectionUtils.isNotEmpty(object.getJSONArray("hashTree"))) {
                    setCopy(object.getJSONArray("hashTree"));
                }
            }
        }
    }

    protected void parseModule(String modulePath, ApiTestImportRequest importRequest, ApiScenarioWithBLOBs apiScenarioWithBLOBs) {
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
                    apiScenarioWithBLOBs.setModulePath("/默认模块" + path);
                }
            }
        }
    }
}
