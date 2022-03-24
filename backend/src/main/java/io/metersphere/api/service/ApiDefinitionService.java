package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.automation.ReferenceDTO;
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
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.SwaggerUrlImportJob;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.StatusReference;
import io.metersphere.log.vo.api.DefinitionReference;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.*;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.service.TestPlanService;
import org.apache.commons.collections.CollectionUtils;
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
    private RelationshipEdgeService relationshipEdgeService;
    @Resource
    private ApiDefinitionFollowMapper apiDefinitionFollowMapper;
    @Resource
    @Lazy
    private TestPlanService testPlanService;
    @Resource
    private ExtProjectVersionMapper extProjectVersionMapper;
    @Resource
    private ProjectApplicationService projectApplicationService;

    private ThreadLocal<Long> currentApiOrder = new ThreadLocal<>();
    private ThreadLocal<Long> currentApiCaseOrder = new ThreadLocal<>();

    public List<ApiDefinitionResult> list(ApiDefinitionRequest request) {
        request = this.initRequest(request, true, true);
        List<ApiDefinitionResult> resList = extApiDefinitionMapper.list(request);
        buildUserInfo(resList);
        if (StringUtils.isNotBlank(request.getProjectId())) {
            buildProjectInfo(resList, request.getProjectId());
            calculateResult(resList, request.getProjectId());
        } else {
            buildProjectInfoWidthoutProject(resList);
        }
        return resList;
    }

    private void buildProjectInfoWidthoutProject(List<ApiDefinitionResult> resList) {
        resList.forEach(i -> {
            Project project = projectMapper.selectByPrimaryKey(i.getProjectId());
            i.setProjectName(project.getName());
            i.setVersionEnable(project.getVersionEnable());
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
        if (!org.apache.commons.collections.CollectionUtils.isEmpty(userIds)) {
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
            String protocal = api.getProtocol();
            if (projectIdMap.containsKey(projectId)) {
                if (projectIdMap.get(projectId).containsKey(protocal)) {
                    projectIdMap.get(projectId).get(protocal).add(api);
                } else {
                    List<ApiDefinition> list = new ArrayList<>();
                    list.add(api);
                    projectIdMap.get(projectId).put(protocal, list);
                }
            } else {
                List<ApiDefinition> list = new ArrayList<>();
                list.add(api);
                Map<String, List<ApiDefinition>> map = new HashMap<>();
                map.put(protocal, list);
                projectIdMap.put(projectId, map);
            }
        }
        ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
        for (Map.Entry<String, Map<String, List<ApiDefinition>>> entry : projectIdMap.entrySet()) {
            String projectId = entry.getKey();
            Map<String, List<ApiDefinition>> map = entry.getValue();

            for (Map.Entry<String, List<ApiDefinition>> itemEntry : map.entrySet()) {
                String protocal = itemEntry.getKey();
                ApiModule node = apiModuleService.getDefaultNodeUnCreateNew(projectId, protocal);
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
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiDefinitionMapper.selectIds(query));
        if (CollectionUtils.isEmpty(request.getIds())) {
            return new ArrayList<>();
        }
        List<ApiDefinitionResult> resList = extApiDefinitionMapper.listByIds(request.getIds());
        calculateResult(resList, request.getProjectId());
        return resList;
    }

    /**
     * 初始化部分参数
     *
     * @param request
     * @param setDefultOrders
     * @param checkThisWeekData
     * @return
     */
    private ApiDefinitionRequest initRequest(ApiDefinitionRequest request, boolean setDefultOrders, boolean checkThisWeekData) {
        if (setDefultOrders) {
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

    public ApiDefinitionWithBLOBs create(SaveApiDefinitionRequest request, List<MultipartFile> bodyFiles) {
        checkQuota();
        if (StringUtils.equals(request.getProtocol(), "DUBBO")) {
            request.setMethod("dubbo://");
        }
        if (StringUtils.isNotEmpty(request.getSourceId())) {
            // 检查附件复制出附件
            FileUtils.copyBodyFiles(request.getSourceId(), request.getId());
        } else {
            FileUtils.createBodyFiles(request.getRequest().getId(), bodyFiles);
        }
        ApiDefinitionWithBLOBs returnModel = createTest(request);
        return returnModel;
    }

    public ApiDefinitionWithBLOBs update(SaveApiDefinitionRequest request, List<MultipartFile> bodyFiles) {
        checkQuota();
        if (request.getRequest() != null) {
            deleteFileByTestId(request.getRequest().getId());
        }
        request.setBodyUploadIds(null);
        if (StringUtils.equals(request.getProtocol(), "DUBBO")) {
            request.setMethod("dubbo://");
        }
        ApiDefinitionWithBLOBs returnModel = updateTest(request);
        MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
        mockConfigService.updateMockReturnMsgByApi(returnModel);
        FileUtils.createBodyFiles(request.getRequest().getId(), bodyFiles);
        return getBLOBs(returnModel.getId());
    }

    public void checkQuota() {
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        if (quotaService != null) {
            quotaService.checkAPIDefinitionQuota();
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
            deleteFileByTestId(api.getId());
            extApiDefinitionExecResultMapper.deleteByResourceId(api.getId());
            apiDefinitionMapper.deleteByPrimaryKey(api.getId());
            esbApiParamService.deleteByResourceId(api.getId());
            MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
            mockConfigService.deleteMockConfigByApiId(api.getId());
            relationshipEdgeService.delete(api.getId()); // 删除关系图
            FileUtils.deleteBodyFiles(api.getId());
            deleteFollows(api.getId());
        });
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
        apiDefinitionMapper.deleteByExample(example);
        apiTestCaseService.deleteBatchByDefinitionId(apiIds);
        MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
        relationshipEdgeService.delete(apiIds); // 删除关系图
        for (String apiId : apiIds) {
            mockConfigService.deleteMockConfigByApiId(apiId);
            deleteFollows(apiId);
        }
    }

    public void removeToGc(List<String> apiIds) {
        if (CollectionUtils.isEmpty(apiIds)) {
            return;
        }
        apiIds.forEach(apiId -> {
            // 把所有版本的api移到回收站
            ApiDefinitionWithBLOBs api = apiDefinitionMapper.selectByPrimaryKey(apiId);
            if (api == null) {
                return;
            }
            ApiDefinitionExampleWithOperation example = new ApiDefinitionExampleWithOperation();
            example.createCriteria().andRefIdEqualTo(api.getRefId());
            example.setOperator(SessionUtils.getUserId());
            example.setOperationTime(System.currentTimeMillis());
            extApiDefinitionMapper.removeToGcByExample(example);

            ApiDefinitionRequest request = new ApiDefinitionRequest();
            request.setRefId(api.getRefId());
            List<String> ids = extApiDefinitionMapper.selectIds(request);

            // 把所有版本的api case移到回收站
            List<String> apiCaseIds = apiTestCaseService.selectCaseIdsByApiIds(ids);
            if (CollectionUtils.isNotEmpty(apiCaseIds)) {
                ApiTestBatchRequest apiTestBatchRequest = new ApiTestBatchRequest();
                apiTestBatchRequest.setIds(apiCaseIds);
                apiTestBatchRequest.setUnSelectIds(new ArrayList<>());
                apiTestCaseService.deleteToGcByParam(apiTestBatchRequest);
            }
        });
    }

    public void reduction(ApiBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiDefinitionMapper.selectIds(query));
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
                    apiDefinitionRequest.setProjectId(request.getProjectId());
                    apiDefinitionRequest.setMethod(apiDefinition.getMethod());
                    apiDefinitionRequest.setProtocol(apiDefinition.getProtocol());
                    apiDefinitionRequest.setPath(apiDefinition.getPath());
                    apiDefinitionRequest.setName(apiDefinition.getName());
                    apiDefinitionRequest.setId(apiDefinition.getId());
                    apiDefinitionRequest.setVersionId(extProjectVersionMapper.getDefaultVersion(request.getProjectId()));
                    checkNameExist(apiDefinitionRequest);

                    String moduleId = apiDefinition.getModuleId();
                    if (StringUtils.isEmpty(moduleId)) {
                        moduleId = "";
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

    private void checkNameExist(SaveApiDefinitionRequest request) {
        if (StringUtils.isEmpty(request.getVersionId())) {
            request.setVersionId(extProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        }
        ApiDefinitionExample example = new ApiDefinitionExample();
        if (StringUtils.isNotEmpty(request.getProtocol()) && request.getProtocol().equals(RequestType.HTTP)) {
            ApiDefinitionExample.Criteria criteria = example.createCriteria();
            criteria.andMethodEqualTo(request.getMethod()).andStatusNotEqualTo("Trash")
                    .andProtocolEqualTo(request.getProtocol()).andPathEqualTo(request.getPath())
                    .andProjectIdEqualTo(request.getProjectId()).andIdNotEqualTo(request.getId())
                    .andVersionIdEqualTo(request.getVersionId());
            Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
            ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.URL_REPEATABLE.name());
            boolean urlRepeat = config.getUrlRepeatable();
            if (project != null && urlRepeat) {
                criteria.andNameEqualTo(request.getName());
                if (apiDefinitionMapper.countByExample(example) > 0) {
                    MSException.throwException(Translator.get("api_definition_name_not_repeating"));
                }
            } else {
                if (apiDefinitionMapper.countByExample(example) > 0) {
                    MSException.throwException(Translator.get("api_definition_url_not_repeating"));
                }
            }
        } else {
            example.createCriteria().andProtocolEqualTo(request.getProtocol()).andStatusNotEqualTo("Trash")
                    .andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId())
                    .andIdNotEqualTo(request.getId()).andVersionIdEqualTo(request.getVersionId());
            if (apiDefinitionMapper.countByExample(example) > 0) {
                MSException.throwException(Translator.get("load_test_already_exists"));
            }
        }
    }

    private List<ApiDefinition> getSameRequest(SaveApiDefinitionRequest request) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        if (request.getProtocol().equals(RequestType.HTTP)) {
            example.createCriteria().andMethodEqualTo(request.getMethod()).andStatusNotEqualTo("Trash")
                    .andPathEqualTo(request.getPath())
                    .andProjectIdEqualTo(request.getProjectId()).andIdNotEqualTo(request.getId());
            return apiDefinitionMapper.selectByExample(example);
        } else {
            example.createCriteria().andProtocolEqualTo(request.getProtocol()).andStatusNotEqualTo("Trash")
                    .andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId())
                    .andIdNotEqualTo(request.getId());
            return apiDefinitionMapper.selectByExample(example);
        }
    }

    private List<ApiDefinition> getSameRequestById(String id, String projectId) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andStatusNotEqualTo("Trash")
                .andProjectIdEqualTo(projectId)
                .andIdEqualTo(id);
        return apiDefinitionMapper.selectByExample(example);
    }

    private List<ApiDefinition> getSameRequestWithName(SaveApiDefinitionRequest request) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        if (request.getProtocol().equals(RequestType.HTTP)) {
            example.createCriteria()
                    .andMethodEqualTo(request.getMethod())
                    .andStatusNotEqualTo("Trash")
                    .andPathEqualTo(request.getPath())
                    .andNameEqualTo(request.getName())
                    .andProjectIdEqualTo(request.getProjectId())
                    .andIdNotEqualTo(request.getId());
        } else {
            example.createCriteria()
                    .andStatusNotEqualTo("Trash")
                    .andNameEqualTo(request.getName())
                    .andProjectIdEqualTo(request.getProjectId())
                    .andIdNotEqualTo(request.getId());
        }

        return apiDefinitionMapper.selectByExample(example);

    }

    private ApiDefinitionWithBLOBs updateTest(SaveApiDefinitionRequest request) {
        checkNameExist(request);
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
        if (StringUtils.isNotEmpty(request.getTags()) && !StringUtils.equals(request.getTags(), "[]")) {
            test.setTags(request.getTags());
        } else {
            test.setTags("");
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
            // 创建新版是否关联备注
            if (!request.isNewVersionRemark()) {
                test.setRemark(null);
            }
            apiDefinitionMapper.insertSelective(test);

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

        // 同步修改用例路径
        if (StringUtils.equals(test.getProtocol(), "HTTP")) {
            List<String> ids = new ArrayList<>();
            ids.add(request.getId());
            apiTestCaseService.updateByApiDefinitionId(ids, test.getPath(), test.getMethod(), test.getProtocol());
        }
        //
        ApiDefinitionWithBLOBs result = apiDefinitionMapper.selectByPrimaryKey(test.getId());
        checkAndSetLatestVersion(result.getRefId());
        return result;
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

    private ApiDefinitionWithBLOBs createTest(SaveApiDefinitionRequest request) {
        checkNameExist(request);
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
        test.setRequest(JSONObject.toJSONString(request.getRequest()));
        test.setCreateTime(System.currentTimeMillis());
        test.setUpdateTime(System.currentTimeMillis());
        test.setStatus(APITestStatus.Underway.name());
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
        if (apiDefinitionMapper.selectByPrimaryKey(test.getId()) == null) {
            apiDefinitionMapper.insert(test);
            saveFollows(test.getId(), request.getFollows());
        }
        return test;
    }

    public int getNextNum(String projectId) {
        ApiDefinition apiDefinition = extApiDefinitionMapper.getNextNum(projectId);
        if (apiDefinition == null || apiDefinition.getNum() == null) {
            return 100001;
        } else {
            return Optional.of(apiDefinition.getNum() + 1).orElse(100001);
        }
    }

    private ApiDefinition importCreate(ApiDefinitionWithBLOBs apiDefinition, ApiDefinitionMapper batchMapper,
                                       ApiTestCaseMapper apiTestCaseMapper, ExtApiDefinitionMapper extApiDefinitionMapper,
                                       ApiTestImportRequest apiTestImportRequest, List<ApiTestCaseWithBLOBs> cases, List<MockConfigImportDTO> mocks,
                                       Boolean repeatable) {
        SaveApiDefinitionRequest saveReq = new SaveApiDefinitionRequest();
        BeanUtils.copyBean(saveReq, apiDefinition);
        apiDefinition.setCreateTime(System.currentTimeMillis());
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isEmpty(apiDefinition.getStatus())) {
            apiDefinition.setStatus(APITestStatus.Underway.name());
        }
        if (apiDefinition.getUserId() == null) {
            apiDefinition.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        } else {
            apiDefinition.setUserId(apiDefinition.getUserId());
        }
        if(apiDefinition.getModuleId()==null){
            if (StringUtils.isEmpty(apiDefinition.getModuleId()) || "default-module".equals(apiDefinition.getModuleId())) {
                initModulePathAndId(apiDefinition.getProjectId(), apiDefinition);
            }
        }
        apiDefinition.setDescription(apiDefinition.getDescription());

        List<ApiDefinition> sameRequest;
        if (repeatable == null || !repeatable) {
            sameRequest = getSameRequest(saveReq);
        } else {
            // 如果勾选了允许重复，则判断更新要加上name字段
            sameRequest = getSameRequestWithName(saveReq);
        }
        if (CollectionUtils.isEmpty(sameRequest)) {
            sameRequest = getSameRequestById(apiDefinition.getId(), apiTestImportRequest.getProjectId());
        }
        if (StringUtils.equals("fullCoverage", apiTestImportRequest.getModeId())) {
            _importCreate(sameRequest, batchMapper, apiDefinition, apiTestCaseMapper, extApiDefinitionMapper, apiTestImportRequest, cases, mocks);
        } else if (StringUtils.equals("incrementalMerge", apiTestImportRequest.getModeId())) {
            if (CollectionUtils.isEmpty(sameRequest)) {
                //postman 可能含有前置脚本，接口定义去掉脚本
                apiDefinition.setOrder(getImportNextOrder(apiTestImportRequest.getProjectId()));
                String originId = apiDefinition.getId();
                apiDefinition.setId(UUID.randomUUID().toString());
                apiDefinition.setRefId(apiDefinition.getId());
                if (StringUtils.isNotEmpty(apiTestImportRequest.getVersionId())) {
                    apiDefinition.setVersionId(apiTestImportRequest.getVersionId());
                } else {
                    apiDefinition.setVersionId(apiTestImportRequest.getDefaultVersion());
                }
                apiDefinition.setLatest(true); // 新增的接口 latest = true
                batchMapper.insert(apiDefinition);
                String requestStr = setImportHashTree(apiDefinition);

                // case 设置版本
                cases.forEach(c -> {
                    c.setVersionId(apiDefinition.getVersionId());
                });
                reSetImportCasesApiId(cases, originId, apiDefinition.getId());
                reSetImportMocksApiId(mocks, originId, apiDefinition.getId(), apiDefinition.getNum());
                apiDefinition.setRequest(requestStr);
                importApiCase(apiDefinition, apiTestImportRequest);
            } else {
                //不覆盖的接口，如果没有sameRequest则不导入。此时清空mock信息
                mocks.clear();
            }
        } else {
            _importCreate(sameRequest, batchMapper, apiDefinition, apiTestCaseMapper, extApiDefinitionMapper, apiTestImportRequest, cases, mocks);
        }

        return apiDefinition;
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
        order = (order == null ? 0 : order) + ServiceUtils.ORDER_STEP;
        currentApiCaseOrder.set(order);
        return order;
    }

    private void _importCreate(List<ApiDefinition> sameRequest, ApiDefinitionMapper batchMapper, ApiDefinitionWithBLOBs apiDefinition,
                               ApiTestCaseMapper apiTestCaseMapper, ExtApiDefinitionMapper extApiDefinitionMapper,
                               ApiTestImportRequest apiTestImportRequest, List<ApiTestCaseWithBLOBs> cases, List<MockConfigImportDTO> mocks) {
        String originId = apiDefinition.getId();

        if (CollectionUtils.isEmpty(sameRequest)) { // 没有这个接口 新增
            apiDefinition.setId(UUID.randomUUID().toString());
            apiDefinition.setRefId(apiDefinition.getId());
            if (StringUtils.isNotEmpty(apiTestImportRequest.getVersionId())) {
                apiDefinition.setVersionId(apiTestImportRequest.getVersionId());
            } else {
                apiDefinition.setVersionId(apiTestImportRequest.getDefaultVersion());
            }
            apiDefinition.setLatest(true); // 新增接口 latest = true
            // case 设置版本
            cases.forEach(c -> {
                c.setVersionId(apiDefinition.getVersionId());
            });
            apiDefinition.setOrder(getImportNextOrder(apiTestImportRequest.getProjectId()));
            reSetImportCasesApiId(cases, originId, apiDefinition.getId());
            reSetImportMocksApiId(mocks, originId, apiDefinition.getId(), apiDefinition.getNum());
            if (StringUtils.equalsIgnoreCase(apiDefinition.getProtocol(), RequestType.HTTP)) {
                batchMapper.insert(apiDefinition);
                String request = setImportHashTree(apiDefinition);
                apiDefinition.setRequest(request);
                importApiCase(apiDefinition, apiTestImportRequest);
            } else {
                if (StringUtils.equalsAnyIgnoreCase(apiDefinition.getProtocol(), RequestType.TCP)) {
                    setImportTCPHashTree(apiDefinition);
                }
                batchMapper.insert(apiDefinition);
            }

        } else { //如果存在则修改
            if (StringUtils.isEmpty(apiTestImportRequest.getUpdateVersionId())) {
                apiTestImportRequest.setUpdateVersionId(apiTestImportRequest.getDefaultVersion());
            }
            Optional<ApiDefinition> apiOp = sameRequest.stream()
                    .filter(api -> StringUtils.equals(api.getVersionId(), apiTestImportRequest.getUpdateVersionId()))
                    .findFirst();

            if (!apiOp.isPresent()) {
                apiDefinition.setId(UUID.randomUUID().toString());
                apiDefinition.setRefId(sameRequest.get(0).getRefId());
                apiDefinition.setVersionId(apiTestImportRequest.getUpdateVersionId());
                apiDefinition.setNum(sameRequest.get(0).getNum()); // 使用第一个num当作本次的num
                apiDefinition.setOrder(sameRequest.get(0).getOrder());
                if(sameRequest.get(0).getUserId()!=null){
                    apiDefinition.setUserId(sameRequest.get(0).getUserId());
                }
                batchMapper.insert(apiDefinition);
            } else {
                ApiDefinition existApi = apiOp.get();
                apiDefinition.setStatus(existApi.getStatus());
                apiDefinition.setOriginalState(existApi.getOriginalState());
                apiDefinition.setCaseStatus(existApi.getCaseStatus());
                apiDefinition.setNum(existApi.getNum()); //id 不变
                apiDefinition.setRefId(existApi.getRefId());
                apiDefinition.setVersionId(apiTestImportRequest.getUpdateVersionId());
                if(existApi.getUserId()!=null){
                    apiDefinition.setUserId(existApi.getUserId());
                }
                // case 设置版本
                cases.forEach(c -> {
                    c.setVersionId(apiDefinition.getVersionId());
                });
                if (!StringUtils.equalsIgnoreCase(apiTestImportRequest.getPlatform(), ApiImportPlatform.Metersphere.name())) {
                    apiDefinition.setTags(existApi.getTags()); // 其他格式 tag 不变，MS 格式替换
                }
                if (StringUtils.equalsIgnoreCase(apiDefinition.getProtocol(), RequestType.HTTP)) {
                    //如果存在则修改
                    apiDefinition.setId(existApi.getId());
                    String request = setImportHashTree(apiDefinition);
                    apiDefinition.setModuleId(existApi.getModuleId());
                    apiDefinition.setModulePath(existApi.getModulePath());
                    apiDefinition.setOrder(existApi.getOrder());
                    apiDefinitionMapper.updateByPrimaryKeyWithBLOBs(apiDefinition);
                    apiDefinition.setRequest(request);
                    reSetImportCasesApiId(cases, originId, apiDefinition.getId());
                    reSetImportMocksApiId(mocks, originId, apiDefinition.getId(), apiDefinition.getNum());
                    importApiCase(apiDefinition, apiTestImportRequest);
                } else {
                    apiDefinition.setId(existApi.getId());
                    if (StringUtils.equalsAnyIgnoreCase(apiDefinition.getProtocol(), RequestType.TCP)) {
                        setImportTCPHashTree(apiDefinition);
                    }
                    apiDefinition.setOrder(existApi.getOrder());
                    reSetImportCasesApiId(cases, originId, apiDefinition.getId());
                    reSetImportMocksApiId(mocks, originId, apiDefinition.getId(), apiDefinition.getNum());
                    apiDefinitionMapper.updateByPrimaryKeyWithBLOBs(apiDefinition);
                }
            }
            extApiDefinitionMapper.clearLatestVersion(apiDefinition.getRefId());
            extApiDefinitionMapper.addLatestVersion(apiDefinition.getRefId());
        }
    }

    /**
     * 如果是MS格式，带用例导出，最后创建用例，重新设置接口id
     *
     * @param cases
     * @param originId
     * @param newId
     */
    private void reSetImportCasesApiId(List<ApiTestCaseWithBLOBs> cases, String originId, String newId) {
        if (CollectionUtils.isNotEmpty(cases)) {
            cases.forEach(item -> {
                if (StringUtils.equals(item.getApiDefinitionId(), originId)) {
                    item.setApiDefinitionId(newId);
                }
            });
        }
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

    private String setImportHashTree(ApiDefinitionWithBLOBs apiDefinition) {
        String request = apiDefinition.getRequest();
        MsHTTPSamplerProxy msHTTPSamplerProxy = JSONObject.parseObject(request, MsHTTPSamplerProxy.class);
        msHTTPSamplerProxy.setId(apiDefinition.getId());
        msHTTPSamplerProxy.setHashTree(new LinkedList<>());
        apiDefinition.setRequest(JSONObject.toJSONString(msHTTPSamplerProxy));
        return request;
    }

    private String setImportTCPHashTree(ApiDefinitionWithBLOBs apiDefinition) {
        String request = apiDefinition.getRequest();
        MsTCPSampler tcpSampler = JSONObject.parseObject(request, MsTCPSampler.class);
        tcpSampler.setId(apiDefinition.getId());
        tcpSampler.setHashTree(new LinkedList<>());
        apiDefinition.setRequest(JSONObject.toJSONString(tcpSampler));
        return request;
    }

    private void importMsCase(ApiDefinitionImport apiImport, SqlSession sqlSession,
                              ApiTestImportRequest request) {
        List<ApiTestCaseWithBLOBs> cases = apiImport.getCases();
        if (CollectionUtils.isNotEmpty(cases)) {
            int batchCount = 0;
            for (int i = 0; i < cases.size(); i++) {
                ApiTestCaseWithBLOBs item = cases.get(i);
                ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = apiDefinitionMapper.selectByPrimaryKey(item.getApiDefinitionId());
                if (apiDefinitionWithBLOBs == null) {
                    continue;
                }
                insertOrUpdateImportCase(item, request, apiDefinitionWithBLOBs);
            }
            if (batchCount % 300 == 0) {
                sqlSession.flushStatements();
            }
        }
    }

    /**
     * 导入是插件或者postman时创建用例
     * postman考虑是否有前置脚本
     */
    private void importApiCase(ApiDefinitionWithBLOBs apiDefinition, ApiTestImportRequest apiTestImportRequest) {
        try {
            if (StringUtils.equalsAnyIgnoreCase(apiTestImportRequest.getPlatform(), ApiImportPlatform.Plugin.name(), ApiImportPlatform.Postman.name())) {
                ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
                BeanUtils.copyBean(apiTestCase, apiDefinition);
                apiTestCase.setApiDefinitionId(apiDefinition.getId());
                apiTestCase.setPriority("P0");
                if (apiTestCase.getName().length() > 255) {
                    apiTestCase.setName(apiTestCase.getName().substring(0, 255));
                }
                insertOrUpdateImportCase(apiTestCase, apiTestImportRequest, apiDefinition);
            }
        } catch (Exception e) {
            LogUtil.error("导入创建用例异常", e);
        }
    }

    private void insertOrUpdateImportCase(ApiTestCaseWithBLOBs apiTestCase, ApiTestImportRequest apiTestImportRequest, ApiDefinitionWithBLOBs apiDefinition) {
        SaveApiTestCaseRequest checkRequest = new SaveApiTestCaseRequest();
        checkRequest.setName(apiTestCase.getName());
        checkRequest.setApiDefinitionId(apiTestCase.getApiDefinitionId());
        checkRequest.setId(apiTestCase.getId());
        ApiTestCase sameCase = apiTestCaseService.getSameCase(checkRequest);
        if (sameCase == null) {
            sameCase = apiTestCaseService.getSameCaseById(checkRequest);
        }
        apiTestCase.setUpdateUserId(SessionUtils.getUserId());
        if (sameCase == null) {
            apiTestCase.setId(UUID.randomUUID().toString());
            apiTestCase.setStatus("");
            apiTestCase.setNum(apiTestCaseService.getNextNum(apiTestCase.getApiDefinitionId(), apiDefinition.getNum()));
            apiTestCase.setCreateTime(System.currentTimeMillis());
            apiTestCase.setUpdateTime(System.currentTimeMillis());
            apiTestCase.setCreateUserId(SessionUtils.getUserId());
            apiTestCase.setProjectId(SessionUtils.getCurrentProjectId());
            apiTestCase.setOrder(getImportNextCaseOrder(apiTestImportRequest.getProjectId()));
            apiTestCaseMapper.insert(apiTestCase);
        } else if (StringUtils.equals("fullCoverage", apiTestImportRequest.getModeId())) {
            apiTestCase.setId(sameCase.getId());
            apiTestCase.setUpdateTime(System.currentTimeMillis());
            apiTestCase.setCreateTime(null);
            apiTestCase.setPriority(sameCase.getPriority());
            apiTestCase.setCreateUserId(sameCase.getCreateUserId());
            apiTestCase.setNum(sameCase.getNum());
            apiTestCase.setOrder(sameCase.getOrder());
            apiTestCase.setProjectId(sameCase.getProjectId());
            apiTestCase.setVersion((sameCase.getVersion() == null ? 0 : sameCase.getVersion()) + 1);
            apiTestCaseMapper.updateByPrimaryKeySelective(apiTestCase);
        }
    }

    private void deleteFileByTestId(String apiId) {
        ApiTestFileExample apiTestFileExample = new ApiTestFileExample();
        apiTestFileExample.createCriteria().andTestIdEqualTo(apiId);
        final List<ApiTestFile> ApiTestFiles = apiTestFileMapper.selectByExample(apiTestFileExample);
        apiTestFileMapper.deleteByExample(apiTestFileExample);

        if (!CollectionUtils.isEmpty(ApiTestFiles)) {
            final List<String> fileIds = ApiTestFiles.stream().map(ApiTestFile::getFileId).collect(Collectors.toList());
            fileService.deleteFileByIds(fileIds);
        }
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
            String testId = request.getTestElement() != null &&
                    CollectionUtils.isNotEmpty(request.getTestElement().getHashTree()) &&
                    CollectionUtils.isNotEmpty(request.getTestElement().getHashTree().get(0).getHashTree()) ?
                    request.getTestElement().getHashTree().get(0).getHashTree().get(0).getName() : request.getId();
            ApiDefinitionExecResult result = ApiDefinitionExecResultUtil.add(testId, APITestStatus.Running.name(), request.getId());
            result.setProjectId(request.getProjectId());
            result.setTriggerMode(TriggerMode.MANUAL.name());
            apiDefinitionExecResultMapper.insert(result);
        }
        return apiExecuteService.debug(request, bodyFiles);
    }

    /**
     * 获取存储执行结果报告
     *
     * @param testId
     * @return
     */
    public APIReportResult getDbResult(String testId) {
        ApiDefinitionExecResult result = extApiDefinitionExecResultMapper.selectMaxResultByResourceId(testId);
        return buildAPIReportResult(result);
    }

    public APIReportResult getByResultId(String reportId) {
        ApiDefinitionExecResult result = apiDefinitionExecResultMapper.selectByPrimaryKey(reportId);
        return buildAPIReportResult(result);
    }

    public APIReportResult getReportById(String testId) {
        ApiDefinitionExecResult result = apiDefinitionExecResultMapper.selectByPrimaryKey(testId);
        return buildAPIReportResult(result);
    }

    public APIReportResult buildAPIReportResult(ApiDefinitionExecResult result) {
        if (result == null) {
            return null;
        }
        APIReportResult reportResult = new APIReportResult();
        reportResult.setStatus(result.getStatus());
        reportResult.setContent(result.getContent());
        return reportResult;
    }

    public APIReportResult getDbResult(String testId, String type) {
        ApiDefinitionExecResult result = extApiDefinitionExecResultMapper.selectMaxResultByResourceIdAndType(testId, type);
        return buildAPIReportResult(result);
    }

    public ApiDefinitionExecResult getResultByJenkins(String testId, String type) {
        return extApiDefinitionExecResultMapper.selectMaxResultByResourceIdAndType(testId, type);
    }

    public ApiTestCaseWithBLOBs getApiCaseInfo(String apiCaseId) {
        return apiTestCaseMapper.selectByPrimaryKey(apiCaseId);
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
        ApiImportParser runService = ApiDefinitionImportParserFactory.getApiImportParser(request.getPlatform());
        ApiDefinitionImport apiImport = null;
        try {
            apiImport = (ApiDefinitionImport) Objects.requireNonNull(runService).parse(file == null ? null : file.getInputStream(), request);
            if (apiImport.getMocks() == null) {
                apiImport.setMocks(new ArrayList<>());
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            String returnThrowException = e.getMessage();
            if (StringUtils.contains(returnThrowException, "模块树最大深度为")) {
                MSException.throwException(returnThrowException);
            } else {
                MSException.throwException(Translator.get("parse_data_error"));
            }
            // 发送通知
            if (StringUtils.equals(request.getType(), "schedule")) {
                String scheduleId = scheduleService.getScheduleInfo(request.getResourceId());
                String context = request.getSwaggerUrl() + "导入失败";
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("url", request.getSwaggerUrl());
                paramMap.put("projectId", request.getProjectId());
                NoticeModel noticeModel = NoticeModel.builder()
                        .operator(SessionUtils.getUserId())
                        .context(context)
                        .testId(scheduleId)
                        .subject(Translator.get("swagger_url_scheduled_import_notification"))
                        .failedMailTemplate("SwaggerImportFaild")
                        .paramMap(paramMap)
                        .event(NoticeConstants.Event.IMPORT)
                        .build();
                noticeSendService.send(NoticeConstants.TaskType.SWAGGER_TASK, noticeModel);
            }
        }
        try {
            importApi(request, apiImport);
            if (CollectionUtils.isNotEmpty(apiImport.getData())) {
                List<String> names = apiImport.getData().stream().map(ApiDefinitionWithBLOBs::getName).collect(Collectors.toList());
                request.setName(String.join(",", names));
                List<String> ids = apiImport.getData().stream().map(ApiDefinitionWithBLOBs::getId).collect(Collectors.toList());
                request.setId(JSON.toJSONString(ids));
            }
            // 发送通知
            if (StringUtils.equals(request.getType(), "schedule")) {
                String scheduleId = scheduleService.getScheduleInfo(request.getResourceId());
                String context = request.getSwaggerUrl() + "导入成功";
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("url", request.getSwaggerUrl());
                NoticeModel noticeModel = NoticeModel.builder()
                        .operator(SessionUtils.getUserId())
                        .context(context)
                        .testId(scheduleId)
                        .subject(Translator.get("swagger_url_scheduled_import_notification"))
                        .successMailTemplate("SwaggerImport")
                        .paramMap(paramMap)
                        .event(NoticeConstants.Event.EXECUTE_SUCCESSFUL)
                        .build();
                noticeSendService.send(NoticeConstants.Mode.SCHEDULE, "", noticeModel);
            }
        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException(Translator.get("user_import_format_wrong"));
        }
        return apiImport;
    }

    private void importApi(ApiTestImportRequest request, ApiDefinitionImport apiImport) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        currentApiCaseOrder.remove();
        currentApiOrder.remove();
        List<ApiDefinitionWithBLOBs> data = apiImport.getData();
        ApiDefinitionMapper batchMapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        ApiTestCaseMapper apiTestCaseMapper = sqlSession.getMapper(ApiTestCaseMapper.class);
        ExtApiDefinitionMapper extApiDefinitionMapper = sqlSession.getMapper(ExtApiDefinitionMapper.class);

        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.URL_REPEATABLE.name());
        boolean urlRepeat = config.getUrlRepeatable();
        int num = 0;
        if (!CollectionUtils.isEmpty(data) && data.get(0) != null && data.get(0).getProjectId() != null) {
            num = getNextNum(data.get(0).getProjectId());
        }
        String defaultVersion = extProjectVersionMapper.getDefaultVersion(request.getProjectId());
        request.setDefaultVersion(defaultVersion);
        for (int i = 0; i < data.size(); i++) {
            ApiDefinitionWithBLOBs item = data.get(i);
            this.setModule(item);
            if (item.getName().length() > 255) {
                item.setName(item.getName().substring(0, 255));
            }
            item.setNum(num++);
            //如果EsbData需要存储,则需要进行接口是否更新的判断
            if (apiImport.getEsbApiParamsMap() != null) {
                String apiId = item.getId();
                EsbApiParamsWithBLOBs model = apiImport.getEsbApiParamsMap().get(apiId);
                request.setModeId("fullCoverage");//标准版ESB数据导入不区分是否覆盖，默认都为覆盖
                importCreate(item, batchMapper, apiTestCaseMapper, extApiDefinitionMapper, request, apiImport.getCases(), apiImport.getMocks(), urlRepeat);
                if (model != null) {
                    apiImport.getEsbApiParamsMap().remove(apiId);
                    model.setResourceId(item.getId());
                    apiImport.getEsbApiParamsMap().put(item.getId(), model);
                }
            } else {
                importCreate(item, batchMapper, apiTestCaseMapper, extApiDefinitionMapper, request, apiImport.getCases(), apiImport.getMocks(), urlRepeat);
            }
            if (i % 300 == 0) {
                sqlSession.flushStatements();
            }
        }
        sqlSession.flushStatements();
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

        if (!CollectionUtils.isEmpty(apiImport.getCases())) {
            importMsCase(apiImport, sqlSession, request);
        }
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }


    public ReferenceDTO getReference(ApiScenarioRequest request) {
        ReferenceDTO dto = new ReferenceDTO();
        dto.setScenarioList(extApiScenarioMapper.selectReference(request));
        QueryTestPlanRequest planRequest = new QueryTestPlanRequest();
        planRequest.setApiId(request.getId());
        planRequest.setProjectId(request.getProjectId());
        dto.setTestPlanList(extTestPlanMapper.selectTestPlanByRelevancy(planRequest));
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
        //name在这里只是查询参数
        request.setName(null);
        ApiDefinitionWithBLOBs definitionWithBLOBs = new ApiDefinitionWithBLOBs();
        BeanUtils.copyBean(definitionWithBLOBs, request);
        definitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMapper.updateByExampleSelective(definitionWithBLOBs, getBatchExample(request));
    }

    public void testPlanRelevance(ApiCaseRelevanceRequest request) {
        apiTestCaseService.relevanceByApi(request);
    }

    public void testCaseReviewRelevance(ApiCaseRelevanceRequest request) {
        apiTestCaseService.relevanceByApiByReview(request);
    }

    /**
     * 数据统计-接口类型
     *
     * @param projectId 项目ID
     * @return
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

    public List<ApiDefinition> selectApiDefinitionBydIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(ids);
        return apiDefinitionMapper.selectByExample(example);
    }

    public void deleteByParams(ApiBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiDefinitionMapper.selectIds(query));
        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ids.forEach(id -> {
            // 把所有版本的api移到回收站
            ApiDefinitionWithBLOBs api = apiDefinitionMapper.selectByPrimaryKey(id);
            if (api == null) {
                return;
            }
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andRefIdEqualTo(api.getRefId());
            List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);

            List<String> apiIds = apiDefinitions.stream().map(ApiDefinition::getId).collect(Collectors.toList());
            apiTestCaseService.deleteBatchByDefinitionId(apiIds);
            //
            apiDefinitionMapper.deleteByExample(example);
        });
    }

    public ApiDefinitionExample getBatchExample(ApiBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiDefinitionMapper.selectIds(query));
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(request.getIds());
        return example;
    }

    private List<String> getAllApiIdsByFontedSelect(Map<String, List<String>> filters, String name, List<String> moduleIds, String projectId, List<String> unSelectIds, String protocol) {
        ApiDefinitionRequest request = new ApiDefinitionRequest();
        request.setFilters(filters);
        request.setName(name);
        request.setModuleIds(moduleIds);
        request.setProjectId(projectId);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        request.setProtocol(protocol);
        List<ApiDefinitionResult> resList = extApiDefinitionMapper.list(request);
        List<String> ids = new ArrayList<>(0);
        if (!resList.isEmpty()) {
            List<String> allIds = resList.stream().map(ApiDefinitionResult::getId).collect(Collectors.toList());
            ids = allIds.stream().filter(id -> !unSelectIds.contains(id)).collect(Collectors.toList());
        }
        return ids;
    }

    public void removeToGcByParams(ApiBatchRequest request) {
        // 去除Name排序
        if (request.getCondition() != null && CollectionUtils.isNotEmpty(request.getCondition().getOrders())) {
            request.getCondition().getOrders().clear();
        }
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiDefinitionMapper.selectIds(query));

        this.removeToGc(request.getIds());
//        ApiDefinitionExampleWithOperation example = new ApiDefinitionExampleWithOperation();
//        example.createCriteria().andIdIn(request.getIds());
//        example.setOperator(SessionUtils.getUserId());
//        example.setOperationTime(System.currentTimeMillis());
//        extApiDefinitionMapper.removeToGcByExample(example);
    }

    public Pager<List<ApiDefinitionResult>> listRelevance(ApiDefinitionRequest request, int goPage, int pageSize) {
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        if (StringUtils.isNotBlank(request.getPlanId()) && testPlanService.isAllowedRepeatCase(request.getPlanId())) {
            request.setRepeatCase(true);
        }
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        List<ApiDefinitionResult> resList = extApiDefinitionMapper.listRelevance(request);
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
                List<Scenario> scenarioList = extApiDefinitionMapper.scenarioList(res.getId());
                String count = String.valueOf(scenarioList.size());
                res.setScenarioTotal(count);
                String[] strings = new String[scenarioList.size()];
                String[] ids2 = scenarioList.stream().map(Scenario::getId).collect(Collectors.toList()).toArray(strings);
                res.setIds(ids2);
                res.setScenarioType("scenario");
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
                        res.setCaseStatus(Translator.get("not_execute"));
                    } else if (compRes.getError() > 0) {
                        res.setCaseStatus(Translator.get("execute_not_pass"));
                    } else {
                        res.setCaseStatus(Translator.get("execute_pass"));
                    }
                } else {
                    res.setCaseType("apiCase");
                    res.setCaseTotal("-");
                    res.setCasePassingRate("-");
                    res.setCaseStatus("-");
                }

                apiDefinitionMapper.updateByPrimaryKeySelective(res);
                if (StringUtils.equalsIgnoreCase("esb", res.getMethod())) {
                    esbApiParamService.handleApiEsbParams(res);
                }
            }
        }
    }

    /*swagger定时导入*/
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


    // 设置 SwaggerUrl 同步鉴权参数
    public String setAuthParams(ScheduleRequest request) {
        // list 数组转化成 json 字符串
        JSONObject configObj = new JSONObject();
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
        String resourceId = "";
        if (list.size() == 1) {
            resourceId = list.get(0).getId();
        }
        return resourceId;
    }

    public List<SwaggerTaskResult> getSwaggerScheduleList(String projectId) {
        List<SwaggerTaskResult> resultList = extSwaggerUrlScheduleMapper.getSwaggerTaskList(projectId);
        int dataIndex = 1;
        for (SwaggerTaskResult swaggerTaskResult :
                resultList) {
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
        ApiExportResult apiExportResult;
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiDefinitionMapper.selectIds(query));
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(request.getIds());

        if (StringUtils.equals(type, "MS")) { //  导出为 Metersphere 格式
            MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
            apiExportResult = new MsApiExportResult();
            ((MsApiExportResult) apiExportResult).setData(apiDefinitionMapper.selectByExampleWithBLOBs(example));
            ((MsApiExportResult) apiExportResult).setCases(apiTestCaseService.selectCasesBydApiIds(request.getIds()));
            ((MsApiExportResult) apiExportResult).setMocks(mockConfigService.selectMockExpectConfigByApiIdIn(request.getIds()));
            ((MsApiExportResult) apiExportResult).setProjectName(request.getProjectId());
            ((MsApiExportResult) apiExportResult).setProtocol(request.getProtocol());
            ((MsApiExportResult) apiExportResult).setProjectId(request.getProjectId());
            ((MsApiExportResult) apiExportResult).setVersion(System.getenv("MS_VERSION"));
            if (CollectionUtils.isNotEmpty(((MsApiExportResult) apiExportResult).getData())) {
                List<String> names = ((MsApiExportResult) apiExportResult).getData().stream().map(ApiDefinitionWithBLOBs::getName).collect(Collectors.toList());
                request.setName(String.join(",", names));
                List<String> ids = ((MsApiExportResult) apiExportResult).getData().stream().map(ApiDefinitionWithBLOBs::getId).collect(Collectors.toList());
                request.setId(JSON.toJSONString(ids));
            }
        } else { //  导出为 Swagger 格式
            Swagger3Parser swagger3Parser = new Swagger3Parser();
            apiExportResult = swagger3Parser.swagger3Export(apiDefinitionMapper.selectByExampleWithBLOBs(example));
        }

        return apiExportResult;
    }

    public List<ApiDefinition> selectEffectiveIdByProjectId(String projectId) {
        return extApiDefinitionMapper.selectEffectiveIdByProjectId(projectId);
    }

    public List<ApiDefinitionWithBLOBs> preparedUrl(String projectId, String method, String baseUrlSuffix) {

        if (StringUtils.isEmpty(baseUrlSuffix)) {
            return new ArrayList<>();
        } else {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andMethodEqualTo(method).andProjectIdEqualTo(projectId).andStatusNotEqualTo("Trash").andProtocolEqualTo("HTTP");
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
                urlParams[urlParams.length - 1] = "";
            }
            for (ApiDefinition api : apiList) {
                if(StringUtils.equalsAny(api.getPath(),baseUrlSuffix,"/"+baseUrlSuffix)){
                    apiIdList.add(api.getId());
                }else {
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
                    columns.clear();
                    definitions.forEach(item -> {
                        DetailColumn column = new DetailColumn(DefinitionReference.definitionColumns.get("method"), "method", item.getMethod(), null);
                        columns.add(column);
                    });
                } else if (StringUtils.isNotEmpty(request.getStatus())) {
                    columns.clear();
                    definitions.forEach(item -> {
                        DetailColumn column = new DetailColumn(DefinitionReference.definitionColumns.get("status"), "status", StatusReference.statusMap.get(item.getStatus()), null);
                        columns.add(column);
                    });
                } else if (StringUtils.isNotEmpty(request.getUserId())) {
                    columns.clear();
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
            List<String> names = apiTestCases.stream().map(ApiTestCase::getName).collect(Collectors.toList());
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getPlanId());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(request.getSelectIds()), testPlan.getProjectId(), String.join(",", names), testPlan.getCreator(), new LinkedList<>());
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
        apiList.forEach(item -> {
            apiIdList.add(item.getId());
        });
        this.removeToGc(apiIdList);
    }

    public APIReportResult getTestPlanApiCaseReport(String testId, String type) {
        ApiDefinitionExecResult result = extApiDefinitionExecResultMapper.selectPlanApiMaxResultByTestIdAndType(testId, type);
        return buildAPIReportResult(result);
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(ApiDefinitionWithBLOBs.class, ApiDefinitionMapper.class,
                extApiDefinitionMapper::selectProjectIds,
                extApiDefinitionMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, ApiDefinitionWithBLOBs.class,
                apiDefinitionMapper::selectByPrimaryKey,
                extApiDefinitionMapper::getPreOrder,
                extApiDefinitionMapper::getLastOrder,
                apiDefinitionMapper::updateByPrimaryKeySelective);
    }

    public ApiDefinitionResult getById(String id) {
        ApiDefinitionRequest request = new ApiDefinitionRequest();
        request.setId(id);
        List<ApiDefinitionResult> list = extApiDefinitionMapper.list(request);
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
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

    public long countQuotedApiByProjectId(String projectId) {
        return extApiDefinitionMapper.countQuotedApiByProjectId(projectId);
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
            JSONObject object = JSON.parseObject(bloBs.getResponse());
            JSONObject body = (JSONObject) object.get("body");
            if (body != null) {
                String raw = body.getString("raw");
                String dataType = body.getString("type");
                if ((StringUtils.isNotEmpty(raw) || StringUtils.isNotEmpty(body.getString("jsonSchema"))) && StringUtils.isNotEmpty(dataType)) {
                    if (StringUtils.equals(type, "JSON")) {
                        String format = body.getString("format");
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
        ProjectService projectService = CommonBeanFactory.getBean(ProjectService.class);
        projectService.checkProjectIsRepeatable(request.getProjectId());
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiDefinitionMapper.selectIds(query));
        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) return;
        List<ApiDefinitionWithBLOBs> apis = getByIds(ids);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDefinitionMapper mapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        Long nextOrder = ServiceUtils.getNextOrder(request.getProjectId(), extApiDefinitionMapper::getLastOrder);

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
                if (i % 50 == 0)
                    sqlSession.flushStatements();
            }
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public ApiDefinition getApiDefinition(ApiDefinitionExample apiDefinitionExample) {
        List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(apiDefinitionExample);
        if(apiDefinitions==null||apiDefinitions.size()==0){
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

}
