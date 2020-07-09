package io.metersphere.api.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.parse.ApiImport;
import io.metersphere.api.dto.parse.postman.PostmanItem;
import io.metersphere.api.dto.parse.postman.PostmanRequest;
import io.metersphere.api.dto.parse.postman.PostmanUrl;
import io.metersphere.api.dto.parse.swagger.SwaggerApi;
import io.metersphere.api.dto.parse.swagger.SwaggerInfo;
import io.metersphere.api.dto.parse.swagger.SwaggerRequest;
import io.metersphere.api.dto.scenario.Request;
import io.metersphere.api.dto.scenario.Scenario;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SwaggerParser extends ApiImportAbstractParser {

    @Override
    public ApiImport parse(InputStream source) {
        String testStr = getApiTestStr(source);

        SwaggerApi swaggerApi = JSON.parseObject(testStr.toString(), SwaggerApi.class);

        SwaggerInfo info = swaggerApi.getInfo();

        String title =  info.getTitle();


//        List<Request> requests = parseRequests(swaggerApi);
//        ApiImport apiImport = new ApiImport();
//        List<Scenario> scenarios = new ArrayList<>();
//        Scenario scenario = new Scenario();
//        scenario.setRequests(requests);
//        scenario.setName(info.getName());
//        scenarios.add(scenario);
//        apiImport.setScenarios(scenarios);
//        return apiImport;
        return null;
    }

//    private List<Request> parseRequests(SwaggerApi swaggerApi) {
//        JSONObject paths = swaggerApi.getPaths();
//
//        Set<String> pathNames = paths.keySet();
//
//        for (String path : pathNames) {
//            JSONObject pathObject = paths.getJSONObject(path);
//            Set<String> methods = pathObject.keySet();
//            for (String method : methods) {
//                SwaggerRequest swaggerRequest = JSON.parseObject(pathObject.getJSONObject(method).toJSONString(), SwaggerRequest.class);
//                Request request = new Request();
//                request.setName(swaggerRequest.getOperationId());
//                request.setUrl(url.getRaw());
//                request.setPath(.getRaw());
//                request.setUseEnvironment(false);
//                request.setMethod(requestDesc.getMethod());
//                request.setHeaders(parseKeyValue(requestDesc.getHeader()));
//                request.setParameters(parseKeyValue(url.getQuery()));
//
//            }
//        }
//
//        List<PostmanItem> item = postmanCollection.getItem();
//        List<Request> requests = new ArrayList<>();
//        for (PostmanItem requestItem : item) {
//            Request request = new Request();
//            PostmanRequest requestDesc = requestItem.getRequest();
//            PostmanUrl url = requestDesc.getUrl();
//            request.setName(requestItem.getName());
//            request.setUrl(url.getRaw());
//            request.setUseEnvironment(false);
//            request.setMethod(requestDesc.getMethod());
//            request.setHeaders(parseKeyValue(requestDesc.getHeader()));
//            request.setParameters(parseKeyValue(url.getQuery()));
//            Body body = new Body();
//            JSONObject postmanBody = requestDesc.getBody();
//            String bodyMode = postmanBody.getString("mode");
//            if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.RAW.value())) {
//                body.setRaw(postmanBody.getString(bodyMode));
//                body.setType(MsRequestBodyType.RAW.value());
//                String contentType = postmanBody.getJSONObject("options").getJSONObject("raw").getString("language");
//                List<KeyValue> headers = request.getHeaders();
//                boolean hasContentType = false;
//                for (KeyValue header : headers) {
//                    if (StringUtils.equalsIgnoreCase(header.getName(), "Content-Type")) {
//                        hasContentType = true;
//                    }
//                }
//                if (!hasContentType) {
//                    headers.add(new KeyValue("Content-Type", contentType));
//                }
//            } else if (StringUtils.equals(bodyMode, PostmanRequestBodyMode.FORM_DATA.value()) || StringUtils.equals(bodyMode, PostmanRequestBodyMode.URLENCODED.value())) {
//                List<PostmanKeyValue> postmanKeyValues = JSON.parseArray(postmanBody.getString(bodyMode), PostmanKeyValue.class);
//                body.setType(MsRequestBodyType.KV.value());
//                body.setKvs(parseKeyValue(postmanKeyValues));
//            }
//            request.setBody(body);
//            requests.add(request);
//        }
//        return requests;
//    }
}
