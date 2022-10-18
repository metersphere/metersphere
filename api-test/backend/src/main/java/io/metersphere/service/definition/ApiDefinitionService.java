package io.metersphere.service.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.github.ningyu.jmeter.plugin.dubbo.sample.ProviderService;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.automation.ReferenceDTO;
import io.metersphere.api.dto.automation.TcpTreeTableDataStruct;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.assertions.document.DocumentElement;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsJDBCSampler;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.dto.mock.config.MockConfigImportDTO;
import io.metersphere.api.dto.swaggerurl.SwaggerTaskResult;
import io.metersphere.api.dto.swaggerurl.SwaggerUrlRequest;
import io.metersphere.api.exec.api.ApiExecuteService;
import io.metersphere.api.parse.ApiImportParser;
import io.metersphere.api.parse.api.ApiDefinitionImport;
import io.metersphere.api.parse.api.ApiDefinitionImportParserFactory;
import io.metersphere.api.parse.api.Swagger3Parser;
import io.metersphere.api.parse.scenario.TcpTreeTableDataParser;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.enums.ApiTestDataStatus;
import io.metersphere.commons.enums.FileAssociationTypeEnums;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.*;
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
import io.metersphere.request.ApiSyncCaseRequest;
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
import io.metersphere.xpack.quota.service.QuotaService;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.json.JSONObject;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.util.*;
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
    private EsbApiParamService esbApiParamService;
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
    private EsbApiParamsMapper esbApiParamsMapper;
    @Resource
    private ApiExecutionInfoService apiExecutionInfoService;
    @Resource
    private ApiCaseExecutionInfoService apiCaseExecutionInfoService;
    @Lazy
    @Resource
    private ApiModuleService apiModuleService;
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
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ApiCustomFieldService customFieldApiService;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;


    private final ThreadLocal<Long> currentApiOrder = new ThreadLocal<>();
    private final ThreadLocal<Long> currentApiCaseOrder = new ThreadLocal<>();
    private static final String COPY = "Copy";
    private static final String SCHEDULE = "schedule";

    public List<ApiDefinitionResult> list(ApiDefinitionRequest request) {
        request = this.initRequest(request, true, true);
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

    /**
     * 工作台获取待应用管理设置的更新的条件
     * @param request
     */
    public void getApplicationUpdateRule(ApiDefinitionRequest request){
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

    public List<ApiDefinitionResult> weekList(ApiDefinitionRequest request) {
        //获取7天之前的日期
        Date startDay = DateUtils.dateSum(new Date(), -6);
        //将日期转化为 00:00:00 的时间戳
        Date startTime = null;
        try {
            startTime = DateUtils.getDayStartTime(startDay);
        } catch (Exception e) {
        }
        if (startTime == null) {
            return new ArrayList<>(0);
        } else {
            request = this.initRequest(request, true, true);
            List<ApiDefinitionResult> resList = extApiDefinitionMapper.weekList(request, startTime.getTime());
            calculateResult(resList, request.getProjectId());
            calculateResultSce(resList);
            resList.stream().forEach(item -> item.setApiType("api"));
            return resList;
        }
    }

    public void initDefaultModuleId() {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andModuleIdIsNull();
        List<ApiDefinition> updateApiList = apiDefinitionMapper.selectByExample(example);
        Map<String, Map<String, List<ApiDefinition>>> projectIdMap = new HashMap<>();
        for (ApiDefinition api : updateApiList) {
            String projectId = api.getProjectId();
            String protocol = api.getProtocol();
            if (projectIdMap.containsKey(projectId)) {
                if (projectIdMap.get(projectId).containsKey(protocol)) {
                    projectIdMap.get(projectId).get(protocol).add(api);
                } else {
                    List<ApiDefinition> list = new ArrayList<>();
                    list.add(api);
                    projectIdMap.get(projectId).put(protocol, list);
                }
            } else {
                List<ApiDefinition> list = new ArrayList<>();
                list.add(api);
                Map<String, List<ApiDefinition>> map = new HashMap<>();
                map.put(protocol, list);
                projectIdMap.put(projectId, map);
            }
        }
        for (Map.Entry<String, Map<String, List<ApiDefinition>>> entry : projectIdMap.entrySet()) {
            String projectId = entry.getKey();
            Map<String, List<ApiDefinition>> map = entry.getValue();

            for (Map.Entry<String, List<ApiDefinition>> itemEntry : map.entrySet()) {
                String protocol = itemEntry.getKey();
                ApiModule node = apiModuleService.getDefaultNodeUnCreateNew(projectId, protocol);
                if (node != null) {
                    List<ApiDefinition> testCaseList = itemEntry.getValue();
                    for (ApiDefinition apiDefinition : testCaseList) {
                        ApiDefinitionWithBLOBs updateCase = new ApiDefinitionWithBLOBs();
                        updateCase.setId(apiDefinition.getId());
                        updateCase.setModuleId(node.getId());
                        updateCase.setModulePath("/" + node.getName());

                        apiDefinitionMapper.updateByPrimaryKeySelective(updateCase);
                    }
                }
            }
        }
    }

    public List<ApiDefinitionResult> listBatch(ApiBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extApiDefinitionMapper.selectIds(query));
        if (CollectionUtils.isEmpty(request.getIds())) {
            return new ArrayList<>();
        }
        List<ApiDefinitionResult> resList = extApiDefinitionMapper.listByIds(request.getIds());
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
    public ApiDefinitionRequest checkFilterHasCoverage(ApiDefinitionRequest request) {
        if (StringUtils.isNotEmpty(request.getProjectId())) {
            List<ApiDefinition> definitionList = null;
            if (StringUtils.equalsAnyIgnoreCase(request.getApiCoverage(), "uncoverage", "coverage")) {
                //计算没有用例接口的覆盖数量
                definitionList = this.selectEffectiveIdByProjectIdAndHaveNotCase(request.getProjectId());
            }
            if (StringUtils.equalsAnyIgnoreCase(request.getScenarioCoverage(), "uncoverage", "coverage")) {
                //计算全部用例
                definitionList = this.selectEffectiveIdByProjectId(request.getProjectId());
            }

            if (CollectionUtils.isNotEmpty(definitionList)) {
                //如果查询条件中有未覆盖/已覆盖， 则需要解析出没有用例的接口中，有多少是符合场景覆盖规律的。然后将这些接口的id作为查询参数
                Map<String, Map<String, String>> scenarioUrlList = apiAutomationService.selectScenarioUseUrlByProjectId(request.getProjectId());
                List<String> apiIdInScenario = apiAutomationService.getApiIdInScenario(request.getProjectId(), scenarioUrlList, definitionList);
                if (CollectionUtils.isNotEmpty(apiIdInScenario)) {
                    request.setCoverageIds(apiIdInScenario);
                }
            }
        }
        return request;
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
            FileUtils.copyBodyFiles(request.getSourceId(), request.getId());
        } else {
            FileUtils.createBodyFiles(request.getRequest().getId(), bodyFiles);
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
        FileUtils.createBodyFiles(request.getRequest().getId(), bodyFiles);
        String context = SessionUtils.getUserId().concat(Translator.get("update_api")).concat(":").concat(returnModel.getName());
        Map<String, Object> paramMap = new HashMap<>();
        getParamMap(paramMap, returnModel.getProjectId(), SessionUtils.getUserId(), returnModel.getId(), returnModel.getName(), returnModel.getCreateUser());
        paramMap.put("userId", returnModel.getUserId());
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
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        if (quotaService != null) {
            quotaService.checkAPIDefinitionQuota(projectId);
        }
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
            apiExecutionInfoService.deleteByApiId(api.getId());
            esbApiParamService.deleteByResourceId(api.getId());
            MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
            mockConfigService.deleteMockConfigByApiId(api.getId());
            // 删除自定义字段关联关系
            customFieldApiService.deleteByResourceId(api.getId());
            // 删除关系图
            relationshipEdgeService.delete(api.getId());
            FileUtils.deleteBodyFiles(api.getId());
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
        esbApiParamService.deleteByResourceIdIn(apiIds);
        apiExecutionInfoService.deleteByApiIdList(apiIds);
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
        ApiDefinitionExample  apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andIdIn(apiIds);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(apiDefinitionExample);
        if (CollectionUtils.isEmpty(apiDefinitions)){
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
        if (StringUtils.equals(request.getMethod(), "ESB")) {
            //ESB的接口类型数据，采用TCP方式去发送。并将方法类型改为TCP。 并修改发送数据
            request = esbApiParamService.handleEsbRequest(request);
        } else if (StringUtils.equals(request.getMethod(), "TCP")) {
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
        checkAndSetLatestVersion(result.getRefId());

        // 存储附件关系
        extFileAssociationService.saveApi(test.getId(), request.getRequest(), FileAssociationTypeEnums.API.name());
        //保存自定义字段
        customFieldApiService.editFields(test.getId(), request.getEditFields());
        customFieldApiService.addFields(test.getId(), request.getAddFields());
        return result;
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
        checkNameExist(request, false);
        if (StringUtils.equals(request.getMethod(), "ESB")) {
            //ESB的接口类型数据，采用TCP方式去发送。并将方法类型改为TCP。 并修改发送数据
            request = esbApiParamService.handleEsbRequest(request);
        }
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
        if (StringUtils.isEmpty(request.getModuleId()) || "default-module".equals(request.getModuleId())) {
            initModulePathAndId(test.getProjectId(), test);
        }
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

    private ApiImportSendNoticeDTO importCreate(ApiDefinitionMapper batchMapper, ExtApiDefinitionMapper extApiDefinitionMapper, ApiTestCaseMapper apiTestCaseMapper, ApiDefinitionImportParamDTO apiDefinitionImportParamDTO) {
        ApiImportSendNoticeDTO apiImportSendNoticeDTO = new ApiImportSendNoticeDTO();
        SaveApiDefinitionRequest saveReq = new SaveApiDefinitionRequest();
        ApiDefinitionWithBLOBs apiDefinition = apiDefinitionImportParamDTO.getApiDefinition();
        BeanUtils.copyBean(saveReq, apiDefinition);

        if (StringUtils.isEmpty(apiDefinition.getStatus())) {
            apiDefinition.setStatus(ApiTestDataStatus.UNDERWAY.getValue());
        }
        if (apiDefinition.getUserId() == null) {
            apiDefinition.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        }

        apiDefinition.setDescription(apiDefinition.getDescription());
        List<ApiDefinitionWithBLOBs> collect = apiDefinitionImportParamDTO.getUpdateList().stream().filter(t -> t.getId().equals(apiDefinition.getId())).collect(Collectors.toList());
        apiDefinitionImportParamDTO.setUpdateList(collect);
        ApiTestImportRequest apiTestImportRequest = apiDefinitionImportParamDTO.getApiTestImportRequest();
        List<MockConfigImportDTO> mocks = apiDefinitionImportParamDTO.getMocks();
        List<ApiTestCaseWithBLOBs> caseList = apiDefinitionImportParamDTO.getCaseList();
        if (StringUtils.equals("fullCoverage", apiTestImportRequest.getModeId())) {
            return _importCreate(batchMapper, extApiDefinitionMapper, apiTestCaseMapper, apiDefinitionImportParamDTO);
        } else if (StringUtils.equals("incrementalMerge", apiTestImportRequest.getModeId())) {
            if (CollectionUtils.isEmpty(collect)) {
                String originId = apiDefinition.getId();
                apiDefinition.setId(UUID.randomUUID().toString());
                apiDefinition.setCreateTime(System.currentTimeMillis());
                apiDefinition.setUpdateTime(System.currentTimeMillis());
                //postman 可能含有前置脚本，接口定义去掉脚本
                if (apiDefinition.getVersionId() != null && apiDefinition.getVersionId().equals("new")) {
                    apiDefinition.setLatest(apiTestImportRequest.getVersionId().equals(apiTestImportRequest.getDefaultVersion()));
                } else {
                    apiDefinition.setOrder(getImportNextOrder(apiTestImportRequest.getProjectId()));
                    apiDefinition.setRefId(apiDefinition.getId());
                    apiDefinition.setLatest(true); // 新增的接口 latest = true
                }
                if (StringUtils.isNotEmpty(apiTestImportRequest.getVersionId())) {
                    apiDefinition.setVersionId(apiTestImportRequest.getVersionId());
                } else {
                    apiDefinition.setVersionId(apiTestImportRequest.getDefaultVersion());
                }
                boolean newCreate = !StringUtils.equals(ApiImportPlatform.Swagger2.name(), apiDefinitionImportParamDTO.getApiTestImportRequest().getPlatform())
                        && !StringUtils.isNotBlank(apiDefinitionImportParamDTO.getApiTestImportRequest().getSwaggerUrl())
                        && !StringUtils.equals("idea", apiDefinitionImportParamDTO.getApiTestImportRequest().getOrigin());
                caseList = setRequestAndAddNewCase(apiDefinition, caseList, newCreate);
                reSetImportMocksApiId(mocks, originId, apiDefinition.getId(), apiDefinition.getNum());
                batchMapper.insert(apiDefinition);
                List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, apiTestCaseMapper, caseList);
                apiImportSendNoticeDTO.setCaseDTOList(apiTestCaseDTOS);
                extApiDefinitionMapper.clearLatestVersion(apiDefinition.getRefId());
                extApiDefinitionMapper.addLatestVersion(apiDefinition.getRefId());
                ApiDefinitionResult apiDefinitionResult = getApiDefinitionResult(apiDefinition, false);
                apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
                return apiImportSendNoticeDTO;
            } else {
                //不覆盖的接口，如果没有sameRequest则不导入。此时清空mock信息
                mocks.clear();
                return null;
            }
        } else {
            return _importCreate(batchMapper, extApiDefinitionMapper, apiTestCaseMapper, apiDefinitionImportParamDTO);
        }
    }

    private ApiDefinitionResult getApiDefinitionResult(ApiDefinitionWithBLOBs apiDefinition, boolean isUpdate) {
        ApiDefinitionResult apiDefinitionResult = new ApiDefinitionResult();
        BeanUtils.copyBean(apiDefinitionResult, apiDefinition);
        apiDefinitionResult.setUpdated(isUpdate);
        return apiDefinitionResult;
    }

    private List<ApiTestCaseDTO> importCase(ApiDefinitionWithBLOBs apiDefinition, ApiTestCaseMapper apiTestCaseMapper, List<ApiTestCaseWithBLOBs> caseList) {
        if (CollectionUtils.isEmpty(caseList)) {
            return new ArrayList<>();
        }
        List<ApiTestCaseDTO> apiTestCaseDTOS = new ArrayList<>();
        for (int i = 0; i < caseList.size(); i++) {
            ApiTestCaseDTO apiTestCaseDTO = new ApiTestCaseDTO();
            ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = caseList.get(i);
            apiTestCaseWithBLOBs.setApiDefinitionId(apiDefinition.getId());
            apiTestCaseWithBLOBs.setToBeUpdated(apiDefinition.getToBeUpdated() != null && apiDefinition.getToBeUpdated() && StringUtils.equalsIgnoreCase(apiTestCaseWithBLOBs.getVersionId(), "old_case"));
            apiTestCaseWithBLOBs.setVersionId(apiDefinition.getVersionId());
            if (apiTestCaseWithBLOBs.getCreateTime() == null) {
                apiTestCaseWithBLOBs.setCreateTime(System.currentTimeMillis());
            }
            apiTestCaseWithBLOBs.setUpdateTime(System.currentTimeMillis());

            if (StringUtils.isBlank(apiTestCaseWithBLOBs.getCaseStatus())) {
                apiTestCaseWithBLOBs.setCaseStatus(ApiTestDataStatus.PREPARE.getValue());
            }
            if (StringUtils.isBlank(apiTestCaseWithBLOBs.getCreateUserId())) {
                apiTestCaseWithBLOBs.setCreateUserId(apiDefinition.getUserId());
            }
            if (apiTestCaseWithBLOBs.getOrder() == null) {
                apiTestCaseWithBLOBs.setOrder(getImportNextCaseOrder(apiDefinition.getProjectId()));
            }
            if (apiTestCaseWithBLOBs.getNum() == null) {
                apiTestCaseWithBLOBs.setNum(apiTestCaseService.getNextNum(apiDefinition.getId(), apiDefinition.getNum() + i, apiDefinition.getProjectId()));
            }

            if (apiDefinition.getToBeUpdateTime() != null) {
                apiTestCaseWithBLOBs.setToBeUpdateTime(apiDefinition.getToBeUpdateTime());
            }

            if (StringUtils.isBlank(apiTestCaseWithBLOBs.getPriority())) {
                apiTestCaseWithBLOBs.setPriority("P0");
            }
            apiTestCaseWithBLOBs.setStatus(StringUtils.EMPTY);

            if (StringUtils.isNotBlank(apiTestCaseWithBLOBs.getId())) {
                BeanUtils.copyBean(apiTestCaseDTO, apiTestCaseWithBLOBs);
                apiTestCaseDTO.setUpdated(true);
                apiTestCaseMapper.updateByPrimaryKeyWithBLOBs(apiTestCaseWithBLOBs);
            } else {
                apiTestCaseWithBLOBs.setId(UUID.randomUUID().toString());
                apiTestCaseWithBLOBs.setCreateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
                apiTestCaseWithBLOBs.setUpdateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
                BeanUtils.copyBean(apiTestCaseDTO, apiTestCaseWithBLOBs);
                apiTestCaseDTO.setUpdated(false);
                apiTestCaseMapper.insert(apiTestCaseWithBLOBs);
            }
            apiTestCaseDTOS.add(apiTestCaseDTO);
        }
        return apiTestCaseDTOS;
    }


    public Long getImportNextOrder(String projectId) {
        Long order = currentApiOrder.get();
        if (order == null) {
            order = ServiceUtils.getNextOrder(projectId, extApiDefinitionMapper::getLastOrder);
        }
        order = (order == null ? 0 : order) + ServiceUtils.ORDER_STEP;
        currentApiOrder.set(order);
        return order;
    }

    public Long getImportNextCaseOrder(String projectId) {
        Long order = currentApiCaseOrder.get();
        if (order == null) {
            order = ServiceUtils.getNextOrder(projectId, extApiTestCaseMapper::getLastOrder);
        }
        order = order + ServiceUtils.ORDER_STEP;
        currentApiCaseOrder.set(order);
        return order;
    }

    private ApiImportSendNoticeDTO _importCreate(ApiDefinitionMapper batchMapper, ExtApiDefinitionMapper extApiDefinitionMapper, ApiTestCaseMapper apiTestCaseMapper, ApiDefinitionImportParamDTO apiDefinitionImportParamDTO) {
        ApiDefinitionWithBLOBs apiDefinition = apiDefinitionImportParamDTO.getApiDefinition();
        ApiTestImportRequest apiTestImportRequest = apiDefinitionImportParamDTO.getApiTestImportRequest();
        List<ApiDefinitionWithBLOBs> sameRequest = apiDefinitionImportParamDTO.getUpdateList();
        List<MockConfigImportDTO> mocks = apiDefinitionImportParamDTO.getMocks();
        List<ApiTestCaseWithBLOBs> caseList = apiDefinitionImportParamDTO.getCaseList();
        String originId = apiDefinition.getId();
        ApiImportSendNoticeDTO apiImportSendNoticeDTO = new ApiImportSendNoticeDTO();
        if (CollectionUtils.isEmpty(sameRequest)) { // 没有这个接口 新增
            apiDefinition.setId(UUID.randomUUID().toString());

            apiDefinition.setCreateTime(System.currentTimeMillis());
            apiDefinition.setUpdateTime(System.currentTimeMillis());
            if (apiDefinition.getVersionId() != null && apiDefinition.getVersionId().equals("update")) {
                if (StringUtils.isNotEmpty(apiTestImportRequest.getUpdateVersionId())) {
                    apiDefinition.setVersionId(apiTestImportRequest.getUpdateVersionId());
                } else {
                    apiDefinition.setVersionId(apiTestImportRequest.getDefaultVersion());
                }
                apiDefinition.setLatest(apiTestImportRequest.getVersionId().equals(apiTestImportRequest.getDefaultVersion()));
            } else {
                apiDefinition.setRefId(apiDefinition.getId());
                apiDefinition.setLatest(true); // 新增接口 latest = true
                apiDefinition.setOrder(getImportNextOrder(apiTestImportRequest.getProjectId()));
                if (StringUtils.isNotEmpty(apiTestImportRequest.getVersionId())) {
                    apiDefinition.setVersionId(apiTestImportRequest.getVersionId());
                } else {
                    apiDefinition.setVersionId(apiTestImportRequest.getDefaultVersion());
                }
            }

            reSetImportMocksApiId(mocks, originId, apiDefinition.getId(), apiDefinition.getNum());
            boolean newCreate = !StringUtils.equals(ApiImportPlatform.Swagger2.name(), apiDefinitionImportParamDTO.getApiTestImportRequest().getPlatform())
                    && !StringUtils.isNotBlank(apiDefinitionImportParamDTO.getApiTestImportRequest().getSwaggerUrl())
                    && !StringUtils.equals("idea", apiDefinitionImportParamDTO.getApiTestImportRequest().getOrigin());
            caseList = setRequestAndAddNewCase(apiDefinition, caseList, newCreate);
            batchMapper.insert(apiDefinition);
            ApiDefinitionResult apiDefinitionResult = getApiDefinitionResult(apiDefinition, false);
            apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
            List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, apiTestCaseMapper, caseList);
            apiImportSendNoticeDTO.setCaseDTOList(apiTestCaseDTOS);
        } else { //如果存在则修改
            if (StringUtils.isEmpty(apiTestImportRequest.getUpdateVersionId())) {
                apiTestImportRequest.setUpdateVersionId(apiTestImportRequest.getDefaultVersion());
            }
            Optional<ApiDefinitionWithBLOBs> apiOp = sameRequest.stream().filter(api -> StringUtils.equals(api.getVersionId(), apiTestImportRequest.getUpdateVersionId())).findFirst();

            if (!apiOp.isPresent()) {
                apiDefinition.setId(UUID.randomUUID().toString());
                apiDefinition.setCreateTime(System.currentTimeMillis());
                apiDefinition.setUpdateTime(System.currentTimeMillis());
                if (!apiDefinition.getVersionId().equals("update")) {
                    if (sameRequest.get(0).getRefId() != null) {
                        apiDefinition.setRefId(sameRequest.get(0).getRefId());
                    } else {
                        apiDefinition.setRefId(apiDefinition.getId());
                    }
                    apiDefinition.setNum(sameRequest.get(0).getNum()); // 使用第一个num当作本次的num
                    apiDefinition.setOrder(sameRequest.get(0).getOrder());
                }
                apiDefinition.setLatest(apiTestImportRequest.getVersionId().equals(apiTestImportRequest.getDefaultVersion()));
                apiDefinition.setVersionId(apiTestImportRequest.getUpdateVersionId());

                if (sameRequest.get(0).getUserId() != null) {
                    apiDefinition.setUserId(sameRequest.get(0).getUserId());
                }
                batchMapper.insert(apiDefinition);
                ApiDefinitionResult apiDefinitionResult = getApiDefinitionResult(apiDefinition, false);
                apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
                List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, apiTestCaseMapper, caseList);
                apiImportSendNoticeDTO.setCaseDTOList(apiTestCaseDTOS);
            } else {
                ApiDefinitionWithBLOBs existApi = apiOp.get();
                apiDefinition.setStatus(existApi.getStatus());
                apiDefinition.setOriginalState(existApi.getOriginalState());
                apiDefinition.setCaseStatus(existApi.getCaseStatus());
                apiDefinition.setNum(existApi.getNum()); //id 不变
                if (existApi.getRefId() != null) {
                    apiDefinition.setRefId(existApi.getRefId());
                } else {
                    apiDefinition.setRefId(apiDefinition.getId());
                }
                apiDefinition.setVersionId(apiTestImportRequest.getUpdateVersionId());
                if (existApi.getUserId() != null) {
                    apiDefinition.setUserId(existApi.getUserId());
                }
                //Check whether the content has changed, if not, do not change the creation time
                if (StringUtils.equalsIgnoreCase(apiDefinition.getProtocol(), RequestTypeConstants.HTTP)) {
                    Boolean toChangeTime = checkIsSynchronize(existApi, apiDefinition);
                    if (toChangeTime) {
                        apiDefinition.setUpdateTime(System.currentTimeMillis());
                    } else if (apiTestImportRequest.getCoverModule() != null && apiTestImportRequest.getCoverModule()) {
                        apiDefinition.setUpdateTime(System.currentTimeMillis());
                    }
                    if (CollectionUtils.isEmpty(caseList)) {
                        apiDefinition.setToBeUpdated(false);
                    } else {
                        List<ApiTestCaseWithBLOBs> oldCaseList = caseList.stream().filter(t -> StringUtils.equalsIgnoreCase("old_case", t.getVersionId()) && StringUtils.isNotBlank(t.getId())).collect(Collectors.toList());
                        if (CollectionUtils.isEmpty(oldCaseList)) {
                            apiDefinition.setToBeUpdated(false);
                        }
                    }
                } else {
                    apiDefinition.setUpdateTime(System.currentTimeMillis());
                }

                if (!StringUtils.equalsIgnoreCase(apiTestImportRequest.getPlatform(), ApiImportPlatform.Metersphere.name())) {
                    apiDefinition.setTags(existApi.getTags()); // 其他格式 tag 不变，MS 格式替换
                }
                apiDefinition.setId(existApi.getId());
                setRequestAndAddNewCase(apiDefinition, caseList, false);
                apiDefinition.setOrder(existApi.getOrder());
                reSetImportMocksApiId(mocks, originId, apiDefinition.getId(), apiDefinition.getNum());
                batchMapper.updateByPrimaryKeyWithBLOBs(apiDefinition);
                ApiDefinitionResult apiDefinitionResult = getApiDefinitionResult(apiDefinition, true);
                apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
                List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, apiTestCaseMapper, caseList);
                apiImportSendNoticeDTO.setCaseDTOList(apiTestCaseDTOS);
            }
        }
        extApiDefinitionMapper.clearLatestVersion(apiDefinition.getRefId());
        extApiDefinitionMapper.addLatestVersion(apiDefinition.getRefId());
        return apiImportSendNoticeDTO;
    }

    private List<ApiTestCaseWithBLOBs> setRequestAndAddNewCase(ApiDefinitionWithBLOBs apiDefinition, List<ApiTestCaseWithBLOBs> caseList, boolean newCreate) {
        boolean createCase = false;
        if (StringUtils.equalsIgnoreCase(apiDefinition.getProtocol(), RequestTypeConstants.HTTP)) {
            createCase = setImportHashTree(apiDefinition);
        } else if (StringUtils.equalsIgnoreCase(apiDefinition.getProtocol(), RequestTypeConstants.TCP)) {
            createCase = setImportTCPHashTree(apiDefinition);
        }
        if (newCreate && createCase && CollectionUtils.isEmpty(caseList)) {
            ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = addNewCase(apiDefinition);
            caseList = new ArrayList<>();
            caseList.add(apiTestCaseWithBLOBs);
        }
        return caseList;
    }


    public void sendImportApiCreateNotice(ApiDefinitionWithBLOBs apiDefinitionWithBLOBs) {
        String context = SessionUtils.getUserId().concat(Translator.get("create_api")).concat(":").concat(apiDefinitionWithBLOBs.getName());
        Map<String, Object> paramMap = new HashMap<>();
        getParamMap(paramMap, apiDefinitionWithBLOBs.getProjectId(), SessionUtils.getUserId(), apiDefinitionWithBLOBs.getId(), apiDefinitionWithBLOBs.getName(), apiDefinitionWithBLOBs.getCreateUser());
        paramMap.put("userId", apiDefinitionWithBLOBs.getUserId());
        NoticeModel noticeModel = NoticeModel.builder().operator(SessionUtils.getUserId()).context(context).testId(apiDefinitionWithBLOBs.getId()).subject(Translator.get("api_create_notice")).paramMap(paramMap).excludeSelf(true).event(NoticeConstants.Event.CREATE).build();
        noticeSendService.send(NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }

    public void sendImportApiUpdateNotice(ApiDefinitionWithBLOBs apiDefinitionWithBLOBs) {
        String context = SessionUtils.getUserId().concat(Translator.get("update_api")).concat(":").concat(apiDefinitionWithBLOBs.getName());
        Map<String, Object> paramMap = new HashMap<>();
        getParamMap(paramMap, apiDefinitionWithBLOBs.getProjectId(), SessionUtils.getUserId(), apiDefinitionWithBLOBs.getId(), apiDefinitionWithBLOBs.getName(), apiDefinitionWithBLOBs.getCreateUser());
        paramMap.put("userId", apiDefinitionWithBLOBs.getUserId());
        NoticeModel noticeModel = NoticeModel.builder().operator(SessionUtils.getUserId()).context(context).testId(apiDefinitionWithBLOBs.getId()).subject(Translator.get("api_update_notice")).paramMap(paramMap).excludeSelf(true).event(NoticeConstants.Event.UPDATE).build();
        noticeSendService.send(NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }

    public void sendImportCaseUpdateNotice(ApiTestCase apiTestCase) {
        String context = SessionUtils.getUserId().concat(Translator.get("update_api_case")).concat(":").concat(apiTestCase.getName());
        Map<String, Object> paramMap = new HashMap<>();
        getParamMap(paramMap, apiTestCase.getProjectId(), SessionUtils.getUserId(), apiTestCase.getId(), apiTestCase.getName(), apiTestCase.getCreateUserId());
        NoticeModel noticeModel = NoticeModel.builder().operator(SessionUtils.getUserId()).context(context).testId(apiTestCase.getId()).subject(Translator.get("api_case_update_notice")).paramMap(paramMap).excludeSelf(true).event(NoticeConstants.Event.CASE_UPDATE).build();
        noticeSendService.send(NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }

    public void sendImportCaseCreateNotice(ApiTestCase apiTestCase) {
        String context = SessionUtils.getUserId().concat(Translator.get("create_api_case")).concat(":").concat(apiTestCase.getName());
        Map<String, Object> paramMap = new HashMap<>();
        getParamMap(paramMap, apiTestCase.getProjectId(), SessionUtils.getUserId(), apiTestCase.getId(), apiTestCase.getName(), apiTestCase.getCreateUserId());
        NoticeModel noticeModel = NoticeModel.builder().operator(SessionUtils.getUserId()).context(context).testId(apiTestCase.getId()).subject(Translator.get("api_case_create_notice")).paramMap(paramMap).excludeSelf(true).event(NoticeConstants.Event.CASE_CREATE).build();
        noticeSendService.send(NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }

    private void getParamMap(Map<String, Object> paramMap, String projectId, String userId, String id, String name, String createUser) {
        paramMap.put("projectId", projectId);
        paramMap.put("operator", userId);
        paramMap.put("id", id);
        paramMap.put("name", name);
        paramMap.put("createUser", createUser);
    }

    public Boolean checkIsSynchronize(ApiDefinitionWithBLOBs existApi, ApiDefinitionWithBLOBs apiDefinition) {

        ApiDefinition exApi;
        ApiDefinition api;
        exApi = existApi;
        api = apiDefinition;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String exApiString = null;
        String apiString = null;
        try {
            exApiString = objectMapper.writeValueAsString(exApi);
            apiString = objectMapper.writeValueAsString(api);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        ApiSyncCaseRequest apiSyncCaseRequest = new ApiSyncCaseRequest();
        Boolean toUpdate = false;
        ApiDefinitionSyncService apiDefinitionSyncService = CommonBeanFactory.getBean(ApiDefinitionSyncService.class);
        if (apiDefinitionSyncService != null) {
            toUpdate = apiDefinitionSyncService.getProjectApplications(existApi.getProjectId());
            apiSyncCaseRequest = apiDefinitionSyncService.getApiSyncCaseRequest(existApi.getProjectId());
        }
        //Compare the basic information of the API. If it contains the comparison that needs to be done for the synchronization information,
        // put the data into the to-be-synchronized
        if (!StringUtils.equals(exApiString, apiString)) {
            if (!StringUtils.equals(apiDefinition.getMethod(), existApi.getMethod())) {
                if (apiSyncCaseRequest.getMethod() && toUpdate) {
                    apiDefinition.setToBeUpdated(true);
                    apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
                }
                return true;
            }
            if (!StringUtils.equals(apiDefinition.getProtocol(), existApi.getProtocol())) {
                if (apiSyncCaseRequest.getProtocol() && toUpdate) {
                    apiDefinition.setToBeUpdated(true);
                    apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
                }
                return true;
            }

            if (!StringUtils.equals(apiDefinition.getPath(), existApi.getPath())) {
                if (apiSyncCaseRequest.getPath() && toUpdate) {
                    apiDefinition.setToBeUpdated(true);
                    apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
                }
                return true;
            }
        }

        JsonNode exApiRequest = null;
        JsonNode apiRequest = null;
        try {
            exApiRequest = objectMapper.readTree(existApi.getRequest());
            apiRequest = objectMapper.readTree(apiDefinition.getRequest());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (exApiRequest == null || apiRequest == null) {
            return exApiRequest != null || apiRequest != null;
        }

        if (exApiRequest.get("headers") != null && apiRequest.get("headers") != null) {
            if (!StringUtils.equals(exApiRequest.get("headers").toString(), apiRequest.get("headers").toString())) {
                if (apiSyncCaseRequest.getHeaders() && toUpdate) {
                    apiDefinition.setToBeUpdated(true);
                    apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
                }
                return true;
            }
        }

        if (exApiRequest.get("arguments") != null && apiRequest.get("arguments") != null) {
            if (!StringUtils.equals(exApiRequest.get("arguments").toString(), apiRequest.get("arguments").toString())) {
                if (apiSyncCaseRequest.getQuery() && toUpdate) {
                    apiDefinition.setToBeUpdated(true);
                    apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
                }
                return true;
            }
        }

        if (exApiRequest.get("rest") != null && apiRequest.get("rest") != null) {
            if (!StringUtils.equals(exApiRequest.get("rest").toString(), apiRequest.get("rest").toString())) {
                if (apiSyncCaseRequest.getRest() && toUpdate) {
                    apiDefinition.setToBeUpdated(true);
                    apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
                }
                return true;
            }
        }

        if (exApiRequest.get("body") != null && apiRequest.get("body") != null) {
            if (!StringUtils.equals(exApiRequest.get("body").toString(), apiRequest.get("body").toString())) {
                if (apiSyncCaseRequest.getBody() && toUpdate) {
                    apiDefinition.setToBeUpdated(true);
                    apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
                }
                return true;
            }
        }

        if (!StringUtils.equals(apiDefinition.getCreateUser(), existApi.getCreateUser())) {
            return true;
        }

        if (!StringUtils.equals(apiDefinition.getStatus(), existApi.getStatus()) && StringUtils.isNotBlank(existApi.getStatus()) && StringUtils.isNotBlank(apiDefinition.getStatus())) {
            return true;
        }

        if (!StringUtils.equals(apiDefinition.getTags(), existApi.getTags())) {
            if (apiDefinition.getTags() != null && Objects.equals(apiDefinition.getTags(), StringUtils.EMPTY) && existApi.getTags() != null && Objects.equals(existApi.getTags(), StringUtils.EMPTY)) {
                return true;
            }
        }

        if (!StringUtils.equals(existApi.getRemark(), apiDefinition.getRemark()) && StringUtils.isNotBlank(existApi.getRemark()) && StringUtils.isNotBlank(apiDefinition.getRemark())) {
            return true;
        }

        if (!StringUtils.equals(existApi.getDescription(), apiDefinition.getDescription()) && StringUtils.isNotBlank(existApi.getDescription()) && StringUtils.isNotBlank(apiDefinition.getDescription())) {
            return true;
        }

        JsonNode exApiResponse = null;
        JsonNode apiResponse = null;

        if (StringUtils.isBlank(apiDefinition.getResponse()) || StringUtils.isBlank(existApi.getResponse())) {
            return !StringUtils.isBlank(apiDefinition.getResponse()) || !StringUtils.isBlank(existApi.getResponse());
        }

        try {
            exApiResponse = objectMapper.readTree(existApi.getResponse());
            apiResponse = objectMapper.readTree(apiDefinition.getResponse());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (exApiResponse == null || apiResponse == null) {
            return exApiResponse != null || apiResponse != null;
        }

        if (exApiResponse.get("headers") != null && apiResponse.get("headers") != null) {
            if (!StringUtils.equals(exApiResponse.get("headers").toString(), apiResponse.get("headers").toString())) {
                return true;
            }
        }

        if (exApiResponse.get(PropertyConstant.TYPE) != null && apiResponse.get(PropertyConstant.TYPE) != null) {
            if (!StringUtils.equals(exApiResponse.get(PropertyConstant.TYPE).toString(), apiResponse.get(PropertyConstant.TYPE).toString())) {
                return true;
            }
        }

        if (exApiResponse.get("name") != null && apiResponse.get("name") != null) {
            if (!StringUtils.equals(exApiResponse.get("name").toString(), apiResponse.get("name").toString())) {
                return true;
            }
        }

        if (exApiResponse.get("body") != null && apiResponse.get("body") != null) {
            if (!StringUtils.equals(exApiResponse.get("body").toString(), apiResponse.get("body").toString())) {
                return true;
            }
        }

        if (exApiResponse.get("statusCode") != null && apiResponse.get("statusCode") != null) {
            if (!StringUtils.equals(exApiResponse.get("statusCode").toString(), apiResponse.get("statusCode").toString())) {
                return true;
            }
        }

        if (exApiResponse.get("enable") != null && apiResponse.get("enable") != null) {
            return !StringUtils.equals(exApiResponse.get("enable").toString(), apiResponse.get("enable").toString());
        }
        return false;
    }

    private void reSetImportMocksApiId(List<MockConfigImportDTO> mocks, String originId, String newId, int apiNum) {
        if (CollectionUtils.isNotEmpty(mocks)) {
            int index = 1;
            for (MockConfigImportDTO item : mocks) {
                if (StringUtils.equals(item.getApiId(), originId)) {
                    item.setApiId(newId);
                }
                item.setExpectNum(apiNum + "_" + index);
                index++;
            }
        }
    }

    private ApiTestCaseWithBLOBs addNewCase(ApiDefinitionWithBLOBs apiDefinition) {
        ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
        apiTestCase.setApiDefinitionId(apiDefinition.getId());
        apiTestCase.setProjectId(apiDefinition.getProjectId());
        apiTestCase.setName(apiDefinition.getName());
        apiTestCase.setRequest(apiDefinition.getRequest());
        return apiTestCase;
    }

    private boolean setImportHashTree(ApiDefinitionWithBLOBs apiDefinition) {
        String request = apiDefinition.getRequest();
        JSONObject jsonObject = JSONUtil.parseObject(request);
        ElementUtil.dataFormatting(jsonObject);
        MsHTTPSamplerProxy msHTTPSamplerProxy = JSONUtil.parseObject(jsonObject.toString(), MsHTTPSamplerProxy.class);
        boolean createCase = CollectionUtils.isNotEmpty(msHTTPSamplerProxy.getHeaders());
        if (CollectionUtils.isNotEmpty(msHTTPSamplerProxy.getArguments()) && !createCase) {
            createCase = true;
        }
        if (msHTTPSamplerProxy.getBody() != null && !createCase) {
            createCase = true;
        }
        if (CollectionUtils.isNotEmpty(msHTTPSamplerProxy.getRest()) && !createCase) {
            createCase = true;
        }
        msHTTPSamplerProxy.setId(apiDefinition.getId());
        msHTTPSamplerProxy.setHashTree(new LinkedList<>());
        apiDefinition.setRequest(JSON.toJSONString(msHTTPSamplerProxy));
        return createCase;
    }

    private boolean setImportTCPHashTree(ApiDefinitionWithBLOBs apiDefinition) {
        String request = apiDefinition.getRequest();
        MsTCPSampler tcpSampler = JSON.parseObject(request, MsTCPSampler.class);
        boolean createCase = CollectionUtils.isNotEmpty(tcpSampler.getParameters());
        if (StringUtils.isNotBlank(tcpSampler.getJsonDataStruct()) && !createCase) {
            createCase = true;
        }
        if (StringUtils.isNotBlank(tcpSampler.getRawDataStruct()) && !createCase) {
            createCase = true;
        }
        if (CollectionUtils.isNotEmpty(tcpSampler.getXmlDataStruct()) && !createCase) {
            createCase = true;
        }
        tcpSampler.setId(apiDefinition.getId());
        tcpSampler.setHashTree(new LinkedList<>());
        apiDefinition.setRequest(JSON.toJSONString(tcpSampler));
        return createCase;
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

    public ApiReportResult getByResultId(String reportId) {
        ApiDefinitionExecResultWithBLOBs result = apiDefinitionExecResultMapper.selectByPrimaryKey(reportId);
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
            JSONObject content = JSONUtil.parseObject(result.getContent());
            if (StringUtils.isNotEmpty(result.getEnvConfig())) {
                content.put("envName", this.getEnvNameByEnvConfig(result.getProjectId(), result.getEnvConfig()));
            }
            contentStr = content.toString();
            reportResult.setContent(contentStr);
        } catch (Exception e) {
            LogUtil.error("解析content失败!", e);
        }
        return reportResult;
    }


    public String getEnvNameByEnvConfig(String projectId, String envConfig) {
        String envName = null;
        RunModeConfigDTO runModeConfigDTO = null;
        try {
            runModeConfigDTO = JSON.parseObject(envConfig, RunModeConfigDTO.class);
        } catch (Exception e) {
            LogUtil.error("解析" + envConfig + "为RunModeConfigDTO时失败！", e);
        }
        if (StringUtils.isNotEmpty(projectId) && runModeConfigDTO != null && MapUtils.isNotEmpty(runModeConfigDTO.getEnvMap())) {
            String envId = runModeConfigDTO.getEnvMap().get(projectId);
            envName = apiTestEnvironmentService.selectNameById(envId);
        }
        return envName;
    }

    public ApiReportResult getDbResult(String testId, String type) {
        ApiDefinitionExecResultWithBLOBs result = extApiDefinitionExecResultMapper.selectMaxResultByResourceIdAndType(testId, type);
        return buildAPIReportResult(result);
    }

    private void setModule(ApiDefinitionWithBLOBs item) {
        if (item != null && StringUtils.isEmpty(item.getModuleId()) || "default-module".equals(item.getModuleId())) {
            ApiModuleExample example = new ApiModuleExample();
            example.createCriteria().andProjectIdEqualTo(item.getProjectId()).andProtocolEqualTo(item.getProtocol()).andNameEqualTo("未规划接口");
            List<ApiModule> modules = apiModuleMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(modules)) {
                item.setModuleId(modules.get(0).getId());
                item.setModulePath(modules.get(0).getName());
            }
        }
    }

    public ApiDefinitionImport apiTestImport(MultipartFile file, ApiTestImportRequest request) {
        //通过platform，获取对应的导入解析类型。
        if (file != null) {
            String originalFilename = file.getOriginalFilename();
            if (StringUtils.isNotBlank(originalFilename)) {
                String suffixName = originalFilename.substring(originalFilename.indexOf(".") + 1);
                checkFileSuffixName(request, suffixName);
            }
        }
        ApiImportParser runService = ApiDefinitionImportParserFactory.getApiImportParser(request.getPlatform());
        ApiDefinitionImport apiImport = null;
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        if (StringUtils.isNotBlank(request.getSwaggerUrl())) {
            if (!request.getPlatform().equalsIgnoreCase("Swagger2")) {
                this.sendFailMessage(request, project);
                MSException.throwException("文件格式不符合要求");
            }
            try {
                UrlTestUtils.testUrl(request.getSwaggerUrl(), 30000);
            } catch (Exception e) {
                this.sendFailMessage(request, project);
                MSException.throwException(e.getMessage());
            }
        }
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
            this.sendFailMessage(request, project);
            throw e;
        } catch (Exception e) {
            // 发送通知
            this.sendFailMessage(request, project);
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("parse_data_error"));
        }

        try {
            List<ApiImportSendNoticeDTO> apiImportSendNoticeDTOS = importApi(request, apiImport);
            if (CollectionUtils.isNotEmpty(apiImport.getData())) {
                List<String> names = apiImport.getData().stream().map(ApiDefinitionWithBLOBs::getName).collect(Collectors.toList());
                request.setName(String.join(",", names));
                List<String> ids = apiImport.getData().stream().map(ApiDefinitionWithBLOBs::getId).collect(Collectors.toList());
                request.setId(JSON.toJSONString(ids));
            }
            // 发送通知
            if (StringUtils.equals(request.getType(), SCHEDULE)) {
                String scheduleId = scheduleService.getScheduleInfo(request.getResourceId());
                String context = request.getSwaggerUrl() + "导入成功";
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("url", request.getSwaggerUrl());
                NoticeModel noticeModel = NoticeModel.builder().operator(project.getCreateUser()).context(context).testId(scheduleId).subject(Translator.get("swagger_url_scheduled_import_notification")).paramMap(paramMap).event(NoticeConstants.Event.EXECUTE_SUCCESSFUL).build();
                noticeSendService.send(NoticeConstants.Mode.SCHEDULE, StringUtils.EMPTY, noticeModel);
            }
            if (!StringUtils.equals(request.getType(), SCHEDULE) && CollectionUtils.isNotEmpty(apiImportSendNoticeDTOS)) {
                for (ApiImportSendNoticeDTO apiImportSendNoticeDTO : apiImportSendNoticeDTOS) {
                    if (apiImportSendNoticeDTO.getApiDefinitionResult() != null && !apiImportSendNoticeDTO.getApiDefinitionResult().isUpdated()) {
                        sendImportApiCreateNotice(apiImportSendNoticeDTO.getApiDefinitionResult());
                    }
                    if (apiImportSendNoticeDTO.getApiDefinitionResult() != null && apiImportSendNoticeDTO.getApiDefinitionResult().isUpdated()) {
                        sendImportApiUpdateNotice(apiImportSendNoticeDTO.getApiDefinitionResult());
                    }
                    if (CollectionUtils.isNotEmpty(apiImportSendNoticeDTO.getCaseDTOList())) {
                        for (ApiTestCaseDTO apiTestCaseDTO : apiImportSendNoticeDTO.getCaseDTOList()) {
                            if (apiTestCaseDTO.isUpdated()) {
                                sendImportCaseUpdateNotice(apiTestCaseDTO);
                            } else {
                                sendImportCaseCreateNotice(apiTestCaseDTO);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            this.sendFailMessage(request, project);
            LogUtil.error(e);
            MSException.throwException(Translator.get("user_import_format_wrong"));
        }
        return apiImport;
    }


    private void sendFailMessage(ApiTestImportRequest request, Project project) {
        if (StringUtils.equals(request.getType(), SCHEDULE)) {
            String scheduleId = scheduleService.getScheduleInfo(request.getResourceId());
            String context = request.getSwaggerUrl() + "导入失败";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("url", request.getSwaggerUrl());
            paramMap.put("projectId", request.getProjectId());
            NoticeModel noticeModel = NoticeModel.builder().operator(project.getCreateUser()).context(context).testId(scheduleId).subject(Translator.get("swagger_url_scheduled_import_notification")).paramMap(paramMap).event(NoticeConstants.Event.EXECUTE_FAILED).build();
            noticeSendService.send(NoticeConstants.Mode.SCHEDULE, StringUtils.EMPTY, noticeModel);
        }
    }

    private void checkFileSuffixName(ApiTestImportRequest request, String suffixName) {
        if (suffixName.equalsIgnoreCase("jmx")) {
            if (!request.getPlatform().equalsIgnoreCase("JMeter")) {
                MSException.throwException("文件格式不符合要求");
            }
        }
        if (suffixName.equalsIgnoreCase("har")) {
            if (!request.getPlatform().equalsIgnoreCase("Har")) {
                MSException.throwException("文件格式不符合要求");
            }
        }
        if (suffixName.equalsIgnoreCase("json")) {
            if (request.getPlatform().equalsIgnoreCase("Har") || request.getPlatform().equalsIgnoreCase("Jmeter")) {
                MSException.throwException("文件格式不符合要求");
            }
        }
    }

    private List<ApiImportSendNoticeDTO> importApi(ApiTestImportRequest request, ApiDefinitionImport apiImport) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        currentApiCaseOrder.remove();
        currentApiOrder.remove();
        String defaultVersion = baseProjectVersionMapper.getDefaultVersion(request.getProjectId());
        request.setDefaultVersion(defaultVersion);
        if (request.getVersionId() == null) {
            request.setVersionId(defaultVersion);
        }
        List<ApiDefinitionWithBLOBs> initData = apiImport.getData();

        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.URL_REPEATABLE.name());
        boolean urlRepeat = config.getUrlRepeatable();
        //过滤(一次只导入一个协议)
        List<ApiDefinitionWithBLOBs> filterData = initData.stream().filter(t -> t.getProtocol().equals(request.getProtocol())).collect(Collectors.toList());
        if (filterData.isEmpty()) {
            return new ArrayList<>();
        }
        UpdateApiModuleDTO updateApiModuleDTO = apiModuleService.checkApiModule(request, apiImport, filterData, StringUtils.equals("fullCoverage", request.getModeId()), urlRepeat);
        List<ApiDefinitionWithBLOBs> updateList = updateApiModuleDTO.getNeedUpdateList();
        List<ApiDefinitionWithBLOBs> data = updateApiModuleDTO.getDefinitionWithBLOBs();
        List<ApiModule> moduleList = updateApiModuleDTO.getModuleList();
        List<ApiTestCaseWithBLOBs> caseWithBLOBs = updateApiModuleDTO.getCaseWithBLOBs();
        Map<String, List<ApiTestCaseWithBLOBs>> apiIdCaseMap = caseWithBLOBs.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));

        ApiDefinitionMapper batchMapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        ApiTestCaseMapper apiTestCaseMapper = sqlSession.getMapper(ApiTestCaseMapper.class);
        ExtApiDefinitionMapper extApiDefinitionMapper = sqlSession.getMapper(ExtApiDefinitionMapper.class);
        ApiModuleMapper apiModuleMapper = sqlSession.getMapper(ApiModuleMapper.class);

        int num = 0;
        if (!CollectionUtils.isEmpty(data) && data.get(0) != null && data.get(0).getProjectId() != null) {
            num = getNextNum(data.get(0).getProjectId());
        }

        if (moduleList != null) {
            for (ApiModule apiModule : moduleList) {
                apiModuleMapper.insert(apiModule);
            }
        }
        //如果需要导入的数据为空。此时清空mock信息
        if (data.isEmpty()) {
            apiImport.getMocks().clear();
        }
        List<ApiImportSendNoticeDTO> apiImportSendNoticeDTOS = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            ApiDefinitionWithBLOBs item = data.get(i);
            List<ApiTestCaseWithBLOBs> caseList = apiIdCaseMap.get(item.getId());
            this.setModule(item);
            if (item.getName().length() > 255) {
                item.setName(item.getName().substring(0, 255));
            }
            //如果是创建版本数据，则num和其他版本数据一致
            if (item.getVersionId() == null || (!item.getVersionId().equals("new") && !item.getVersionId().equals("update"))) {
                item.setNum(num++);
            }
            //如果EsbData需要存储,则需要进行接口是否更新的判断
            ApiDefinitionImportParamDTO apiDefinitionImportParamDTO = new ApiDefinitionImportParamDTO(item, request, apiImport.getMocks(), updateList, caseList);
            if (apiImport.getEsbApiParamsMap() != null) {
                String apiId = item.getId();
                EsbApiParamsWithBLOBs model = apiImport.getEsbApiParamsMap().get(apiId);
                request.setModeId("fullCoverage");//标准版ESB数据导入不区分是否覆盖，默认都为覆盖

                ApiImportSendNoticeDTO apiImportSendNoticeDTO = importCreate(batchMapper, extApiDefinitionMapper, apiTestCaseMapper, apiDefinitionImportParamDTO);
                if (model != null) {
                    apiImport.getEsbApiParamsMap().remove(apiId);
                    model.setResourceId(item.getId());
                    apiImport.getEsbApiParamsMap().put(item.getId(), model);
                }
                if (apiImportSendNoticeDTO != null) {
                    apiImportSendNoticeDTOS.add(apiImportSendNoticeDTO);
                }
            } else {
                ApiImportSendNoticeDTO apiImportSendNoticeDTO = importCreate(batchMapper, extApiDefinitionMapper, apiTestCaseMapper, apiDefinitionImportParamDTO);
                if (apiImportSendNoticeDTO != null) {
                    apiImportSendNoticeDTOS.add(apiImportSendNoticeDTO);
                }
            }
            if (i % 300 == 0) {
                sqlSession.flushStatements();
            }
        }

        //判断EsbData是否需要存储
        if (apiImport.getEsbApiParamsMap() != null && apiImport.getEsbApiParamsMap().size() > 0) {
            EsbApiParamsMapper esbApiParamsMapper = sqlSession.getMapper(EsbApiParamsMapper.class);
            for (EsbApiParamsWithBLOBs model : apiImport.getEsbApiParamsMap().values()) {
                EsbApiParamsExample example = new EsbApiParamsExample();
                example.createCriteria().andResourceIdEqualTo(model.getResourceId());
                List<EsbApiParamsWithBLOBs> exiteModelList = esbApiParamsMapper.selectByExampleWithBLOBs(example);
                if (exiteModelList.isEmpty()) {
                    esbApiParamsMapper.insert(model);
                } else {
                    model.setId(exiteModelList.get(0).getId());
                    esbApiParamsMapper.updateByPrimaryKeyWithBLOBs(model);
                }
            }
        }

        if (!CollectionUtils.isEmpty(apiImport.getMocks())) {
            MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
            mockConfigService.importMock(apiImport, sqlSession, request);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }

        return apiImportSendNoticeDTOS;
    }


    public ReferenceDTO getReference(ApiScenarioRequest request) {
        ReferenceDTO dto = new ReferenceDTO();
        dto.setScenarioList(extApiScenarioMapper.selectReference(request));
        QueryReferenceRequest planRequest = new QueryReferenceRequest();
        planRequest.setApiId(request.getId());
        planRequest.setProjectId(request.getProjectId());
        dto.setTestPlanList(extTestPlanApiCaseMapper.selectTestPlanByRelevancy(planRequest));
        return dto;
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
     * 数据统计-接口类型
     *
     * @param projectId 项目ID
     * @return List
     */
    public List<ApiDataCountResult> countProtocolByProjectID(String projectId) {
        return extApiDefinitionMapper.countProtocolByProjectID(projectId);
    }

    /**
     * 统计本周创建的数据总量
     *
     * @param projectId
     * @return
     */
    public long countByProjectIDAndCreateInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiDefinitionMapper.countByProjectIDAndCreateInThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<ApiDataCountResult> countStateByProjectID(String projectId) {
        return extApiDefinitionMapper.countStateByProjectID(projectId);
    }

    public List<ApiDataCountResult> countApiCoverageByProjectID(String projectId) {
        return extApiDefinitionMapper.countApiCoverageByProjectID(projectId);
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
        if (CollectionUtils.isEmpty(apiDefinitionList)){
            return;
        }
        List<String> refIds = apiDefinitionList.stream().map(ApiDefinition::getRefId).collect(Collectors.toList());
        example = new ApiDefinitionExample();
        example.createCriteria().andRefIdIn(refIds);
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
        List<String> apiIds = apiDefinitions.stream().map(ApiDefinition::getId).collect(Collectors.toList());
        //删除Api、ApiCase中resourceID被删除了的执行记录
        apiExecutionInfoService.deleteByApiIdList(apiIds);
        apiCaseExecutionInfoService.deleteByApiDefeinitionIdList(apiIds);
        apiTestCaseService.deleteBatchByDefinitionId(apiIds);
        apiDefinitionMapper.deleteByExample(example);
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

    public void calculateResultSce(List<ApiDefinitionResult> resList) {
        if (!resList.isEmpty()) {
            resList.stream().forEach(res -> {
                List<ApiScenario> scenarioList = extApiDefinitionMapper.scenarioList(res.getId());
                String count = String.valueOf(scenarioList.size());
                res.setScenarioTotal(count);
                String[] strings = new String[scenarioList.size()];
                String[] ids2 = scenarioList.stream().map(ApiScenario::getId).collect(Collectors.toList()).toArray(strings);
                res.setIds(ids2);
                res.setScenarioType(ElementConstants.SCENARIO);
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
                if (StringUtils.equalsIgnoreCase("esb", res.getMethod())) {
                    esbApiParamService.handleApiEsbParams(res);
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
            MSException.throwException("URL 格式不正确！");
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
            MSException.throwException("URL 格式不正确！");
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
        configObj.put("headers", request.getHeaders());
        configObj.put("arguments", request.getArguments());
        // 设置 BaseAuth 参数
        if (request.getAuthManager() != null && StringUtils.isNotBlank(request.getAuthManager().getUsername()) && StringUtils.isNotBlank(request.getAuthManager().getPassword())) {
            configObj.put("authManager", request.getAuthManager());
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

        List<ApiDefinitionWithBLOBs> apiDefinitions = getByIds(request.getIds());

        if (StringUtils.equals(type, "MS")) { //  导出为 Metersphere 格式
            MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
            MsApiExportResult msApiExportResult = new MsApiExportResult();
            msApiExportResult.setData(apiDefinitions);
            msApiExportResult.setCases(apiTestCaseService.selectCasesBydApiIds(request.getIds()));
            msApiExportResult.setMocks(mockConfigService.selectMockExpectConfigByApiIdIn(request.getIds()));
            msApiExportResult.setProjectName(request.getProjectId());
            msApiExportResult.setProtocol(request.getProtocol());
            msApiExportResult.setProjectId(request.getProjectId());
            msApiExportResult.setVersion(System.getenv("MS_VERSION"));
            if (CollectionUtils.isNotEmpty((msApiExportResult).getData())) {
                List<String> names = (msApiExportResult).getData().stream().map(ApiDefinitionWithBLOBs::getName).collect(Collectors.toList());
                request.setName(String.join(",", names));
                List<String> ids = msApiExportResult.getData().stream().map(ApiDefinitionWithBLOBs::getId).collect(Collectors.toList());
                request.setId(JSON.toJSONString(ids));
            }
            return msApiExportResult;
        } else { //  导出为 Swagger 格式
            Swagger3Parser swagger3Parser = new Swagger3Parser();
            Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
            return swagger3Parser.swagger3Export(apiDefinitions, project);
        }
    }

    public List<ApiDefinition> selectEffectiveIdByProjectId(String projectId) {
        return extApiDefinitionMapper.selectEffectiveIdByProjectId(projectId);
    }

    public List<ApiDefinition> selectEffectiveIdByProjectIdAndHaveNotCase(String projectId) {
        return extApiDefinitionMapper.selectEffectiveIdByProjectIdAndHaveNotCase(projectId);
    }

    public List<ApiDefinitionWithBLOBs> preparedUrl(String projectId, String method, String baseUrlSuffix, String mockApiResourceId) {
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
            if (StringUtils.equals(bloBs.getMethod(), "ESB")) {
                EsbApiParamsExample example = new EsbApiParamsExample();
                example.createCriteria().andResourceIdEqualTo(id);
                List<EsbApiParamsWithBLOBs> list = esbApiParamsMapper.selectByExampleWithBLOBs(example);
                JSONObject request = JSONUtil.parseObject(bloBs.getRequest());
                Object backEsbDataStruct = request.get("backEsbDataStruct");
                Map<String, Object> map = new HashMap<>();
                if (backEsbDataStruct != null) {
                    map.put("backEsbDataStruct", backEsbDataStruct);
                    if (CollectionUtils.isNotEmpty(list)) {
                        map.put("backScript", list.get(0).getBackedScript());
                    }
                    map.put(PropertyConstant.TYPE, "ESB");
                }
                request.remove("backEsbDataStruct");
                bloBs.setRequest(request.toString());
                String response = JSON.toJSONString(map);
                bloBs.setResponse(response);
            }
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
                        DetailColumn column = new DetailColumn(DefinitionReference.definitionColumns.get("userId"), "userId", item.getUserId(), null);
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

    public ApiDefinition selectUrlAndMethodById(String id) {
        return extApiDefinitionMapper.selectUrlAndMethodById(id);
    }

    public void removeToGcByExample(ApiDefinitionExampleWithOperation apiDefinitionExample) {
        List<ApiDefinition> apiList = apiDefinitionMapper.selectByExample(apiDefinitionExample);
        List<String> apiIdList = new ArrayList<>();
        apiList.forEach(item -> apiIdList.add(item.getId()));
        this.removeToGc(apiIdList);
    }

    public ApiReportResult getTestPlanApiCaseReport(String testId, String type) {
        ApiDefinitionExecResultWithBLOBs result = extApiDefinitionExecResultMapper.selectPlanApiMaxResultByTestIdAndType(testId, type);
        return buildAPIReportResult(result);
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(ApiDefinitionWithBLOBs.class, ApiDefinitionMapper.class, extApiDefinitionMapper::selectProjectIds, extApiDefinitionMapper::getIdsOrderByUpdateTime);
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
        ApiDefinitionResult result = null;
        if (CollectionUtils.isNotEmpty(list)) {
            result = list.get(0);
            this.checkApiAttachInfo(result);
        }
        return result;
    }

    private void checkApiAttachInfo(ApiDefinitionResult result) {
        if (StringUtils.equalsIgnoreCase("esb", result.getMethod())) {
            esbApiParamService.handleApiEsbParams(result);
        }
    }


    public long countEffectiveByProjectId(String projectId) {
        if (StringUtils.isEmpty(projectId)) {
            return 0;
        } else {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andProjectIdEqualTo(projectId).andStatusNotEqualTo("Trash").andLatestEqualTo(true);
            return apiDefinitionMapper.countByExample(example);
        }
    }

    public long countApiByProjectIdAndHasCase(String projectId) {
        return extApiDefinitionMapper.countApiByProjectIdAndHasCase(projectId);
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
            JSONObject body = (JSONObject) object.get("body");
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
        //删除Api、ApiCase中resourceID被删除了的执行记录
        apiExecutionInfoService.deleteByApiIdList(ids);
        apiCaseExecutionInfoService.deleteByApiDefeinitionIdList(ids);

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
                FileUtils.copyBodyFiles(sourceId, api.getId());

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
        example.createCriteria().andProjectIdEqualTo(projectId).andProtocolEqualTo(test.getProtocol()).andNameEqualTo("未规划接口").andLevelEqualTo(1);
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
                result.setEnvConfig(JSON.toJSONString(runModeConfigDTO));
            }
            apiDefinitionExecResultMapper.insert(result);
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

    public Map<String, List<String>> getProjectEnvNameByEnvConfig(String projectId, String envConfig) {
        Map<String, List<String>> returnMap = new HashMap<>();
        RunModeConfigDTO runModeConfigDTO = null;
        try {
            runModeConfigDTO = JSON.parseObject(envConfig, RunModeConfigDTO.class);
        } catch (Exception e) {
            LogUtil.error("解析" + envConfig + "为RunModeConfigDTO时失败！", e);
        }
        if (StringUtils.isNotEmpty(projectId) && runModeConfigDTO != null && MapUtils.isNotEmpty(runModeConfigDTO.getEnvMap())) {
            String envId = runModeConfigDTO.getEnvMap().get(projectId);
            String envName = apiTestEnvironmentService.selectNameById(envId);
            Project project = baseProjectService.getProjectById(projectId);
            String projectName = project == null ? null : project.getName();
            if (StringUtils.isNoneEmpty(envName, projectName)) {
                returnMap.put(projectName, new ArrayList<>() {{
                    this.add(envName);
                }});
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
}
