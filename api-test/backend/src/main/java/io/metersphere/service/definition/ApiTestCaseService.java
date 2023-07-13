package io.metersphere.service.definition;

import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.automation.ApiScenarioDTO;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.datacount.response.ExecuteResultCountDTO;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.dto.definition.request.MsThreadGroup;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.Body;
import io.metersphere.api.exec.generator.JSONSchemaParser;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.enums.ApiTestDataStatus;
import io.metersphere.commons.enums.ExecutionExecuteTypeEnum;
import io.metersphere.commons.enums.FileAssociationTypeEnums;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.ParamsDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.DefinitionReference;
import io.metersphere.notice.service.NotificationService;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.request.OrderRequest;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.service.BaseProjectApplicationService;
import io.metersphere.service.BaseProjectService;
import io.metersphere.service.BaseUserService;
import io.metersphere.service.ServiceUtils;
import io.metersphere.service.ext.ExtFileAssociationService;
import io.metersphere.service.plan.TestPlanApiCaseService;
import io.metersphere.xpack.api.service.ApiCaseBatchSyncService;
import io.metersphere.xpack.api.service.ApiTestCaseSyncService;
import io.metersphere.xpack.version.service.ProjectVersionService;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.comparators.FixedOrderComparator;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.json.JSONObject;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static io.metersphere.commons.utils.ShareUtil.getTimeMills;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestCaseService {
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private BaseUserService userService;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private ApiTestCaseFollowMapper apiTestCaseFollowMapper;
    @Resource
    private BaseProjectVersionMapper baseProjectVersionMapper;
    @Resource
    private TcpApiParamService tcpApiParamService;
    @Resource
    private ApiCaseExecutionInfoService apiCaseExecutionInfoService;
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private ExtFileAssociationService extFileAssociationService;
    @Resource
    private ApiScenarioReferenceIdMapper apiScenarioReferenceIdMapper;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ExtApiScenarioReferenceIdMapper extApiScenarioReferenceIdMapper;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private NotificationService notificationService;
    @Resource
    private BaseProjectService baseProjectService;


    private static final String BODY_FILE_DIR = FileUtils.BODY_FILE_DIR;

    private static final String DEFAULT_TIME_DATE = "-3D";

    //查询测试用例详情
    public ApiTestCaseWithBLOBs getInfoJenkins(String id) {
        ApiTestCaseWithBLOBs apiTest = apiTestCaseMapper.selectByPrimaryKey(id);
        return apiTest;
    }

    public List<ApiTestCaseResult> list(ApiTestCaseRequest request) {
        initRequest(request, true, false);
        if (request.getModuleIds() == null && request.getModuleId() != null) {
            List<String> moduleIds = new ArrayList<>();
            moduleIds.add(request.getModuleId());
            request.setModuleIds(moduleIds);
        }
        return extApiTestCaseMapper.list(request);
    }

    public List<ApiTestCase> selectByIds(ApiTestCaseRequest request) {
        if (request != null && CollectionUtils.isNotEmpty(request.getIds())) {
            ApiTestCaseExample example = new ApiTestCaseExample();
            example.createCriteria().andIdIn(request.getIds());
            List<ApiTestCase> list = apiTestCaseMapper.selectByExample(example);
            return list;
        }
        return new ArrayList<>();
    }

    public List<ApiTestCaseDTO> listSimple(ApiTestCaseRequest request) {
        request = this.initRequest(request, true, true);
        List<ApiTestCaseDTO> apiTestCases = extApiTestCaseMapper.listSimple(request);
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        buildUserInfo(apiTestCases, request.isSelectEnvironment());
        return apiTestCases;
    }

    public Long getToBeUpdatedTime(String projectId) {
        if (StringUtils.isBlank(projectId)) {
            return getTimeMills(System.currentTimeMillis(), DEFAULT_TIME_DATE);
        }
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andTypeEqualTo(ProjectApplicationType.OPEN_UPDATE_TIME.name()).andProjectIdEqualTo(projectId);
        List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(projectApplications)) {
            return getTimeMills(System.currentTimeMillis(), DEFAULT_TIME_DATE);
        }
        example = new ProjectApplicationExample();
        example.createCriteria().andTypeEqualTo(ProjectApplicationType.TRIGGER_UPDATE.name()).andProjectIdEqualTo(projectId);
        List<ProjectApplication> projectApplicationRules = projectApplicationMapper.selectByExample(example);
        ProjectApplication projectApplication = projectApplications.get(0);
        String typeValue = projectApplication.getTypeValue();
        if (CollectionUtils.isEmpty(projectApplicationRules) && StringUtils.equals(typeValue, "false")) {
            return getTimeMills(System.currentTimeMillis(), DEFAULT_TIME_DATE);
        } else if (StringUtils.equals(typeValue, "false")) {
            return null;
        }
        example = new ProjectApplicationExample();
        example.createCriteria().andTypeEqualTo(ProjectApplicationType.OPEN_UPDATE_RULE_TIME.name()).andProjectIdEqualTo(projectId);
        List<ProjectApplication> projectApplicationTimes = projectApplicationMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(projectApplications)) {
            return getTimeMills(System.currentTimeMillis(), DEFAULT_TIME_DATE);
        }
        ProjectApplication projectApplicationTime = projectApplicationTimes.get(0);
        String time = projectApplicationTime.getTypeValue();
        if (StringUtils.isNotBlank(time)) {
            time = "-" + time;
            return getTimeMills(System.currentTimeMillis(), time);
        }
        return null;
    }


    public void setCaseEnvironment(List<ApiTestCaseDTO> apiTestCases) {
        for (ApiTestCaseDTO apiCase : apiTestCases) {
            ApiTestEnvironment environment = getApiCaseEnvironment(apiCase.getId());
            if (environment != null) {
                apiCase.setEnvironment(environment.getName());
            }
            if (apiCase.getExecResult() == null && StringUtils.isNotEmpty(apiCase.getStatus()) && !StringUtils.equalsIgnoreCase(apiCase.getStatus(), "trash")) {
                apiCase.setExecResult(apiCase.getStatus());
            }
        }
    }

    public List<String> idSimple(ApiTestCaseRequest request) {
        request = this.initRequest(request, true, true);
        List<ApiTestCaseDTO> apiTestCaseDTOS = extApiTestCaseMapper.listSimple(request);
        return apiTestCaseDTOS.stream().map(ApiTestCaseDTO::getId).collect(Collectors.toList());
    }

    /**
     * 初始化部分参数
     *
     * @param request
     * @param setDefaultOrders
     * @param checkThisWeekData
     * @return
     */
    private ApiTestCaseRequest initRequest(ApiTestCaseRequest request, boolean setDefaultOrders, boolean checkThisWeekData) {
        if (setDefaultOrders) {
            List<OrderRequest> orders = ServiceUtils.getDefaultSortOrder(request.getOrders());
            orders.forEach(i -> {
                if (i.getName().equals("path")) {
                    i.setPrefix("a");
                } else {
                    i.setPrefix("t1");
                }
            });
            if (request.getFilters() != null) {
                Map<String, List<String>> filters = request.getFilters();
                List<String> status = filters.get("status");
                if (status == null) {
                    // sql 需要有这个字段
                    filters.put("status", new ArrayList<>());
                }
            }
            request.setOrders(orders);
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

    public void buildUserInfo(List<? extends ApiTestCaseDTO> apiTestCases, boolean isSelectEnvironment) {
        List<String> userIds = new ArrayList();
        userIds.addAll(apiTestCases.stream().map(ApiTestCaseDTO::getCreateUserId).collect(Collectors.toList()));
        userIds.addAll(apiTestCases.stream().map(ApiTestCaseDTO::getUpdateUserId).collect(Collectors.toList()));
        List<String> ids = apiTestCases.stream().map(ApiTestCaseDTO::getId).collect(Collectors.toList());
        List<ParamsDTO> passRateList = extApiTestCaseMapper.findPassRateByIds(ids);

        Map<String, String> passRates = passRateList.stream().collect(Collectors.toMap(ParamsDTO::getId, ParamsDTO::getValue));
        Map<String, String> envMap = null;
        if (BooleanUtils.isTrue(isSelectEnvironment)) {
            envMap = this.getApiCaseEnvironments(ids);
        }
        if (!CollectionUtils.isEmpty(userIds)) {
            Map<String, User> userMap = userService.queryNameByIds(userIds);
            Map<String, String> finalEnvMap = envMap;
            apiTestCases.forEach(caseResult -> {
                caseResult.setPassRate(passRates.get(caseResult.getId()));
                User createUser = userMap.get(caseResult.getCreateUserId());
                if (createUser != null) {
                    caseResult.setCreateUser(createUser.getName());
                }
                if (finalEnvMap != null && finalEnvMap.containsKey(caseResult.getId())) {
                    caseResult.setEnvironment(finalEnvMap.get(caseResult.getId()));
                }
                User updateUser = userMap.get(caseResult.getUpdateUserId());
                if (updateUser != null) {
                    caseResult.setUpdateUser(updateUser.getName());
                }
            });
        }
    }

    public ApiTestCaseInfo get(String id) {
        return extApiTestCaseMapper.selectApiCaseInfoByPrimaryKey(id);
    }

    public List<ApiTestCaseInfo> selectByCaseIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return extApiTestCaseMapper.selectByCaseIds(ids);
    }

    public ApiTestCaseInfo getResult(String id) {
        return extApiTestCaseMapper.selectApiCaseInfoByPrimaryKey(id);
    }

    public ApiTestCase create(SaveApiTestCaseRequest request, List<MultipartFile> bodyFiles) {
        if (StringUtils.isBlank(request.getVersionId())) {
            request.setVersionId(baseProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        }
        ApiTestCase test = createTest(request, bodyFiles);
        return test;
    }

    public ApiTestCase updateExecuteInfo(SaveApiTestCaseRequest request) {
        ApiTestCaseWithBLOBs test = new ApiTestCaseWithBLOBs();
        test.setId(request.getId());
        test.setUpdateTime(System.currentTimeMillis());
        test.setUpdateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        apiTestCaseMapper.updateByPrimaryKeySelective(test);
        return test;
    }

    public ApiTestCase update(SaveApiTestCaseRequest request, List<MultipartFile> bodyFiles) {
        request.setBodyUploadIds(null);
        ApiTestCase test = updateTest(request);
        if (request.getRequest() != null) {
            // requestID 跟接口id 不一致的情况
            ApiFileUtil.createBodyFiles(request.getRequest().getId(), bodyFiles);
        } else {
            ApiFileUtil.createBodyFiles(request.getId(), bodyFiles);
        }
        // 发送通知
        ApiCaseBatchSyncService apiCaseBatchSyncService = CommonBeanFactory.getBean(ApiCaseBatchSyncService.class);
        if (apiCaseBatchSyncService != null) {
            apiCaseBatchSyncService.sendCaseNotice(test);
        }
        return test;
    }

    public void delete(String testId) {
        testPlanApiCaseService.deleteByCaseId(testId);
        extApiDefinitionExecResultMapper.deleteByResourceId(testId);
        apiTestCaseMapper.deleteByPrimaryKey(testId);
        // 删除附件关系
        extFileAssociationService.deleteByResourceId(testId);
        deleteFollows(testId);
        ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(testId);
        if (apiTestCase != null) {
            ApiFileUtil.deleteBodyFiles(apiTestCase.getId());
        }
    }

    private void deleteFollows(String testId) {
        ApiTestCaseFollowExample example = new ApiTestCaseFollowExample();
        example.createCriteria().andCaseIdEqualTo(testId);
        apiTestCaseFollowMapper.deleteByExample(example);
    }

    private void deleteFollows(List<String> testIds) {
        ApiTestCaseFollowExample example = new ApiTestCaseFollowExample();
        example.createCriteria().andCaseIdIn(testIds);
        apiTestCaseFollowMapper.deleteByExample(example);
    }

    public void deleteTestCase(String apiId) {
        ApiTestCaseExample testCaseExample = new ApiTestCaseExample();
        testCaseExample.createCriteria().andApiDefinitionIdEqualTo(apiId);
        List<ApiTestCase> testCases = apiTestCaseMapper.selectByExample(testCaseExample);
        if (testCases.size() > 0) {
            for (ApiTestCase testCase : testCases) {
                this.delete(testCase.getId());
            }
        }
    }

    public void checkNameExist(SaveApiTestCaseRequest request) {
        if (hasSameCase(request)) {
            MSException.throwException(Translator.get("case_name_is_already_exist") + ": " + request.getName());
        }
    }

    public Boolean hasSameCase(SaveApiTestCaseRequest request) {
        if (getSameCase(request) != null) {
            return true;
        }
        return false;
    }

    public ApiTestCase getSameCase(SaveApiTestCaseRequest request) {
        List<ApiTestCase> apiTestCases = extApiTestCaseMapper.checkName(request);
        if (CollectionUtils.isNotEmpty(apiTestCases)) {
            return apiTestCases.get(0);
        }
        return null;
    }

    public ApiTestCase getSameCaseById(SaveApiTestCaseRequest request) {
        if (StringUtils.isNotBlank(request.getId())) {
            ApiTestCaseExample example = new ApiTestCaseExample();
            ApiTestCaseExample.Criteria criteria = example.createCriteria();
            criteria.andStatusNotEqualTo("Trash").andApiDefinitionIdEqualTo(request.getApiDefinitionId());
            criteria.andIdEqualTo(request.getId());
            List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(apiTestCases)) {
                return apiTestCases.get(0);
            }
        }
        return null;
    }

    private ApiTestCase updateTest(SaveApiTestCaseRequest request) {
        checkNameExist(request);
        request.setRequest(tcpApiParamService.parseMsTestElement(request.getRequest()));
        final ApiTestCaseWithBLOBs test = apiTestCaseMapper.selectByPrimaryKey(request.getId());
        if (test != null) {
            String requestOrg = test.getRequest();
            test.setName(request.getName());
            test.setCaseStatus(request.getCaseStatus());
            if (StringUtils.isEmpty(request.getCaseStatus())) {
                test.setCaseStatus(ApiTestDataStatus.UNDERWAY.getValue());
            }
            test.setApiDefinitionId(request.getApiDefinitionId());
            test.setUpdateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
            test.setProjectId(request.getProjectId());
            test.setRequest(JSON.toJSONString(request.getRequest()));
            test.setPriority(request.getPriority());
            test.setUpdateTime(System.currentTimeMillis());
            test.setDescription(request.getDescription());
            test.setVersion(request.getVersion() == null ? 0 : request.getVersion() + 1);
            test.setVersionId(request.getVersionId());
            if (StringUtils.equals("[]", request.getTags())) {
                test.setTags(StringUtils.EMPTY);
            } else {
                test.setTags(request.getTags());
            }
            ApiTestCaseSyncService apiTestCaseSyncService = CommonBeanFactory.getBean(ApiTestCaseSyncService.class);
            if (apiTestCaseSyncService != null) {
                apiTestCaseSyncService.setCaseUpdateValue(test);
            }
            apiTestCaseMapper.updateByPrimaryKeySelective(test);
            saveFollows(test.getId(), request.getFollows());
            this.checkAndSendReviewMessage(test.getId(),
                    test.getName(),
                    test.getProjectId(),
                    "接口用例通知",
                    NoticeConstants.TaskType.API_DEFINITION_TASK,
                    requestOrg,
                    test.getRequest(),
                    test.getCreateUserId()
            );
        }
        // 存储附件关系
        extFileAssociationService.saveApi(test.getId(), request.getRequest(), FileAssociationTypeEnums.CASE.name());
        return test;
    }

    public void saveFollows(String testId, List<String> follows) {
        ApiTestCaseFollowExample example = new ApiTestCaseFollowExample();
        example.createCriteria().andCaseIdEqualTo(testId);
        apiTestCaseFollowMapper.deleteByExample(example);
        if (!org.springframework.util.CollectionUtils.isEmpty(follows)) {
            for (String follow : follows) {
                ApiTestCaseFollow caseFollow = new ApiTestCaseFollow();
                caseFollow.setCaseId(testId);
                caseFollow.setFollowId(follow);
                apiTestCaseFollowMapper.insert(caseFollow);
            }
        }
    }


    private ApiTestCase createTest(SaveApiTestCaseRequest request, List<MultipartFile> bodyFiles) {
        checkNameExist(request);
        ApiFileUtil.createBodyFiles(request.getId(), bodyFiles);
        if (request.getRequest() != null) {
            request.getRequest().setRefType(MsHashTreeConstants.CASE);
            request.getRequest().setEnable(true);
        }
        request.setRequest(tcpApiParamService.parseMsTestElement(request.getRequest()));
        if (StringUtils.isNotBlank(request.getSourceIdByCopy())) {
            ApiFileUtil.copyBdyFile(request.getSourceIdByCopy(), request.getId());
        } else {
            ApiFileUtil.copyBdyFile(request.getApiDefinitionId(), request.getId());
        }


        final ApiTestCaseWithBLOBs test = new ApiTestCaseWithBLOBs();
        test.setId(request.getId());
        test.setName(request.getName());
        test.setStatus(StringUtils.EMPTY);
        test.setCaseStatus(request.getCaseStatus());
        if (StringUtils.isEmpty(request.getCaseStatus())) {
            test.setCaseStatus(ApiTestDataStatus.UNDERWAY.getValue());
        }
        test.setApiDefinitionId(request.getApiDefinitionId());
        test.setCreateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        test.setUpdateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        test.setProjectId(request.getProjectId());
        test.setRequest(JSON.toJSONString(request.getRequest()));
        test.setCreateTime(System.currentTimeMillis());
        test.setPriority(request.getPriority());
        test.setUpdateTime(System.currentTimeMillis());
        test.setDescription(request.getDescription());
        test.setNum(getNextNum(request.getApiDefinitionId()));
        test.setOrder(ServiceUtils.getNextOrder(request.getProjectId(), extApiTestCaseMapper::getLastOrder));
        test.setVersionId(request.getVersionId());
        if (StringUtils.equals("[]", request.getTags())) {
            test.setTags(StringUtils.EMPTY);
        } else {
            test.setTags(request.getTags());
        }
        ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(test.getId());
        if (apiTestCaseWithBLOBs == null) {
            apiTestCaseMapper.insert(test);
            saveFollows(test.getId(), request.getFollows());
        }
        this.checkAndSendReviewMessage(test.getId(),
                test.getName(),
                test.getProjectId(),
                "接口用例通知",
                NoticeConstants.TaskType.API_DEFINITION_TASK,
                null,
                test.getRequest(),
                test.getCreateUserId()
        );
        // 存储附件关系
        extFileAssociationService.saveApi(test.getId(), request.getRequest(), FileAssociationTypeEnums.CASE.name());
        return test;
    }

    public int getNextNum(String definitionId) {
        ApiTestCase apiTestCase = extApiTestCaseMapper.getNextNum(definitionId);
        if (apiTestCase == null) {
            ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = apiDefinitionMapper.selectByPrimaryKey(definitionId);
            int n = apiDefinitionWithBLOBs.getNum();
            return n * 1000 + 1;
        } else {
            return this.getNextNum(apiTestCase.getNum());
        }
    }

    public int getNextNum(Integer lastApiCaseNum) {
        return Optional.of(lastApiCaseNum + 1).orElse(lastApiCaseNum * 1000 + 1);
    }

    public int getNextNum(String definitionId, Integer definitionNum, String projectId) {
        ApiTestCase apiTestCase = extApiTestCaseMapper.getNextNum(definitionId);
        ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = apiDefinitionMapper.selectByPrimaryKey(definitionId);
        int apiDefinitionNum;
        if (apiDefinitionWithBLOBs != null && apiDefinitionWithBLOBs.getNum() != null) {
            apiDefinitionNum = apiDefinitionWithBLOBs.getNum() * 1000 + 1;
        } else {
            apiDefinitionNum = getNextNumByProjectId(projectId);
        }
        if (apiTestCase == null) {
            if (definitionNum == null) {
                return apiDefinitionNum;

            }
            return definitionNum * 1000 + 1;
        } else {
            return Optional.of(apiTestCase.getNum() + 1).orElse(apiDefinitionNum);
        }
    }

    public int getNextNumByProjectId(String projectId) {
        ApiDefinition apiDefinition = extApiDefinitionMapper.getNextNum(projectId);
        if (apiDefinition == null || apiDefinition.getNum() == null) {
            return 100001;
        } else {
            return Optional.of(apiDefinition.getNum() + 1).orElse(100001);
        }
    }

    public void editApiBath(ApiCaseEditRequest request) {
        ApiTestCaseExample apiDefinitionExample = new ApiTestCaseExample();
        apiDefinitionExample.createCriteria().andIdIn(request.getIds());
        ApiTestCaseWithBLOBs apiDefinitionWithBLOBs = new ApiTestCaseWithBLOBs();
        BeanUtils.copyBean(apiDefinitionWithBLOBs, request);
        apiDefinitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
        apiTestCaseMapper.updateByExampleSelective(apiDefinitionWithBLOBs, apiDefinitionExample);
    }

    public void deleteBatch(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        deleteFollows(ids);
        testPlanApiCaseService.deleteByCaseIds(ids);
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(ids);
        apiTestCaseMapper.deleteByExample(example);
        // 删除附件关系
        extFileAssociationService.deleteByResourceIds(ids);
    }

    public void deleteBatchByDefinitionId(List<String> definitionIds) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andApiDefinitionIdIn(definitionIds);
        apiTestCaseMapper.deleteByExample(example);
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        List<String> caseIds = apiTestCases.stream().map(ApiTestCase::getId).collect(Collectors.toList());
        testPlanApiCaseService.deleteByCaseIds(caseIds);
    }

    public void relevanceByApiByReview(ApiCaseRelevanceRequest request) {
        List<String> ids = request.getSelectIds();
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        // 尽量保持与用例顺序一致
        Collections.reverse(ids);

        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(ids);
    }

    public void relevanceByCase(ApiCaseRelevanceRequest request) {
        List<String> ids = request.getSelectIds();
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        Collections.reverse(ids);
        testPlanApiCaseService.relevance(ids, request);
    }

    public List<String> selectIdsNotExistsInPlan(String projectId, String planId) {
        return extApiTestCaseMapper.selectIdsNotExistsInPlan(projectId, planId);
    }

    public List<String> selectIdsNotExistsInReview(String projectId, String reviewId) {
        return extApiTestCaseMapper.selectIdsNotExistsInReview(projectId, reviewId);
    }

    public List<ApiDataCountResult> countProtocolByProjectID(String projectId, String versionId) {
        return extApiTestCaseMapper.countProtocolByProjectID(projectId, versionId);
    }

    public long countByProjectIDAndCreateInThisWeek(String projectId, String versionId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiTestCaseMapper.countByProjectIDAndCreateInThisWeek(projectId, versionId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<ApiTestCase> selectCasesBydIds(List<String> caseIds) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(caseIds);
        return apiTestCaseMapper.selectByExample(example);
    }

    public List<ApiTestCaseWithBLOBs> selectCasesBydApiIds(List<String> apiIds) {
        return extApiTestCaseMapper.unTrashCaseListByApiIds(apiIds);
    }

    public Map<String, String> getRequest(ApiTestCaseRequest request) {
        List<ApiTestCaseInfo> list = extApiTestCaseMapper.getRequest(request);
        return list.stream().collect(Collectors.toMap(ApiTestCaseWithBLOBs::getId, ApiTestCaseWithBLOBs::getRequest));
    }

    public void deleteBatchByParam(ApiTestBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.isSelectAll()) {
            ids = this.getAllApiCaseIdsByFrontedSelect(request.getFilters(), request.getModuleIds(), request.getName(), request.getProjectId(), request.getProtocol(), request.getUnSelectIds(), request.getStatus(), null, request.getCombine());
        }
        this.deleteBatch(ids);
    }

    public void editApiBathByParam(ApiTestBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.isSelectAll()) {
            ids = this.getAllApiCaseIdsByFrontedSelect(request.getFilters(), request.getModuleIds(), request.getName(), request.getProjectId(), request.getProtocol(), request.getUnSelectIds(), request.getStatus(), request.getApiDefinitionId(), request.getCombine());
        }

        if (StringUtils.equals("tags", request.getType())) {
            this.batchEditTags(request, ids);
            return;
        }
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ApiTestCaseExample apiDefinitionExample = new ApiTestCaseExample();
        apiDefinitionExample.createCriteria().andIdIn(ids);
        if (StringUtils.isNotEmpty(request.getPriority())) {
            ApiTestCaseWithBLOBs apiDefinitionWithBLOBs = new ApiTestCaseWithBLOBs();
            apiDefinitionWithBLOBs.setPriority(request.getPriority());
            apiDefinitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
            apiTestCaseMapper.updateByExampleSelective(apiDefinitionWithBLOBs, apiDefinitionExample);
        }
        if (StringUtils.isNotEmpty(request.getCaseStatus())) {
            ApiTestCaseWithBLOBs apiDefinitionWithBLOBs = new ApiTestCaseWithBLOBs();
            apiDefinitionWithBLOBs.setCaseStatus(request.getCaseStatus());
            apiDefinitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
            apiTestCaseMapper.updateByExampleSelective(apiDefinitionWithBLOBs, apiDefinitionExample);
        }
        if (StringUtils.isNotEmpty(request.getEnvId())) {
            List<ApiTestCaseWithBLOBs> bloBs = apiTestCaseMapper.selectByExampleWithBLOBs(apiDefinitionExample);
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiTestCaseMapper batchMapper = sqlSession.getMapper(ApiTestCaseMapper.class);

            bloBs.forEach(apiTestCase -> {
                JSONObject req = JSONUtil.parseObject(apiTestCase.getRequest());
                req.put(PropertyConstant.ENVIRONMENT, request.getEnvId());
                String requestStr = JSON.toJSONString(req);
                apiTestCase.setRequest(requestStr);
                batchMapper.updateByPrimaryKeySelective(apiTestCase);
            });
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
        if (StringUtils.isNotEmpty(request.getFollow())) {
            if (StringUtils.equals(request.getFollow(), "cancel")) {
                ApiTestCaseFollowExample apiTestCaseFollowExample = new ApiTestCaseFollowExample();
                apiTestCaseFollowExample.createCriteria().andCaseIdIn(ids);
                apiTestCaseFollowMapper.deleteByExample(apiTestCaseFollowExample);
            }
        }
    }

    private void batchEditTags(ApiTestBatchRequest request, List<String> ids) {
        if (request.getTagList().isEmpty() || CollectionUtils.isEmpty(ids)) {
            return;
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiTestCaseMapper mapper = sqlSession.getMapper(ApiTestCaseMapper.class);
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(ids);
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        for (ApiTestCase apiTestCase : apiTestCases) {
            String tags = apiTestCase.getTags();
            if (StringUtils.isBlank(tags) || BooleanUtils.isFalse(request.isAppendTag())) {
                apiTestCase.setTags(JSON.toJSONString(request.getTagList()));
                apiTestCase.setUpdateTime(System.currentTimeMillis());
            } else {
                try {
                    List<String> list = JSON.parseArray(tags, String.class);
                    list.addAll(request.getTagList());
                    apiTestCase.setTags(JSON.toJSONString(list));
                    apiTestCase.setUpdateTime(System.currentTimeMillis());
                } catch (Exception e) {
                    LogUtil.error("batch edit tags error.");
                    LogUtil.error(e, e.getMessage());
                    apiTestCase.setTags(JSON.toJSONString(request.getTagList()));
                }
            }
            mapper.updateByPrimaryKey(apiTestCase);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public void updateByApiDefinitionId(List<String> ids, ApiDefinitionWithBLOBs test, String apiUpdateRule) {
        String method = test.getMethod();
        String path = test.getPath();
        String protocol = test.getProtocol();
        if ((StringUtils.isNotEmpty(method) || StringUtils.isNotEmpty(path) && RequestTypeConstants.HTTP.equals(protocol))) {
            ApiTestCaseExample apiDefinitionExample = new ApiTestCaseExample();
            apiDefinitionExample.createCriteria().andApiDefinitionIdIn(ids);
            List<ApiTestCaseWithBLOBs> caseWithBLOBs = apiTestCaseMapper.selectByExampleWithBLOBs(apiDefinitionExample);
            List<String> caseIds = caseWithBLOBs.stream().map(ApiTestCaseWithBLOBs::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(caseIds)) {
                return;
            }
            List<ApiTestCaseWithBLOBs> bloBs = extApiTestCaseMapper.unTrashCaseListByIds(caseIds);

            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiTestCaseMapper batchMapper = sqlSession.getMapper(ApiTestCaseMapper.class);

            bloBs.forEach(apiTestCase -> {
                JSONObject element = JSONUtil.parseObject(apiTestCase.getRequest());
                ElementUtil.dataFormatting(element);
                MsHTTPSamplerProxy req = JSON.parseObject(element.toString(), MsHTTPSamplerProxy.class);
                if (StringUtils.isNotEmpty(element.optString(ElementConstants.HASH_TREE))) {
                    req.setHashTree(JSONUtil.readValue(element.optString(ElementConstants.HASH_TREE)));
                }
                if (StringUtils.isNotBlank(method)) {
                    req.setMethod(method);
                }
                if (StringUtils.isNotBlank(path)) {
                    req.setPath(path);
                }
                apiTestCase.setRequest(JSON.toJSONString(req));
                // sync case
                ApiCaseBatchSyncService apiCaseBatchSyncService = CommonBeanFactory.getBean(ApiCaseBatchSyncService.class);
                if (apiCaseBatchSyncService != null) {
                    apiCaseBatchSyncService.oneClickSyncCase(apiUpdateRule, test, apiTestCase);
                    // 存储附件关系
                    JSONObject requestCase = JSONUtil.parseObject(apiTestCase.getRequest());
                    ElementUtil.dataFormatting(requestCase);
                    MsHTTPSamplerProxy caseProxy = JSON.parseObject(requestCase.toString(), MsHTTPSamplerProxy.class);
                    extFileAssociationService.saveApi(apiTestCase.getId(), caseProxy, FileAssociationTypeEnums.CASE.name());
                }
                batchMapper.updateByPrimaryKeySelective(apiTestCase);

            });
            sqlSession.flushStatements();
            if (sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    private List<String> getAllApiCaseIdsByFrontedSelect(Map<String, List<String>> filters, List<String> moduleIds, String name, String projectId, String protocol, List<String> unSelectIds, String status, String apiId, Map<String, Object> combine) {
        ApiTestCaseRequest selectRequest = new ApiTestCaseRequest();
        selectRequest.setFilters(filters);
        selectRequest.setModuleIds(moduleIds);
        selectRequest.setName(name);
        selectRequest.setProjectId(projectId);
        selectRequest.setProtocol(protocol);
        selectRequest.setStatus(status);
        selectRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        selectRequest.setApiDefinitionId(apiId);
        if (combine != null) {
            selectRequest.setCombine(combine);
        }
        List<ApiTestCaseDTO> list = extApiTestCaseMapper.listSimple(selectRequest);
        List<String> allIds = list.stream().map(ApiTestCaseDTO::getId).collect(Collectors.toList());
        List<String> ids = allIds.stream().filter(id -> !unSelectIds.contains(id)).collect(Collectors.toList());
        return ids;
    }

    public String getExecResult(String id) {
        String status = extApiDefinitionExecResultMapper.selectExecResult(id);
        return status;
    }

    public ApiDefinitionExecResult getInfo(String id) {
        return apiDefinitionExecResultMapper.selectByPrimaryKey(id);
    }

    public List<ApiTestCase> selectEffectiveTestCaseByProjectId(String projectId) {
        return extApiTestCaseMapper.selectEffectiveTestCaseByProjectId(projectId);
    }

    public List<ApiTestCaseInfo> findApiTestCaseBLOBs(ApiTestCaseRequest request) {
        List<String> ids = request.getIds();
        if (request.isSelectAll()) {
            request.setIds(null);
            ids = this.idSimple(request);
            ids.removeAll(request.getUnSelectIds());
            request.setIds(ids);
        }
        List<ApiTestCaseInfo> list = null;
        if (StringUtils.isEmpty(request.getId()) && CollectionUtils.isEmpty(request.getIds())) {
            list = new ArrayList<>();
        } else {
            list = extApiTestCaseMapper.getCaseInfo(request);
            list.forEach(item -> {
                        MsTestElement msTestElement = JSONUtil.parseObject(item.getRequest(), MsTestElement.class);
                        if (msTestElement instanceof MsHTTPSamplerProxy) {
                            MsHTTPSamplerProxy requestBody = (MsHTTPSamplerProxy) JSONUtil.parseObject(item.getRequest(), MsTestElement.class);
                            Body body = requestBody.getBody();
                            if (StringUtils.isNotBlank(body.getType()) && StringUtils.equals(body.getType(), Body.JSON_STR)) {
                                if (StringUtils.isNotEmpty(body.getFormat()) && body.getJsonSchema() != null && Body.JSON_SCHEMA.equals(body.getFormat())) {
                                    body.setRaw(JSONSchemaParser.preview(JSONUtil.toJSONString(body.getJsonSchema())));
                                }
                            }
                            item.setRequest(JSONUtil.toJSONString(requestBody));
                        }
                    }
            );
        }
        // 排序
        FixedOrderComparator<String> fixedOrderComparator = new FixedOrderComparator<String>(request.getIds());
        fixedOrderComparator.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.BEFORE);
        BeanComparator beanComparator = new BeanComparator("id", fixedOrderComparator);
        Collections.sort(list, beanComparator);

        return list;
    }

    public String getLogDetails(String id) {
        ApiTestCaseWithBLOBs bloBs = apiTestCaseMapper.selectByPrimaryKey(id);
        if (bloBs != null) {
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), bloBs.getProjectId(), bloBs.getName(), bloBs.getCreateUserId(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        ApiTestCaseExample.Criteria criteria = example.createCriteria();
        criteria.andIdIn(ids);
        List<ApiTestCase> nodes = apiTestCaseMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(nodes)) {
            List<String> names = nodes.stream().map(ApiTestCase::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), nodes.get(0).getProjectId(), String.join(",", names), nodes.get(0).getCreateUserId(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(SaveApiTestCaseRequest request) {
        ApiTestCaseWithBLOBs bloBs = null;
        if (StringUtils.isNotEmpty(request.getId())) {
            bloBs = apiTestCaseMapper.selectByPrimaryKey(request.getId());
        }
        if (bloBs == null && StringUtils.isNotEmpty(request.getName())) {
            ApiTestCaseExample example = new ApiTestCaseExample();
            ApiTestCaseExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId()).andApiDefinitionIdEqualTo(request.getApiDefinitionId());
            List<ApiTestCaseWithBLOBs> list = apiTestCaseMapper.selectByExampleWithBLOBs(example);
            if (CollectionUtils.isNotEmpty(list)) {
                bloBs = list.get(0);
            }
        }
        if (bloBs != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(bloBs, DefinitionReference.caseColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(bloBs.getId()), bloBs.getProjectId(), bloBs.getCreateUserId(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public void deleteToGc(String id) {
        ApiTestCaseRequest request = new ApiTestCaseRequest();
        List<String> ids = new ArrayList<>();
        ids.add(id);
        request.setIds(ids);
        request.setDeleteUserId(SessionUtils.getUserId());
        request.setDeleteTime(System.currentTimeMillis());
        extApiTestCaseMapper.deleteToGc(request);
    }

    public void deleteToGc(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            ApiTestCaseRequest request = new ApiTestCaseRequest();
            request.setIds(ids);
            request.setDeleteUserId(SessionUtils.getUserId());
            request.setDeleteTime(System.currentTimeMillis());
            extApiTestCaseMapper.deleteToGc(request);
        }
    }

    public void deleteToGcByParam(ApiTestBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.isSelectAll()) {
            ids = this.getAllApiCaseIdsByFrontedSelect(request.getFilters(), request.getModuleIds(), request.getName(), request.getProjectId(), request.getProtocol(), request.getUnSelectIds(), request.getStatus(), request.getApiDefinitionId(), request.getCombine());
        }
        this.deleteToGc(ids);
    }

    public List<String> reduction(ApiTestBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.isSelectAll()) {
            ids = this.getAllApiCaseIdsByFrontedSelect(request.getFilters(), request.getModuleIds(), request.getName(), request.getProjectId(), request.getProtocol(), request.getUnSelectIds(), request.getStatus(), null, request.getCombine());
        }
        ApiTestCaseExample apiTestCaseExample = new ApiTestCaseExample();
        apiTestCaseExample.createCriteria().andIdIn(ids);
        List<ApiTestCase> apiCaseList = apiTestCaseMapper.selectByExample(apiTestCaseExample);
        SaveApiTestCaseRequest saveApiTestCaseRequest = new SaveApiTestCaseRequest();
        if (CollectionUtils.isNotEmpty(apiCaseList)) {
            for (ApiTestCase apiTestCaseDTO : apiCaseList) {
                saveApiTestCaseRequest.setName(apiTestCaseDTO.getName());
                saveApiTestCaseRequest.setApiDefinitionId(apiTestCaseDTO.getApiDefinitionId());
                saveApiTestCaseRequest.setId(apiTestCaseDTO.getId());
                saveApiTestCaseRequest.setVersionId(apiTestCaseDTO.getVersionId());
                checkNameExist(saveApiTestCaseRequest);
            }
        }
        List<String> cannotReductionAPiName = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ids)) {
            List<ApiTestCaseDTO> cannotReductionApiCaseList = extApiTestCaseMapper.getCannotReductionApiCaseList(ids);
            List<String> cannotReductionCaseId = new ArrayList<>();
            for (ApiTestCaseDTO apiTestCaseDTO : cannotReductionApiCaseList) {
                if (!cannotReductionAPiName.contains(apiTestCaseDTO.getApiName())) {
                    cannotReductionAPiName.add(apiTestCaseDTO.getApiName());
                }
                cannotReductionCaseId.add(apiTestCaseDTO.getId());
            }
            cannotReductionApiCaseList.stream().map(ApiTestCaseDTO::getId).collect(Collectors.toList());
            List<String> deleteIds = ids.stream().filter(id -> !cannotReductionCaseId.contains(id)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(deleteIds)) {
                extApiTestCaseMapper.reduction(deleteIds);
            }
        }
        return cannotReductionAPiName;
    }

    public List<String> selectCaseIdsByApiIds(List<String> apiIds) {
        if (CollectionUtils.isEmpty(apiIds)) {
            return new ArrayList<>(0);
        } else {
            return extApiTestCaseMapper.selectCaseIdsByApiIds(apiIds);
        }
    }

    public DeleteCheckResult checkDeleteData(ApiTestBatchRequest request) {
        DeleteCheckResult result = new DeleteCheckResult();
        List<String> deleteIds = request.getIds();
        if (request.isSelectAll()) {
            deleteIds = this.getAllApiCaseIdsByFrontedSelect(request.getFilters(), request.getModuleIds(), request.getName(), request.getProjectId(), request.getProtocol(), request.getUnSelectIds(), request.getStatus(), request.getApiDefinitionId(), request.getCombine());
        }
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            //场景引用
            List<ApiScenarioReferenceId> apiScenarioReferenceIdList = extApiScenarioReferenceIdMapper.selectReferenceIdByIds(deleteIds);
            Map<String, List<String>> scenarioDic = new HashMap<>();
            if (CollectionUtils.isNotEmpty(apiScenarioReferenceIdList)) {
                apiScenarioReferenceIdList.forEach(item -> {
                    String referenceId = item.getReferenceId();
                    String scenarioId = item.getApiScenarioId();
                    if (scenarioDic.containsKey(referenceId)) {
                        scenarioDic.get(referenceId).add(scenarioId);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(scenarioId);
                        scenarioDic.put(referenceId, list);
                    }
                });
            }
            //测试计划引用
            List<TestPlanApiCase> testPlanApiCases = extTestPlanApiCaseMapper.selectByRefIds(deleteIds);
            Map<String, List<String>> planList = new HashMap<>();
            if (CollectionUtils.isNotEmpty(testPlanApiCases)) {
                testPlanApiCases.forEach(item -> {
                    String referenceId = item.getApiCaseId();
                    String testPlanId = item.getTestPlanId();
                    if (planList.containsKey(referenceId)) {
                        planList.get(referenceId).add(testPlanId);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(testPlanId);
                        planList.put(referenceId, list);
                    }
                });
            }
            if (MapUtils.isNotEmpty(scenarioDic) || MapUtils.isNotEmpty(planList)) {
                if (StringUtils.equals("batch", request.getType())) {
                    Map<String, List<String>> map = scenarioDic.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2, () -> new HashMap<>(planList)));
                    result.setRefCount(map.size());
                    result.setCheckMsg(new ArrayList<>(map.keySet()));
                } else {
                    ArrayList<List<String>> scenarioList = new ArrayList<>(scenarioDic.values());
                    if (CollectionUtils.isNotEmpty(scenarioList)) {
                        result.setScenarioCount(new TreeSet<String>(scenarioList.get(0)).size());
                    }
                    ArrayList<List<String>> testPlanList = new ArrayList<>(planList.values());
                    if (CollectionUtils.isNotEmpty(testPlanList)) {
                        result.setPlanCount(new TreeSet<String>(testPlanList.get(0)).size());
                    }
                }
                result.setDeleteFlag(scenarioDic.size() > 0 || planList.size() > 0);
            }
        }
        return result;
    }

    public List<ApiTestCase> getApiCaseByIds(List<String> apiCaseIds) {
        if (CollectionUtils.isNotEmpty(apiCaseIds)) {
            ApiTestCaseExample example = new ApiTestCaseExample();
            example.createCriteria().andIdIn(apiCaseIds);
            example.or().andStatusNotEqualTo(CommonConstants.TRASH_STATUS).andStatusIsNull();
            return apiTestCaseMapper.selectByExample(example);
        }
        return new ArrayList<>();
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(ApiTestCaseWithBLOBs.class, ApiTestCaseMapper.class, extApiTestCaseMapper::selectProjectIds, extApiTestCaseMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, ApiTestCaseWithBLOBs.class, apiTestCaseMapper::selectByPrimaryKey, extApiTestCaseMapper::getPreOrder, extApiTestCaseMapper::getLastOrder, apiTestCaseMapper::updateByPrimaryKeySelective);
    }

    public ApiTestEnvironment getApiCaseEnvironment(String caseId) {
        String environmentId = extApiTestCaseMapper.getApiCaseEnvironment(caseId);
        if (StringUtils.isBlank(environmentId)) {
            return null;
        }
        try {
            return apiTestEnvironmentMapper.selectByPrimaryKey(environmentId);
        } catch (Exception e) {
            LogUtil.error("api case environmentId incorrect parsing. api case id: " + caseId);
        }
        return null;
    }

    public Map<String, String> getApiCaseEnvironments(List<String> caseIds) {
        if (CollectionUtils.isEmpty(caseIds)) {
            return null;
        }
        List<ParamsDTO> environments = extApiTestCaseMapper.getApiCaseEnvironments(caseIds);
        if (CollectionUtils.isEmpty(environments)) {
            return null;
        }
        try {
            List<String> envIds = environments.stream().filter(t -> StringUtils.isNotBlank(t.getValue()) && !StringUtils.equalsIgnoreCase(t.getValue(), "null")).map(ParamsDTO::getValue).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(envIds)) {
                ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
                example.createCriteria().andIdIn(envIds);
                List<ApiTestEnvironment> environmentList = apiTestEnvironmentMapper.selectByExample(example);

                if (CollectionUtils.isEmpty(environmentList)) {
                    return null;
                }
                Map<String, String> envMap = environmentList.stream().collect(Collectors.toMap(ApiTestEnvironment::getId, ApiTestEnvironment::getName));

                Map<String, String> caseEnvMap = environments.stream().filter(t -> StringUtils.isNotBlank(t.getValue()) && !StringUtils.equalsIgnoreCase(t.getValue(), "null")).collect(HashMap::new, (m, v) -> m.put(v.getId(), v.getValue()), HashMap::putAll);
                caseEnvMap.forEach((k, v) -> {
                    if (envMap.containsKey(v)) {
                        caseEnvMap.put(k, envMap.get(v));
                    }
                });
                return caseEnvMap;
            }
        } catch (Exception e) {
            LogUtil.error("api case environmentId incorrect parsing", e);
        }
        return null;
    }

    public List<String> getFollows(String testId) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(testId)) {
            return result;
        }
        ApiTestCaseFollowExample example = new ApiTestCaseFollowExample();
        example.createCriteria().andCaseIdEqualTo(testId);
        List<ApiTestCaseFollow> follows = apiTestCaseFollowMapper.selectByExample(example);
        return follows.stream().map(ApiTestCaseFollow::getFollowId).distinct().collect(Collectors.toList());
    }

    public ApiTestCaseWithBLOBs getSameCaseWithBLOBs(SaveApiTestCaseRequest request) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        ApiTestCaseExample.Criteria criteria = example.createCriteria();
        criteria.andStatusNotEqualTo("Trash").andNameEqualTo(request.getName()).andApiDefinitionIdEqualTo(request.getApiDefinitionId());
        if (StringUtils.isNotBlank(request.getId())) {
            criteria.andIdNotEqualTo(request.getId());
        }
        if (StringUtils.isNotBlank(request.getVersionId())) {
            criteria.andVersionIdEqualTo(request.getVersionId());
        }
        List<ApiTestCaseWithBLOBs> apiTestCaseWithBLOBs = apiTestCaseMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(apiTestCaseWithBLOBs)) {
            return apiTestCaseWithBLOBs.get(0);
        }
        return null;
    }

    public List<ExecuteResultCountDTO> selectExecuteResultByProjectId(long allApiCaseNum, String projectId, String versionId) {
        //之前的查询方式采用的Left join,通过关联字段为null来判断是不是未执行过的用例。优化为查询执行过的用例最新状态，最后使用总数进行相减计算
        List<ExecuteResultCountDTO> executeResultCountDTOList = extApiTestCaseMapper.selectExecuteResultByProjectId(projectId, versionId);
        AtomicLong executedCaseCountLong = new AtomicLong();
        executeResultCountDTOList.forEach(item -> {
            executedCaseCountLong.addAndGet(item.getCount());
        });
        long executedCaseCount = executedCaseCountLong.get();
        if (allApiCaseNum > executedCaseCount) {
            //补足未执行的用例状态
            ExecuteResultCountDTO notExecuteCountDTO = new ExecuteResultCountDTO();
            notExecuteCountDTO.setExecResult(StringUtils.EMPTY);
            notExecuteCountDTO.setCount(allApiCaseNum - executedCaseCount);
            executeResultCountDTOList.add(notExecuteCountDTO);
        }
        return executeResultCountDTOList;
    }

    /**
     * 工作台查询应用管理里设置的用例待更新条件
     *
     * @param request
     */
    public void initRequestBySearch(ApiTestCaseRequest request) {
        if (!request.isToBeUpdated()) {
            return;
        }
        Long toBeUpdatedTime = this.getToBeUpdatedTime(request.getProjectId());
        if (toBeUpdatedTime != null) {
            request.setToBeUpdateTime(toBeUpdatedTime);
            request.setUpdateTime(toBeUpdatedTime);
        }
        if (StringUtils.isBlank(request.getProjectId())) {
            List<String> syncRuleCaseStatus = new ArrayList<>();
            syncRuleCaseStatus.add(ApiReportStatus.ERROR.name());
            request.setStatusList(syncRuleCaseStatus);
            return;
        }
        if (request.isNoSearchStatus()) {
            request.setStatusList(new ArrayList<>());
        } else {
            ApiTestCaseSyncService apiTestCaseSyncService = CommonBeanFactory.getBean(ApiTestCaseSyncService.class);
            if (apiTestCaseSyncService != null) {
                List<String> syncRuleCaseStatus = apiTestCaseSyncService.getSyncRuleCaseStatus(request.getProjectId());
                if (CollectionUtils.isEmpty(syncRuleCaseStatus)) {
                    syncRuleCaseStatus = new ArrayList<>();
                }
                request.setStatusList(syncRuleCaseStatus);
            }
        }
    }

    public Integer getCitedScenarioCount(String testId) {
        ApiScenarioReferenceIdExample apiScenarioReferenceIdExample = new ApiScenarioReferenceIdExample();
        apiScenarioReferenceIdExample.createCriteria().andDataTypeEqualTo(ReportTriggerMode.CASE.name()).andReferenceTypeEqualTo(MsTestElementConstants.REF.name()).andReferenceIdEqualTo(testId);
        List<ApiScenarioReferenceId> apiScenarioReferenceIds = apiScenarioReferenceIdMapper.selectByExample(apiScenarioReferenceIdExample);
        return apiScenarioReferenceIds.size();
    }

    public List<ApiCountChartResult> countByRequest(ApiCountRequest request) {
        return extApiTestCaseMapper.countByRequest(request);
    }

    public List<ApiTestCaseDTO> getRelevanceApiList(ApiTestCaseRequest request) {
        List<ApiTestCaseDTO> apiTestCaseDTOS = extApiTestCaseMapper.relevanceApiList(request);
        List<String> versionIds = apiTestCaseDTOS.stream().map(ApiTestCaseDTO::getVersionId).collect(Collectors.toList());
        ProjectVersionService projectVersionService = CommonBeanFactory.getBean(ProjectVersionService.class);
        if (projectVersionService != null) {
            Map<String, String> projectVersionMap = projectVersionService.getProjectVersionByIds(versionIds).stream()
                    .collect(Collectors.toMap(ProjectVersion::getId, ProjectVersion::getName));
            apiTestCaseDTOS.forEach(apiTestCaseDTO -> {
                apiTestCaseDTO.setVersionName(projectVersionMap.get(apiTestCaseDTO.getVersionId()));
            });
        }
        return apiTestCaseDTOS;
    }

    public List<ApiScenarioDTO> getRelevanceScenarioList(ApiScenarioRequest request) {
        List<ApiScenarioDTO> apiScenarioDTOS = extApiScenarioMapper.relevanceScenarioList(request);
        List<String> versionIds = apiScenarioDTOS.stream().map(ApiScenarioDTO::getVersionId).collect(Collectors.toList());
        ProjectVersionService projectVersionService = CommonBeanFactory.getBean(ProjectVersionService.class);
        if (projectVersionService != null) {
            Map<String, String> projectVersionMap = projectVersionService.getProjectVersionByIds(versionIds).stream()
                    .collect(Collectors.toMap(ProjectVersion::getId, ProjectVersion::getName));
            apiScenarioDTOS.forEach(apiTestCaseDTO -> {
                apiTestCaseDTO.setVersionName(projectVersionMap.get(apiTestCaseDTO.getVersionId()));
            });
        }
        return apiScenarioDTOS;
    }


    public List<JmxInfoDTO> exportJmx(List<String> caseIds, String envId) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(caseIds);
        List<ApiTestCaseWithBLOBs> apiTestCases = apiTestCaseMapper.selectByExampleWithBLOBs(example);
        List<JmxInfoDTO> list = new ArrayList<>();
        apiTestCases.forEach(item -> {
            list.add(parse2Jmx(item, envId));
        });
        return list;
    }

    private JmxInfoDTO parse2Jmx(ApiTestCaseWithBLOBs apiTestCase, String envId) {
        String request = apiTestCase.getRequest();
        MsHTTPSamplerProxy msHTTPSamplerProxy = JSON.parseObject(request, MsHTTPSamplerProxy.class);
        msHTTPSamplerProxy.setName(apiTestCase.getId());
        msHTTPSamplerProxy.setUseEnvironment(envId);

        LinkedList<MsTestElement> hashTree = new LinkedList<>();
        hashTree.add(msHTTPSamplerProxy);
        MsThreadGroup msThreadGroup = new MsThreadGroup();
        msThreadGroup.setHashTree(hashTree);
        msThreadGroup.setName("ThreadGroup");
        msThreadGroup.setLabel("ThreadGroup");
        msThreadGroup.setId(UUID.randomUUID().toString());

        LinkedList<MsTestElement> planHashTree = new LinkedList<>();
        planHashTree.add(msThreadGroup);
        MsTestPlan msTestPlan = new MsTestPlan();
        msTestPlan.setHashTree(planHashTree);
        msTestPlan.setId(UUID.randomUUID().toString());
        msTestPlan.setName("TestPlan");
        msTestPlan.setLabel("TestPlan");

        HashMap<String, String> envMap = new HashMap<>();
        envMap.put(apiTestCase.getProjectId(), envId);

        RunDefinitionRequest runRequest = new RunDefinitionRequest();
        runRequest.setEnvironmentMap(envMap);
        runRequest.setEnvironmentId(envId);
        runRequest.setId(apiTestCase.getId());
        runRequest.setTestElement(msTestPlan);
        runRequest.setProjectId(apiTestCase.getProjectId());

        JmxInfoDTO jmxInfoDTO = DataFormattingUtil.getJmxInfoDTO(runRequest, new ArrayList<>());
        jmxInfoDTO.setId(apiTestCase.getId());
        jmxInfoDTO.setVersion(apiTestCase.getVersion());
        jmxInfoDTO.setName(apiTestCase.getName());
        return jmxInfoDTO;
    }

    public int getCaseCountById(String id) {
        return extApiTestCaseMapper.getCaseCountById(id);
    }

    public long countExecutedTimesByProjectId(String projectId, String executeType, String version) {
        return apiCaseExecutionInfoService.countExecutedTimesByProjectIdAndVersion(projectId, executeType, version);
    }

    @Async
    public void setProjectIdInExecutionInfo() {
        long lastCount = 0;
        long allSourceIdCount = apiCaseExecutionInfoService.countSourceIdByProjectIdIsNull();
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
            List<String> sourceIdAboutProjectIdIsNull = apiCaseExecutionInfoService.selectSourceIdByProjectIdIsNull();
            PageHelper.clearPage();
            //批量更新
            for (String sourceId : sourceIdAboutProjectIdIsNull) {
                ApiCaseBasicInfoDTO apiCaseBasicInfoDTO = this.selectApiCaseBasicInfoById(sourceId);
                if (apiCaseBasicInfoDTO != null) {
                    apiCaseExecutionInfoService.updateProjectIdBySourceIdAndProjectIdIsNull(apiCaseBasicInfoDTO.getProjectId(), ExecutionExecuteTypeEnum.BASIC.name(), apiCaseBasicInfoDTO.getVersionId(), sourceId);
                } else {
                    TestPlanApiCase testPlanApiCase = testPlanApiCaseService.getById(sourceId);
                    if (testPlanApiCase != null) {
                        String projectId = testPlanApiCaseService.selectProjectId(sourceId);
                        apiCaseBasicInfoDTO = this.selectApiCaseBasicInfoById(testPlanApiCase.getApiCaseId());
                        if (StringUtils.isNotEmpty(projectId) && apiCaseBasicInfoDTO != null) {
                            apiCaseExecutionInfoService.updateProjectIdBySourceIdAndProjectIdIsNull(projectId, ExecutionExecuteTypeEnum.TEST_PLAN.name(), apiCaseBasicInfoDTO.getVersionId(), sourceId);
                        } else {
                            apiCaseExecutionInfoService.deleteBySourceIdAndProjectIsNull(sourceId);
                        }
                    } else {
                        apiCaseExecutionInfoService.deleteBySourceIdAndProjectIsNull(sourceId);
                    }
                }
            }
            allSourceIdCount = apiCaseExecutionInfoService.countSourceIdByProjectIdIsNull();
        }
    }

    private ApiCaseBasicInfoDTO selectApiCaseBasicInfoById(String id) {
        return extApiTestCaseMapper.selectApiCaseBasicInfoById(id);
    }

    public void updateByPrimaryKeySelective(ApiTestCaseWithBLOBs apiTestCase) {
        apiTestCaseMapper.updateByPrimaryKeySelective(apiTestCase);
    }

    public String getPassRate(String id) {
        return extApiTestCaseMapper.findPassRateById(id);

    }

    //检查并发送脚本审核的通知
    @Async
    public void checkAndSendReviewMessage(
            String id,
            String name,
            String projectId,
            String title,
            String resourceType,
            String requestOrg,
            String requestTarget,
            String sendUser) {

        try {
            ProjectApplication scriptEnable = baseProjectApplicationService
                    .getProjectApplication(projectId, ProjectApplicationType.API_REVIEW_TEST_SCRIPT.name());

            if (BooleanUtils.toBoolean(scriptEnable.getTypeValue())) {
                List<String> org = ElementUtil.scriptList(requestOrg);
                List<String> target = ElementUtil.scriptList(requestTarget);
                boolean isSend = ElementUtil.isSend(org, target);
                if (isSend) {
                    ProjectApplication reviewer = baseProjectApplicationService
                            .getProjectApplication(projectId, ProjectApplicationType.API_SCRIPT_REVIEWER.name());
                    if (StringUtils.isNotEmpty(reviewer.getTypeValue())) {
                        sendUser = reviewer.getTypeValue();
                    }
                    if (baseProjectService.isProjectMember(projectId, sendUser)) {
                        Notification notification = new Notification();
                        notification.setTitle(title);
                        notification.setOperator(reviewer.getTypeValue());
                        notification.setOperation(NoticeConstants.Event.REVIEW);
                        notification.setResourceId(id);
                        notification.setResourceName(name);
                        notification.setResourceType(resourceType);
                        notification.setType(NotificationConstants.Type.SYSTEM_NOTICE.name());
                        notification.setStatus(NotificationConstants.Status.UNREAD.name());
                        notification.setCreateTime(System.currentTimeMillis());
                        notification.setReceiver(sendUser);
                        notificationService.sendAnnouncement(notification);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error("发送通知失败", e);
        }
    }

}
