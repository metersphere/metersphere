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
import io.metersphere.api.dto.mock.ApiDefinitionResponseDTO;
import io.metersphere.api.dto.mock.MockConfigRequestParams;
import io.metersphere.api.dto.mock.MockParamSuggestions;
import io.metersphere.api.dto.mock.RequestMockParams;
import io.metersphere.api.dto.mockconfig.MockConfigImportDTO;
import io.metersphere.api.dto.mockconfig.MockConfigRequest;
import io.metersphere.api.dto.mockconfig.MockExpectConfigRequest;
import io.metersphere.api.dto.mockconfig.response.MockConfigResponse;
import io.metersphere.api.dto.mockconfig.response.MockExpectConfigResponse;
import io.metersphere.api.mock.utils.MockApiUtils;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.MockConfigMapper;
import io.metersphere.base.mapper.MockExpectConfigMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.ExtMockExpectConfigMapper;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.jmeter.utils.ScriptEngineUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.service.ProjectApplicationService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
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
    @Resource
    private ProjectApplicationService projectApplicationService;

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
            return new MockConfigResponse(config, expectConfigResponseList);
        } else {
            return new MockConfigResponse(null, new ArrayList<>());
        }
    }

    public void initExpectNum() {
        MockExpectConfigExample example = new MockExpectConfigExample();
        example.createCriteria().andExpectNumIsNull();
        List<MockExpectConfigWithBLOBs> mockExpectConfigList = mockExpectConfigMapper.selectByExampleWithBLOBs(example);

        Map<String, List<MockExpectConfigWithBLOBs>> mockConfigIdMap = mockExpectConfigList.stream().collect(Collectors.groupingBy(MockExpectConfig::getMockConfigId));
        for (Map.Entry<String, List<MockExpectConfigWithBLOBs>> entry :
                mockConfigIdMap.entrySet()) {
            String mockConfigId = entry.getKey();
            List<MockExpectConfigWithBLOBs> list = entry.getValue();
            String apiNum = extMockExpectConfigMapper.selectApiNumberByMockConfigId(mockConfigId);
            if (StringUtils.isEmpty(apiNum) || StringUtils.equalsIgnoreCase(apiNum, "null")) {
                continue;
            }
            int expectNumIndex = this.getMockExpectNumIndex(mockConfigId, apiNum);
            for (MockExpectConfigWithBLOBs config : list) {
                config.setExpectNum(apiNum + "_" + expectNumIndex);
                mockExpectConfigMapper.updateByPrimaryKeySelective(config);
                expectNumIndex++;
            }
        }

    }

    public MockConfigResponse genMockConfig(MockConfigRequest request) {
        MockConfigResponse returnRsp;

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
        if (isSave) {
            String expectNum = this.getMockExpectId(request.getMockConfigId());
            model.setExpectNum(expectNum);
        }
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

    private String getMockExpectId(String mockConfigId) {
        List<String> savedExpectNumber = extMockExpectConfigMapper.selectExlectNumByMockConfigId(mockConfigId);
        String apiNum = extMockExpectConfigMapper.selectApiNumberByMockConfigId(mockConfigId);
        if (StringUtils.isEmpty(apiNum)) {
            apiNum = "";
        } else {
            apiNum = apiNum + "_";
        }

        int index = 1;
        for (String expectNum : savedExpectNumber) {
            if (StringUtils.startsWith(expectNum, apiNum)) {
                String numStr = StringUtils.substringAfter(expectNum, apiNum);
                try {
                    int savedIndex = Integer.parseInt(numStr);
                    if (index <= savedIndex) {
                        index = savedIndex + 1;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return apiNum + index;
    }

    private int getMockExpectNumIndex(String mockConfigId, String apiNumber) {
        List<String> savedExpectNumber = extMockExpectConfigMapper.selectExlectNumByMockConfigId(mockConfigId);
        String apiNum = apiNumber;
        if (StringUtils.isEmpty(apiNum)) {
            apiNum = "";
        } else {
            apiNum = apiNum + "_";
        }

        int index = 1;
        for (String expectNum : savedExpectNumber) {
            if (StringUtils.startsWith(expectNum, apiNum)) {
                String numStr = StringUtils.substringAfter(expectNum, apiNum);
                try {
                    int savedIndex = Integer.parseInt(numStr);
                    if (index <= savedIndex) {
                        index = savedIndex + 1;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return index;
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
            return null;
        }

        for (MockExpectConfigResponse model : mockExpectConfigList) {
            try {
                if (!model.isStatus()) {
                    continue;
                }
                JSONObject mockExpectRequestObj = model.getRequest();
                boolean isMatch;
                if (mockExpectRequestObj.containsKey("params")) {
                    isMatch = this.isRequestMockExpectMatchingByParams(requestHeaderMap, mockExpectRequestObj, requestMockParams);
                } else {
                    isMatch = this.isRequestMockExpectMatching(mockExpectRequestObj, requestMockParams);
                }

                if (isMatch) {
                    returnModel = model;
                    break;
                }
            } catch (Exception e) {
                LogUtil.error(e);
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
                if (jsonObject.containsKey("name") && jsonObject.containsKey("value")) {
                    String headerName = jsonObject.getString("name");
                    String headerValue = jsonObject.getString("value");
                    if (StringUtils.isNotEmpty(headerName)) {
                        if (!requestHeaderMap.containsKey(headerName) || !StringUtils.equals(requestHeaderMap.get(headerName), headerValue)) {
                            return false;
                        }
                    }
                }
            }
        }

        if (expectParamsObj.containsKey("body")) {
            JSONObject expectBodyObject = expectParamsObj.getJSONObject("body");
            JSONArray jsonArray = requestMockParams.getBodyParams();
            String type = expectBodyObject.getString("type");
            String paramsFilterType = "And";
            if (expectBodyObject.containsKey("paramsFilterType")) {
                paramsFilterType = expectBodyObject.getString("paramsFilterType");
            }
            if (StringUtils.equalsAnyIgnoreCase(type, "Form Data", "WWW_FORM") && expectBodyObject.containsKey("kvs")) {
                JSONArray kvsArr = expectBodyObject.getJSONArray("kvs");
                List<MockConfigRequestParams> mockConfigRequestParams = MockApiUtils.getParamsByJSONArray(kvsArr);
                if(CollectionUtils.isNotEmpty(mockConfigRequestParams)){
                    if (!MockApiUtils.checkParamsCompliance(jsonArray, mockConfigRequestParams, StringUtils.equals(paramsFilterType, "And"))) {
                        return false;
                    }
                }
            } else {
                JSON mockExpectJsonArray = MockApiUtils.getExpectBodyParams(expectBodyObject);
                if (mockExpectJsonArray instanceof JSONObject) {
                    if (!JsonStructUtils.checkJsonArrayCompliance(jsonArray, (JSONObject) mockExpectJsonArray)) {
                        return false;
                    }
                } else if (mockExpectJsonArray instanceof JSONArray && ((JSONArray) mockExpectJsonArray).size() > 0) {
                    if (!JsonStructUtils.checkJsonArrayCompliance(jsonArray, (JSONArray) mockExpectJsonArray)) {
                        return false;
                    }
                }
            }
        }

        String paramsFilterType = "And";
        if (expectParamsObj.containsKey("paramsFilterType")) {
            paramsFilterType = expectParamsObj.getString("paramsFilterType");
        }

        if (expectParamsObj.containsKey("arguments")) {
            JSONArray argumentsArray = expectParamsObj.getJSONArray("arguments");
            List<MockConfigRequestParams> mockConfigRequestParams = MockApiUtils.getParamsByJSONArray(argumentsArray);
            if (!MockApiUtils.checkParamsCompliance(requestMockParams.getQueryParamsObj(), mockConfigRequestParams, StringUtils.equals(paramsFilterType, "And"))) {
                return false;
            }
        }

        if (expectParamsObj.containsKey("rest")) {
            JSONArray restArray = expectParamsObj.getJSONArray("rest");
            List<MockConfigRequestParams> mockConfigRequestParams = MockApiUtils.getParamsByJSONArray(restArray);
            if (!MockApiUtils.checkParamsCompliance(requestMockParams.getRestParamsObj(), mockConfigRequestParams, StringUtils.equals(paramsFilterType, "And"))) {
                return false;
            }
        }
        return true;
    }

    private boolean isRequestMockExpectMatching(JSONObject mockExpectRequestObj, RequestMockParams requestMockParams) {
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

        boolean matchRest = false;
        boolean matchQuery = false;
        boolean matchBody = false;

        if (requestMockParams.getQueryParamsObj() != null) {
            matchQuery = JsonStructUtils.checkJsonObjCompliance(requestMockParams.getQueryParamsObj(), mockExpectJson);
        }
        if (requestMockParams.getRestParamsObj() != null) {
            matchRest = JsonStructUtils.checkJsonObjCompliance(requestMockParams.getRestParamsObj(), mockExpectJson);
        }
        if (requestMockParams.getBodyParams() != null) {
            for (int i = 0; i < requestMockParams.getBodyParams().size(); i++) {
                JSONObject reqJsonObj = requestMockParams.getBodyParams().getJSONObject(i);
                matchBody = JsonStructUtils.checkJsonObjCompliance(reqJsonObj, mockExpectJson);
                if (matchBody) {
                    break;
                }
            }
        }

//        boolean isMatching = JsonStructUtils.checkJsonObjCompliance(reqJsonObj, mockExpectJson);
        return matchRest || matchQuery || matchBody;
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
                        if (restObj.containsKey("name") && restObj.containsKey("value")) {
                            return null;
                        }
                    }
                }
                //判断arguments为空
                if (paramsObj.containsKey("arguments")) {
                    JSONArray argumentsArray = paramsObj.getJSONArray("arguments");
                    for (int i = 0; i < argumentsArray.size(); i++) {
                        JSONObject argumentsObj = argumentsArray.getJSONObject(i);
                        if (argumentsObj.containsKey("name") && argumentsObj.containsKey("value")) {
                            return null;
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
                                    if (kvsObj.containsKey("name") && kvsObj.containsKey("value")) {
                                        return null;
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
                LogUtil.error(e);
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
                        if (object.containsKey("name") && object.containsKey("value")) {
                            String name = String.valueOf(object.get("name")).trim();
                            String value = String.valueOf(object.get("value")).trim();
                            if (StringUtils.isNotEmpty(name)) {
                                response.setHeader(name, value);
                            }
                        }
                    }
                }
                if (responseJsonObj.containsKey("body")) {
                    MockApiUtils mockApiUtils = new MockApiUtils();
                    boolean useScript = false;
                    if (responseJsonObj.containsKey("usePostScript")) {
                        useScript = responseJsonObj.getBoolean("usePostScript");
                    }
                    returnStr = mockApiUtils.getResultByResponseResult(responseJsonObj.getJSONObject("body"), url, headerMap, requestMockParams, useScript);
                }
                if (responseJsonObj.containsKey("httpCode")) {
                    int httpCodeNum = 500;
                    try {
                        httpCodeNum = Integer.parseInt(responseJsonObj.getString("httpCode"));
                    } catch (Exception e) {
                    }
                    response.setStatus(httpCodeNum);
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
            LogUtil.error(e);
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

    public Map<String, List<MockParamSuggestions>> getApiParamsByApiDefinitionBLOBs(ApiDefinitionWithBLOBs apiModel) {
        if (apiModel == null) {
            return new HashMap<>();
        } else if (StringUtils.equalsIgnoreCase("tcp", apiModel.getMethod())) {
            return this.getTCPApiParams(apiModel);
        } else {
            return this.getHTTPApiParams(apiModel);
        }
    }

    private Map<String, List<MockParamSuggestions>> getTCPApiParams(ApiDefinitionWithBLOBs apiModel) {
        Map<String, List<MockParamSuggestions>> returnMap = new HashMap<>();
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
        List<MockParamSuggestions> list = new ArrayList<>();
        paramNameList.forEach(item -> {
            MockParamSuggestions model = new MockParamSuggestions();
            model.setValue(item);
            list.add(model);
        });
        returnMap.put("value", list);
        return returnMap;
    }

    private Map<String, List<MockParamSuggestions>> getHTTPApiParams(ApiDefinitionWithBLOBs apiModel) {
        Map<String, List<MockParamSuggestions>> returnMap = new HashMap<>();

        List<String> queryParamList = new ArrayList<>();
        List<String> restParamList = new ArrayList<>();
        List<String> formDataList = new ArrayList<>();

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
                                if (headObj.containsKey("name") && !queryParamList.contains(headObj.containsKey("name"))) {
                                    queryParamList.add(String.valueOf(headObj.get("name")));
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
                                if (headObj.containsKey("name") && !restParamList.contains(headObj.containsKey("name"))) {
                                    restParamList.add(String.valueOf(headObj.get("name")));
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
                                            if (kv.containsKey("name") && !formDataList.contains(kv.containsKey("name"))) {
                                                formDataList.add(String.valueOf(kv.get("name")));
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

        if (CollectionUtils.isNotEmpty(queryParamList)) {
            List<MockParamSuggestions> list = new ArrayList<>();
            queryParamList.forEach(item -> {
                MockParamSuggestions model = new MockParamSuggestions();
                model.setValue(item);
                list.add(model);
            });
            returnMap.put("query", list);
        }
        if (CollectionUtils.isNotEmpty(restParamList)) {
            List<MockParamSuggestions> list = new ArrayList<>();
            restParamList.forEach(item -> {
                MockParamSuggestions model = new MockParamSuggestions();
                model.setValue(item);
                list.add(model);
            });
            returnMap.put("rest", list);
        }
        if (CollectionUtils.isNotEmpty(formDataList)) {
            List<MockParamSuggestions> list = new ArrayList<>();
            formDataList.forEach(item -> {
                MockParamSuggestions model = new MockParamSuggestions();
                model.setValue(item);
                list.add(model);
            });
            returnMap.put("form", list);
        }
        return returnMap;
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
        boolean matchApi = false;
        String url = request.getRequestURL().toString();
        if (project != null) {
            String urlSuffix = this.getUrlSuffix(project.getSystemId(), request);
            List<ApiDefinitionWithBLOBs> aualifiedApiList = apiDefinitionService.preparedUrl(project.getId(), method, urlSuffix);
            JSON paramJson = MockApiUtils.getPostParamMap(request);
            JSONObject parameterObject = MockApiUtils.getParameterJsonObject(request);
            for (ApiDefinitionWithBLOBs api : aualifiedApiList) {
                if(StringUtils.isEmpty(returnStr)){
                    RequestMockParams mockParams = MockApiUtils.getParams(urlSuffix, api.getPath(), parameterObject, paramJson, true);
                    MockConfigResponse mockConfigData = this.findByApiId(api.getId());
                    MockExpectConfigResponse finalExpectConfig = this.findExpectConfig(requestHeaderMap, mockConfigData.getMockExpectConfigList(), mockParams);
                    if (finalExpectConfig != null) {
                        returnStr = this.updateHttpServletResponse(finalExpectConfig, url, requestHeaderMap, mockParams, response);
                    }else {
                        returnStr = this.getApiDefinitionResponse(api, response);
                    }
                }
            }
            if(CollectionUtils.isNotEmpty(aualifiedApiList)){
                matchApi = true;
            }
        }

        if (!matchApi) {
            response.setStatus(404);
            returnStr = Translator.get("mock_warning");
        }
        return returnStr;
    }

    public String checkReturnWithMockExpectByUrlParam(String method, Map<String, String> requestHeaderMap, Project project, HttpServletRequest
            request, HttpServletResponse response) {
        String returnStr = "";
        boolean matchApi = false;
        String url = request.getRequestURL().toString();
        List<ApiDefinitionWithBLOBs> aualifiedApiList = new ArrayList<>();
        if (project != null) {
            String urlSuffix = this.getUrlSuffix(project.getSystemId(), request);
            aualifiedApiList = apiDefinitionService.preparedUrl(project.getId(), method, urlSuffix);

            /**
             * GET/DELETE 这种通过url穿参数的接口，在接口路径相同的情况下可能会出现这样的情况：
             *  api1: /api/{name}   参数 name = "ABC"
             *  api2: /api/{testParam} 参数 testParam = "ABC"
             *
             *  匹配预期Mock的逻辑为： 循环apiId进行筛选，直到筛选到预期Mock。如果筛选不到，则取Api的响应模版来进行返回
             */
            JSON paramJson = MockApiUtils.getPostParamMap(request);
            JSONObject parameterObject = MockApiUtils.getParameterJsonObject(request);

            for (ApiDefinitionWithBLOBs api : aualifiedApiList) {
                if(StringUtils.isEmpty(returnStr)){
                    RequestMockParams paramMap = MockApiUtils.getParams(urlSuffix, api.getPath(), parameterObject, paramJson, false);
                    MockConfigResponse mockConfigData = this.findByApiId(api.getId());
                    if (mockConfigData != null && mockConfigData.getMockExpectConfigList() != null) {
                        MockExpectConfigResponse finalExpectConfig = this.findExpectConfig(requestHeaderMap, mockConfigData.getMockExpectConfigList(), paramMap);
                        if (finalExpectConfig != null) {
                            returnStr = this.updateHttpServletResponse(finalExpectConfig, url, requestHeaderMap, paramMap, response);
                        }else {
                            returnStr = this.getApiDefinitionResponse(api, response);
                        }
                    }
                }
            }

            if(CollectionUtils.isNotEmpty(aualifiedApiList)){
                matchApi = true;
            }
        }

        if (!matchApi) {
            response.setStatus(404);
            returnStr = Translator.get("mock_warning");
        }
        return returnStr;
    }

    private String getApiDefinitionResponse(ApiDefinitionWithBLOBs api, HttpServletResponse response) {
        String returnStr = null;
        try {
            ApiDefinitionResponseDTO responseDTO = MockApiUtils.getApiResponse(api.getResponse());
            returnStr = responseDTO.getReturnData();
            response.setStatus(responseDTO.getReturnCode());
            if (MapUtils.isNotEmpty(responseDTO.getHeaders())) {
                for (Map.Entry<String, String> entry : responseDTO.getHeaders().entrySet()) {
                    if(StringUtils.isNotEmpty(entry.getKey())){
                        response.setHeader(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return returnStr;
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
        ProjectApplicationExample pae = new ProjectApplicationExample();
        pae.createCriteria().andTypeEqualTo(ProjectApplicationType.MOCK_TCP_OPEN.name())
                .andTypeValueEqualTo(String.valueOf(true));
        pae.or().andTypeEqualTo(ProjectApplicationType.MOCK_TCP_PORT.name())
                .andTypeValueEqualTo(String.valueOf(port));
        List<ProjectApplication> projectApplications = projectApplicationService.selectByExample(pae);
        List<String> projectIds = projectApplications.stream().map(ProjectApplication::getProjectId).collect(Collectors.toList());
        List<Project> projectList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(projectIds)) {
            ProjectExample projectExample = new ProjectExample();
            projectExample.createCriteria().andIdIn(projectIds);
            projectList = projectMapper.selectByExample(projectExample);
        }

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
                                try {
                                    List<TcpTreeTableDataStruct> tcpDataList = JSONArray.parseArray(requestJson.getString("xmlDataStruct"), TcpTreeTableDataStruct.class);
                                    isMatch = TcpTreeTableDataParser.isMatchTreeTableData(sourceObj, tcpDataList);
                                } catch (Exception e) {
                                    LogUtil.error(e);
                                }
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
            XMLUtils.setExpandEntityReferencesFalse(documentBuilderFactory);
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            builder.parse(new InputSource(new ByteArrayInputStream(message.getBytes("utf-8"))));
            isXml = true;
        } catch (Exception e) {
            e.printStackTrace();
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
                String apiId = dto.getApiId();
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
                List<MockExpectConfigWithBLOBs> list = entry.getValue();

                MockConfig mockConfig = this.selectMockConfigByApiId(apiId);
                if (mockConfig == null) {
                    this.insertMockExpectConfigs(apiId, request.getProjectId(), list, sqlSession);
                } else {
                    this.updateMockExpectConfigs(mockConfig, list, sqlSession);
                }
            }

        }
    }

    private void updateMockExpectConfigs(MockConfig mockConfig, List<MockExpectConfigWithBLOBs> list, SqlSession sqlSession) {
        int batchCount = 0;
        for (MockExpectConfigWithBLOBs mockExpect : list) {
            MockExpectConfig expectInDb = this.findMockExpectConfigByMockConfigIdAndExpectNum(mockConfig.getId(), mockExpect.getExpectNum());
            if (expectInDb == null) {
                mockExpect.setId(UUID.randomUUID().toString());
                mockExpect.setMockConfigId(mockConfig.getId());
                mockExpect.setCreateTime(System.currentTimeMillis());
                mockExpect.setUpdateTime(System.currentTimeMillis());
                mockExpect.setCreateUserId(SessionUtils.getUserId());
                mockExpectConfigMapper.insert(mockExpect);
            } else {
                mockExpect.setMockConfigId(mockConfig.getId());
                mockExpect.setId(expectInDb.getId());
                mockExpect.setUpdateTime(System.currentTimeMillis());
                mockExpectConfigMapper.updateByPrimaryKeyWithBLOBs(mockExpect);
            }

        }
        if (batchCount % 300 == 0) {
            sqlSession.flushStatements();
        }
    }

    private MockExpectConfig findMockExpectConfigByMockConfigIdAndExpectNum(String mockConfigId, String expectNum) {
        MockExpectConfigExample example = new MockExpectConfigExample();
        example.createCriteria().andMockConfigIdEqualTo(mockConfigId).andExpectNumEqualTo(expectNum);
        List<MockExpectConfig> bloBs = this.mockExpectConfigMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(bloBs)) {
            return bloBs.get(0);
        } else {
            return null;
        }
    }

    private void insertMockExpectConfigs(String apiId, String projectId, List<MockExpectConfigWithBLOBs> list, SqlSession sqlSession) {
        String mockId = UUID.randomUUID().toString();
        MockConfig config = new MockConfig();
        config.setProjectId(projectId);
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


    private MockConfig selectMockConfigByApiId(String apiId) {
        MockConfigExample example = new MockConfigExample();
        example.createCriteria().andApiIdEqualTo(apiId);
        List<MockConfig> mockConfigList = this.mockConfigMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(mockConfigList)) {
            return mockConfigList.get(0);
        } else {
            return null;
        }
    }

    public void updateMockReturnMsgByApi(ApiDefinitionWithBLOBs apiDefinitionWithBLOBs) {
        if (apiDefinitionWithBLOBs == null) {
            return;
        }
        ApiDefinitionResponseDTO responseDTO = MockApiUtils.getApiResponse(apiDefinitionWithBLOBs.getResponse());
        if (StringUtils.isEmpty(responseDTO.getReturnData())) {
            return;
        }
        List<MockExpectConfigWithBLOBs> updateList = this.selectMockExpectConfigByApiId(apiDefinitionWithBLOBs.getId());
        if (CollectionUtils.isNotEmpty(updateList)) {
            for (MockExpectConfigWithBLOBs model : updateList) {
                if (StringUtils.isNotEmpty(model.getResponse())) {
                    try {
                        JSONObject responseObj = JSONObject.parseObject(model.getResponse());
                        if (responseObj.containsKey("responseResult")) {
                            JSONObject responseResultObject = responseObj.getJSONObject("responseResult");
                            if (responseResultObject.containsKey("body")) {
                                responseResultObject.getJSONObject("body").put("apiRspRaw", responseDTO.getReturnData());

                                model.setResponse(responseObj.toJSONString());
                                mockExpectConfigMapper.updateByPrimaryKeySelective(model);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
    }
}
