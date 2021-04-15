package io.metersphere.api.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.mockconfig.MockConfigRequest;
import io.metersphere.api.dto.mockconfig.MockExpectConfigRequest;
import io.metersphere.api.dto.mockconfig.response.MockConfigResponse;
import io.metersphere.api.dto.mockconfig.response.MockExpectConfigResponse;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionMapper;
import io.metersphere.base.mapper.MockConfigMapper;
import io.metersphere.base.mapper.MockExpectConfigMapper;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class MockConfigService {

    @Resource
    private MockConfigMapper mockConfigMapper;
    @Resource
    private MockExpectConfigMapper mockExpectConfigMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    public MockConfigResponse findByApiId(String apiId) {
        MockConfigRequest request = new MockConfigRequest();
        request.setApiId(apiId);
        return this.genMockConfig(request);
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

    public MockExpectConfigResponse findExpectConfig(List<MockExpectConfigResponse> mockExpectConfigList, Map<String, String> paramMap) {
        MockExpectConfigResponse returnModel = null;

        if (paramMap == null || paramMap.isEmpty()) {
            return returnModel;
        }
        for (MockExpectConfigResponse model : mockExpectConfigList) {
            try {
                if (!model.isStatus()) {
                    continue;
                }
                JSONObject requestObj = model.getRequest();
                boolean isJsonParam = requestObj.getBoolean("jsonParam");
                Map<String, String> reqParamMap = new HashMap<>();
                if (isJsonParam) {
                    JSONObject jsonParam = JSONObject.parseObject(requestObj.getString("jsonData"));
                    for (String head : jsonParam.keySet()) {
                        reqParamMap.put(head.trim(), String.valueOf(jsonParam.get(head)).trim());
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
                            reqParamMap.put(name, value);
                        }
                    }
                }

                boolean notMatching = false;

                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    String key = entry.getKey().trim();
                    String value = entry.getValue();
                    if (value != null) {
                        value = value.trim();
                    }

                    if (!reqParamMap.containsKey(key) || !StringUtils.equals(value, reqParamMap.get(key))) {
                        notMatching = true;
                        break;
                    }
                }
                if (!notMatching) {
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

    public MockExpectConfigWithBLOBs findMockExpectConfigById(String id) {
        return mockExpectConfigMapper.selectByPrimaryKey(id);
    }

    public void deleteMockExpectConfig(String id) {
        mockExpectConfigMapper.deleteByPrimaryKey(id);
    }

    public Map<String, String> getGetParamMap(HttpServletRequest request, String apiId) {
        String urlPrefix = "/mock/" + apiId + "/";
        String requestUri = request.getRequestURI();
        String[] urlParamArr = requestUri.split(urlPrefix);
        String urlParams = urlParamArr[urlParamArr.length - 1];

        Map<String, String> paramMap = this.getSendRestParamMapByIdAndUrl(apiId, urlParams);
        return paramMap;
    }

    public Map<String, String> getPostParamMap(HttpServletRequest request) {
        Enumeration<String> paramNameItor = request.getParameterNames();

        Map<String, String> paramMap = new HashMap<>();
        while (paramNameItor.hasMoreElements()) {
            String key = paramNameItor.nextElement();
            String value = request.getParameter(key);
            paramMap.put(key, value);
        }
        return paramMap;
    }

    public Map<String, String> getSendRestParamMapByIdAndUrl(String apiId, String urlParams) {
        ApiDefinitionWithBLOBs api = apiDefinitionMapper.selectByPrimaryKey(apiId);
        Map<String, String> returnMap = new HashMap<>();
        if (api != null) {
            String path = api.getPath();
            String[] pathArr = path.split("/");
            List<String> sendParams = new ArrayList<>();
            for (String param : pathArr) {
                if (param.startsWith("{") && param.endsWith("}")) {
                    param = param.substring(1, param.length() - 1);
                    sendParams.add(param);
                }
            }
            try {
                JSONObject requestJson = JSONObject.parseObject(api.getRequest());
                if (requestJson.containsKey("rest")) {
                    JSONArray jsonArray = requestJson.getJSONArray("rest");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        if (object.containsKey("name") && object.containsKey("enable") && object.getBoolean("enable")) {
                            String name = object.getString("name");
                            if (sendParams.contains(name)) {
                                String value = "";
                                if (object.containsKey("value")) {
                                    value = object.getString("value");
                                }
                                returnMap.put(name, value);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return returnMap;
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
}
