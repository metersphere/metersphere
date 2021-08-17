package io.metersphere.api.dto.automation.parse;

import com.alibaba.fastjson.JSON;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.parse.postman.PostmanCollection;
import io.metersphere.api.dto.parse.postman.PostmanCollectionInfo;
import io.metersphere.api.dto.parse.postman.PostmanItem;
import io.metersphere.api.dto.parse.postman.PostmanKeyValue;
import io.metersphere.api.parse.PostmanAbstractParserParser;
import io.metersphere.base.domain.ApiScenarioModule;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.commons.constants.VariableTypeConstants;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PostmanScenarioParser extends PostmanAbstractParserParser<ScenarioImport> {

    @Override
    public ScenarioImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        PostmanCollection postmanCollection = JSON.parseObject(testStr, PostmanCollection.class);
        List<PostmanKeyValue> variables = postmanCollection.getVariable();
        ScenarioImport scenarioImport = new ScenarioImport();
        // 场景步骤
        LinkedList<MsTestElement> apiScenarioWithBLOBs = new LinkedList<>();
        PostmanCollectionInfo info = postmanCollection.getInfo();
        ApiScenarioWithBLOBs scenario = new ApiScenarioWithBLOBs();
        scenario.setName(info.getName());

        MsScenario msScenario = new MsScenario();
        msScenario.setName(info.getName());
        this.projectId = request.getProjectId();
        parseItem(postmanCollection.getItem(), variables, msScenario, apiScenarioWithBLOBs);
        // 生成场景对象
        List<ApiScenarioWithBLOBs> scenarioWithBLOBs = new LinkedList<>();
        parseScenarioWithBLOBs(scenarioWithBLOBs, msScenario, request);
        scenarioImport.setData(scenarioWithBLOBs);
        return scenarioImport;
    }

    private void parseScenarioWithBLOBs(List<ApiScenarioWithBLOBs> scenarioWithBLOBsList, MsScenario msScenario, ApiTestImportRequest request) {
        ApiScenarioModule selectModule = ApiScenarioImportUtil.getSelectModule(request.getModuleId());

        ApiScenarioModule module = ApiScenarioImportUtil.buildModule(selectModule, msScenario.getName(), this.projectId);
        ApiScenarioWithBLOBs scenarioWithBLOBs = parseScenario(msScenario);
        if (module != null) {
            scenarioWithBLOBs.setApiScenarioModuleId(module.getId());
            if (selectModule != null) {
                String selectModulePath = ApiScenarioImportUtil.getSelectModulePath(selectModule.getName(), selectModule.getParentId());
                scenarioWithBLOBs.setModulePath(selectModulePath + "/" + module.getName());
            } else {
                scenarioWithBLOBs.setModulePath("/" + module.getName());
            }
        }
        scenarioWithBLOBsList.add(scenarioWithBLOBs);
    }

    private void parseItem(List<PostmanItem> items, List<PostmanKeyValue> variables, MsScenario scenario, LinkedList<MsTestElement> results) {
        for (PostmanItem item : items) {
            List<PostmanItem> childItems = item.getItem();
            if (childItems != null) {
                parseItem(childItems, variables, scenario, results);
            } else {
                MsHTTPSamplerProxy request = parsePostman(item);
                if (request != null) {
                    results.add(request);
                }
            }
        }
        scenario.setVariables(parseScenarioVariable(variables));
        scenario.setHashTree(results);
    }

    private List<ScenarioVariable> parseScenarioVariable(List<PostmanKeyValue> postmanKeyValues) {
        if (postmanKeyValues == null) {
            return null;
        }
        List<ScenarioVariable> keyValues = new ArrayList<>();
        postmanKeyValues.forEach(item -> keyValues.add(new ScenarioVariable(item.getKey(), item.getValue(), item.getDescription(), VariableTypeConstants.CONSTANT.name())));
        return keyValues;
    }
}
