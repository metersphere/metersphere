package io.metersphere.api.dto.definition.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.parse.MsAbstractParser;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.commons.constants.ApiImportPlatform;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.*;

public class MsDefinitionParser extends MsAbstractParser<ApiDefinitionImport> {

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        JSONObject testObject = JSONObject.parseObject(testStr, Feature.OrderedField);
        this.projectId = request.getProjectId();
        if (testObject.get("projectName") != null || testObject.get("projectId") != null ) {
            return parseMsFormat(testStr, request);
        } else {
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
                moduleId = ApiDefinitionImportUtil.buildModule(ApiDefinitionImportUtil.getSelectModule(importRequest.getModuleId()), tag, this.projectId).getId();
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
        apiDefinitionImport.getData().forEach(apiDefinition -> {
            parseApiDefinition(apiDefinition, importRequest);
        });
        return apiDefinitionImport;
    }

    private void parseApiDefinition(ApiDefinitionWithBLOBs apiDefinition, ApiTestImportRequest importRequest) {
        String id = UUID.randomUUID().toString();
        if (StringUtils.isBlank(apiDefinition.getModulePath())) {
            apiDefinition.setModuleId(null);
        }
        parseModule(apiDefinition.getModulePath(), importRequest, apiDefinition);
        apiDefinition.setId(id);
        apiDefinition.setProjectId(this.projectId);
        String request = apiDefinition.getRequest();
        JSONObject requestObj = JSONObject.parseObject(request);
        requestObj.put("id", id);
        apiDefinition.setRequest(JSONObject.toJSONString(requestObj));
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
        ApiModule parent = ApiDefinitionImportUtil.getSelectModule(importRequest.getModuleId());
        Iterator<String> iterator = modules.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            parent = ApiDefinitionImportUtil.buildModule(parent, item, this.projectId);
            if (!iterator.hasNext()) {
                apiDefinition.setModuleId(parent.getId());
            }
        }
    }
}
