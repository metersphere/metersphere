package io.metersphere.api.parse.api;


import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.response.HttpResponse;
import io.metersphere.api.parse.ApiImportAbstractParser;
import io.metersphere.api.parse.MsAbstractParser;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.commons.constants.ApiImportPlatform;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.constants.RequestTypeConstants;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MsDefinitionParser extends MsAbstractParser<ApiDefinitionImport> {

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        JSONObject testObject = JSONUtil.parseObject(testStr);
        this.projectId = request.getProjectId();
        if (this.projectId == null) {
            MSException.throwException("wrong format");
        }
        if (testObject.opt("projectName") != null || testObject.opt("projectId") != null) {//  metersphere 格式导入
            return parseMsFormat(testStr, request);
        } else {    //  chrome 插件录制格式导入
            request.setPlatform(ApiImportPlatform.Plugin.name());
            ApiDefinitionImport apiImport = new ApiDefinitionImport();
            apiImport.setProtocol(RequestTypeConstants.HTTP);
            apiImport.setData(parsePluginFormat(testObject, request, true));
            return apiImport;
        }
    }

    protected List<ApiDefinitionWithBLOBs> parsePluginFormat(JSONObject testObject, ApiTestImportRequest importRequest, Boolean isCreateModule) {
        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();
        if (testObject != null) {
            testObject.keySet().forEach(tag -> {

                List<MsHTTPSamplerProxy> msHTTPSamplerProxies = parseMsHTTPSamplerProxy(testObject, tag, false);
                for (MsHTTPSamplerProxy msHTTPSamplerProxy : msHTTPSamplerProxies) {
                    ApiDefinitionWithBLOBs apiDefinition = buildApiDefinition(msHTTPSamplerProxy.getId(), msHTTPSamplerProxy.getName(), msHTTPSamplerProxy.getPath(), msHTTPSamplerProxy.getMethod(), importRequest);
                    apiDefinition.setProjectId(this.projectId);
                    apiDefinition.setModulePath(tag);
                    apiDefinition.setRequest(JSON.toJSONString(msHTTPSamplerProxy));
                    apiDefinition.setName(apiDefinition.getPath() + " [" + apiDefinition.getMethod() + "]");
                    results.add(apiDefinition);
                }
            });
        }
        return results;
    }

    private ApiDefinitionImport parseMsFormat(String testStr, ApiTestImportRequest importRequest) {
        ApiDefinitionImport apiDefinitionImport = JSON.parseObject(testStr, ApiDefinitionImport.class);

        List<ApiDefinitionWithBLOBs> protocol = apiDefinitionImport.getData().stream().filter(item -> StringUtils.equals(importRequest.getProtocol(), item.getProtocol())).collect(Collectors.toList());
        apiDefinitionImport.setData(protocol);
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

        apiDefinitionImport.getData().forEach(apiDefinition -> {
            parseApiDefinition(apiDefinition, importRequest, caseMap);
        });
        if (MapUtils.isNotEmpty(caseMap)) {
            List<ApiTestCaseWithBLOBs> list = new ArrayList<>();
            caseMap.forEach((k, v) -> {
                list.addAll(v);
            });
            apiDefinitionImport.setCases(list);
        }
        return apiDefinitionImport;
    }

    private void parseApiDefinition(ApiDefinitionWithBLOBs apiDefinition, ApiTestImportRequest importRequest,
                                    Map<String, List<ApiTestCaseWithBLOBs>> caseMap) {
        String originId = apiDefinition.getId();

        apiDefinition.setProjectId(this.projectId);
        JSONObject requestObj = this.parseObject(apiDefinition.getRequest(), apiDefinition.getProjectId());
        if (requestObj != null) {
            if (StringUtils.isBlank(requestObj.optString("path"))) {
                if (StringUtils.isNotBlank(requestObj.optString("url"))) {
                    ApiImportAbstractParser apiImportAbstractParser = CommonBeanFactory.getBean(ApiImportAbstractParser.class);
                    String path = apiImportAbstractParser.formatPath(requestObj.optString("url"));
                    requestObj.put("path", path);
                }
            }
            requestObj.put("url", StringUtils.EMPTY);
            apiDefinition.setRequest(requestObj.toString());
        }
        JSONObject responseObject = new JSONObject();
        try {
            //  设置响应体
            responseObject = JSONUtil.parseObject(apiDefinition.getResponse());
        } catch (Exception e) {
            responseObject = new JSONObject(new HttpResponse());
        }
        apiDefinition.setResponse(responseObject.toString());
        apiDefinition.setCreateUser(SessionUtils.getUserId());
        apiDefinition.setUserId(SessionUtils.getUserId());
        apiDefinition.setDeleteUserId(null);
        parseCase(caseMap, apiDefinition, importRequest, originId);
    }

    private void parseCase(Map<String, List<ApiTestCaseWithBLOBs>> caseMap, ApiDefinitionWithBLOBs apiDefinition,
                           ApiTestImportRequest importRequest, String originId) {
        List<ApiTestCaseWithBLOBs> cases = caseMap.get(originId);
        if (CollectionUtils.isEmpty(cases)) {
            return;
        }
        List<ApiTestCaseWithBLOBs> errorRequests = new ArrayList<>();
        cases.forEach(item -> {
            item.setApiDefinitionId(apiDefinition.getId());
            item.setProjectId(importRequest.getProjectId());
            // request 内容处理
            JSONObject requestObj = this.parseObject(item.getRequest(), item.getProjectId());
            if (requestObj != null) {
                item.setRequest(requestObj.toString());
            } else {
                errorRequests.add(item);
            }
        });
        if (CollectionUtils.isNotEmpty(errorRequests)) {
            cases.removeAll(errorRequests);
            if (CollectionUtils.isEmpty(cases)) {
                caseMap.remove(originId);
            }
        }
    }

    private JSONObject parseObject(String request, String projectId) {
        try {
            if (StringUtils.isEmpty(request)) {
                return null;
            }
            JSONObject requestObj = JSONUtil.parseObject(request);
            requestObj.put(PropertyConstant.ENVIRONMENT, StringUtils.EMPTY);
            if (!requestObj.optString("protocol").equals(RequestTypeConstants.SQL)) {
                requestObj.put(PropertyConstant.ENVIRONMENT_ID, StringUtils.EMPTY);
            }
            requestObj.put("projectId", projectId);
            return requestObj;
        } catch (Exception e) {
            LogUtil.error(request, e);
            return null;
        }
    }

}
