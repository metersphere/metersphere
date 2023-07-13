package io.metersphere.api.parse.scenario;


import com.fasterxml.jackson.databind.JsonNode;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.parse.MsAbstractParser;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.commons.utils.JSON;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.commons.utils.JSONUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MsScenarioParser extends MsAbstractParser<ScenarioImport> {

    @Override
    public ScenarioImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        this.projectId = request.getProjectId();
        JSONObject testObject = JSONUtil.parseObject(testStr);

        if (StringUtils.isNotEmpty(testObject.optString("projectName")) || StringUtils.isNotEmpty(testObject.optString("projectId"))) {
            return parseMsFormat(testStr, request);
        } else {
            JsonNode node = JSONUtil.parseNode(testStr);
            ScenarioImport apiImport = new ScenarioImport();
            ArrayList<ApiScenarioWithBLOBs> apiScenarioWithBLOBs = new ArrayList<>();
            apiScenarioWithBLOBs.add(parsePluginFormat(node, request));
            apiImport.setData(apiScenarioWithBLOBs);
            return apiImport;
        }
    }

    protected ApiScenarioWithBLOBs parsePluginFormat(JsonNode  testObject, ApiTestImportRequest importRequest) {
        LinkedList<MsTestElement> results = new LinkedList<>();
        testObject.forEach(node -> {
            if(node != null ) {
                results.addAll(parseMsHTTPSamplerProxy(node, true));
            }
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

        if (CollectionUtils.isNotEmpty(data)) {
            data.forEach(item -> {
                //导入的用例，执行次数应当归零，否则会影响到首页的统计
                item.setExecuteTimes(0);
                String scenarioDefinitionStr = item.getScenarioDefinition();
                if (StringUtils.isNotBlank(scenarioDefinitionStr)) {
                    JSONObject scenarioDefinition = JSONUtil.parseObject(scenarioDefinitionStr);
                    if (scenarioDefinition != null) {
                        JSONObject environmentMap = scenarioDefinition.optJSONObject("environmentMap");
                        if (environmentMap != null) {
                            scenarioDefinition.put("environmentMap", new HashMap<>());
                        }
                        item.setEnvironmentType(EnvironmentType.JSON.name());
                        item.setEnvironmentJson(null);
                        item.setEnvironmentGroupId(null);
                        scenarioDefinition.put("projectId", this.projectId);
                        item.setScenarioDefinition(scenarioDefinition.toString());
                    }
                }
                item.setProjectId(this.projectId);
            });
        }
        return scenarioImport;
    }
}
