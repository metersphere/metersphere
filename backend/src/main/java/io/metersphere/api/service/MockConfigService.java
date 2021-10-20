package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.automation.EsbDataStruct;
import io.metersphere.api.dto.automation.TcpTreeTableDataStruct;
import io.metersphere.api.dto.automation.parse.TcpTreeTableDataParser;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.api.dto.mock.MockApiUtils;
import io.metersphere.api.dto.mock.RequestMockParams;
import io.metersphere.api.dto.mockconfig.MockConfigImportDTO;
import io.metersphere.api.dto.mockconfig.MockConfigRequest;
import io.metersphere.api.dto.mockconfig.MockExpectConfigRequest;
import io.metersphere.api.dto.mockconfig.response.JsonSchemaReturnObj;
import io.metersphere.api.dto.mockconfig.response.MockConfigResponse;
import io.metersphere.api.dto.mockconfig.response.MockExpectConfigResponse;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.MockConfigMapper;
import io.metersphere.base.mapper.MockExpectConfigMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.ExtMockExpectConfigMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.json.XML;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class MockConfigService {

    @Resource
    private MockConfigMapper mockConfigMapper;
    @Resource
    private MockExpectConfigMapper mockExpectConfigMapper;
    @Resource
    private ExtMockExpectConfigMapper extMockExpectConfigMapper;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ProjectMapper projectMapper;

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

    public List<MockExpectConfigWithBLOBs> selectMockExpectConfigByApiId(String apiId) {
        return extMockExpectConfigMapper.selectByApiId(apiId);
    }

    public List<MockConfigImportDTO> selectMockExpectConfigByApiIdIn(List<String> apiIds) {
        if (CollectionUtils.isNotEmpty(apiIds)) {
            List<MockConfigImportDTO> returnDTO = new ArrayList<>();
            for (String apiId : apiIds) {
                List<MockExpectConfigWithBLOBs> mockExpectConfigWithBLOBsList = extMockExpectConfigMapper.selectByApiId(apiId);
                for (MockExpectConfigWithBLOBs model : mockExpectConfigWithBLOBsList) {
                    MockConfigImportDTO dto = new MockConfigImportDTO();
                    BeanUtils.copyBean(dto, model);
                    dto.setApiId(apiId);
                    returnDTO.add(dto);
                }
            }
            return returnDTO;
        } else {
            return new ArrayList<>();
        }
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
            config.setId(UUID.randomUUID().toString());
            config.setCreateUserId(SessionUtils.getUserId());
            config.setCreateTime(createTimeStmp);
            config.setUpdateTime(createTimeStmp);
            if (request.getApiId() != null) {
                config.setApiId(request.getApiId());
                mockConfigMapper.insert(config);
            }
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

    public MockExpectConfig updateMockExpectConfigStatus(MockExpectConfigRequest request) {
        if (StringUtils.isNotEmpty(request.getId()) && StringUtils.isNotEmpty(request.getStatus())) {
            MockExpectConfigWithBLOBs model = new MockExpectConfigWithBLOBs();
            long timeStmp = System.currentTimeMillis();
            model.setId(request.getId());
            model.setUpdateTime(timeStmp);
            model.setStatus(request.getStatus());
            mockExpectConfigMapper.updateByPrimaryKeySelective(model);
            return model;
        } else {
            return null;
        }
    }

    public MockExpectConfig updateMockExpectConfig(MockExpectConfigRequest request, List<MultipartFile> bodyFiles) {
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
        if (StringUtils.isNotEmpty(request.getCopyId())) {
            FileUtils.copyBdyFile(request.getCopyId(), model.getId());
        }
        FileUtils.createBodyFiles(model.getId(), bodyFiles);
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

    public MockExpectConfigResponse findExpectConfig(Map<String, String> requestHeaderMap, List<MockExpectConfigResponse> mockExpectConfigList, RequestMockParams requestMockParams) {
        MockExpectConfigResponse returnModel = null;

        if (requestMockParams == null || requestMockParams.isEmpty()) {
            //如果参数为空，则匹配请求为空的期望
            for (MockExpectConfigResponse model : mockExpectConfigList) {
                if (!model.isStatus()) {
                    continue;
                }
                JSONObject requestObj = model.getRequest();
                MockExpectConfigResponse resultModel = null;
                if (requestObj.containsKey("params")) {
                    resultModel = this.getEmptyRequestMockExpectByParams(requestHeaderMap, model);
                } else {
                    resultModel = this.getEmptyRequestMockExpect(model);
                }
                if (resultModel != null) {
                    return resultModel;
                }
            }
        }
        for (MockExpectConfigResponse model : mockExpectConfigList) {
            try {
                if (!model.isStatus()) {
                    continue;
                }
                JSONObject mockExpectRequestObj = model.getRequest();
                boolean mathing = false;
                if (mockExpectRequestObj.containsKey("params")) {
                    mathing = this.isRequestMockExpectMatchingByParams(requestHeaderMap, mockExpectRequestObj, requestMockParams);
                } else {
                    mathing = this.isRequestMockExpectMatching(mockExpectRequestObj, requestMockParams.getQueryParamsObj());
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

    private boolean isRequestMockExpectMatchingByParams(Map<String, String> requestHeaderMap, JSONObject mockExpectRequestObj, RequestMockParams requestMockParams) {
        JSONObject expectParamsObj = mockExpectRequestObj.getJSONObject("params");
        if (expectParamsObj.containsKey("headers")) {
            //检测headers
            JSONArray headerArr = expectParamsObj.getJSONArray("headers");
            for (int i = 0; i < headerArr.size(); i++) {
                JSONObject jsonObject = headerArr.getJSONObject(i);
                if (jsonObject.containsKey("enable") && jsonObject.containsKey("name") && jsonObject.containsKey("value")) {
                    boolean isEnable = jsonObject.getBoolean("enable");
                    if (isEnable) {
                        String headerName = jsonObject.getString("name");
                        String headerValue = jsonObject.getString("value");
                        if (!requestHeaderMap.containsKey(headerName) || !StringUtils.equals(requestHeaderMap.get(headerName), headerValue)) {
                            return false;
                        }
                    }
                }
            }
        }

        JSONArray jsonArray = requestMockParams.getBodyParams();
        if (jsonArray == null) {
            //url or get 参数
            JSONArray argumentsArray = expectParamsObj.getJSONArray("arguments");
            JSONArray restArray = expectParamsObj.getJSONArray("rest");

            JSONObject urlRequestParamObj = MockApiUtils.getParams(argumentsArray);
            JSONObject restRequestParamObj = MockApiUtils.getParams(restArray);

            if (requestMockParams.getQueryParamsObj() == null || requestMockParams.getQueryParamsObj().isEmpty()) {
                return JsonStructUtils.checkJsonObjCompliance(requestMockParams.getRestParamsObj(), restRequestParamObj);
            } else if (requestMockParams.getRestParamsObj() == null || requestMockParams.getRestParamsObj().isEmpty()) {
                return JsonStructUtils.checkJsonObjCompliance(requestMockParams.getQueryParamsObj(), urlRequestParamObj);
            } else {
                return JsonStructUtils.checkJsonObjCompliance(requestMockParams.getQueryParamsObj(), urlRequestParamObj)
                        && JsonStructUtils.checkJsonObjCompliance(requestMockParams.getRestParamsObj(), restRequestParamObj);
            }

        } else {
            // body参数
            JSONObject expectBodyObject = expectParamsObj.getJSONObject("body");
            JSONArray mockExpectJsonArray = MockApiUtils.getExpectBodyParams(expectBodyObject);

            return JsonStructUtils.checkJsonArrayCompliance(jsonArray, mockExpectJsonArray);
        }

//        JSONObject mockExpectJson = new JSONObject();
//        if (isJsonParam) {
//            String jsonParams = mockExpectRequestObj.getString("jsonData");
//            JSONValidator jsonValidator = JSONValidator.from(jsonParams);
//            if (StringUtils.equalsIgnoreCase("Array", jsonValidator.getType().name())) {
//                JSONArray mockExpectArr = JSONArray.parseArray(jsonParams);
//                for (int expectIndex = 0; expectIndex < mockExpectArr.size(); expectIndex++) {
//                    JSONObject itemObj = mockExpectArr.getJSONObject(expectIndex);
//                    mockExpectJson = itemObj;
//                }
//            } else if (StringUtils.equalsIgnoreCase("Object", jsonValidator.getType().name())) {
//                JSONObject mockExpectJsonItem = JSONObject.parseObject(jsonParams);
//                mockExpectJson = mockExpectJsonItem;
//            }
//        } else {
//            JSONArray jsonArray = mockExpectRequestObj.getJSONArray("variables");
//            for (int i = 0; i < jsonArray.size(); i++) {
//                JSONObject object = jsonArray.getJSONObject(i);
//                String name = "";
//                String value = "";
//                if (object.containsKey("name")) {
//                    name = String.valueOf(object.get("name")).trim();
//                }
//                if (object.containsKey("value")) {
//                    value = String.valueOf(object.get("value")).trim();
//                }
//                if (StringUtils.isNotEmpty(name)) {
//                    mockExpectJson.put(name, value);
//                }
//            }
//        }

//        boolean isMatching = JsonStructUtils.checkJsonObjCompliance(mockExpectRequestObj, mockExpectJson);
//        return isMatching;
    }

    private boolean isRequestMockExpectMatching(JSONObject mockExpectRequestObj, JSONObject reqJsonObj) {
        boolean isJsonParam = mockExpectRequestObj.getBoolean("jsonParam");
        JSONObject mockExpectJson = new JSONObject();
        if (isJsonParam) {
            String jsonParams = mockExpectRequestObj.getString("jsonData");
            JSONValidator jsonValidator = JSONValidator.from(jsonParams);
            if (StringUtils.equalsIgnoreCase("Array", jsonValidator.getType().name())) {
                JSONArray mockExpectArr = JSONArray.parseArray(jsonParams);
                for (int expectIndex = 0; expectIndex < mockExpectArr.size(); expectIndex++) {
                    JSONObject itemObj = mockExpectArr.getJSONObject(expectIndex);
                    mockExpectJson = itemObj;
                }
            } else if (StringUtils.equalsIgnoreCase("Object", jsonValidator.getType().name())) {
                JSONObject mockExpectJsonItem = JSONObject.parseObject(jsonParams);
                mockExpectJson = mockExpectJsonItem;
            }
        } else {
            JSONArray jsonArray = mockExpectRequestObj.getJSONArray("variables");
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

        boolean isMatching = JsonStructUtils.checkJsonObjCompliance(reqJsonObj, mockExpectJson);
        return isMatching;
    }

    private MockExpectConfigResponse getEmptyRequestMockExpectByParams(Map<String, String> requestHeaderMap, MockExpectConfigResponse model) {
        JSONObject requestObj = model.getRequest();
        if (requestObj.containsKey("params")) {
            JSONObject paramsObj = requestObj.getJSONObject("params");
            if (paramsObj.containsKey("headers")) {
                JSONArray headArray = paramsObj.getJSONArray("headers");
                boolean isHeadMatch = MockApiUtils.matchRequestHeader(headArray, requestHeaderMap);
                if (!isHeadMatch) {
                    return null;
                }
                //判断rest为空
                if (paramsObj.containsKey("rest")) {
                    JSONArray restArray = paramsObj.getJSONArray("rest");
                    for (int i = 0; i < restArray.size(); i++) {
                        JSONObject restObj = restArray.getJSONObject(i);
                        if (restObj.containsKey("enable") && restObj.containsKey("name") && restObj.containsKey("value")) {
                            boolean isEnable = restObj.getBoolean("enable");
                            if (isEnable) {
                                return null;
                            }
                        }
                    }
                }
                //判断arguments为空
                if (paramsObj.containsKey("arguments")) {
                    JSONArray argumentsArray = paramsObj.getJSONArray("arguments");
                    for (int i = 0; i < argumentsArray.size(); i++) {
                        JSONObject argumentsObj = argumentsArray.getJSONObject(i);
                        if (argumentsObj.containsKey("enable") && argumentsObj.containsKey("name") && argumentsObj.containsKey("value")) {
                            boolean isEnable = argumentsObj.getBoolean("enable");
                            if (isEnable) {
                                return null;
                            }
                        }
                    }
                }
                //判断请求体为空
                if (paramsObj.containsKey("body")) {
                    JSONObject bodyObj = paramsObj.getJSONObject("body");
                    if (bodyObj.containsKey("type")) {
                        String type = bodyObj.getString("type");
                        if (StringUtils.equalsIgnoreCase(type, "json")) {
                            if (bodyObj.containsKey("format") && StringUtils.equalsIgnoreCase(bodyObj.getString("format"), "json-schema") && bodyObj.containsKey("jsonSchema") && bodyObj.get("jsonSchema") != null) {
                                return null;
                            } else {
                                if (bodyObj.containsKey("raw")) {
                                    String raw = bodyObj.getString("raw");
                                    if (StringUtils.isNotEmpty(raw)) {
                                        return null;
                                    }
                                }
                            }
                            //Binary的先不处理
//                        }else if(StringUtils.equalsIgnoreCase(type,"binary")){
//                            if(bodyObj.containsKey("binary")){
//                                JSONArray binaryArray = bodyObj.getJSONArray("binary");
//                                for(int i = 0; i < binaryArray.size(); i ++){
//                                    JSONObject binarObj = binaryArray.getJSONObject(i);
//                                    if(binarObj.containsKey("enable") && binarObj.containsKey("description")){
//                                        boolean isEnable = binarObj.getBoolean("enable");
//                                        if(isEnable){
//                                            return null;
//                                        }
//                                    }
//                                }
//                            }
                        } else if (StringUtils.equalsAnyIgnoreCase(type, "KeyValue", "Form Data", "WWW_FORM")) {
                            if (bodyObj.containsKey("kvs")) {
                                JSONArray kvsArray = bodyObj.getJSONArray("kvs");
                                for (int i = 0; i < kvsArray.size(); i++) {
                                    JSONObject kvsObj = kvsArray.getJSONObject(i);
                                    if (kvsObj.containsKey("enable") && kvsObj.containsKey("name") && kvsObj.containsKey("value")) {
                                        boolean isEnable = kvsObj.getBoolean("enable");
                                        if (isEnable) {
                                            return null;
                                        }
                                    }
                                }
                            }
                        } else if (StringUtils.equalsAnyIgnoreCase(type, "XML", "Raw")) {
                            String raw = bodyObj.getString("raw");
                            if (StringUtils.isNotEmpty(raw)) {
                                return null;
                            }
                        }
                    }
                }
                return model;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private MockExpectConfigResponse getEmptyRequestMockExpect(MockExpectConfigResponse model) {
        JSONObject requestObj = model.getRequest();
        boolean isJsonParam = requestObj.getBoolean("jsonParam");
        if (isJsonParam) {
            if (StringUtils.isEmpty(requestObj.getString("jsonData"))) {
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
        return null;
    }

    public MockExpectConfigResponse findExpectConfigByArrayJson(Map<String, String> requestHeaderMap, List<MockExpectConfigResponse> mockExpectConfigList, JSONArray reqJsonArray) {
        MockExpectConfigResponse returnModel = null;

        if (reqJsonArray == null || reqJsonArray.isEmpty()) {
            for (MockExpectConfigResponse model : mockExpectConfigList) {
                if (!model.isStatus()) {
                    continue;
                }
                JSONObject requestObj = model.getRequest();
                boolean isJsonParam = requestObj.getBoolean("jsonParam");

                if (isJsonParam) {
                    if (StringUtils.isEmpty(requestObj.getString("jsonData"))) {
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
                    if (StringUtils.equalsIgnoreCase("Array", jsonValidator.getType().name())) {
                        JSONArray mockExpectArr = JSONArray.parseArray(jsonParams);
                        for (int expectIndex = 0; expectIndex < mockExpectArr.size(); expectIndex++) {
                            JSONObject itemObj = mockExpectArr.getJSONObject(expectIndex);
                            mathing = JsonStructUtils.checkJsonArrayCompliance(reqJsonArray, itemObj);
                            if (!mathing) {
                                break;
                            }
                        }
                    } else if (StringUtils.equalsIgnoreCase("Object", jsonValidator.getType().name())) {
                        JSONObject mockExpectJson = JSONObject.parseObject(jsonParams);
                        mathing = JsonStructUtils.checkJsonArrayCompliance(reqJsonArray, mockExpectJson);
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
                    mathing = JsonStructUtils.checkJsonArrayCompliance(reqJsonArray, mockExpectJson);
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

    public String updateHttpServletResponse(MockExpectConfigResponse finalExpectConfig, String url, Map<String, String> headerMap, RequestMockParams requestMockParams, HttpServletResponse response) {
        String returnStr = "";
        try {
            //设置响应头和响应码
            JSONObject responseObj = finalExpectConfig.getResponse();
            String httpCode = responseObj.getString("httpCode");
            if (StringUtils.isEmpty(httpCode)) {
                httpCode = "500";
            }
            response.setStatus(Integer.parseInt(httpCode));
            long sleepTime = 0;
            if (responseObj.containsKey("responseResult")) {
                JSONObject responseJsonObj = responseObj.getJSONObject("responseResult");
                if (responseJsonObj.containsKey("headers")) {
                    JSONArray jsonArray = responseJsonObj.getJSONArray("headers");
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        if (object.containsKey("name") && object.containsKey("enable") && object.containsKey("value")) {
                            boolean isEnable = object.getBoolean("enable");
                            if (isEnable) {
                                String name = String.valueOf(object.get("name")).trim();
                                String value = String.valueOf(object.get("value")).trim();
                                if (StringUtils.isNotEmpty(name)) {
                                    response.setHeader(name, value);
                                }
                            }
                        }
                    }
                }
                if (responseJsonObj.containsKey("body")) {
                    returnStr = MockApiUtils.getResultByResponseResult(responseJsonObj.getJSONObject("body"), url, headerMap, requestMockParams);
                }
                if (responseJsonObj.containsKey("httpCode")) {
                    response.setStatus(Integer.parseInt(responseJsonObj.getString("httpCode")));
                }
                if (responseJsonObj.containsKey("delayed")) {
                    try {
                        sleepTime = Long.parseLong(String.valueOf(responseJsonObj.get("delayed")));
                    } catch (Exception e) {
                    }
                }
            } else {
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
                returnStr = String.valueOf(responseObj.get("body"));
                if (responseObj.containsKey("delayed")) {
                    try {
                        sleepTime = Long.parseLong(String.valueOf(responseObj.get("delayed")));
                    } catch (Exception e) {
                    }
                }
            }
            if (sleepTime > 0) {
                try {
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
            if (CollectionUtils.isEmpty(apis)) {
                response.setStatus(404);
            } else {
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
                                            JSONObject returnObj = MockApiUtils.parseJsonSchema(bodyReturnObj);
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
                                    //Binary的先不处理
//                                } else if (StringUtils.equals(type, "BINARY")) {
//                                    Map<String, String> paramMap = new LinkedHashMap<>();
//                                    if (bodyObj.containsKey("binary")) {
//                                        JSONArray kvsArr = bodyObj.getJSONArray("kvs");
//                                        for (int i = 0; i < kvsArr.size(); i++) {
//                                            JSONObject kv = kvsArr.getJSONObject(i);
//                                            if (kv.containsKey("description") && kv.containsKey("files")) {
//                                                String name = kv.getString("description");
//                                                JSONArray fileArr = kv.getJSONArray("files");
//                                                String allValue = "";
//                                                for (int j = 0; j < fileArr.size(); j++) {
//                                                    JSONObject fileObj = fileArr.getJSONObject(j);
//                                                    if (fileObj.containsKey("name")) {
//                                                        String values = fileObj.getString("name");
//                                                        if (StringUtils.isEmpty(values)) {
//                                                            values = "";
//                                                        } else {
//                                                            try {
//                                                                values = values.startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(values) : values;
//                                                            } catch (Exception e) {
//                                                            }
//                                                        }
//
//                                                        allValue += values + " ;";
//                                                    }
//                                                }
//                                                paramMap.put(name, allValue);
//                                            }
//                                        }
//                                    }
//                                    returnStr = JSONObject.toJSONString(paramMap);
                                }
                            }
                        }
                    }
                    if (StringUtils.isNotEmpty(returnStr) && status == 404) {
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

    public MockExpectConfigWithBLOBs findMockExpectConfigById(String id) {
        return mockExpectConfigMapper.selectByPrimaryKey(id);
    }

    public void deleteMockExpectConfig(String id) {
        MockExpectConfigWithBLOBs mockBlobs = mockExpectConfigMapper.selectByPrimaryKey(id);
        if (mockBlobs != null) {
            this.deleteMockExpectFiles(mockBlobs);
            mockExpectConfigMapper.deleteByPrimaryKey(id);
        }
    }

    private void deleteMockExpectFiles(MockExpectConfigWithBLOBs mockExpectConfigWithBLOBs) {
        if (mockExpectConfigWithBLOBs != null && StringUtils.isNotEmpty(mockExpectConfigWithBLOBs.getRequest())) {
            try {
                FileUtils.deleteBodyFiles(mockExpectConfigWithBLOBs.getId());
            } catch (Exception e) {
            }
        }
    }

    public void deleteMockConfigByApiId(String apiId) {
        MockConfigExample configExample = new MockConfigExample();
        configExample.createCriteria().andApiIdEqualTo(apiId);
        List<MockConfig> mockConfigList = mockConfigMapper.selectByExample(configExample);
        MockExpectConfigExample example = new MockExpectConfigExample();
        for (MockConfig mockConfig : mockConfigList) {
            example.clear();
            example.createCriteria().andMockConfigIdEqualTo(mockConfig.getId());
            List<MockExpectConfigWithBLOBs> deleteBolobs = mockExpectConfigMapper.selectByExampleWithBLOBs(example);
            for (MockExpectConfigWithBLOBs model : deleteBolobs) {
                this.deleteMockExpectFiles(model);
            }
            mockExpectConfigMapper.deleteByExample(example);
        }
        mockConfigMapper.deleteByExample(configExample);
    }

    public RequestMockParams getGetParamMap(String urlParams, ApiDefinitionWithBLOBs api, HttpServletRequest request) {
        RequestMockParams requestMockParams = new RequestMockParams();

        JSONObject urlParamsObject = this.getSendRestParamMapByIdAndUrl(api, urlParams);

        JSONObject queryParamsObject = new JSONObject();
        Enumeration<String> paramNameItor = request.getParameterNames();
        while (paramNameItor.hasMoreElements()) {
            String key = paramNameItor.nextElement();
            String value = request.getParameter(key);
            queryParamsObject.put(key, value);
        }

        requestMockParams.setRestParamsObj(urlParamsObject);
        requestMockParams.setQueryParamsObj(queryParamsObject);
        return requestMockParams;
    }

    public JSON getPostParamMap(HttpServletRequest request) {
        if (StringUtils.equalsIgnoreCase("application/JSON", request.getContentType())) {
            JSON returnJson = null;
            try {
                String param = this.getRequestPostStr(request);
                JSONValidator jsonValidator = JSONValidator.from(param);
                if (StringUtils.equalsIgnoreCase("Array", jsonValidator.getType().name())) {
                    returnJson = JSONArray.parseArray(param);
                } else if (StringUtils.equalsIgnoreCase("Object", jsonValidator.getType().name())) {
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
            object.put("raw",bodyParam);
//            if (!StringUtils.isEmpty(bodyParam)) {
//                try {
//                    object = JSONObject.parseObject(bodyParam);
//                } catch (Exception e) {
//                    object.put("raw",bodyParam);
//                }
//            }

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

        }
        return returnJson;
    }

    public List<Map<String, String>> getApiParamsByApiDefinitionBLOBs(ApiDefinitionWithBLOBs apiModel) {
        if (apiModel == null) {
            return new ArrayList<>();
        } else if (StringUtils.equalsIgnoreCase("tcp", apiModel.getMethod())) {
            return this.getTCPApiParams(apiModel);
        } else {
            return this.getHTTPApiParams(apiModel);
        }
    }

    private List<Map<String, String>> getTCPApiParams(ApiDefinitionWithBLOBs apiModel) {
        List<Map<String, String>> list = new ArrayList<>();
        List<String> paramNameList = new ArrayList<>();
        if (apiModel != null) {
            if (apiModel.getRequest() != null) {
                JSONObject requestObj = this.genJSONObject(apiModel.getRequest());
                if (requestObj != null && requestObj.containsKey("reportType")) {
                    String reportType = requestObj.getString("reportType");
                    if (StringUtils.equalsIgnoreCase(reportType, "xml") && requestObj.containsKey("xmlDataStruct")) {
                        paramNameList = this.parseByTcpTreeDataStruct(requestObj.getString("xmlDataStruct"));
                    } else if (StringUtils.equalsIgnoreCase(reportType, "json") && requestObj.containsKey("jsonDataStruct")) {
                        paramNameList = this.parseByJsonDataStruct(requestObj.getString("jsonDataStruct"));
                    } else if (requestObj.containsKey("protocol")) {
                        String protocol = requestObj.getString("protocol");
                        if (StringUtils.equalsIgnoreCase("ESB", protocol) && requestObj.containsKey("esbDataStruct")) {
                            paramNameList = this.parseByESBDataStruct(requestObj.getString("esbDataStruct"));
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

    private List<Map<String, String>> getHTTPApiParams(ApiDefinitionWithBLOBs apiModel) {
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
                                    //Binary的先不处理
//                                } else if (StringUtils.equals(type, "BINARY")) {
//                                    if (bodyObj.containsKey("binary")) {
//                                        List<Map<String, String>> bodyParamList = new ArrayList<>();
//                                        JSONArray kvsArr = bodyObj.getJSONArray("binary");
//
//                                        for (int i = 0; i < kvsArr.size(); i++) {
//                                            JSONObject kv = kvsArr.getJSONObject(i);
//                                            if (kv.containsKey("description") && kv.containsKey("files")) {
//                                                String name = kv.getString("description");
//                                                JSONArray fileArr = kv.getJSONArray("files");
//                                                String value = "";
//                                                for (int j = 0; j < fileArr.size(); j++) {
//                                                    JSONObject fileObj = fileArr.getJSONObject(j);
//                                                    if (fileObj.containsKey("name")) {
//                                                        value += fileObj.getString("name") + " ;";
//                                                    }
//                                                }
//                                                if (!paramNameList.contains(name)) {
//                                                    paramNameList.add(name);
//                                                }
//                                            }
//                                        }
//                                    }
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

    public String checkReturnWithMockExpectByBodyParam(String method, Map<String, String> requestHeaderMap, Project project, HttpServletRequest
            request, HttpServletResponse response) {
        String returnStr = "";
        boolean isMatch = false;
        String url = request.getRequestURL().toString();
        List<ApiDefinitionWithBLOBs> aualifiedApiList = new ArrayList<>();
        if (project != null) {
            String urlSuffix = this.getUrlSuffix(project.getSystemId(), request);
            aualifiedApiList = apiDefinitionService.preparedUrl(project.getId(), method, urlSuffix, urlSuffix);
            List<String> apiIdList = aualifiedApiList.stream().map(ApiDefinitionWithBLOBs::getId).collect(Collectors.toList());
            MockConfigResponse mockConfigData = this.findByApiIdList(apiIdList);

            if (mockConfigData != null && mockConfigData.getMockExpectConfigList() != null) {
                JSON paramJson = this.getPostParamMap(request);
                if (paramJson instanceof JSONObject) {
                    JSONArray paramsArray = new JSONArray();
                    paramsArray.add(paramJson);
                    RequestMockParams mockParams = new RequestMockParams();
                    mockParams.setBodyParams(paramsArray);
                    MockExpectConfigResponse finalExpectConfig = this.findExpectConfig(requestHeaderMap, mockConfigData.getMockExpectConfigList(), mockParams);
                    if (finalExpectConfig != null) {
                        isMatch = true;
                        returnStr = this.updateHttpServletResponse(finalExpectConfig, url, requestHeaderMap, mockParams, response);
                    }
                } else if (paramJson instanceof JSONArray) {
                    JSONArray paramArray = (JSONArray) paramJson;
                    RequestMockParams mockParams = new RequestMockParams();
                    mockParams.setBodyParams(paramArray);
                    MockExpectConfigResponse finalExpectConfig = this.findExpectConfig(requestHeaderMap, mockConfigData.getMockExpectConfigList(), mockParams);
                    if (finalExpectConfig != null) {
                        isMatch = true;
                        returnStr = this.updateHttpServletResponse(finalExpectConfig, url, requestHeaderMap, mockParams, response);
                    }
                }
            }
        }

        if (!isMatch) {
//            returnStr = this.updateHttpServletResponse(aualifiedApiList, response);
            response.setStatus(404);
        }
        return returnStr;
    }

    public String checkReturnWithMockExpectByUrlParam(String method, Map<String, String> requestHeaderMap, Project project, HttpServletRequest
            request, HttpServletResponse response) {
        String returnStr = "";
        boolean isMatch = false;
        String url = request.getRequestURI();
        List<ApiDefinitionWithBLOBs> aualifiedApiList = new ArrayList<>();
        if (project != null) {
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
                RequestMockParams paramMap = this.getGetParamMap(urlSuffix, api, request);

                MockConfigResponse mockConfigData = this.findByApiId(api.getId());
                if (mockConfigData != null && mockConfigData.getMockExpectConfigList() != null) {
                    MockExpectConfigResponse finalExpectConfig = this.findExpectConfig(requestHeaderMap, mockConfigData.getMockExpectConfigList(), paramMap);
                    if (finalExpectConfig != null) {
                        returnStr = this.updateHttpServletResponse(finalExpectConfig, url, requestHeaderMap, paramMap, response);
                        isMatch = true;
                        break;
                    }
                }
            }
        }

        if (!isMatch) {
//            returnStr = this.updateHttpServletResponse(aualifiedApiList, response);
            response.setStatus(404);
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

    private List<String> parseByJsonDataStruct(String dataString) {
        List<String> returnList = new ArrayList<>();
        try {
            JSONValidator validator = JSONValidator.from(dataString);
            Map<String, String> keyValueMap = new HashMap<>();
            if (StringUtils.equalsIgnoreCase(validator.getType().name(), "Object")) {
                JsonStructUtils.deepParseKeyByJsonObject(JSONObject.parseObject(dataString), returnList);
            } else if (StringUtils.equalsIgnoreCase(validator.getType().name(), "Array")) {
                JsonStructUtils.deepParseKeyByJsonArray(JSONArray.parseArray(dataString), returnList);
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
        return returnList;
    }

    private List<String> parseByTcpTreeDataStruct(String dataString) {
        List<TcpTreeTableDataStruct> list = JSONArray.parseArray(dataString, TcpTreeTableDataStruct.class);
        List<String> returnList = new ArrayList<>();
        for (TcpTreeTableDataStruct dataStruct : list) {
            List<String> nameList = dataStruct.getNameDeep();
            for (String name : nameList) {
                if (!returnList.contains(nameList)) {
                    returnList.add(name);
                }
            }
        }
        return returnList;
    }

    private List<String> parseByESBDataStruct(String dataString) {
        List<EsbDataStruct> list = JSONArray.parseArray(dataString, EsbDataStruct.class);
        List<String> returnList = new ArrayList<>();
        for (EsbDataStruct dataStruct : list) {
            List<String> nameList = dataStruct.getNameDeep();
            for (String name : nameList) {
                if (!returnList.contains(nameList)) {
                    returnList.add(name);
                }
            }
        }
        return returnList;
    }

    public MockExpectConfigWithBLOBs matchTcpMockExpect(String message, int port) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andMockTcpPortEqualTo(port).andIsMockTcpOpenEqualTo(true);
        List<Project> projectList = projectMapper.selectByExample(projectExample);

        boolean isJsonMessage = this.checkMessageIsJson(message);
        boolean isXMLMessage = this.checkMessageIsXml(message);

        List<MockExpectConfigWithBLOBs> structResult = new ArrayList<>();
        List<MockExpectConfigWithBLOBs> rawResult = new ArrayList<>();

        for (Project project : projectList) {
            String projectId = project.getId();
            List<MockExpectConfigWithBLOBs> mockExpectConfigList = extMockExpectConfigMapper.selectByProjectIdAndStatusIsOpen(projectId);
            for (MockExpectConfigWithBLOBs expectConfig : mockExpectConfigList) {
                String requestStr = expectConfig.getRequest();
                String responseStr = expectConfig.getResponse();
                if (StringUtils.isEmpty(responseStr)) {
                    continue;
                }

                try {
                    JSONObject requestJson = JSONObject.parseObject(requestStr);
                    if (requestJson.containsKey("reportType")) {
                        boolean isMatch = false;
                        boolean isRaw = false;
                        String reportType = requestJson.getString("reportType");

                        if (isJsonMessage && StringUtils.equalsIgnoreCase(reportType, "json")) {
                            if (requestJson.containsKey("jsonDataStruct")) {
                                isMatch = JsonStructUtils.checkJsonCompliance(message, requestJson.getString("jsonDataStruct"));
                            }
                        } else if (isXMLMessage && StringUtils.equalsIgnoreCase(reportType, "xml")) {
                            if (requestJson.containsKey("xmlDataStruct")) {
                                JSONObject sourceObj = XMLUtils.XmlToJson(message);
                                String xmlStr = "";
                                try {
                                    List<TcpTreeTableDataStruct> tcpDataList = JSONArray.parseArray(requestJson.getString("xmlDataStruct"), TcpTreeTableDataStruct.class);
                                    xmlStr = TcpTreeTableDataParser.treeTableData2Xml(tcpDataList);
                                } catch (Exception e) {

                                }
                                JSONObject matchObj = XMLUtils.XmlToJson(xmlStr);
                                isMatch = JsonStructUtils.checkJsonObjCompliance(sourceObj, matchObj);
                            }
                        } else if (StringUtils.equalsIgnoreCase(reportType, "raw")) {
                            if (requestJson.containsKey("rawDataStruct")) {
                                String rawDataStruct = requestJson.getString("rawDataStruct");
                                if (StringUtils.contains(message, rawDataStruct)) {
                                    isMatch = true;
                                    isRaw = true;
                                } else {
                                    Pattern pattern = Pattern.compile(rawDataStruct);
                                    Matcher matcher = pattern.matcher(message);
                                    if (matcher.find()) {
                                        isMatch = true;
                                        isRaw = true;
                                    }
                                }
                            }
                        }

                        if (isMatch) {
                            JSONObject responseObj = JSONObject.parseObject(responseStr);
                            if (responseObj.containsKey("body")) {
                                if (isRaw) {
                                    rawResult.add(expectConfig);
                                } else {
                                    structResult.add(expectConfig);
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                }
            }
        }
        //优先返回结构匹配的数据
        if (!structResult.isEmpty()) {
            return structResult.get(0);
        } else {
            if (!rawResult.isEmpty()) {
                return rawResult.get(0);
            } else {
                return null;
            }
        }
    }

    private boolean checkMessageIsXml(String message) {
        boolean isXml = false;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            builder.parse(new InputSource(new ByteArrayInputStream(message.getBytes("utf-8"))));
            isXml = true;
        } catch (Exception e) {
        }
        return isXml;
    }

    private boolean checkMessageIsJson(String message) {
        boolean isJson = false;
        try {
            JSONValidator validator = JSONValidator.from(message);
            String type = validator.getType().name();
            if (!StringUtils.equalsIgnoreCase("value", type)) {
                isJson = true;
            }
        } catch (Exception e) {
        }
        return isJson;
    }

    public void importMock(ApiDefinitionImport apiImport, SqlSession sqlSession, ApiTestImportRequest request) {
        if (CollectionUtils.isNotEmpty(apiImport.getMocks())) {
            Map<String, List<MockExpectConfigWithBLOBs>> saveMap = new HashMap<>();
            for (MockConfigImportDTO dto : apiImport.getMocks()) {
                String apiId = dto.getApiId();//de33108c-26e2-4d4f-826a-a5f8e017d2f4
                if (saveMap.containsKey(apiId)) {
                    saveMap.get(apiId).add(dto);
                } else {
                    List<MockExpectConfigWithBLOBs> list = new ArrayList<>();
                    list.add(dto);
                    saveMap.put(apiId, list);
                }
            }

            for (Map.Entry<String, List<MockExpectConfigWithBLOBs>> entry : saveMap.entrySet()) {
                String apiId = entry.getKey();
                this.deleteMockConfigByApiId(apiId);

                List<MockExpectConfigWithBLOBs> list = entry.getValue();

                String mockId = UUID.randomUUID().toString();
                MockConfig config = new MockConfig();
                config.setProjectId(request.getProjectId());
                config.setId(mockId);
                config.setCreateUserId(SessionUtils.getUserId());
                config.setCreateTime(System.currentTimeMillis());
                config.setUpdateTime(System.currentTimeMillis());
                config.setApiId(apiId);
                mockConfigMapper.insert(config);

                int batchCount = 0;
                for (MockExpectConfigWithBLOBs mockExpect : list) {
                    mockExpect.setId(UUID.randomUUID().toString());
                    mockExpect.setMockConfigId(mockId);
                    mockExpect.setCreateTime(System.currentTimeMillis());
                    mockExpect.setUpdateTime(System.currentTimeMillis());
                    mockExpect.setCreateUserId(SessionUtils.getUserId());
                    mockExpectConfigMapper.insert(mockExpect);
                }
                if (batchCount % 300 == 0) {
                    sqlSession.flushStatements();
                }
            }

        }
    }
}
