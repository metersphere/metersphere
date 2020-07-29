package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.parse.ApiImport;
import io.metersphere.api.dto.parse.postman.*;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.Scenario;
import io.metersphere.api.dto.scenario.request.HttpRequest;
import io.metersphere.api.dto.scenario.request.Request;
import io.metersphere.commons.constants.MsRequestBodyType;
import io.metersphere.commons.constants.PostmanRequestBodyMode;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.*;

public class PostmanParser extends ApiImportAbstractParser {

    @Override
    public ApiImport parse(InputStream source, ApiTestImportRequest request) {

        String testStr = getApiTestStr(source);
        PostmanCollection postmanCollection = JSON.parseObject(testStr, PostmanCollection.class);
        PostmanCollectionInfo info = postmanCollection.getInfo();
        List<PostmanKeyValue> variables = postmanCollection.getVariable();
        ApiImport apiImport = new ApiImport();
        List<Scenario> scenarios = new ArrayList<>();

        Scenario scenario = new Scenario();
        scenario.setName(info.getName());
        setScenarioByRequest(scenario, request);
        parseItem(postmanCollection.getItem(), scenario, variables, scenarios);
        apiImport.setScenarios(scenarios);

        return apiImport;
    }

    private List<KeyValue> parseKeyValue(List<PostmanKeyValue> postmanKeyValues) {
        if (postmanKeyValues == null) {
            return null;
        }
        List<KeyValue> keyValues = new ArrayList<>();
        postmanKeyValues.forEach(item -> keyValues.add(new KeyValue(item.getKey(), item.getValue())));
        return keyValues;
    }

    private void parseItem(List<PostmanItem> items, Scenario scenario, List<PostmanKeyValue> variables, List<Scenario> scenarios) {
        List<Request> requests = new ArrayList<>();
        for (PostmanItem item : items) {
            List<PostmanItem> childItems = item.getItem();
            if (childItems != null) {
                Scenario subScenario = new Scenario();
                subScenario.setName(item.getName());
                subScenario.setEnvironmentId(scenario.getEnvironmentId());
                parseItem(childItems, subScenario, variables, scenarios);
            } else {
                Request request = parseRequest(item);
                if (request != null) {
                    requests.add(request);
                }
            }
        }
        scenario.setVariables(parseKeyValue(variables));
        scenario.setRequests(requests);
        scenarios.add(scenario);
    }

    private Request parseRequest(PostmanItem requestItem) {
        HttpRequest request = new HttpRequest();
        PostmanRequest requestDesc = requestItem.getRequest();
        if (requestDesc == null) {
            return null;
        }
        PostmanUrl url = requestDesc.getUrl();
        request.setName(requestItem.getName());
        request.setUrl(url.getRaw());
        request.setUseEnvironment(false);
        request.setMethod(requestDesc.getMethod());
        request.setHeaders(parseKeyValue(requestDesc.getHeader()));
        request.setParameters(parseKeyValue(url.getQuery()));
        request.setBody(parseBody(requestDesc));
        return request;
    }

    private Body parseBody(PostmanRequest requestDesc) {
        Body body = new Body();
        JSONObject postmanBody = requestDesc.getBody();
        if (postmanBody == null) {
            return null;
        }
        String bodyMode = postmanBody.getString("mode");
        if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.RAW.value())) {
            body.setRaw(postmanBody.getString(bodyMode));
            body.setType(MsRequestBodyType.RAW.value());
            JSONObject options = postmanBody.getJSONObject("options");
            if (options != null) {
                JSONObject raw = options.getJSONObject(PostmanRequestBodyMode.RAW.value());
                if (raw != null) {
                    body.setFormat(raw.getString("language"));
                }
            }
        } else if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.FORM_DATA.value()) || StringUtils.equals(bodyMode, PostmanRequestBodyMode.URLENCODED.value())) {
            List<PostmanKeyValue> postmanKeyValues = JSON.parseArray(postmanBody.getString(bodyMode), PostmanKeyValue.class);
            body.setType(MsRequestBodyType.KV.value());
            body.setKvs(parseKeyValue(postmanKeyValues));
        }
        return body;
    }

}
