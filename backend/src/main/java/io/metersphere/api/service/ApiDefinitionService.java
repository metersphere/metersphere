package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.automation.ReferenceDTO;
import io.metersphere.api.dto.automation.TcpTreeTableDataStruct;
import io.metersphere.api.dto.automation.parse.TcpTreeTableDataParser;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImportParserFactory;
import io.metersphere.api.dto.definition.parse.Swagger3Parser;
import io.metersphere.api.dto.definition.request.assertions.document.DocumentElement;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.dto.mockconfig.MockConfigImportDTO;
import io.metersphere.api.dto.scenario.Scenario;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.dto.swaggerurl.SwaggerTaskResult;
import io.metersphere.api.dto.swaggerurl.SwaggerUrlRequest;
import io.metersphere.api.exec.api.ApiExecuteService;
import io.metersphere.api.exec.utils.ApiDefinitionExecResultUtil;
import io.metersphere.api.parse.ApiImportParser;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.json.JSONSchemaToDocumentUtils;
import io.metersphere.commons.json.JSONToDocumentUtils;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.RelationshipEdgeRequest;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.dto.RelationshipEdgeDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.SwaggerUrlImportJob;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.StatusReference;
import io.metersphere.log.vo.api.DefinitionReference;
import io.metersphere.metadata.service.FileAssociationService;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.*;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.service.TestCaseService;
import io.metersphere.track.service.TestPlanService;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
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
    private ApiTestFileMapper apiTestFileMapper;
    @Resource
    private FileService fileService;
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
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ScheduleService scheduleService;
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
    private TestPlanMapper testPlanMapper;
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
    private TestCaseService testCaseService;
    @Resource
    private ApiDefinitionFollowMapper apiDefinitionFollowMapper;
    @Resource
    @Lazy
    private TestPlanService testPlanService;
    @Resource
    private ExtProjectVersionMapper extProjectVersionMapper;
    @Resource
    private ProjectApplicationService projectApplicationService;
    @Resource
    private EsbApiParamsMapper esbApiParamsMapper;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ApiExecutionInfoService apiExecutionInfoService;
    @Resource
    private ApiCaseExecutionInfoService apiCaseExecutionInfoService;
    @Lazy
    @Resource
    private ApiModuleService apiModuleService;
    @Resource
    private ApiTestEnvironmentService apiTestEnvironmentService;
    @Lazy
    @Resource
    private ProjectService projectService;

    @Lazy
    @Resource
    private ApiAutomationService apiAutomationService;
    @Resource
    private FileAssociationService fileAssociationService;
    @Resource
    private ApiScenarioReferenceIdMapper apiScenarioReferenceIdMapper;

    private final ThreadLocal<Long> currentApiOrder = new ThreadLocal<>();
    private final ThreadLocal<Long> currentApiCaseOrder = new ThreadLocal<>();
    private static final String COPY = "Copy";

    public List<ApiDefinitionResult> list(ApiDefinitionRequest request) {
        request = this.initRequest(request, true, true);
        final List<ApiDefinitionResult> resList = this.extApiDefinitionMapper.list(request);
        buildUserInfo(resList);
        if (StringUtils.isNotBlank(request.getProjectId())) {
            buildProjectInfo(resList, request.getProjectId());
            calculateResult(resList, request.getProjectId(), request);
        } else {
            buildProjectInfoWithoutProject(resList);
        }
        return resList;
    }

    private void buildProjectInfoWithoutProject(final List<ApiDefinitionResult> resList) {
        resList.forEach(i -> {
            final Project project = this.projectMapper.selectByPrimaryKey(i.getProjectId());
            if (project == null) {
                i.setProjectName("");
                i.setVersionEnable(false);
            } else {
                i.setProjectName(project.getName());
                i.setVersionEnable(project.getVersionEnable());
            }
        });
    }

    public void buildUserInfo(final List<ApiDefinitionResult> apis) {
        if (CollectionUtils.isEmpty(apis)) {
            return;
        }
        final Set<String> userIds = new HashSet<>();
        apis.forEach(i -> {
            userIds.add(i.getUserId());
            userIds.add(i.getDeleteUserId());
            userIds.add(i.getCreateUser());
        });
        if (!org.apache.commons.collections.CollectionUtils.isEmpty(userIds)) {
            final Map<String, String> userMap = ServiceUtils.getUserNameMap(new ArrayList<>(userIds));
            apis.forEach(caseResult -> {
                caseResult.setCreateUser(userMap.get(caseResult.getCreateUser()));
                caseResult.setDeleteUser(userMap.get(caseResult.getDeleteUserId()));
                caseResult.setUserName(userMap.get(caseResult.getUserId()));
            });
        }
    }

    public void buildProjectInfo(final List<ApiDefinitionResult> apis, final String projectId) {
        final Project project = this.projectMapper.selectByPrimaryKey(projectId);
        apis.forEach(i -> {
            i.setProjectName(project.getName());
            i.setVersionEnable(project.getVersionEnable());
        });
    }

    public List<ApiDefinitionResult> weekList(ApiDefinitionRequest request) {
        //获取7天之前的日期
        final Date startDay = DateUtils.dateSum(new Date(), -6);
        //将日期转化为 00:00:00 的时间戳
        Date startTime = null;
        try {
            startTime = DateUtils.getDayStartTime(startDay);
        } catch (final Exception e) {
        }
        if (startTime == null) {
            return new ArrayList<>(0);
        } else {
            request = this.initRequest(request, true, true);
            final List<ApiDefinitionResult> resList = this.extApiDefinitionMapper.weekList(request, startTime.getTime());
            calculateResult(resList, request.getProjectId());
            calculateResultSce(resList);
            resList.stream().forEach(item -> item.setApiType("api"));
            return resList;
        }
    }

    public void initDefaultModuleId() {
        final ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andModuleIdIsNull();
        final List<ApiDefinition> updateApiList = this.apiDefinitionMapper.selectByExample(example);
        final Map<String, Map<String, List<ApiDefinition>>> projectIdMap = new HashMap<>();
        for (final ApiDefinition api : updateApiList) {
            final String projectId = api.getProjectId();
            final String protocol = api.getProtocol();
            if (projectIdMap.containsKey(projectId)) {
                if (projectIdMap.get(projectId).containsKey(protocol)) {
                    projectIdMap.get(projectId).get(protocol).add(api);
                } else {
                    final List<ApiDefinition> list = new ArrayList<>();
                    list.add(api);
                    projectIdMap.get(projectId).put(protocol, list);
                }
            } else {
                final List<ApiDefinition> list = new ArrayList<>();
                list.add(api);
                final Map<String, List<ApiDefinition>> map = new HashMap<>();
                map.put(protocol, list);
                projectIdMap.put(projectId, map);
            }
        }
        final ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
        for (final Map.Entry<String, Map<String, List<ApiDefinition>>> entry : projectIdMap.entrySet()) {
            final String projectId = entry.getKey();
            final Map<String, List<ApiDefinition>> map = entry.getValue();

            for (final Map.Entry<String, List<ApiDefinition>> itemEntry : map.entrySet()) {
                final String protocol = itemEntry.getKey();
                final ApiModule node = apiModuleService.getDefaultNodeUnCreateNew(projectId, protocol);
                if (node != null) {
                    final List<ApiDefinition> testCaseList = itemEntry.getValue();
                    for (final ApiDefinition apiDefinition : testCaseList) {
                        final ApiDefinitionWithBLOBs updateCase = new ApiDefinitionWithBLOBs();
                        updateCase.setId(apiDefinition.getId());
                        updateCase.setModuleId(node.getId());
                        updateCase.setModulePath("/" + node.getName());

                        this.apiDefinitionMapper.updateByPrimaryKeySelective(updateCase);
                    }
                }
            }
        }
    }

    public List<ApiDefinitionResult> listBatch(final ApiBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> this.extApiDefinitionMapper.selectIds(query));
        if (CollectionUtils.isEmpty(request.getIds())) {
            return new ArrayList<>();
        }
        final List<ApiDefinitionResult> resList = this.extApiDefinitionMapper.listByIds(request.getIds());
        // 排序
        final FixedOrderComparator<String> fixedOrderComparator = new FixedOrderComparator<String>(request.getIds());
        fixedOrderComparator.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.BEFORE);
        final BeanComparator beanComparator = new BeanComparator("id", fixedOrderComparator);
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
    private ApiDefinitionRequest initRequest(final ApiDefinitionRequest request, final boolean defaultSorting, final boolean checkThisWeekData) {
        if (defaultSorting) {
            request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        }
        if (checkThisWeekData) {
            if (request.isSelectThisWeedData()) {
                final Map<String, Date> weekFirstTimeAndLastTime = DateUtils.getWeedFirstTimeAndLastTime(new Date());
                final Date weekFirstTime = weekFirstTimeAndLastTime.get("firstTime");
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
    public ApiDefinitionRequest checkFilterHasCoverage(final ApiDefinitionRequest request) {
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
            //如果查询条件中有未覆盖/已覆盖， 则需要解析出没有用例的接口中，有多少是符合场景覆盖规律的。然后将这些接口的id作为查询参数
            final Map<String, Map<String, String>> scenarioUrlList = this.apiAutomationService.selectScenarioUseUrlByProjectId(request.getProjectId());
            final List<String> apiIdInScenario = this.apiAutomationService.getApiIdInScenario(request.getProjectId(), scenarioUrlList, definitionList);
            if (CollectionUtils.isNotEmpty(apiIdInScenario)) {
                request.setCoverageIds(apiIdInScenario);
            }
        }
        return request;
    }

    public ApiDefinition get(final String id) {
        return this.apiDefinitionMapper.selectByPrimaryKey(id);
    }

    public ApiDefinitionWithBLOBs getBLOBs(final String id) {
        return this.apiDefinitionMapper.selectByPrimaryKey(id);
    }

    public List<ApiDefinitionWithBLOBs> getBLOBs(final List<String> idList) {
        if (idList == null || idList.isEmpty()) {
            return new ArrayList<>(0);
        } else {
            final ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(idList);
            example.setOrderByClause("create_time DESC ");
            return this.apiDefinitionMapper.selectByExampleWithBLOBs(example);
        }
    }

    public ApiDefinitionResult create(final SaveApiDefinitionRequest request, final List<MultipartFile> bodyFiles) {
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

    public ApiDefinitionResult update(final SaveApiDefinitionRequest request, final List<MultipartFile> bodyFiles) {
        if (request.getRequest() != null && StringUtils.isNotEmpty(request.getRequest().getId())) {
            deleteFileByTestId(request.getRequest().getId());
        }
        request.setBodyUploadIds(null);
        if (StringUtils.equals(request.getProtocol(), "DUBBO")) {
            request.setMethod("dubbo://");
        }

        // 设置是否需要进入待更新列表
        final ApiDefinitionSyncService apiDefinitionSyncService = CommonBeanFactory.getBean(ApiDefinitionSyncService.class);
        if (apiDefinitionSyncService != null) {
            apiDefinitionSyncService.syncApi(request);
        }

        final ApiDefinitionWithBLOBs returnModel = updateTest(request);

        final MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
        mockConfigService.updateMockReturnMsgByApi(returnModel);
        FileUtils.createBodyFiles(request.getRequest().getId(), bodyFiles);

        final String context = SessionUtils.getUserId().concat(Translator.get("update_api")).concat(":").concat(returnModel.getName());
        final Map<String, Object> paramMap = new HashMap<>();
        getParamMap(paramMap, returnModel.getProjectId(), SessionUtils.getUserId(), returnModel.getId(), returnModel.getName(), returnModel.getCreateUser());
        paramMap.put("userId", returnModel.getUserId());
        // 发送通知
        final ApiCaseBatchSyncService apiCaseSyncService = CommonBeanFactory.getBean(ApiCaseBatchSyncService.class);
        if (apiCaseSyncService != null) {
            apiCaseSyncService.sendApiNotice(returnModel, paramMap);
        }
        final NoticeModel noticeModel = NoticeModel.builder()
                .operator(SessionUtils.getUserId())
                .context(context)
                .testId(returnModel.getId())
                .subject(Translator.get("api_update_notice"))
                .paramMap(paramMap)
                .excludeSelf(true)
                .event(NoticeConstants.Event.UPDATE)
                .build();
        this.noticeSendService.send(NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
        return getById(returnModel.getId());
    }


    public void checkQuota(final String projectId) {
        final QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        if (quotaService != null) {
            quotaService.checkAPIDefinitionQuota(projectId);
        }
    }

    public void delete(final String apiId) {
        final ApiDefinition apiDefinition = get(apiId);
        if (apiDefinition == null) {
            return;
        }
        final ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andRefIdEqualTo(apiDefinition.getRefId());
        final List<ApiDefinition> apiDefinitions = this.apiDefinitionMapper.selectByExample(example);
        apiDefinitions.forEach(api -> {
            this.apiTestCaseService.deleteTestCase(api.getId());
            deleteFileByTestId(api.getId());
            this.extApiDefinitionExecResultMapper.deleteByResourceId(api.getId());
            this.apiDefinitionMapper.deleteByPrimaryKey(api.getId());
            this.apiExecutionInfoService.deleteByApiId(api.getId());
            this.esbApiParamService.deleteByResourceId(api.getId());
            final MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
            mockConfigService.deleteMockConfigByApiId(api.getId());
            this.relationshipEdgeService.delete(api.getId()); // 删除关系图
            FileUtils.deleteBodyFiles(api.getId());
            deleteFollows(api.getId());
        });
        // 删除用例和接口的关联关系
        this.testCaseService.deleteTestCaseTestByTestIds(List.of(apiId));
        // 删除附件关系
        this.fileAssociationService.deleteByResourceId(apiId);
    }

    private void deleteFollows(final String apiId) {
        final ApiDefinitionFollowExample example = new ApiDefinitionFollowExample();
        example.createCriteria().andDefinitionIdEqualTo(apiId);
        this.apiDefinitionFollowMapper.deleteByExample(example);
    }

    public void deleteBatch(final List<String> apiIds) {
        final ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(apiIds);
        this.esbApiParamService.deleteByResourceIdIn(apiIds);
        this.apiExecutionInfoService.deleteByApiIdList(apiIds);
        this.apiDefinitionMapper.deleteByExample(example);
        this.apiTestCaseService.deleteBatchByDefinitionId(apiIds);
        // 删除附件关系
        this.fileAssociationService.deleteByResourceIds(apiIds);
        final MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
        this.relationshipEdgeService.delete(apiIds); // 删除关系图
        for (final String apiId : apiIds) {
            mockConfigService.deleteMockConfigByApiId(apiId);
            deleteFollows(apiId);
        }
        this.testCaseService.deleteTestCaseTestByTestIds(apiIds);
    }

    public void removeToGc(final List<String> apiIds) {
        if (CollectionUtils.isEmpty(apiIds)) {
            return;
        }
        apiIds.forEach(apiId -> {
            // 把所有版本的api移到回收站
            final ApiDefinitionWithBLOBs api = this.apiDefinitionMapper.selectByPrimaryKey(apiId);
            if (api == null) {
                return;
            }
            final ApiDefinitionExampleWithOperation example = new ApiDefinitionExampleWithOperation();
            example.createCriteria().andRefIdEqualTo(api.getRefId());
            example.setOperator(SessionUtils.getUserId());
            example.setOperationTime(System.currentTimeMillis());
            this.extApiDefinitionMapper.removeToGcByExample(example);

            final ApiDefinitionRequest request = new ApiDefinitionRequest();
            request.setRefId(api.getRefId());
            final List<String> ids = this.extApiDefinitionMapper.selectIds(request);

            // 把所有版本的api case移到回收站
            final List<String> apiCaseIds = this.apiTestCaseService.selectCaseIdsByApiIds(ids);
            if (CollectionUtils.isNotEmpty(apiCaseIds)) {
                final ApiTestBatchRequest apiTestBatchRequest = new ApiTestBatchRequest();
                apiTestBatchRequest.setIds(apiCaseIds);
                apiTestBatchRequest.setUnSelectIds(new ArrayList<>());
                this.apiTestCaseService.deleteToGcByParam(apiTestBatchRequest);
            }
        });
    }

    public void reduction(final ApiBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> this.extApiDefinitionMapper.selectIds(query));
        if (request.getIds() != null || !request.getIds().isEmpty()) {
            request.getIds().forEach(apiId -> {
                final ApiDefinitionWithBLOBs api = this.apiDefinitionMapper.selectByPrimaryKey(apiId);
                if (api == null) {
                    return;
                }
                //检查原来模块是否还在
                final ApiDefinitionExample example = new ApiDefinitionExample();
                example.createCriteria().andRefIdEqualTo(api.getRefId());
                final List<ApiDefinition> reductionCaseList = this.apiDefinitionMapper.selectByExample(example);
                final Map<String, List<ApiDefinition>> nodeMap = new HashMap<>();
                final List<String> reductionIds = new ArrayList<>();
                for (final ApiDefinition apiDefinition : reductionCaseList) {
                    //检查是否同名
                    final SaveApiDefinitionRequest apiDefinitionRequest = new SaveApiDefinitionRequest();
                    apiDefinitionRequest.setProjectId(apiDefinition.getProjectId());
                    apiDefinitionRequest.setMethod(apiDefinition.getMethod());
                    apiDefinitionRequest.setProtocol(apiDefinition.getProtocol());
                    apiDefinitionRequest.setPath(apiDefinition.getPath());
                    apiDefinitionRequest.setName(apiDefinition.getName());
                    apiDefinitionRequest.setId(apiDefinition.getId());
                    apiDefinitionRequest.setModuleId(apiDefinition.getModuleId());
                    apiDefinitionRequest.setModulePath(apiDefinition.getModulePath());
                    String moduleId = apiDefinition.getModuleId();
                    final long nodeCount = this.apiModuleService.countById(moduleId);
                    if (nodeCount <= 0) {
                        checkNameExist(apiDefinitionRequest, true);
                    } else {
                        checkNameExist(apiDefinitionRequest, false);
                    }

                    if (StringUtils.isEmpty(moduleId)) {
                        moduleId = "";
                    }
                    if (nodeMap.containsKey(moduleId)) {
                        nodeMap.get(moduleId).add(apiDefinition);
                    } else {
                        final List<ApiDefinition> list = new ArrayList<>();
                        list.add(apiDefinition);
                        nodeMap.put(moduleId, list);
                    }
                    reductionIds.add(apiDefinition.getId());
                }
                final ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
                for (final Map.Entry<String, List<ApiDefinition>> entry : nodeMap.entrySet()) {
                    final String nodeId = entry.getKey();
                    final long nodeCount = apiModuleService.countById(nodeId);
                    if (nodeCount <= 0) {
                        final String projectId = request.getProjectId();
                        final ApiModule node = apiModuleService.getDefaultNode(projectId, request.getProtocol());
                        for (final ApiDefinition apiDefinition : entry.getValue()) {
                            final ApiDefinitionWithBLOBs updateCase = new ApiDefinitionWithBLOBs();
                            updateCase.setId(apiDefinition.getId());
                            updateCase.setModuleId(node.getId());
                            updateCase.setModulePath("/" + node.getName());

                            this.apiDefinitionMapper.updateByPrimaryKeySelective(updateCase);
                        }
                    }
                }
                this.extApiDefinitionMapper.checkOriginalStatusByIds(reductionIds);
                this.extApiDefinitionMapper.reduction(reductionIds);

                final List<String> apiCaseIds = this.apiTestCaseService.selectCaseIdsByApiIds(reductionIds);
                if (CollectionUtils.isNotEmpty(apiCaseIds)) {
                    final ApiTestBatchRequest apiTestBatchRequest = new ApiTestBatchRequest();
                    apiTestBatchRequest.setIds(apiCaseIds);
                    apiTestBatchRequest.setUnSelectIds(new ArrayList<>());
                    this.apiTestCaseService.reduction(apiTestBatchRequest);
                }
            });

        }
    }

    private void checkNameExist(final SaveApiDefinitionRequest request, final Boolean moduleIdNotExist) {
        if (StringUtils.isEmpty(request.getVersionId())) {
            request.setVersionId(this.extProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        }
        ApiDefinitionExample example = new ApiDefinitionExample();
        final ApiDefinitionExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotEmpty(request.getProtocol()) && request.getProtocol().equals(RequestType.HTTP)) {
            criteria.andMethodEqualTo(request.getMethod()).andStatusNotEqualTo("Trash")
                    .andProtocolEqualTo(request.getProtocol()).andPathEqualTo(request.getPath())
                    .andProjectIdEqualTo(request.getProjectId()).andIdNotEqualTo(request.getId());
            final Project project = this.projectMapper.selectByPrimaryKey(request.getProjectId());
            final ProjectConfig config = this.projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.URL_REPEATABLE.name());
            final boolean urlRepeat = config.getUrlRepeatable();
            if (urlRepeat) {
                if (moduleIdNotExist) {
                    criteria.andModulePathEqualTo(request.getModulePath());
                } else {
                    criteria.andModuleIdEqualTo(request.getModuleId());
                }
                criteria.andNameEqualTo(request.getName());
                if (this.apiDefinitionMapper.countByExample(example) > 0 && request.getNewCreate() != null && request.getNewCreate()) {
                    MSException.throwException(Translator.get("api_versions_create"));
                }
                criteria.andVersionIdEqualTo(request.getVersionId());
                if (this.apiDefinitionMapper.countByExample(example) > 0) {
                    MSException.throwException(Translator.get("api_definition_name_not_repeating") + " :" + Translator.get("api_definition_module") + ":" + request.getModulePath() + " ," + Translator.get("api_definition_name") + " :" + request.getName() + "-" + request.getPath());
                }
            } else {
                if (this.apiDefinitionMapper.countByExample(example) > 0 && request.getNewCreate() != null && request.getNewCreate()) {
                    MSException.throwException(Translator.get("api_versions_create"));
                }
                criteria.andVersionIdEqualTo(request.getVersionId());
                if (this.apiDefinitionMapper.countByExample(example) > 0) {
                    MSException.throwException(Translator.get("api_definition_url_not_repeating") + " :" + Translator.get("api_definition_module") + ":" + request.getModulePath() + " ," + Translator.get("api_definition_name") + " :" + request.getName());
                }
            }
        } else {
            criteria.andProtocolEqualTo(request.getProtocol()).andStatusNotEqualTo("Trash")
                    .andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId())
                    .andIdNotEqualTo(request.getId());
            if (moduleIdNotExist) {
                criteria.andModulePathEqualTo(request.getModulePath());
            } else {
                criteria.andModuleIdEqualTo(request.getModuleId());
            }
            if (this.apiDefinitionMapper.countByExample(example) > 0 && request.getNewCreate() != null && request.getNewCreate()) {
                MSException.throwException(Translator.get("api_versions_create"));
            }
            criteria.andVersionIdEqualTo(request.getVersionId());
            if (this.apiDefinitionMapper.countByExample(example) > 0) {
                MSException.throwException(Translator.get("api_definition_name_already_exists") + " :" + Translator.get("api_definition_module") + ":" + request.getModulePath() + " ," + Translator.get("api_definition_name") + " :" + request.getName());
            }
        }
        if (StringUtils.isNotBlank(request.getId())) {
            final ApiDefinitionWithBLOBs result = this.apiDefinitionMapper.selectByPrimaryKey(request.getId());
            if (result != null) {
                example = new ApiDefinitionExample();
                example.createCriteria().andRefIdEqualTo(result.getRefId()).andStatusNotEqualTo("Trash");
                final List<ApiDefinition> apiDefinitions = this.apiDefinitionMapper.selectByExample(example);
                if (apiDefinitions != null && apiDefinitions.size() > 1) {
                    if (request.getProtocol().equals(RequestType.HTTP) && (!StringUtils.equals(result.getMethod(), request.getMethod()) || !StringUtils.equals(result.getPath(), request.getPath()))) {
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
            request = this.esbApiParamService.handleEsbRequest(request);
        } else if (StringUtils.equals(request.getMethod(), "TCP")) {
            request = this.tcpApiParamService.handleTcpRequest(request);
        }
        final ApiDefinitionWithBLOBs test = new ApiDefinitionWithBLOBs();
        test.setId(request.getId());
        test.setName(request.getName());
        test.setPath(request.getPath());
        test.setProjectId(request.getProjectId());
        request.getRequest().setId(request.getId());
        test.setRequest(JSONObject.toJSONString(request.getRequest()));
        test.setUpdateTime(System.currentTimeMillis());
        test.setStatus(request.getStatus());
        test.setModulePath(request.getModulePath());
        test.setModuleId(request.getModuleId());
        test.setMethod(request.getMethod());
        test.setProtocol(request.getProtocol());
        test.setDescription(request.getDescription());
        test.setResponse(JSONObject.toJSONString(request.getResponse()));
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
            test.setTags("");
        }
        this.setModule(test);

        // 更新数据
        final ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdEqualTo(test.getId()).andVersionIdEqualTo(request.getVersionId());
        if (this.apiDefinitionMapper.updateByExampleSelective(test, example) == 0) {
            // 插入新版本的数据
            final ApiDefinitionWithBLOBs oldApi = this.apiDefinitionMapper.selectByPrimaryKey(test.getId());
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

            this.apiDefinitionMapper.insertSelective(test);
        }
        // 同步修改用例路径
        if (StringUtils.equals(test.getProtocol(), "HTTP")) {
            final List<String> ids = new ArrayList<>();
            ids.add(request.getId());
            this.apiTestCaseService.updateByApiDefinitionId(ids, test, request.getTriggerUpdate());
        }
        final ApiDefinitionWithBLOBs result = this.apiDefinitionMapper.selectByPrimaryKey(test.getId());
        checkAndSetLatestVersion(result.getRefId());

        // 存储附件关系
        this.fileAssociationService.saveApi(test.getId(), request.getRequest(), FileAssociationType.API.name());
        return result;
    }

    private void saveExtendInfo(final SaveApiDefinitionRequest request, final ApiDefinitionWithBLOBs test, final ApiDefinitionWithBLOBs oldApi) {
        // 创建新版是否关联备注
        if (!request.isNewVersionRemark()) {
            test.setRemark(null);
        }
        if (request.isNewVersionCase()) {
            test.setCaseTotal(oldApi.getCaseTotal());
            this.extApiTestCaseMapper.insertNewVersionCases(test, oldApi);
        }
        if (request.isNewVersionMock()) {
            final MockConfigExample mockConfigExample = new MockConfigExample();
            mockConfigExample.createCriteria().andApiIdEqualTo(oldApi.getId());
            final List<MockConfig> mockConfigs = this.mockConfigMapper.selectByExample(mockConfigExample);
            mockConfigs.forEach(config -> {
                final String newMockConfigId = UUID.randomUUID().toString();
                // 1
                final MockExpectConfigExample expectConfigExample = new MockExpectConfigExample();
                expectConfigExample.createCriteria().andMockConfigIdEqualTo(config.getId());
                final List<MockExpectConfigWithBLOBs> mockExpectConfigWithBLOBs = this.mockExpectConfigMapper.selectByExampleWithBLOBs(expectConfigExample);
                mockExpectConfigWithBLOBs.forEach(expectConfig -> {
                    expectConfig.setId(UUID.randomUUID().toString());
                    expectConfig.setMockConfigId(newMockConfigId);
                    this.mockExpectConfigMapper.insert(expectConfig);
                });

                // 2
                config.setId(newMockConfigId);
                config.setApiId(test.getId());
                this.mockConfigMapper.insert(config);
            });
        }

        // 创建新版是否关联依赖关系
        if (request.isNewVersionDeps()) {
            final List<RelationshipEdgeDTO> pre = this.getRelationshipApi(oldApi.getId(), "PRE");
            final List<String> targetIds = pre.stream().map(RelationshipEdgeKey::getTargetId).collect(Collectors.toList());
            final RelationshipEdgeRequest req = new RelationshipEdgeRequest();
            req.setTargetIds(targetIds);
            req.setType("API");
            req.setId(test.getId());
            this.relationshipEdgeService.saveBatch(req);

            final List<RelationshipEdgeDTO> post = this.getRelationshipApi(oldApi.getId(), "POST");
            final List<String> sourceIds = post.stream().map(RelationshipEdgeKey::getSourceId).collect(Collectors.toList());
            final RelationshipEdgeRequest req2 = new RelationshipEdgeRequest();
            req2.setSourceIds(sourceIds);
            req2.setType("API");
            req2.setId(test.getId());
            this.relationshipEdgeService.saveBatch(req2);
        }
    }

    /**
     * 检查设置最新版本
     */
    private void checkAndSetLatestVersion(final String refId) {
        this.extApiDefinitionMapper.clearLatestVersion(refId);
        this.extApiDefinitionMapper.addLatestVersion(refId);
    }

    public void saveFollows(final String definitionId, final List<String> follows) {
        final ApiDefinitionFollowExample example = new ApiDefinitionFollowExample();
        example.createCriteria().andDefinitionIdEqualTo(definitionId);
        this.apiDefinitionFollowMapper.deleteByExample(example);
        if (!org.springframework.util.CollectionUtils.isEmpty(follows)) {
            for (final String follow : follows) {
                final ApiDefinitionFollow item = new ApiDefinitionFollow();
                item.setDefinitionId(definitionId);
                item.setFollowId(follow);
                this.apiDefinitionFollowMapper.insert(item);
            }
        }
    }

    private ApiDefinitionResult createTest(SaveApiDefinitionRequest request) {
        checkNameExist(request, false);
        if (StringUtils.equals(request.getMethod(), "ESB")) {
            //ESB的接口类型数据，采用TCP方式去发送。并将方法类型改为TCP。 并修改发送数据
            request = this.esbApiParamService.handleEsbRequest(request);
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
        test.setRequest(JSONObject.toJSONString(request.getRequest()));
        test.setCreateTime(System.currentTimeMillis());
        test.setUpdateTime(System.currentTimeMillis());
        test.setStatus(APITestStatus.Underway.name());
        test.setModulePath(request.getModulePath());
        test.setModuleId(request.getModuleId());
        test.setRemark(request.getRemark());
        test.setOrder(ServiceUtils.getNextOrder(request.getProjectId(), this.extApiDefinitionMapper::getLastOrder));
        test.setRefId(request.getId());
        test.setVersionId(request.getVersionId());
        test.setLatest(true); // 新建一定是最新的
        if (StringUtils.isEmpty(request.getModuleId()) || "default-module".equals(request.getModuleId())) {
            initModulePathAndId(test.getProjectId(), test);
        }
        test.setResponse(JSONObject.toJSONString(request.getResponse()));
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
            test.setTags("");
        }
        if (this.apiDefinitionMapper.selectByPrimaryKey(test.getId()) == null) {
            this.apiDefinitionMapper.insert(test);
            saveFollows(test.getId(), request.getFollows());
        }
        // 存储附件关系
        this.fileAssociationService.saveApi(test.getId(), request.getRequest(), FileAssociationType.API.name());

        return getById(test.getId());
    }

    public int getNextNum(final String projectId) {
        final ApiDefinition apiDefinition = this.extApiDefinitionMapper.getNextNum(projectId);
        if (apiDefinition == null || apiDefinition.getNum() == null) {
            return 100001;
        } else {
            return Optional.of(apiDefinition.getNum() + 1).orElse(100001);
        }
    }

    private ApiImportSendNoticeDTO importCreate(final ApiDefinitionMapper batchMapper, final ExtApiDefinitionMapper extApiDefinitionMapper, final ApiTestCaseMapper apiTestCaseMapper, final ApiDefinitionImportParamDTO apiDefinitionImportParamDTO) {
        final ApiImportSendNoticeDTO apiImportSendNoticeDTO = new ApiImportSendNoticeDTO();
        final SaveApiDefinitionRequest saveReq = new SaveApiDefinitionRequest();
        final ApiDefinitionWithBLOBs apiDefinition = apiDefinitionImportParamDTO.getApiDefinition();
        BeanUtils.copyBean(saveReq, apiDefinition);

        if (StringUtils.isEmpty(apiDefinition.getStatus())) {
            apiDefinition.setStatus(APITestStatus.Underway.name());
        }
        if (apiDefinition.getUserId() == null) {
            apiDefinition.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        }

        apiDefinition.setDescription(apiDefinition.getDescription());
        final List<ApiDefinitionWithBLOBs> collect = apiDefinitionImportParamDTO.getUpdateList().stream().filter(t -> t.getId().equals(apiDefinition.getId())).collect(Collectors.toList());
        apiDefinitionImportParamDTO.setUpdateList(collect);
        final ApiTestImportRequest apiTestImportRequest = apiDefinitionImportParamDTO.getApiTestImportRequest();
        final List<MockConfigImportDTO> mocks = apiDefinitionImportParamDTO.getMocks();
        List<ApiTestCaseWithBLOBs> caseList = apiDefinitionImportParamDTO.getCaseList();
        if (StringUtils.equals("fullCoverage", apiTestImportRequest.getModeId())) {
            return _importCreate(batchMapper, extApiDefinitionMapper, apiTestCaseMapper, apiDefinitionImportParamDTO);
        } else if (StringUtils.equals("incrementalMerge", apiTestImportRequest.getModeId())) {
            if (CollectionUtils.isEmpty(collect)) {
                final String originId = apiDefinition.getId();
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
                final Map<String, String> stringObjectMap = setImportHashTree(apiDefinition);
                final String requestStr = stringObjectMap.get("request");
                final String createCase = stringObjectMap.get("createCase");
                if (StringUtils.equalsIgnoreCase(createCase, "true") && CollectionUtils.isEmpty(caseList)) {
                    final ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = addNewCase(apiDefinition);
                    caseList = new ArrayList<>();
                    caseList.add(apiTestCaseWithBLOBs);
                }
                reSetImportMocksApiId(mocks, originId, apiDefinition.getId(), apiDefinition.getNum());
                apiDefinition.setRequest(requestStr);
                batchMapper.insert(apiDefinition);
                final List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, apiTestCaseMapper, caseList);
                apiImportSendNoticeDTO.setCaseDTOList(apiTestCaseDTOS);
                extApiDefinitionMapper.clearLatestVersion(apiDefinition.getRefId());
                extApiDefinitionMapper.addLatestVersion(apiDefinition.getRefId());
                final ApiDefinitionResult apiDefinitionResult = getApiDefinitionResult(apiDefinition, false);
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

    private ApiDefinitionResult getApiDefinitionResult(final ApiDefinitionWithBLOBs apiDefinition, final boolean isUpdate) {
        final ApiDefinitionResult apiDefinitionResult = new ApiDefinitionResult();
        BeanUtils.copyBean(apiDefinitionResult, apiDefinition);
        apiDefinitionResult.setUpdated(isUpdate);
        return apiDefinitionResult;
    }

    private List<ApiTestCaseDTO> importCase(final ApiDefinitionWithBLOBs apiDefinition, final ApiTestCaseMapper apiTestCaseMapper, final List<ApiTestCaseWithBLOBs> caseList) {
        if (CollectionUtils.isEmpty(caseList)) {
            return new ArrayList<>();
        }
        final List<ApiTestCaseDTO> apiTestCaseDTOS = new ArrayList<>();
        for (int i = 0; i < caseList.size(); i++) {
            final ApiTestCaseDTO apiTestCaseDTO = new ApiTestCaseDTO();
            final ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = caseList.get(i);
            apiTestCaseWithBLOBs.setApiDefinitionId(apiDefinition.getId());
            apiTestCaseWithBLOBs.setToBeUpdated(apiDefinition.getToBeUpdated() != null && apiDefinition.getToBeUpdated() && StringUtils.equalsIgnoreCase(apiTestCaseWithBLOBs.getVersionId(), "old_case"));
            apiTestCaseWithBLOBs.setVersionId(apiDefinition.getVersionId());
            if (apiTestCaseWithBLOBs.getCreateTime() == null) {
                apiTestCaseWithBLOBs.setCreateTime(System.currentTimeMillis());
            }
            apiTestCaseWithBLOBs.setUpdateTime(System.currentTimeMillis());

            if (StringUtils.isBlank(apiTestCaseWithBLOBs.getCaseStatus())) {
                apiTestCaseWithBLOBs.setCaseStatus(APITestStatus.Prepare.name());
            }
            if (StringUtils.isBlank(apiTestCaseWithBLOBs.getCreateUserId())) {
                apiTestCaseWithBLOBs.setCreateUserId(apiDefinition.getUserId());
            }
            if (apiTestCaseWithBLOBs.getOrder() == null) {
                apiTestCaseWithBLOBs.setOrder(getImportNextCaseOrder(apiDefinition.getProjectId()));
            }
            if (apiTestCaseWithBLOBs.getNum() == null) {
                apiTestCaseWithBLOBs.setNum(this.apiTestCaseService.getNextNum(apiDefinition.getId(), apiDefinition.getNum() + i, apiDefinition.getProjectId()));
            }

            if (apiDefinition.getToBeUpdateTime() != null) {
                apiTestCaseWithBLOBs.setToBeUpdateTime(apiDefinition.getToBeUpdateTime());
            }

            if (StringUtils.isBlank(apiTestCaseWithBLOBs.getPriority())) {
                apiTestCaseWithBLOBs.setPriority("P0");
            }

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

    public Long getImportNextOrder(final String projectId) {
        Long order = this.currentApiOrder.get();
        if (order == null) {
            order = ServiceUtils.getNextOrder(projectId, this.extApiDefinitionMapper::getLastOrder);
        }
        order = (order == null ? 0 : order) + ServiceUtils.ORDER_STEP;
        this.currentApiOrder.set(order);
        return order;
    }

    public Long getImportNextCaseOrder(final String projectId) {
        Long order = this.currentApiCaseOrder.get();
        if (order == null) {
            order = ServiceUtils.getNextOrder(projectId, this.extApiTestCaseMapper::getLastOrder);
        }
        order = order + ServiceUtils.ORDER_STEP;
        this.currentApiCaseOrder.set(order);
        return order;
    }

    private ApiImportSendNoticeDTO _importCreate(final ApiDefinitionMapper batchMapper,
                                                 final ExtApiDefinitionMapper extApiDefinitionMapper,
                                                 final ApiTestCaseMapper apiTestCaseMapper, final ApiDefinitionImportParamDTO apiDefinitionImportParamDTO) {
        final ApiDefinitionWithBLOBs apiDefinition = apiDefinitionImportParamDTO.getApiDefinition();
        final ApiTestImportRequest apiTestImportRequest = apiDefinitionImportParamDTO.getApiTestImportRequest();
        final List<ApiDefinitionWithBLOBs> sameRequest = apiDefinitionImportParamDTO.getUpdateList();
        final List<MockConfigImportDTO> mocks = apiDefinitionImportParamDTO.getMocks();
        List<ApiTestCaseWithBLOBs> caseList = apiDefinitionImportParamDTO.getCaseList();
        final String originId = apiDefinition.getId();
        final ApiImportSendNoticeDTO apiImportSendNoticeDTO = new ApiImportSendNoticeDTO();
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
            if (StringUtils.equalsIgnoreCase(apiDefinition.getProtocol(), RequestType.HTTP)) {
                final Map<String, String> stringStringMap = setImportHashTree(apiDefinition);
                final String requestStr = stringStringMap.get("request");
                final String createCase = stringStringMap.get("createCase");
                if (StringUtils.equalsIgnoreCase(createCase, "true") && CollectionUtils.isEmpty(caseList)) {
                    final ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = addNewCase(apiDefinition);
                    caseList = new ArrayList<>();
                    caseList.add(apiTestCaseWithBLOBs);
                }
                apiDefinition.setRequest(requestStr);
                batchMapper.insert(apiDefinition);

                final ApiDefinitionResult apiDefinitionResult = getApiDefinitionResult(apiDefinition, false);
                apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
            } else {
                if (StringUtils.equalsAnyIgnoreCase(apiDefinition.getProtocol(), RequestType.TCP)) {
                    setImportTCPHashTree(apiDefinition);
                }
                batchMapper.insert(apiDefinition);
                final ApiDefinitionResult apiDefinitionResult = getApiDefinitionResult(apiDefinition, false);
                apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
            }
            final List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, apiTestCaseMapper, caseList);
            apiImportSendNoticeDTO.setCaseDTOList(apiTestCaseDTOS);
        } else { //如果存在则修改
            if (StringUtils.isEmpty(apiTestImportRequest.getUpdateVersionId())) {
                apiTestImportRequest.setUpdateVersionId(apiTestImportRequest.getDefaultVersion());
            }
            final Optional<ApiDefinitionWithBLOBs> apiOp = sameRequest.stream()
                    .filter(api -> StringUtils.equals(api.getVersionId(), apiTestImportRequest.getUpdateVersionId()))
                    .findFirst();

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
                final ApiDefinitionResult apiDefinitionResult = getApiDefinitionResult(apiDefinition, false);
                apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
                final List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, apiTestCaseMapper, caseList);
                apiImportSendNoticeDTO.setCaseDTOList(apiTestCaseDTOS);
            } else {
                final ApiDefinitionWithBLOBs existApi = apiOp.get();
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
                if (apiDefinition.getProtocol().equals("HTTP")) {
                    final Boolean toChangeTime = checkIsSynchronize(existApi, apiDefinition);
                    if (toChangeTime) {
                        apiDefinition.setUpdateTime(System.currentTimeMillis());
                    } else if (apiTestImportRequest.getCoverModule() != null && apiTestImportRequest.getCoverModule()) {
                        apiDefinition.setUpdateTime(System.currentTimeMillis());
                    }
                    if (CollectionUtils.isEmpty(caseList)) {
                        apiDefinition.setToBeUpdated(false);
                    } else {
                        final List<ApiTestCaseWithBLOBs> oldCaseList = caseList.stream().filter(t -> StringUtils.equalsIgnoreCase("old_case", t.getVersionId())
                                && StringUtils.isNotBlank(t.getId())).collect(Collectors.toList());
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
                if (StringUtils.equalsIgnoreCase(apiDefinition.getProtocol(), RequestType.HTTP)) {
                    //如果存在则修改
                    apiDefinition.setId(existApi.getId());
                    final Map<String, String> stringStringMap = setImportHashTree(apiDefinition);
                    final String requestStr = stringStringMap.get("request");
                    apiDefinition.setOrder(existApi.getOrder());
                    apiDefinition.setRequest(requestStr);
                    reSetImportMocksApiId(mocks, originId, apiDefinition.getId(), apiDefinition.getNum());
                    batchMapper.updateByPrimaryKeyWithBLOBs(apiDefinition);
                    final ApiDefinitionResult apiDefinitionResult = getApiDefinitionResult(apiDefinition, true);
                    apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
                } else {
                    apiDefinition.setId(existApi.getId());
                    if (StringUtils.equalsAnyIgnoreCase(apiDefinition.getProtocol(), RequestType.TCP)) {
                        setImportTCPHashTree(apiDefinition);
                    }
                    apiDefinition.setOrder(existApi.getOrder());
                    reSetImportMocksApiId(mocks, originId, apiDefinition.getId(), apiDefinition.getNum());
                    batchMapper.updateByPrimaryKeyWithBLOBs(apiDefinition);
                    final ApiDefinitionResult apiDefinitionResult = getApiDefinitionResult(apiDefinition, true);
                    apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
                }
                final List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, apiTestCaseMapper, caseList);
                apiImportSendNoticeDTO.setCaseDTOList(apiTestCaseDTOS);
            }
        }
        extApiDefinitionMapper.clearLatestVersion(apiDefinition.getRefId());
        extApiDefinitionMapper.addLatestVersion(apiDefinition.getRefId());
        return apiImportSendNoticeDTO;
    }

    private ApiTestCaseWithBLOBs addNewCase(final ApiDefinitionWithBLOBs apiDefinition) {
        final ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
        apiTestCase.setApiDefinitionId(apiDefinition.getId());
        apiTestCase.setProjectId(apiDefinition.getProjectId());
        apiTestCase.setName(apiDefinition.getName());
        apiTestCase.setRequest(apiDefinition.getRequest());
        return apiTestCase;
    }

    public void sendImportApiUpdateNotice(final ApiDefinitionWithBLOBs apiDefinitionWithBLOBs) {
        final String context = SessionUtils.getUserId().concat(Translator.get("update_api")).concat(":").concat(apiDefinitionWithBLOBs.getName());
        final Map<String, Object> paramMap = new HashMap<>();
        getParamMap(paramMap, apiDefinitionWithBLOBs.getProjectId(), SessionUtils.getUserId(), apiDefinitionWithBLOBs.getId(), apiDefinitionWithBLOBs.getName(), apiDefinitionWithBLOBs.getCreateUser());
        paramMap.put("userId", apiDefinitionWithBLOBs.getUserId());
        final NoticeModel noticeModel = NoticeModel.builder()
                .operator(SessionUtils.getUserId())
                .context(context)
                .testId(apiDefinitionWithBLOBs.getId())
                .subject(Translator.get("api_update_notice"))
                .paramMap(paramMap)
                .excludeSelf(true)
                .event(NoticeConstants.Event.UPDATE)
                .build();
        this.noticeSendService.send(NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }

    public void sendImportApiCreateNotice(final ApiDefinitionWithBLOBs apiDefinitionWithBLOBs) {
        final String context = SessionUtils.getUserId().concat(Translator.get("create_api")).concat(":").concat(apiDefinitionWithBLOBs.getName());
        final Map<String, Object> paramMap = new HashMap<>();
        getParamMap(paramMap, apiDefinitionWithBLOBs.getProjectId(), SessionUtils.getUserId(), apiDefinitionWithBLOBs.getId(), apiDefinitionWithBLOBs.getName(), apiDefinitionWithBLOBs.getCreateUser());
        paramMap.put("userId", apiDefinitionWithBLOBs.getUserId());
        final NoticeModel noticeModel = NoticeModel.builder()
                .operator(SessionUtils.getUserId())
                .context(context)
                .testId(apiDefinitionWithBLOBs.getId())
                .subject(Translator.get("api_create_notice"))
                .paramMap(paramMap)
                .excludeSelf(true)
                .event(NoticeConstants.Event.CREATE)
                .build();
        this.noticeSendService.send(NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }

    public void sendImportCaseUpdateNotice(final ApiTestCase apiTestCase) {
        final String context = SessionUtils.getUserId().concat(Translator.get("update_api_case")).concat(":").concat(apiTestCase.getName());
        final Map<String, Object> paramMap = new HashMap<>();
        getParamMap(paramMap, apiTestCase.getProjectId(), SessionUtils.getUserId(), apiTestCase.getId(), apiTestCase.getName(), apiTestCase.getCreateUserId());
        final NoticeModel noticeModel = NoticeModel.builder()
                .operator(SessionUtils.getUserId())
                .context(context)
                .testId(apiTestCase.getId())
                .subject(Translator.get("api_case_update_notice"))
                .paramMap(paramMap)
                .excludeSelf(true)
                .event(NoticeConstants.Event.CASE_UPDATE)
                .build();
        this.noticeSendService.send(NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }

    public void sendImportCaseCreateNotice(final ApiTestCase apiTestCase) {
        final String context = SessionUtils.getUserId().concat(Translator.get("create_api_case")).concat(":").concat(apiTestCase.getName());
        final Map<String, Object> paramMap = new HashMap<>();
        getParamMap(paramMap, apiTestCase.getProjectId(), SessionUtils.getUserId(), apiTestCase.getId(), apiTestCase.getName(), apiTestCase.getCreateUserId());
        final NoticeModel noticeModel = NoticeModel.builder()
                .operator(SessionUtils.getUserId())
                .context(context)
                .testId(apiTestCase.getId())
                .subject(Translator.get("api_case_create_notice"))
                .paramMap(paramMap)
                .excludeSelf(true)
                .event(NoticeConstants.Event.CASE_CREATE)
                .build();
        this.noticeSendService.send(NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }

    private void getParamMap(final Map<String, Object> paramMap, final String projectId, final String userId, final String id, final String name, final String createUser) {
        paramMap.put("projectId", projectId);
        paramMap.put("operator", userId);
        paramMap.put("id", id);
        paramMap.put("name", name);
        paramMap.put("createUser", createUser);
    }

    public Boolean checkIsSynchronize(final ApiDefinitionWithBLOBs existApi, final ApiDefinitionWithBLOBs apiDefinition) {

        final ApiDefinition exApi;
        final ApiDefinition api;
        exApi = existApi;
        api = apiDefinition;
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String exApiString = null;
        String apiString = null;
        try {
            exApiString = objectMapper.writeValueAsString(exApi);
            apiString = objectMapper.writeValueAsString(api);
        } catch (final JsonProcessingException e) {
            e.printStackTrace();
        }
        ApiSyncCaseRequest apiSyncCaseRequest = new ApiSyncCaseRequest();
        Boolean toUpdate = false;
        final ApiDefinitionSyncService apiDefinitionSyncService = CommonBeanFactory.getBean(ApiDefinitionSyncService.class);
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
        } catch (final JsonProcessingException e) {
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
            if (apiDefinition.getTags() != null && Objects.equals(apiDefinition.getTags(), "") && existApi.getTags() != null && Objects.equals(existApi.getTags(), "")) {
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
        } catch (final JsonProcessingException e) {
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

        if (exApiResponse.get("type") != null && apiResponse.get("type") != null) {
            if (!StringUtils.equals(exApiResponse.get("type").toString(), apiResponse.get("type").toString())) {
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

    private void reSetImportMocksApiId(final List<MockConfigImportDTO> mocks, final String originId, final String newId, final int apiNum) {
        if (CollectionUtils.isNotEmpty(mocks)) {
            int index = 1;
            for (final MockConfigImportDTO item : mocks) {
                if (StringUtils.equals(item.getApiId(), originId)) {
                    item.setApiId(newId);
                }
                item.setExpectNum(apiNum + "_" + index);
                index++;
            }
        }
    }

    private Map<String, String> setImportHashTree(final ApiDefinitionWithBLOBs apiDefinition) {
        final Map<String, String> map = new HashMap<>();
        final String request = apiDefinition.getRequest();
        final MsHTTPSamplerProxy msHTTPSamplerProxy = JSONObject.parseObject(request, MsHTTPSamplerProxy.class, Feature.DisableSpecialKeyDetect);
        boolean createCase = CollectionUtils.isNotEmpty(msHTTPSamplerProxy.getHeaders());
        if (CollectionUtils.isNotEmpty(msHTTPSamplerProxy.getArguments())) {
            if (!createCase) {
                createCase = true;
            }
        }
        if (msHTTPSamplerProxy.getBody() != null) {
            if (!createCase) {
                createCase = true;
            }
        }
        if (CollectionUtils.isNotEmpty(msHTTPSamplerProxy.getRest())) {
            if (!createCase) {
                createCase = true;
            }
        }
        msHTTPSamplerProxy.setId(apiDefinition.getId());
        msHTTPSamplerProxy.setHashTree(new LinkedList<>());
        apiDefinition.setRequest(JSONObject.toJSONString(msHTTPSamplerProxy));
        map.put("request", request);
        map.put("createCase", String.valueOf(createCase));
        return map;
    }

    private String setImportTCPHashTree(final ApiDefinitionWithBLOBs apiDefinition) {
        final String request = apiDefinition.getRequest();
        final MsTCPSampler tcpSampler = JSONObject.parseObject(request, MsTCPSampler.class, Feature.DisableSpecialKeyDetect);
        tcpSampler.setId(apiDefinition.getId());
        tcpSampler.setHashTree(new LinkedList<>());
        apiDefinition.setRequest(JSONObject.toJSONString(tcpSampler));
        return request;
    }

    private void deleteFileByTestId(final String apiId) {
        final ApiTestFileExample apiTestFileExample = new ApiTestFileExample();
        apiTestFileExample.createCriteria().andTestIdEqualTo(apiId);
        final List<ApiTestFile> ApiTestFiles = this.apiTestFileMapper.selectByExample(apiTestFileExample);
        this.apiTestFileMapper.deleteByExample(apiTestFileExample);

        if (!CollectionUtils.isEmpty(ApiTestFiles)) {
            final List<String> fileIds = ApiTestFiles.stream().map(ApiTestFile::getFileId).collect(Collectors.toList());
            this.fileService.deleteFileByIds(fileIds);
        }
    }

    /**
     * 测试执行
     *
     * @param request
     * @param bodyFiles
     * @return
     */
    public MsExecResponseDTO run(final RunDefinitionRequest request, final List<MultipartFile> bodyFiles) {
        if (!request.isDebug()) {
            final String testId = request.getTestElement() != null &&
                    CollectionUtils.isNotEmpty(request.getTestElement().getHashTree()) &&
                    CollectionUtils.isNotEmpty(request.getTestElement().getHashTree().get(0).getHashTree()) ?
                    request.getTestElement().getHashTree().get(0).getHashTree().get(0).getName() : request.getId();
            final String reportName = this.getReportNameByTestId(testId);
            final ApiDefinitionExecResultWithBLOBs result = ApiDefinitionExecResultUtil.add(testId, APITestStatus.Running.name(), request.getId(), Objects.requireNonNull(SessionUtils.getUser()).getId());
            result.setName(reportName);
            result.setProjectId(request.getProjectId());
            result.setTriggerMode(TriggerMode.MANUAL.name());
            if (StringUtils.isNotEmpty(request.getEnvironmentId())) {
                final RunModeConfigDTO runModeConfigDTO = new RunModeConfigDTO();
                runModeConfigDTO.setEnvMap(new HashMap<>() {{
                    this.put(request.getProjectId(), request.getEnvironmentId());
                }});
                result.setEnvConfig(JSONObject.toJSONString(runModeConfigDTO));
            }
            this.apiDefinitionExecResultMapper.insert(result);
        }
        if (request.isEditCaseRequest() && CollectionUtils.isNotEmpty(request.getTestElement().getHashTree()) &&
                CollectionUtils.isNotEmpty(request.getTestElement().getHashTree().get(0).getHashTree())) {
            final ApiTestCaseWithBLOBs record = new ApiTestCaseWithBLOBs();
            record.setRequest(JSON.toJSONString(request.getTestElement().getHashTree().get(0).getHashTree().get(0)));
            record.setId(request.getTestElement().getHashTree().get(0).getHashTree().get(0).getName());
            this.apiTestCaseMapper.updateByPrimaryKeySelective(record);
        }
        return this.apiExecuteService.debug(request, bodyFiles);
    }

    private String getReportNameByTestId(final String testId) {
        String testName = this.extApiDefinitionMapper.selectNameById(testId);
        if (StringUtils.isEmpty(testName)) {
            testName = this.extApiTestCaseMapper.selectNameById(testId);
            if (StringUtils.isEmpty(testName)) {
                final String resourceID = this.extTestPlanApiCaseMapper.getApiTestCaseIdById(testId);
                if (StringUtils.isNotEmpty(resourceID)) {
                    testName = this.extApiTestCaseMapper.selectNameById(resourceID);
                }
            }
        }
        return testName;
    }

    /**
     * 获取存储执行结果报告
     *
     * @param testId
     * @return
     */
    public APIReportResult getDbResult(final String testId) {
        final ApiDefinitionExecResultWithBLOBs result = this.extApiDefinitionExecResultMapper.selectMaxResultByResourceId(testId);
        return buildAPIReportResult(result);
    }

    public APIReportResult getByResultId(final String reportId) {
        final ApiDefinitionExecResultWithBLOBs result = this.apiDefinitionExecResultMapper.selectByPrimaryKey(reportId);
        return buildAPIReportResult(result);
    }

    public APIReportResult getReportById(final String testId) {
        final ApiDefinitionExecResultWithBLOBs result = this.apiDefinitionExecResultMapper.selectByPrimaryKey(testId);
        return buildAPIReportResult(result);
    }

    public APIReportResult buildAPIReportResult(final ApiDefinitionExecResultWithBLOBs result) {
        if (result == null) {
            return null;
        }
        final APIReportResult reportResult = new APIReportResult();
        reportResult.setStatus(result.getStatus());
        String contentStr = result.getContent();
        try {
            final JSONObject content = JSONObject.parseObject(result.getContent());
            if (StringUtils.isNotEmpty(result.getEnvConfig())) {
                content.put("envName", this.getEnvNameByEnvConfig(result.getProjectId(), result.getEnvConfig()));
            }
            contentStr = content.toString();
            reportResult.setContent(contentStr);
        } catch (final Exception e) {
            LogUtil.error("解析content失败!", e);
        }
        return reportResult;
    }

    public Map<String, List<String>> getProjectEnvNameByEnvConfig(final String projectId, final String envConfig) {
        final Map<String, List<String>> returnMap = new HashMap<>();
        RunModeConfigDTO runModeConfigDTO = null;
        try {
            runModeConfigDTO = JSONObject.parseObject(envConfig, RunModeConfigDTO.class);
        } catch (final Exception e) {
            LogUtil.error("解析" + envConfig + "为RunModeConfigDTO时失败！", e);
        }
        if (StringUtils.isNotEmpty(projectId) && runModeConfigDTO != null && MapUtils.isNotEmpty(runModeConfigDTO.getEnvMap())) {
            final String envId = runModeConfigDTO.getEnvMap().get(projectId);
            final String envName = this.apiTestEnvironmentService.selectNameById(envId);
            final String projectName = this.projectService.selectNameById(projectId);
            if (StringUtils.isNoneEmpty(envName, projectName)) {
                returnMap.put(projectName, new ArrayList<>() {{
                    this.add(envName);
                }});
            }
        }
        return returnMap;
    }

    public String getEnvNameByEnvConfig(final String projectId, final String envConfig) {
        String envName = null;
        RunModeConfigDTO runModeConfigDTO = null;
        try {
            runModeConfigDTO = JSONObject.parseObject(envConfig, RunModeConfigDTO.class);
        } catch (final Exception e) {
            LogUtil.error("解析" + envConfig + "为RunModeConfigDTO时失败！", e);
        }
        if (StringUtils.isNotEmpty(projectId) && runModeConfigDTO != null && MapUtils.isNotEmpty(runModeConfigDTO.getEnvMap())) {
            final String envId = runModeConfigDTO.getEnvMap().get(projectId);
            envName = this.apiTestEnvironmentService.selectNameById(envId);
        }
        return envName;
    }

    public APIReportResult getDbResult(final String testId, final String type) {
        final ApiDefinitionExecResultWithBLOBs result = this.extApiDefinitionExecResultMapper.selectMaxResultByResourceIdAndType(testId, type);
        return buildAPIReportResult(result);
    }

    private void setModule(final ApiDefinitionWithBLOBs item) {
        if (item != null && StringUtils.isEmpty(item.getModuleId()) || "default-module".equals(item.getModuleId())) {
            final ApiModuleExample example = new ApiModuleExample();
            example.createCriteria().andProjectIdEqualTo(item.getProjectId()).andProtocolEqualTo(item.getProtocol()).andNameEqualTo("未规划接口");
            final List<ApiModule> modules = this.apiModuleMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(modules)) {
                item.setModuleId(modules.get(0).getId());
                item.setModulePath(modules.get(0).getName());
            }
        }
    }

    public ApiDefinitionImport apiTestImport(final MultipartFile file, final ApiTestImportRequest request) {
        //通过platform，获取对应的导入解析类型。
        if (file != null) {
            final String originalFilename = file.getOriginalFilename();
            if (StringUtils.isNotBlank(originalFilename)) {
                final String suffixName = originalFilename.substring(originalFilename.indexOf(".") + 1);
                checkFileSuffixName(request, suffixName);
            }
        }
        final ApiImportParser runService = ApiDefinitionImportParserFactory.getApiImportParser(request.getPlatform());
        ApiDefinitionImport apiImport = null;
        final Project project = this.projectMapper.selectByPrimaryKey(request.getProjectId());
        if (StringUtils.isNotBlank(request.getSwaggerUrl())) {
            if (!request.getPlatform().equalsIgnoreCase("Swagger2")) {
                this.sendFailMessage(request, project);
                MSException.throwException("文件格式不符合要求");
            }
            try {
                UrlTestUtils.testUrl(request.getSwaggerUrl(), 30000);
            } catch (final Exception e) {
                this.sendFailMessage(request, project);
                MSException.throwException(e.getMessage());
            }
        }
        if (StringUtils.equals(request.getType(), "schedule")) {
            request.setProtocol("HTTP");
        }
        try {
            apiImport = (ApiDefinitionImport) Objects.requireNonNull(runService).parse(file == null ? null : file.getInputStream(), request);
            if (apiImport.getMocks() == null) {
                apiImport.setMocks(new ArrayList<>());
            }
        } catch (final MSException e) {
            // 发送通知
            this.sendFailMessage(request, project);
            throw e;
        } catch (final Exception e) {
            // 发送通知
            this.sendFailMessage(request, project);
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("parse_data_error"));
        }

        try {
            final List<ApiImportSendNoticeDTO> apiImportSendNoticeDTOS = importApi(request, apiImport);
            if (CollectionUtils.isNotEmpty(apiImport.getData())) {
                final List<String> names = apiImport.getData().stream().map(ApiDefinitionWithBLOBs::getName).collect(Collectors.toList());
                request.setName(String.join(",", names));
                final List<String> ids = apiImport.getData().stream().map(ApiDefinitionWithBLOBs::getId).collect(Collectors.toList());
                request.setId(JSON.toJSONString(ids));
            }
            // 发送通知
            if (StringUtils.equals(request.getType(), "schedule")) {
                final String scheduleId = this.scheduleService.getScheduleInfo(request.getResourceId());
                final String context = request.getSwaggerUrl() + "导入成功";
                final Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("url", request.getSwaggerUrl());
                final NoticeModel noticeModel = NoticeModel.builder()
                        .operator(project.getCreateUser())
                        .context(context)
                        .testId(scheduleId)
                        .subject(Translator.get("swagger_url_scheduled_import_notification"))
                        .paramMap(paramMap)
                        .event(NoticeConstants.Event.EXECUTE_SUCCESSFUL)
                        .build();
                this.noticeSendService.send(NoticeConstants.Mode.SCHEDULE, "", noticeModel);
            }
            if (CollectionUtils.isNotEmpty(apiImportSendNoticeDTOS)) {
                for (final ApiImportSendNoticeDTO apiImportSendNoticeDTO : apiImportSendNoticeDTOS) {
                    if (apiImportSendNoticeDTO.getApiDefinitionResult() != null && !apiImportSendNoticeDTO.getApiDefinitionResult().isUpdated()) {
                        sendImportApiCreateNotice(apiImportSendNoticeDTO.getApiDefinitionResult());
                    }
                    if (apiImportSendNoticeDTO.getApiDefinitionResult() != null && apiImportSendNoticeDTO.getApiDefinitionResult().isUpdated()) {
                        sendImportApiUpdateNotice(apiImportSendNoticeDTO.getApiDefinitionResult());
                    }
                    if (CollectionUtils.isNotEmpty(apiImportSendNoticeDTO.getCaseDTOList())) {
                        for (final ApiTestCaseDTO apiTestCaseDTO : apiImportSendNoticeDTO.getCaseDTOList()) {
                            if (apiTestCaseDTO.isUpdated()) {
                                sendImportCaseUpdateNotice(apiTestCaseDTO);
                            }
                            if (!apiTestCaseDTO.isUpdated()) {
                                sendImportCaseCreateNotice(apiTestCaseDTO);
                            }
                        }
                    }
                }
            }
        } catch (final Exception e) {
            this.sendFailMessage(request, project);
            LogUtil.error(e);
            MSException.throwException(Translator.get("user_import_format_wrong"));
        }
        return apiImport;
    }

    private void sendFailMessage(final ApiTestImportRequest request, final Project project) {
        if (StringUtils.equals(request.getType(), "schedule")) {
            final String scheduleId = this.scheduleService.getScheduleInfo(request.getResourceId());
            final String context = request.getSwaggerUrl() + "导入失败";
            final Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("url", request.getSwaggerUrl());
            paramMap.put("projectId", request.getProjectId());
            final NoticeModel noticeModel = NoticeModel.builder()
                    .operator(project.getCreateUser())
                    .context(context)
                    .testId(scheduleId)
                    .subject(Translator.get("swagger_url_scheduled_import_notification"))
                    .paramMap(paramMap)
                    .event(NoticeConstants.Event.EXECUTE_FAILED)
                    .build();
            this.noticeSendService.send(NoticeConstants.Mode.SCHEDULE, "", noticeModel);
        }
    }

    private void checkFileSuffixName(final ApiTestImportRequest request, final String suffixName) {
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

    private List<ApiImportSendNoticeDTO> importApi(final ApiTestImportRequest request, final ApiDefinitionImport apiImport) {
        final SqlSession sqlSession = this.sqlSessionFactory.openSession(ExecutorType.BATCH);
        this.currentApiCaseOrder.remove();
        this.currentApiOrder.remove();
        final String defaultVersion = this.extProjectVersionMapper.getDefaultVersion(request.getProjectId());
        request.setDefaultVersion(defaultVersion);
        if (request.getVersionId() == null) {
            request.setVersionId(defaultVersion);
        }
        final List<ApiDefinitionWithBLOBs> initData = apiImport.getData();

        final Project project = this.projectMapper.selectByPrimaryKey(request.getProjectId());
        final ProjectConfig config = this.projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.URL_REPEATABLE.name());
        final boolean urlRepeat = config.getUrlRepeatable();
        //过滤(一次只导入一个协议)
        final List<ApiDefinitionWithBLOBs> filterData = initData.stream().filter(t -> t.getProtocol().equals(request.getProtocol())).collect(Collectors.toList());
        if (filterData.isEmpty()) {
            return new ArrayList<>();
        }
        final UpdateApiModuleDTO updateApiModuleDTO = this.apiModuleService.checkApiModule(request, apiImport, filterData, StringUtils.equals("fullCoverage", request.getModeId()), urlRepeat);
        final List<ApiDefinitionWithBLOBs> updateList = updateApiModuleDTO.getNeedUpdateList();
        final List<ApiDefinitionWithBLOBs> data = updateApiModuleDTO.getDefinitionWithBLOBs();
        final List<ApiModule> moduleList = updateApiModuleDTO.getModuleList();
        final List<ApiTestCaseWithBLOBs> caseWithBLOBs = updateApiModuleDTO.getCaseWithBLOBs();
        final Map<String, List<ApiTestCaseWithBLOBs>> apiIdCaseMap = caseWithBLOBs.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));

        final ApiDefinitionMapper batchMapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        final ApiTestCaseMapper apiTestCaseMapper = sqlSession.getMapper(ApiTestCaseMapper.class);
        final ExtApiDefinitionMapper extApiDefinitionMapper = sqlSession.getMapper(ExtApiDefinitionMapper.class);
        final ApiModuleMapper apiModuleMapper = sqlSession.getMapper(ApiModuleMapper.class);

        int num = 0;
        if (!CollectionUtils.isEmpty(data) && data.get(0) != null && data.get(0).getProjectId() != null) {
            num = getNextNum(data.get(0).getProjectId());
        }

        if (moduleList != null) {
            for (final ApiModule apiModule : moduleList) {
                apiModuleMapper.insert(apiModule);
            }
        }
        //如果需要导入的数据为空。此时清空mock信息
        if (data.isEmpty()) {
            apiImport.getMocks().clear();
        }
        final List<ApiImportSendNoticeDTO> apiImportSendNoticeDTOS = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            final ApiDefinitionWithBLOBs item = data.get(i);
            final List<ApiTestCaseWithBLOBs> caseList = apiIdCaseMap.get(item.getId());
            this.setModule(item);
            if (item.getName().length() > 255) {
                item.setName(item.getName().substring(0, 255));
            }
            //如果是创建版本数据，则num和其他版本数据一致
            if (item.getVersionId() == null || (!item.getVersionId().equals("new") && !item.getVersionId().equals("update"))) {
                item.setNum(num++);
            }
            //如果EsbData需要存储,则需要进行接口是否更新的判断
            final ApiDefinitionImportParamDTO apiDefinitionImportParamDTO = new ApiDefinitionImportParamDTO(item, request, apiImport.getMocks(), updateList, caseList);
            if (apiImport.getEsbApiParamsMap() != null) {
                final String apiId = item.getId();
                final EsbApiParamsWithBLOBs model = apiImport.getEsbApiParamsMap().get(apiId);
                request.setModeId("fullCoverage");//标准版ESB数据导入不区分是否覆盖，默认都为覆盖

                final ApiImportSendNoticeDTO apiImportSendNoticeDTO = importCreate(batchMapper, extApiDefinitionMapper, apiTestCaseMapper, apiDefinitionImportParamDTO);
                if (model != null) {
                    apiImport.getEsbApiParamsMap().remove(apiId);
                    model.setResourceId(item.getId());
                    apiImport.getEsbApiParamsMap().put(item.getId(), model);
                }
                if (apiImportSendNoticeDTO != null) {
                    apiImportSendNoticeDTOS.add(apiImportSendNoticeDTO);
                }
            } else {
                final ApiImportSendNoticeDTO apiImportSendNoticeDTO = importCreate(batchMapper, extApiDefinitionMapper, apiTestCaseMapper, apiDefinitionImportParamDTO);
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
            final EsbApiParamsMapper esbApiParamsMapper = sqlSession.getMapper(EsbApiParamsMapper.class);
            for (final EsbApiParamsWithBLOBs model : apiImport.getEsbApiParamsMap().values()) {
                final EsbApiParamsExample example = new EsbApiParamsExample();
                example.createCriteria().andResourceIdEqualTo(model.getResourceId());
                final List<EsbApiParamsWithBLOBs> exiteModelList = esbApiParamsMapper.selectByExampleWithBLOBs(example);
                if (exiteModelList.isEmpty()) {
                    esbApiParamsMapper.insert(model);
                } else {
                    model.setId(exiteModelList.get(0).getId());
                    esbApiParamsMapper.updateByPrimaryKeyWithBLOBs(model);
                }
            }
        }

        if (!CollectionUtils.isEmpty(apiImport.getMocks())) {
            final MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
            mockConfigService.importMock(apiImport, sqlSession, request);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && this.sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, this.sqlSessionFactory);
        }

        return apiImportSendNoticeDTOS;
    }

    public ReferenceDTO getReference(final ApiScenarioRequest request) {
        final ReferenceDTO dto = new ReferenceDTO();
        dto.setScenarioList(this.extApiScenarioMapper.selectReference(request));
        final QueryTestPlanRequest planRequest = new QueryTestPlanRequest();
        planRequest.setApiId(request.getId());
        planRequest.setProjectId(request.getProjectId());
        dto.setTestPlanList(this.extTestPlanMapper.selectTestPlanByRelevancy(planRequest));
        return dto;
    }

    public void editApiBath(final ApiBatchRequest request) {
        final ApiDefinitionExample definitionExample = new ApiDefinitionExample();
        definitionExample.createCriteria().andIdIn(request.getIds());

        final ApiDefinitionWithBLOBs definitionWithBLOBs = new ApiDefinitionWithBLOBs();
        BeanUtils.copyBean(definitionWithBLOBs, request);
        definitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
        this.apiDefinitionMapper.updateByExampleSelective(definitionWithBLOBs, definitionExample);
    }

    public void editApiByParam(final ApiBatchRequest request) {
        if (request == null) {
            return;
        }
        if (StringUtils.equals("tags", request.getType())) {
            this.batchEditDefinitionTags(request);
            return;
        }
        //name在这里只是查询参数
        request.setName(null);
        final ApiDefinitionWithBLOBs definitionWithBLOBs = new ApiDefinitionWithBLOBs();
        BeanUtils.copyBean(definitionWithBLOBs, request);
        definitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> this.extApiDefinitionMapper.selectIds(query));
        if (CollectionUtils.isNotEmpty(request.getIds())) {
            request.getIds().forEach(apiId -> {
                final ApiDefinitionWithBLOBs api = this.apiDefinitionMapper.selectByPrimaryKey(apiId);
                if (api == null) {
                    return;
                }
                //检查是否同名
                final SaveApiDefinitionRequest apiDefinitionRequest = new SaveApiDefinitionRequest();
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
            this.apiDefinitionMapper.updateByExampleSelective(definitionWithBLOBs, getBatchExample(request));
        }
    }

    private void batchEditDefinitionTags(final ApiBatchRequest request) {
        if (request.getTagList().isEmpty()) {
            return;
        }
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> this.extApiDefinitionMapper.selectIds(query));
        if (CollectionUtils.isEmpty(request.getIds())) {
            return;
        }
        final SqlSession sqlSession = this.sqlSessionFactory.openSession(ExecutorType.BATCH);
        final ApiDefinitionMapper mapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        final ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(request.getIds());
        final List<ApiDefinition> apiDefinitions = this.apiDefinitionMapper.selectByExample(example);
        for (final ApiDefinition apiDefinition : apiDefinitions) {
            final String tags = apiDefinition.getTags();
            if (StringUtils.isBlank(tags) || BooleanUtils.isFalse(request.isAppendTag())) {
                apiDefinition.setTags(JSON.toJSONString(request.getTagList()));
            } else {
                try {
                    final List<String> list = JSON.parseArray(tags, String.class);
                    list.addAll(request.getTagList());
                    apiDefinition.setTags(JSON.toJSONString(list));
                } catch (final Exception e) {
                    LogUtil.error("batch edit tags error.");
                    LogUtil.error(e, e.getMessage());
                    apiDefinition.setTags(JSON.toJSONString(request.getTagList()));
                }
            }
            mapper.updateByPrimaryKey(apiDefinition);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && this.sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, this.sqlSessionFactory);
        }
    }

    public void testPlanRelevance(final ApiCaseRelevanceRequest request) {
        this.apiTestCaseService.relevanceByApi(request);
    }

    public void testCaseReviewRelevance(final ApiCaseRelevanceRequest request) {
        this.apiTestCaseService.relevanceByApiByReview(request);
    }

    /**
     * 数据统计-接口类型
     *
     * @param projectId 项目ID
     * @return List
     */
    public List<ApiDataCountResult> countProtocolByProjectID(final String projectId) {
        return this.extApiDefinitionMapper.countProtocolByProjectID(projectId);
    }

    /**
     * 统计本周创建的数据总量
     *
     * @param projectId
     * @return
     */
    public long countByProjectIDAndCreateInThisWeek(final String projectId) {
        final Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        final Date firstTime = startAndEndDateInWeek.get("firstTime");
        final Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return this.extApiDefinitionMapper.countByProjectIDAndCreateInThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<ApiDataCountResult> countStateByProjectID(final String projectId) {
        return this.extApiDefinitionMapper.countStateByProjectID(projectId);
    }

    public List<ApiDataCountResult> countApiCoverageByProjectID(final String projectId) {
        return this.extApiDefinitionMapper.countApiCoverageByProjectID(projectId);
    }

    public List<ApiDefinition> selectApiDefinitionBydIds(final List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        final ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(ids);
        return this.apiDefinitionMapper.selectByExample(example);
    }

    public void deleteByParams(final ApiBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> this.extApiDefinitionMapper.selectIds(query));
        final List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        ids.forEach(id -> {
            // 把所有版本的api移到回收站
            final ApiDefinitionWithBLOBs api = this.apiDefinitionMapper.selectByPrimaryKey(id);
            if (api == null) {
                return;
            }
            final ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andRefIdEqualTo(api.getRefId());
            final List<ApiDefinition> apiDefinitions = this.apiDefinitionMapper.selectByExample(example);
            final List<String> apiIds = apiDefinitions.stream().map(ApiDefinition::getId).collect(Collectors.toList());
            //删除Api、ApiCase中resourceID被删除了的执行记录
            this.apiExecutionInfoService.deleteByApiIdList(apiIds);
            this.apiCaseExecutionInfoService.deleteByApiDefeinitionIdList(apiIds);
            this.apiTestCaseService.deleteBatchByDefinitionId(apiIds);
            this.apiDefinitionMapper.deleteByExample(example);
        });
    }

    public ApiDefinitionExample getBatchExample(final ApiBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> this.extApiDefinitionMapper.selectIds(query));
        final ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(request.getIds());
        return example;
    }

    public void removeToGcByParams(final ApiBatchRequest request) {
        // 去除Name排序
        if (request.getCondition() != null && CollectionUtils.isNotEmpty(request.getCondition().getOrders())) {
            request.getCondition().getOrders().clear();
        }
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> this.extApiDefinitionMapper.selectIds(query));

        this.removeToGc(request.getIds());
    }

    public Pager<List<ApiDefinitionResult>> listRelevance(final ApiDefinitionRequest request, final int goPage, final int pageSize) {
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        if (StringUtils.isNotBlank(request.getPlanId()) && this.testPlanService.isAllowedRepeatCase(request.getPlanId())) {
            request.setRepeatCase(true);
        }
        final Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        final List<ApiDefinitionResult> resList = this.extApiDefinitionMapper.listRelevance(request);
        buildUserInfo(resList);
        calculateResult(resList, request.getProjectId());
        return PageUtils.setPageInfo(page, resList);
    }

    public List<ApiDefinitionResult> listRelevanceReview(final ApiDefinitionRequest request) {
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        List<ApiDefinitionResult> resList = this.extApiDefinitionMapper.listRelevanceReview(request);
        calculateResult(resList, request.getProjectId());
        resList = this.extApiDefinitionMapper.list(request);
        return resList;
    }

    public void calculateResultSce(final List<ApiDefinitionResult> resList) {
        if (!resList.isEmpty()) {
            resList.stream().forEach(res -> {
                final List<Scenario> scenarioList = this.extApiDefinitionMapper.scenarioList(res.getId());
                final String count = String.valueOf(scenarioList.size());
                res.setScenarioTotal(count);
                final String[] strings = new String[scenarioList.size()];
                final String[] ids2 = scenarioList.stream().map(Scenario::getId).collect(Collectors.toList()).toArray(strings);
                res.setIds(ids2);
                res.setScenarioType("scenario");
            });
        }
    }

    public void calculateResult(final List<ApiDefinitionResult> resList, final String projectId) {
        if (!resList.isEmpty()) {
            final List<String> ids = resList.stream().map(ApiDefinitionResult::getId).collect(Collectors.toList());
            final List<ApiComputeResult> results = this.extApiDefinitionMapper.selectByIdsAndStatusIsNotTrash(ids, projectId);
            calculateResultList(resList, results);
        }
    }

    private void calculateResultList(final List<ApiDefinitionResult> resList, final List<ApiComputeResult> results) {
        final Map<String, ApiComputeResult> resultMap = results.stream().collect(Collectors.toMap(ApiComputeResult::getApiDefinitionId, Function.identity()));
        for (final ApiDefinitionResult res : resList) {
            final ApiComputeResult compRes = resultMap.get(res.getId());
            if (compRes != null) {
                res.setCaseType("apiCase");
                res.setCaseTotal(String.valueOf(compRes.getCaseTotal()));
                res.setCasePassingRate(compRes.getPassRate());
                // 状态优先级 未执行，未通过，通过
                if ((compRes.getError() + compRes.getSuccess()) < compRes.getCaseTotal()) {
                    res.setCaseStatus(Translator.get("not_execute"));
                } else if (compRes.getError() > 0) {
                    res.setCaseStatus(Translator.get("execute_not_pass"));
                } else {
                    res.setCaseStatus(Translator.get("execute_pass"));
                }
            } else {
                res.setCaseType("apiCase");
                res.setCaseTotal("0");
                res.setCasePassingRate("-");
                res.setCaseStatus("-");
            }
            if (StringUtils.equalsIgnoreCase("esb", res.getMethod())) {
                this.esbApiParamService.handleApiEsbParams(res);
            }
        }
    }

    public void calculateResult(final List<ApiDefinitionResult> resList, final String projectId, final ApiDefinitionRequest request) {
        if (!resList.isEmpty()) {
            final List<String> ids = resList.stream().map(ApiDefinitionResult::getId).collect(Collectors.toList());
            List<ApiComputeResult> results = new ArrayList<>();
            if (request != null && MapUtils.isNotEmpty(request.getFilters()) && request.getFilters().containsKey("status") && request.getFilters().get("status").get(0).equals("Trash")) {
                results = this.extApiDefinitionMapper.selectByIdsAndStatusIsTrash(ids, projectId);
            } else {
                results = this.extApiDefinitionMapper.selectByIdsAndStatusIsNotTrash(ids, projectId);
            }
            calculateResultList(resList, results);
        }
    }

    /**
     * swagger定时导入
     */
    public void createSchedule(final ScheduleRequest request) {
        /*保存swaggerUrl*/
        final SwaggerUrlProject swaggerUrlProject = new SwaggerUrlProject();
        BeanUtils.copyBean(swaggerUrlProject, request);
        swaggerUrlProject.setId(UUID.randomUUID().toString());
        // 设置鉴权信息
        if (request.getHeaders() != null || request.getArguments() != null || request.getAuthManager() != null) {
            final String config = setAuthParams(request);
            swaggerUrlProject.setConfig(config);
        }
        this.scheduleService.addSwaggerUrlSchedule(swaggerUrlProject);

        request.setResourceId(swaggerUrlProject.getId());
        final Schedule schedule = this.scheduleService.buildApiTestSchedule(request);
        schedule.setProjectId(swaggerUrlProject.getProjectId());
        try {
            schedule.setName(new java.net.URL(swaggerUrlProject.getSwaggerUrl()).getHost());
        } catch (final MalformedURLException e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException("URL 格式不正确！");
        }
        schedule.setJob(SwaggerUrlImportJob.class.getName());
        schedule.setGroup(ScheduleGroup.SWAGGER_IMPORT.name());
        schedule.setType(ScheduleType.CRON.name());
        this.scheduleService.addSchedule(schedule);
        this.addOrUpdateSwaggerImportCronJob(request);

    }

    public void updateSchedule(final ScheduleRequest request) {
        final SwaggerUrlProject swaggerUrlProject = new SwaggerUrlProject();
        BeanUtils.copyBean(swaggerUrlProject, request);
        // 设置鉴权信息
        if (request.getHeaders() != null || request.getArguments() != null || request.getAuthManager() != null) {
            final String config = setAuthParams(request);
            swaggerUrlProject.setConfig(config);
        } else {
            swaggerUrlProject.setConfig(null);
        }
        this.scheduleService.updateSwaggerUrlSchedule(swaggerUrlProject);
        // 只修改表达式和名称
        final Schedule schedule = new Schedule();
        schedule.setId(request.getTaskId());
        schedule.setValue(request.getValue().trim());
        schedule.setEnable(request.getEnable());
        try {
            schedule.setName(new java.net.URL(swaggerUrlProject.getSwaggerUrl()).getHost());
        } catch (final MalformedURLException e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException("URL 格式不正确！");
        }
        this.scheduleService.editSchedule(schedule);
        request.setResourceId(swaggerUrlProject.getId());
        this.addOrUpdateSwaggerImportCronJob(request);
    }

    /**
     * 设置 SwaggerUrl 同步鉴权参数
     */
    public String setAuthParams(final ScheduleRequest request) {
        // list 数组转化成 json 字符串
        final JSONObject configObj = new JSONObject();
        configObj.put("headers", request.getHeaders());
        configObj.put("arguments", request.getArguments());
        // 设置 BaseAuth 参数
        if (request.getAuthManager() != null
                && StringUtils.isNotBlank(request.getAuthManager().getUsername())
                && StringUtils.isNotBlank(request.getAuthManager().getPassword())) {
            configObj.put("authManager", request.getAuthManager());
        }
        return JSONObject.toJSONString(configObj);
    }

    /**
     * 列表开关切换
     *
     * @param request
     */
    public void switchSchedule(final Schedule request) {
        this.scheduleService.editSchedule(request);
        this.addOrUpdateSwaggerImportCronJob(request);
    }

    //删除
    public void deleteSchedule(final ScheduleRequest request) {
        this.swaggerUrlProjectMapper.deleteByPrimaryKey(request.getId());
        this.scheduleService.deleteByResourceId(request.getId(), ScheduleGroup.SWAGGER_IMPORT.name());
    }

    //查询swaggerUrl详情
    public SwaggerUrlProject getSwaggerInfo(final String resourceId) {
        return this.swaggerUrlProjectMapper.selectByPrimaryKey(resourceId);
    }

    public String getResourceId(final SwaggerUrlRequest swaggerUrlRequest) {
        final SwaggerUrlProjectExample swaggerUrlProjectExample = new SwaggerUrlProjectExample();
        final SwaggerUrlProjectExample.Criteria criteria = swaggerUrlProjectExample.createCriteria();
        criteria.andProjectIdEqualTo(swaggerUrlRequest.getProjectId()).andSwaggerUrlEqualTo(swaggerUrlRequest.getSwaggerUrl());
        if (StringUtils.isNotBlank(swaggerUrlRequest.getModuleId())) {
            criteria.andModuleIdEqualTo(swaggerUrlRequest.getModuleId());
        }
        final List<SwaggerUrlProject> list = this.swaggerUrlProjectMapper.selectByExample(swaggerUrlProjectExample);
        String resourceId = "";
        if (list.size() == 1) {
            resourceId = list.get(0).getId();
        }
        return resourceId;
    }

    public List<SwaggerTaskResult> getSwaggerScheduleList(final String projectId) {
        final List<SwaggerTaskResult> resultList = this.extSwaggerUrlScheduleMapper.getSwaggerTaskList(projectId);
        int dataIndex = 1;
        for (final SwaggerTaskResult swaggerTaskResult :
                resultList) {
            swaggerTaskResult.setIndex(dataIndex++);
            final Date nextExecutionTime = CronUtils.getNextTriggerTime(swaggerTaskResult.getRule());
            if (nextExecutionTime != null) {
                swaggerTaskResult.setNextExecutionTime(nextExecutionTime.getTime());
            }
        }
        return resultList;
    }

    private void addOrUpdateSwaggerImportCronJob(final Schedule request) {
        this.scheduleService.addOrUpdateCronJob(request, SwaggerUrlImportJob.getJobKey(request.getResourceId()), SwaggerUrlImportJob.getTriggerKey(request.getResourceId()), SwaggerUrlImportJob.class);
    }

    public ApiExportResult export(final ApiBatchRequest request, final String type) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> this.extApiDefinitionMapper.selectIds(query));

        final List<ApiDefinitionWithBLOBs> apiDefinitions = getByIds(request.getIds());

        if (StringUtils.equals(type, "MS")) { //  导出为 Metersphere 格式
            final MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
            final MsApiExportResult msApiExportResult = new MsApiExportResult();
            msApiExportResult.setData(apiDefinitions);
            msApiExportResult.setCases(this.apiTestCaseService.selectCasesBydApiIds(request.getIds()));
            msApiExportResult.setMocks(mockConfigService.selectMockExpectConfigByApiIdIn(request.getIds()));
            msApiExportResult.setProjectName(request.getProjectId());
            msApiExportResult.setProtocol(request.getProtocol());
            msApiExportResult.setProjectId(request.getProjectId());
            msApiExportResult.setVersion(System.getenv("MS_VERSION"));
            if (CollectionUtils.isNotEmpty((msApiExportResult).getData())) {
                final List<String> names = (msApiExportResult).getData().stream().map(ApiDefinitionWithBLOBs::getName).collect(Collectors.toList());
                request.setName(String.join(",", names));
                final List<String> ids = msApiExportResult.getData().stream().map(ApiDefinitionWithBLOBs::getId).collect(Collectors.toList());
                request.setId(JSON.toJSONString(ids));
            }
            return msApiExportResult;
        } else { //  导出为 Swagger 格式
            final Swagger3Parser swagger3Parser = new Swagger3Parser();
            return swagger3Parser.swagger3Export(apiDefinitions);
        }
    }

    public List<ApiDefinition> selectEffectiveIdByProjectId(final String projectId) {
        return this.extApiDefinitionMapper.selectEffectiveIdByProjectId(projectId);
    }

    public List<ApiDefinition> selectEffectiveIdByProjectIdAndHaveNotCase(final String projectId) {
        return this.extApiDefinitionMapper.selectEffectiveIdByProjectIdAndHaveNotCase(projectId);
    }

    public List<ApiDefinitionWithBLOBs> preparedUrl(final String projectId, final String method, final String baseUrlSuffix) {
        if (StringUtils.isEmpty(baseUrlSuffix)) {
            return new ArrayList<>();
        } else {
            final ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andMethodEqualTo(method).andProjectIdEqualTo(projectId).andStatusNotEqualTo("Trash").andProtocolEqualTo("HTTP").andLatestEqualTo(true);
            final List<ApiDefinition> apiList = this.apiDefinitionMapper.selectByExample(example);
            final List<String> apiIdList = new ArrayList<>();
            boolean urlSuffixEndEmpty = false;
            String urlSuffix = baseUrlSuffix;
            if (urlSuffix.endsWith("/")) {
                urlSuffixEndEmpty = true;
                urlSuffix = urlSuffix + "testMock";
            }
            final String[] urlParams = urlSuffix.split("/");
            if (urlSuffixEndEmpty) {
                urlParams[urlParams.length - 1] = "";
            }
            for (final ApiDefinition api : apiList) {
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
                        final String[] pathArr = path.split("/");
                        if (pathArr.length == urlParams.length) {
                            boolean isFetch = true;
                            for (int i = 0; i < urlParams.length; i++) {
                                final String pathItem = pathArr[i];
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
                return this.apiDefinitionMapper.selectByExampleWithBLOBs(example);
            }
        }
    }

    public String getLogDetails(final String id) {
        final ApiDefinitionWithBLOBs bloBs = this.apiDefinitionMapper.selectByPrimaryKey(id);
        if (bloBs != null) {
            if (StringUtils.equals(bloBs.getMethod(), "ESB")) {
                final EsbApiParamsExample example = new EsbApiParamsExample();
                example.createCriteria().andResourceIdEqualTo(id);
                final List<EsbApiParamsWithBLOBs> list = this.esbApiParamsMapper.selectByExampleWithBLOBs(example);
                final JSONObject request = JSONObject.parseObject(bloBs.getRequest());
                final Object backEsbDataStruct = request.get("backEsbDataStruct");
                final Map<String, Object> map = new HashMap<>();
                if (backEsbDataStruct != null) {
                    map.put("backEsbDataStruct", backEsbDataStruct);
                    if (CollectionUtils.isNotEmpty(list)) {
                        map.put("backScript", list.get(0).getBackedScript());
                    }
                    map.put("type", "ESB");
                }
                request.remove("backEsbDataStruct");
                bloBs.setRequest(JSONObject.toJSONString(request));
                final String response = JSONObject.toJSONString(map);
                bloBs.setResponse(response);
            }
            final List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, DefinitionReference.definitionColumns);
            final OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), bloBs.getProjectId(), bloBs.getName(), bloBs.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(final List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            final ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(ids);
            final List<ApiDefinition> definitions = this.apiDefinitionMapper.selectByExample(example);
            final List<String> names = definitions.stream().map(ApiDefinition::getName).collect(Collectors.toList());
            final OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), definitions.get(0).getProjectId(), String.join(",", names), definitions.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(final ApiBatchRequest request) {
        request.getCondition();
        if (CollectionUtils.isNotEmpty(request.getIds())) {
            final ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(request.getIds());
            final List<ApiDefinition> definitions = this.apiDefinitionMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(definitions)) {
                final List<DetailColumn> columns = new LinkedList<>();
                if (StringUtils.isNotEmpty(request.getMethod())) {
                    definitions.forEach(item -> {
                        final DetailColumn column = new DetailColumn(DefinitionReference.definitionColumns.get("method"), "method", item.getMethod(), null);
                        columns.add(column);
                    });
                } else if (StringUtils.isNotEmpty(request.getStatus())) {
                    definitions.forEach(item -> {
                        final DetailColumn column = new DetailColumn(DefinitionReference.definitionColumns.get("status"), "status", StatusReference.statusMap.get(item.getStatus()), null);
                        columns.add(column);
                    });
                } else if (StringUtils.isNotEmpty(request.getUserId())) {
                    definitions.forEach(item -> {
                        final DetailColumn column = new DetailColumn(DefinitionReference.definitionColumns.get("userId"), "userId", item.getUserId(), null);
                        columns.add(column);
                    });
                }
                final List<String> names = definitions.stream().map(ApiDefinition::getName).collect(Collectors.toList());
                final OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(request.getIds()), request.getProjectId(), String.join(",", names), definitions.get(0).getCreateUser(), columns);
                return JSON.toJSONString(details);
            }
        }
        return null;
    }

    public String getLogDetails(final ApiCaseRelevanceRequest request) {
        final ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andApiDefinitionIdIn(request.getSelectIds());
        final List<ApiTestCase> apiTestCases = this.apiTestCaseMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiTestCases)) {
            final List<String> names = apiTestCases.stream().map(ApiTestCase::getName).collect(Collectors.toList());
            final TestPlan testPlan = this.testPlanMapper.selectByPrimaryKey(request.getPlanId());
            final OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(request.getSelectIds()), testPlan.getProjectId(), String.join(",", names), testPlan.getCreator(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public ApiDefinition selectUrlAndMethodById(final String id) {
        return this.extApiDefinitionMapper.selectUrlAndMethodById(id);
    }

    public void removeToGcByExample(final ApiDefinitionExampleWithOperation apiDefinitionExample) {
        final List<ApiDefinition> apiList = this.apiDefinitionMapper.selectByExample(apiDefinitionExample);
        final List<String> apiIdList = new ArrayList<>();
        apiList.forEach(item -> apiIdList.add(item.getId()));
        this.removeToGc(apiIdList);
    }

    public APIReportResult getTestPlanApiCaseReport(final String testId, final String type) {
        final ApiDefinitionExecResultWithBLOBs result = this.extApiDefinitionExecResultMapper.selectPlanApiMaxResultByTestIdAndType(testId, type);
        return buildAPIReportResult(result);
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(ApiDefinitionWithBLOBs.class, ApiDefinitionMapper.class,
                this.extApiDefinitionMapper::selectProjectIds,
                this.extApiDefinitionMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(final ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, ApiDefinitionWithBLOBs.class,
                this.apiDefinitionMapper::selectByPrimaryKey,
                this.extApiDefinitionMapper::getPreOrder,
                this.extApiDefinitionMapper::getLastOrder,
                this.apiDefinitionMapper::updateByPrimaryKeySelective);
    }

    public ApiDefinitionResult getById(final String id) {
        final ApiDefinitionRequest request = new ApiDefinitionRequest();
        request.setId(id);
        final List<ApiDefinitionResult> list = this.extApiDefinitionMapper.list(request);
        ApiDefinitionResult result = null;
        if (CollectionUtils.isNotEmpty(list)) {
            result = list.get(0);
            this.checkApiAttachInfo(result);
        }
        return result;
    }

    private void checkApiAttachInfo(final ApiDefinitionResult result) {
        if (StringUtils.equalsIgnoreCase("esb", result.getMethod())) {
            this.esbApiParamService.handleApiEsbParams(result);
        }
    }


    public long countEffectiveByProjectId(final String projectId) {
        if (StringUtils.isEmpty(projectId)) {
            return 0;
        } else {
            final ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andProjectIdEqualTo(projectId).andStatusNotEqualTo("Trash").andLatestEqualTo(true);
            return this.apiDefinitionMapper.countByExample(example);
        }
    }

    public long countApiByProjectIdAndHasCase(final String projectId) {
        return this.extApiDefinitionMapper.countApiByProjectIdAndHasCase(projectId);
    }

    public int getRelationshipCount(final String id) {
        return this.relationshipEdgeService.getRelationshipCount(id, this.extApiDefinitionMapper::countByIds);
    }

    public List<RelationshipEdgeDTO> getRelationshipApi(final String id, final String relationshipType) {
        final List<RelationshipEdge> relationshipEdges = this.relationshipEdgeService.getRelationshipEdgeByType(id, relationshipType);
        final List<String> ids = this.relationshipEdgeService.getRelationIdsByType(relationshipType, relationshipEdges);

        if (CollectionUtils.isNotEmpty(ids)) {
            final ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(ids).andStatusNotEqualTo("Trash");
            final List<ApiDefinition> apiDefinitions = this.apiDefinitionMapper.selectByExample(example);
            final Map<String, ApiDefinition> apiMap = apiDefinitions.stream().collect(Collectors.toMap(ApiDefinition::getId, i -> i));
            final List<RelationshipEdgeDTO> results = new ArrayList<>();
            final Map<String, String> userNameMap = ServiceUtils.getUserNameMap(apiDefinitions.stream().map(ApiDefinition::getUserId).collect(Collectors.toList()));
            for (final RelationshipEdge relationshipEdge : relationshipEdges) {
                final RelationshipEdgeDTO relationshipEdgeDTO = new RelationshipEdgeDTO();
                BeanUtils.copyBean(relationshipEdgeDTO, relationshipEdge);
                final ApiDefinition apiDefinition;
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

    public Pager<List<ApiDefinitionResult>> getRelationshipRelateList(ApiDefinitionRequest request, final int goPage, @PathVariable final int pageSize) {
        request = this.initRequest(request, true, true);
        // 排除同一个api的不同版本
        final ApiDefinitionWithBLOBs currentApi = this.apiDefinitionMapper.selectByPrimaryKey(request.getId());
        final ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andRefIdEqualTo(currentApi.getRefId());
        final List<ApiDefinition> apiDefinitions = this.apiDefinitionMapper.selectByExample(example);
        final List<String> sameApiIds = apiDefinitions.stream().map(ApiDefinition::getId).collect(Collectors.toList());
        final List<String> relationshipIds = this.relationshipEdgeService.getRelationshipIds(request.getId());
        sameApiIds.addAll(relationshipIds);
        request.setNotInIds(sameApiIds);
        request.setId(null); // 去掉id的查询条件
        final Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, this.extApiDefinitionMapper.list(request));
    }

    public List<String> getFollows(final String definitionId) {
        final List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(definitionId)) {
            return result;
        }
        final ApiDefinitionFollowExample example = new ApiDefinitionFollowExample();
        example.createCriteria().andDefinitionIdEqualTo(definitionId);
        final List<ApiDefinitionFollow> follows = this.apiDefinitionFollowMapper.selectByExample(example);
        return follows.stream().map(ApiDefinitionFollow::getFollowId).distinct().collect(Collectors.toList());
    }

    public List<DocumentElement> getDocument(final String id, final String type) {
        final ApiDefinitionWithBLOBs bloBs = this.apiDefinitionMapper.selectByPrimaryKey(id);
        List<DocumentElement> elements = new LinkedList<>();
        if (bloBs != null && StringUtils.isNotEmpty(bloBs.getResponse())) {
            final JSONObject object = JSON.parseObject(bloBs.getResponse(), Feature.DisableSpecialKeyDetect);
            final JSONObject body = (JSONObject) object.get("body");
            if (body != null) {
                final String raw = body.getString("raw");
                final String dataType = body.getString("type");
                if ((StringUtils.isNotEmpty(raw) || StringUtils.isNotEmpty(body.getString("jsonSchema"))) && StringUtils.isNotEmpty(dataType)) {
                    if (StringUtils.equals(type, "JSON")) {
                        final String format = body.getString("format");
                        if (StringUtils.equals(format, "JSON-SCHEMA") && StringUtils.isNotEmpty(body.getString("jsonSchema"))) {
                            elements = JSONSchemaToDocumentUtils.getDocument(body.getString("jsonSchema"));
                        } else {
                            elements = JSONToDocumentUtils.getDocument(raw, dataType);
                        }
                    } else if (StringUtils.equals(dataType, "XML")) {
                        elements = JSONToDocumentUtils.getDocument(raw, type);
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(elements)) {
            elements.add(new DocumentElement().newRoot("root", null));
        }
        return elements;
    }

    public List<ApiDefinitionResult> getApiDefinitionVersions(final String definitionId) {
        final ApiDefinitionWithBLOBs definition = this.apiDefinitionMapper.selectByPrimaryKey(definitionId);
        if (definition == null) {
            return new ArrayList<>();
        }
        final ApiDefinitionRequest request = new ApiDefinitionRequest();
        request.setRefId(definition.getRefId());
        return this.list(request);
    }

    public ApiDefinitionResult getApiDefinitionByVersion(final String refId, final String versionId) {
        final ApiDefinitionRequest request = new ApiDefinitionRequest();
        request.setRefId(refId);
        request.setVersionId(versionId);
        final List<ApiDefinitionResult> list = this.list(request);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public void deleteApiDefinitionByVersion(final String refId, final String version) {
        final ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andRefIdEqualTo(refId).andVersionIdEqualTo(version);
        final List<ApiDefinition> apiDefinitions = this.apiDefinitionMapper.selectByExample(example);
        final List<String> ids = apiDefinitions.stream().map(ApiDefinition::getId).collect(Collectors.toList());
        //删除Api、ApiCase中resourceID被删除了的执行记录
        this.apiExecutionInfoService.deleteByApiIdList(ids);
        this.apiCaseExecutionInfoService.deleteByApiDefeinitionIdList(ids);

        final ApiTestCaseExample apiTestCaseExample = new ApiTestCaseExample();
        apiTestCaseExample.createCriteria().andApiDefinitionIdIn(ids);
        this.apiTestCaseMapper.deleteByExample(apiTestCaseExample);
        //
        this.apiDefinitionMapper.deleteByExample(example);
        checkAndSetLatestVersion(refId);
    }

    public List<ApiDefinitionWithBLOBs> getByIds(final List<String> ids) {
        final ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(ids);
        return this.apiDefinitionMapper.selectByExampleWithBLOBs(example);
    }

    public void batchCopy(final ApiBatchRequest request) {
        //检查测试项目是否开启了url可重复
        final ProjectService projectService = CommonBeanFactory.getBean(ProjectService.class);
        if (projectService != null) {
            projectService.checkProjectIsRepeatable(request.getProjectId());
        }
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> this.extApiDefinitionMapper.selectIds(query));
        final List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) return;
        final List<ApiDefinitionWithBLOBs> apis = getByIds(ids);
        final SqlSession sqlSession = this.sqlSessionFactory.openSession(ExecutorType.BATCH);
        final ApiDefinitionMapper mapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        long nextOrder = ServiceUtils.getNextOrder(request.getProjectId(), this.extApiDefinitionMapper::getLastOrder);

        int nextNum = getNextNum(request.getProjectId());

        try {
            for (int i = 0; i < apis.size(); i++) {
                final ApiDefinitionWithBLOBs api = apis.get(i);
                final String sourceId = api.getId();
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
                if (i % 50 == 0)
                    sqlSession.flushStatements();
            }
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, this.sqlSessionFactory);
        }
    }

    public ApiDefinition getApiDefinition(final ApiDefinitionExample apiDefinitionExample) {
        final List<ApiDefinition> apiDefinitions = this.apiDefinitionMapper.selectByExample(apiDefinitionExample);
        if (apiDefinitions == null || apiDefinitions.size() == 0) {
            return null;
        }
        return apiDefinitions.get(0);
    }

    public void initModulePathAndId(final String projectId, final ApiDefinitionWithBLOBs test) {
        final ApiModuleExample example = new ApiModuleExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andProtocolEqualTo(test.getProtocol()).andNameEqualTo("未规划接口").andLevelEqualTo(1);
        final List<ApiModule> modules = this.apiModuleMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(modules)) {
            test.setModuleId(modules.get(0).getId());
            test.setModulePath("/未规划接口");
        }
    }

    public void saveRelationshipBatch(final ApiDefinitionRelationshipEdgeRequest request) {
        final List<String> relationshipIds = this.relationshipEdgeService.getRelationshipIds(request.getId());
        request.getCondition().setNotInIds(relationshipIds);
        request.getCondition().setId(null);
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> this.extApiDefinitionMapper.selectIds(query));
        final List<String> ids = request.getIds();
        ids.remove(request.getId());
        if (CollectionUtils.isNotEmpty(ids)) {
            if (CollectionUtils.isNotEmpty(request.getTargetIds())) {
                request.setTargetIds(ids);
            } else if (CollectionUtils.isNotEmpty(request.getSourceIds())) {
                request.setSourceIds(ids);
            }
            this.relationshipEdgeService.saveBatch(request);
        }
    }

    public void deleteFollows(final List<String> definitionIds) {
        if (CollectionUtils.isNotEmpty(definitionIds)) {
            final ApiDefinitionFollowExample example = new ApiDefinitionFollowExample();
            example.createCriteria().andDefinitionIdIn(definitionIds);
            this.apiDefinitionFollowMapper.deleteByExample(example);
        }
    }

    public Map<String, List<ApiDefinition>> selectApiBaseInfoGroupByModuleId(final String projectId, final String protocol, final String versionId, final String status) {
        final List<ApiDefinition> apiList = this.extApiDefinitionMapper.selectApiBaseInfoByProjectIdAndProtocolAndStatus(projectId, protocol, versionId, status);
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
    public void updateNoModuleApiToDefaultModule(final String projectId, final String protocol, final String status, final String versionId, final String id) {
        if (StringUtils.isNoneEmpty(projectId, protocol, id)) {
            this.extApiDefinitionMapper.updateNoModuleApiToDefaultModule(projectId, protocol, status, versionId, id);
        }
    }

    public Integer getCitedScenarioCount(final String testId) {
        final ApiScenarioReferenceIdExample apiScenarioReferenceIdExample = new ApiScenarioReferenceIdExample();
        apiScenarioReferenceIdExample.createCriteria().andDataTypeEqualTo(ReportTriggerMode.API.name()).andReferenceTypeEqualTo(COPY).andReferenceIdEqualTo(testId);
        final List<ApiScenarioReferenceId> apiScenarioReferenceIds = this.apiScenarioReferenceIdMapper.selectByExample(apiScenarioReferenceIdExample);
        return apiScenarioReferenceIds.size();
    }

    public List<TcpTreeTableDataStruct> rawToXml(final String rawData) {
        return TcpTreeTableDataParser.xml2TreeTableData(rawData);
    }
}
