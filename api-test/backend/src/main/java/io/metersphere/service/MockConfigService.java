package io.metersphere.service;

import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.automation.TcpTreeTableDataStruct;
import io.metersphere.api.dto.mock.*;
import io.metersphere.api.dto.mock.config.MockConfigImportDTO;
import io.metersphere.api.dto.mock.config.MockConfigRequest;
import io.metersphere.api.dto.mock.config.MockExpectConfigRequest;
import io.metersphere.api.dto.mock.config.response.MockConfigResponse;
import io.metersphere.api.dto.mock.config.response.MockExpectConfigResponse;
import io.metersphere.api.parse.api.ApiDefinitionImport;
import io.metersphere.api.parse.scenario.TcpTreeTableDataParser;
import io.metersphere.api.tcp.TCPPool;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtMockExpectConfigMapper;
import io.metersphere.commons.constants.NoticeConstants;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.commons.utils.mock.MockApiUtils;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.definition.ApiDefinitionService;
import io.metersphere.service.ext.ExtProjectApplicationService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
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
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private ExtProjectApplicationService extProjectApplicationService;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Value("${tcp.mock.port}")
    private String tcpMockPorts;
    @Resource
    private BaseEnvironmentService baseEnvironmentService;
    @Resource
    private NoticeSendService noticeSendService;

    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    @Resource
    private ProjectVersionMapper projectVersionMapper;

    public List<MockExpectConfigWithBLOBs> selectMockExpectConfigByApiId(String apiId) {
        return extMockExpectConfigMapper.selectByApiId(apiId);
    }

    public List<MockExpectConfig> selectSimpleMockExpectConfigByMockConfigId(String mockConfigId) {
        MockExpectConfigExample example = new MockExpectConfigExample();
        example.createCriteria().andMockConfigIdEqualTo(mockConfigId);
        return mockExpectConfigMapper.selectByExample(example);
    }

    public List<MockConfigImportDTO> selectMockExpectConfigByApiIdIn(List<String> apiIds) {
        if (CollectionUtils.isNotEmpty(apiIds)) {
            return extMockExpectConfigMapper.selectByApiIdIn(apiIds);
        } else {
            return new ArrayList<>();
        }
    }

    private MockConfigResponse assemblyMockConingResponse(List<MockConfig> configList) {
        if (!configList.isEmpty()) {
            MockConfig config = configList.get(0);
            MockExpectConfigExample expectConfigExample = new MockExpectConfigExample();
            expectConfigExample.createCriteria().andMockConfigIdEqualTo(config.getId());
            expectConfigExample.setOrderByClause("update_time DESC");

            List<MockExpectConfigResponse> expectConfigResponseList = new ArrayList<>();

            List<MockExpectConfigWithBLOBs> expectConfigList = mockExpectConfigMapper.selectByExampleWithBLOBs(expectConfigExample);
            for (MockExpectConfigWithBLOBs expectConfig : expectConfigList) {
                MockExpectConfigResponse response = new MockExpectConfigResponse(expectConfig);
                expectConfigResponseList.add(response);

            }
            return new MockConfigResponse(config, expectConfigResponseList);
        } else {
            return new MockConfigResponse(null, new ArrayList<>());
        }
    }

    /**
     * 这个接口是生成用的！
     * 如果没数据就生成，有数据就返回一套数据结构！
     * 所有入参都没有必填项！
     * 如果为了查询，请写一个新的接口！
     */
    public MockConfigResponse genMockConfig(MockConfigRequest request) {
        MockConfigResponse returnRsp;
        MockConfigExample example = new MockConfigExample();
        MockConfigExample.Criteria criteria = example.createCriteria();
        if (request.getId() != null) {
            criteria.andIdEqualTo(request.getId());
        }
        if (request.getApiId() != null) {
            criteria.andApiIdEqualTo(request.getApiId());
        } else if (StringUtils.isEmpty(request.getId())) {
            return new MockConfigResponse(null, new ArrayList<>());
        }
        if (request.getProjectId() != null) {
            criteria.andProjectIdEqualTo(request.getProjectId());
        }

        List<MockConfig> configList = mockConfigMapper.selectByExample(example);
        if (configList.isEmpty()) {
            long createTimeLong = System.currentTimeMillis();

            MockConfig config = new MockConfig();
            config.setProjectId(request.getProjectId());
            config.setId(UUID.randomUUID().toString());
            config.setCreateUserId(SessionUtils.getUserId());
            config.setCreateTime(createTimeLong);
            config.setUpdateTime(createTimeLong);
            if (request.getApiId() != null) {
                config.setApiId(request.getApiId());
                mockConfigMapper.insert(config);
            }
            returnRsp = new MockConfigResponse(config, new ArrayList<>());
        } else {
            returnRsp = this.assemblyMockConingResponse(configList);
        }
        return returnRsp;
    }


    public void sendMockNotice(MockExpectConfigWithBLOBs mockExpectConfigWithBLOBs, String defaultContext, String event) {
        if (SessionUtils.getUserId() != null) {
            String context = SessionUtils.getUserId().concat(defaultContext).concat(":").concat(mockExpectConfigWithBLOBs.getName());
            Map<String, Object> paramMap = new HashMap<>();
            MockConfig mockConfig = mockConfigMapper.selectByPrimaryKey(mockExpectConfigWithBLOBs.getMockConfigId());
            getParamMap(paramMap, SessionUtils.getUserId(), mockExpectConfigWithBLOBs, mockConfig);
            NoticeModel noticeModel = NoticeModel.builder().operator(SessionUtils.getUserId()).context(context).testId(mockExpectConfigWithBLOBs.getId()).subject("接口定义通知").paramMap(paramMap).excludeSelf(true).event(event).build();
            noticeSendService.send(NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
        }
    }

    public void sendMockNotice(String mockExpectId, String defaultContext, String event) {
        MockExpectConfigWithBLOBs mockExpectConfigWithBLOBs = mockExpectConfigMapper.selectByPrimaryKey(mockExpectId);
        if (mockExpectConfigWithBLOBs != null) {
            this.sendMockNotice(mockExpectConfigWithBLOBs, defaultContext, event);
        }
    }

    private void getParamMap(Map<String, Object> paramMap, String userId, MockExpectConfigWithBLOBs mockExpectConfigWithBLOBs, MockConfig mockConfig) {
        paramMap.put("operator", userId);
        paramMap.put("id", mockExpectConfigWithBLOBs.getId());
        paramMap.put("name", mockExpectConfigWithBLOBs.getName());
        paramMap.put("createUser", mockExpectConfigWithBLOBs.getCreateUserId());
        if (mockConfig != null) {
            ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = apiDefinitionMapper.selectByPrimaryKey(mockConfig.getApiId());
            if (apiDefinitionWithBLOBs != null) {
                paramMap.put("projectId", apiDefinitionWithBLOBs.getProjectId());
                paramMap.put("apiDefinitionId", apiDefinitionWithBLOBs.getId());
                ProjectVersion projectVersion = projectVersionMapper.selectByPrimaryKey(apiDefinitionWithBLOBs.getVersionId());
                paramMap.put("version", projectVersion.getName());
            }

        }

    }

    public MockExpectConfig updateMockExpectConfigStatus(MockExpectConfigRequest request) {
        if (StringUtils.isNotEmpty(request.getId()) && StringUtils.isNotEmpty(request.getStatus())) {
            MockExpectConfigWithBLOBs model = new MockExpectConfigWithBLOBs();
            long timestamp = System.currentTimeMillis();
            model.setId(request.getId());
            model.setUpdateTime(timestamp);
            model.setStatus(request.getStatus());
            mockExpectConfigMapper.updateByPrimaryKeySelective(model);
            sendMockNotice(request.getId(), "更新了mock", NoticeConstants.Event.MOCK_UPDATE);
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
        long timestamp = System.currentTimeMillis();
        MockExpectConfigWithBLOBs model = new MockExpectConfigWithBLOBs();
        if (isSave) {
            String expectNum = this.getMockExpectId(request.getMockConfigId());
            model.setExpectNum(expectNum);
        }
        model.setId(request.getId());

        model.setMockConfigId(request.getMockConfigId());
        model.setUpdateTime(timestamp);
        model.setStatus(request.getStatus());
        if (request.getTags() != null) {
            model.setTags(JSON.toJSONString(request.getTags()));
        }
        model.setName(request.getName());
        if (request.getRequest() != null) {
            model.setRequest(JSON.toJSONString(request.getRequest()));
        }
        if (request.getResponse() != null) {
            model.setResponse(JSON.toJSONString(request.getResponse()));
        }
        if (isSave) {
            model.setCreateTime(timestamp);
            model.setCreateUserId(SessionUtils.getUserId());
            model.setStatus("true");
            mockExpectConfigMapper.insert(model);
            sendMockNotice(model, "新建了mock", NoticeConstants.Event.MOCK_CREATE);
        } else {
            mockExpectConfigMapper.updateByPrimaryKeySelective(model);
            model = mockExpectConfigMapper.selectByPrimaryKey(model.getId());
            sendMockNotice(model, "更新了mock", NoticeConstants.Event.MOCK_UPDATE);
        }
        if (StringUtils.isNotEmpty(request.getCopyId())) {
            FileUtils.copyBdyFile(request.getCopyId(), model.getId());
        }
        FileUtils.createBodyFiles(model.getId(), bodyFiles);
        return mockExpectConfigMapper.selectByPrimaryKey(model.getId());
    }

    public String getMockExpectId(String mockConfigId) {
        List<String> savedExpectNumber = this.selectExpectNumberByConfigId(mockConfigId);
        String apiNum = extMockExpectConfigMapper.selectApiNumberByMockConfigId(mockConfigId);
        return this.getMockExpectId(apiNum, savedExpectNumber);
    }

    public List<String> selectExpectNumberByConfigId(String mockConfigId) {
        return extMockExpectConfigMapper.selectExlectNumByMockConfigId(mockConfigId);
    }

    public String getMockExpectId(String apiNum, List<String> savedExpectNumber) {
        if (StringUtils.isEmpty(apiNum)) {
            apiNum = StringUtils.EMPTY;
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
                JSONObject requestObj = JSONUtil.parseObject(JSON.toJSONString(model.getRequest()));
                MockExpectConfigResponse resultModel;
                if (requestObj.has("params")) {
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
                JSONObject mockExpectRequestObj = JSONUtil.parseObject(JSON.toJSONString(model.getRequest()));
                boolean isMatch = false;
                if (mockExpectRequestObj.has("params")) {
                    isMatch = this.isRequestMockExpectMatchingByParams(requestHeaderMap, mockExpectRequestObj, requestMockParams);
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
        JSONObject expectParamsObj = mockExpectRequestObj.optJSONObject("params");
        if (expectParamsObj.has("headers")) {
            //检测headers
            JSONArray headerArr = expectParamsObj.optJSONArray("headers");
            for (int i = 0; i < headerArr.length(); i++) {
                JSONObject jsonObject = headerArr.optJSONObject(i);
                if (jsonObject.has("name") && jsonObject.has("value")) {
                    String headerName = jsonObject.optString("name");
                    String headerValue = StringUtils.trim(jsonObject.optString("value"));
                    if (StringUtils.isNotEmpty(headerName)) {
                        if (!requestHeaderMap.containsKey(headerName) || !StringUtils.equals(requestHeaderMap.get(headerName), headerValue)) {
                            return false;
                        }
                    }
                }
            }
        }

        if (expectParamsObj.has("body")) {
            JSONObject expectBodyObject = expectParamsObj.optJSONObject("body");
            String type = expectBodyObject.optString(PropertyConstant.TYPE);
            String paramsFilterType = "And";
            if (expectBodyObject.has("paramsFilterType")) {
                paramsFilterType = expectBodyObject.optString("paramsFilterType");
            }
            if (StringUtils.equalsAnyIgnoreCase(type, "Form Data", "WWW_FORM") && expectBodyObject.has("kvs")) {
                JSONArray kvsArr = expectBodyObject.optJSONArray("kvs");
                List<MockConfigRequestParams> mockConfigRequestParams = MockApiUtils.getParamsByJSONArray(kvsArr);
                if (CollectionUtils.isNotEmpty(mockConfigRequestParams)) {
                    if (!MockApiUtils.checkParamsCompliance(requestMockParams.getQueryParamsObj(), mockConfigRequestParams, StringUtils.equals(paramsFilterType, "And"))) {
                        return false;
                    }
                }
            } else if (StringUtils.equalsAnyIgnoreCase(type, "Raw") && expectBodyObject.has("raw")) {
                String rawExpect = expectBodyObject.optString("raw");
                if (StringUtils.isEmpty(requestMockParams.getRaw()) || !StringUtils.contains(requestMockParams.getRaw(), rawExpect)) {
                    return false;
                }
            } else {
                JSONArray jsonArray = null;
                if (StringUtils.equalsIgnoreCase(type, "xml") && requestMockParams.getXmlToJsonParam() != null) {
                    jsonArray = new JSONArray();
                    jsonArray.put(requestMockParams.getXmlToJsonParam());
                } else if (StringUtils.equalsIgnoreCase(type, "json") && requestMockParams.getJsonParam() != null) {
                    if (requestMockParams.getJsonParam() instanceof JSONArray) {
                        jsonArray = (JSONArray) requestMockParams.getJsonParam();
                    } else if (requestMockParams.getJsonParam() instanceof JSONObject) {
                        jsonArray = new JSONArray();
                        jsonArray.put(requestMockParams.getJsonParam());
                    }
                }
                Object mockExpectJsonArray = MockApiUtils.getExpectBodyParams(expectBodyObject);
                if (mockExpectJsonArray instanceof JSONObject) {
                    if (!JsonStructUtils.checkJsonArrayCompliance(jsonArray, (JSONObject) mockExpectJsonArray)) {
                        return false;
                    }
                } else if (mockExpectJsonArray instanceof JSONArray && ((JSONArray) mockExpectJsonArray).length() > 0) {
                    if (!JsonStructUtils.checkJsonArrayCompliance(jsonArray, (JSONArray) mockExpectJsonArray)) {
                        return false;
                    }
                }
            }
        }

        String paramsFilterType = "And";
        if (expectParamsObj.has("paramsFilterType")) {
            paramsFilterType = expectParamsObj.optString("paramsFilterType");
        }

        if (expectParamsObj.has("arguments")) {
            JSONArray argumentsArray = expectParamsObj.optJSONArray("arguments");
            List<MockConfigRequestParams> mockConfigRequestParams = MockApiUtils.getParamsByJSONArray(argumentsArray);
            if (!MockApiUtils.checkParamsCompliance(requestMockParams.getQueryParamsObj(), mockConfigRequestParams, StringUtils.equals(paramsFilterType, "And"))) {
                return false;
            }
        }

        String restFilterType = paramsFilterType;
        if (expectParamsObj.has("restFilterType") && StringUtils.isNotEmpty(expectParamsObj.optString("restFilterType"))) {
            restFilterType = expectParamsObj.optString("restFilterType");
        }
        if (expectParamsObj.has("rest")) {
            JSONArray restArray = expectParamsObj.optJSONArray("rest");
            List<MockConfigRequestParams> mockConfigRequestParams = MockApiUtils.getParamsByJSONArray(restArray);
            return MockApiUtils.checkParamsCompliance(requestMockParams.getRestParamsObj(), mockConfigRequestParams, StringUtils.equals(restFilterType, "And"));
        }
        return true;
    }

    private MockExpectConfigResponse getEmptyRequestMockExpectByParams(Map<String, String> requestHeaderMap, MockExpectConfigResponse model) {
        JSONObject requestObj = JSONUtil.parseObject(JSON.toJSONString(model.getRequest()));
        if (requestObj.has("params")) {
            JSONObject paramsObj = requestObj.optJSONObject("params");
            if (paramsObj.has("headers")) {
                JSONArray headArray = paramsObj.optJSONArray("headers");
                boolean isHeadMatch = MockApiUtils.matchRequestHeader(headArray, requestHeaderMap);
                if (!isHeadMatch) {
                    return null;
                }
                //判断rest为空
                if (paramsObj.has("rest")) {
                    JSONArray restArray = paramsObj.optJSONArray("rest");
                    for (int i = 0; i < restArray.length(); i++) {
                        JSONObject restObj = restArray.optJSONObject(i);
                        if (restObj.has("name") && restObj.has("value")) {
                            return null;
                        }
                    }
                }
                //判断arguments为空
                if (paramsObj.has("arguments")) {
                    JSONArray argumentsArray = paramsObj.optJSONArray("arguments");
                    for (int i = 0; i < argumentsArray.length(); i++) {
                        JSONObject argumentsObj = argumentsArray.optJSONObject(i);
                        if (argumentsObj.has("name") && argumentsObj.has("value")) {
                            return null;
                        }
                    }
                }
                //判断请求体为空
                if (paramsObj.has("body")) {
                    JSONObject bodyObj = paramsObj.optJSONObject("body");
                    if (bodyObj.has(PropertyConstant.TYPE)) {
                        String type = bodyObj.optString(PropertyConstant.TYPE);
                        if (StringUtils.equalsIgnoreCase(type, "json")) {
                            if (bodyObj.has("format") && StringUtils.equalsIgnoreCase(bodyObj.optString("format"), "json-schema") && bodyObj.has("jsonSchema") && bodyObj.get("jsonSchema") != null) {
                                return null;
                            } else {
                                if (bodyObj.has("raw")) {
                                    String raw = bodyObj.optString("raw");
                                    if (StringUtils.isNotEmpty(raw)) {
                                        return null;
                                    }
                                }
                            }
                        } else if (StringUtils.equalsAnyIgnoreCase(type, "KeyValue", "Form Data", "WWW_FORM")) {
                            if (bodyObj.has("kvs")) {
                                JSONArray kvsArray = bodyObj.optJSONArray("kvs");
                                for (int i = 0; i < kvsArray.length(); i++) {
                                    JSONObject kvsObj = kvsArray.optJSONObject(i);
                                    if (kvsObj.has("name") && kvsObj.has("value")) {
                                        return null;
                                    }
                                }
                            }
                        } else if (StringUtils.equalsAnyIgnoreCase(type, "XML", "Raw")) {
                            String raw = bodyObj.optString("raw");
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
        JSONObject requestObj = JSONUtil.parseObject(JSON.toJSONString(model.getRequest()));
        boolean isJsonParam = requestObj.getBoolean("jsonParam");
        if (isJsonParam) {
            if (StringUtils.isEmpty(requestObj.optString("jsonData"))) {
                return model;
            }
        } else {
            JSONObject mockExpectJson = new JSONObject();
            JSONArray jsonArray = requestObj.optJSONArray("variables");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.optJSONObject(i);
                String name = StringUtils.EMPTY;
                String value = StringUtils.EMPTY;
                if (object.has("name")) {
                    name = String.valueOf(object.get("name")).trim();
                }
                if (object.has("value")) {
                    value = String.valueOf(object.get("value")).trim();
                }
                if (StringUtils.isNotEmpty(name)) {
                    mockExpectJson.put(name, value);
                }
            }
            if (JSONUtil.isEmpty(mockExpectJson)) {
                return model;
            }
        }
        return null;
    }

    public String updateHttpServletResponse(String projectId, MockExpectConfigResponse finalExpectConfig, String url, Map<String, String> headerMap, RequestMockParams requestMockParams, HttpServletResponse response) {
        String returnStr = StringUtils.EMPTY;
        try {
            //设置响应头和响应码
            JSONObject responseObj = JSONUtil.parseObject(JSON.toJSONString(finalExpectConfig.getResponse()));
            String httpCode = responseObj.optString("httpCode");
            if (StringUtils.isEmpty(httpCode)) {
                httpCode = "500";
            }
            response.setStatus(Integer.parseInt(httpCode));
            long sleepTime = 0;
            if (responseObj.has("responseResult")) {
                JSONObject responseJsonObj = responseObj.optJSONObject("responseResult");
                if (responseJsonObj.has("headers")) {
                    JSONArray jsonArray = responseJsonObj.optJSONArray("headers");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.optJSONObject(i);
                        if (object.has("name") && object.has("value")) {
                            String name = String.valueOf(object.get("name")).trim();
                            String value = String.valueOf(object.get("value")).trim();
                            if (StringUtils.isNotEmpty(name)) {
                                response.setHeader(name, value);
                            }
                        }
                    }
                }
                if (responseJsonObj.has("body")) {
                    MockApiUtils mockApiUtils = new MockApiUtils();
                    boolean useScript = false;
                    if (responseJsonObj.has("usePostScript")) {
                        useScript = responseJsonObj.getBoolean("usePostScript");
                    }
                    returnStr = mockApiUtils.getResultByResponseResult(projectId, responseJsonObj.optJSONObject("body"), url, headerMap, requestMockParams, useScript);
                }
                if (responseJsonObj.has("httpCode")) {
                    int httpCodeNum = 500;
                    try {
                        httpCodeNum = Integer.parseInt(responseJsonObj.optString("httpCode"));
                    } catch (Exception ignored) {
                    }
                    response.setStatus(httpCodeNum);
                }
                if (responseJsonObj.has("delayed")) {
                    try {
                        sleepTime = Long.parseLong(String.valueOf(responseJsonObj.get("delayed")));
                    } catch (Exception ignored) {
                    }
                }
            } else {
                JSONArray jsonArray = responseObj.optJSONArray("httpHeads");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.optJSONObject(i);
                    String name = null;
                    String value = StringUtils.EMPTY;
                    if (object.has("name")) {
                        name = String.valueOf(object.get("name")).trim();
                    }
                    if (object.has("value")) {
                        value = String.valueOf(object.get("value")).trim();
                    }
                    if (StringUtils.isNotEmpty(name)) {
                        response.setHeader(name, value);
                    }
                }
                returnStr = String.valueOf(responseObj.get("body"));
                if (responseObj.has("delayed")) {
                    try {
                        sleepTime = Long.parseLong(String.valueOf(responseObj.get("delayed")));
                    } catch (Exception ignored) {
                    }
                }
            }
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (Exception ignored) {
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
            sendMockNotice(mockBlobs, "删除了mock", NoticeConstants.Event.MOCK_DELETE);
            this.deleteMockExpectFiles(mockBlobs);
            mockExpectConfigMapper.deleteByPrimaryKey(id);
        }
    }

    private void deleteMockExpectFiles(MockExpectConfigWithBLOBs mockExpectConfigWithBLOBs) {
        if (mockExpectConfigWithBLOBs != null && StringUtils.isNotEmpty(mockExpectConfigWithBLOBs.getRequest())) {
            try {
                FileUtils.deleteBodyFiles(mockExpectConfigWithBLOBs.getId());
            } catch (Exception ignored) {
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
            List<MockExpectConfigWithBLOBs> deleteBlobs = mockExpectConfigMapper.selectByExampleWithBLOBs(example);
            for (MockExpectConfigWithBLOBs model : deleteBlobs) {
                this.deleteMockExpectFiles(model);
            }
            mockExpectConfigMapper.deleteByExample(example);
        }
        mockConfigMapper.deleteByExample(configExample);
    }

    public void deleteMockConfigByApiIds(List<String> apiIds) {
        MockConfigExample configExample = new MockConfigExample();
        configExample.createCriteria().andApiIdIn(apiIds);
        List<MockConfig> mockConfigList = mockConfigMapper.selectByExample(configExample);
        List<String> mockConfigIds = mockConfigList.stream().map(MockConfig::getId).toList();
        if (CollectionUtils.isNotEmpty(mockConfigIds)) {
            MockExpectConfigExample example = new MockExpectConfigExample();
            example.createCriteria().andMockConfigIdIn(mockConfigIds);
            List<MockExpectConfigWithBLOBs> deleteBlobs = mockExpectConfigMapper.selectByExampleWithBLOBs(example);
            for (MockExpectConfigWithBLOBs model : deleteBlobs) {
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
                if (requestObj != null && requestObj.has("reportType")) {
                    String reportType = requestObj.optString("reportType");
                    if (StringUtils.equalsIgnoreCase(reportType, "xml") && requestObj.has("xmlDataStruct")) {
                        paramNameList = this.parseByTcpTreeDataStruct(requestObj.optString("xmlDataStruct"));
                    } else if (StringUtils.equalsIgnoreCase(reportType, "json") && requestObj.has("jsonDataStruct")) {
                        paramNameList = this.parseByJsonDataStruct(requestObj.optString("jsonDataStruct"));
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
                    if (requestObj.has("arguments")) {
                        try {
                            JSONArray headArr = requestObj.optJSONArray("arguments");
                            for (int index = 0; index < headArr.length(); index++) {

                                JSONObject headObj = headArr.optJSONObject(index);
                                if (headObj.has("name") && !queryParamList.contains(headObj.optString("name"))) {
                                    queryParamList.add(String.valueOf(headObj.get("name")));
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    if (requestObj.has("rest")) {
                        try {
                            JSONArray headArr = requestObj.optJSONArray("rest");
                            for (int index = 0; index < headArr.length(); index++) {
                                JSONObject headObj = headArr.optJSONObject(index);
                                if (headObj.has("name") && !restParamList.contains(headObj.optString("name"))) {
                                    restParamList.add(String.valueOf(headObj.get("name")));
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    //请求体参数类型
                    if (requestObj.has("body")) {
                        try {
                            JSONObject bodyObj = requestObj.optJSONObject("body");
                            if (bodyObj.has(PropertyConstant.TYPE)) {
                                String type = bodyObj.optString(PropertyConstant.TYPE);

                                if (StringUtils.equalsAny(type, "Form Data", "WWW_FORM")) {
                                    if (bodyObj.has("kvs")) {
                                        JSONArray kvsArr = bodyObj.optJSONArray("kvs");
                                        for (int i = 0; i < kvsArr.length(); i++) {
                                            JSONObject kv = kvsArr.optJSONObject(i);
                                            if (kv.has("name") && !formDataList.contains(kv.optString("name"))) {
                                                formDataList.add(String.valueOf(kv.get("name")));
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception ignored) {
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
            returnObj = JSONUtil.parseObject(request);
        } catch (Exception ignored) {
        }
        return returnObj;
    }

    public String getUrlSuffix(String projectId, HttpServletRequest request) {
        String urlPrefix = "/mock/" + projectId + "/";
        String requestUri = request.getRequestURI();
        String[] urlParamArr = requestUri.split(urlPrefix);
        return urlParamArr.length == 0 ? "" : urlParamArr[urlParamArr.length - 1];
    }

    public MockConfigResponse findByApiId(String id) {
        MockConfigExample example = new MockConfigExample();
        MockConfigExample.Criteria criteria = example.createCriteria();
        criteria.andApiIdEqualTo(id);
        List<MockConfig> configList = mockConfigMapper.selectByExample(example);
        return this.assemblyMockConingResponse(configList);
    }

    public String checkReturnWithMockExpectByBodyParam(String method, Map<String, String> requestHeaderMap, Project project, HttpServletRequest request, HttpServletResponse response) {
        String returnStr = StringUtils.EMPTY;
        boolean matchApi = false;
        String url = request.getRequestURL().toString();
        if (project != null) {
            RequestMockParams requestMockParams = MockApiUtils.genRequestMockParamsFromHttpRequest(request, true);
            String urlSuffix = this.getUrlSuffix(project.getSystemId(), request);
            LogUtil.info("Mock [" + url + "] Header:{}", requestHeaderMap);
            LogUtil.info("Mock [" + url + "] request:{}", JSON.toJSONString(requestMockParams));
            List<ApiDefinitionWithBLOBs> qualifiedApiList = apiDefinitionService.preparedUrl(project.getId(), method, urlSuffix, requestHeaderMap.get(MockApiHeaders.MOCK_API_RESOURCE_ID));
            for (ApiDefinitionWithBLOBs api : qualifiedApiList) {
                if (StringUtils.isEmpty(returnStr)) {
                    //补足rest参数
                    MockApiUtils.complementRestParam(urlSuffix, api.getPath(), requestMockParams);
                    MockConfigResponse mockConfigData = this.findByApiId(api.getId());
                    MockExpectConfigResponse finalExpectConfig = this.findExpectConfig(requestHeaderMap, mockConfigData.getMockExpectConfigList(), requestMockParams);
                    if (finalExpectConfig != null) {
                        returnStr = this.updateHttpServletResponse(project.getId(), finalExpectConfig, url, requestHeaderMap, requestMockParams, response);
                    } else {
                        returnStr = this.getApiDefinitionResponse(api, response);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(qualifiedApiList)) {
                matchApi = true;
            }
        }

        if (!matchApi) {
            response.setStatus(404);
            returnStr = Translator.get("mock_warning");
        }
        LogUtil.info("Mock [" + url + "] response:{}", returnStr);
        return returnStr;
    }

    public String checkReturnWithMockExpectByUrlParam(String method, Map<String, String> requestHeaderMap, Project project, HttpServletRequest request, HttpServletResponse response) {
        String returnStr = StringUtils.EMPTY;
        boolean matchApi = false;
        String url = request.getRequestURL().toString();
        if (project != null) {
            RequestMockParams requestMockParams = MockApiUtils.genRequestMockParamsFromHttpRequest(request, false);

            String urlSuffix = this.getUrlSuffix(project.getSystemId(), request);
            LogUtil.info("Mock [" + url + "] Header:{}", requestHeaderMap);
            LogUtil.info("Mock [" + url + "] request:{}", JSON.toJSONString(requestMockParams));
            List<ApiDefinitionWithBLOBs> qualifiedApiList = apiDefinitionService.preparedUrl(project.getId(), method, urlSuffix, requestHeaderMap.get(MockApiHeaders.MOCK_API_RESOURCE_ID));
            /*
              GET/DELETE 这种通过url穿参数的接口，在接口路径相同的情况下可能会出现这样的情况：
               api1: /api/{name}   参数 name = "ABC"
               api2: /api/{testParam} 参数 testParam = "ABC"

               匹配预期Mock的逻辑为： 循环apiId进行筛选，直到筛选到预期Mock。如果筛选不到，则取Api的响应模版来进行返回
             */
            for (ApiDefinitionWithBLOBs api : qualifiedApiList) {
                if (StringUtils.isEmpty(returnStr)) {
                    //补足rest参数
                    MockApiUtils.complementRestParam(urlSuffix, api.getPath(), requestMockParams);
                    MockConfigResponse mockConfigData = this.findByApiId(api.getId());
                    if (mockConfigData != null && mockConfigData.getMockExpectConfigList() != null) {
                        MockExpectConfigResponse finalExpectConfig = this.findExpectConfig(requestHeaderMap, mockConfigData.getMockExpectConfigList(), requestMockParams);
                        if (finalExpectConfig != null) {
                            returnStr = this.updateHttpServletResponse(project.getId(), finalExpectConfig, url, requestHeaderMap, requestMockParams, response);
                        } else {
                            returnStr = this.getApiDefinitionResponse(api, response);
                        }
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(qualifiedApiList)) {
                matchApi = true;
            }
        }

        if (!matchApi) {
            response.setStatus(404);
            returnStr = Translator.get("mock_warning");
        }
        LogUtil.info("Mock [" + url + "] response:{}", returnStr);
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
                    if (StringUtils.isNotEmpty(entry.getKey())) {
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
            if (StringUtils.equalsIgnoreCase(validator.getType().name(), "Object")) {
                JsonStructUtils.deepParseKeyByJsonObject(JSONUtil.parseObject(dataString), returnList);
            } else if (StringUtils.equalsIgnoreCase(validator.getType().name(), "Array")) {
                JsonStructUtils.deepParseKeyByJsonArray(JSONUtil.parseArray(dataString), returnList);
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
        return returnList;
    }

    private List<String> parseByTcpTreeDataStruct(String dataString) {
        List<TcpTreeTableDataStruct> list = JSON.parseArray(dataString, TcpTreeTableDataStruct.class);
        List<String> returnList = new ArrayList<>();
        for (TcpTreeTableDataStruct dataStruct : list) {
            List<String> nameList = dataStruct.getNameDeep();
            for (String name : nameList) {
                if (!returnList.contains(name)) {
                    returnList.add(name);
                }
            }
        }
        return returnList;
    }

    public MockExpectConfigDTO matchTcpMockExpect(String message, int port) {
        ProjectApplicationExample pae = new ProjectApplicationExample();
        pae.createCriteria().andTypeEqualTo(ProjectApplicationType.MOCK_TCP_PORT.name()).andTypeValueEqualTo(String.valueOf(port));
        List<ProjectApplication> projectApplicationsByMockTcpPort = baseProjectApplicationService.selectByExample(pae);
        List<ProjectApplication> projectApplications = new ArrayList<>();
        for (ProjectApplication projectApp : projectApplicationsByMockTcpPort) {
            pae.clear();
            pae.createCriteria().andProjectIdEqualTo(projectApp.getProjectId()).andTypeEqualTo(ProjectApplicationType.MOCK_TCP_OPEN.name()).andTypeValueEqualTo(String.valueOf(true));
            projectApplications.addAll(baseProjectApplicationService.selectByExample(pae));
        }
        List<String> projectIds = projectApplications.stream().map(ProjectApplication::getProjectId).collect(Collectors.toList());
        List<Project> projectList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(projectIds)) {
            ProjectExample projectExample = new ProjectExample();
            projectExample.createCriteria().andIdIn(projectIds);
            projectList = projectMapper.selectByExample(projectExample);
        }

        boolean isJsonMessage = this.checkMessageIsJson(message);
        boolean isXMLMessage = this.checkMessageIsXml(message);

        List<MockExpectConfigDTO> structResult = new ArrayList<>();
        List<MockExpectConfigDTO> rawResult = new ArrayList<>();

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
                    JSONObject requestJson = JSONUtil.parseObject(requestStr);
                    if (requestJson.has("reportType")) {
                        boolean isMatch = false;
                        boolean isRaw = false;
                        String reportType = requestJson.optString("reportType");

                        if (isJsonMessage && StringUtils.equalsIgnoreCase(reportType, "json")) {
                            if (requestJson.has("jsonDataStruct")) {
                                isMatch = JsonStructUtils.checkJsonCompliance(message, requestJson.optString("jsonDataStruct"));
                            }
                        } else if (isXMLMessage && StringUtils.equalsIgnoreCase(reportType, "xml")) {
                            if (requestJson.has("xmlDataStruct")) {
                                JSONObject sourceObj = XMLUtil.xmlStringToJSONObject(message);
                                if (JSONUtil.isNotEmpty(sourceObj)) {
                                    try {
                                        List<TcpTreeTableDataStruct> tcpDataList = JSON.parseArray(requestJson.optString("xmlDataStruct"), TcpTreeTableDataStruct.class);
                                        isMatch = TcpTreeTableDataParser.isMatchTreeTableData(sourceObj, tcpDataList);
                                    } catch (Exception e) {
                                        LogUtil.error(e);
                                    }
                                }
                            }
                        } else if (StringUtils.equalsIgnoreCase(reportType, "raw")) {
                            if (requestJson.has("rawDataStruct")) {
                                String rawDataStruct = requestJson.optString("rawDataStruct");
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
                            JSONObject responseObj = JSONUtil.parseObject(responseStr);
                            if (responseObj.has("body")) {
                                MockExpectConfigDTO dto = new MockExpectConfigDTO();
                                dto.setMockExpectConfig(expectConfig);
                                dto.setProjectId(projectId);
                                if (isRaw) {
                                    rawResult.add(dto);
                                } else {
                                    structResult.add(dto);
                                }
                            }
                        }
                    }

                } catch (Exception ignored) {
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
            XMLUtil.setExpandEntityReferencesFalse(documentBuilderFactory);
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            builder.parse(new InputSource(new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8))));
            isXml = true;
        } catch (Exception e) {
            LogUtil.error(e);
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
        } catch (Exception ignored) {
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
        sqlSession.flushStatements();
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

        for (MockExpectConfigWithBLOBs mockExpect : list) {
            mockExpect.setId(UUID.randomUUID().toString());
            mockExpect.setMockConfigId(mockId);
            mockExpect.setCreateTime(System.currentTimeMillis());
            mockExpect.setUpdateTime(System.currentTimeMillis());
            mockExpect.setCreateUserId(SessionUtils.getUserId());
            mockExpectConfigMapper.insert(mockExpect);
        }
        sqlSession.flushStatements();
    }


    public MockConfig selectMockConfigByApiId(String apiId) {
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
                        JSONObject responseObj = JSONUtil.parseObject(model.getResponse());
                        if (responseObj.has("responseResult")) {
                            JSONObject responseResultObject = responseObj.optJSONObject("responseResult");
                            if (responseResultObject.has("body")) {
                                responseResultObject.optJSONObject("body").put("apiRspRaw", responseDTO.getReturnData());

                                model.setResponse(responseObj.toString());
                                mockExpectConfigMapper.updateByPrimaryKeySelective(model);
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.error(e);
                    }
                }
            }
        }
    }

    public void closeTcpPort(int tcpPort) {
        if (tcpPort != 0) {
            TCPPool.closeTcp(tcpPort);
        }
    }

    public void openTcpPort(int port) {
        TCPPool.createTcp(port);
    }

    /**
     * 检查状态为开启的TCP-Mock服务端口
     */
    public void initMockTcpService() {
        try {
            ProjectApplicationExample pae = new ProjectApplicationExample();
            pae.createCriteria().andTypeEqualTo(ProjectApplicationType.MOCK_TCP_OPEN.name()).andTypeValueEqualTo(String.valueOf(true));
            pae.or().andTypeEqualTo(ProjectApplicationType.MOCK_TCP_PORT.name()).andTypeValueEqualTo(String.valueOf(0));
            List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(pae);
            List<String> projectIds = projectApplications.stream().map(ProjectApplication::getProjectId).collect(Collectors.toList());
            List<Integer> openedPortList = new ArrayList<>();
            for (String projectId : projectIds) {
                ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(projectId, ProjectApplicationType.MOCK_TCP_PORT.name());
                Integer mockPort = config.getMockTcpPort();
                boolean isPortInRange = this.isMockTcpPortIsInRange(mockPort);
                if (isPortInRange && !openedPortList.contains(mockPort)) {
                    openedPortList.add(mockPort);
                    Project project = new Project();
                    project.setId(projectId);
                    this.openMockTcp(project);
                } else {
                    if (openedPortList.contains(mockPort)) {
                        extProjectApplicationService.createOrUpdateConfig(projectId, ProjectApplicationType.MOCK_TCP_PORT.name(), String.valueOf(mockPort));
                    }
                    extProjectApplicationService.createOrUpdateConfig(projectId, ProjectApplicationType.MOCK_TCP_OPEN.name(), String.valueOf(false));
                }
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    public void openMockTcp(Project project) {
        if (project == null) {
            MSException.throwException("Project not found!");
        } else {
            ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.MOCK_TCP_PORT.name());
            Integer mockPort = config.getMockTcpPort();
            if (mockPort == null) {
                MSException.throwException("Mock tcp port is not Found!");
            } else {
                TCPPool.createTcp(mockPort);
            }
        }
    }

    private boolean isMockTcpPortIsInRange(int port) {
        boolean inRange = false;
        if (StringUtils.isNotEmpty(this.tcpMockPorts)) {
            try {
                if (this.tcpMockPorts.contains("-")) {
                    String[] tcpMockPortArr = this.tcpMockPorts.split("-");
                    int num1 = Integer.parseInt(tcpMockPortArr[0]);
                    int num2 = Integer.parseInt(tcpMockPortArr[1]);

                    int startNum = Math.min(num1, num2);
                    int endNum = Math.max(num1, num2);

                    if (!(port < startNum || port > endNum)) {
                        inRange = true;
                    }
                } else {
                    int tcpPortConfigNum = Integer.parseInt(this.tcpMockPorts);
                    if (port == tcpPortConfigNum) {
                        inRange = true;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return inRange;
    }

    public String getMockInfo(String projectId) {
        String returnStr = StringUtils.EMPTY;
        ApiTestEnvironmentWithBLOBs mockEnv = baseEnvironmentService.getMockEnvironmentByProjectId(projectId);
        if (mockEnv != null && mockEnv.getConfig() != null) {
            try {
                JSONObject configObj = JSONUtil.parseObject(mockEnv.getConfig());
                if (configObj.has("tcpConfig")) {
                    JSONObject tcpConfigObj = configObj.optJSONObject("tcpConfig");
                    int tcpPort;
                    if (tcpConfigObj.has("port")) {
                        tcpPort = tcpConfigObj.optInt("port");
                        if (tcpPort == 0 || !TCPPool.isTcpOpen(tcpPort)) {
                            return returnStr;
                        }
                    } else {
                        return returnStr;
                    }
                    if (tcpConfigObj.has("server")) {
                        String server = tcpConfigObj.optString("server");
                        returnStr = server + ":" + tcpPort;
                    } else {
                        return returnStr;
                    }
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
        return returnStr;
    }
}
