package io.metersphere.api.dto.automation.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.parse.MsAbstractParser;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.plugin.core.MsTestElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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
        JSONObject testObject = JSONObject.parseObject(testStr, Feature.OrderedField);

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
        ScenarioImport scenarioImport = JSON.parseObject(testStr, ScenarioImport.class, Feature.DisableSpecialKeyDetect);
        List<ApiScenarioWithBLOBs> data = scenarioImport.getData();

        if (CollectionUtils.isNotEmpty(data)) {
            data.forEach(item -> {
                String scenarioDefinitionStr = item.getScenarioDefinition();
                if (StringUtils.isNotBlank(scenarioDefinitionStr)) {
                    JSONObject scenarioDefinition = JSONObject.parseObject(scenarioDefinitionStr, Feature.DisableSpecialKeyDetect);
                    if (scenarioDefinition != null) {
                        JSONObject environmentMap = scenarioDefinition.getJSONObject("environmentMap");
                        if (environmentMap != null) {
                            scenarioDefinition.put("environmentMap", new HashMap<>());
                        }
                        item.setEnvironmentType(EnvironmentType.JSON.name());
                        item.setEnvironmentJson(null);
                        item.setEnvironmentGroupId(null);
                        scenarioDefinition.put("projectId", this.projectId);
                        item.setScenarioDefinition(JSONObject.toJSONString(scenarioDefinition));
                    }
                }

                item.setProjectId(this.projectId);
            });
        }
        return scenarioImport;
    }
}
