package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.service.ProjectApplicationService;
import io.metersphere.track.service.TestPlanApiCaseService;
import io.metersphere.track.service.TestPlanScenarioCaseService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

import static io.metersphere.api.service.utils.ShareUtill.getTimeMills;

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
    TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    TestPlanScenarioCaseService testPlanScenarioCaseService;
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


    public List<ApiDocumentInfoDTO> findApiDocumentSimpleInfoByRequest(ApiDocumentRequest request) {
        if (this.isParamLegitimacy(request)) {
            if (request.getProjectId() == null) {
                List<String> shareIdList = this.selectShareIdByShareInfoId(request.getShareId());
                request.setApiIdList(shareIdList);
                if (shareIdList.isEmpty()) {
                    return new ArrayList<>();
                } else {
                    return extShareInfoMapper.findApiDocumentSimpleInfoByRequest(request);
                }
            } else {
                return extShareInfoMapper.findApiDocumentSimpleInfoByRequest(request);
            }
        } else {
            return new ArrayList<>();
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

    public ApiDocumentInfoDTO conversionModelToDTO(ApiDefinitionWithBLOBs apiModel, Map<String,User> userIdMap) {
        ApiDocumentInfoDTO apiInfoDTO = new ApiDocumentInfoDTO();
        JSONArray previewJsonArray = new JSONArray();
        if (apiModel != null) {
            apiInfoDTO.setId(apiModel.getId());
            apiInfoDTO.setName(apiModel.getName());
            apiInfoDTO.setMethod(apiModel.getMethod());
            apiInfoDTO.setUri(apiModel.getPath());
            apiInfoDTO.setStatus(apiModel.getStatus());

            if(StringUtils.isNotEmpty(apiModel.getTags())){
                JSONArray tagsArr = JSONArray.parseArray(apiModel.getTags());
                List<String> tagList = new ArrayList<>();
                for(int i = 0;i < tagsArr.size();i ++){
                    tagList.add(tagsArr.getString(i));
                }
                if(!tagList.isEmpty()){
                    apiInfoDTO.setTags(StringUtils.join(tagList,","));
                }
            }

            apiInfoDTO.setResponsibler(userIdMap.get(apiModel.getUserId()) == null? apiModel.getUserId() : userIdMap.get(apiModel.getUserId()).getName());
            apiInfoDTO.setCreateUser(userIdMap.get(apiModel.getCreateUser()) == null? apiModel.getCreateUser() : userIdMap.get(apiModel.getCreateUser()).getName());
            apiInfoDTO.setDesc(apiModel.getDescription());
            ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
            apiInfoDTO.setModules(apiModuleService.getModuleNameById(apiModel.getModuleId()));

            if (apiModel.getRequest() != null) {
                JSONObject requestObj = this.genJSONObject(apiModel.getRequest());
                if (requestObj != null) {
                    if (requestObj.containsKey("headers")) {
                        JSONArray requestHeadDataArr = new JSONArray();
                        //head赋值
                        JSONArray headArr = requestObj.getJSONArray("headers");
                        for (int index = 0; index < headArr.size(); index++) {
                            JSONObject headObj = headArr.getJSONObject(index);
                            if (headObj != null && headObj.containsKey("name") && headObj.containsKey("value")) {
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
                                if (headObj.containsKey("name") && headObj.containsKey("value")) {
                                    urlParamArr.add(headObj);
                                }
                            }
                        } catch (Exception e) {
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
                                if (headObj.containsKey("name")) {
                                    restParamArr.add(headObj);
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    apiInfoDTO.setUrlParams(urlParamArr.toJSONString());
                    apiInfoDTO.setRestParams(restParamArr.toJSONString());
                    //请求体参数类型
                    if (requestObj.containsKey("body")) {
                        try {
                            JSONObject bodyObj = requestObj.getJSONObject("body");
                            if (bodyObj.containsKey("type")) {
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
                                    if (isJsonSchema) {
                                        apiInfoDTO.setRequestBodyParamType("JSON-SCHEMA");
                                        apiInfoDTO.setJsonSchemaBody(bodyObj);
                                        if (bodyObj.containsKey("jsonSchema")) {
                                            JSONObject jsonSchemaObj = bodyObj.getJSONObject("jsonSchema");
                                            apiInfoDTO.setRequestPreviewData(JSON.parse(JSONSchemaGenerator.getJson(jsonSchemaObj.toJSONString())));
                                        }
                                    } else {
                                        if (bodyObj.containsKey("raw")) {
                                            String raw = bodyObj.getString("raw");
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
                                            if (kv.containsKey("name")) {
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
                                            if (kv.containsKey("description") && kv.containsKey("files")) {
                                                Map<String, String> bodyMap = new HashMap<>();
                                                String name = kv.getString("description");
                                                JSONArray fileArr = kv.getJSONArray("files");
                                                String value = "";
                                                for (int j = 0; j < fileArr.size(); j++) {
                                                    JSONObject fileObj = fileArr.getJSONObject(j);
                                                    if (fileObj.containsKey("name")) {
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

                        }

                    }
                }
            }

            //赋值响应头
            if (apiModel.getResponse() != null) {
                JSONObject responseJsonObj = this.genJSONObject(apiModel.getResponse());
                if (responseJsonObj != null && responseJsonObj.containsKey("headers")) {
                    try {
                        JSONArray responseHeadDataArr = new JSONArray();
                        JSONArray headArr = responseJsonObj.getJSONArray("headers");
                        for (int index = 0; index < headArr.size(); index++) {
                            JSONObject headObj = headArr.getJSONObject(index);
                            if (headObj.containsKey("name") && headObj.containsKey("value")) {
                                responseHeadDataArr.add(headObj);
                            }
                        }
                        apiInfoDTO.setResponseHead(responseHeadDataArr.toJSONString());
                    } catch (Exception e) {

                    }
                }
                // 赋值响应体
                if (responseJsonObj != null && responseJsonObj.containsKey("body")) {
                    try {
                        JSONObject bodyObj = responseJsonObj.getJSONObject("body");
                        if (bodyObj.containsKey("type")) {
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
//                                    apiInfoDTO.setRequestBodyParamType("JSON-SCHEMA");
                                    apiInfoDTO.setResponseBodyParamType("JSON-SCHEMA");
                                    apiInfoDTO.setJsonSchemaResponseBody(bodyObj);
//                                    apiInfoDTO.setJsonSchemaBody(bodyObj);
                                } else {
                                    if (bodyObj.containsKey("raw")) {
                                        String raw = bodyObj.getString("raw");
                                        apiInfoDTO.setResponseBodyStrutureData(raw);
                                        //转化jsonObje 或者 jsonArray
                                        this.setPreviewData(previewJsonArray, raw);
                                    }
                                }
//                                if (bodyObj.containsKey("raw")) {
//                                    String raw = bodyObj.getString("raw");
//                                    apiInfoDTO.setResponseBodyStrutureData(raw);
//                                }
                            } else if (StringUtils.equalsAny(type, "Form Data", "WWW_FORM")) {
                                if (bodyObj.containsKey("kvs")) {
                                    JSONArray bodyParamArr = new JSONArray();
                                    JSONArray kvsArr = bodyObj.getJSONArray("kvs");
                                    for (int i = 0; i < kvsArr.size(); i++) {
                                        JSONObject kv = kvsArr.getJSONObject(i);
                                        if (kv.containsKey("name")) {
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
                                                if (fileObj.containsKey("name")) {
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

                    }

                }
                // 赋值响应码
                if (responseJsonObj != null && responseJsonObj.containsKey("statusCode")) {
                    try {
                        JSONArray responseStatusDataArr = new JSONArray();
                        JSONArray statusArr = responseJsonObj.getJSONArray("statusCode");
                        for (int index = 0; index < statusArr.size(); index++) {
                            JSONObject statusObj = statusArr.getJSONObject(index);
                            if (statusObj.containsKey("name") && statusObj.containsKey("value")) {
                                responseStatusDataArr.add(statusObj);
                            }
                        }
                        apiInfoDTO.setResponseCode(responseStatusDataArr.toJSONString());
                    } catch (Exception e) {

                    }
                }
            }
        }
        if (!previewJsonArray.isEmpty()) {
            apiInfoDTO.setRequestPreviewData(previewJsonArray);
        }
        apiInfoDTO.setSelectedFlag(true);
        return apiInfoDTO;
    }

    private JSONObject genJSONObject(String request) {
        JSONObject returnObj = null;
        try {
            returnObj = JSONObject.parseObject(request);
        } catch (Exception e) {
        }
        return returnObj;
    }

    private void setPreviewData(JSONArray previewArray, String data) {
        try {
            JSONObject previewObj = JSONObject.parseObject(data);
            previewArray.add(previewObj);
        } catch (Exception e) {
        }
        try {
            previewArray = JSONArray.parseArray(data);
        } catch (Exception e) {
        }
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
        if (request.getShareApiIdList() != null && !request.getShareApiIdList().isEmpty()
                && StringUtils.equalsAny(request.getShareType(), ShareInfoType.Single.name(), ShareInfoType.Batch.name())) {
            //将ID进行排序
            ShareInfo shareInfoRequest = new ShareInfo();
            BeanUtils.copyBean(shareInfoRequest, request);
            shareInfoRequest.setCustomData(genShareIdJsonString(request.getShareApiIdList()));
            return generateShareInfo(shareInfoRequest);
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

    public ShareInfo createShareInfo(ShareInfo request) {
        long createTime = System.currentTimeMillis();
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setId(UUID.randomUUID().toString());
        shareInfo.setCustomData(request.getCustomData());
        shareInfo.setCreateUserId(request.getCreateUserId());
        shareInfo.setCreateTime(createTime);
        shareInfo.setUpdateTime(createTime);
        shareInfo.setShareType(request.getShareType());
        shareInfoMapper.insert(shareInfo);
        return shareInfo;
    }

    private List<ShareInfo> findByShareTypeAndShareApiIdWithBLOBs(String shareType, List<String> shareApiIdList) {
        String shareApiIdString = this.genShareIdJsonString(shareApiIdList);
        return extShareInfoMapper.selectByShareTypeAndShareApiIdWithBLOBs(shareType, shareApiIdString);
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
            MSException.throwException("shareInfo not exist!");
        } else {
            if (!StringUtils.equals(customData, shareInfo.getCustomData())) {
                MSException.throwException("validate failure!");
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
        if(shareInfo == null ){
            MSException.throwException(Translator.get("connection_expired"));
        }
        String type = "";
        String projectId="";
        if(shareInfo.getShareType().equals("PERFORMANCE_REPORT")){
            type = ProjectApplicationType.PERFORMANCE_SHARE_REPORT_TIME.toString();
            LoadTestReportWithBLOBs loadTestReportWithBLOBs = loadTestReportMapper.selectByPrimaryKey(shareInfo.getCustomData());
            if(loadTestReportWithBLOBs!=null){
                projectId = loadTestReportWithBLOBs.getProjectId();
            }
        }
        if(shareInfo.getShareType().equals("PLAN_DB_REPORT")){
            type = ProjectApplicationType.TRACK_SHARE_REPORT_TIME.toString();
            TestPlanWithBLOBs testPlan = getTestPlan(shareInfo);
            if (testPlan != null){
                projectId = testPlan.getProjectId();
            };

        }
        if(shareInfo.getShareType().equals("API_REPORT")){
            type = ProjectApplicationType.API_SHARE_REPORT_TIME.toString();
            APIScenarioReportResult reportResult = extApiScenarioReportMapper.get(shareInfo.getCustomData());
            if (reportResult != null){
                projectId = reportResult.getProjectId();
            };

        }
        if(StringUtils.isBlank(type)|| Strings.isBlank(projectId)){
            millisCheck(System.currentTimeMillis() - shareInfo.getUpdateTime() ,1000 * 60 * 60 * 24,shareInfo.getId());
        }else{
            ProjectApplication projectApplication = projectApplicationService.getProjectApplication(projectId,type);
            if(projectApplication.getTypeValue()==null){
                millisCheck(System.currentTimeMillis() - shareInfo.getUpdateTime() ,1000 * 60 * 60 * 24,shareInfo.getId());
            }else {
                String expr= projectApplication.getTypeValue();
                long timeMills = getTimeMills(shareInfo.getUpdateTime(),expr);
                millisCheck(System.currentTimeMillis(),timeMills,shareInfo.getId());
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
                if(testPlanReport!=null){
                    return testPlanMapper.selectByPrimaryKey(testPlanReport.getTestPlanId());
                }

            }
        }
        return null;
    }

    private void millisCheck(long compareMillis, long millis,String shareInfoId) {
        if (compareMillis>millis) {
            shareInfoMapper.deleteByPrimaryKey(shareInfoId);
            MSException.throwException(Translator.get("connection_expired"));
        }
    }

    public void apiReportValidate(String shareId, String testId) {
        TestPlanApiCase testPlanApiCase = testPlanApiCaseService.getById(testId);
        if (!StringUtils.equals(getPlanId(shareId), testPlanApiCase.getTestPlanId())) {
            MSException.throwException("validate failure!");
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

    public void scenarioReportValidate(String shareId, String reportId) {
        TestPlanApiScenario testPlanApiScenario = testPlanScenarioCaseService.selectByReportId(reportId);
        if (!StringUtils.equals(getPlanId(shareId), testPlanApiScenario.getTestPlanId())) {
            MSException.throwException("validate failure!");
        }
    }
}
