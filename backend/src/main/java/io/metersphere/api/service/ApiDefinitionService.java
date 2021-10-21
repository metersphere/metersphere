package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.automation.ReferenceDTO;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImportParserFactory;
import io.metersphere.api.dto.definition.parse.Swagger3Parser;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.dto.mockconfig.MockConfigImportDTO;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.dto.scenario.Scenario;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.dto.swaggerurl.SwaggerTaskResult;
import io.metersphere.api.dto.swaggerurl.SwaggerUrlRequest;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.RequestResult;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.api.parse.ApiImportParser;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.dto.BaseSystemConfigDTO;
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
import io.metersphere.service.FileService;
import io.metersphere.service.RelationshipEdgeService;
import io.metersphere.service.ScheduleService;
import io.metersphere.service.SystemParameterService;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jorphan.collections.HashTree;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sun.security.util.Cache;

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
    private JMeterService jMeterService;
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
    private ApiTestEnvironmentService environmentService;
    @Resource
    private EsbApiParamService esbApiParamService;
    @Resource
    private TcpApiParamService tcpApiParamService;
    @Resource
    private ApiModuleMapper apiModuleMapper;
    @Resource
    private SystemParameterService systemParameterService;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private RelationshipEdgeService relationshipEdgeService;

    private static Cache cache = Cache.newHardMemoryCache(0, 3600);

    private ThreadLocal<Long> currentApiOrder = new ThreadLocal<>();
    private ThreadLocal<Long> currentApiCaseOrder = new ThreadLocal<>();

    public List<ApiDefinitionResult> list(ApiDefinitionRequest request) {
        request = this.initRequest(request, true, true);
        List<ApiDefinitionResult> resList = extApiDefinitionMapper.list(request);
        calculateResult(resList, request.getProjectId());
        return resList;
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
        if (StringUtils.equals(request.getProtocol(), "DUBBO")) {
            request.setMethod("dubbo://");
        }
        ApiDefinitionWithBLOBs returnModel = createTest(request);
        FileUtils.createBodyFiles(request.getRequest().getId(), bodyFiles);
        return returnModel;
    }

    public ApiDefinitionWithBLOBs update(SaveApiDefinitionRequest request, List<MultipartFile> bodyFiles) {
        if (request.getRequest() != null) {
            deleteFileByTestId(request.getRequest().getId());
        }
        request.setBodyUploadIds(null);
        if (StringUtils.equals(request.getProtocol(), "DUBBO")) {
            request.setMethod("dubbo://");
        }
        ApiDefinitionWithBLOBs returnModel = updateTest(request);
        FileUtils.createBodyFiles(request.getRequest().getId(), bodyFiles);
        return returnModel;
    }

    public void delete(String apiId) {
        apiTestCaseService.deleteTestCase(apiId);
        deleteFileByTestId(apiId);
        extApiDefinitionExecResultMapper.deleteByResourceId(apiId);
        apiDefinitionMapper.deleteByPrimaryKey(apiId);
        esbApiParamService.deleteByResourceId(apiId);
        MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
        mockConfigService.deleteMockConfigByApiId(apiId);
        relationshipEdgeService.delete(apiId); // 删除关系图
        FileUtils.deleteBodyFiles(apiId);
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
        }
    }

    public void removeToGc(List<String> apiIds) {
        if (CollectionUtils.isEmpty(apiIds)) {
            return;
        }
        ApiDefinitionExampleWithOperation example = new ApiDefinitionExampleWithOperation();
        example.createCriteria().andIdIn(apiIds);
        example.setOperator(SessionUtils.getUserId());
        example.setOperationTime(System.currentTimeMillis());
        extApiDefinitionMapper.removeToGcByExample(example);

        List<String> apiCaseIds = apiTestCaseService.selectCaseIdsByApiIds(apiIds);
        if (CollectionUtils.isNotEmpty(apiCaseIds)) {
            ApiTestBatchRequest apiTestBatchRequest = new ApiTestBatchRequest();
            apiTestBatchRequest.setIds(apiCaseIds);
            apiTestBatchRequest.setUnSelectIds(new ArrayList<>());
            apiTestCaseService.deleteToGcByParam(apiTestBatchRequest);
        }
    }

    public void reduction(ApiBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extApiDefinitionMapper.selectIds(query));
        if (request.getIds() != null || !request.getIds().isEmpty()) {
            //检查模块是否还在

            //检查原来模块是否还在
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(request.getIds());
            List<ApiDefinition> reductionCaseList = apiDefinitionMapper.selectByExample(example);
            Map<String, List<ApiDefinition>> nodeMap = new HashMap<>();
            for (ApiDefinition api : reductionCaseList) {
                String moduleId = api.getModuleId();
                if (StringUtils.isEmpty(moduleId)) {
                    moduleId = "";
                }
                if (nodeMap.containsKey(moduleId)) {
                    nodeMap.get(moduleId).add(api);
                } else {
                    List<ApiDefinition> list = new ArrayList<>();
                    list.add(api);
                    nodeMap.put(moduleId, list);
                }
            }
//            Map<String,List<ApiDefinition>> nodeMap = reductionCaseList.stream().collect(Collectors.groupingBy(ApiDefinition :: getModuleId));
            ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
            for (Map.Entry<String, List<ApiDefinition>> entry : nodeMap.entrySet()) {
                String nodeId = entry.getKey();
                long nodeCount = apiModuleService.countById(nodeId);
                if (nodeCount <= 0) {
                    String projectId = request.getProjectId();
                    ApiModule node = apiModuleService.getDefaultNode(projectId, request.getProtocol());
                    List<ApiDefinition> testCaseList = entry.getValue();
                    for (ApiDefinition apiDefinition : testCaseList) {
                        ApiDefinitionWithBLOBs updateCase = new ApiDefinitionWithBLOBs();
                        updateCase.setId(apiDefinition.getId());
                        updateCase.setModuleId(node.getId());
                        updateCase.setModulePath("/" + node.getName());

                        apiDefinitionMapper.updateByPrimaryKeySelective(updateCase);
                    }
                }
            }
            extApiDefinitionMapper.checkOriginalStatusByIds(request.getIds());
            extApiDefinitionMapper.reduction(request.getIds());

            List<String> apiCaseIds = apiTestCaseService.selectCaseIdsByApiIds(request.getIds());
            if (CollectionUtils.isNotEmpty(apiCaseIds)) {
                ApiTestBatchRequest apiTestBatchRequest = new ApiTestBatchRequest();
                apiTestBatchRequest.setIds(apiCaseIds);
                apiTestBatchRequest.setUnSelectIds(new ArrayList<>());
                apiTestCaseService.reduction(apiTestBatchRequest);
            }
        }
    }

    private void checkNameExist(SaveApiDefinitionRequest request) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        if (StringUtils.isNotEmpty(request.getProtocol()) && request.getProtocol().equals(RequestType.HTTP)) {
            ApiDefinitionExample.Criteria criteria = example.createCriteria();
            criteria.andMethodEqualTo(request.getMethod()).andStatusNotEqualTo("Trash")
                    .andProtocolEqualTo(request.getProtocol()).andPathEqualTo(request.getPath())
                    .andProjectIdEqualTo(request.getProjectId()).andIdNotEqualTo(request.getId());
            Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
            if (project != null && project.getRepeatable() != null && project.getRepeatable()) {
                criteria.andNameEqualTo(request.getName());
            }
            if (apiDefinitionMapper.countByExample(example) > 0) {
                MSException.throwException(Translator.get("api_definition_url_not_repeating"));
            }
        } else {
            example.createCriteria().andProtocolEqualTo(request.getProtocol()).andStatusNotEqualTo("Trash")
                    .andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId())
                    .andIdNotEqualTo(request.getId());
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
        test.setFollowPeople(request.getFollowPeople());
        if (StringUtils.isNotEmpty(request.getTags()) && !StringUtils.equals(request.getTags(), "[]")) {
            test.setTags(request.getTags());
        } else {
            test.setTags("");
        }
        this.setModule(test);
        apiDefinitionMapper.updateByPrimaryKeySelective(test);
        // 同步修改用例
        List<String> ids = new ArrayList<>();
        ids.add(request.getId());
        apiTestCaseService.updateByApiDefinitionId(ids, test.getPath(), test.getMethod(), test.getProtocol());

        return test;
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
        test.setFollowPeople(request.getFollowPeople());
        test.setRemark(request.getRemark());
        test.setOrder(ServiceUtils.getNextOrder(request.getProjectId(), extApiDefinitionMapper::getLastOrder));
        if (StringUtils.isEmpty(request.getModuleId()) || "default-module".equals(request.getModuleId())) {
            ApiModuleExample example = new ApiModuleExample();
            example.createCriteria().andProjectIdEqualTo(test.getProjectId()).andProtocolEqualTo(test.getProtocol()).andNameEqualTo("未规划接口");
            List<ApiModule> modules = apiModuleMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(modules)) {
                test.setModuleId(modules.get(0).getId());
                test.setModulePath("/未规划接口");
            }
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
        apiDefinitionMapper.insert(test);
        return test;
    }

    private int getNextNum(String projectId) {
        ApiDefinition apiDefinition = extApiDefinitionMapper.getNextNum(projectId);
        if (apiDefinition == null || apiDefinition.getNum() == null) {
            return 100001;
        } else {
            return Optional.of(apiDefinition.getNum() + 1).orElse(100001);
        }
    }

    private ApiDefinition importCreate(ApiDefinitionWithBLOBs apiDefinition, ApiDefinitionMapper batchMapper,
                                       ApiTestCaseMapper apiTestCaseMapper, ApiTestImportRequest apiTestImportRequest, List<ApiTestCaseWithBLOBs> cases, List<MockConfigImportDTO> mocks,
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
        apiDefinition.setDescription(apiDefinition.getDescription());

        List<ApiDefinition> sameRequest;
        if (repeatable == null || repeatable == false) {
            sameRequest = getSameRequest(saveReq);
        } else {
            // 如果勾选了允许重复，则判断更新要加上name字段
            sameRequest = getSameRequestWithName(saveReq);
        }
        if (CollectionUtils.isEmpty(sameRequest)) {
            sameRequest = getSameRequestById(apiDefinition.getId(), apiTestImportRequest.getProjectId());
        }
        if (StringUtils.equals("fullCoverage", apiTestImportRequest.getModeId())) {
            _importCreate(sameRequest, batchMapper, apiDefinition, apiTestCaseMapper, apiTestImportRequest, cases, mocks);
        } else if (StringUtils.equals("incrementalMerge", apiTestImportRequest.getModeId())) {
            if (CollectionUtils.isEmpty(sameRequest)) {
                //postman 可能含有前置脚本，接口定义去掉脚本
                apiDefinition.setOrder(getImportNextOrder(apiTestImportRequest.getProjectId()));
                String originId = apiDefinition.getId();
                apiDefinition.setId(UUID.randomUUID().toString());
                batchMapper.insert(apiDefinition);
                String requestStr = setImportHashTree(apiDefinition);
                reSetImportCasesApiId(cases, originId, apiDefinition.getId());
                apiDefinition.setRequest(requestStr);
                importApiCase(apiDefinition, apiTestImportRequest);
            }
        } else {
            _importCreate(sameRequest, batchMapper, apiDefinition, apiTestCaseMapper, apiTestImportRequest, cases, mocks);
        }

        return apiDefinition;
    }

    private Long getImportNextOrder(String projectId) {
        Long order = currentApiOrder.get();
        if (order == null) {
            order = ServiceUtils.getNextOrder(projectId, extApiDefinitionMapper::getLastOrder);
        }
        order = (order == null ? 0 : order) + 5000;
        currentApiOrder.set(order);
        return order;
    }

    private Long getImportNextCaseOrder(String projectId) {
        Long order = currentApiCaseOrder.get();
        if (order == null) {
            order = ServiceUtils.getNextOrder(projectId, extApiTestCaseMapper::getLastOrder);
        }
        order = (order == null ? 0 : order) + 5000;
        currentApiCaseOrder.set(order);
        return order;
    }

    private void _importCreate(List<ApiDefinition> sameRequest, ApiDefinitionMapper batchMapper, ApiDefinitionWithBLOBs apiDefinition,
                               ApiTestCaseMapper apiTestCaseMapper, ApiTestImportRequest apiTestImportRequest, List<ApiTestCaseWithBLOBs> cases, List<MockConfigImportDTO> mocks) {
        String originId = apiDefinition.getId();
        if (CollectionUtils.isEmpty(sameRequest)) {
            apiDefinition.setId(UUID.randomUUID().toString());
            apiDefinition.setOrder(getImportNextOrder(apiTestImportRequest.getProjectId()));
            reSetImportCasesApiId(cases, originId, apiDefinition.getId());
            reSetImportMocksApiId(mocks, originId, apiDefinition.getId());
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

        } else {
            apiDefinition.setStatus(sameRequest.get(0).getStatus());
            apiDefinition.setOriginalState(sameRequest.get(0).getOriginalState());
            apiDefinition.setCaseStatus(sameRequest.get(0).getCaseStatus());
            if (StringUtils.equalsIgnoreCase(apiDefinition.getProtocol(), RequestType.HTTP)) {
                //如果存在则修改
                apiDefinition.setId(sameRequest.get(0).getId());
                String request = setImportHashTree(apiDefinition);
                apiDefinition.setModuleId(sameRequest.get(0).getModuleId());
                apiDefinition.setModulePath(sameRequest.get(0).getModulePath());
                apiDefinition.setNum(sameRequest.get(0).getNum()); //id 不变
                apiDefinition.setOrder(sameRequest.get(0).getOrder());
                apiDefinitionMapper.updateByPrimaryKeyWithBLOBs(apiDefinition);
                apiDefinition.setRequest(request);
                reSetImportCasesApiId(cases, originId, apiDefinition.getId());
                importApiCase(apiDefinition, apiTestImportRequest);
            } else {
                apiDefinition.setId(sameRequest.get(0).getId());
                if (StringUtils.equalsAnyIgnoreCase(apiDefinition.getProtocol(), RequestType.TCP)) {
                    setImportTCPHashTree(apiDefinition);
                }
                apiDefinition.setOrder(sameRequest.get(0).getOrder());
                reSetImportCasesApiId(cases, originId, apiDefinition.getId());
                apiDefinitionMapper.updateByPrimaryKeyWithBLOBs(apiDefinition);
            }

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

    private void reSetImportMocksApiId(List<MockConfigImportDTO> mocks, String originId, String newId) {
        if (CollectionUtils.isNotEmpty(mocks)) {
            mocks.forEach(item -> {
                if (StringUtils.equals(item.getApiId(), originId)) {
                    item.setApiId(newId);
                }
            });
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
    public String run(RunDefinitionRequest request, List<MultipartFile> bodyFiles) {
        int count = 100;
        BaseSystemConfigDTO dto = systemParameterService.getBaseInfo();
        if (StringUtils.isNotEmpty(dto.getConcurrency())) {
            count = Integer.parseInt(dto.getConcurrency());
        }
        if (request.getTestElement() != null && request.getTestElement().getHashTree().size() == 1 && request.getTestElement().getHashTree().get(0).getHashTree().size() > count) {
            MSException.throwException("并发数量过大，请重新选择！");
        }

        ParameterConfig config = new ParameterConfig();
        config.setProjectId(request.getProjectId());

        Map<String, EnvironmentConfig> envConfig = new HashMap<>();
        Map<String, String> map = request.getEnvironmentMap();
        if (map != null && map.size() > 0) {
            for (String key : map.keySet()) {
                ApiTestEnvironmentWithBLOBs environment = environmentService.get(map.get(key));
                if (environment != null) {
                    EnvironmentConfig env = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
                    env.setApiEnvironmentid(environment.getId());
                    envConfig.put(key, env);
                }
            }
            config.setConfig(envConfig);
        }

        if (CollectionUtils.isNotEmpty(bodyFiles)) {
            List<MsHTTPSamplerProxy> requests = MsHTTPSamplerProxy.findHttpSampleFromHashTree(request.getTestElement());
            // 单接口调试生成tmp临时目录
            requests.forEach(item -> {
                Body body = item.getBody();
                String tmpFilePath = "tmp/" + UUID.randomUUID().toString();
                body.setTmpFilePath(tmpFilePath);
                FileUtils.copyBdyFile(item.getId(), tmpFilePath);
                FileUtils.createBodyFiles(tmpFilePath, bodyFiles);
            });
        }


        try {
            //检查TCP数据结构，等其他进行处理
            tcpApiParamService.checkTestElement(request.getTestElement());
        } catch (Exception e) {
        }

        HashTree hashTree = request.getTestElement().generateHashTree(config);
        String runMode = ApiRunMode.DEFINITION.name();
        if (StringUtils.isNotBlank(request.getType()) && StringUtils.equals(request.getType(), ApiRunMode.API_PLAN.name())) {
            runMode = ApiRunMode.API_PLAN.name();
        }

        // 调用执行方法
        if (request.getConfig() != null && StringUtils.isNotBlank(request.getConfig().getResourcePoolId())) {
            jMeterService.runTest(request.getId(), request.getId(), runMode, null, request.getConfig());
        } else {
            jMeterService.runLocal(request.getId(), hashTree, request.getReportId(), runMode);
        }
        return request.getId();
    }

    public void addResult(TestResult res) {
        if (res != null && CollectionUtils.isNotEmpty(res.getScenarios()) && res.getScenarios().get(0) != null && CollectionUtils.isNotEmpty(res.getScenarios().get(0).getRequestResults())) {
            RequestResult result = res.getScenarios().get(0).getRequestResults().get(0);
            if (result.getName().indexOf(DelimiterConstants.SEPARATOR.toString()) != -1) {
                result.setName(result.getName().substring(0, result.getName().indexOf(DelimiterConstants.SEPARATOR.toString())));
            }
            result.getResponseResult().setConsole(res.getConsole());
            cache.put(res.getTestId(), result);
        } else {
            RequestResult result = new RequestResult();
            result.getResponseResult().setConsole(res.getConsole());
            cache.put(res.getTestId(), result);
            //MSException.throwException(Translator.get("test_not_found"));
        }
    }

    /**
     * 获取零时执行结果报告
     *
     * @param testId
     * @param test
     * @return
     */
    public APIReportResult getResult(String testId, String test) {
        Object res = cache.get(testId);
        if (res != null) {
            cache.remove(testId);
            APIReportResult reportResult = new APIReportResult();
            reportResult.setContent(JSON.toJSONString(res));
            return reportResult;
        }
        return null;
    }

    public void removeCache(String testId) {
        if (StringUtils.isNotEmpty(testId)) {
            cache.remove(testId);
        }
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
        ApiImportParser apiImportParser = ApiDefinitionImportParserFactory.getApiImportParser(request.getPlatform());
        ApiDefinitionImport apiImport = null;
        try {
            apiImport = (ApiDefinitionImport) Objects.requireNonNull(apiImportParser).parse(file == null ? null : file.getInputStream(), request);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("parse_data_error"));
            // 发送通知
            if (StringUtils.equals(request.getType(), "schedule")) {
                String scheduleId = scheduleService.getScheduleInfo(request.getResourceId());
                String context = request.getSwaggerUrl() + "导入失败";
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("url", request.getSwaggerUrl());
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
        return apiImport;
    }

    private void importApi(ApiTestImportRequest request, ApiDefinitionImport apiImport) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        currentApiCaseOrder.remove();
        currentApiOrder.remove();
        List<ApiDefinitionWithBLOBs> data = apiImport.getData();
        ApiDefinitionMapper batchMapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        ApiTestCaseMapper apiTestCaseMapper = sqlSession.getMapper(ApiTestCaseMapper.class);
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        int num = 0;
        if (!CollectionUtils.isEmpty(data) && data.get(0) != null && data.get(0).getProjectId() != null) {
            num = getNextNum(data.get(0).getProjectId());
        }
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
                importCreate(item, batchMapper, apiTestCaseMapper, request, apiImport.getCases(), apiImport.getMocks(), project.getRepeatable());
                if (model != null) {
                    apiImport.getEsbApiParamsMap().remove(apiId);
                    model.setResourceId(item.getId());
                    apiImport.getEsbApiParamsMap().put(item.getId(), model);
                }
            } else {
                importCreate(item, batchMapper, apiTestCaseMapper, request, apiImport.getCases(), apiImport.getMocks(), project.getRepeatable());
            }
            if (i % 300 == 0) {
                sqlSession.flushStatements();
            }
            sqlSession.flushStatements();
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

        if (!CollectionUtils.isEmpty(apiImport.getCases())) {
            importMsCase(apiImport, sqlSession, request);
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
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(ids);
        return apiDefinitionMapper.selectByExample(example);
    }

    public void deleteByParams(ApiBatchRequest request) {
        apiDefinitionMapper.deleteByExample(getBatchExample(request));
        apiTestCaseService.deleteBatchByDefinitionId(request.getIds());
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

    public List<ApiDefinitionResult> listRelevance(ApiDefinitionRequest request) {
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        List<ApiDefinitionResult> resList = extApiDefinitionMapper.listRelevance(request);
        calculateResult(resList, request.getProjectId());
        return resList;
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

//    public List<ApiDefinition> selectByProjectIdAndMethodAndUrl(String projectId, String method,String url) {
//        ApiDefinitionExample example = new ApiDefinitionExample();
//        ApiDefinitionExample.Criteria criteria = example.createCriteria().andMethodEqualTo(method).andProjectIdEqualTo(projectId);
//        if(StringUtils.isNotEmpty(url)){
//            criteria.andPathEqualTo(url);
//        }
//        return  apiDefinitionMapper.selectByExample(example);
//    }

    public List<ApiDefinitionWithBLOBs> preparedUrl(String projectId, String method, String url, String urlSuffix) {

        if (StringUtils.isEmpty(urlSuffix)) {
            return new ArrayList<>();
        } else {
            if (StringUtils.equalsAnyIgnoreCase(method, "GET", "DELETE")) {
                ApiDefinitionExample example = new ApiDefinitionExample();
                ApiDefinitionExample.Criteria criteria = example.createCriteria().andMethodEqualTo(method).andProjectIdEqualTo(projectId);
                if (StringUtils.isNotEmpty(url)) {
                    criteria.andPathEqualTo(url);
                }
                List<ApiDefinition> apiList = apiDefinitionMapper.selectByExample(example);

                List<String> apiIdList = new ArrayList<>();
                boolean urlSuffixEndEmpty = false;
                if (urlSuffix.endsWith("/")) {
                    urlSuffixEndEmpty = true;
                    urlSuffix = urlSuffix + "testMock";
                }
                String[] urlParams = urlSuffix.split("/");
                if (urlSuffixEndEmpty) {
                    urlParams[urlParams.length - 1] = "";
                }
                for (ApiDefinition api : apiList) {
                    String path = api.getPath();
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
                if (apiIdList.isEmpty()) {
                    return new ArrayList<>();
                } else {
                    example.clear();
                    example.createCriteria().andIdIn(apiIdList);
                    return apiDefinitionMapper.selectByExampleWithBLOBs(example);
                }
            } else {
                if (!url.startsWith("/")) {
                    url = "/" + url;
                }
                ApiDefinitionExample example = new ApiDefinitionExample();
                ApiDefinitionExample.Criteria criteria = example.createCriteria().andMethodEqualTo(method).andProjectIdEqualTo(projectId).andPathEqualTo(url);
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
        List<ApiDefinitionResult> list = list(request);
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
            example.createCriteria().andProjectIdEqualTo(projectId).andStatusNotEqualTo("Trash");
            return apiDefinitionMapper.countByExample(example);
        }
    }

    public long countQuotedApiByProjectId(String projectId) {
        return extApiDefinitionMapper.countQuotedApiByProjectId(projectId);
    }

    public List<RelationshipEdgeDTO> getRelationshipApi(String id, String relationshipType) {
        List<RelationshipEdge> relationshipEdges = relationshipEdgeService.getRelationshipEdgeByType(id, relationshipType);
        List<String> ids = relationshipEdgeService.getRelationIdsByType(relationshipType, relationshipEdges);

        if (CollectionUtils.isNotEmpty(ids)) {
            ApiDefinitionExample example = new ApiDefinitionExample();
            example.createCriteria().andIdIn(ids);
            List<ApiDefinition> apiDefinitions = apiDefinitionMapper.selectByExample(example);
            Map<String, ApiDefinition> apiMap = apiDefinitions.stream().collect(Collectors.toMap(ApiDefinition::getId, i -> i));
            List<RelationshipEdgeDTO> results = new ArrayList<>();
            for (RelationshipEdge relationshipEdge : relationshipEdges) {
                RelationshipEdgeDTO relationshipEdgeDTO = new RelationshipEdgeDTO();
                BeanUtils.copyBean(relationshipEdgeDTO, relationshipEdge);
                ApiDefinition apiDefinition;
                if (StringUtils.equals(relationshipType, "PRE")) {
                    apiDefinition = apiMap.get(relationshipEdge.getTargetId());
                } else {
                    apiDefinition = apiMap.get(relationshipEdge.getSourceId());
                }
                relationshipEdgeDTO.setTargetName(apiDefinition.getName());
                relationshipEdgeDTO.setCreator(apiDefinition.getCreateUser());
                relationshipEdgeDTO.setTargetNum(apiDefinition.getNum());
                results.add(relationshipEdgeDTO);
            }
            return results;
        }
        return new ArrayList<>();
    }

    public List<ApiDefinitionResult> getRelationshipRelateList(ApiDefinitionRequest request) {
        request = this.initRequest(request, true, true);
        List<String> relationshipIds = relationshipEdgeService.getRelationshipIds(request.getId());
        request.setNotInIds(relationshipIds);
        request.setId(null); // 去掉id的查询条件
        return extApiDefinitionMapper.list(request);
    }
}
