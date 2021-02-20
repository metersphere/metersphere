package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.parse.postman.PostmanCollection;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiModule;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ScenarioPostmanParser extends PostmanParser {

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        this.projectId = request.getProjectId();
        ApiDefinitionImport apiImport = new ApiDefinitionImport();
        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();
        PostmanCollection postmanCollection = JSON.parseObject(getApiTestStr(source), PostmanCollection.class);
        parseItem(postmanCollection.getItem(), postmanCollection.getVariable(), results, null, false);

        MsScenario msScenario = new MsScenario();
        LinkedList<MsTestElement> msHTTPSamplerProxies = new LinkedList<>();
        results.forEach(res -> {
            msHTTPSamplerProxies.add(JSONObject.parseObject(res.getRequest(), MsHTTPSamplerProxy.class));
        });
        msScenario.setHashTree(msHTTPSamplerProxies);
        msScenario.setType("scenario");
        msScenario.setName(postmanCollection.getInfo().getName());
        apiImport.setScenarioDefinition(msScenario);
        return apiImport;
    }
}
