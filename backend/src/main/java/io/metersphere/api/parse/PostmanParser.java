package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.parse.ApiImport;
import io.metersphere.api.dto.parse.postman.*;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.Request;
import io.metersphere.api.dto.scenario.Scenario;
import io.metersphere.commons.constants.MsRequestBodyType;
import io.metersphere.commons.constants.PostmanRequestBodyMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PostmanParser extends ApiImportAbstractParser {

    @Override
    public ApiImport parse(InputStream source) {
        String testStr = getApiTestStr(source);
        PostmanCollection postmanCollection = JSON.parseObject(testStr.toString(), PostmanCollection.class);
        PostmanCollectionInfo info = postmanCollection.getInfo();
        List<Request> requests = parseRequests(postmanCollection);
        ApiImport apiImport = new ApiImport();
        List<Scenario> scenarios = new ArrayList<>();
        Scenario scenario = new Scenario();
        scenario.setRequests(requests);
        scenario.setName(info.getName());
        scenarios.add(scenario);
        apiImport.setScenarios(scenarios);
        return apiImport;
    }

    private List<KeyValue> parseKeyValue(List<PostmanKeyValue> postmanKeyValues) {
        if (postmanKeyValues == null) {
            return null;
        }
        List<KeyValue> keyValues = new ArrayList<>();
        postmanKeyValues.forEach(item -> {
            keyValues.add(new KeyValue(item.getKey(), item.getValue()));
        });
        return keyValues;
    }

    private List<Request> parseRequests(PostmanCollection postmanCollection) {
        List<PostmanItem> item = postmanCollection.getItem();
        List<Request> requests = new ArrayList<>();
        for (PostmanItem requestItem : item) {
            Request request = new Request();
            PostmanRequest requestDesc = requestItem.getRequest();
            PostmanUrl url = requestDesc.getUrl();
            request.setName(requestItem.getName());
            request.setUrl(url.getRaw());
            request.setUseEnvironment(false);
            request.setMethod(requestDesc.getMethod());
            request.setHeaders(parseKeyValue(requestDesc.getHeader()));
            request.setParameters(parseKeyValue(url.getQuery()));
            Body body = new Body();
            JSONObject postmanBody = requestDesc.getBody();
            String bodyMode = postmanBody.getString("mode");
            if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.RAW.value())) {
                body.setRaw(postmanBody.getString(bodyMode));
                body.setType(MsRequestBodyType.RAW.value());
                String contentType = postmanBody.getJSONObject("options").getJSONObject("raw").getString("language");
                List<KeyValue> headers = request.getHeaders();
                boolean hasContentType = false;
                for (KeyValue header : headers) {
                    if (StringUtils.equalsIgnoreCase(header.getName(), "Content-Type")) {
                        hasContentType = true;
                    }
                }
                if (!hasContentType) {
                    headers.add(new KeyValue("Content-Type", contentType));
                }
            } else if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.FORM_DATA.value()) || StringUtils.equals(bodyMode, PostmanRequestBodyMode.URLENCODED.value())) {
                List<PostmanKeyValue> postmanKeyValues = JSON.parseArray(postmanBody.getString(bodyMode), PostmanKeyValue.class);
                body.setType(MsRequestBodyType.KV.value());
                body.setKvs(parseKeyValue(postmanKeyValues));
            }
            request.setBody(body);
            requests.add(request);
        }
        return requests;
    }

}
