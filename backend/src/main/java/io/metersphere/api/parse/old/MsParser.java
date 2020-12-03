package io.metersphere.api.parse.old;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.api.dto.parse.ApiImport;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.commons.constants.MsRequestBodyType;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

public class MsParser extends ApiImportAbstractParser {

    @Override
    public ApiImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        ApiImport apiImport = JSON.parseObject(parsePluginFormat(testStr), ApiImport.class);
        apiImport.getScenarios().forEach(scenario -> setScenarioByRequest(scenario, request));
        return apiImport;
    }

    @Override
    public ApiDefinitionImport parseApi(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        ApiDefinitionImport apiImport = JSON.parseObject(testStr, ApiDefinitionImport.class);
        return apiImport;
    }

    private String parsePluginFormat(String testStr) {
        JSONObject testObject = JSONObject.parseObject(testStr, Feature.OrderedField);
        if (testObject.get("scenarios") != null) {
            return testStr;
        } else {
            //插件格式
            JSONArray scenarios = new JSONArray();
            testObject.keySet().forEach(scenarioName -> {
                JSONObject scenario = new JSONObject();
                scenario.put("name", scenarioName);
                JSONArray requestsObjects = new JSONArray();
                JSONObject requestsObject = testObject.getJSONObject(scenarioName);
                requestsObject.keySet().forEach(requestName -> {
                    JSONObject requestObject = new JSONObject(true);
                    JSONObject requestTmpObject = requestsObject.getJSONObject(requestName);
                    //排序，确保type在第一个，否则转换失败
                    if (StringUtils.isBlank(requestTmpObject.getString("type"))) {
                        requestObject.put("type", RequestType.HTTP);
                    }

                    requestTmpObject.keySet().forEach(key -> requestObject.put(key, requestTmpObject.get(key)));
                    requestObject.put("name", requestName);
                    parseBody(requestObject);
                    requestsObjects.add(requestObject);
                });
                scenario.put("requests", requestsObjects);
                scenarios.add(scenario);
            });
            JSONObject result = new JSONObject();
            result.put("scenarios", scenarios);
            return result.toJSONString();
        }
    }

    private void parseBody(JSONObject requestObject) {
        if (requestObject.containsKey("body")) {
            Object body = requestObject.get("body");
            if (body instanceof JSONArray) {
                JSONArray bodies = requestObject.getJSONArray("body");
                if (bodies != null) {
                    StringBuilder bodyStr = new StringBuilder();
                    for (int i = 0; i < bodies.size(); i++) {
                        String tmp = bodies.getString(i);
                        bodyStr.append(tmp);
                    }
                    JSONObject bodyObject = new JSONObject();
                    bodyObject.put("raw", bodyStr);
                    bodyObject.put("type", MsRequestBodyType.RAW.value());
                    requestObject.put("body", bodyObject);
                }
            } else if (body instanceof JSONObject) {
                JSONObject bodyObj = requestObject.getJSONObject("body");
                if (bodyObj != null) {
                    JSONArray kvs = new JSONArray();
                    bodyObj.keySet().forEach(key -> {
                        JSONObject kv = new JSONObject();
                        kv.put("name", key);
                        kv.put("value", bodyObj.getString(key));
                        kvs.add(kv);
                    });
                    JSONObject bodyRes = new JSONObject();
                    bodyRes.put("kvs", kvs);
                    bodyRes.put("type", MsRequestBodyType.KV.value());
                    requestObject.put("body", bodyRes);
                }
            }
        }
    }
}
