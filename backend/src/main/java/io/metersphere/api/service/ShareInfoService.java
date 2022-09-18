package io.metersphere.api.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.automation.APIScenarioReportResult;
import io.metersphere.api.dto.share.*;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportMapper;
import io.metersphere.base.mapper.ext.ExtShareInfoMapper;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.constants.ShareType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.json.JSONSchemaGenerator;
import io.metersphere.commons.utils.*;
import io.metersphere.i18n.Translator;
import io.metersphere.service.ProjectApplicationService;
import io.metersphere.service.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.api.service.utils.ShareUtil.getTimeMills;

/**
 * @author song.tianyang
 * @Date 2021/2/7 10:37 上午
 * @Description
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ShareInfoService {

    @Resource
    ExtShareInfoMapper extShareInfoMapper;
    @Resource
    ShareInfoMapper shareInfoMapper;
    @Resource
    TestPlanReportMapper testPlanReportMapper;
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Lazy
    @Resource
    TestPlanReportContentMapper testPlanReportContentMapper;
    @Lazy
    @Resource
    TestPlanMapper testPlanMapper;
    @Resource
    private ExtApiScenarioReportMapper extApiScenarioReportMapper;
    @Resource
    ApiScenarioReportService apiScenarioReportService;
    @Resource
    ApiModuleService apiModuleService;
    @Resource
    UserService userService;

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
            Map<String, User> seletedUserMap = userService.getUserIdMapByIds(userIdList);
            Map<String, String> moduleNameMap = apiModuleService.getApiModuleNameDicByIds(apiModuleIdList);
            LogUtil.info("开始遍历组装数据");
            returnList = this.conversionModelListToDTO(apiDocumentInfoDTOS, seletedUserMap, moduleNameMap);
        }


        return PageUtils.setPageInfo(page, returnList);
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
        List<String> shareIdList = this.selectShareIdByShareInfoId(request.getShareId());
        request.setApiIdList(shareIdList);
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
                JSONArray jsonArray = JSONArray.parseArray(share.getCustomData());
                for (int i = 0; i < jsonArray.size(); i++) {
                    String apiId = jsonArray.getString(i);
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
                JSONArray previewJsonArray = new JSONArray();
                if (apiModel != null) {
                    LogUtil.info(apiModel.getName() + ": start");
                    apiInfoDTO.setId(apiModel.getId());
                    apiInfoDTO.setName(apiModel.getName());
                    apiInfoDTO.setMethod(apiModel.getMethod());
                    apiInfoDTO.setUri(apiModel.getPath());
                    apiInfoDTO.setStatus(apiModel.getStatus());
                    LogUtil.info(apiModel.getName() + ":tag");
                    if (StringUtils.isNotEmpty(apiModel.getTags())) {
                        JSONArray tagsArr = JSONArray.parseArray(apiModel.getTags());
                        List<String> tagList = new ArrayList<>();
                        for (int i = 0; i < tagsArr.size(); i++) {
                            tagList.add(tagsArr.getString(i));
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
                        apiInfoDTO.setModules(moduleMap.containsKey(apiModel.getModuleId()) ? moduleMap.get(apiModel.getModuleId()) : "");
                    } else {
                        ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
                        apiInfoDTO.setModules(apiModuleService.getModuleNameById(apiModel.getModuleId()));
                    }
                    LogUtil.info(apiModel.getName() + ":request");
                    if (apiModel.getRequest() != null) {
                        JSONObject requestObj = this.genJSONObject(apiModel.getRequest());
                        if (requestObj != null) {
                            if (requestObj.containsKey("headers")) {
                                JSONArray requestHeadDataArr = new JSONArray();
                                //head赋值
                                JSONArray headArr = requestObj.getJSONArray("headers");
                                for (int index = 0; index < headArr.size(); index++) {
                                    JSONObject headObj = headArr.getJSONObject(index);
                                    if (this.isObjectHasKey(headObj, "name")) {
                                        requestHeadDataArr.add(headObj);
                                    }
                                }
                                apiInfoDTO.setRequestHead(requestHeadDataArr.toJSONString());
                            }
                            //url参数赋值
                            JSONArray urlParamArr = new JSONArray();
                            if (requestObj.containsKey("arguments")) {
                                try {
                                    JSONArray headArr = requestObj.getJSONArray("arguments");
                                    for (int index = 0; index < headArr.size(); index++) {
                                        JSONObject headObj = headArr.getJSONObject(index);
                                        if (this.isObjectHasKey(headObj, "name")) {
                                            urlParamArr.add(headObj);
                                        }
                                    }
                                } catch (Exception e) {
                                    LogUtil.error(e.getMessage());
                                }
                            }
                            //rest参数设置
                            JSONArray restParamArr = new JSONArray();
                            if (requestObj.containsKey("rest")) {
                                try {
                                    //urlParam -- rest赋值
                                    JSONArray headArr = requestObj.getJSONArray("rest");
                                    for (int index = 0; index < headArr.size(); index++) {
                                        JSONObject headObj = headArr.getJSONObject(index);
                                        if (this.isObjectHasKey(headObj, "name")) {
                                            restParamArr.add(headObj);
                                        }
                                    }
                                } catch (Exception e) {
                                    LogUtil.error(e.getMessage());
                                }
                            }
                            apiInfoDTO.setUrlParams(urlParamArr.toJSONString());
                            apiInfoDTO.setRestParams(restParamArr.toJSONString());
                            //请求体参数类型
                            if (requestObj.containsKey("body")) {
                                try {
                                    JSONObject bodyObj = requestObj.getJSONObject("body");
                                    if (this.isObjectHasKey(bodyObj, "type")) {
                                        String type = bodyObj.getString("type");
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
                                            if (bodyObj.containsKey("format")) {
                                                String foramtValue = String.valueOf(bodyObj.get("format"));
                                                if (StringUtils.equals("JSON-SCHEMA", foramtValue)) {
                                                    isJsonSchema = true;
                                                }
                                            }
                                            JSONSchemaBodyDTO jsonSchemaBodyDTO = new JSONSchemaBodyDTO();
                                            if (isJsonSchema) {
                                                jsonSchemaBodyDTO.setJsonSchema(bodyObj.get("jsonSchema"));
                                                apiInfoDTO.setJsonSchemaBody(jsonSchemaBodyDTO);
                                                String raw = JSONSchemaGenerator.getJson(JSONObject.toJSONString(bodyObj.get("jsonSchema")));
                                                this.setPreviewData(previewJsonArray, raw);
                                            } else {
                                                if (bodyObj.containsKey("raw")) {
                                                    String raw = bodyObj.getString("raw");
                                                    jsonSchemaBodyDTO.setRaw(raw);
                                                    apiInfoDTO.setJsonSchemaBody(jsonSchemaBodyDTO);
                                                    apiInfoDTO.setRequestBodyStrutureData(raw);
                                                    //转化jsonObje 或者 jsonArray
                                                    this.setPreviewData(previewJsonArray, raw);
                                                }
                                            }
                                        } else if (StringUtils.equalsAny(type, "XML", "Raw")) {
                                            if (bodyObj.containsKey("raw")) {
                                                String raw = bodyObj.getString("raw");
                                                apiInfoDTO.setRequestBodyStrutureData(raw);
                                                this.setPreviewData(previewJsonArray, raw);
                                            }
                                        } else if (StringUtils.equalsAny(type, "Form Data", "WWW_FORM")) {
                                            if (bodyObj.containsKey("kvs")) {
                                                JSONArray bodyParamArr = new JSONArray();
                                                JSONArray kvsArr = bodyObj.getJSONArray("kvs");
                                                Map<String, String> previewObjMap = new LinkedHashMap<>();
                                                for (int i = 0; i < kvsArr.size(); i++) {
                                                    JSONObject kv = kvsArr.getJSONObject(i);
                                                    if (this.isObjectHasKey(kv, "name")) {
                                                        String value = "";
                                                        if (kv.containsKey("value")) {
                                                            value = String.valueOf(kv.get("value"));
                                                        }
                                                        bodyParamArr.add(kv);
                                                        previewObjMap.put(String.valueOf(kv.get("name")), value);
                                                    }
                                                }
                                                this.setPreviewData(previewJsonArray, JSONObject.toJSONString(previewObjMap));
                                                apiInfoDTO.setRequestBodyFormData(bodyParamArr.toJSONString());
                                            }
                                        } else if (StringUtils.equals(type, "BINARY")) {
                                            if (bodyObj.containsKey("binary")) {
                                                List<Map<String, String>> bodyParamList = new ArrayList<>();
                                                JSONArray kvsArr = bodyObj.getJSONArray("binary");

                                                Map<String, String> previewObjMap = new LinkedHashMap<>();
                                                for (int i = 0; i < kvsArr.size(); i++) {
                                                    JSONObject kv = kvsArr.getJSONObject(i);
                                                    if (this.isObjectHasKey(kv, "description") && this.isObjectHasKey(kv, "files")) {
                                                        Map<String, String> bodyMap = new HashMap<>();
                                                        String name = kv.getString("description");
                                                        JSONArray fileArr = kv.getJSONArray("files");
                                                        String value = "";
                                                        for (int j = 0; j < fileArr.size(); j++) {
                                                            JSONObject fileObj = fileArr.getJSONObject(j);
                                                            if (this.isObjectHasKey(fileObj, "name")) {
                                                                value += fileObj.getString("name") + " ;";
                                                            }
                                                        }
                                                        bodyMap.put("name", name);
                                                        bodyMap.put("value", value);
                                                        bodyMap.put("contentType", "File");
                                                        bodyParamList.add(bodyMap);

                                                        previewObjMap.put(String.valueOf(name), String.valueOf(value));

                                                    }
                                                }
                                                this.setPreviewData(previewJsonArray, JSONObject.toJSONString(previewObjMap));
                                                apiInfoDTO.setRequestBodyFormData(JSONArray.toJSONString(bodyParamList));
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
                        JSONObject responseJsonObj = this.genJSONObject(apiModel.getResponse());
                        if (this.isObjectHasKey(responseJsonObj, "headers")) {
                            try {
                                JSONArray responseHeadDataArr = new JSONArray();
                                JSONArray headArr = responseJsonObj.getJSONArray("headers");
                                for (int index = 0; index < headArr.size(); index++) {
                                    JSONObject headObj = headArr.getJSONObject(index);
                                    if (this.isObjectHasKey(headObj, "name")) {
                                        responseHeadDataArr.add(headObj);
                                    }
                                }
                                apiInfoDTO.setResponseHead(responseHeadDataArr.toJSONString());
                            } catch (Exception e) {
                                LogUtil.error(e.getMessage());
                            }
                        }
                        // 赋值响应体
                        if (this.isObjectHasKey(responseJsonObj, "body")) {
                            try {
                                JSONObject bodyObj = responseJsonObj.getJSONObject("body");
                                if (this.isObjectHasKey(bodyObj, "type")) {
                                    String type = bodyObj.getString("type");
                                    if (StringUtils.equals(type, "WWW_FORM")) {
                                        apiInfoDTO.setResponseBodyParamType("x-www-from-urlencoded");
                                    } else if (StringUtils.equals(type, "Form Data")) {
                                        apiInfoDTO.setResponseBodyParamType("form-data");
                                    } else {
                                        apiInfoDTO.setResponseBodyParamType(type);
                                    }
                                    if (StringUtils.equalsAny(type, "JSON", "XML", "Raw")) {

                                        //判断是否是JsonSchema
                                        boolean isJsonSchema = false;
                                        if (bodyObj.containsKey("format")) {
                                            String foramtValue = String.valueOf(bodyObj.get("format"));
                                            if (StringUtils.equals("JSON-SCHEMA", foramtValue)) {
                                                isJsonSchema = true;
                                            }
                                        }
                                        if (isJsonSchema) {
                                            apiInfoDTO.setResponseBodyParamType("JSON-SCHEMA");
                                            apiInfoDTO.setJsonSchemaResponseBody(bodyObj);
                                        } else {
                                            if (bodyObj.containsKey("raw")) {
                                                String raw = bodyObj.getString("raw");
                                                apiInfoDTO.setResponseBodyStrutureData(raw);
                                            }
                                        }
                                    } else if (StringUtils.equalsAny(type, "Form Data", "WWW_FORM")) {
                                        if (bodyObj.containsKey("kvs")) {
                                            JSONArray bodyParamArr = new JSONArray();
                                            JSONArray kvsArr = bodyObj.getJSONArray("kvs");
                                            for (int i = 0; i < kvsArr.size(); i++) {
                                                JSONObject kv = kvsArr.getJSONObject(i);
                                                if (this.isObjectHasKey(kv, "name")) {
                                                    bodyParamArr.add(kv);
                                                }
                                            }
                                            apiInfoDTO.setResponseBodyFormData(bodyParamArr.toJSONString());
                                        }
                                    } else if (StringUtils.equals(type, "BINARY")) {
                                        if (bodyObj.containsKey("binary")) {
                                            List<Map<String, String>> bodyParamList = new ArrayList<>();
                                            JSONArray kvsArr = bodyObj.getJSONArray("kvs");
                                            for (int i = 0; i < kvsArr.size(); i++) {
                                                JSONObject kv = kvsArr.getJSONObject(i);
                                                if (kv.containsKey("description") && kv.containsKey("files")) {
                                                    Map<String, String> bodyMap = new HashMap<>();

                                                    String name = kv.getString("description");
                                                    JSONArray fileArr = kv.getJSONArray("files");
                                                    String value = "";
                                                    for (int j = 0; j < fileArr.size(); j++) {
                                                        JSONObject fileObj = fileArr.getJSONObject(j);
                                                        if (this.isObjectHasKey(fileObj, "name")) {
                                                            value += fileObj.getString("name") + " ;";
                                                        }
                                                    }
                                                    bodyMap.put("name", name);
                                                    bodyMap.put("value", value);
                                                    bodyParamList.add(bodyMap);
                                                }
                                            }
                                            apiInfoDTO.setResponseBodyFormData(JSONArray.toJSONString(bodyParamList));
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
                                JSONArray responseStatusDataArr = new JSONArray();
                                JSONArray statusArr = responseJsonObj.getJSONArray("statusCode");
                                for (int index = 0; index < statusArr.size(); index++) {
                                    JSONObject statusObj = statusArr.getJSONObject(index);
                                    if (this.isObjectHasKey(statusObj, "name")) {
                                        responseStatusDataArr.add(statusObj);
                                    }
                                }
                                apiInfoDTO.setResponseCode(responseStatusDataArr.toJSONString());
                            } catch (Exception e) {
                                LogUtil.error(e.getMessage());
                            }
                        }
                    }
                    apiInfoDTO.setRemark(apiModel.getRemark());
                }
                LogUtil.info(apiModel.getName() + ":end and preview");
                if (!previewJsonArray.isEmpty()) {
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

    private JSONObject genJSONObject(String request) {
        JSONObject returnObj = null;
        try {
            returnObj = JSONObject.parseObject(request, Feature.DisableCircularReferenceDetect);
        } catch (Exception e) {
            LogUtil.error(e.getMessage());
        }
        return returnObj;
    }

    private JSONArray setPreviewData(JSONArray previewArray, String data) {

        try {
            if (StringUtils.startsWith(data, "{") && StringUtils.endsWith(data, "}")) {
                JSONObject previewObj = JSONObject.parseObject(data, Feature.DisableCircularReferenceDetect);
                previewArray.add(previewObj);
            } else {
                previewArray = JSONArray.parseArray(data);
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
     * 生成分享连接
     * 如果该数据有连接则，返回已有的连接，不做有效期判断
     *
     * @param request
     * @return
     */
    public ShareInfo generateShareInfo(ShareInfo request) {
        List<ShareInfo> shareInfos = extShareInfoMapper.selectByShareTypeAndShareApiIdWithBLOBs(request.getShareType(), request.getCustomData());
        if (shareInfos.isEmpty()) {
            return createShareInfo(request);
        } else {
            return shareInfos.get(0);
        }
    }

    public ShareInfo createShareInfo(ShareInfo shareInfo) {
        long createTime = System.currentTimeMillis();
        shareInfo.setId(UUID.randomUUID().toString());
        shareInfo.setCreateTime(createTime);
        shareInfo.setUpdateTime(createTime);
        shareInfoMapper.insert(shareInfo);
        return shareInfo;
    }

    /**
     * 根据treeSet排序生成apiId的Jsonarray字符串
     *
     * @param shareApiIdList 要分享的ID集合
     * @return 要分享的ID JSON格式字符串
     */
    private String genShareIdJsonString(Collection<String> shareApiIdList) {
        TreeSet<String> treeSet = new TreeSet<>(shareApiIdList);
        return JSONArray.toJSONString(treeSet);
    }

    public ShareInfoDTO conversionShareInfoToDTO(ShareInfo apiShare) {
        ShareInfoDTO returnDTO = new ShareInfoDTO();
        if (!StringUtils.isEmpty(apiShare.getCustomData())) {
            String url = "?shareId=" + apiShare.getId();
            returnDTO.setId(apiShare.getId());
            returnDTO.setShareUrl(url);
        }
        return returnDTO;
    }

    public String getTestPlanShareUrl(String testPlanReportId, String userId) {
        ShareInfo shareRequest = new ShareInfo();
        shareRequest.setCustomData(testPlanReportId);
        shareRequest.setShareType(ShareType.PLAN_DB_REPORT.name());
        shareRequest.setCreateUserId(userId);
        ShareInfo shareInfo = generateShareInfo(shareRequest);
        return conversionShareInfoToDTO(shareInfo).getShareUrl();
    }

    public ShareInfo get(String id) {
        return shareInfoMapper.selectByPrimaryKey(id);
    }

    public void validate(String shareId, String customData) {
        ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(shareId);
        ShareInfoService shareInfoService = CommonBeanFactory.getBean(ShareInfoService.class);
        shareInfoService.validateExpired(shareInfo);
        if (shareInfo == null) {
            MSException.throwException("ShareInfo not exist!");
        } else {
            if (!StringUtils.equals(customData, shareInfo.getCustomData())) {
                MSException.throwException("ShareInfo validate failure!");
            }
        }
    }

    public void validateExpired(String shareId) {
        ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(shareId);
        ShareInfoService shareInfoService = CommonBeanFactory.getBean(ShareInfoService.class);
        shareInfoService.validateExpired(shareInfo);
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
        String type = "";
        String projectId = "";
        if (shareInfo.getShareType().equals("PERFORMANCE_REPORT")) {
            type = ProjectApplicationType.PERFORMANCE_SHARE_REPORT_TIME.toString();
            LoadTestReportWithBLOBs loadTestReportWithBLOBs = loadTestReportMapper.selectByPrimaryKey(shareInfo.getCustomData());
            if (loadTestReportWithBLOBs != null) {
                projectId = loadTestReportWithBLOBs.getProjectId();
            }
        }
        if (shareInfo.getShareType().equals("PLAN_DB_REPORT")) {
            type = ProjectApplicationType.TRACK_SHARE_REPORT_TIME.toString();
            TestPlanWithBLOBs testPlan = getTestPlan(shareInfo);
            if (testPlan != null) {
                projectId = testPlan.getProjectId();
            }
        }
        if (shareInfo.getShareType().equals("API_REPORT")) {
            type = ProjectApplicationType.API_SHARE_REPORT_TIME.toString();
            APIScenarioReportResult reportResult = extApiScenarioReportMapper.get(shareInfo.getCustomData());
            if (reportResult != null) {
                projectId = reportResult.getProjectId();
            } else {
                // case 集成报告
                APIScenarioReportResult result = apiScenarioReportService.getApiIntegrated(shareInfo.getCustomData());
                if (result != null) {
                    projectId = result.getProjectId();
                }
            }
        }

        if (StringUtils.equals(shareInfo.getShareType(), "UI_REPORT")) {
            type = ProjectApplicationType.UI_SHARE_REPORT_TIME.toString();
            APIScenarioReportResult reportResult = extApiScenarioReportMapper.get(shareInfo.getCustomData());
            if (reportResult != null) {
                projectId = reportResult.getProjectId();
            }
        }

        if (StringUtils.isBlank(type) || Strings.isBlank(projectId)) {
            millisCheck(System.currentTimeMillis() - shareInfo.getUpdateTime(), 1000 * 60 * 60 * 24, shareInfo.getId());
        } else {
            ProjectApplication projectApplication = projectApplicationService.getProjectApplication(projectId, type);
            if (projectApplication.getTypeValue() == null) {
                millisCheck(System.currentTimeMillis() - shareInfo.getUpdateTime(), 1000 * 60 * 60 * 24, shareInfo.getId());
            } else {
                String expr = projectApplication.getTypeValue();
                long timeMills = getTimeMills(shareInfo.getUpdateTime(), expr);
                millisCheck(System.currentTimeMillis(), timeMills, shareInfo.getId());
            }
        }
    }

    private TestPlanWithBLOBs getTestPlan(ShareInfo shareInfo) {
        TestPlanReportContentExample example = new TestPlanReportContentExample();
        example.createCriteria().andTestPlanReportIdEqualTo(shareInfo.getCustomData());
        List<TestPlanReportContentWithBLOBs> testPlanReportContents = testPlanReportContentMapper.selectByExampleWithBLOBs(example);
        if (!CollectionUtils.isEmpty(testPlanReportContents)) {
            TestPlanReportContentWithBLOBs testPlanReportContent = testPlanReportContents.get(0);
            if (testPlanReportContent != null) {
                TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(testPlanReportContent.getTestPlanReportId());
                if (testPlanReport != null) {
                    return testPlanMapper.selectByPrimaryKey(testPlanReport.getTestPlanId());
                }

            }
        }
        return null;
    }

    private void millisCheck(long compareMillis, long millis, String shareInfoId) {
        if (compareMillis > millis) {
            shareInfoMapper.deleteByPrimaryKey(shareInfoId);
            MSException.throwException(Translator.get("connection_expired"));
        }
    }

    public String getPlanId(String shareId) {
        ShareInfo shareInfo = shareInfoMapper.selectByPrimaryKey(shareId);
        String planId = shareInfo.getCustomData();
        if (ShareType.PLAN_DB_REPORT.name().equals(shareInfo.getShareType())) {
            String reportId = shareInfo.getCustomData();
            TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(reportId);
            planId = testPlanReport.getTestPlanId();
        }
        return planId;
    }

    private boolean isObjectHasKey(JSONObject object, String key) {
        return object != null && object.containsKey(key);
    }
}
