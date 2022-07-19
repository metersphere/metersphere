package io.metersphere.api.dto.definition.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.response.HttpResponse;
import io.metersphere.api.dto.parse.postman.PostmanCollection;
import io.metersphere.api.dto.parse.postman.PostmanItem;
import io.metersphere.api.parse.PostmanAbstractParserParser;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PostmanDefinitionParser extends PostmanAbstractParserParser<ApiDefinitionImport> {

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        this.projectId = request.getProjectId();
        JSONObject jsonObject = JSON.parseObject(testStr);
        Object info = jsonObject.get("info");
        if (info == null) {
            MSException.throwException("wrong format");
        } else {
            JSONObject jsonObject1 = JSON.parseObject(info.toString());
            if (jsonObject1.get("_postman_id") == null) {
                MSException.throwException("wrong format");
            }
        }
        PostmanCollection postmanCollection = JSON.parseObject(testStr, PostmanCollection.class, Feature.DisableSpecialKeyDetect);
        ApiDefinitionImport apiImport = new ApiDefinitionImport();
        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();

        String modulePath = null;
        if (StringUtils.isNotBlank(postmanCollection.getInfo().getName())) {
            modulePath = "/" + postmanCollection.getInfo().getName();
        }
        List<ApiTestCaseWithBLOBs> cases = new ArrayList<>();
        parseItem(postmanCollection.getItem(), modulePath, results,
                cases);
        apiImport.setData(results);
        apiImport.setCases(cases);
        return apiImport;
    }

    protected void parseItem(List<PostmanItem> items, String modulePath, List<ApiDefinitionWithBLOBs> results,
                             List<ApiTestCaseWithBLOBs> cases) {
        for (PostmanItem item : items) {
            List<PostmanItem> childItems = item.getItem();
            if (childItems != null) {
                String itemModulePath;
                if (StringUtils.isNotBlank(modulePath) && StringUtils.isNotBlank(item.getName())) {
                    itemModulePath = modulePath + "/" + item.getName();
                } else {
                    itemModulePath = item.getName();
                }
                parseItem(childItems, itemModulePath, results, cases);
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
                ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
                BeanUtils.copyBean(apiTestCase, request);
                apiTestCase.setApiDefinitionId(request.getId());
                apiTestCase.setPriority("P0");
                cases.add(apiTestCase);
            }
        }
    }
}
