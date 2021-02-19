package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.automation.ApiScenrioExportResult;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.service.ApiModuleService;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.commons.constants.ApiImportPlatform;
import io.metersphere.commons.utils.CommonBeanFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.UUID;

public class ScenarioMsParser extends MsParser {

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        JSONObject testObject = JSONObject.parseObject(testStr, Feature.OrderedField);
        apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
        this.projectId = request.getProjectId();
        if (testObject.get("projectId") != null) {
            return parseMsFormat(testStr, request);
        } else {
            request.setPlatform(ApiImportPlatform.Plugin.name());
            ApiDefinitionImport apiDefinitionImport = parsePluginFormat(testObject, request, false);
            MsScenario msScenario = new MsScenario();
            LinkedList<MsTestElement> msHTTPSamplerProxies = new LinkedList<>();
            apiDefinitionImport.getData().forEach(res -> {
                msHTTPSamplerProxies.add(JSONObject.parseObject(res.getRequest(), MsHTTPSamplerProxy.class));
            });
            msScenario.setHashTree(msHTTPSamplerProxies);
            msScenario.setType("scenario");
            msScenario.setName("test");
            apiDefinitionImport.setScenarioDefinition(msScenario);
            return apiDefinitionImport;
        }
    }

    private ApiDefinitionImport parseMsFormat(String testStr, ApiTestImportRequest importRequest) {
        ApiScenrioExportResult apiScenrioExportResult = JSON.parseObject(testStr, ApiScenrioExportResult.class);
        apiScenrioExportResult.getData().forEach(scenario -> {
            parseApiDefinition(scenario, importRequest);
        });
        ApiDefinitionImport apiDefinitionImport = new ApiDefinitionImport();
        apiDefinitionImport.setScenarioDefinitionData(apiScenrioExportResult.getData());
        return apiDefinitionImport;
    }

    private void parseApiDefinition(ApiScenarioWithBLOBs scenario, ApiTestImportRequest importRequest) {
        String id = UUID.randomUUID().toString();
        if (StringUtils.isBlank(scenario.getModulePath())) {
            scenario.setApiScenarioModuleId(null);
        }
//        parseModule(scenario, importRequest);
        scenario.setId(id);
        scenario.setProjectId(this.projectId);
        String scenarioDefinition = scenario.getScenarioDefinition();
        JSONObject scenarioDefinitionObj = JSONObject.parseObject(scenarioDefinition);
        scenarioDefinitionObj.put("id", id);
        scenario.setScenarioDefinition(JSONObject.toJSONString(scenarioDefinitionObj));
    }
}
