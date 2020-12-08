package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.service.ApiModuleService;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.commons.utils.CommonBeanFactory;
import org.apache.commons.collections.CollectionUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MsParser extends ApiImportAbstractParser {

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        JSONObject testObject = JSONObject.parseObject(testStr, Feature.OrderedField);
        apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
        this.projectId = request.getProjectId();
        if (testObject.get("projectName") != null) {
            return parseMsFormat(testStr, request);
        } else {
            return parsePluginFormat(testObject);
        }
    }

    private ApiDefinitionImport parseMsFormat(String testStr, ApiTestImportRequest importRequest) {

        ApiDefinitionImport apiDefinitionImport = JSON.parseObject(testStr, ApiDefinitionImport.class);
        List<ApiDefinitionResult> data = apiDefinitionImport.getData();
        data.forEach(apiDefinition -> {
            String id = UUID.randomUUID().toString();
            apiDefinition.setModuleId(null);
            apiDefinition.setId(id);
            apiDefinition.setProjectId(this.projectId);
            String request = apiDefinition.getRequest();
            JSONObject requestObj = JSONObject.parseObject(request);
            requestObj.put("id", id);
            apiDefinition.setRequest(JSONObject.toJSONString(requestObj));
        });
        return apiDefinitionImport;
    }

    private ApiDefinitionImport parsePluginFormat(JSONObject testObject) {
        List<ApiDefinitionResult> results = new ArrayList<>();
        ApiDefinitionImport apiImport = new ApiDefinitionImport();
        apiImport.setProtocol(RequestType.HTTP);
        apiImport.setData(results);
        testObject.keySet().forEach(tag -> {
            ApiModule module = apiModuleService.getNewModule(tag, this.projectId, 1);
            createModule(module);
            JSONObject requests = testObject.getJSONObject(tag);
            requests.keySet().forEach(requestName -> {

                JSONObject requestObject = requests.getJSONObject(requestName);
                String path = requestObject.getString("url");
                String method = requestObject.getString("method");

                MsHTTPSamplerProxy request = buildRequest(requestName, path, method);
                ApiDefinitionResult apiDefinition = buildApiDefinition(request.getId(), requestName, path, method);
                apiDefinition.setModuleId(module.getId());
                apiDefinition.setProjectId(this.projectId);

                parseBody(requestObject, request.getBody());
                parseHeader(requestObject, request.getHeaders());
                apiDefinition.setRequest(JSONObject.toJSONString(request));
                results.add(apiDefinition);
            });
        });
        return apiImport;
    }

    private void parseHeader(JSONObject requestObject, List<KeyValue> msHeaders) {
        JSONArray headers = requestObject.getJSONArray("headers");
        if (CollectionUtils.isNotEmpty(headers)) {
            for (int i = 0; i < headers.size(); i++) {
                JSONObject header = headers.getJSONObject(i);
                msHeaders.add(new KeyValue(header.getString("name"), header.getString("value")));
            }
        }
    }

    private void parseBody(JSONObject requestObject, Body msBody) {
        if (requestObject.containsKey("body")) {
            Object body = requestObject.get("body");
            if (body instanceof JSONArray) {
                JSONArray bodies = requestObject.getJSONArray("body");
                if (bodies != null) {
                    StringBuilder bodyStr = new StringBuilder();
                    for (int i = 0; i < bodies.size(); i++) {
                        String tmp = bodies.getString(i);
                        bodyStr.append(tmp);
                    }
                    msBody.setType(Body.RAW);
                    msBody.setRaw(bodyStr.toString());
                }
            } else if (body instanceof JSONObject) {
                JSONObject bodyObj = requestObject.getJSONObject("body");
                if (bodyObj != null) {
                    ArrayList<KeyValue> kvs = new ArrayList<>();
                    bodyObj.keySet().forEach(key -> {
                        kvs.add(new KeyValue(key, bodyObj.getString(key)));
                    });
                    msBody.setKvs(kvs);
                    msBody.setType(Body.WWW_FROM);
                }
            }
        }
    }
}
