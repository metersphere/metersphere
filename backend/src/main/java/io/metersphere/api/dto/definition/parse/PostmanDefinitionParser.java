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
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PostmanDefinitionParser extends PostmanAbstractParserParser<ApiDefinitionImport> {

    private ApiModule selectModule;

    private String selectModulePath;

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        this.projectId = request.getProjectId();
        PostmanCollection postmanCollection = JSON.parseObject(testStr, PostmanCollection.class);
        List<PostmanKeyValue> variables = postmanCollection.getVariable();
        ApiDefinitionImport apiImport = new ApiDefinitionImport();
        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();
        this.selectModule = ApiDefinitionImportUtil.getSelectModule(request.getModuleId());
        if (this.selectModule != null) {
            this.selectModulePath = ApiDefinitionImportUtil.getSelectModulePath(this.selectModule.getName(), this.selectModule.getParentId());
        }

        ApiModule apiModule = ApiDefinitionImportUtil.buildModule(this.selectModule, postmanCollection.getInfo().getName(), this.projectId);
        parseItem(postmanCollection.getItem(), variables, results, apiModule, apiModule.getName());
        apiImport.setData(results);
        return apiImport;
    }

    protected void parseItem(List<PostmanItem> items, List<PostmanKeyValue> variables, List<ApiDefinitionWithBLOBs> results,
                             ApiModule parentModule, String path) {
        for (PostmanItem item : items) {
            List<PostmanItem> childItems = item.getItem();
            if (childItems != null) {
                ApiModule module = null;
                module = ApiDefinitionImportUtil.buildModule(parentModule, item.getName(), this.projectId);
                parseItem(childItems, variables, results, module, path + "/" + module.getName());
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
                    if (StringUtils.isNotBlank(this.selectModulePath)) {
                        request.setModulePath(this.selectModulePath + "/" + path);
                    } else {
                        request.setModulePath("/" + path);
                    }
                }
            }
        }
    }
}
