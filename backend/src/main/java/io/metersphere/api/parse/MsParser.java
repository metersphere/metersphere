package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.parse.ApiImport;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.commons.constants.MsRequestBodyType;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpMethod;

import java.io.InputStream;

public class MsParser extends ApiImportAbstractParser {

    @Override
    public ApiImport parse(InputStream source, ApiTestImportRequest request) {
        String testStr = getApiTestStr(source);
        ApiImport apiImport = JSON.parseObject(parsePluginFormat(testStr), ApiImport.class);
        apiImport.getScenarios().forEach(scenario -> {
            setScenarioByRequest(scenario, request);
        });
        return apiImport;
    }

    private String parsePluginFormat(String testStr) {
        JSONObject testObject = JSONObject.parseObject(testStr);
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
                    JSONObject requestObject = requestsObject.getJSONObject(requestName);
                    requestObject.put("name", requestName);
                    requestObject.put("type", RequestType.HTTP);
                    JSONArray bodies = requestObject.getJSONArray("body");
                    if (StringUtils.equalsIgnoreCase(requestObject.getString("method"), HttpMethod.POST.name()) && bodies != null) {
                        StringBuilder bodyStr = new StringBuilder();
                        for (int i = 0; i < bodies.size(); i++) {
                            String body = bodies.getString(i);
                            bodyStr.append(body);
                        }
                        JSONObject bodyObject = new JSONObject();
                        bodyObject.put("raw", bodyStr);
                        bodyObject.put("type", MsRequestBodyType.RAW.value());
                        requestObject.put("body", bodyObject);
                    }
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
}
