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

//        String testStr = "{\n" +
//                "\t\"info\": {\n" +
//                "\t\t\"_postman_id\": \"9721cd51-8626-4f61-9ac1-e77b8399cca8\",\n" +
//                "\t\t\"name\": \"test\",\n" +
//                "\t\t\"schema\": \"https://schema.getpostman.com/json/collection/v2.1.0/collection.json\"\n" +
//                "\t},\n" +
//                "\t\"item\": [\n" +
//                "\t\t{\n" +
//                "\t\t\t\"name\": \"test\",\n" +
//                "\t\t\t\"request\": {\n" +
//                "\t\t\t\t\"auth\": {\n" +
//                "\t\t\t\t\t\"type\": \"basic\",\n" +
//                "\t\t\t\t\t\"basic\": [\n" +
//                "\t\t\t\t\t\t{\n" +
//                "\t\t\t\t\t\t\t\"key\": \"password\",\n" +
//                "\t\t\t\t\t\t\t\"value\": \"test\",\n" +
//                "\t\t\t\t\t\t\t\"type\": \"string\"\n" +
//                "\t\t\t\t\t\t},\n" +
//                "\t\t\t\t\t\t{\n" +
//                "\t\t\t\t\t\t\t\"key\": \"username\",\n" +
//                "\t\t\t\t\t\t\t\"value\": \"test\",\n" +
//                "\t\t\t\t\t\t\t\"type\": \"string\"\n" +
//                "\t\t\t\t\t\t}\n" +
//                "\t\t\t\t\t]\n" +
//                "\t\t\t\t},\n" +
//                "\t\t\t\t\"method\": \"POST\",\n" +
//                "\t\t\t\t\"header\": [\n" +
//                "\t\t\t\t\t{\n" +
//                "\t\t\t\t\t\t\"key\": \"aaa\",\n" +
//                "\t\t\t\t\t\t\"value\": \"testH\",\n" +
//                "\t\t\t\t\t\t\"type\": \"text\"\n" +
//                "\t\t\t\t\t}\n" +
//                "\t\t\t\t],\n" +
//                "\t\t\t\t\"body\": {\n" +
//                "\t\t\t\t\t\"mode\": \"raw\",\n" +
//                "\t\t\t\t\t\"raw\": \"<html>\\n</html>\\n\",\n" +
//                "\t\t\t\t\t\"options\": {\n" +
//                "\t\t\t\t\t\t\"raw\": {\n" +
//                "\t\t\t\t\t\t\t\"language\": \"html\"\n" +
//                "\t\t\t\t\t\t}\n" +
//                "\t\t\t\t\t}\n" +
//                "\t\t\t\t},\n" +
//                "\t\t\t\t\"url\": {\n" +
//                "\t\t\t\t\t\"raw\": \"https://localhost:8080?tset=test\",\n" +
//                "\t\t\t\t\t\"protocol\": \"https\",\n" +
//                "\t\t\t\t\t\"host\": [\n" +
//                "\t\t\t\t\t\t\"localhost\"\n" +
//                "\t\t\t\t\t],\n" +
//                "\t\t\t\t\t\"port\": \"8080\",\n" +
//                "\t\t\t\t\t\"query\": [\n" +
//                "\t\t\t\t\t\t{\n" +
//                "\t\t\t\t\t\t\t\"key\": \"tset\",\n" +
//                "\t\t\t\t\t\t\t\"value\": \"test\",\n" +
//                "\t\t\t\t\t\t\t\"description\": \"test\"\n" +
//                "\t\t\t\t\t\t}\n" +
//                "\t\t\t\t\t]\n" +
//                "\t\t\t\t},\n" +
//                "\t\t\t\t\"description\": \"dd\"\n" +
//                "\t\t\t},\n" +
//                "\t\t\t\"response\": []\n" +
//                "\t\t},\n" +
//                "\t\t{\n" +
//                "\t\t\t\"name\": \"test Copy\",\n" +
//                "\t\t\t\"request\": {\n" +
//                "\t\t\t\t\"auth\": {\n" +
//                "\t\t\t\t\t\"type\": \"basic\",\n" +
//                "\t\t\t\t\t\"basic\": [\n" +
//                "\t\t\t\t\t\t{\n" +
//                "\t\t\t\t\t\t\t\"key\": \"password\",\n" +
//                "\t\t\t\t\t\t\t\"value\": \"test\",\n" +
//                "\t\t\t\t\t\t\t\"type\": \"string\"\n" +
//                "\t\t\t\t\t\t},\n" +
//                "\t\t\t\t\t\t{\n" +
//                "\t\t\t\t\t\t\t\"key\": \"username\",\n" +
//                "\t\t\t\t\t\t\t\"value\": \"test\",\n" +
//                "\t\t\t\t\t\t\t\"type\": \"string\"\n" +
//                "\t\t\t\t\t\t}\n" +
//                "\t\t\t\t\t]\n" +
//                "\t\t\t\t},\n" +
//                "\t\t\t\t\"method\": \"POST\",\n" +
//                "\t\t\t\t\"header\": [\n" +
//                "\t\t\t\t\t{\n" +
//                "\t\t\t\t\t\t\"key\": \"testH\",\n" +
//                "\t\t\t\t\t\t\"value\": \"testH\",\n" +
//                "\t\t\t\t\t\t\"type\": \"text\"\n" +
//                "\t\t\t\t\t}\n" +
//                "\t\t\t\t],\n" +
//                "\t\t\t\t\"body\": {\n" +
//                "\t\t\t\t\t\"mode\": \"raw\",\n" +
//                "\t\t\t\t\t\"raw\": \"{\\\"name\\\":\\\"test\\\"}\\n\",\n" +
//                "\t\t\t\t\t\"options\": {\n" +
//                "\t\t\t\t\t\t\"raw\": {\n" +
//                "\t\t\t\t\t\t\t\"language\": \"json\"\n" +
//                "\t\t\t\t\t\t}\n" +
//                "\t\t\t\t\t}\n" +
//                "\t\t\t\t},\n" +
//                "\t\t\t\t\"url\": {\n" +
//                "\t\t\t\t\t\"raw\": \"http://localhost:8081\",\n" +
//                "\t\t\t\t\t\"protocol\": \"http\",\n" +
//                "\t\t\t\t\t\"host\": [\n" +
//                "\t\t\t\t\t\t\"localhost\"\n" +
//                "\t\t\t\t\t],\n" +
//                "\t\t\t\t\t\"port\": \"8081\"\n" +
//                "\t\t\t\t},\n" +
//                "\t\t\t\t\"description\": \"dd\"\n" +
//                "\t\t\t},\n" +
//                "\t\t\t\"response\": []\n" +
//                "\t\t},\n" +
//                "\t\t{\n" +
//                "\t\t\t\"name\": \"test Copy Copy\",\n" +
//                "\t\t\t\"request\": {\n" +
//                "\t\t\t\t\"auth\": {\n" +
//                "\t\t\t\t\t\"type\": \"basic\",\n" +
//                "\t\t\t\t\t\"basic\": [\n" +
//                "\t\t\t\t\t\t{\n" +
//                "\t\t\t\t\t\t\t\"key\": \"password\",\n" +
//                "\t\t\t\t\t\t\t\"value\": \"test\",\n" +
//                "\t\t\t\t\t\t\t\"type\": \"string\"\n" +
//                "\t\t\t\t\t\t},\n" +
//                "\t\t\t\t\t\t{\n" +
//                "\t\t\t\t\t\t\t\"key\": \"username\",\n" +
//                "\t\t\t\t\t\t\t\"value\": \"test\",\n" +
//                "\t\t\t\t\t\t\t\"type\": \"string\"\n" +
//                "\t\t\t\t\t\t}\n" +
//                "\t\t\t\t\t]\n" +
//                "\t\t\t\t},\n" +
//                "\t\t\t\t\"method\": \"POST\",\n" +
//                "\t\t\t\t\"header\": [\n" +
//                "\t\t\t\t\t{\n" +
//                "\t\t\t\t\t\t\"key\": \"testH\",\n" +
//                "\t\t\t\t\t\t\"value\": \"testH\",\n" +
//                "\t\t\t\t\t\t\"type\": \"text\"\n" +
//                "\t\t\t\t\t}\n" +
//                "\t\t\t\t],\n" +
//                "\t\t\t\t\"body\": {\n" +
//                "\t\t\t\t\t\"mode\": \"urlencoded\",\n" +
//                "\t\t\t\t\t\"urlencoded\": [\n" +
//                "\t\t\t\t\t\t{\n" +
//                "\t\t\t\t\t\t\t\"key\": \"test\",\n" +
//                "\t\t\t\t\t\t\t\"value\": \"tset\",\n" +
//                "\t\t\t\t\t\t\t\"description\": \"dd\",\n" +
//                "\t\t\t\t\t\t\t\"type\": \"text\"\n" +
//                "\t\t\t\t\t\t}\n" +
//                "\t\t\t\t\t],\n" +
//                "\t\t\t\t\t\"options\": {\n" +
//                "\t\t\t\t\t\t\"raw\": {\n" +
//                "\t\t\t\t\t\t\t\"language\": \"json\"\n" +
//                "\t\t\t\t\t\t}\n" +
//                "\t\t\t\t\t}\n" +
//                "\t\t\t\t},\n" +
//                "\t\t\t\t\"url\": {\n" +
//                "\t\t\t\t\t\"raw\": \"http://localhost:8081\",\n" +
//                "\t\t\t\t\t\"protocol\": \"http\",\n" +
//                "\t\t\t\t\t\"host\": [\n" +
//                "\t\t\t\t\t\t\"localhost\"\n" +
//                "\t\t\t\t\t],\n" +
//                "\t\t\t\t\t\"port\": \"8081\"\n" +
//                "\t\t\t\t},\n" +
//                "\t\t\t\t\"description\": \"dd\"\n" +
//                "\t\t\t},\n" +
//                "\t\t\t\"response\": []\n" +
//                "\t\t}\n" +
//                "\t],\n" +
//                "\t\"protocolProfileBehavior\": {}\n" +
//                "}";

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
