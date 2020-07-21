package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.parse.ApiImport;
import io.metersphere.api.dto.parse.postman.*;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.request.HttpRequest;
import io.metersphere.api.dto.scenario.Scenario;
import io.metersphere.api.dto.scenario.request.Request;
import io.metersphere.commons.constants.MsRequestBodyType;
import io.metersphere.commons.constants.PostmanRequestBodyMode;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostmanParser extends ApiImportAbstractParser {

    private static Map<String, String> postmanBodyRowMap;

    static {
        postmanBodyRowMap = new HashMap<>();
        postmanBodyRowMap.put("json", "application/json");
        postmanBodyRowMap.put("text", "text/plain");
        postmanBodyRowMap.put("html", "text/html");
        postmanBodyRowMap.put("xml", "text/xml");
        postmanBodyRowMap.put("javascript", "application/x-javascript");
    }

    @Override
    public ApiImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        PostmanCollection postmanCollection = JSON.parseObject(testStr, PostmanCollection.class);
        PostmanCollectionInfo info = postmanCollection.getInfo();
        List<Request> requests = parseRequests(postmanCollection);
        ApiImport apiImport = new ApiImport();
        List<Scenario> scenarios = new ArrayList<>();
        Scenario scenario = new Scenario();
        scenario.setRequests(requests);
        scenario.setName(info.getName());
        setScenarioByRequest(scenario, request);
        scenarios.add(scenario);
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

    private List<Request> parseRequests(PostmanCollection postmanCollection) {
        List<PostmanItem> item = postmanCollection.getItem();
        List<Request> requests = new ArrayList<>();
        for (PostmanItem requestItem : item) {
            HttpRequest request = new HttpRequest();
            PostmanRequest requestDesc = requestItem.getRequest();
            PostmanUrl url = requestDesc.getUrl();
            request.setName(requestItem.getName());
            request.setUrl(url.getRaw());
            request.setUseEnvironment(false);
            request.setMethod(requestDesc.getMethod());
            request.setHeaders(parseKeyValue(requestDesc.getHeader()));
            request.setParameters(parseKeyValue(url.getQuery()));
            request.setBody(parseBody(requestDesc, request));
            requests.add(request);
        }
        return requests;
    }

    private Body parseBody(PostmanRequest requestDesc, HttpRequest request) {
        Body body = new Body();
        JSONObject postmanBody = requestDesc.getBody();
        if (postmanBody == null) {
            return null;
        }
        String bodyMode = postmanBody.getString("mode");
        if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.RAW.value())) {
            body.setRaw(postmanBody.getString(bodyMode));
            body.setType(MsRequestBodyType.RAW.value());
            String contentType = postmanBodyRowMap.get(postmanBody.getJSONObject("options").getJSONObject("raw").getString("language"));
            addContentType(request, contentType);
        } else if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.FORM_DATA.value()) || StringUtils.equals(bodyMode, PostmanRequestBodyMode.URLENCODED.value())) {
            List<PostmanKeyValue> postmanKeyValues = JSON.parseArray(postmanBody.getString(bodyMode), PostmanKeyValue.class);
            body.setType(MsRequestBodyType.KV.value());
            body.setKvs(parseKeyValue(postmanKeyValues));
        }
        return body;
    }

}
