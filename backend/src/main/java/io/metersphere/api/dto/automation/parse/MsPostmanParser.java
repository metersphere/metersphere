package io.metersphere.api.dto.automation.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.variable.ScenarioVariable;
import io.metersphere.api.dto.parse.postman.*;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.base.domain.ApiScenarioModule;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.commons.constants.MsRequestBodyType;
import io.metersphere.commons.constants.PostmanRequestBodyMode;
import io.metersphere.commons.constants.VariableTypeConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MsPostmanParser extends ScenarioImportAbstractParser {

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
        paseScenario(scenarioWithBLOBs, msScenario, request);
        scenarioImport.setData(scenarioWithBLOBs);
        return scenarioImport;
    }

    private void paseScenario(List<ApiScenarioWithBLOBs> scenarioWithBLOBsList, MsScenario msScenario, ApiTestImportRequest request) {
        ApiScenarioModule module = buildModule(getSelectModule(request.getModuleId()), msScenario.getName());
        ApiScenarioWithBLOBs scenarioWithBLOBs = new ApiScenarioWithBLOBs();
        scenarioWithBLOBs.setName(msScenario.getName());
        scenarioWithBLOBs.setProjectId(request.getProjectId());
        if (msScenario != null && CollectionUtils.isNotEmpty(msScenario.getHashTree())) {
            scenarioWithBLOBs.setStepTotal(msScenario.getHashTree().size());
        }
        if (module != null) {
            scenarioWithBLOBs.setApiScenarioModuleId(module.getId());
            scenarioWithBLOBs.setModulePath("/" + module.getName());
        }
        scenarioWithBLOBs.setId(UUID.randomUUID().toString());
        scenarioWithBLOBs.setScenarioDefinition(JSON.toJSONString(msScenario));
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

    private MsHTTPSamplerProxy parsePostman(PostmanItem requestItem) {
        PostmanRequest requestDesc = requestItem.getRequest();
        if (requestDesc == null) {
            return null;
        }
        requestDesc.getAuth(); // todo 认证方式等待优化
        PostmanUrl url = requestDesc.getUrl();
        MsHTTPSamplerProxy request = buildRequest(requestItem.getName(), url.getRaw(), requestDesc.getMethod());
        if (StringUtils.isNotBlank(request.getPath())) {
            String path = request.getPath().split("\\?")[0];
            path = path.replace("{{", "${");
            path = path.replace("}}", "}");
            request.setPath(path);
        } else {
            request.setPath("/");
        }
        parseBody(request.getBody(), requestDesc);
        request.setArguments(parseKeyValue(url.getQuery()));
        request.setHeaders(parseKeyValue(requestDesc.getHeader()));
        addBodyHeader(request);
        addPreScript(request, requestItem.getEvent());
        return request;
    }

    private void addPreScript(MsHTTPSamplerProxy request, List<PostmanEvent> event) {
        if (request != null && CollectionUtils.isNotEmpty(event)) {
            StringBuilder scriptStr = new StringBuilder();
            event = event.stream()
                    .filter(item -> item.getScript() != null)
                    .collect(Collectors.toList());
            event.forEach(item -> {
                PostmanScript script = item.getScript();
                if (script != null) {
                    List<String> exec = script.getExec();
                    if (CollectionUtils.isNotEmpty(exec)) {
                        exec.forEach(col -> {
                            if (StringUtils.isNotEmpty(col)) {
                                scriptStr.append(col + "/n");
                            }
                        });
                    }
                }
            });
            if (StringUtils.isNotBlank(scriptStr)) {
                MsJSR223PreProcessor jsr223PreProcessor = new MsJSR223PreProcessor();
                jsr223PreProcessor.setName("JSR223PreProcessor");
                jsr223PreProcessor.setScriptLanguage("javascript");
                jsr223PreProcessor.setScript(scriptStr.toString());
                LinkedList<MsTestElement> hashTree = new LinkedList<>();
                hashTree.add(jsr223PreProcessor);
                request.setHashTree(hashTree);
            }
        }
    }

    private List<KeyValue> parseKeyValue(List<PostmanKeyValue> postmanKeyValues) {
        if (postmanKeyValues == null) {
            return null;
        }
        List<KeyValue> keyValues = new ArrayList<>();
        postmanKeyValues.forEach(item -> keyValues.add(new KeyValue(item.getKey(), item.getValue(), item.getDescription(), item.getContentType())));
        return keyValues;
    }

    private List<ScenarioVariable> parseScenarioVariable(List<PostmanKeyValue> postmanKeyValues) {
        if (postmanKeyValues == null) {
            return null;
        }
        List<ScenarioVariable> keyValues = new ArrayList<>();
        postmanKeyValues.forEach(item -> keyValues.add(new ScenarioVariable(item.getKey(), item.getValue(), item.getDescription(), VariableTypeConstants.CONSTANT.name())));
        return keyValues;
    }

    private void parseBody(Body body, PostmanRequest requestDesc) {
        JSONObject postmanBody = requestDesc.getBody();
        if (postmanBody == null) {
            return;
        }
        String bodyMode = postmanBody.getString("mode");
        if (StringUtils.isNotEmpty(bodyMode) && StringUtils.equals(bodyMode, PostmanRequestBodyMode.RAW.value())) {
            parseRawBody(body, postmanBody, bodyMode);
        } else if (StringUtils.isNotEmpty(bodyMode) && StringUtils.equalsAny(bodyMode, PostmanRequestBodyMode.FORM_DATA.value(), PostmanRequestBodyMode.URLENCODED.value())) {
            List<PostmanKeyValue> postmanKeyValues = JSON.parseArray(postmanBody.getString(bodyMode), PostmanKeyValue.class);
            body.setKvs(parseKeyValue(postmanKeyValues));
            if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.FORM_DATA.value())) {
                body.setType(Body.FORM_DATA);
            } else if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.URLENCODED.value())) {
                body.setType(Body.WWW_FROM);
            }
        } else if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.FILE.value())) {
            body.setType(Body.BINARY);
            body.setKvs(new ArrayList<>());
        }
    }

    private void parseRawBody(Body body, JSONObject postmanBody, String bodyMode) {
        body.setRaw(postmanBody.getString(bodyMode));
        body.setType(MsRequestBodyType.RAW.value());
        JSONObject options = postmanBody.getJSONObject("options");
        if (options != null) {
            JSONObject raw = options.getJSONObject(PostmanRequestBodyMode.RAW.value());
            if (raw != null && raw.getString("language") != null) {
                String bodyType = "";
                switch (raw.getString("language")) {
                    case "json":
                        bodyType = Body.JSON;
                        break;
                    case "xml":
                        bodyType = Body.XML;
                        break;
                    default:
                        bodyType = Body.RAW;
                }
                body.setType(bodyType);
            }
        }
    }

}
