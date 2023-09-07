package io.metersphere.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.automation.ApiScenarioReportResult;
import io.metersphere.api.dto.share.*;
import io.metersphere.api.exec.generator.JSONSchemaParser;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ShareInfoMapper;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportMapper;
import io.metersphere.base.mapper.ext.ExtShareInfoMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiScenarioMapper;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.constants.PropertyConstant;
import io.metersphere.commons.constants.ShareType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.i18n.Translator;
import io.metersphere.service.definition.ApiModuleService;
import io.metersphere.service.scenario.ApiScenarioReportService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.commons.user.ShareUtil.getTimeMills;

/**
 * @author song.tianyang
 * @Date 2021/2/7 10:37 上午
 * @Description
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ShareInfoService extends BaseShareInfoService {

    @Resource
    ExtShareInfoMapper extShareInfoMapper;
    @Resource
    ShareInfoMapper shareInfoMapper;
    @Resource
    ApiModuleService apiModuleService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    private ApiScenarioReportService apiScenarioReportService;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    ExtTestPlanApiScenarioMapper extTestPlanApiScenarioMapper;

    public Pager<List<ApiDocumentInfoDTO>> selectApiInfoByParam(ApiDocumentRequest apiDocumentRequest, int goPage, int pageSize) {
        this.iniApiDocumentRequest(apiDocumentRequest);

        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        List<ApiDefinitionWithBLOBs> apiDocumentInfoDTOS = new ArrayList<>();
        if (this.isParamLegitimacy(apiDocumentRequest)) {
            apiDocumentInfoDTOS = this.findApiDocumentSimpleInfoByRequest(apiDocumentRequest, goPage, pageSize);
        }
        PageHelper.clearPage();
        List<ApiDocumentInfoDTO> returnList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(apiDocumentInfoDTOS)) {
            List<String> apiModuleIdList = new ArrayList<>();
            LogUtil.info("查找模块相关信息");
            List<String> userIdList = new ArrayList<>();
            apiDocumentInfoDTOS.forEach(item -> {
                if (StringUtils.isNotBlank(item.getModuleId()) && !apiModuleIdList.contains(item.getModuleId())) {
                    apiModuleIdList.add(item.getModuleId());
                    if (!userIdList.contains(item.getUserId())) {
                        userIdList.add(item.getUserId());
                    }
                    if (!userIdList.contains(item.getCreateUser())) {
                        userIdList.add(item.getCreateUser());
                    }
                }
            });
            Map<String, User> selectedUserMap = this.getUserIdMapByIds(userIdList);
            Map<String, String> moduleNameMap = apiModuleService.getApiModuleNameDicByIds(apiModuleIdList);
            LogUtil.info("开始遍历组装数据");
            returnList = this.conversionModelListToDTO(apiDocumentInfoDTOS, selectedUserMap, moduleNameMap);
        }
        return PageUtils.setPageInfo(page, returnList);
    }

    private Map<String, User> getUserIdMapByIds(List<String> userIdList) {
        Map<String, User> map = new HashMap<>();
        if (CollectionUtils.isEmpty(userIdList)) {
            return map;
        }
        UserExample example = new UserExample();
        example.createCriteria().andIdIn(userIdList);
        List<User> users = userMapper.selectByExample(example);
        users.forEach(item -> {
            map.put(item.getId(), item);
        });
        return map;
    }

    public List<ApiDefinitionWithBLOBs> findApiDocumentSimpleInfoByRequest(ApiDocumentRequest request, int goPage, int pageSize) {
        List<ApiDefinitionWithBLOBs> apiList = this.selectByRequest(request);
        return this.sortApiListByRequest(apiList, request);
    }

    private List<ApiDefinitionWithBLOBs> sortApiListByRequest(List<ApiDefinitionWithBLOBs> apiList, ApiDocumentRequest request) {
        if (CollectionUtils.isNotEmpty(request.getApiIdList()) && CollectionUtils.isNotEmpty(apiList)) {
            List<ApiDefinitionWithBLOBs> returnList = new ArrayList<>(apiList.size());
            Map<String, ApiDefinitionWithBLOBs> apiDefinitionWithBLOBsMap = apiList.stream().collect(Collectors.toMap(ApiDefinitionWithBLOBs::getId, (k1 -> k1)));
            for (String id : request.getApiIdList()) {
                ApiDefinitionWithBLOBs api = apiDefinitionWithBLOBsMap.get(id);
                if (api != null) {
                    returnList.add(api);
                }
            }
            return returnList;
        } else {
            return apiList;
        }
    }

    private void iniApiDocumentRequest(ApiDocumentRequest request) {
        if (StringUtils.isNotBlank(request.getShareId())) {
            List<String> shareIdList = this.selectShareIdByShareInfoId(request.getShareId());
            request.setApiIdList(shareIdList);
        }
    }

    public List<ApiDefinitionWithBLOBs> selectByRequest(ApiDocumentRequest request) {
        if (StringUtils.isNotBlank(request.getProjectId()) || CollectionUtils.isNotEmpty(request.getApiIdList())) {
            return extShareInfoMapper.findApiDocumentSimpleInfoByRequest(request);
        } else {
            return new ArrayList<>(0);
        }
    }

    private List<String> selectShareIdByShareInfoId(String shareId) {
        List<String> shareApiIdList = new ArrayList<>();
        ShareInfo share = shareInfoMapper.selectByPrimaryKey(shareId);
        if (share != null) {
            try {
                ArrayNode jsonArray = JSONUtil.parseArrayNode(share.getCustomData());
                for (int i = 0; i < jsonArray.size(); i++) {
                    String apiId = jsonArray.get(i).asText();
                    shareApiIdList.add(apiId);
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }
        }
        return shareApiIdList;
    }

    //参数是否合法
    private boolean isParamLegitimacy(ApiDocumentRequest request) {
        if (StringUtils.isAllEmpty(request.getProjectId(), request.getShareId())) {
            return false;
        }
        return true;
    }

    public List<ApiDocumentInfoDTO> conversionModelListToDTO(List<ApiDefinitionWithBLOBs> apiModelList, Map<String, User> userIdMap, Map<String, String> moduleMap) {
        if (CollectionUtils.isEmpty(apiModelList)) {
            return new ArrayList<>(0);
        } else {
            List<ApiDocumentInfoDTO> returnList = new ArrayList<>(apiModelList.size());
            for (ApiDefinitionWithBLOBs apiModel : apiModelList) {
                ApiDocumentInfoDTO apiInfoDTO = new ApiDocumentInfoDTO();
                ArrayNode previewJsonArray = JSONUtil.createArray();
                if (apiModel != null) {
                    LogUtil.info(apiModel.getName() + ": start");
                    apiInfoDTO.setId(apiModel.getId());
                    apiInfoDTO.setName(apiModel.getName());
                    apiInfoDTO.setMethod(apiModel.getMethod());
                    apiInfoDTO.setUri(apiModel.getPath());
                    apiInfoDTO.setStatus(apiModel.getStatus());
                    LogUtil.info(apiModel.getName() + ":tag");
                    if (StringUtils.isNotEmpty(apiModel.getTags())) {
                        ArrayNode tagsArr = JSONUtil.parseArrayNode(apiModel.getTags());
                        List<String> tagList = new ArrayList<>();
                        for (int i = 0; i < tagsArr.size(); i++) {
                            tagList.add(tagsArr.get(i).asText());
                        }
                        if (!tagList.isEmpty()) {
                            apiInfoDTO.setTags(StringUtils.join(tagList, ","));
                        }
                    }
                    LogUtil.info(apiModel.getName() + ":baseInfo");
                    apiInfoDTO.setResponsibler(userIdMap.get(apiModel.getUserId()) == null ? apiModel.getUserId() : userIdMap.get(apiModel.getUserId()).getName());
                    apiInfoDTO.setCreateUser(userIdMap.get(apiModel.getCreateUser()) == null ? apiModel.getCreateUser() : userIdMap.get(apiModel.getCreateUser()).getName());
                    apiInfoDTO.setDesc(apiModel.getDescription());
                    if (MapUtils.isNotEmpty(moduleMap)) {
                        apiInfoDTO.setModules(moduleMap.containsKey(apiModel.getModuleId()) ? moduleMap.get(apiModel.getModuleId()) : StringUtils.EMPTY);
                    } else {
                        ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
                        apiInfoDTO.setModules(apiModuleService.getModuleNameById(apiModel.getModuleId()));
                    }
                    LogUtil.info(apiModel.getName() + ":request");
                    if (apiModel.getRequest() != null) {
                        ObjectNode requestObj = this.genJSONObject(apiModel.getRequest());
                        if (requestObj != null) {
                            if (requestObj.has("headers")) {
                                List<JsonNode> requestHeadDataArr = new LinkedList<>();
                                //head赋值
                                ArrayNode headArr = requestObj.withArray("headers");
                                for (int index = 0; index < headArr.size(); index++) {
                                    JsonNode headObj = headArr.get(index);
                                    if (this.isObjectHasKey(headObj, "name")) {
                                        requestHeadDataArr.add(headObj);
                                    }
                                }
                                apiInfoDTO.setRequestHead(requestHeadDataArr.toString());
                            }
                            //url参数赋值
                            ArrayNode urlParamArr = JSONUtil.createArray();
                            if (requestObj.has("arguments")) {
                                try {
                                    ArrayNode headArr = requestObj.withArray("arguments");
                                    for (int index = 0; index < headArr.size(); index++) {
                                        JsonNode headObj = headArr.get(index);
                                        if (this.isObjectHasKey(headObj, "name")) {
                                            urlParamArr.add(headObj);
                                        }
                                    }
                                } catch (Exception e) {
                                    LogUtil.error(e.getMessage());
                                }
                            }
                            //rest参数设置
                            ArrayNode restParamArr = JSONUtil.createArray();
                            if (requestObj.has("rest")) {
                                try {
                                    //urlParam -- rest赋值
                                    ArrayNode headArr = requestObj.withArray("rest");
                                    for (int index = 0; index < headArr.size(); index++) {
                                        JsonNode headObj = headArr.get(index);
                                        if (this.isObjectHasKey(headObj, "name")) {
                                            restParamArr.add(headObj);
                                        }
                                    }
                                } catch (Exception e) {
                                    LogUtil.error(e.getMessage());
                                }
                            }
                            apiInfoDTO.setUrlParams(urlParamArr.toString());
                            apiInfoDTO.setRestParams(restParamArr.toString());
                            //请求体参数类型
                            if (requestObj.has("body")) {
                                try {
                                    JsonNode bodyObj = requestObj.get("body");
                                    if (this.isObjectHasKey(bodyObj, PropertyConstant.TYPE)) {
                                        String type = bodyObj.get(PropertyConstant.TYPE).asText();
                                        if (StringUtils.equals(type, "WWW_FORM")) {
                                            apiInfoDTO.setRequestBodyParamType("x-www-from-urlencoded");
                                        } else if (StringUtils.equals(type, "Form Data")) {
                                            apiInfoDTO.setRequestBodyParamType("form-data");
                                        } else {
                                            apiInfoDTO.setRequestBodyParamType(type);
                                        }

                                        if (StringUtils.equals(type, "JSON")) {
                                            //判断是否是JsonSchema
                                            boolean isJsonSchema = false;
                                            if (bodyObj.has("format")) {
                                                String formatValue = bodyObj.get("format").asText();
                                                if (StringUtils.equals("JSON-SCHEMA", formatValue)) {
                                                    isJsonSchema = true;
                                                }
                                            }
                                            JSONSchemaBodyDTO jsonSchemaBodyDTO = new JSONSchemaBodyDTO();
                                            if (isJsonSchema) {
                                                jsonSchemaBodyDTO.setJsonSchema(bodyObj.get("jsonSchema"));
                                                apiInfoDTO.setJsonSchemaBody(jsonSchemaBodyDTO);
                                                String raw = JSONSchemaParser.schemaToJson(JSONUtil.toJSONString(bodyObj.get("jsonSchema")));
                                                this.setPreviewData(previewJsonArray, raw);
                                            } else {
                                                if (bodyObj.has("raw")) {
                                                    String raw = bodyObj.get("raw").asText();
                                                    jsonSchemaBodyDTO.setRaw(raw);
                                                    apiInfoDTO.setJsonSchemaBody(jsonSchemaBodyDTO);
                                                    apiInfoDTO.setRequestBodyStructureData(raw);
                                                    //转化jsonObje 或者 jsonArray
                                                    this.setPreviewData(previewJsonArray, raw);
                                                }
                                            }
                                        } else if (StringUtils.equalsAny(type, "XML", "Raw")) {
                                            if (bodyObj.has("raw")) {
                                                String raw = bodyObj.get("raw").asText();
                                                apiInfoDTO.setRequestBodyStructureData(raw);
                                                this.setPreviewData(previewJsonArray, raw);
                                            }
                                        } else if (StringUtils.equalsAny(type, "Form Data", "WWW_FORM")) {
                                            if (bodyObj.has("kvs")) {
                                                ArrayNode bodyParamArr = JSONUtil.createArray();
                                                ArrayNode kvsArr = bodyObj.withArray("kvs");
                                                Map<String, String> previewObjMap = new LinkedHashMap<>();
                                                for (int i = 0; i < kvsArr.size(); i++) {
                                                    JsonNode kv = kvsArr.get(i);
                                                    if (this.isObjectHasKey(kv, "name")) {
                                                        String value = StringUtils.EMPTY;
                                                        if (kv.has("value")) {
                                                            value = kv.get("value").asText();
                                                        }
                                                        bodyParamArr.add(kv);
                                                        previewObjMap.put(kv.get("name").asText(), value);
                                                    }
                                                }
                                                this.setPreviewData(previewJsonArray, JSON.toJSONString(previewObjMap));
                                                apiInfoDTO.setRequestBodyFormData(bodyParamArr.toString());
                                            }
                                        } else if (StringUtils.equals(type, "BINARY")) {
                                            if (bodyObj.has("binary")) {
                                                List<Map<String, String>> bodyParamList = new ArrayList<>();
                                                ArrayNode kvsArr = bodyObj.withArray("binary");
                                                Map<String, String> previewObjMap = new LinkedHashMap<>();
                                                for (int i = 0; i < kvsArr.size(); i++) {
                                                    JsonNode kv = kvsArr.get(i);
                                                    if (this.isObjectHasKey(kv, "description") && this.isObjectHasKey(kv, "files")) {
                                                        Map<String, String> bodyMap = new HashMap<>();
                                                        String name = kv.get("description").asText();
                                                        ArrayNode fileArr = kv.withArray("files");
                                                        String value = StringUtils.EMPTY;
                                                        for (int j = 0; j < fileArr.size(); j++) {
                                                            JsonNode fileObj = fileArr.get(j);
                                                            if (this.isObjectHasKey(fileObj, "name")) {
                                                                value += fileObj.get("name") + " ;";
                                                            }
                                                        }
                                                        bodyMap.put("name", name);
                                                        bodyMap.put("value", value);
                                                        bodyMap.put("contentType", "File");
                                                        bodyParamList.add(bodyMap);

                                                        previewObjMap.put(name, value);

                                                    }
                                                }
                                                this.setPreviewData(previewJsonArray, JSON.toJSONString(previewObjMap));
                                                apiInfoDTO.setRequestBodyFormData(JSON.toJSONString(bodyParamList));
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    LogUtil.error(e.getMessage());
                                }
                            }
                        }
                    }
                    LogUtil.info(apiModel.getName() + ":response");
                    //赋值响应头
                    if (apiModel.getResponse() != null) {
                        ObjectNode responseJsonObj = this.genJSONObject(apiModel.getResponse());
                        if (this.isObjectHasKey(responseJsonObj, "headers")) {
                            try {
                                ArrayNode list = JSONUtil.createArray();
                                ArrayNode headArr = responseJsonObj.withArray("headers");
                                for (int index = 0; index < headArr.size(); index++) {
                                    JsonNode headObj = headArr.get(index);
                                    if (this.isObjectHasKey(headObj, "name")) {
                                        list.add(headObj);
                                    }
                                }
                                apiInfoDTO.setResponseHead(list.toString());
                            } catch (Exception e) {
                                LogUtil.error(e.getMessage());
                            }
                        }
                        // 赋值响应体
                        if (this.isObjectHasKey(responseJsonObj, "body")) {
                            try {
                                JsonNode bodyObj = responseJsonObj.get("body");
                                if (this.isObjectHasKey(bodyObj, PropertyConstant.TYPE)) {
                                    String type = bodyObj.get(PropertyConstant.TYPE).asText();
                                    if (StringUtils.equals(type, "WWW_FORM")) {
                                        apiInfoDTO.setResponseBodyParamType("x-www-from-urlencoded");
                                    } else if (StringUtils.equals(type, "Form Data")) {
                                        apiInfoDTO.setResponseBodyParamType("form-data");
                                    } else {
                                        apiInfoDTO.setResponseBodyParamType(type);
                                    }
                                    if (StringUtils.equalsIgnoreCase(type, "Raw")) {
                                        if (bodyObj.has("raw")) {
                                            String raw = bodyObj.get("raw").asText();
                                            apiInfoDTO.setResponseBodyStructureData(raw);
                                        }
                                    } else if (StringUtils.equalsAny(type, "JSON", "XML")) {

                                        //判断是否是JsonSchema
                                        boolean isJsonSchema = false;
                                        if (bodyObj.has("format")) {
                                            String formatValue = bodyObj.get("format").asText();
                                            if (StringUtils.equals("JSON-SCHEMA", formatValue)) {
                                                isJsonSchema = true;
                                            }
                                        }

                                        JSONSchemaBodyDTO jsonSchemaBodyDTO = new JSONSchemaBodyDTO();
                                        if (isJsonSchema) {
                                            jsonSchemaBodyDTO.setJsonSchema(bodyObj.get("jsonSchema"));
                                            apiInfoDTO.setResponseBodyParamType("JSON-SCHEMA");
                                            apiInfoDTO.setJsonSchemaResponseBody(jsonSchemaBodyDTO);
                                        } else {
                                            if (bodyObj.has("raw")) {
                                                String raw = bodyObj.get("raw").asText();
                                                apiInfoDTO.setResponseBodyStructureData(raw);
                                                jsonSchemaBodyDTO.setRaw(raw);
                                                apiInfoDTO.setResponseBodyParamType("JSON-SCHEMA");
                                                apiInfoDTO.setJsonSchemaResponseBody(jsonSchemaBodyDTO);
                                            }
                                        }
                                    } else if (StringUtils.equalsAny(type, "Form Data", "WWW_FORM")) {
                                        if (bodyObj.has("kvs")) {
                                            ArrayNode bodyParamArr = JSONUtil.createArray();
                                            ArrayNode kvsArr = bodyObj.withArray("kvs");
                                            for (int i = 0; i < kvsArr.size(); i++) {
                                                JsonNode kv = kvsArr.get(i);
                                                if (this.isObjectHasKey(kv, "name")) {
                                                    bodyParamArr.add(kv);
                                                }
                                            }
                                            apiInfoDTO.setResponseBodyFormData(bodyParamArr.toString());
                                        }
                                    } else if (StringUtils.equals(type, "BINARY")) {
                                        if (bodyObj.has("binary")) {
                                            List<Map<String, String>> bodyParamList = new ArrayList<>();
                                            ArrayNode kvsArr = bodyObj.withArray("kvs");
                                            for (int i = 0; i < kvsArr.size(); i++) {
                                                JsonNode kv = kvsArr.get(i);
                                                if (kv.has("description") && kv.has("files")) {
                                                    Map<String, String> bodyMap = new HashMap<>();

                                                    String name = kv.get("description").asText();
                                                    ArrayNode fileArr = kv.withArray("files");
                                                    String value = StringUtils.EMPTY;
                                                    for (int j = 0; j < fileArr.size(); j++) {
                                                        JsonNode fileObj = fileArr.get(j);
                                                        if (this.isObjectHasKey(fileObj, "name")) {
                                                            value += fileObj.get("name") + " ;";
                                                        }
                                                    }
                                                    bodyMap.put("name", name);
                                                    bodyMap.put("value", value);
                                                    bodyParamList.add(bodyMap);
                                                }
                                            }
                                            apiInfoDTO.setResponseBodyFormData(JSON.toJSONString(bodyParamList));
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                LogUtil.error(e.getMessage());
                            }

                        }
                        // 赋值响应码
                        if (this.isObjectHasKey(responseJsonObj, "statusCode")) {
                            try {
                                ArrayNode responseStatusDataArr = JSONUtil.createArray();
                                ArrayNode statusArr = responseJsonObj.withArray("statusCode");
                                for (int index = 0; index < statusArr.size(); index++) {
                                    JsonNode statusObj = statusArr.get(index);
                                    if (this.isObjectHasKey(statusObj, "name")) {
                                        responseStatusDataArr.add(statusObj);
                                    }
                                }
                                apiInfoDTO.setResponseCode(responseStatusDataArr.toString());
                            } catch (Exception e) {
                                LogUtil.error(e.getMessage());
                            }
                        }
                    }
                    apiInfoDTO.setRemark(apiModel.getRemark());
                }
                LogUtil.info(apiModel.getName() + ":end and preview");
                if (previewJsonArray != null) {
                    if (previewJsonArray.size() == 1) {
                        apiInfoDTO.setRequestPreviewData(previewJsonArray.get(0));
                    } else {
                        apiInfoDTO.setRequestPreviewData(previewJsonArray);
                    }
                }
                apiInfoDTO.setSelectedFlag(true);
                LogUtil.info(apiModel.getName() + ":over");
                returnList.add(apiInfoDTO);
            }
            return returnList;
        }
    }

    private ObjectNode genJSONObject(String request) {
        ObjectNode returnObj = null;
        try {
            returnObj = JSONUtil.parseObjectNode(request);
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
        return returnObj;
    }

    private ArrayNode setPreviewData(ArrayNode previewArray, String data) {
        try {
            if (StringUtils.startsWith(data, "{") && StringUtils.endsWith(data, "}")) {
                ObjectNode previewObj = JSONUtil.parseObjectNode(data);
                previewArray.add(previewObj);
            } else {
                previewArray = JSONUtil.parseArrayNode(data);
            }

        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
        return previewArray;
    }

    /**
     * 生成 api接口文档分享信息
     * 根据要分享的api_id和分享方式来进行完全匹配搜索。
     * 搜索的到就返回那条数据，搜索不到就新增一条信息
     *
     * @param request 入参
     * @return ShareInfo数据对象
     */
    public ShareInfo generateApiDocumentShareInfo(ApiDocumentShareRequest request) {
        if (StringUtils.equalsAny(request.getShareType(), ShareInfoType.Single.name(), ShareInfoType.Batch.name())) {
            String customData = null;
            if (request.getSelectRequest() != null) {
                List<String> shareIdList = extShareInfoMapper.findIdByRequest(request.getSelectRequest());
                customData = genShareIdJsonString(shareIdList);
            } else if (StringUtils.isNotBlank(request.getShareId())) {
                customData = genShareIdJsonString(new ArrayList<>(1) {{
                    this.add(request.getShareId());
                }});
            }

            if (StringUtils.isNotBlank(customData)) {
                ShareInfo shareInfoRequest = new ShareInfo();
                BeanUtils.copyBean(shareInfoRequest, request);
                shareInfoRequest.setCustomData(customData);
                return generateShareInfo(shareInfoRequest);
            }
        }
        return new ShareInfo();
    }

    /**
     * 根据treeSet排序生成apiId的Jsonarray字符串
     *
     * @param shareApiIdList 要分享的ID集合
     * @return 要分享的ID JSON格式字符串
     */
    private String genShareIdJsonString(Collection<String> shareApiIdList) {
        TreeSet<String> treeSet = new TreeSet<>(shareApiIdList);
        return JSON.toJSONString(treeSet);
    }

    public String getTestPlanShareUrl(String testPlanReportId, String userId) {
        ShareInfo shareRequest = new ShareInfo();
        shareRequest.setCustomData(testPlanReportId);
        shareRequest.setShareType(ShareType.PLAN_DB_REPORT.name());
        shareRequest.setCreateUserId(userId);
        ShareInfo shareInfo = generateShareInfo(shareRequest);
        return conversionShareInfoToDTO(shareInfo).getShareUrl();
    }

    private boolean isObjectHasKey(JsonNode object, String key) {
        return object != null && object.has(key);
    }


    public void validateExpired(String shareId) {
        ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(shareId);
        this.validateExpired(shareInfo);
    }

    /**
     * 不加入事务，抛出异常不回滚
     * 若在当前类中调用请使用如下方式调用，否则该方法的事务注解不生效
     * ShareInfoService shareInfoService = CommonBeanFactory.getBean(ShareInfoService.class);
     * shareInfoService.validateExpired(shareInfo);
     *
     * @param shareInfo
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void validateExpired(ShareInfo shareInfo) {
        // 有效期根据类型从ProjectApplication中获取

        if (shareInfo == null) {
            MSException.throwException(Translator.get("connection_expired"));
        }
        String type = ProjectApplicationType.API_SHARE_REPORT_TIME.toString();
        String projectId = "";
        ApiScenarioReportResult reportResult = extApiScenarioReportMapper.get(shareInfo.getCustomData());
        if (reportResult != null) {
            projectId = reportResult.getProjectId();
        } else {
            // case 集成报告
            ApiScenarioReportResult result = apiScenarioReportService.getApiIntegrated(shareInfo.getCustomData());
            if (result != null) {
                projectId = result.getProjectId();
            }
        }

        if (StringUtils.isBlank(type) || Strings.isBlank(projectId)) {
            millisCheck(System.currentTimeMillis() - shareInfo.getUpdateTime(), 1000 * 60 * 60 * 24, shareInfo.getId());
        } else {
            ProjectApplication projectApplication = baseProjectApplicationService.getProjectApplication(projectId, type);
            if (projectApplication.getTypeValue() == null) {
                millisCheck(System.currentTimeMillis() - shareInfo.getUpdateTime(), 1000 * 60 * 60 * 24, shareInfo.getId());
            } else {
                String expr = projectApplication.getTypeValue();
                long timeMills = getTimeMills(shareInfo.getUpdateTime(), expr);
                millisCheck(System.currentTimeMillis(), timeMills, shareInfo.getId());
            }
        }
    }

    private void millisCheck(long compareMillis, long millis, String shareInfoId) {
        if (compareMillis > millis) {
            shareInfoMapper.deleteByPrimaryKey(shareInfoId);
            MSException.throwException(Translator.get("connection_expired"));
        }
    }

    public void validate(String shareId) {
        ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(shareId);
        if (ObjectUtils.isNotEmpty(shareInfo)) {
            String projectId = extTestPlanApiScenarioMapper.selectPlanIdByTestPlanId(shareInfo.getCustomData());
            validateExpiredTestPlan(shareInfo, projectId);
        }
        if (shareInfo == null) {
            MSException.throwException("ShareInfo not exist!");
        }
    }


    public void render(Pager<List<ApiDocumentInfoDTO>> listPager, HttpServletResponse response) throws
            UnsupportedEncodingException {
        response.reset();
        response.setContentType("application/octet-stream");
        response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("test", StandardCharsets.UTF_8));

        try (InputStreamReader isr = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/public/api-doc.html")), StandardCharsets.UTF_8);
             ServletOutputStream outputStream = response.getOutputStream()) {
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while (null != (line = bufferedReader.readLine())) {
                if (line.contains("\"#export-doc\"")) {
                    String reportInfo = JSON.toJSONString(listPager);
                    line = line.replace("\"#export-doc\"", reportInfo);
                }
                line += StringUtils.LF;
                byte[] lineBytes = line.getBytes(StandardCharsets.UTF_8);
                int start = 0;
                while (start < lineBytes.length) {
                    if (start + 1024 < lineBytes.length) {
                        outputStream.write(lineBytes, start, 1024);
                    } else {
                        outputStream.write(lineBytes, start, lineBytes.length - start);
                    }
                    outputStream.flush();
                    start += 1024;
                }
            }
        } catch (Throwable e) {
            LogUtil.error(e);
            MSException.throwException(e);
        }
    }

    public void exportPageDoc(ApiDocumentRequest apiDocumentRequest, int goPage, int pageSize, HttpServletResponse response) {
        Pager<List<ApiDocumentInfoDTO>> listPager = this.selectApiInfoByParam(apiDocumentRequest, goPage, pageSize);
        try {
            this.render(listPager, response);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
