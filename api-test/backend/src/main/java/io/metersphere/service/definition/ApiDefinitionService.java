package io.metersphere.service.definition;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.github.ningyu.jmeter.plugin.dubbo.sample.ProviderService;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.automation.TcpTreeTableDataStruct;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.request.assertions.document.DocumentElement;
import io.metersphere.api.dto.definition.request.auth.MsAuthManager;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.swaggerurl.SwaggerTaskResult;
import io.metersphere.api.dto.swaggerurl.SwaggerUrlRequest;
import io.metersphere.api.exec.api.ApiExecuteService;
import io.metersphere.api.exec.generator.JSONSchemaParser;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.parse.ApiImportParser;
import io.metersphere.api.parse.api.ApiDefinitionImport;
import io.metersphere.api.parse.api.ApiDefinitionImportParserFactory;
import io.metersphere.api.parse.api.Swagger3Parser;
import io.metersphere.api.parse.scenario.TcpTreeTableDataParser;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.enums.StorageEnums;
import io.metersphere.commons.enums.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.*;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.environment.service.BaseEnvironmentService;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.StatusReference;
import io.metersphere.log.vo.api.DefinitionReference;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.quota.service.BaseQuotaService;
import io.metersphere.request.OrderRequest;
import io.metersphere.request.RelationshipEdgeRequest;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.request.SyncApiDefinitionRequest;
import io.metersphere.sechedule.SwaggerUrlImportJob;
import io.metersphere.service.*;
import io.metersphere.service.ext.ExtApiScheduleService;
import io.metersphere.service.ext.ExtFileAssociationService;
import io.metersphere.service.plan.TestPlanApiCaseService;
import io.metersphere.service.scenario.ApiScenarioService;
import io.metersphere.xpack.api.service.ApiCaseBatchSyncService;
import io.metersphere.xpack.api.service.ApiDefinitionSyncService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.URL;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.json.JSONObject;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionService {
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ExtApiScheduleService scheduleService;
    @Resource
    private SwaggerUrlProjectMapper swaggerUrlProjectMapper;
    @Resource
    private ExtSwaggerUrlScheduleMapper extSwaggerUrlScheduleMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private TcpApiParamService tcpApiParamService;
    @Resource
    private ApiModuleMapper apiModuleMapper;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private MockConfigMapper mockConfigMapper;
    @Resource
    private MockExpectConfigMapper mockExpectConfigMapper;
    @Resource
    private RelationshipEdgeService relationshipEdgeService;
    @Resource
    private ApiDefinitionFollowMapper apiDefinitionFollowMapper;
    @Resource
    private BaseProjectVersionMapper baseProjectVersionMapper;
    @Resource
    private BaseProjectApplicationService projectApplicationService;
    @Resource
    private BaseProjectService baseProjectService;
    @Resource
    private ApiExecutionInfoService apiExecutionInfoService;
    @Lazy
    @Resource
    private ApiModuleService apiModuleService;
    @Lazy
    @Resource
    private MockConfigService mockConfigService;
    @Resource
    private BaseEnvironmentService apiTestEnvironmentService;
    @Lazy
    @Resource
    private ApiScenarioService apiAutomationService;
    @Resource
    private ExtFileAssociationService extFileAssociationService;
    @Resource
    private ApiScenarioReferenceIdMapper apiScenarioReferenceIdMapper;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ApiCustomFieldService customFieldApiService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private BaseTestResourcePoolService baseTestResourcePoolService;

    @Resource
    private ApiDefinitionImportUtilService apiDefinitionImportUtilService;
    @Resource
    private BaseQuotaService baseQuotaService;
    @Resource
    private BaseEnvGroupProjectService environmentGroupProjectService;
    @Resource
    private ApiCaseResultService apiCaseResultService;


    private static final String COPY = "Copy";

    public static final String HEADERS = "headers";
    public static final String ARGUMENTS = "arguments";
    public static final String BODY = "body";
    private static final String SCHEDULE = "schedule";
    public static final String TYPE = "type";
    public static final String HTTP = "HTTPSamplerProxy";
    public static final String CLAZZ = "className";
    public static final String FORMAT = "format";
    public static final String RAW = "raw";
    public static final String JSONSCHEMA = "jsonSchema";


    public List<ApiDefinitionResult> list(ApiDefinitionRequest request) {
        request = this.initRequest(request, true, true);
        ServiceUtils.setBaseQueryRequestCustomMultipleFields(request);
        List<ApiDefinitionResult> resList = extApiDefinitionMapper.list(request);
        buildUserInfo(resList);
        if (StringUtils.isNotBlank(request.getProjectId())) {
            buildProjectInfo(resList, request.getProjectId());
            calculateResult(resList, request.getProjectId());
        } else {
            buildProjectInfoWithoutProject(resList);
        }
        buildCustomField(resList);
        return resList;
    }

    private void setCustomFieldsOrder(ApiDefinitionRequest request) {
        if (request.getCombine() != null && !request.getCombine().isEmpty()) {
            request.setIsCustomSorted(true);
        }
    }

    /**
     * 工作台获取待应用管理设置的更新的条件
     *
     * @param request
     */
    public void getApplicationUpdateRule(ApiDefinitionRequest request) {
        // 来自工作台条件
        if (BooleanUtils.isTrue(request.getToBeUpdated())) {
            Long toBeUpdatedTime = apiTestCaseService.getToBeUpdatedTime(request.getProjectId());
            if (toBeUpdatedTime != null) {
                request.setToBeUpdateTime(toBeUpdatedTime);
            }
        }
    }

    public List<ApiDefinition> selectByIds(ApiDefinitionRequest request) {
        if (request != null) {
            return selectByIds(request.getIds());
        }
        return new ArrayList<>();
    }

    public List<ApiDefinition> selectByIds(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(ids);
            List<ApiDefinition> list = apiDefinitionMapper.selectByExample(example);
            return list;
        }
        return new ArrayList<>();
    }

    private void buildCustomField(List<ApiDefinitionResult> data) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        Map<String, List<CustomFieldDao>> fieldMap =
                customFieldApiService.getMapByResourceIds(data.stream().map(ApiDefinitionResult::getId).collect(Collectors.toList()));
        data.forEach(i -> i.setFields(fieldMap.get(i.getId())));
    }

    private void buildProjectInfoWithoutProject(List<ApiDefinitionResult> resList) {
        resList.forEach(i -> {
            Project project = projectMapper.selectByPrimaryKey(i.getProjectId());
            if (project == null) {
                i.setProjectName(StringUtils.EMPTY);
                i.setVersionEnable(false);
            } else {
                i.setProjectName(project.getName());
                i.setVersionEnable(project.getVersionEnable());
            }
        });
    }

    public void buildUserInfo(List<ApiDefinitionResult> apis) {
        if (CollectionUtils.isEmpty(apis)) {
            return;
        }
        Set<String> userIds = new HashSet<>();
        apis.forEach(i -> {
            userIds.add(i.getUserId());
            userIds.add(i.getDeleteUserId());
            userIds.add(i.getCreateUser());
        });
        if (!CollectionUtils.isEmpty(userIds)) {
            Map<String, String> userMap = ServiceUtils.getUserNameMap(new ArrayList<>(userIds));
            apis.forEach(caseResult -> {
                caseResult.setCreateUser(userMap.get(caseResult.getCreateUser()));
                caseResult.setDeleteUser(userMap.get(caseResult.getDeleteUserId()));
                caseResult.setUserName(userMap.get(caseResult.getUserId()));
            });
        }
    }

    public void buildProjectInfo(List<ApiDefinitionResult> apis, String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        apis.forEach(i -> {
            i.setProjectName(project.getName());
            i.setVersionEnable(project.getVersionEnable());
        });
    }

    public List<ApiDefinitionResult> weekList(String projectId, String versionId) {
        //获取7天之前的日期
        Date startDay = DateUtils.dateSum(new Date(), -6);
        //将日期转化为 00:00:00 的时间戳
        Date startTime = null;
        try {
            startTime = DateUtils.getDayStartTime(startDay);
        } catch (Exception e) {
            LogUtil.error("获取日期出错", e);
        }
        if (startTime == null) {
            return new ArrayList<>(0);
        } else {
            ApiDefinitionRequest request = new ApiDefinitionRequest();
            request.setOrders(
                    new ArrayList<>() {{
                        add(new OrderRequest() {{
                                this.setName("update_time");
                                this.setType("desc");
                            }}
                        );
                    }}
            );
            request.setProjectId(projectId);
            request = this.initRequest(request, false, true);
            request.setNotEqStatus(ApiTestDataStatus.TRASH.getValue());
            if (StringUtils.isNotBlank(versionId)) {
                request.setVersionId(versionId);
            }
            List<ApiDefinitionResult> resList = extApiDefinitionMapper.weekList(request, startTime.getTime());
            calculateResult(resList, request.getProjectId());
            calculateRelationScenario(resList);
            resList.forEach(item -> item.setApiType("api"));
            return resList;
        }
    }


    public List<ApiDefinitionResult> listBatch(ApiBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiDefinitionMapper.selectIds(query));
        if (CollectionUtils.isEmpty(request.getIds())) {
            return new ArrayList<>();
        }
        List<ApiDefinitionResult> resList = extApiDefinitionMapper.listByIds(request.getIds());
        resList.forEach(item -> {
            JSONObject jsonObject = JSONUtil.parseObject(item.getRequest());
            if (jsonObject != null && jsonObject.has(TYPE) && jsonObject.optString(TYPE).equals(HTTP)) {
                jsonObject.put(CLAZZ, MsHTTPSamplerProxy.class.getCanonicalName());
                JSONObject body = jsonObject.optJSONObject(BODY);
                if (StringUtils.isNotBlank(body.optString(TYPE))
                        && StringUtils.equals(body.optString(TYPE), Body.JSON_STR)
                        && StringUtils.isNotEmpty(body.optString(FORMAT))
                        && body.optJSONObject(JSONSCHEMA) != null
                        && Body.JSON_SCHEMA.equals(body.optString(FORMAT))) {
                    body.put(RAW, JSONSchemaParser.preview(body.optString(JSONSCHEMA)));
                    jsonObject.put(BODY, body);
                }
                item.setRequest(jsonObject.toString());
            }
        });
        // 排序
        FixedOrderComparator<String> fixedOrderComparator = new FixedOrderComparator<String>(request.getIds());
        fixedOrderComparator.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.BEFORE);
        BeanComparator beanComparator = new BeanComparator("id", fixedOrderComparator);
        Collections.sort(resList, beanComparator);

        calculateResult(resList, request.getProjectId());
        return resList;
    }

    /**
     * 初始化部分参数
     *
     * @param request           api 请求体
     * @param defaultSorting
     * @param checkThisWeekData
     * @return ApiDefinitionRequest
     */
    private ApiDefinitionRequest initRequest(ApiDefinitionRequest request, boolean defaultSorting, boolean checkThisWeekData) {
        if (defaultSorting) {
            request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        }
        if (checkThisWeekData) {
            if (request.isSelectThisWeedData()) {
                Map<String, Date> weekFirstTimeAndLastTime = DateUtils.getWeedFirstTimeAndLastTime(new Date());
                Date weekFirstTime = weekFirstTimeAndLastTime.get("firstTime");
                if (weekFirstTime != null) {
                    request.setCreateTime(weekFirstTime.getTime());
                }
            }
        }
        return request;
    }

    /**
     * 检查请求中是否含有覆盖率的查询
     *
     * @param request
     * @return
     */
    public void checkFilterHasCoverage(ApiDefinitionRequest request) {
        if (StringUtils.isNotEmpty(request.getProjectId())) {
            List<ApiDefinition> definitionList = null;
            String versionId = StringUtils.isEmpty(request.getVersionId()) ? null : request.getVersionId();
            if (StringUtils.equalsAnyIgnoreCase(request.getApiCoverage(), ApiHomeFilterEnum.NOT_COVERED, ApiHomeFilterEnum.COVERED)) {
                //计算没有用例接口的覆盖数量
                definitionList = this.selectEffectiveIdByProjectIdAndHaveNotCase(request.getProjectId(), versionId);
            } else if (StringUtils.equalsAnyIgnoreCase(request.getScenarioCoverage(), ApiHomeFilterEnum.NOT_COVERED, ApiHomeFilterEnum.COVERED)) {
                //计算全部用例
                definitionList = this.selectEffectiveIdByProjectId(request.getProjectId(), versionId);
            }
            if (CollectionUtils.isNotEmpty(definitionList)) {
                //如果查询条件中有未覆盖/已覆盖， 则需要解析出没有用例的接口中，有多少是符合场景覆盖规律的。然后将这些接口的id作为查询参数. 这里不根据版本筛选覆盖的url。
                Map<String, Map<String, String>> scenarioUrlList = apiAutomationService.selectScenarioUseUrlByProjectId(request.getProjectId(), null);
                List<String> apiIdInScenario = apiAutomationService.getApiIdInScenario(request.getProjectId(), scenarioUrlList, definitionList);
                if (CollectionUtils.isNotEmpty(apiIdInScenario)) {
                    request.setCoverageIds(apiIdInScenario);
                }
            }
        }
    }

    public ApiDefinition get(String id) {
        return apiDefinitionMapper.selectByPrimaryKey(id);
    }

    public ApiDefinitionWithBLOBs getBLOBs(String id) {
        return apiDefinitionMapper.selectByPrimaryKey(id);
    }

    public List<ApiDefinitionWithBLOBs> getBLOBs(List<String> idList) {
        if (idList == null || idList.isEmpty()) {
            return new ArrayList<>(0);
        } else {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(idList);
            example.setOrderByClause("create_time DESC ");
            return apiDefinitionMapper.selectByExampleWithBLOBs(example);
        }
    }

    public ApiDefinitionResult create(SaveApiDefinitionRequest request, List<MultipartFile> bodyFiles) {
        checkQuota(request.getProjectId());
        if (StringUtils.equals(request.getProtocol(), "DUBBO")) {
            request.setMethod("dubbo://");
        }
        if (StringUtils.isNotEmpty(request.getSourceId())) {
            // 检查附件复制出附件
            ApiFileUtil.copyBodyFiles(request.getSourceId(), request.getId());
        } else {
            ApiFileUtil.createBodyFiles(request.getRequest().getId(), bodyFiles);
        }
        request.setNewCreate(true);
        return createTest(request);
    }

    public ApiDefinitionResult update(SaveApiDefinitionRequest request, List<MultipartFile> bodyFiles) {
        request.setBodyUploadIds(null);
        if (StringUtils.equals(request.getProtocol(), "DUBBO")) {
            request.setMethod("dubbo://");
        }

        // 设置是否需要进入待更新列表
        ApiDefinitionSyncService apiDefinitionSyncService = CommonBeanFactory.getBean(ApiDefinitionSyncService.class);
        if (apiDefinitionSyncService != null) {
            SyncApiDefinitionRequest syncApiDefinitionRequest = new SyncApiDefinitionRequest(request.getId(), request.getProjectId(), request.getProtocol(), request.getRequest(), request.getToBeUpdated());
            apiDefinitionSyncService.syncApi(syncApiDefinitionRequest);
            request.setToBeUpdated(syncApiDefinitionRequest.getToBeUpdated());
        }

        ApiDefinitionWithBLOBs returnModel = updateTest(request);

        MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
        mockConfigService.updateMockReturnMsgByApi(returnModel);
        ApiFileUtil.createBodyFiles(request.getRequest().getId(), bodyFiles);
        String context = SessionUtils.getUserId().concat(Translator.get("update_api")).concat(":").concat(returnModel.getName());
        BeanMap beanMap = new BeanMap(returnModel);
        Map paramMap = new HashMap<>(beanMap);
        paramMap.put("operator", SessionUtils.getUserId());
        // 发送通知
        ApiCaseBatchSyncService apiCaseBatchSyncService = CommonBeanFactory.getBean(ApiCaseBatchSyncService.class);
        if (apiCaseBatchSyncService != null) {
            apiCaseBatchSyncService.sendApiNotice(returnModel, paramMap);
        }
        NoticeModel noticeModel = NoticeModel.builder().operator(SessionUtils.getUserId()).context(context).testId(returnModel.getId()).subject(Translator.get("api_update_notice")).paramMap(paramMap).excludeSelf(true).event(NoticeConstants.Event.UPDATE).build();
        noticeSendService.send(NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
        return getById(returnModel.getId());
    }


    public void checkQuota(String projectId) {
        baseQuotaService.checkAPIDefinitionQuota(projectId);
    }

    public void delete(String apiId) {
        ApiDefinition apiDefinition = get(apiId);
        if (apiDefinition == null) {
            return;
        }
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andRefIdEqualTo(apiDefinition.getRefId());
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
        apiDefinitions.forEach(api -> {
            apiTestCaseService.deleteTestCase(api.getId());
            extApiDefinitionExecResultMapper.deleteByResourceId(api.getId());
            apiDefinitionMapper.deleteByPrimaryKey(api.getId());
            MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
            mockConfigService.deleteMockConfigByApiId(api.getId());
            // 删除自定义字段关联关系
            customFieldApiService.deleteByResourceId(api.getId());
            // 删除关系图
            relationshipEdgeService.delete(api.getId());
            ApiFileUtil.deleteBodyFiles(api.getId());
            deleteFollows(api.getId());
        });
        // 删除附件关系
        extFileAssociationService.deleteByResourceId(apiId);
    }

    private void deleteFollows(String apiId) {
        ApiDefinitionFollowExample example = new ApiDefinitionFollowExample();
        example.createCriteria().andDefinitionIdEqualTo(apiId);
        apiDefinitionFollowMapper.deleteByExample(example);
    }

    public void deleteBatch(List<String> apiIds) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(apiIds);
        apiDefinitionMapper.deleteByExample(example);
        apiTestCaseService.deleteBatchByDefinitionId(apiIds);
        // 删除附件关系
        extFileAssociationService.deleteByResourceIds(apiIds);
        MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
        // 删除自定义字段关联关系
        customFieldApiService.deleteByResourceIds(apiIds);
        // 删除关系图
        relationshipEdgeService.delete(apiIds);
        for (String apiId : apiIds) {
            mockConfigService.deleteMockConfigByApiId(apiId);
            deleteFollows(apiId);
        }
    }

    public void removeToGc(List<String> apiIds) {
        if (CollectionUtils.isEmpty(apiIds)) {
            return;
        }
        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andIdIn(apiIds);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(apiDefinitionExample);
        if (CollectionUtils.isEmpty(apiDefinitions)) {
            return;
        }
        List<String> refIds = apiDefinitions.stream().map(ApiDefinition::getRefId).collect(Collectors.toList());
        ApiDefinitionExampleWithOperation example = new ApiDefinitionExampleWithOperation();
        example.createCriteria().andRefIdIn(refIds);
        example.setOperator(SessionUtils.getUserId());
        example.setOperationTime(System.currentTimeMillis());
        extApiDefinitionMapper.removeToGcByExample(example);

        apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andRefIdIn(refIds);
        List<ApiDefinition> apiDefinitionList = apiDefinitionMapper.selectByExample(apiDefinitionExample);
        List<String> ids = apiDefinitionList.stream().map(ApiDefinition::getId).collect(Collectors.toList());

        // 把所有版本的api case移到回收站
        List<String> apiCaseIds = apiTestCaseService.selectCaseIdsByApiIds(ids);
        if (CollectionUtils.isNotEmpty(apiCaseIds)) {
            ApiTestBatchRequest apiTestBatchRequest = new ApiTestBatchRequest();
            apiTestBatchRequest.setIds(apiCaseIds);
            apiTestBatchRequest.setUnSelectIds(new ArrayList<>());
            apiTestCaseService.deleteToGcByParam(apiTestBatchRequest);
        }
    }

    public ApiDefinitionImport apiTestImport(MultipartFile file, ApiTestImportRequest request) {
        //通过platform，获取对应的导入解析类型。
        if (file != null) {
            String originalFilename = file.getOriginalFilename();
            if (StringUtils.isNotBlank(originalFilename)) {
                String suffixName = originalFilename.substring(originalFilename.indexOf(".") + 1);
                apiDefinitionImportUtilService.checkFileSuffixName(request, suffixName);
            }
        }
        ApiImportParser runService = ApiDefinitionImportParserFactory.getApiImportParser(request.getPlatform());
        ApiDefinitionImport apiImport = null;
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        apiDefinitionImportUtilService.checkUrl(request, project);
        if (StringUtils.equals(request.getType(), SCHEDULE)) {
            request.setProtocol("HTTP");
        }
        try {
            apiImport = (ApiDefinitionImport) Objects.requireNonNull(runService).parse(file == null ? null : file.getInputStream(), request);
            if (apiImport.getMocks() == null) {
                apiImport.setMocks(new ArrayList<>());
            }
        } catch (MSException e) {
            // 发送通知
            apiDefinitionImportUtilService.sendFailMessage(request, project);
            throw e;
        } catch (Exception e) {
            // 发送通知
            apiDefinitionImportUtilService.sendFailMessage(request, project);
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("parse_data_error"));
        }

        try {
            List<ApiImportSendNoticeDTO> apiImportSendNoticeDTOS = apiDefinitionImportUtilService.importApi(request, apiImport);
            if (CollectionUtils.isNotEmpty(apiImport.getData())) {
                List<String> names = apiImport.getData().stream().map(ApiDefinitionWithBLOBs::getName).collect(Collectors.toList());
                request.setName(String.join(",", names));
                List<String> ids = apiImport.getData().stream().map(ApiDefinitionWithBLOBs::getId).collect(Collectors.toList());
                request.setId(JSON.toJSONString(ids));
            }
            // 发送通知
            apiDefinitionImportUtilService.sendImportNotice(request, apiImportSendNoticeDTOS, project);
        } catch (Exception e) {
            apiDefinitionImportUtilService.sendFailMessage(request, project);
            LogUtil.error(e);
            MSException.throwException(Translator.get("user_import_format_wrong"));
        }
        return apiImport;
    }

    public void reduction(ApiBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiDefinitionMapper.selectIds(query));
        if (request.getIds() != null || !request.getIds().isEmpty()) {
            request.getIds().forEach(apiId -> {
                ApiDefinitionWithBLOBs api = apiDefinitionMapper.selectByPrimaryKey(apiId);
                if (api == null) {
                    return;
                }
                //检查原来模块是否还在
                ApiDefinitionExample example = new ApiDefinitionExample();
                example.createCriteria().andRefIdEqualTo(api.getRefId());
                List<ApiDefinition> reductionCaseList = apiDefinitionMapper.selectByExample(example);
                Map<String, List<ApiDefinition>> nodeMap = new HashMap<>();
                List<String> reductionIds = new ArrayList<>();
                for (ApiDefinition apiDefinition : reductionCaseList) {
                    //检查是否同名
                    SaveApiDefinitionRequest apiDefinitionRequest = new SaveApiDefinitionRequest();
                    apiDefinitionRequest.setProjectId(apiDefinition.getProjectId());
                    apiDefinitionRequest.setMethod(apiDefinition.getMethod());
                    apiDefinitionRequest.setProtocol(apiDefinition.getProtocol());
                    apiDefinitionRequest.setPath(apiDefinition.getPath());
                    apiDefinitionRequest.setName(apiDefinition.getName());
                    apiDefinitionRequest.setId(apiDefinition.getId());
                    apiDefinitionRequest.setModuleId(apiDefinition.getModuleId());
                    apiDefinitionRequest.setModulePath(apiDefinition.getModulePath());
                    String moduleId = apiDefinition.getModuleId();
                    long nodeCount = apiModuleService.countById(moduleId);
                    if (nodeCount <= 0) {
                        checkNameExist(apiDefinitionRequest, true);
                    } else {
                        checkNameExist(apiDefinitionRequest, false);
                    }

                    if (StringUtils.isEmpty(moduleId)) {
                        moduleId = StringUtils.EMPTY;
                    }
                    if (nodeMap.containsKey(moduleId)) {
                        nodeMap.get(moduleId).add(apiDefinition);
                    } else {
                        List<ApiDefinition> list = new ArrayList<>();
                        list.add(apiDefinition);
                        nodeMap.put(moduleId, list);
                    }
                    reductionIds.add(apiDefinition.getId());
                }
                ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
                for (Map.Entry<String, List<ApiDefinition>> entry : nodeMap.entrySet()) {
                    String nodeId = entry.getKey();
                    long nodeCount = apiModuleService.countById(nodeId);
                    if (nodeCount <= 0) {
                        String projectId = request.getProjectId();
                        ApiModule node = apiModuleService.getDefaultNode(projectId, request.getProtocol());
                        for (ApiDefinition apiDefinition : entry.getValue()) {
                            ApiDefinitionWithBLOBs updateCase = new ApiDefinitionWithBLOBs();
                            updateCase.setId(apiDefinition.getId());
                            updateCase.setModuleId(node.getId());
                            updateCase.setModulePath("/" + node.getName());

                            apiDefinitionMapper.updateByPrimaryKeySelective(updateCase);
                        }
                    }
                }
                extApiDefinitionMapper.checkOriginalStatusByIds(reductionIds);
                extApiDefinitionMapper.reduction(reductionIds);

                List<String> apiCaseIds = apiTestCaseService.selectCaseIdsByApiIds(reductionIds);
                if (CollectionUtils.isNotEmpty(apiCaseIds)) {
                    ApiTestBatchRequest apiTestBatchRequest = new ApiTestBatchRequest();
                    apiTestBatchRequest.setIds(apiCaseIds);
                    apiTestBatchRequest.setUnSelectIds(new ArrayList<>());
                    apiTestCaseService.reduction(apiTestBatchRequest);
                }
            });

        }
    }

    private void checkNameExist(SaveApiDefinitionRequest request, Boolean moduleIdNotExist) {
        if (StringUtils.isEmpty(request.getVersionId())) {
            request.setVersionId(baseProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        }
        ApiDefinitionExample example = new ApiDefinitionExample();
        ApiDefinitionExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotEmpty(request.getProtocol()) && request.getProtocol().equals(RequestTypeConstants.HTTP)) {
            criteria.andMethodEqualTo(request.getMethod()).andStatusNotEqualTo("Trash").andProtocolEqualTo(request.getProtocol()).andPathEqualTo(request.getPath()).andProjectIdEqualTo(request.getProjectId()).andIdNotEqualTo(request.getId());
            Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
            ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.URL_REPEATABLE.name());
            boolean urlRepeat = config.getUrlRepeatable();
            if (urlRepeat) {
                if (moduleIdNotExist) {
                    criteria.andModulePathEqualTo(request.getModulePath());
                } else {
                    criteria.andModuleIdEqualTo(request.getModuleId());
                }
                criteria.andNameEqualTo(request.getName());
                if (apiDefinitionMapper.countByExample(example) > 0 && request.getNewCreate() != null && request.getNewCreate()) {
                    MSException.throwException(Translator.get("api_versions_create"));
                }
                criteria.andVersionIdEqualTo(request.getVersionId());
                if (apiDefinitionMapper.countByExample(example) > 0) {
                    MSException.throwException(Translator.get("api_definition_name_not_repeating") + " :" + Translator.get("api_definition_module") + ":" + request.getModulePath() + " ," + Translator.get("api_definition_name") + " :" + request.getName() + "-" + request.getPath());
                }
            } else {
                if (apiDefinitionMapper.countByExample(example) > 0 && request.getNewCreate() != null && request.getNewCreate()) {
                    MSException.throwException(Translator.get("api_versions_create"));
                }
                criteria.andVersionIdEqualTo(request.getVersionId());
                if (apiDefinitionMapper.countByExample(example) > 0) {
                    MSException.throwException(Translator.get("api_definition_url_not_repeating") + " :" + Translator.get("api_definition_module") + ":" + request.getModulePath() + " ," + Translator.get("api_definition_name") + " :" + request.getName());
                }
            }
        } else {
            criteria.andProtocolEqualTo(request.getProtocol()).andStatusNotEqualTo("Trash").andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId()).andIdNotEqualTo(request.getId());
            if (moduleIdNotExist) {
                criteria.andModulePathEqualTo(request.getModulePath());
            } else {
                criteria.andModuleIdEqualTo(request.getModuleId());
            }
            if (apiDefinitionMapper.countByExample(example) > 0 && request.getNewCreate() != null && request.getNewCreate()) {
                MSException.throwException(Translator.get("api_versions_create"));
            }
            criteria.andVersionIdEqualTo(request.getVersionId());
            if (apiDefinitionMapper.countByExample(example) > 0) {
                MSException.throwException(Translator.get("api_definition_name_already_exists") + " :" + Translator.get("api_definition_module") + ":" + request.getModulePath() + " ," + Translator.get("api_definition_name") + " :" + request.getName());
            }
        }
        if (StringUtils.isNotBlank(request.getId())) {
            ApiDefinitionWithBLOBs result = apiDefinitionMapper.selectByPrimaryKey(request.getId());
            if (result != null) {
                example = new ApiDefinitionExample();
                example.createCriteria().andRefIdEqualTo(result.getRefId()).andStatusNotEqualTo("Trash");
                List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
                if (apiDefinitions != null && apiDefinitions.size() > 1) {
                    if (request.getProtocol().equals(RequestTypeConstants.HTTP) && (!StringUtils.equals(result.getMethod(), request.getMethod()) || !StringUtils.equals(result.getPath(), request.getPath()))) {
                        MSException.throwException(Translator.get("api_versions_update_http"));
                    } else {
                        if (!StringUtils.equals(result.getName(), request.getName())) {
                            MSException.throwException(Translator.get("api_versions_update"));
                        }
                    }

                }
            }
        }
    }

    private ApiDefinitionWithBLOBs updateTest(SaveApiDefinitionRequest request) {
        checkNameExist(request, false);
        if (StringUtils.equals(request.getMethod(), "TCP")) {
            request = tcpApiParamService.handleTcpRequest(request);
        }
        final ApiDefinitionWithBLOBs test = new ApiDefinitionWithBLOBs();
        test.setId(request.getId());
        test.setName(request.getName());
        test.setPath(request.getPath());
        test.setProjectId(request.getProjectId());
        request.getRequest().setId(request.getId());
        test.setRequest(JSON.toJSONString(request.getRequest()));
        test.setUpdateTime(System.currentTimeMillis());
        test.setStatus(request.getStatus());
        test.setModulePath(request.getModulePath());
        test.setModuleId(request.getModuleId());
        test.setMethod(request.getMethod());
        test.setProtocol(request.getProtocol());
        test.setDescription(request.getDescription());
        test.setResponse(JSON.toJSONString(request.getResponse()));
        test.setEnvironmentId(request.getEnvironmentId());
        test.setUserId(request.getUserId());
        test.setRemark(request.getRemark());
        if (request.getToBeUpdated() != null) {
            test.setToBeUpdated(request.getToBeUpdated());
            if (request.getToBeUpdated()) {
                test.setToBeUpdateTime(System.currentTimeMillis());
            }
        }
        if (StringUtils.isNotEmpty(request.getTags()) && !StringUtils.equals(request.getTags(), "[]")) {
            test.setTags(request.getTags());
        } else {
            test.setTags(StringUtils.EMPTY);
        }
        this.setModule(test);

        // 更新数据
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdEqualTo(test.getId()).andVersionIdEqualTo(request.getVersionId());
        if (apiDefinitionMapper.updateByExampleSelective(test, example) == 0) {
            // 插入新版本的数据
            ApiDefinitionWithBLOBs oldApi = apiDefinitionMapper.selectByPrimaryKey(test.getId());
            test.setId(UUID.randomUUID().toString());
            test.setNum(oldApi.getNum());
            test.setVersionId(request.getVersionId());
            test.setCreateTime(System.currentTimeMillis());
            test.setUpdateTime(System.currentTimeMillis());
            test.setCreateUser(SessionUtils.getUserId());
            test.setOrder(oldApi.getOrder());
            test.setRefId(oldApi.getRefId());
            // 保存扩展信息
            saveExtendInfo(request, test, oldApi);

            apiDefinitionMapper.insertSelective(test);
        }
        // 同步修改用例路径
        if (StringUtils.equals(test.getProtocol(), "HTTP")) {
            List<String> ids = new ArrayList<>();
            ids.add(request.getId());
            apiTestCaseService.updateByApiDefinitionId(ids, test, request.getTriggerUpdate());
        }
        ApiDefinitionWithBLOBs result = apiDefinitionMapper.selectByPrimaryKey(test.getId());
        String defaultVersion = baseProjectVersionMapper.getDefaultVersion(request.getProjectId());
        if (StringUtils.equalsIgnoreCase(request.getVersionId(), defaultVersion)) {
            checkAndSetLatestVersion(result.getRefId());
        }
        //同步修改所有版本的模块路径
        updateOtherVersionModule(result);
        // 存储附件关系
        extFileAssociationService.saveApi(test.getId(), request.getRequest(), FileAssociationTypeEnums.API.name());
        //保存自定义字段
        customFieldApiService.editFields(test.getId(), request.getEditFields());
        customFieldApiService.addFields(test.getId(), request.getAddFields());
        return result;
    }

    private void updateOtherVersionModule(ApiDefinitionWithBLOBs result) {
        extApiDefinitionMapper.updateVersionModule(result.getRefId(), result.getVersionId(), result.getModuleId(), result.getModulePath());
    }

    private void saveExtendInfo(SaveApiDefinitionRequest request, ApiDefinitionWithBLOBs test, ApiDefinitionWithBLOBs oldApi) {
        // 创建新版是否关联备注
        if (!request.isNewVersionRemark()) {
            test.setRemark(null);
        }
        if (request.isNewVersionCase()) {
            test.setCaseTotal(oldApi.getCaseTotal());
            extApiTestCaseMapper.insertNewVersionCases(test, oldApi);
        }
        if (request.isNewVersionMock()) {
            MockConfigExample mockConfigExample = new MockConfigExample();
            mockConfigExample.createCriteria().andApiIdEqualTo(oldApi.getId());
            List<MockConfig> mockConfigs = mockConfigMapper.selectByExample(mockConfigExample);
            mockConfigs.forEach(config -> {
                String newMockConfigId = UUID.randomUUID().toString();
                // 1
                MockExpectConfigExample expectConfigExample = new MockExpectConfigExample();
                expectConfigExample.createCriteria().andMockConfigIdEqualTo(config.getId());
                List<MockExpectConfigWithBLOBs> mockExpectConfigWithBLOBs = mockExpectConfigMapper.selectByExampleWithBLOBs(expectConfigExample);
                mockExpectConfigWithBLOBs.forEach(expectConfig -> {
                    expectConfig.setId(UUID.randomUUID().toString());
                    expectConfig.setMockConfigId(newMockConfigId);
                    mockExpectConfigMapper.insert(expectConfig);
                });

                // 2
                config.setId(newMockConfigId);
                config.setApiId(test.getId());
                mockConfigMapper.insert(config);
            });
        }

        // 创建新版是否关联依赖关系
        if (request.isNewVersionDeps()) {
            List<RelationshipEdgeDTO> pre = this.getRelationshipApi(oldApi.getId(), "PRE");
            List<String> targetIds = pre.stream().map(RelationshipEdgeKey::getTargetId).collect(Collectors.toList());
            RelationshipEdgeRequest req = new RelationshipEdgeRequest();
            req.setTargetIds(targetIds);
            req.setType("API");
            req.setId(test.getId());
            relationshipEdgeService.saveBatch(req);

            List<RelationshipEdgeDTO> post = this.getRelationshipApi(oldApi.getId(), "POST");
            List<String> sourceIds = post.stream().map(RelationshipEdgeKey::getSourceId).collect(Collectors.toList());
            RelationshipEdgeRequest req2 = new RelationshipEdgeRequest();
            req2.setSourceIds(sourceIds);
            req2.setType("API");
            req2.setId(test.getId());
            relationshipEdgeService.saveBatch(req2);
        }
    }

    /**
     * 检查设置最新版本
     */
    private void checkAndSetLatestVersion(String refId) {
        extApiDefinitionMapper.clearLatestVersion(refId);
        extApiDefinitionMapper.addLatestVersion(refId);
    }

    public void saveFollows(String definitionId, List<String> follows) {
        ApiDefinitionFollowExample example = new ApiDefinitionFollowExample();
        example.createCriteria().andDefinitionIdEqualTo(definitionId);
        apiDefinitionFollowMapper.deleteByExample(example);
        if (!org.springframework.util.CollectionUtils.isEmpty(follows)) {
            for (String follow : follows) {
                ApiDefinitionFollow item = new ApiDefinitionFollow();
                item.setDefinitionId(definitionId);
                item.setFollowId(follow);
                apiDefinitionFollowMapper.insert(item);
            }
        }
    }

    private ApiDefinitionResult createTest(SaveApiDefinitionRequest request) {
        if (StringUtils.isEmpty(request.getModuleId()) || "default-module".equals(request.getModuleId())) {
            ApiModuleExample example = new ApiModuleExample();
            example.createCriteria().andProjectIdEqualTo(request.getProjectId()).andProtocolEqualTo(request.getProtocol()).andNameEqualTo(ProjectModuleDefaultNodeEnum.API_MODULE_DEFAULT_NODE.getNodeName()).andLevelEqualTo(1);
            List<ApiModule> modules = apiModuleMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(modules)) {
                request.setModuleId(modules.get(0).getId());
                request.setModulePath("/未规划接口");
            }
        }
        checkNameExist(request, false);
        final ApiDefinitionWithBLOBs test = new ApiDefinitionWithBLOBs();
        test.setId(request.getId());
        test.setName(request.getName());
        test.setProtocol(request.getProtocol());
        test.setMethod(request.getMethod());
        test.setPath(request.getPath());
        test.setCreateUser(SessionUtils.getUserId());
        test.setProjectId(request.getProjectId());
        request.getRequest().setId(request.getId());
        test.setRequest(JSON.toJSONString(request.getRequest()));
        test.setCreateTime(System.currentTimeMillis());
        test.setUpdateTime(System.currentTimeMillis());
        test.setStatus(ApiTestDataStatus.UNDERWAY.getValue());
        test.setModulePath(request.getModulePath());
        test.setModuleId(request.getModuleId());
        test.setRemark(request.getRemark());
        test.setOrder(ServiceUtils.getNextOrder(request.getProjectId(), extApiDefinitionMapper::getLastOrder));
        test.setRefId(request.getId());
        test.setVersionId(request.getVersionId());
        test.setLatest(true); // 新建一定是最新的
        test.setResponse(JSON.toJSONString(request.getResponse()));
        test.setEnvironmentId(request.getEnvironmentId());
        test.setNum(getNextNum(request.getProjectId()));
        if (request.getUserId() == null) {
            test.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        } else {
            test.setUserId(request.getUserId());
        }
        test.setDescription(request.getDescription());
        if (StringUtils.isNotEmpty(request.getTags()) && !StringUtils.equals(request.getTags(), "[]")) {
            test.setTags(request.getTags());
        } else {
            test.setTags(StringUtils.EMPTY);
        }
        if (apiDefinitionMapper.selectByPrimaryKey(test.getId()) == null) {
            apiDefinitionMapper.insert(test);
            saveFollows(test.getId(), request.getFollows());
        }
        // 存储附件关系
        extFileAssociationService.saveApi(test.getId(), request.getRequest(), FileAssociationTypeEnums.API.name());
        //保存自定义字段
        List<CustomFieldResourceDTO> addFields = request.getAddFields();
        if (CollectionUtils.isNotEmpty(addFields)) {
            addFields.addAll(request.getEditFields());
            customFieldApiService.addFields(request.getId(), addFields);
        }
        return getById(test.getId());
    }

    public int getNextNum(String projectId) {
        ApiDefinition apiDefinition = extApiDefinitionMapper.getNextNum(projectId);
        if (apiDefinition == null || apiDefinition.getNum() == null) {
            return 100001;
        } else {
            return Optional.of(apiDefinition.getNum() + 1).orElse(100001);
        }
    }


    /**
     * 获取存储执行结果报告
     *
     * @param testId
     * @return
     */
    public ApiReportResult getDbResult(String testId) {
        ApiDefinitionExecResultWithBLOBs result = extApiDefinitionExecResultMapper.selectMaxResultByResourceId(testId);
        return buildAPIReportResult(result);
    }

    public ApiReportResult getReportById(String testId) {
        ApiDefinitionExecResultWithBLOBs result = apiDefinitionExecResultMapper.selectByPrimaryKey(testId);
        return buildAPIReportResult(result);
    }

    public ApiReportResult buildAPIReportResult(ApiDefinitionExecResultWithBLOBs result) {
        if (result == null) {
            return null;
        }
        ApiReportResult reportResult = new ApiReportResult();
        reportResult.setStatus(result.getStatus());
        String contentStr = result.getContent();
        try {
            if (StringUtils.isNotBlank(contentStr)) {
                JSONObject content = JSONUtil.parseObject(contentStr);
                if (StringUtils.isNotEmpty(result.getEnvConfig())) {
                    ApiReportEnvConfigDTO envNameByEnvConfig = this.getEnvNameByEnvConfig(result.getProjectId(), result.getEnvConfig());
                    if (envNameByEnvConfig != null) {
                        content.put("envName", envNameByEnvConfig.getEnvName());
                        content.put("poolName", envNameByEnvConfig.getResourcePoolName());
                    }
                }
                contentStr = content.toString();
                reportResult.setContent(contentStr);
            }
        } catch (Exception e) {
            LogUtil.error("解析content失败!", e);
        }
        return reportResult;
    }

    public ApiReportEnvConfigDTO getEnvNameByEnvConfig(String projectId, String envConfig) {
        ApiReportEnvConfigDTO apiReportEnvConfig = new ApiReportEnvConfigDTO();
        RunModeConfigDTO runModeConfigDTO = null;
        try {
            runModeConfigDTO = JSON.parseObject(envConfig, RunModeConfigDTO.class);
        } catch (Exception e) {
            LogUtil.error("解析" + envConfig + "为RunModeConfigDTO时失败！", e);
        }
        if (StringUtils.isNotEmpty(projectId) && runModeConfigDTO != null && MapUtils.isNotEmpty(runModeConfigDTO.getEnvMap())) {
            String envId = runModeConfigDTO.getEnvMap().get(projectId);
            apiReportEnvConfig.setEnvName(apiTestEnvironmentService.selectNameById(envId));
        }
        if (runModeConfigDTO != null && StringUtils.isNotBlank(runModeConfigDTO.getResourcePoolId())) {
            TestResourcePool resourcePool = baseTestResourcePoolService.getResourcePool(runModeConfigDTO.getResourcePoolId());
            if (resourcePool != null) {
                apiReportEnvConfig.setResourcePoolName(resourcePool.getName());
            }
        } else {
            apiReportEnvConfig.setResourcePoolName(StorageEnums.LOCAL.name());
        }
        return apiReportEnvConfig;
    }

    public ApiReportResult getDbResult(String testId, String type) {
        ApiDefinitionExecResultWithBLOBs result = extApiDefinitionExecResultMapper.selectMaxResultByResourceIdAndType(testId, type);
        return buildAPIReportResult(result);
    }

    private void setModule(ApiDefinitionWithBLOBs item) {
        if (item != null && StringUtils.isEmpty(item.getModuleId()) || "default-module".equals(item.getModuleId())) {
            ApiModuleExample example = new ApiModuleExample();
            example.createCriteria().andProjectIdEqualTo(item.getProjectId()).andProtocolEqualTo(item.getProtocol()).andNameEqualTo(ProjectModuleDefaultNodeEnum.API_MODULE_DEFAULT_NODE.getNodeName());
            List<ApiModule> modules = apiModuleMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(modules)) {
                item.setModuleId(modules.get(0).getId());
                item.setModulePath(modules.get(0).getName());
            }
        }
    }

    public List<ApiScenarioDTO> getReference(ApiScenarioRequest request) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            return new ArrayList<>();
        } else {
            return extApiScenarioMapper.selectReference(request);
        }
    }

    public void getReferenceIds(ApiScenarioRequest request) {
        ApiScenarioReferenceIdExample example = new ApiScenarioReferenceIdExample();
        example.createCriteria().andReferenceIdEqualTo(request.getId()).andReferenceTypeEqualTo(request.getRefType());
        List<ApiScenarioReferenceId> scenarioReferenceIds = apiScenarioReferenceIdMapper.selectByExample(example);
        List<String> scenarioIds = scenarioReferenceIds.stream().map(ApiScenarioReferenceId::getApiScenarioId).collect(Collectors.toList());
        request.setIds(scenarioIds);
    }

    public void editApiBath(ApiBatchRequest request) {
        ApiDefinitionExample definitionExample = new ApiDefinitionExample();
        definitionExample.createCriteria().andIdIn(request.getIds());

        ApiDefinitionWithBLOBs definitionWithBLOBs = new ApiDefinitionWithBLOBs();
        BeanUtils.copyBean(definitionWithBLOBs, request);
        definitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMapper.updateByExampleSelective(definitionWithBLOBs, definitionExample);
    }

    public void editApiByParam(ApiBatchRequest request) {
        if (request == null) {
            return;
        }
        if (StringUtils.equals("tags", request.getType())) {
            this.batchEditDefinitionTags(request);
            return;
        }
        if (StringUtils.isNotBlank(request.getMethod())) {
            this.batchEditDefinitionMethod(request);
            return;
        }
        //name在这里只是查询参数
        request.setName(null);
        ApiDefinitionWithBLOBs definitionWithBLOBs = new ApiDefinitionWithBLOBs();
        BeanUtils.copyBean(definitionWithBLOBs, request);
        definitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiDefinitionMapper.selectIds(query));
        if (CollectionUtils.isNotEmpty(request.getIds())) {
            request.getIds().forEach(apiId -> {
                ApiDefinitionWithBLOBs api = apiDefinitionMapper.selectByPrimaryKey(apiId);
                if (api == null) {
                    return;
                }
                //检查是否同名
                SaveApiDefinitionRequest apiDefinitionRequest = new SaveApiDefinitionRequest();
                apiDefinitionRequest.setProjectId(api.getProjectId());
                if (StringUtils.isEmpty(request.getMethod())) {
                    apiDefinitionRequest.setMethod(api.getMethod());
                } else {
                    apiDefinitionRequest.setMethod(request.getMethod());
                }
                apiDefinitionRequest.setProtocol(api.getProtocol());
                apiDefinitionRequest.setPath(api.getPath());
                apiDefinitionRequest.setName(api.getName());
                apiDefinitionRequest.setId(api.getId());
                if (StringUtils.isEmpty(request.getModuleId())) {
                    apiDefinitionRequest.setModuleId(api.getModuleId());
                } else {
                    apiDefinitionRequest.setModuleId((request.getModuleId()));
                }
                if (StringUtils.isEmpty(request.getModulePath())) {
                    apiDefinitionRequest.setModulePath(api.getModulePath());
                } else {
                    apiDefinitionRequest.setModulePath(request.getModulePath());
                }
                apiDefinitionRequest.setVersionId(api.getVersionId());
                checkNameExist(apiDefinitionRequest, false);
                //同步修改所有版本的模块路径
                if (StringUtils.isNotEmpty(request.getModuleId()) && StringUtils.isNotEmpty(request.getModulePath())) {
                    api.setModuleId(request.getModuleId());
                    api.setModulePath(request.getModulePath());
                    updateOtherVersionModule(api);
                }
            });
            if (StringUtils.isNotEmpty(request.getFollow())) {
                if (StringUtils.equals(request.getFollow(), "cancel")) {
                    this.deleteFollows(request.getIds());
                }
            }
        }
        if (StringUtils.isEmpty(request.getFollow())) {
            apiDefinitionMapper.updateByExampleSelective(definitionWithBLOBs, getBatchExample(request));
        }
    }

    private void batchEditDefinitionMethod(ApiBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiDefinitionMapper.selectIds(query));
        if (CollectionUtils.isEmpty(request.getIds())) {
            return;
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDefinitionMapper mapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(request.getIds());
        List<ApiDefinitionWithBLOBs> apiDefinitions = apiDefinitionMapper.selectByExampleWithBLOBs(example);
        for (ApiDefinitionWithBLOBs apiDefinition : apiDefinitions) {
            apiDefinition.setMethod(request.getMethod());
            JSONObject jsonObject = JSONUtil.parseObject(apiDefinition.getRequest());
            jsonObject.put("method", request.getMethod());
            apiDefinition.setRequest(jsonObject.toString());
            apiDefinition.setUpdateTime(System.currentTimeMillis());
            mapper.updateByPrimaryKeyWithBLOBs(apiDefinition);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private void batchEditDefinitionTags(ApiBatchRequest request) {
        if (request.getTagList().isEmpty()) {
            return;
        }
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiDefinitionMapper.selectIds(query));
        if (CollectionUtils.isEmpty(request.getIds())) {
            return;
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDefinitionMapper mapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(request.getIds());
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
        for (ApiDefinition apiDefinition : apiDefinitions) {
            String tags = apiDefinition.getTags();
            if (StringUtils.isBlank(tags) || BooleanUtils.isFalse(request.isAppendTag())) {
                apiDefinition.setTags(JSON.toJSONString(request.getTagList()));
                apiDefinition.setUpdateTime(System.currentTimeMillis());
            } else {
                try {
                    List<String> list = JSON.parseArray(tags, String.class);
                    list.addAll(request.getTagList());
                    apiDefinition.setTags(JSON.toJSONString(list));
                    apiDefinition.setUpdateTime(System.currentTimeMillis());
                } catch (Exception e) {
                    LogUtil.error("batch edit tags error.");
                    LogUtil.error(e, e.getMessage());
                    apiDefinition.setTags(JSON.toJSONString(request.getTagList()));
                }
            }
            mapper.updateByPrimaryKey(apiDefinition);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public void testPlanRelevance(ApiCaseRelevanceRequest request) {
        relevanceByApi(request);
    }

    public void relevanceByApi(ApiCaseRelevanceRequest request) {
        if (CollectionUtils.isEmpty(request.getSelectIds())) {
            return;
        }
        Collections.reverse(request.getSelectIds());
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andApiDefinitionIdIn(request.getSelectIds());
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        List<String> ids = apiTestCases.stream().map(ApiTestCase::getId).collect(Collectors.toList());
        testPlanApiCaseService.relevance(ids, request);
    }

    public void testCaseReviewRelevance(ApiCaseRelevanceRequest request) {
        apiTestCaseService.relevanceByApiByReview(request);
    }

    /**
     * 统计本周创建的数据总量
     *
     * @return
     */
    public List<ApiDefinition> getApiByCreateInThisWeek(Map<String, List<ApiDefinition>> protocalAllApiList) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return new ArrayList<>();
        } else {
            List<ApiDefinition> apiCreatedInWeekList = new ArrayList<>();
            for (List<ApiDefinition> apiList : protocalAllApiList.values()) {
                for (ApiDefinition api : apiList) {
                    if (api.getCreateTime() >= firstTime.getTime() && api.getCreateTime() <= lastTime.getTime()) {
                        apiCreatedInWeekList.add(api);
                    }
                }
            }
            return apiCreatedInWeekList;
        }
    }

    public List<ApiDataCountResult> countStateByProjectID(String projectId, String versionId) {
        return extApiDefinitionMapper.countStateByProjectID(projectId, versionId);
    }

    public List<ApiDataCountResult> countApiCoverageByProjectID(String projectId, String versionId) {
        ApiDefinitionExample countApiExample = new ApiDefinitionExample();
        ApiDefinitionExample.Criteria apiCountCriteria = countApiExample.createCriteria();
        apiCountCriteria.andProjectIdEqualTo(projectId).andStatusNotEqualTo("Trash");
        if (StringUtils.isNotEmpty(versionId)) {
            apiCountCriteria.andVersionIdEqualTo(versionId);
        } else {
            apiCountCriteria.andLatestEqualTo(true);
        }
        long apiCount = apiDefinitionMapper.countByExample(countApiExample);

        //之前的方法使用left join来判断api下有无case。 数据量大了之后sql就变得比较慢。  现在改为先查询出没有case的api，然后用减法操作
        List<ApiDataCountResult> apiDataCountResultList = extApiDefinitionMapper.countApiHasNotCaseByProjectID(projectId, versionId);

        AtomicLong unCoveredAtomicLong = new AtomicLong();
        apiDataCountResultList.forEach(item -> {
            unCoveredAtomicLong.addAndGet(item.getCountNumber());
        });
        long coveredLong = apiCount - unCoveredAtomicLong.get();
        ApiDataCountResult coveredResult = new ApiDataCountResult();
        coveredResult.setGroupField("covered");
        coveredResult.setCountNumber(coveredLong < 0 ? 0 : coveredLong);
        apiDataCountResultList.add(coveredResult);
        return apiDataCountResultList;
    }

    public void deleteByParams(ApiBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiDefinitionMapper.selectIds(query));
        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        this.deleteApiByIds(ids);
    }

    private void deleteApiByIds(List<String> ids) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(ids);
        List<ApiDefinition> apiDefinitionList = apiDefinitionMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(apiDefinitionList)) {
            return;
        }
        List<String> refIds = apiDefinitionList.stream().map(ApiDefinition::getRefId).collect(Collectors.toList());
        example = new ApiDefinitionExample();
        example.createCriteria().andRefIdIn(refIds);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
        List<String> apiIds = apiDefinitions.stream().map(ApiDefinition::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(apiIds)) {
            //删除Api、ApiCase中resourceID被删除了的执行记录
            apiTestCaseService.deleteBatchByDefinitionId(apiIds);
            apiDefinitionMapper.deleteByExample(example);
            // 删除附件关系
            extFileAssociationService.deleteByResourceIds(apiIds);
            // 删除自定义字段关联关系
            customFieldApiService.deleteByResourceIds(apiIds);
            // 删除关系图
            relationshipEdgeService.delete(apiIds);
            mockConfigService.deleteMockConfigByApiIds(apiIds);
            deleteFollows(apiIds);
        }
    }

    public ApiDefinitionExample getBatchExample(ApiBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiDefinitionMapper.selectIds(query));
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(request.getIds());
        return example;
    }

    public void removeToGcByParams(ApiBatchRequest request) {
        // 去除Name排序
        if (request.getCondition() != null && CollectionUtils.isNotEmpty(request.getCondition().getOrders())) {
            request.getCondition().getOrders().clear();
        }
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiDefinitionMapper.selectIds(query));

        this.removeToGc(request.getIds());
    }

    public Pager<List<ApiDefinitionResult>> listRelevance(ApiDefinitionRequest request, int goPage, int pageSize) {
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        List<ApiDefinitionResult> resList = extApiDefinitionMapper.listRelevance(request);
        buildUserInfo(resList);
        calculateResult(resList, request.getProjectId());
        return PageUtils.setPageInfo(page, resList);
    }

    public List<ApiDefinitionResult> listRelevanceReview(ApiDefinitionRequest request) {
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        List<ApiDefinitionResult> resList = extApiDefinitionMapper.listRelevanceReview(request);
        calculateResult(resList, request.getProjectId());
        resList = extApiDefinitionMapper.list(request);
        return resList;
    }

    public void calculateRelationScenario(List<ApiDefinitionResult> resList) {
        if (!resList.isEmpty()) {
            resList.forEach(res -> {
                List<ApiScenario> scenarioList = extApiDefinitionMapper.scenarioList(res.getId());
                res.setScenarioTotal(scenarioList.size());
                List<String> scenarioIdList = scenarioList.stream().map(ApiScenario::getId).collect(Collectors.toList());
                res.setScenarioIds(scenarioIdList);
            });
        }
    }

    public void calculateResult(List<ApiDefinitionResult> resList, String projectId) {
        if (!resList.isEmpty()) {
            List<String> ids = resList.stream().map(ApiDefinitionResult::getId).collect(Collectors.toList());
            List<ApiComputeResult> results = extApiDefinitionMapper.selectByIdsAndStatusIsNotTrash(ids, projectId);
            Map<String, ApiComputeResult> resultMap = results.stream().collect(Collectors.toMap(ApiComputeResult::getApiDefinitionId, Function.identity()));
            for (ApiDefinitionResult res : resList) {
                ApiComputeResult compRes = resultMap.get(res.getId());
                if (compRes != null) {
                    res.setCaseType("apiCase");
                    res.setCaseTotal(String.valueOf(compRes.getCaseTotal()));
                    res.setCasePassingRate(compRes.getPassRate());
                    // 状态优先级 未执行，未通过，通过
                    if ((compRes.getError() + compRes.getSuccess()) < compRes.getCaseTotal()) {
                        res.setCaseStatus(ApiReportStatus.PENDING.name());
                    } else if (compRes.getError() > 0) {
                        res.setCaseStatus(ApiReportStatus.ERROR.name());
                    } else {
                        res.setCaseStatus(ApiReportStatus.SUCCESS.name());
                    }
                } else {
                    res.setCaseType("apiCase");
                    res.setCaseTotal("0");
                    res.setCasePassingRate("-");
                    res.setCaseStatus("-");
                }
            }
        }
    }

    /**
     * swagger定时导入
     */
    public void createSchedule(ScheduleRequest request) {
        /*保存swaggerUrl*/
        SwaggerUrlProject swaggerUrlProject = new SwaggerUrlProject();
        BeanUtils.copyBean(swaggerUrlProject, request);
        swaggerUrlProject.setId(UUID.randomUUID().toString());
        // 设置鉴权信息
        if (request.getHeaders() != null || request.getArguments() != null || request.getAuthManager() != null) {
            String config = setAuthParams(request);
            swaggerUrlProject.setConfig(config);
        }
        scheduleService.addSwaggerUrlSchedule(swaggerUrlProject);

        request.setResourceId(swaggerUrlProject.getId());
        Schedule schedule = scheduleService.buildApiTestSchedule(request);
        schedule.setProjectId(swaggerUrlProject.getProjectId());
        try {
            schedule.setName(new java.net.URL(swaggerUrlProject.getSwaggerUrl()).getHost());
        } catch (MalformedURLException e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("url_is_not_valid"));
        }
        schedule.setJob(SwaggerUrlImportJob.class.getName());
        schedule.setGroup(ScheduleGroup.SWAGGER_IMPORT.name());
        schedule.setType(ScheduleType.CRON.name());
        scheduleService.addSchedule(schedule);
        this.addOrUpdateSwaggerImportCronJob(request);

    }

    public void updateSchedule(ScheduleRequest request) {
        SwaggerUrlProject swaggerUrlProject = new SwaggerUrlProject();
        BeanUtils.copyBean(swaggerUrlProject, request);
        // 设置鉴权信息
        if (request.getHeaders() != null || request.getArguments() != null || request.getAuthManager() != null) {
            String config = setAuthParams(request);
            swaggerUrlProject.setConfig(config);
        } else {
            swaggerUrlProject.setConfig(null);
        }
        scheduleService.updateSwaggerUrlSchedule(swaggerUrlProject);
        // 只修改表达式和名称
        Schedule schedule = new Schedule();
        schedule.setId(request.getTaskId());
        schedule.setValue(request.getValue().trim());
        schedule.setEnable(request.getEnable());
        try {
            schedule.setName(new java.net.URL(swaggerUrlProject.getSwaggerUrl()).getHost());
        } catch (MalformedURLException e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("url_is_not_valid"));
        }
        scheduleService.editSchedule(schedule);
        request.setResourceId(swaggerUrlProject.getId());
        this.addOrUpdateSwaggerImportCronJob(request);
    }

    /**
     * 设置 SwaggerUrl 同步鉴权参数
     */
    public String setAuthParams(ScheduleRequest request) {
        // list 数组转化成 json 字符串
        JSONObject configObj = new JSONObject();
        configObj.put(HEADERS, request.getHeaders());
        configObj.put(ARGUMENTS, request.getArguments());
        // 设置 BaseAuth 参数
        if (request.getAuthManager() != null && StringUtils.isNotBlank(request.getAuthManager().getUsername()) && StringUtils.isNotBlank(request.getAuthManager().getPassword())) {
            MsAuthManager authManager = request.getAuthManager();
            configObj.put("authManager", JSONUtil.parseObject(JSONUtil.toJSONString(authManager)));
        }
        return configObj.toString();
    }

    /**
     * 列表开关切换
     *
     * @param request
     */
    public void switchSchedule(Schedule request) {
        scheduleService.editSchedule(request);
        this.addOrUpdateSwaggerImportCronJob(request);
    }

    //删除
    public void deleteSchedule(ScheduleRequest request) {
        swaggerUrlProjectMapper.deleteByPrimaryKey(request.getId());
        scheduleService.deleteByResourceId(request.getId(), ScheduleGroup.SWAGGER_IMPORT.name());
    }

    //查询swaggerUrl详情
    public SwaggerUrlProject getSwaggerInfo(String resourceId) {
        return swaggerUrlProjectMapper.selectByPrimaryKey(resourceId);
    }

    public String getResourceId(SwaggerUrlRequest swaggerUrlRequest) {
        SwaggerUrlProjectExample swaggerUrlProjectExample = new SwaggerUrlProjectExample();
        SwaggerUrlProjectExample.Criteria criteria = swaggerUrlProjectExample.createCriteria();
        criteria.andProjectIdEqualTo(swaggerUrlRequest.getProjectId()).andSwaggerUrlEqualTo(swaggerUrlRequest.getSwaggerUrl());
        if (StringUtils.isNotBlank(swaggerUrlRequest.getModuleId())) {
            criteria.andModuleIdEqualTo(swaggerUrlRequest.getModuleId());
        }
        List<SwaggerUrlProject> list = swaggerUrlProjectMapper.selectByExample(swaggerUrlProjectExample);
        String resourceId = StringUtils.EMPTY;
        if (list.size() == 1) {
            resourceId = list.get(0).getId();
        }
        return resourceId;
    }

    public List<SwaggerTaskResult> getSwaggerScheduleList(String projectId) {
        List<SwaggerTaskResult> resultList = extSwaggerUrlScheduleMapper.getSwaggerTaskList(projectId);
        int dataIndex = 1;
        for (SwaggerTaskResult swaggerTaskResult : resultList) {
            swaggerTaskResult.setIndex(dataIndex++);
            Date nextExecutionTime = CronUtils.getNextTriggerTime(swaggerTaskResult.getRule());
            if (nextExecutionTime != null) {
                swaggerTaskResult.setNextExecutionTime(nextExecutionTime.getTime());
            }
        }
        return resultList;
    }

    private void addOrUpdateSwaggerImportCronJob(Schedule request) {
        scheduleService.addOrUpdateCronJob(request, SwaggerUrlImportJob.getJobKey(request.getResourceId()), SwaggerUrlImportJob.getTriggerKey(request.getResourceId()), SwaggerUrlImportJob.class);
    }

    public ApiExportResult export(ApiBatchRequest request, String type) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiDefinitionMapper.selectIds(query));
        LogUtil.info("导出总量 ", request.getIds());

        List<ApiDefinitionWithBLOBs> apiDefinitions = getByIds(request.getIds());

        if (StringUtils.equals(type, "MS")) {
            MsApiExportResult msApiExportResult = new MsApiExportResult();
            msApiExportResult.setData(apiDefinitions);
            msApiExportResult.setCases(apiTestCaseService.selectCasesBydApiIds(request.getIds()));
            msApiExportResult.setMocks(mockConfigService.selectMockExpectConfigByApiIdIn(request.getIds()));
            msApiExportResult.setProjectName(request.getProjectId());
            msApiExportResult.setProtocol(request.getProtocol());
            msApiExportResult.setProjectId(request.getProjectId());
            msApiExportResult.setVersion(System.getenv("MS_VERSION"));
            LogUtil.info("导出数据组装完成 ", request.getIds());
            return msApiExportResult;
        } else { //  导出为 Swagger 格式
            Swagger3Parser swagger3Parser = new Swagger3Parser();
            Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
            return swagger3Parser.swagger3Export(apiDefinitions, project);
        }
    }

    public List<ApiDefinition> selectEffectiveIdByProjectId(String projectId, String versionId) {
        return extApiDefinitionMapper.selectEffectiveIdByProjectId(projectId, versionId);
    }

    public List<ApiDefinition> selectEffectiveIdByProjectIdAndHaveNotCase(String projectId, String versionId) {
        return extApiDefinitionMapper.selectEffectiveIdByProjectIdAndHaveNotCase(projectId, versionId);
    }

    public List<ApiDefinitionWithBLOBs> preparedUrl(String projectId, String method, String baseUrlSuffix, String mockApiResourceId) {

        /*
         * 获取符合条件的api时，先检查请求头中自带的mockApiResourceId
         * mockApiResourceId在MsHttpSample拼装的过程中进行添加。初衷是在接口定义调试和接口用例执行时定位到设置了Mock期望的接口。 所以如果请求来源于第三方工具(如Postman等)则不保证该ID的必填性和正确性
         * 当这个ID能查出数据，则以该api为主。如果查不出来，按照之前匹配URL的方式继续处理。
         */
        List<ApiDefinitionWithBLOBs> specifyData = new ArrayList<>();
        if (StringUtils.isNotBlank(mockApiResourceId)) {
            ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
            apiDefinitionExample.createCriteria().andIdEqualTo(mockApiResourceId).andStatusNotEqualTo("Trash");
            List<ApiDefinitionWithBLOBs> apiDefinitions = apiDefinitionMapper.selectByExampleWithBLOBs(apiDefinitionExample);
            ApiDefinitionWithBLOBs apiDefinition;
            if (CollectionUtils.isNotEmpty(apiDefinitions)) {
                specifyData.add(apiDefinitions.get(0));
            } else {
                ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(mockApiResourceId);
                if (testCase != null) {
                    apiDefinition = apiDefinitionMapper.selectByPrimaryKey(testCase.getApiDefinitionId());
                    if (apiDefinition != null) {
                        specifyData.add(apiDefinition);
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(specifyData)) {
            return specifyData;
        } else {
            if (StringUtils.isEmpty(baseUrlSuffix)) {
                return new ArrayList<>();
            } else {
                String apiId = this.getApiIdFromMockApiResourceId(mockApiResourceId, projectId);
                ApiDefinitionExample example = new ApiDefinitionExample();
                ApiDefinitionExample.Criteria criteria = example.createCriteria();
                criteria.andMethodEqualTo(method).andProjectIdEqualTo(projectId).andStatusNotEqualTo("Trash").andProtocolEqualTo("HTTP").andLatestEqualTo(true);
                if (StringUtils.isNotBlank(apiId)) {
                    criteria.andIdEqualTo(apiId);
                }
                List<ApiDefinition> apiList = apiDefinitionMapper.selectByExample(example);
                List<String> apiIdList = new ArrayList<>();
                boolean urlSuffixEndEmpty = false;
                String urlSuffix = baseUrlSuffix;
                if (urlSuffix.endsWith("/")) {
                    urlSuffixEndEmpty = true;
                    urlSuffix = urlSuffix + "testMock";
                }
                String[] urlParams = urlSuffix.split("/");
                if (urlSuffixEndEmpty) {
                    urlParams[urlParams.length - 1] = StringUtils.EMPTY;
                }
                for (ApiDefinition api : apiList) {
                    if (StringUtils.equalsAny(api.getPath(), baseUrlSuffix, "/" + baseUrlSuffix)) {
                        apiIdList.add(api.getId());
                    } else {
                        String path = api.getPath();
                        if (StringUtils.isEmpty(path)) {
                            continue;
                        }
                        if (path.startsWith("/")) {
                            path = path.substring(1);
                        }
                        if (StringUtils.isNotEmpty(path)) {
                            String[] pathArr = path.split("/");
                            if (pathArr.length == urlParams.length) {
                                boolean isFetch = true;
                                for (int i = 0; i < urlParams.length; i++) {
                                    String pathItem = pathArr[i];
                                    if (!(pathItem.startsWith("{") && pathItem.endsWith("}"))) {
                                        if (!StringUtils.equals(pathArr[i], urlParams[i])) {
                                            isFetch = false;
                                            break;
                                        }
                                    }

                                }
                                if (isFetch) {
                                    apiIdList.add(api.getId());
                                }
                            }
                        }
                    }
                }
                if (apiIdList.isEmpty()) {
                    return new ArrayList<>();
                } else {
                    example.clear();
                    example.createCriteria().andIdIn(apiIdList);
                    return apiDefinitionMapper.selectByExampleWithBLOBs(example);
                }
            }
        }
    }

    private String getApiIdFromMockApiResourceId(String mockApiResourceId, String projectId) {
        String returnId = null;
        ApiDefinition apiDefinition = this.get(mockApiResourceId);
        if (apiDefinition == null) {
            ApiTestCase testCase = apiTestCaseMapper.selectByPrimaryKey(mockApiResourceId);
            if (testCase != null) {
                if (StringUtils.equals(testCase.getProjectId(), projectId) && !StringUtils.equals(testCase.getStatus(), "Trash")) {
                    returnId = testCase.getApiDefinitionId();
                }
            }
        } else {
            if (StringUtils.equals(apiDefinition.getProjectId(), projectId) && !StringUtils.equals(apiDefinition.getStatus(), "Trash") && apiDefinition.getLatest()) {
                returnId = mockApiResourceId;
            }
        }
        return returnId;
    }

    public String getLogDetails(String id) {
        ApiDefinitionWithBLOBs bloBs = apiDefinitionMapper.selectByPrimaryKey(id);
        if (bloBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, DefinitionReference.definitionColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), bloBs.getProjectId(), bloBs.getName(), bloBs.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(ids);
            List<ApiDefinition> definitions = apiDefinitionMapper.selectByExample(example);
            List<String> names = definitions.stream().map(ApiDefinition::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), definitions.get(0).getProjectId(), String.join(",", names), definitions.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(ApiBatchRequest request) {
        request.getCondition();
        if (CollectionUtils.isNotEmpty(request.getIds())) {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(request.getIds());
            List<ApiDefinition> definitions = apiDefinitionMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(definitions)) {
                List<DetailColumn> columns = new LinkedList<>();
                if (StringUtils.isNotEmpty(request.getMethod())) {
                    definitions.forEach(item -> {
                        DetailColumn column = new DetailColumn(DefinitionReference.definitionColumns.get("method"), "method", item.getMethod(), null);
                        columns.add(column);
                    });
                } else if (StringUtils.isNotEmpty(request.getStatus())) {
                    definitions.forEach(item -> {
                        DetailColumn column = new DetailColumn(DefinitionReference.definitionColumns.get("status"), "status", StatusReference.statusMap.get(item.getStatus()), null);
                        columns.add(column);
                    });
                } else if (StringUtils.isNotEmpty(request.getUserId())) {
                    definitions.forEach(item -> {
                        DetailColumn column = new DetailColumn(DefinitionReference.definitionColumns.get(CommonConstants.USER_ID), CommonConstants.USER_ID, item.getUserId(), null);
                        columns.add(column);
                    });
                }
                List<String> names = definitions.stream().map(ApiDefinition::getName).collect(Collectors.toList());
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(request.getIds()), request.getProjectId(), String.join(",", names), definitions.get(0).getCreateUser(), columns);
                return JSON.toJSONString(details);
            }
        }
        return null;
    }

    public String getLogDetails(ApiCaseRelevanceRequest request) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andApiDefinitionIdIn(request.getSelectIds());
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiTestCases)) {
            String projectId = apiTestCases.get(0).getProjectId();
            String creator = apiTestCases.get(0).getCreateUserId();
            List<String> names = apiTestCases.stream().map(ApiTestCase::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(request.getSelectIds()), projectId, String.join(",", names), creator, new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public ApiReportResult getTestPlanApiCaseReport(String testId, String type) {
        ApiDefinitionExecResultWithBLOBs result = extApiDefinitionExecResultMapper.selectPlanApiMaxResultByTestIdAndType(testId, type);
        return buildAPIReportResult(result);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, ApiDefinitionWithBLOBs.class, apiDefinitionMapper::selectByPrimaryKey, extApiDefinitionMapper::getPreOrder, extApiDefinitionMapper::getLastOrder, apiDefinitionMapper::updateByPrimaryKeySelective);
    }

    public ApiDefinitionResult getById(String id) {
        ApiDefinitionRequest request = new ApiDefinitionRequest();
        request.setId(id);
        List<ApiDefinitionResult> list = extApiDefinitionMapper.list(request);
        buildCustomField(list);
        ApiDefinitionResult result = null;
        if (CollectionUtils.isNotEmpty(list)) {
            result = list.get(0);
        }
        return result;
    }

    public List<ApiDefinitionResult> getApiByIds(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            List<ApiDefinitionResult> list = extApiDefinitionMapper.selectApiByIds(ids);
            buildCustomField(list);
            return list;
        }
        return new ArrayList<>();
    }


    public Map<String, List<ApiDefinition>> countEffectiveByProjectId(String projectId, String versionId) {
        if (StringUtils.isEmpty(projectId)) {
            return new HashMap<>();
        } else {
            List<ApiDefinition> apiDefinitionList = extApiDefinitionMapper.selectBaseInfoByProjectIDAndVersion(projectId, versionId);
            return apiDefinitionList.stream().collect(Collectors.groupingBy(ApiDefinition::getProtocol));
        }
    }

    public List<ApiDefinition> selectBaseInfoByProjectIdAndHasCase(String projectId, String versionId) {
        return extApiDefinitionMapper.selectBaseInfoByProjectIdAndHasCase(projectId, versionId);
    }

    public int getRelationshipCount(String id) {
        return relationshipEdgeService.getRelationshipCount(id, extApiDefinitionMapper::countByIds);
    }

    public List<RelationshipEdgeDTO> getRelationshipApi(String id, String relationshipType) {
        List<RelationshipEdge> relationshipEdges = relationshipEdgeService.getRelationshipEdgeByType(id, relationshipType);
        List<String> ids = relationshipEdgeService.getRelationIdsByType(relationshipType, relationshipEdges);

        if (CollectionUtils.isNotEmpty(ids)) {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(ids).andStatusNotEqualTo("Trash");
            List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
            Map<String, ApiDefinition> apiMap = apiDefinitions.stream().collect(Collectors.toMap(ApiDefinition::getId, i -> i));
            List<RelationshipEdgeDTO> results = new ArrayList<>();
            Map<String, String> userNameMap = ServiceUtils.getUserNameMap(apiDefinitions.stream().map(ApiDefinition::getUserId).collect(Collectors.toList()));
            for (RelationshipEdge relationshipEdge : relationshipEdges) {
                RelationshipEdgeDTO relationshipEdgeDTO = new RelationshipEdgeDTO();
                BeanUtils.copyBean(relationshipEdgeDTO, relationshipEdge);
                ApiDefinition apiDefinition;
                if (StringUtils.equals(relationshipType, "PRE")) {
                    apiDefinition = apiMap.get(relationshipEdge.getTargetId());
                } else {
                    apiDefinition = apiMap.get(relationshipEdge.getSourceId());
                }
                if (apiDefinition == null) {
                    continue;
                }
                relationshipEdgeDTO.setTargetName(apiDefinition.getName());
                relationshipEdgeDTO.setCreator(userNameMap.get(apiDefinition.getUserId()));
                relationshipEdgeDTO.setTargetNum(apiDefinition.getNum());
                relationshipEdgeDTO.setStatus(apiDefinition.getStatus());
                relationshipEdgeDTO.setVersionId(apiDefinition.getVersionId());
                results.add(relationshipEdgeDTO);
            }
            return results;
        }
        return new ArrayList<>();
    }

    public Pager<List<ApiDefinitionResult>> getRelationshipRelateList(ApiDefinitionRequest request, int goPage, @PathVariable int pageSize) {
        request = this.initRequest(request, true, true);
        // 排除同一个api的不同版本
        ApiDefinitionWithBLOBs currentApi = apiDefinitionMapper.selectByPrimaryKey(request.getId());
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andRefIdEqualTo(currentApi.getRefId());
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
        List<String> sameApiIds = apiDefinitions.stream().map(ApiDefinition::getId).collect(Collectors.toList());
        List<String> relationshipIds = relationshipEdgeService.getRelationshipIds(request.getId());
        sameApiIds.addAll(relationshipIds);
        request.setNotInIds(sameApiIds);
        request.setId(null); // 去掉id的查询条件
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, extApiDefinitionMapper.list(request));
    }

    public List<String> getFollows(String definitionId) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(definitionId)) {
            return result;
        }
        ApiDefinitionFollowExample example = new ApiDefinitionFollowExample();
        example.createCriteria().andDefinitionIdEqualTo(definitionId);
        List<ApiDefinitionFollow> follows = apiDefinitionFollowMapper.selectByExample(example);
        return follows.stream().map(ApiDefinitionFollow::getFollowId).distinct().collect(Collectors.toList());
    }

    public List<DocumentElement> getDocument(String id, String type) {
        ApiDefinitionWithBLOBs bloBs = apiDefinitionMapper.selectByPrimaryKey(id);
        List<DocumentElement> elements = new LinkedList<>();
        if (bloBs != null && StringUtils.isNotEmpty(bloBs.getResponse())) {
            JSONObject object = JSONUtil.parseObject(bloBs.getResponse());
            JSONObject body = (JSONObject) object.get(BODY);
            if (body != null) {
                String raw = body.optString("raw");
                String dataType = body.optString(PropertyConstant.TYPE);
                if ((StringUtils.isNotEmpty(raw) || StringUtils.isNotEmpty(body.optString("jsonSchema"))) && StringUtils.isNotEmpty(dataType)) {
                    if (StringUtils.equals(type, "JSON")) {
                        String format = body.optString("format");
                        if (StringUtils.equals(format, "JSON-SCHEMA") && StringUtils.isNotEmpty(body.optString("jsonSchema"))) {
                            elements = JSONSchemaToDocumentUtil.getDocument(body.optString("jsonSchema"));
                        } else {
                            elements = JSONToDocumentUtil.getDocument(raw, dataType);
                        }
                    } else if (StringUtils.equals(dataType, "XML")) {
                        elements = JSONToDocumentUtil.getDocument(raw, type);
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(elements)) {
            elements.add(new DocumentElement().newRoot(PropertyConstant.ROOT, null));
        }
        return elements;
    }

    public List<ApiDefinitionResult> getApiDefinitionVersions(String definitionId) {
        ApiDefinitionWithBLOBs definition = apiDefinitionMapper.selectByPrimaryKey(definitionId);
        if (definition == null) {
            return new ArrayList<>();
        }
        ApiDefinitionRequest request = new ApiDefinitionRequest();
        request.setRefId(definition.getRefId());
        return this.list(request);
    }

    public ApiDefinitionResult getApiDefinitionByVersion(String refId, String versionId) {
        ApiDefinitionRequest request = new ApiDefinitionRequest();
        request.setRefId(refId);
        request.setVersionId(versionId);
        List<ApiDefinitionResult> list = this.list(request);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public void deleteApiDefinitionByVersion(String refId, String version) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andRefIdEqualTo(refId).andVersionIdEqualTo(version);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
        List<String> ids = apiDefinitions.stream().map(ApiDefinition::getId).collect(Collectors.toList());

        ApiTestCaseExample apiTestCaseExample = new ApiTestCaseExample();
        apiTestCaseExample.createCriteria().andApiDefinitionIdIn(ids);
        apiTestCaseMapper.deleteByExample(apiTestCaseExample);
        //
        apiDefinitionMapper.deleteByExample(example);
        checkAndSetLatestVersion(refId);
    }

    public List<ApiDefinitionWithBLOBs> getByIds(List<String> ids) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(ids);
        return apiDefinitionMapper.selectByExampleWithBLOBs(example);
    }

    public void batchCopy(ApiBatchRequest request) {
        //检查测试项目是否开启了url可重复
        baseProjectService.checkProjectIsRepeatable(request.getProjectId());
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiDefinitionMapper.selectIds(query));
        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) return;
        List<ApiDefinitionWithBLOBs> apis = getByIds(ids);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDefinitionMapper mapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        long nextOrder = ServiceUtils.getNextOrder(request.getProjectId(), extApiDefinitionMapper::getLastOrder);

        int nextNum = getNextNum(request.getProjectId());

        try {
            for (int i = 0; i < apis.size(); i++) {
                ApiDefinitionWithBLOBs api = apis.get(i);
                String sourceId = api.getId();
                api.setId(UUID.randomUUID().toString());
                api.setName(ServiceUtils.getCopyName(api.getName()));
                api.setModuleId(request.getModuleId());
                api.setModulePath(request.getModulePath());
                api.setOrder(nextOrder += ServiceUtils.ORDER_STEP);
                api.setNum(nextNum++);
                api.setCreateTime(System.currentTimeMillis());
                api.setUpdateTime(System.currentTimeMillis());
                api.setRefId(api.getId());
                // 检查附件复制出附件
                ApiFileUtil.copyBodyFiles(sourceId, api.getId());

                List<CustomFieldResourceDTO> byResourceId = customFieldApiService.getByResourceId(sourceId);
                if (CollectionUtils.isNotEmpty(byResourceId)) {
                    customFieldApiService.addFields(api.getId(), byResourceId);
                }
                mapper.insert(api);
                if (i % 50 == 0) sqlSession.flushStatements();
            }
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public ApiDefinition getApiDefinition(ApiDefinitionExample apiDefinitionExample) {
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(apiDefinitionExample);
        if (apiDefinitions == null || apiDefinitions.size() == 0) {
            return null;
        }
        return apiDefinitions.get(0);
    }

    public void initModulePathAndId(String projectId, ApiDefinitionWithBLOBs test) {
        ApiModuleExample example = new ApiModuleExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andProtocolEqualTo(test.getProtocol()).andNameEqualTo(ProjectModuleDefaultNodeEnum.API_MODULE_DEFAULT_NODE.getNodeName()).andLevelEqualTo(1);
        List<ApiModule> modules = apiModuleMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(modules)) {
            test.setModuleId(modules.get(0).getId());
            test.setModulePath("/未规划接口");
        }
    }

    public void saveRelationshipBatch(ApiDefinitionRelationshipEdgeRequest request) {
        List<String> relationshipIds = relationshipEdgeService.getRelationshipIds(request.getId());
        request.getCondition().setNotInIds(relationshipIds);
        request.getCondition().setId(null);
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiDefinitionMapper.selectIds(query));
        List<String> ids = request.getIds();
        ids.remove(request.getId());
        if (CollectionUtils.isNotEmpty(ids)) {
            if (CollectionUtils.isNotEmpty(request.getTargetIds())) {
                request.setTargetIds(ids);
            } else if (CollectionUtils.isNotEmpty(request.getSourceIds())) {
                request.setSourceIds(ids);
            }
            relationshipEdgeService.saveBatch(request);
        }
    }

    public void deleteFollows(List<String> definitionIds) {
        if (CollectionUtils.isNotEmpty(definitionIds)) {
            ApiDefinitionFollowExample example = new ApiDefinitionFollowExample();
            example.createCriteria().andDefinitionIdIn(definitionIds);
            apiDefinitionFollowMapper.deleteByExample(example);
        }
    }

    public Map<String, List<ApiDefinition>> selectApiBaseInfoGroupByModuleId(String projectId, String protocol, String versionId, String status) {
        List<ApiDefinition> apiList = extApiDefinitionMapper.selectApiBaseInfoByProjectIdAndProtocolAndStatus(projectId, protocol, versionId, status);
        return apiList.stream().collect(Collectors.groupingBy(ApiDefinition::getModuleId));
    }

    public Map<String, List<ApiDefinition>> selectApiBaseInfoGroupByModuleId(String projectId, String protocol, String versionId, String status, ApiDefinitionRequest request) {
        List<ApiDefinition> apiList = extApiDefinitionMapper.selectApiBaseInfoByCondition(projectId, protocol, versionId, status, request);
        return apiList.stream().collect(Collectors.groupingBy(ApiDefinition::getModuleId));
    }

    /**
     * 将模块删除了的接口模块改为默认模块
     *
     * @param projectId
     * @param protocol
     * @param status
     * @param versionId
     * @param id
     */
    public void updateNoModuleApiToDefaultModule(String projectId, String protocol, String status, String versionId, String id) {
        if (StringUtils.isNoneEmpty(projectId, protocol, id)) {
            extApiDefinitionMapper.updateNoModuleApiToDefaultModule(projectId, protocol, status, versionId, id);
        }
    }

    public Integer getCitedScenarioCount(String testId) {
        ApiScenarioReferenceIdExample apiScenarioReferenceIdExample = new ApiScenarioReferenceIdExample();
        apiScenarioReferenceIdExample.createCriteria().andDataTypeEqualTo(ReportTriggerMode.API.name()).andReferenceTypeEqualTo(COPY).andReferenceIdEqualTo(testId);
        List<ApiScenarioReferenceId> apiScenarioReferenceIds = apiScenarioReferenceIdMapper.selectByExample(apiScenarioReferenceIdExample);
        return apiScenarioReferenceIds.size();
    }

    /**
     * 测试执行
     *
     * @param request
     * @param bodyFiles
     * @return
     */
    public MsExecResponseDTO run(RunDefinitionRequest request, List<MultipartFile> bodyFiles) {
        if (request.getConfig() == null) {
            request.setConfig(new RunModeConfigDTO());
        }

        if (StringUtils.isNotBlank(request.getEnvironmentGroupId())) {
            request.setEnvironmentMap(environmentGroupProjectService.getEnvMap(request.getEnvironmentGroupId()));
        }
        // 验证是否本地执行
        jMeterService.verifyPool(request.getProjectId(), request.getConfig());
        if (!request.isDebug()) {
            String testId = request.getTestElement() != null && CollectionUtils.isNotEmpty(request.getTestElement().getHashTree()) && CollectionUtils.isNotEmpty(request.getTestElement().getHashTree().get(0).getHashTree()) ? request.getTestElement().getHashTree().get(0).getHashTree().get(0).getName() : request.getId();
            String reportName = this.getReportNameByTestId(testId);
            ApiDefinitionExecResultWithBLOBs result = ApiDefinitionExecResultUtil.add(testId,
                    ApiReportStatus.RUNNING.name(), request.getId(),
                    Objects.requireNonNull(SessionUtils.getUser()).getId());
            result.setName(reportName);
            result.setProjectId(request.getProjectId());
            result.setTriggerMode(TriggerMode.MANUAL.name());
            if (StringUtils.isNotEmpty(request.getEnvironmentId())) {
                RunModeConfigDTO runModeConfigDTO = new RunModeConfigDTO();
                runModeConfigDTO.setEnvMap(new HashMap<>() {{
                    this.put(request.getProjectId(), request.getEnvironmentId());
                }});
                runModeConfigDTO.setResourcePoolId(request.getConfig().getResourcePoolId());
                result.setEnvConfig(JSON.toJSONString(runModeConfigDTO));
            }
            result.setActuator(request.getConfig().getResourcePoolId());
            apiCaseResultService.batchSave(result);

        }
        if (request.isEditCaseRequest() && CollectionUtils.isNotEmpty(request.getTestElement().getHashTree()) && CollectionUtils.isNotEmpty(request.getTestElement().getHashTree().get(0).getHashTree())) {
            ApiTestCaseWithBLOBs record = new ApiTestCaseWithBLOBs();
            record.setRequest(JSON.toJSONString(request.getTestElement().getHashTree().get(0).getHashTree().get(0)));
            record.setId(request.getTestElement().getHashTree().get(0).getHashTree().get(0).getName());
            apiTestCaseMapper.updateByPrimaryKeySelective(record);
        }
        return apiExecuteService.debug(request, bodyFiles);
    }

    private String getReportNameByTestId(String testId) {
        String testName = extApiDefinitionMapper.selectNameById(testId);
        if (StringUtils.isEmpty(testName)) {
            testName = extApiTestCaseMapper.selectNameById(testId);
        }
        return testName;
    }

    public List<TcpTreeTableDataStruct> rawToXml(String rawData) {
        return TcpTreeTableDataParser.xml2TreeTableData(rawData);
    }

    public List<DubboProvider> getProviders(RegistryCenter registry) {
        ProviderService providerService = ProviderService.get(registry.getAddress());
        List<String> providers = providerService.getProviders(registry.getProtocol(), registry.getAddress(), registry.getGroup());
        List<DubboProvider> list = new ArrayList<>();
        providers.forEach(p -> {
            DubboProvider provider = new DubboProvider();
            String[] info = p.split(":");
            if (info.length > 1) {
                provider.setVersion(info[1]);
            }
            provider.setService(p);
            provider.setServiceInterface(info[0]);
            Map<String, URL> services = providerService.findByService(p);
            if (services != null && !services.isEmpty() && !CollectionUtils.isEmpty(services.values())) {
                String parameter = services.values().stream().findFirst().get().getParameter(CommonConstants.METHODS_KEY);
                if (StringUtils.isNotBlank(parameter)) {
                    String[] methods = parameter.split(",");
                    provider.setMethods(Arrays.asList(methods));
                } else {
                    provider.setMethods(new ArrayList<>());
                }
            } else {
                provider.setMethods(new ArrayList<>());
            }
            list.add(provider);
        });
        return list;
    }

    public Map<String, List<String>> selectEnvironmentByHashTree(String projectId, MsTestElement testElement) {
        Map<String, List<String>> projectEnvMap = new HashMap<>();
        if (testElement != null) {
            List<String> envIdList = this.getEnvIdByHashTree(testElement);
            projectEnvMap.put(projectId, envIdList);
        }
        return projectEnvMap;
    }

    private List<String> getEnvIdByHashTree(MsTestElement testElement) {
        List<String> envIdList = new ArrayList<>();
        if (testElement instanceof MsHTTPSamplerProxy) {
            String envId = ((MsHTTPSamplerProxy) testElement).getUseEnvironment();
            if (StringUtils.isNotEmpty(envId)) {
                envIdList.add(envId);
            }
        } else if (testElement instanceof MsTCPSampler) {
            String envId = ((MsTCPSampler) testElement).getUseEnvironment();
            if (StringUtils.isNotEmpty(envId)) {
                envIdList.add(envId);
            }
        } else if (testElement instanceof MsJDBCSampler) {
            String envId = ((MsJDBCSampler) testElement).getUseEnvironment();
            if (StringUtils.isNotEmpty(envId)) {
                envIdList.add(envId);
            }
        } else if (CollectionUtils.isNotEmpty(testElement.getHashTree())) {
            for (MsTestElement child : testElement.getHashTree()) {
                List<String> childEnvId = this.getEnvIdByHashTree(child);
                childEnvId.forEach(envId -> {
                    if (StringUtils.isNotEmpty(envId) && !envIdList.contains(envId)) {
                        envIdList.add(envId);
                    }
                });
            }
        }
        return envIdList;
    }


    public Map<String, List<String>> getProjectEnvNameByEnvConfig(Map<String, List<String>> projectEnvConfigMap) {
        Map<String, List<String>> returnMap = new HashMap<>();
        if (MapUtils.isNotEmpty(projectEnvConfigMap)) {
            for (Map.Entry<String, List<String>> entry : projectEnvConfigMap.entrySet()) {
                String projectId = entry.getKey();
                List<String> configList = entry.getValue();
                Project project = baseProjectService.getProjectById(projectId);
                List<String> envIdList = new ArrayList<>();
                configList.forEach(envConfig -> {
                    RunModeConfigDTO runModeConfigDTO = null;
                    try {
                        runModeConfigDTO = JSON.parseObject(envConfig, RunModeConfigDTO.class);
                    } catch (Exception e) {
                        LogUtil.error("解析" + envConfig + "为RunModeConfigDTO时失败！", e);
                    }

                    if (StringUtils.isNotEmpty(projectId) && runModeConfigDTO != null && MapUtils.isNotEmpty(runModeConfigDTO.getEnvMap())) {
                        String envId = runModeConfigDTO.getEnvMap().get(projectId);
                        if (!envIdList.contains(envId)) {
                            envIdList.add(envId);
                        }
                    }
                });
                String projectName = project == null ? null : project.getName();
                if (StringUtils.isNotEmpty(projectName) && CollectionUtils.isNotEmpty(envIdList)) {
                    List<String> envNameList = apiTestEnvironmentService.selectNameByIdList(envIdList);
                    returnMap.put(projectName, envNameList);
                }
            }
        }
        return returnMap;
    }

    public List<ApiDefinition> selectApiDefinitionBydIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(ids);
        return apiDefinitionMapper.selectByExample(example);
    }

    public void updateFileMetadataId(List<ReplaceFileIdRequest> requestList) {
        if (CollectionUtils.isNotEmpty(requestList)) {
            requestList.forEach(request -> {
                if (CollectionUtils.isNotEmpty(request.getApiIdList())) {
                    ApiDefinitionExample example = new ApiDefinitionExample();
                    example.createCriteria().andIdIn(request.getApiIdList());
                    List<ApiDefinitionWithBLOBs> apiDefinitionWithBLOBsList = apiDefinitionMapper.selectByExampleWithBLOBs(example);
                    apiDefinitionWithBLOBsList.forEach(apiDefinitionWithBLOBs -> {
                        String requestStr = apiDefinitionWithBLOBs.getRequest();
                        requestStr = StringUtils.replace(requestStr, request.getOldFileMetadataId(), request.getNewFileMetadataId());
                        apiDefinitionWithBLOBs.setRequest(requestStr);
                        apiDefinitionMapper.updateByPrimaryKeyWithBLOBs(apiDefinitionWithBLOBs);
                    });
                }
                if (CollectionUtils.isNotEmpty(request.getApiTestCaseIdList())) {
                    ApiTestCaseExample example = new ApiTestCaseExample();
                    example.createCriteria().andIdIn(request.getApiTestCaseIdList());
                    List<ApiTestCaseWithBLOBs> apiTestCaseWithBLOBsList = apiTestCaseMapper.selectByExampleWithBLOBs(example);
                    apiTestCaseWithBLOBsList.forEach(testCaseWithBLOBs -> {
                        String requestStr = testCaseWithBLOBs.getRequest();
                        requestStr = StringUtils.replace(requestStr, request.getOldFileMetadataId(), request.getNewFileMetadataId());
                        testCaseWithBLOBs.setRequest(requestStr);
                        apiTestCaseMapper.updateByPrimaryKeyWithBLOBs(testCaseWithBLOBs);
                    });
                }

                if (CollectionUtils.isNotEmpty(request.getApiScenarioIdList())) {
                    ApiScenarioExample example = new ApiScenarioExample();
                    example.createCriteria().andIdIn(request.getApiScenarioIdList());
                    List<ApiScenarioWithBLOBs> apiScenarioWithBLOBsList = apiScenarioMapper.selectByExampleWithBLOBs(example);
                    apiScenarioWithBLOBsList.forEach(apiScenarioWithBLOBs -> {
                        String requestStr = apiScenarioWithBLOBs.getScenarioDefinition();
                        requestStr = StringUtils.replace(requestStr, request.getOldFileMetadataId(), request.getNewFileMetadataId());
                        apiScenarioWithBLOBs.setScenarioDefinition(requestStr);
                        apiScenarioMapper.updateByPrimaryKeyWithBLOBs(apiScenarioWithBLOBs);
                    });
                }
            });
        }
    }

    public List<BaseCase> getBaseCaseByProjectId(String projectId) {
        return extApiTestCaseMapper.selectBaseCaseByProjectId(projectId);
    }

    @Async
    public void setProjectIdInExecutionInfo() {
        long lastCount = 0;
        long allSourceIdCount = apiExecutionInfoService.countSourceIdByProjectIdIsNull();
        //分批进行查询更新处理
        int pageSize = 1000;
        while (allSourceIdCount > 0) {
            if (allSourceIdCount == lastCount) {
                //数据无法再更新时跳出循环
                break;
            } else {
                lastCount = allSourceIdCount;
            }
            PageHelper.startPage(0, pageSize, false);
            List<String> sourceIdAboutProjectIdIsNull = apiExecutionInfoService.selectSourceIdByProjectIdIsNull();
            PageHelper.clearPage();
            //批量更新
            for (String apiId : sourceIdAboutProjectIdIsNull) {
                ApiDefinition apiDefinition = this.get(apiId);
                if (apiDefinition != null) {
                    apiExecutionInfoService.updateProjectIdByApiIdAndProjectIdIsNull(apiDefinition.getProjectId(), ExecutionExecuteTypeEnum.BASIC.name(), apiDefinition.getVersionId(), apiId);
                } else {
                    apiExecutionInfoService.deleteBySourceIdAndProjectIdIsNull(apiId);
                }
            }
            allSourceIdCount = apiExecutionInfoService.countSourceIdByProjectIdIsNull();
        }
    }

    public void copyCaseOrMockByVersion(BatchDataCopyRequest request) {
        if (!request.isCopyCase() && !request.isCopyMock()) {
            return;
        }
        if (request.getCondition() == null) {
            return;
        }
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiDefinitionMapper.selectIds(query));
        //        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiDefinitionMapper.selectIds(query));

        if (StringUtils.isBlank(request.getVersionId()) || CollectionUtils.isEmpty(request.getCondition().getIds())) {
            MSException.throwException(Translator.get("invalid_parameter"));
        }
        request.setIds(request.getCondition().getIds());

        //函数是批量操作，可能会出现数据太多导致的sql过长错误，采用批量处理
        BatchProcessingUtil.batchProcessingByDataCopy(request, this::batchCopyCaseOrMockByVersion);

    }

    public void batchCopyCaseOrMockByVersion(BatchDataCopyRequest request) {

        if (sqlSessionFactory != null && CollectionUtils.isNotEmpty(request.getIds())) {
            Map<String, ApiDefinition> refIdMap = new HashMap<>();
            List<ApiDefinition> apiDefinitionList = this.selectByIds(request.getIds());
            apiDefinitionList.forEach(item -> {
                //过滤到自身的引用
                if (!StringUtils.equals(item.getVersionId(), request.getVersionId())) {
                    refIdMap.put(item.getRefId(), item);
                }
            });
            if (MapUtils.isNotEmpty(refIdMap)) {
                ApiDefinitionExample apiExample = new ApiDefinitionExample();
                apiExample.createCriteria().andStatusNotEqualTo(ApiTestDataStatus.TRASH.getValue()).andRefIdIn(new ArrayList<>(refIdMap.keySet())).andVersionIdEqualTo(request.getVersionId());
                List<ApiDefinition> versionApiList = apiDefinitionMapper.selectByExample(apiExample);
                Map<String, String> sourceApiIdRefIdMap = new HashMap<>();
                versionApiList.forEach(item -> sourceApiIdRefIdMap.put(item.getId(), item.getRefId()));
                if (MapUtils.isEmpty(sourceApiIdRefIdMap)) {
                    return;
                }
                SqlSession batchSqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
                if (request.isCopyCase()) {
                    this.copyCaseByVersion(request.getIds(), sourceApiIdRefIdMap, refIdMap, batchSqlSession);
                }
                if (request.isCopyMock()) {
                    this.copyMockByVersion(sourceApiIdRefIdMap, refIdMap, batchSqlSession);
                }
                batchSqlSession.flushStatements();
                if (sqlSessionFactory != null) {
                    SqlSessionUtils.closeSqlSession(batchSqlSession, sqlSessionFactory);
                }
            }
        }
    }

    private void copyMockByVersion(Map<String, String> sourceApiIdRefIdMap, Map<String, ApiDefinition> refIdMap, SqlSession batchSqlSession) {
        long timeStamp = System.currentTimeMillis();
        MockConfigExample mockConfigExample = new MockConfigExample();
        mockConfigExample.createCriteria().andApiIdIn(new ArrayList<>(sourceApiIdRefIdMap.keySet()));
        List<MockConfig> sourceMockConfigList = mockConfigMapper.selectByExample(mockConfigExample);
        if (CollectionUtils.isNotEmpty(sourceMockConfigList)) {
            List<String> sourceMockConfigIdList = sourceMockConfigList.stream().map(MockConfig::getId).collect(Collectors.toList());
            MockExpectConfigExample mockExpectConfigExample = new MockExpectConfigExample();
            mockExpectConfigExample.createCriteria().andMockConfigIdIn(sourceMockConfigIdList);
            List<MockExpectConfigWithBLOBs> mockExpectConfigWithBLOBsList = mockExpectConfigMapper.selectByExampleWithBLOBs(mockExpectConfigExample);
            Map<String, List<MockExpectConfigWithBLOBs>> sourceMockConfigIdMap = mockExpectConfigWithBLOBsList.stream().collect(Collectors.groupingBy(MockExpectConfigWithBLOBs::getMockConfigId));

            List<MockConfig> saveMockList = new ArrayList<>();

            List<MockExpectConfigWithBLOBs> saveMockExpectList = new ArrayList<>();
            List<MockExpectConfigWithBLOBs> updateMockExpectList = new ArrayList<>();

            sourceMockConfigList.forEach(item -> {
                String sourceApiId = item.getApiId();
                String refId = sourceApiIdRefIdMap.get(sourceApiId);
                if (StringUtils.isNotBlank(refId)) {
                    ApiDefinition goalApi = refIdMap.get(refId);
                    if (goalApi != null) {
                        MockConfig goalApiMockConfig = mockConfigService.selectMockConfigByApiId(goalApi.getId());
                        String goalApiMockConfigId = UUID.randomUUID().toString();

                        Map<String, MockExpectConfig> oldMockExpectConfig = new HashMap<>();
                        //已经存储的mock期望编号
                        List<String> saveExpectNumList = new ArrayList<>();

                        if (goalApiMockConfig == null) {
                            MockConfig mockConfig = new MockConfig();
                            BeanUtils.copyBean(mockConfig, item);
                            mockConfig.setApiId(goalApi.getId());
                            mockConfig.setId(goalApiMockConfigId);
                            mockConfig.setCreateTime(timeStamp);
                            mockConfig.setUpdateTime(timeStamp);
                            saveMockList.add(mockConfig);
                        } else {
                            goalApiMockConfigId = goalApiMockConfig.getId();
                            saveExpectNumList = mockConfigService.selectExpectNumberByConfigId(goalApiMockConfigId);
                            List<MockExpectConfig> goalMockExpectList = mockConfigService.selectSimpleMockExpectConfigByMockConfigId(goalApiMockConfigId);
                            goalMockExpectList.forEach(mockExpectConfig -> {
                                oldMockExpectConfig.put(StringUtils.trim(mockExpectConfig.getName()), mockExpectConfig);
                            });
                        }
                        List<MockExpectConfigWithBLOBs> mockExpectConfigList = sourceMockConfigIdMap.get(item.getId());
                        if (CollectionUtils.isNotEmpty(mockExpectConfigList)) {
                            String finalMockConfigId = goalApiMockConfigId;
                            List<String> finalSaveExpectNumList = saveExpectNumList;
                            mockExpectConfigList.forEach(mockExpectConfigWithBLOBs -> {
                                MockExpectConfig oldExpect = oldMockExpectConfig.get(StringUtils.trim(mockExpectConfigWithBLOBs.getName()));
                                MockExpectConfigWithBLOBs expectConfigWithBLOBs = new MockExpectConfigWithBLOBs();
                                BeanUtils.copyBean(expectConfigWithBLOBs, mockExpectConfigWithBLOBs);
                                expectConfigWithBLOBs.setMockConfigId(finalMockConfigId);
                                expectConfigWithBLOBs.setUpdateTime(timeStamp);
                                if (oldExpect == null) {
                                    String newMockExpectNum = mockConfigService.getMockExpectId(String.valueOf(goalApi.getNum()), finalSaveExpectNumList);
                                    finalSaveExpectNumList.add(newMockExpectNum);

                                    expectConfigWithBLOBs.setId(UUID.randomUUID().toString());
                                    expectConfigWithBLOBs.setExpectNum(newMockExpectNum);
                                    expectConfigWithBLOBs.setCreateTime(timeStamp);

                                    saveMockExpectList.add(expectConfigWithBLOBs);
                                } else {
                                    expectConfigWithBLOBs.setId(oldExpect.getId());
                                    expectConfigWithBLOBs.setCreateTime(oldExpect.getCreateTime());
                                    updateMockExpectList.add(expectConfigWithBLOBs);
                                }
                            });
                        }
                    }
                }
            });
            if (CollectionUtils.isNotEmpty(saveMockList)) {
                MockConfigMapper mockConfigBatchMapper = batchSqlSession.getMapper(MockConfigMapper.class);
                saveMockList.forEach(mockConfigBatchMapper::insert);
            }
            MockExpectConfigMapper mockExpectConfigBatchMapper = batchSqlSession.getMapper(MockExpectConfigMapper.class);
            if (CollectionUtils.isNotEmpty(saveMockExpectList)) {
                saveMockExpectList.forEach(mockExpectConfigBatchMapper::insert);
            }
            if (CollectionUtils.isNotEmpty(updateMockExpectList)) {
                updateMockExpectList.forEach(mockExpectConfigBatchMapper::updateByPrimaryKeyWithBLOBs);
            }
        }
    }

    private void copyCaseByVersion(List<String> chooseApiIdList, Map<String, String> sourceApiIdRefIdMap, Map<String, ApiDefinition> refIdMap, SqlSession batchSqlSession) {
        long timeStamp = System.currentTimeMillis();
        List<ApiTestCaseWithBLOBs> sourceApiCaseList = apiTestCaseService.selectCasesBydApiIds(new ArrayList<>(sourceApiIdRefIdMap.keySet()));
        List<ApiTestCaseWithBLOBs> caseInChooseApi = apiTestCaseService.selectCasesBydApiIds(chooseApiIdList);
        Map<String, Map<String, ApiTestCase>> apiIdOldCaseMap = new HashMap<>();
        caseInChooseApi.forEach(item -> {
            String caseName = StringUtils.trim(item.getName());
            if (StringUtils.isNotBlank(caseName)) {
                if (apiIdOldCaseMap.containsKey(item.getApiDefinitionId())) {
                    apiIdOldCaseMap.get(item.getApiDefinitionId()).put(caseName, item);
                } else {
                    apiIdOldCaseMap.put(item.getApiDefinitionId(), new HashMap<>() {{
                        this.put(caseName, item);
                    }});
                }
            }
        });
        List<ApiTestCaseWithBLOBs> saveCaseList = new ArrayList<>();
        List<ApiTestCaseWithBLOBs> updateCaseList = new ArrayList<>();
        Map<String, Integer> lastCaseNumMap = new LinkedHashMap<>();
        //用例文件关联关系数据 <要复制的用例ID， 生成的用例ID>
        Map<String, String> forceOverrideFileMap = new HashMap<>();
        sourceApiCaseList.forEach(item -> {
            String sourceApiId = item.getApiDefinitionId();
            String refId = sourceApiIdRefIdMap.get(sourceApiId);
            if (StringUtils.isNotBlank(refId)) {
                ApiDefinition api = refIdMap.get(refId);
                if (api != null) {
                    //通过用例名称检查是否需要覆盖
                    ApiTestCase oldCase = null;
                    if (apiIdOldCaseMap.containsKey(api.getId())) {
                        oldCase = apiIdOldCaseMap.get(api.getId()).get(StringUtils.trim(item.getName()));
                    }
                    ApiTestCaseWithBLOBs newCase = new ApiTestCaseWithBLOBs();
                    BeanUtils.copyBean(newCase, item);
                    newCase.setApiDefinitionId(api.getId());
                    newCase.setVersionId(api.getVersionId());
                    if (oldCase == null) {
                        int lastCaseNum = 0;
                        if (lastCaseNumMap.containsKey(api.getId())) {
                            lastCaseNum = lastCaseNumMap.get(api.getId());
                        } else {
                            lastCaseNum = apiTestCaseService.getNextNum(api.getId());
                        }
                        int caseNum = apiTestCaseService.getNextNum(lastCaseNum);
                        newCase.setNum(caseNum);
                        newCase.setId(UUID.randomUUID().toString());
                        newCase.setCreateTime(timeStamp);
                        newCase.setUpdateTime(timeStamp);

                        lastCaseNumMap.put(api.getId(), caseNum);
                        saveCaseList.add(newCase);
                    } else {
                        newCase.setId(oldCase.getId());
                        newCase.setNum(oldCase.getNum());
                        newCase.setCreateTime(oldCase.getCreateTime());
                        newCase.setUpdateTime(timeStamp);
                        updateCaseList.add(newCase);
                    }

                    forceOverrideFileMap.put(item.getId(), newCase.getId());
                    //本地文件覆盖
                    FileUtils.forceOverrideBodyFiles(item.getId(), newCase.getId());
                }
            }
        });
        FileAssociationMapper batchFileAssociationMapper = batchSqlSession.getMapper(FileAssociationMapper.class);
        extFileAssociationService.forceOverrideFileAssociation(forceOverrideFileMap, batchFileAssociationMapper);

        ApiTestCaseMapper apiTestCaseBatchMapper = batchSqlSession.getMapper(ApiTestCaseMapper.class);
        if (CollectionUtils.isNotEmpty(saveCaseList)) {
            saveCaseList.forEach(apiTestCaseBatchMapper::insert);
        }
        if (CollectionUtils.isNotEmpty(updateCaseList)) {
            updateCaseList.forEach(apiTestCaseBatchMapper::updateByPrimaryKeyWithBLOBs);
        }
    }

    public List<ApiDefinition> getAPiNotInCollection(Map<String, List<ApiDefinition>> protocolAllDefinitionMap, List<ApiDefinition> checkCollection) {
        List<ApiDefinition> returnList = new ArrayList<>();
        if (MapUtils.isEmpty(protocolAllDefinitionMap)) {
            return new ArrayList<>();
        }
        List<String> idInCheckCollection = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(checkCollection)) {
            idInCheckCollection = checkCollection.stream().map(ApiDefinition::getId).collect(Collectors.toList());
        }
        for (List<ApiDefinition> apiList : protocolAllDefinitionMap.values()) {
            for (ApiDefinition api : apiList) {
                if (!idInCheckCollection.contains(api.getId())) {
                    returnList.add(api);
                }
            }
        }
        return returnList;
    }

    public Map<String, List<ApiDefinition>> getUnCoverageApiMap(List<ApiDefinition> apiNoCaseList, List<String> apiIdInScenario) {
        Map<String, List<ApiDefinition>> returnMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(apiNoCaseList)) {
            if (apiIdInScenario == null) {
                apiIdInScenario = new ArrayList<>();
            }
            for (ApiDefinition api : apiNoCaseList) {
                if (!apiIdInScenario.contains(api.getId())) {
                    if (returnMap.containsKey(api.getProtocol())) {
                        returnMap.get(api.getProtocol()).add(api);
                    } else {
                        returnMap.put(api.getProtocol(), new ArrayList<>() {{
                            this.add(api);
                        }});
                    }
                }
            }
        }
        return returnMap;
    }

    public Map<String, List<ApiDefinition>> filterMap(Map<String, List<ApiDefinition>> targetMap, Map<String, List<ApiDefinition>> filter) {
        Map<String, List<ApiDefinition>> returnMap = new HashMap<>();
        if (MapUtils.isNotEmpty(targetMap)) {
            for (Map.Entry<String, List<ApiDefinition>> entry : targetMap.entrySet()) {
                List<ApiDefinition> filterList = filter.get(entry.getKey());
                List<ApiDefinition> resultList = null;
                if (CollectionUtils.isNotEmpty(filterList)) {
                    resultList = new ArrayList<>(CollectionUtils.subtract(entry.getValue(), filterList));
                } else {
                    resultList = entry.getValue();
                }
                returnMap.put(entry.getKey(), resultList);
            }
        }
        return returnMap;
    }
}
