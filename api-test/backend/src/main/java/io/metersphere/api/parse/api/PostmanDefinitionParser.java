package io.metersphere.api.parse.api;


import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.response.HttpResponse;
import io.metersphere.api.parse.postman.PostmanCollection;
import io.metersphere.api.parse.postman.PostmanItem;
import io.metersphere.api.parse.PostmanAbstractParserParser;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PostmanDefinitionParser extends PostmanAbstractParserParser<ApiDefinitionImport> {

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        this.projectId = request.getProjectId();
        boolean addCase = !StringUtils.equals("idea", request.getOrigin());
        JSONObject jsonObject = JSONUtil.parseObject(testStr);
        Object info = jsonObject.get("info");
        if (info == null) {
            MSException.throwException("wrong format");
        }
        PostmanCollection postmanCollection = JSON.parseObject(testStr, PostmanCollection.class);
        ApiDefinitionImport apiImport = new ApiDefinitionImport();
        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();

        String modulePath = null;
        if (StringUtils.isNotBlank(postmanCollection.getInfo().getName())) {
            modulePath = "/" + postmanCollection.getInfo().getName();
        }
        List<ApiTestCaseWithBLOBs> cases = new ArrayList<>();
        parseItem(postmanCollection.getItem(), modulePath, results,
                cases, addCase);
        apiImport.setData(results);
        apiImport.setCases(cases);
        return apiImport;
    }

    protected void parseItem(List<PostmanItem> items, String modulePath, List<ApiDefinitionWithBLOBs> results,
                             List<ApiTestCaseWithBLOBs> cases, boolean addCase) {
        for (PostmanItem item : items) {
            List<PostmanItem> childItems = item.getItem();
            if (childItems != null) {
                String itemModulePath;
                if (StringUtils.isNotBlank(modulePath) && StringUtils.isNotBlank(item.getName())) {
                    itemModulePath = modulePath + "/" + item.getName();
                } else {
                    itemModulePath = item.getName();
                }
                parseItem(childItems, itemModulePath, results, cases, addCase);
            } else {
                MsHTTPSamplerProxy msHTTPSamplerProxy = parsePostman(item);
                HttpResponse response = parsePostmanResponse(item);
                ApiDefinitionWithBLOBs request = buildApiDefinition(msHTTPSamplerProxy.getId(), msHTTPSamplerProxy.getName(),
                        msHTTPSamplerProxy.getPath(), msHTTPSamplerProxy.getMethod(), new ApiTestImportRequest());
                request.setPath(msHTTPSamplerProxy.getPath());
                request.setRequest(JSON.toJSONString(msHTTPSamplerProxy));
                request.setResponse(JSON.toJSONString(response));

                if (StringUtils.isNotBlank(modulePath)) {
                    request.setModulePath(modulePath);
                }
                results.add(request);
                if (addCase) {
                    ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
                    BeanUtils.copyBean(apiTestCase, request);
                    apiTestCase.setApiDefinitionId(request.getId());
                    apiTestCase.setPriority("P0");
                    cases.add(apiTestCase);
                }
            }
        }
    }
}
