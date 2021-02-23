package io.metersphere.api.dto.definition.parse;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.parse.postman.PostmanCollection;
import io.metersphere.api.dto.parse.postman.PostmanItem;
import io.metersphere.api.dto.parse.postman.PostmanKeyValue;
import io.metersphere.api.parse.PostmanAbstractParserParser;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiModule;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PostmanDefinitionParser extends PostmanAbstractParserParser<ApiDefinitionImport> {

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        this.projectId = request.getProjectId();
        PostmanCollection postmanCollection = JSON.parseObject(testStr, PostmanCollection.class);
        List<PostmanKeyValue> variables = postmanCollection.getVariable();
        ApiDefinitionImport apiImport = new ApiDefinitionImport();
        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();
        parseItem(postmanCollection.getItem(), variables, results,
                ApiDefinitionImportUtil.buildModule(ApiDefinitionImportUtil.getSelectModule(request.getModuleId()), postmanCollection.getInfo().getName(), this.projectId), true);
        apiImport.setData(results);
        return apiImport;
    }

    protected void parseItem(List<PostmanItem> items, List<PostmanKeyValue> variables, List<ApiDefinitionWithBLOBs> results, ApiModule parentModule, Boolean isCreateModule) {
        for (PostmanItem item : items) {
            List<PostmanItem> childItems = item.getItem();
            if (childItems != null) {
                ApiModule module = null;
                if (isCreateModule) {
                    module = ApiDefinitionImportUtil.buildModule(parentModule, item.getName(), this.projectId);
                }
                parseItem(childItems, variables, results, module, isCreateModule);
            } else {
                MsHTTPSamplerProxy msHTTPSamplerProxy = parsePostman(item);
                ApiDefinitionWithBLOBs request = buildApiDefinition(msHTTPSamplerProxy.getId(), msHTTPSamplerProxy.getName(),
                        msHTTPSamplerProxy.getPath(), msHTTPSamplerProxy.getMethod(), new ApiTestImportRequest());
                request.setPath(msHTTPSamplerProxy.getPath());
                request.setRequest(JSON.toJSONString(msHTTPSamplerProxy));

                if (request != null) {
                    results.add(request);
                }
                if (parentModule != null) {
                    request.setModuleId(parentModule.getId());
                }
            }
        }
    }
}
