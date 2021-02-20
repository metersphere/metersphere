package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.ApiDefinitionResult;
import io.metersphere.api.dto.definition.ApiExportResult;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.service.ApiModuleService;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiModule;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.commons.constants.ApiImportPlatform;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

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
            request.setPlatform(ApiImportPlatform.Plugin.name());
            return parsePluginFormat(testObject, request, true);
        }
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
        parseModule(apiDefinition, importRequest);
        apiDefinition.setId(id);
        apiDefinition.setProjectId(this.projectId);
        String request = apiDefinition.getRequest();
        JSONObject requestObj = JSONObject.parseObject(request);
        requestObj.put("id", id);
        apiDefinition.setRequest(JSONObject.toJSONString(requestObj));
    }

    protected ApiDefinitionImport parsePluginFormat(JSONObject testObject,  ApiTestImportRequest importRequest, Boolean isCreateModule) {
        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();
        ApiDefinitionImport apiImport = new ApiDefinitionImport();
        apiImport.setProtocol(RequestType.HTTP);
        apiImport.setData(results);
        testObject.keySet().forEach(tag -> {

            ApiModule module = null;
            if (isCreateModule) {
                module = buildModule(getSelectModule(importRequest.getModuleId()), tag);
            }
            JSONObject requests = testObject.getJSONObject(tag);
            String moduleId = module.getId();

            requests.keySet().forEach(requestName -> {

                JSONObject requestObject = requests.getJSONObject(requestName);
                String path = requestObject.getString("url");
                String method = requestObject.getString("method");

                MsHTTPSamplerProxy request = buildRequest(requestName, path, method);
                ApiDefinitionWithBLOBs apiDefinition = buildApiDefinition(request.getId(), requestName, path, method,importRequest);
                apiDefinition.setModuleId(moduleId);
                apiDefinition.setProjectId(this.projectId);
                parseBody(requestObject, request.getBody());
                parseHeader(requestObject, request.getHeaders());
                parsePath(request, apiDefinition);
                apiDefinition.setRequest(JSONObject.toJSONString(request));
                results.add(apiDefinition);
            });
        });
        return apiImport;
    }

    private void parsePath(MsHTTPSamplerProxy request, ApiDefinitionWithBLOBs apiDefinition) {
        if (StringUtils.isNotBlank(request.getPath())) {
            String[] split = request.getPath().split("\\?");
            String path = split[0];
            parseQueryParameters(split, request.getArguments());
            request.setPath(path);
            apiDefinition.setPath(path);
        } else {
            request.setPath("/");
            apiDefinition.setPath("/");
        }
        apiDefinition.setName(apiDefinition.getPath() + " [" + apiDefinition.getMethod() + "]");
    }

    private void parseQueryParameters(String[] split, List<KeyValue> arguments) {
        if (split.length > 1) {
            try {
                String queryParams = split[1];
                queryParams = URLDecoder.decode(queryParams, "UTF-8");
                String[] params = queryParams.split("&");
                for (String param : params) {
                    String[] kv = param.split("=");
                    arguments.add(new KeyValue(kv[0], kv[1]));
                }
            } catch (UnsupportedEncodingException e) {
                LogUtil.info(e.getMessage(), e);
                return;
            }
        }
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


    private void parseModule(ApiDefinitionWithBLOBs apiDefinition, ApiTestImportRequest importRequest) {
        String modulePath = apiDefinition.getModulePath();
        if (StringUtils.isBlank(modulePath)) {
            return;
        }
        if (modulePath.startsWith("/")) {
            modulePath = modulePath.substring(1, modulePath.length());
        }
        if (modulePath.endsWith("/")) {
            modulePath = modulePath.substring(0, modulePath.length() - 1);
        }
        List<String> modules = Arrays.asList(modulePath.split("/"));
        ApiModule parent = getSelectModule(importRequest.getModuleId());
        Iterator<String> iterator = modules.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            parent = buildModule(parent, item);
            if (!iterator.hasNext()) {
                apiDefinition.setModuleId(parent.getId());
            }
        }
    }
}
