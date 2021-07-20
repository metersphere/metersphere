package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import io.metersphere.api.dto.mockconfig.MockConfigRequest;
import io.metersphere.api.dto.mockconfig.MockExpectConfigRequest;
import io.metersphere.api.dto.mockconfig.response.JsonSchemaReturnObj;
import io.metersphere.api.dto.mockconfig.response.MockConfigResponse;
import io.metersphere.api.dto.mockconfig.response.MockExpectConfigResponse;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.MockConfigMapper;
import io.metersphere.base.mapper.MockExpectConfigMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.JsonPathUtils;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.XML;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class MockConfigService {

    @Resource
    private MockConfigMapper mockConfigMapper;
    @Resource
    private MockExpectConfigMapper mockExpectConfigMapper;
    @Resource
    private ApiDefinitionService apiDefinitionService;

    public MockConfigResponse findByApiIdList(List<String> apiIdList) {
        if (apiIdList.isEmpty()) {
            return new MockConfigResponse(null, new ArrayList<>());
        }
        MockConfigExample example = new MockConfigExample();
        MockConfigExample.Criteria criteria = example.createCriteria();
        criteria.andApiIdIn(apiIdList);
        List<MockConfig> configList = mockConfigMapper.selectByExample(example);
        return this.assemblyMockConfingResponse(configList);
    }

    private MockConfigResponse assemblyMockConfingResponse(List<MockConfig> configList) {
        if (!configList.isEmpty()) {
            MockConfig config = configList.get(0);
            MockExpectConfigExample expectConfigExample = new MockExpectConfigExample();
            expectConfigExample.createCriteria().andMockConfigIdEqualTo(config.getId());
            expectConfigExample.setOrderByClause("update_time DESC");

            List<MockExpectConfigResponse> expectConfigResponseList = new ArrayList<>();

            List<MockExpectConfigWithBLOBs> expectConfigList = mockExpectConfigMapper.selectByExampleWithBLOBs(expectConfigExample);
            for (MockExpectConfigWithBLOBs expectConfig :
                    expectConfigList) {
                MockExpectConfigResponse response = new MockExpectConfigResponse(expectConfig);
                expectConfigResponseList.add(response);

            }
            MockConfigResponse returnRsp = new MockConfigResponse(config, expectConfigResponseList);
            return returnRsp;
        } else {
            return new MockConfigResponse(null, new ArrayList<>());
        }
    }

    public MockConfigResponse genMockConfig(MockConfigRequest request) {
        MockConfigResponse returnRsp = null;

        MockConfigExample example = new MockConfigExample();
        MockConfigExample.Criteria criteria = example.createCriteria();
        if (request.getId() != null) {
            criteria.andIdEqualTo(request.getId());
        }
        if (request.getApiId() != null) {
            criteria.andApiIdEqualTo(request.getApiId());
        }
        if (request.getProjectId() != null) {
            criteria.andProjectIdEqualTo(request.getProjectId());
        }

        List<MockConfig> configList = mockConfigMapper.selectByExample(example);
        if (configList.isEmpty()) {
            long createTimeStmp = System.currentTimeMillis();

            MockConfig config = new MockConfig();
            config.setProjectId(request.getProjectId());
            config.setApiId(request.getApiId());
            config.setId(UUID.randomUUID().toString());
            config.setCreateUserId(SessionUtils.getUserId());
            config.setCreateTime(createTimeStmp);
            config.setUpdateTime(createTimeStmp);
            mockConfigMapper.insert(config);
            returnRsp = new MockConfigResponse(config, new ArrayList<>());
        } else {
            MockConfig config = configList.get(0);
            MockExpectConfigExample expectConfigExample = new MockExpectConfigExample();
            expectConfigExample.createCriteria().andMockConfigIdEqualTo(config.getId());
            expectConfigExample.setOrderByClause("update_time DESC");

            List<MockExpectConfigResponse> expectConfigResponseList = new ArrayList<>();

            List<MockExpectConfigWithBLOBs> expectConfigList = mockExpectConfigMapper.selectByExampleWithBLOBs(expectConfigExample);
            for (MockExpectConfigWithBLOBs expectConfig :
                    expectConfigList) {
                MockExpectConfigResponse response = new MockExpectConfigResponse(expectConfig);
                expectConfigResponseList.add(response);

            }
            returnRsp = new MockConfigResponse(config, expectConfigResponseList);
        }

        return returnRsp;
    }

    public MockExpectConfig updateMockExpectConfig(MockExpectConfigRequest request) {
        boolean isSave = false;
        if (StringUtils.isEmpty(request.getId())) {
            isSave = true;
            request.setId(UUID.randomUUID().toString());
        }

        //检查名称是否存在
        if (request.getName() != null) {
            this.checkNameIsExists(request);
        }
        long timeStmp = System.currentTimeMillis();
        MockExpectConfigWithBLOBs model = new MockExpectConfigWithBLOBs();
        model.setId(request.getId());
        model.setMockConfigId(request.getMockConfigId());
        model.setUpdateTime(timeStmp);
        model.setStatus(request.getStatus());
        if (request.getTags() != null) {
            model.setTags(JSONArray.toJSONString(request.getTags()));
        }
        model.setName(request.getName());
        if (request.getRequest() != null) {
            model.setRequest(JSONObject.toJSONString(request.getRequest()));
        }
        if (request.getResponse() != null) {
            model.setResponse(JSONObject.toJSONString(request.getResponse()));
        }
        if (isSave) {
            model.setCreateTime(timeStmp);
            model.setCreateUserId(SessionUtils.getUserId());
            model.setStatus("true");
            mockExpectConfigMapper.insert(model);
        } else {
            mockExpectConfigMapper.updateByPrimaryKeySelective(model);
        }
        return model;
    }

    private void checkNameIsExists(MockExpectConfigRequest request) {
        MockExpectConfigExample example = new MockExpectConfigExample();
        example.createCriteria().andMockConfigIdEqualTo(request.getMockConfigId()).andNameEqualTo(request.getName().trim()).andIdNotEqualTo(request.getId());
        long count = mockExpectConfigMapper.countByExample(example);
        if (count > 0) {
            MSException.throwException(Translator.get("expect_name_exists") + ":" + request.getName());
        }
    }

    public MockExpectConfigResponse findExpectConfig(List<MockExpectConfigResponse> mockExpectConfigList, JSONObject reqJsonObj) {
        MockExpectConfigResponse returnModel = null;

        if (reqJsonObj == null || reqJsonObj.isEmpty()) {
            for (MockExpectConfigResponse model : mockExpectConfigList) {
                if (!model.isStatus()) {
                    continue;
                }
                JSONObject requestObj = model.getRequest();
                boolean isJsonParam = requestObj.getBoolean("jsonParam");
                if (isJsonParam) {
                    if(StringUtils.isEmpty(requestObj.getString("jsonData"))){
                        return model;
                    }
                } else {
                    JSONObject mockExpectJson = new JSONObject();
                    JSONArray jsonArray = requestObj.getJSONArray("variables");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String name = "";
                        String value = "";
                        if (object.containsKey("name")) {
                            name = String.valueOf(object.get("name")).trim();
                        }
                        if (object.containsKey("value")) {
                            value = String.valueOf(object.get("value")).trim();
                        }
                        if (StringUtils.isNotEmpty(name)) {
                            mockExpectJson.put(name, value);
                        }
                    }
                    if (mockExpectJson.isEmpty()) {
                        return model;
                    }
                }
            }
        }
        for (MockExpectConfigResponse model : mockExpectConfigList) {
            try {
                if (!model.isStatus()) {
                    continue;
                }
                JSONObject requestObj = model.getRequest();
                boolean isJsonParam = requestObj.getBoolean("jsonParam");
                JSONObject mockExpectJson = new JSONObject();
                if (isJsonParam) {
                    String jsonParams = requestObj.getString("jsonData");
                    JSONValidator jsonValidator = JSONValidator.from(jsonParams);
                    if(StringUtils.equalsIgnoreCase("Array",jsonValidator.getType().name())){
                        JSONArray mockExpectArr = JSONArray.parseArray(jsonParams);
                        for(int expectIndex = 0;expectIndex < mockExpectArr.size(); expectIndex ++){
                            JSONObject itemObj = mockExpectArr.getJSONObject(expectIndex);
                            mockExpectJson = itemObj;
                        }
                    }else if(StringUtils.equalsIgnoreCase("Object",jsonValidator.getType().name())){
                        JSONObject mockExpectJsonItem = JSONObject.parseObject(jsonParams);
                        mockExpectJson = mockExpectJsonItem;
                    }
                } else {
                    JSONArray jsonArray = requestObj.getJSONArray("variables");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String name = "";
                        String value = "";
                        if (object.containsKey("name")) {
                            name = String.valueOf(object.get("name")).trim();
                        }
                        if (object.containsKey("value")) {
                            value = String.valueOf(object.get("value")).trim();
                        }
                        if (StringUtils.isNotEmpty(name)) {
                            mockExpectJson.put(name, value);
                        }
                    }
                }

                boolean mathing = JsonPathUtils.checkJsonObjCompliance(reqJsonObj, mockExpectJson);
                if (mathing) {
                    returnModel = model;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return returnModel;
    }

    public MockExpectConfigResponse findExpectConfig(List<MockExpectConfigResponse> mockExpectConfigList, JSONArray reqJsonArray) {
        MockExpectConfigResponse returnModel = null;

        if (reqJsonArray == null || reqJsonArray.isEmpty()) {
            for (MockExpectConfigResponse model : mockExpectConfigList) {
                if (!model.isStatus()) {
                    continue;
                }
                JSONObject requestObj = model.getRequest();
                boolean isJsonParam = requestObj.getBoolean("jsonParam");

                if (isJsonParam) {
                    if(StringUtils.isEmpty(requestObj.getString("jsonData"))){
                        return model;
                    }
                } else {
                    JSONObject mockExpectJson = new JSONObject();
                    JSONArray jsonArray = requestObj.getJSONArray("variables");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String name = "";
                        String value = "";
                        if (object.containsKey("name")) {
                            name = String.valueOf(object.get("name")).trim();
                        }
                        if (object.containsKey("value")) {
                            value = String.valueOf(object.get("value")).trim();
                        }
                        if (StringUtils.isNotEmpty(name)) {
                            mockExpectJson.put(name, value);
                        }
                    }
                    if (mockExpectJson.isEmpty()) {
                        return model;
                    }
                }
            }
        }
        for (MockExpectConfigResponse model : mockExpectConfigList) {
            try {
                if (!model.isStatus()) {
                    continue;
                }
                JSONObject requestObj = model.getRequest();
                boolean isJsonParam = requestObj.getBoolean("jsonParam");
                boolean mathing = false;
                if (isJsonParam) {
                    String jsonParams = requestObj.getString("jsonData");
                    JSONValidator jsonValidator = JSONValidator.from(jsonParams);
                    if(StringUtils.equalsIgnoreCase("Array",jsonValidator.getType().name())){
                        JSONArray mockExpectArr = JSONArray.parseArray(jsonParams);
                        for(int expectIndex = 0;expectIndex < mockExpectArr.size(); expectIndex ++){
                            JSONObject itemObj = mockExpectArr.getJSONObject(expectIndex);
                            mathing = JsonPathUtils.checkJsonArrayCompliance(reqJsonArray, itemObj);
                            if(!mathing){
                                break;
                            }
                        }
                    }else if(StringUtils.equalsIgnoreCase("Object",jsonValidator.getType().name())){
                        JSONObject mockExpectJson = JSONObject.parseObject(jsonParams);
                        mathing = JsonPathUtils.checkJsonArrayCompliance(reqJsonArray, mockExpectJson);
                    }

                } else {
                    JSONArray jsonArray = requestObj.getJSONArray("variables");
                    JSONObject mockExpectJson = new JSONObject();
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String name = "";
                        String value = "";
                        if (object.containsKey("name")) {
                            name = String.valueOf(object.get("name")).trim();
                        }
                        if (object.containsKey("value")) {
                            value = String.valueOf(object.get("value")).trim();
                        }
                        if (StringUtils.isNotEmpty(name)) {
                            mockExpectJson.put(name, value);
                        }
                    }
                    mathing = JsonPathUtils.checkJsonArrayCompliance(reqJsonArray, mockExpectJson);
                }
                if (mathing) {
                    returnModel = model;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return returnModel;
    }

    public String updateHttpServletResponse(MockExpectConfigResponse finalExpectConfig, HttpServletResponse response) {
        String returnStr = "";
        try {
            //设置响应头和响应码
            JSONObject responseObj = finalExpectConfig.getResponse();
            String httpCode = responseObj.getString("httpCode");
            JSONArray jsonArray = responseObj.getJSONArray("httpHeads");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                String name = null;
                String value = "";
                if (object.containsKey("name")) {
                    name = String.valueOf(object.get("name")).trim();
                }
                if (object.containsKey("value")) {
                    value = String.valueOf(object.get("value")).trim();
                }
                if (StringUtils.isNotEmpty(name)) {
                    response.setHeader(name, value);
                }
            }
            response.setStatus(Integer.parseInt(httpCode));

            returnStr = String.valueOf(responseObj.get("body"));

            if (responseObj.containsKey("delayed")) {
                try {
                    long sleepTime = Long.parseLong(String.valueOf(responseObj.get("delayed")));
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnStr;
    }

    public String updateHttpServletResponse(List<ApiDefinitionWithBLOBs> apis, HttpServletResponse response) {
        String returnStr = "";
        try {
            if(CollectionUtils.isEmpty(apis)){
                response.setStatus(404);
            }else {
                for (ApiDefinitionWithBLOBs api : apis) {
                    int status = 404;
                    if (api.getResponse() != null) {
                        JSONObject respObj = JSONObject.parseObject(api.getResponse());
                        if (respObj.containsKey("headers")) {
                            JSONArray headersArr = respObj.getJSONArray("headers");
                            for (int i = 0; i < headersArr.size(); i++) {
                                JSONObject obj = headersArr.getJSONObject(i);
                                if (obj.containsKey("name") && obj.containsKey("value") && StringUtils.isNotEmpty(obj.getString("name"))) {
                                    response.setHeader(obj.getString("name"), obj.getString("value"));
                                }
                            }
                        }
                        if (respObj.containsKey("statusCode")) {
                            JSONArray statusCodeArr = respObj.getJSONArray("statusCode");
                            for (int i = 0; i < statusCodeArr.size(); i++) {
                                JSONObject obj = statusCodeArr.getJSONObject(i);
                                if (obj.containsKey("name") && obj.containsKey("value") && StringUtils.isNotEmpty(obj.getString("name"))) {
//                                response.setHeader(obj.getString("name"), obj.getString("value"));
                                    try {
                                        status = Integer.parseInt(obj.getString("name"));
                                    } catch (Exception e) {
                                    }
                                }
                            }
                        }
                        if (respObj.containsKey("body")) {
                            JSONObject bodyObj = respObj.getJSONObject("body");
                            if (bodyObj.containsKey("type")) {
                                String type = bodyObj.getString("type");
                                if (StringUtils.equals(type, "JSON")) {
                                    //判断是否是JsonSchema
                                    boolean isJsonSchema = false;
                                    if (bodyObj.containsKey("format")) {
                                        String foramtValue = String.valueOf(bodyObj.get("format"));
                                        if (StringUtils.equals("JSON-SCHEMA", foramtValue)) {
                                            isJsonSchema = true;
                                        }
                                    }
                                    if (isJsonSchema) {
                                        if (bodyObj.containsKey("jsonSchema") && bodyObj.getJSONObject("jsonSchema").containsKey("properties")) {
                                            String bodyRetunStr = bodyObj.getJSONObject("jsonSchema").getJSONObject("properties").toJSONString();
                                            JSONObject bodyReturnObj = JSONObject.parseObject(bodyRetunStr);
                                            JSONObject returnObj = this.parseJsonSchema(bodyReturnObj);
                                            returnStr = returnObj.toJSONString();
                                        }
                                    } else {
                                        if (bodyObj.containsKey("raw")) {
                                            returnStr = bodyObj.getString("raw");
                                        }
                                    }
                                } else if (StringUtils.equalsAny(type, "XML", "Raw")) {
                                    if (bodyObj.containsKey("raw")) {
                                        String raw = bodyObj.getString("raw");
                                        returnStr = raw;
                                    }
                                } else if (StringUtils.equalsAny(type, "Form Data", "WWW_FORM")) {
                                    Map<String, String> paramMap = new LinkedHashMap<>();
                                    if (bodyObj.containsKey("kvs")) {
                                        JSONArray bodyParamArr = new JSONArray();
                                        JSONArray kvsArr = bodyObj.getJSONArray("kvs");
                                        for (int i = 0; i < kvsArr.size(); i++) {
                                            JSONObject kv = kvsArr.getJSONObject(i);
                                            if (kv.containsKey("name")) {
                                                String values = kv.getString("value");
                                                if (StringUtils.isEmpty(values)) {
                                                    values = "";
                                                } else {
                                                    try {
                                                        values = values.startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(values) : values;
                                                    } catch (Exception e) {
                                                    }
                                                }
                                                paramMap.put(kv.getString("name"), values);
                                            }
                                        }
                                    }
                                    returnStr = JSONObject.toJSONString(paramMap);
                                } else if (StringUtils.equals(type, "BINARY")) {
                                    Map<String, String> paramMap = new LinkedHashMap<>();
                                    if (bodyObj.containsKey("binary")) {
                                        JSONArray kvsArr = bodyObj.getJSONArray("kvs");
                                        for (int i = 0; i < kvsArr.size(); i++) {
                                            JSONObject kv = kvsArr.getJSONObject(i);
                                            if (kv.containsKey("description") && kv.containsKey("files")) {
                                                String name = kv.getString("description");
                                                JSONArray fileArr = kv.getJSONArray("files");
                                                String allValue = "";
                                                for (int j = 0; j < fileArr.size(); j++) {
                                                    JSONObject fileObj = fileArr.getJSONObject(j);
                                                    if (fileObj.containsKey("name")) {
                                                        String values = fileObj.getString("name");
                                                        if (StringUtils.isEmpty(values)) {
                                                            values = "";
                                                        } else {
                                                            try {
                                                                values = values.startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(values) : values;
                                                            } catch (Exception e) {
                                                            }
                                                        }

                                                        allValue += values + " ;";
                                                    }
                                                }
                                                paramMap.put(name, allValue);
                                            }
                                        }
                                    }
                                    returnStr = JSONObject.toJSONString(paramMap);
                                }
                            }
                        }
                    }
                    if(StringUtils.isNotEmpty(returnStr) && status == 404){
                        status = 200;
                    }
                    response.setStatus(status);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnStr;
    }

    private JSONObject parseJsonSchema(JSONObject bodyReturnObj) {
        JSONObject returnObj = new JSONObject();
        if(bodyReturnObj == null){
            return  returnObj;
        }

        Set<String> keySet = bodyReturnObj.keySet();
        for (String key : keySet) {
            try {
                JsonSchemaReturnObj obj = bodyReturnObj.getObject(key, JsonSchemaReturnObj.class);
                if (StringUtils.equals("object", obj.getType())) {
                    JSONObject itemObj = this.parseJsonSchema(obj.getProperties());
                    if (!itemObj.isEmpty()) {
                        returnObj.put(key, itemObj);
                    }
                } else if (StringUtils.equals("array", obj.getType())) {
                    if (obj.getItems() != null) {
                        JSONObject itemObj = obj.getItems();
                        if (itemObj.containsKey("type")) {
                            if (StringUtils.equals("object", itemObj.getString("type")) && itemObj.containsKey("properties")) {
                                JSONObject arrayObj = itemObj.getJSONObject("properties");
                                JSONObject parseObj = this.parseJsonSchema(arrayObj);
                                JSONArray array = new JSONArray();
                                array.add(parseObj);
                                returnObj.put(key, array);
                            } else if (StringUtils.equals("string", itemObj.getString("type")) && itemObj.containsKey("mock")) {
                                JsonSchemaReturnObj arrayObj = JSONObject.toJavaObject(itemObj, JsonSchemaReturnObj.class);
                                String value = this.getMockValues(arrayObj.getMockValue());
                                JSONArray array = new JSONArray();
                                array.add(value);
                                returnObj.put(key, array);
                            }
                        }
                    }
                }else {
                    String values = obj.getMockValue();
                    if (StringUtils.isEmpty(values)) {
                        values = "";
                    } else {
                        try {
                            values = values.startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(values) : values;
                        } catch (Exception e) {
                        }
                    }
                    returnObj.put(key, values);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return returnObj;
    }

    private String getMockValues(String values) {
        if (StringUtils.isEmpty(values)) {
            values = "";
        } else {
            try {
                values = values.startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(values) : values;
            } catch (Exception e) {
            }
        }
        return values;
    }

    public MockExpectConfigWithBLOBs findMockExpectConfigById(String id) {
        return mockExpectConfigMapper.selectByPrimaryKey(id);
    }

    public void deleteMockExpectConfig(String id) {
        mockExpectConfigMapper.deleteByPrimaryKey(id);
    }

    public JSONObject getGetParamMap(String urlParams, ApiDefinitionWithBLOBs api, HttpServletRequest request) {
        JSONObject paramMap = this.getSendRestParamMapByIdAndUrl(api, urlParams);
        Enumeration<String> paramNameItor = request.getParameterNames();
        while (paramNameItor.hasMoreElements()) {
            String key = paramNameItor.nextElement();
            String value = request.getParameter(key);
            paramMap.put(key, value);
        }
        return paramMap;
    }

    public JSON getPostParamMap(HttpServletRequest request) {
        if (StringUtils.equalsIgnoreCase("application/JSON", request.getContentType())) {
            JSON returnJson = null;
            try {
                String param = this.getRequestPostStr(request);
                JSONValidator jsonValidator = JSONValidator.from(param);
                if(StringUtils.equalsIgnoreCase("Array",jsonValidator.getType().name())){
                    returnJson = JSONArray.parseArray(param);
                }else if(StringUtils.equalsIgnoreCase("Object",jsonValidator.getType().name())){
                    returnJson = JSONObject.parseObject(param);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return returnJson;
        } else if (StringUtils.equalsIgnoreCase("text/xml", request.getContentType())) {
            String xmlString = this.readXml(request);
            System.out.println(xmlString);

            org.json.JSONObject xmlJSONObj = XML.toJSONObject(xmlString);
            String jsonStr = xmlJSONObj.toString();
            JSONObject object = null;
            try {
                object = JSONObject.parseObject(jsonStr);
            } catch (Exception e) {
            }
            return object;
        } else if (StringUtils.equalsIgnoreCase("application/x-www-form-urlencoded", request.getContentType())) {
            JSONObject object = new JSONObject();
            Enumeration<String> paramNameItor = request.getParameterNames();
            while (paramNameItor.hasMoreElements()) {
                String key = paramNameItor.nextElement();
                String value = request.getParameter(key);
                object.put(key, value);
            }
            return object;
        } else {
            JSONObject object = new JSONObject();
            String bodyParam = this.readBody(request);
            if (!StringUtils.isEmpty(bodyParam)) {
                try {
                    object = JSONObject.parseObject(bodyParam);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Enumeration<String> paramNameItor = request.getParameterNames();
            while (paramNameItor.hasMoreElements()) {
                String key = paramNameItor.nextElement();
                String value = request.getParameter(key);
                object.put(key, value);
            }
            return object;
        }
    }

    private String readXml(HttpServletRequest request) {
        {
            String inputLine = null;
            // 接收到的数据
            StringBuffer recieveData = new StringBuffer();
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(
                        request.getInputStream(), "UTF-8"));
                while ((inputLine = in.readLine()) != null) {
                    recieveData.append(inputLine);
                }
            } catch (IOException e) {
            } finally {
                try {
                    if (null != in) {
                        in.close();
                    }
                } catch (IOException e) {
                }
            }

            return recieveData.toString();
        }
    }

    private String readBody(HttpServletRequest request) {
        String result = "";
        try {
            InputStream inputStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            outSteam.close();
            inputStream.close();
            result = new String(outSteam.toByteArray(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public JSONObject getSendRestParamMapByIdAndUrl(ApiDefinitionWithBLOBs api, String urlParams) {
//        ApiDefinitionWithBLOBs api = apiDefinitionMapper.selectByPrimaryKey(apiId);
        JSONObject returnJson = new JSONObject();
        if (api != null) {
            String path = api.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            String[] pathArr = path.split("/");
            String[] sendParamArr = urlParams.split("/");

            //获取 url的<参数名-参数值>，通过匹配api的接口设置和实际发送的url
            for (int i = 0; i < pathArr.length; i++) {
                String param = pathArr[i];
                if (param.startsWith("{") && param.endsWith("}")) {
                    param = param.substring(1, param.length() - 1);
                    String value = "";
                    if (sendParamArr.length > i) {
                        value = sendParamArr[i];
                    }
                    returnJson.put(param, value);
                }
            }

//            List<String> sendParams = new ArrayList<>();
//            for (String param : pathArr) {
//                if (param.startsWith("{") && param.endsWith("}")) {
//                    param = param.substring(1, param.length() - 1);
//                    sendParams.add(param);
//                }
//            }
//            try {
//                JSONObject requestJson = JSONObject.parseObject(api.getRequest());
//                if (requestJson.containsKey("rest")) {
//                    JSONArray jsonArray = requestJson.getJSONArray("rest");
//                    for (int i = 0; i < jsonArray.size(); i++) {
//                        JSONObject object = jsonArray.getJSONObject(i);
//                        if (object.containsKey("name") && object.containsKey("enable") && object.getBoolean("enable")) {
//                            String name = object.getString("name");
//                            if (sendParams.contains(name)) {
//                                String value = "";
//                                if (object.containsKey("value")) {
//                                    value = object.getString("value");
//                                }
//                                returnJson.put(name, value);
//                            }
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
        return returnJson;
    }

    public List<Map<String, String>> getApiParamsByApiDefinitionBLOBs(ApiDefinitionWithBLOBs apiModel) {
        List<Map<String, String>> list = new ArrayList<>();
        List<String> paramNameList = new ArrayList<>();
        if (apiModel != null) {
            if (apiModel.getRequest() != null) {
                JSONObject requestObj = this.genJSONObject(apiModel.getRequest());
                if (requestObj != null) {
                    //url参数赋值
                    if (requestObj.containsKey("arguments")) {
                        try {
                            JSONArray headArr = requestObj.getJSONArray("arguments");
                            for (int index = 0; index < headArr.size(); index++) {

                                JSONObject headObj = headArr.getJSONObject(index);
                                if (headObj.containsKey("name") && !paramNameList.contains(headObj.containsKey("name"))) {
                                    paramNameList.add(String.valueOf(headObj.get("name")));
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    if (requestObj.containsKey("rest")) {
                        try {
                            JSONArray headArr = requestObj.getJSONArray("rest");
                            for (int index = 0; index < headArr.size(); index++) {
                                JSONObject headObj = headArr.getJSONObject(index);
                                if (headObj.containsKey("name") && !paramNameList.contains(headObj.containsKey("name"))) {
                                    paramNameList.add(String.valueOf(headObj.get("name")));
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    //请求体参数类型
                    if (requestObj.containsKey("body")) {
                        try {
                            JSONObject bodyObj = requestObj.getJSONObject("body");
                            if (bodyObj.containsKey("type")) {
                                String type = bodyObj.getString("type");

                                if (StringUtils.equalsAny(type, "Form Data", "WWW_FORM")) {
                                    if (bodyObj.containsKey("kvs")) {
                                        JSONArray kvsArr = bodyObj.getJSONArray("kvs");
                                        Map<String, String> previewObjMap = new LinkedHashMap<>();
                                        for (int i = 0; i < kvsArr.size(); i++) {
                                            JSONObject kv = kvsArr.getJSONObject(i);
                                            if (kv.containsKey("name") && !paramNameList.contains(kv.containsKey("name"))) {
                                                paramNameList.add(String.valueOf(kv.get("name")));
                                            }
                                        }
                                    }
                                } else if (StringUtils.equals(type, "BINARY")) {
                                    if (bodyObj.containsKey("binary")) {
                                        List<Map<String, String>> bodyParamList = new ArrayList<>();
                                        JSONArray kvsArr = bodyObj.getJSONArray("binary");

                                        for (int i = 0; i < kvsArr.size(); i++) {
                                            JSONObject kv = kvsArr.getJSONObject(i);
                                            if (kv.containsKey("description") && kv.containsKey("files")) {
                                                String name = kv.getString("description");
                                                JSONArray fileArr = kv.getJSONArray("files");
                                                String value = "";
                                                for (int j = 0; j < fileArr.size(); j++) {
                                                    JSONObject fileObj = fileArr.getJSONObject(j);
                                                    if (fileObj.containsKey("name")) {
                                                        value += fileObj.getString("name") + " ;";
                                                    }
                                                }
                                                if (!paramNameList.contains(name)) {
                                                    paramNameList.add(name);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                        }

                    }
                }
            }
        }

        for (String param : paramNameList) {
            Map<String, String> map = new HashMap<>();
            map.put("value", param);
            list.add(map);
        }
        return list;
    }

    private JSONObject genJSONObject(String request) {
        JSONObject returnObj = null;
        try {
            returnObj = JSONObject.parseObject(request);
        } catch (Exception e) {
        }
        return returnObj;
    }

    public String getUrlSuffix(String projectId, HttpServletRequest request) {
        String urlPrefix = "/mock/" + projectId + "/";
        String requestUri = request.getRequestURI();
        String[] urlParamArr = requestUri.split(urlPrefix);
        return urlParamArr[urlParamArr.length - 1];
    }

    public MockConfigResponse findByApiId(String id) {
        MockConfigExample example = new MockConfigExample();
        MockConfigExample.Criteria criteria = example.createCriteria();
        criteria.andApiIdEqualTo(id);
        List<MockConfig> configList = mockConfigMapper.selectByExample(example);
        return this.assemblyMockConfingResponse(configList);
    }

    public String checkReturnWithMockExpectByBodyParam(String method, Project project, HttpServletRequest
            request, HttpServletResponse response) {
        String returnStr = "";
        boolean isMatch = false;
        List<ApiDefinitionWithBLOBs> aualifiedApiList = new ArrayList<>();
        if (project != null) {
            String urlSuffix = this.getUrlSuffix(project.getSystemId(), request);
            aualifiedApiList = apiDefinitionService.preparedUrl(project.getId(), method, urlSuffix, urlSuffix);
            List<String> apiIdList = aualifiedApiList.stream().map(ApiDefinitionWithBLOBs::getId).collect(Collectors.toList());
            MockConfigResponse mockConfigData = this.findByApiIdList(apiIdList);

            if (mockConfigData != null && mockConfigData.getMockExpectConfigList() != null) {
                JSON paramJson = this.getPostParamMap(request);
                if(paramJson instanceof JSONObject){
                    JSONObject paramMap = (JSONObject)paramJson;
                    MockExpectConfigResponse finalExpectConfig = this.findExpectConfig(mockConfigData.getMockExpectConfigList(), paramMap);
                    if (finalExpectConfig != null) {
                        isMatch = true;
                        returnStr = this.updateHttpServletResponse(finalExpectConfig, response);
                    }
                }else if(paramJson instanceof JSONArray){
                    JSONArray paramArray = (JSONArray)paramJson;
                    MockExpectConfigResponse finalExpectConfig = this.findExpectConfig(mockConfigData.getMockExpectConfigList(), paramArray);
                    if (finalExpectConfig != null) {
                        isMatch = true;
                        returnStr = this.updateHttpServletResponse(finalExpectConfig, response);
                    }
                }
            }
        }

        if (!isMatch) {
            returnStr = this.updateHttpServletResponse(aualifiedApiList, response);
        }
        return returnStr;
    }

    public String checkReturnWithMockExpectByUrlParam(String method, Project project, HttpServletRequest
            request, HttpServletResponse response) {
        String returnStr = "";
        boolean isMatch = false;
        List<ApiDefinitionWithBLOBs> aualifiedApiList = new ArrayList<>();
        if(project != null){
            String urlSuffix = this.getUrlSuffix(project.getSystemId(), request);
            aualifiedApiList = apiDefinitionService.preparedUrl(project.getId(), method, null, urlSuffix);

            /**
             * GET/DELETE 这种通过url穿参数的接口，在接口路径相同的情况下可能会出现这样的情况：
             *  api1: /api/{name}   参数 name = "ABC"
             *  api2: /api/{testParam} 参数 testParam = "ABC"
             *
             *  匹配预期Mock的逻辑为： 循环apiId进行筛选，直到筛选到预期Mock。如果筛选不到，则取Api的响应模版来进行返回
             */

            for (ApiDefinitionWithBLOBs api : aualifiedApiList) {
                JSONObject paramMap = this.getGetParamMap(urlSuffix, api, request);

                MockConfigResponse mockConfigData = this.findByApiId(api.getId());
                if (mockConfigData != null && mockConfigData.getMockExpectConfigList() != null) {
                    MockExpectConfigResponse finalExpectConfig = this.findExpectConfig(mockConfigData.getMockExpectConfigList(), paramMap);
                    if (finalExpectConfig != null) {
                        returnStr = this.updateHttpServletResponse(finalExpectConfig, response);
                        isMatch = true;
                        break;
                    }
                }
            }
        }

        if (!isMatch) {
            returnStr = this.updateHttpServletResponse(aualifiedApiList, response);
        }
        return returnStr;
    }

    /**
     * 描述:获取 post 请求的 byte[] 数组
     * <pre>
     * 举例：
     * </pre>
     *
     * @param request
     * @return
     * @throws IOException
     */
    public byte[] getRequestPostBytes(HttpServletRequest request) throws IOException {
        int contentLength = request.getContentLength();
        if (contentLength < 0) {
            return null;
        }
        byte buffer[] = new byte[contentLength];
        for (int i = 0; i < contentLength; ) {

            int readlen = request.getInputStream().read(buffer, i,
                    contentLength - i);
            if (readlen == -1) {
                break;
            }
            i += readlen;
        }
        return buffer;
    }

    /**
     * 描述:获取 post 请求内容
     * <pre>
     * 举例：
     * </pre>
     *
     * @param request
     * @return
     * @throws IOException
     */
    public String getRequestPostStr(HttpServletRequest request) throws IOException {
        byte buffer[] = getRequestPostBytes(request);
        String charEncoding = request.getCharacterEncoding();
        if (charEncoding == null) {
            charEncoding = "UTF-8";
        }
        return new String(buffer, charEncoding);
    }
}
