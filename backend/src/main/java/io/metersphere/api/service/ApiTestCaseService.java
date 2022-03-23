package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.ApiCaseEditRequest;
import io.metersphere.api.dto.DeleteCheckResult;
import io.metersphere.api.dto.JmxInfoDTO;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.dto.definition.request.MsThreadGroup;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.constants.TestPlanStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.DefinitionReference;
import io.metersphere.plugin.core.MsTestElement;
import io.metersphere.service.FileService;
import io.metersphere.service.UserService;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import io.metersphere.track.service.TestPlanService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.aspectj.util.FileUtil;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiTestCaseService {
    @Resource
    TestPlanMapper testPlanMapper;
    @Resource
    TestCaseReviewMapper testCaseReviewMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private UserService userService;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ApiTestFileMapper apiTestFileMapper;
    @Resource
    private ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;
    @Resource
    private FileService fileService;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private EsbApiParamService esbApiParamService;
    @Resource
    private ApiScenarioReferenceIdService apiScenarioReferenceIdService;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    @Lazy
    private APITestService apiTestService;
    @Resource
    @Lazy
    private TestPlanService testPlanService;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private ApiTestCaseFollowMapper apiTestCaseFollowMapper;
    @Resource
    private ExtProjectVersionMapper extProjectVersionMapper;
    @Resource
    private TcpApiParamService tcpApiParamService;

    private static final String BODY_FILE_DIR = FileUtils.BODY_FILE_DIR;

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
        List<ApiTestCaseResult> returnList = extApiTestCaseMapper.list(request);
        for (ApiTestCaseResult res : returnList) {
            if (StringUtils.equalsIgnoreCase(res.getApiMethod(), "esb")) {
                esbApiParamService.handleApiEsbParams(res);
            }
        }
        return returnList;
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

    public void setCaseEnvironment(List<ApiTestCaseDTO> apiTestCases) {
        for (ApiTestCaseDTO apiCase : apiTestCases) {
            ApiTestEnvironment environment = getApiCaseEnvironment(apiCase.getId());
            if (environment != null) {
                apiCase.setEnvironment(environment.getName());
            }
            if(apiCase.getExecResult() == null && StringUtils.isNotEmpty(apiCase.getStatus()) && !StringUtils.equalsIgnoreCase(apiCase.getStatus(),"trash")){
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
     * @param setDefultOrders
     * @param checkThisWeekData
     * @return
     */
    private ApiTestCaseRequest initRequest(ApiTestCaseRequest request, boolean setDefultOrders, boolean checkThisWeekData) {
        if (setDefultOrders) {
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
            if (request.isToUpdate()) {
                Long timestamp = DateUtils.getTimestamp(-3);
                request.setUpdateTime(timestamp);
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

        Map<String, String> passRates = passRateList.stream()
                .collect(Collectors.toMap(ParamsDTO::getId, ParamsDTO::getValue));
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
                //检查用例的执行状态是否是null（如果报告被删除）。 如果执行结果是null，取记录最后执行状态的status字段
                if(caseResult.getExecResult() == null && StringUtils.isNotEmpty(caseResult.getStatus()) && !StringUtils.equalsIgnoreCase(caseResult.getStatus(),"trash")){
                    caseResult.setExecResult(caseResult.getStatus());
                }
            });
        }
    }

    public ApiTestCaseInfo get(String id) {
        ApiTestCaseInfo model = extApiTestCaseMapper.selectApiCaseInfoByPrimaryKey(id);
        if (model != null) {
            if (StringUtils.equalsIgnoreCase(model.getApiMethod(), "esb")) {
                esbApiParamService.handleApiEsbParams(model);
            }
        }
        return model;
    }

    public ApiTestCaseInfo getResult(String id) {
        return extApiTestCaseMapper.selectApiCaseInfoByPrimaryKey(id);
    }

    public ApiTestCase create(SaveApiTestCaseRequest request, List<MultipartFile> bodyFiles) {
        if (StringUtils.isBlank(request.getVersionId())) {
            request.setVersionId(extProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        }
        ApiTestCase test = createTest(request, bodyFiles);
        return test;
    }

    public ApiTestCase update(SaveApiTestCaseRequest request, List<MultipartFile> bodyFiles) {
        deleteFileByTestId(request.getId());
        request.setBodyUploadIds(null);
        ApiTestCase test = updateTest(request);
        if (request.getRequest() != null) {
            // requestID 跟接口id 不一致的情况
            FileUtils.createBodyFiles(request.getRequest().getId(), bodyFiles);
        } else {
            FileUtils.createBodyFiles(request.getId(), bodyFiles);
        }
        return test;
    }

    public void delete(String testId) {
        extTestPlanTestCaseMapper.deleteByTestCaseID(testId);
        deleteFileByTestId(testId);
        extApiDefinitionExecResultMapper.deleteByResourceId(testId);
        apiTestCaseMapper.deleteByPrimaryKey(testId);
        esbApiParamService.deleteByResourceId(testId);
        deleteBodyFiles(testId);
        deleteFollows(testId);
    }

    private void deleteFollows(String testId) {
        ApiTestCaseFollowExample example = new ApiTestCaseFollowExample();
        example.createCriteria().andCaseIdEqualTo(testId);
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

    /**
     * 是否已经创建了测试用例
     */
    public void checkIsRelateTest(String apiId) {
        ApiTestCaseExample testCaseExample = new ApiTestCaseExample();
        testCaseExample.createCriteria().andApiDefinitionIdEqualTo(apiId);
        List<ApiTestCase> testCases = apiTestCaseMapper.selectByExample(testCaseExample);
        StringBuilder caseName = new StringBuilder();
        if (testCases.size() > 0) {
            for (ApiTestCase testCase : testCases) {
                caseName = caseName.append(testCase.getName()).append(",");
            }
            String str = caseName.toString().substring(0, caseName.length() - 1);
            MSException.throwException(Translator.get("related_case_del_fail_prefix") + " " + str + " " + Translator.get("related_case_del_fail_suffix"));
        }
    }

    public void deleteBodyFiles(String testId) {
        File file = new File(BODY_FILE_DIR + "/" + testId);
        FileUtil.deleteContents(file);
        if (file.exists()) {
            file.delete();
        }
    }

    public void checkNameExist(SaveApiTestCaseRequest request) {
        if (hasSameCase(request)) {
            MSException.throwException(Translator.get("load_test_already_exists"));
        }
    }

    public Boolean hasSameCase(SaveApiTestCaseRequest request) {
        if (getSameCase(request) != null) {
            return true;
        }
        return false;
    }

    public ApiTestCase getSameCase(SaveApiTestCaseRequest request) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        ApiTestCaseExample.Criteria criteria = example.createCriteria();
        criteria.andStatusNotEqualTo("Trash").andNameEqualTo(request.getName()).andApiDefinitionIdEqualTo(request.getApiDefinitionId());
        if (StringUtils.isNotBlank(request.getId())) {
            criteria.andIdNotEqualTo(request.getId());
        }
        if (StringUtils.isNotBlank(request.getVersionId())) {
            criteria.andVersionIdEqualTo(request.getVersionId());
        }
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(apiTestCases)) {
            return apiTestCases.get(0);
        }
        return null;
    }

    public ApiTestCase getSameCaseById(SaveApiTestCaseRequest request) {
        if (StringUtils.isNotBlank(request.getId())) {
            ApiTestCaseExample example = new ApiTestCaseExample();
            ApiTestCaseExample.Criteria criteria = example.createCriteria();
            criteria.andStatusNotEqualTo("Trash")
                    .andApiDefinitionIdEqualTo(request.getApiDefinitionId());
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
        if (StringUtils.isNotEmpty(request.getEsbDataStruct())) {
            request = esbApiParamService.handleEsbRequest(request);
        }

        final ApiTestCaseWithBLOBs test = apiTestCaseMapper.selectByPrimaryKey(request.getId());
        if (test != null) {
            test.setName(request.getName());
            test.setCaseStatus(request.getCaseStatus());
            if (StringUtils.isEmpty(request.getCaseStatus())) {
                test.setCaseStatus(APITestStatus.Underway.name());
            }
            test.setApiDefinitionId(request.getApiDefinitionId());
            test.setUpdateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
            test.setProjectId(request.getProjectId());
            test.setRequest(JSONObject.toJSONString(request.getRequest()));
            test.setPriority(request.getPriority());
            test.setUpdateTime(System.currentTimeMillis());
            test.setDescription(request.getDescription());
            test.setVersion(request.getVersion() == null ? 0 : request.getVersion() + 1);
            test.setVersionId(request.getVersionId());
            if (StringUtils.equals("[]", request.getTags())) {
                test.setTags("");
            } else {
                test.setTags(request.getTags());
            }
            apiTestCaseMapper.updateByPrimaryKeySelective(test);
            saveFollows(test.getId(), request.getFollows());
        }
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
        FileUtils.createBodyFiles(request.getId(), bodyFiles);
        request.setRequest(tcpApiParamService.parseMsTestElement(request.getRequest()));
        if (StringUtils.isNotEmpty(request.getEsbDataStruct()) || StringUtils.isNotEmpty(request.getBackEsbDataStruct())) {
            request = esbApiParamService.handleEsbRequest(request);
        }
        FileUtils.copyBdyFile(request.getApiDefinitionId(), request.getId());

        final ApiTestCaseWithBLOBs test = new ApiTestCaseWithBLOBs();
        test.setId(request.getId());
        test.setName(request.getName());
        test.setStatus("");
        test.setCaseStatus(request.getCaseStatus());
        if (StringUtils.isEmpty(request.getCaseStatus())) {
            test.setCaseStatus(APITestStatus.Underway.name());
        }
        test.setApiDefinitionId(request.getApiDefinitionId());
        test.setCreateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        test.setUpdateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        test.setProjectId(request.getProjectId());
        test.setRequest(JSONObject.toJSONString(request.getRequest()));
        test.setCreateTime(System.currentTimeMillis());
        test.setPriority(request.getPriority());
        test.setUpdateTime(System.currentTimeMillis());
        test.setDescription(request.getDescription());
        test.setNum(getNextNum(request.getApiDefinitionId()));
        test.setOrder(ServiceUtils.getNextOrder(request.getProjectId(), extApiTestCaseMapper::getLastOrder));
        test.setVersionId(request.getVersionId());
        if (StringUtils.equals("[]", request.getTags())) {
            test.setTags("");
        } else {
            test.setTags(request.getTags());
        }
        ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(test.getId());
        if (apiTestCaseWithBLOBs == null) {
            apiTestCaseMapper.insert(test);
            saveFollows(test.getId(), request.getFollows());
        }
        return test;
    }

    public int getNextNum(String definitionId) {
        ApiTestCase apiTestCase = extApiTestCaseMapper.getNextNum(definitionId);
        if (apiTestCase == null) {
            int n = apiDefinitionMapper.selectByPrimaryKey(definitionId).getNum();
            return n * 1000 + 1;
        } else {
            return Optional.of(apiTestCase.getNum() + 1)
                    .orElse(apiDefinitionMapper.selectByPrimaryKey(definitionId).getNum() * 1000 + 1);
        }
    }

    public int getNextNum(String definitionId, Integer definitionNum) {
        ApiTestCase apiTestCase = extApiTestCaseMapper.getNextNum(definitionId);
        if (apiTestCase == null) {
            if (definitionNum == null) {
                return apiDefinitionMapper.selectByPrimaryKey(definitionId).getNum() * 1000 + 1;
            }
            return definitionNum * 1000 + 1;
        } else {
            return Optional.of(apiTestCase.getNum() + 1)
                    .orElse(apiDefinitionMapper.selectByPrimaryKey(definitionId).getNum() * 1000 + 1);
        }
    }

    private void deleteFileByTestId(String testId) {
        ApiTestFileExample ApiTestFileExample = new ApiTestFileExample();
        ApiTestFileExample.createCriteria().andTestIdEqualTo(testId);
        final List<ApiTestFile> ApiTestFiles = apiTestFileMapper.selectByExample(ApiTestFileExample);
        if (CollectionUtils.isNotEmpty(ApiTestFiles)) {
            apiTestFileMapper.deleteByExample(ApiTestFileExample);
        }

        if (!CollectionUtils.isEmpty(ApiTestFiles)) {
            final List<String> fileIds = ApiTestFiles.stream().map(ApiTestFile::getFileId).collect(Collectors.toList());
            fileService.deleteFileByIds(fileIds);
        }
    }

    public void removeToGc(List<String> ids) {
        // todo
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
        for (String testId : ids) {
            extTestPlanTestCaseMapper.deleteByTestCaseID(testId);
            deleteFollows(testId);
        }
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(ids);
        apiTestCaseMapper.deleteByExample(example);
    }

    public void deleteBatchByDefinitionId(List<String> definitionIds) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andApiDefinitionIdIn(definitionIds);
        apiTestCaseMapper.deleteByExample(example);
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        List<String> caseIds = apiTestCases.stream().map(ApiTestCase::getId).collect(Collectors.toList());
        for (String testId : caseIds) {
            extTestPlanTestCaseMapper.deleteByTestCaseID(testId);
        }
    }

    public void relevanceByApi(ApiCaseRelevanceRequest request) {
        if (CollectionUtils.isEmpty(request.getSelectIds())) {
            return;
        }
        // 尽量保持与用例顺序一致
        Collections.reverse(request.getSelectIds());

        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andApiDefinitionIdIn(request.getSelectIds());
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        relevance(apiTestCases, request);
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
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        relevanceByReview(apiTestCases, request);
    }

    public void relevanceByCase(ApiCaseRelevanceRequest request) {
        List<String> ids = request.getSelectIds();
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(ids);
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        relevance(apiTestCases, request);
    }

    private void relevance(List<ApiTestCase> apiTestCases, ApiCaseRelevanceRequest request) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);

        ExtTestPlanApiCaseMapper batchMapper = sqlSession.getMapper(ExtTestPlanApiCaseMapper.class);
        TestPlanApiCaseMapper batchBaseMapper = sqlSession.getMapper(TestPlanApiCaseMapper.class);
        Long nextOrder = ServiceUtils.getNextOrder(request.getPlanId(), extTestPlanApiCaseMapper::getLastOrder);

        for (ApiTestCase apiTestCase : apiTestCases) {
            TestPlanApiCase testPlanApiCase = new TestPlanApiCase();
            testPlanApiCase.setId(UUID.randomUUID().toString());
            testPlanApiCase.setCreateUser(SessionUtils.getUserId());
            testPlanApiCase.setApiCaseId(apiTestCase.getId());
            testPlanApiCase.setTestPlanId(request.getPlanId());
            testPlanApiCase.setEnvironmentId(request.getEnvironmentId());
            testPlanApiCase.setCreateTime(System.currentTimeMillis());
            testPlanApiCase.setUpdateTime(System.currentTimeMillis());
            testPlanApiCase.setOrder(nextOrder);
            nextOrder += ServiceUtils.ORDER_STEP;
            if (testPlanService.isAllowedRepeatCase(request.getPlanId())) {
                batchBaseMapper.insert(testPlanApiCase);
            } else {
                batchMapper.insertIfNotExists(testPlanApiCase);
            }
        }

        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getPlanId());
        if (StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Prepare.name())
                || StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Completed.name())) {
            testPlan.setStatus(TestPlanStatus.Underway.name());
            testPlan.setActualStartTime(System.currentTimeMillis());  // 将状态更新为进行中时，开始时间也要更新
            testPlan.setActualEndTime(null);
            testPlanMapper.updateByPrimaryKey(testPlan);
        }
        sqlSession.flushStatements();
    }

    private void relevanceByReview(List<ApiTestCase> apiTestCases, ApiCaseRelevanceRequest request) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtTestCaseReviewApiCaseMapper batchMapper = sqlSession.getMapper(ExtTestCaseReviewApiCaseMapper.class);
        apiTestCases.forEach(apiTestCase -> {
            TestCaseReviewApiCase TestCaseReviewApiCase = new TestCaseReviewApiCase();
            TestCaseReviewApiCase.setId(UUID.randomUUID().toString());
            TestCaseReviewApiCase.setApiCaseId(apiTestCase.getId());
            TestCaseReviewApiCase.setTestCaseReviewId(request.getReviewId());
            TestCaseReviewApiCase.setEnvironmentId(request.getEnvironmentId());
            TestCaseReviewApiCase.setCreateTime(System.currentTimeMillis());
            TestCaseReviewApiCase.setUpdateTime(System.currentTimeMillis());
            batchMapper.insertIfNotExists(TestCaseReviewApiCase);
        });
        TestCaseReview testCaseReview = testCaseReviewMapper.selectByPrimaryKey(request.getReviewId());
        if (StringUtils.equals(testCaseReview.getStatus(), TestPlanStatus.Prepare.name())
                || StringUtils.equals(testCaseReview.getStatus(), TestPlanStatus.Completed.name())) {
            testCaseReview.setStatus(TestPlanStatus.Underway.name());
            testCaseReviewMapper.updateByPrimaryKey(testCaseReview);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public List<String> selectIdsNotExistsInPlan(String projectId, String planId) {
        return extApiTestCaseMapper.selectIdsNotExistsInPlan(projectId, planId);
    }

    public List<String> selectIdsNotExistsInReview(String projectId, String reviewId) {
        return extApiTestCaseMapper.selectIdsNotExistsInReview(projectId, reviewId);
    }

    public List<ApiDataCountResult> countProtocolByProjectID(String projectId) {
        return extApiTestCaseMapper.countProtocolByProjectID(projectId);
    }

    public long countByProjectIDAndCreateInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiTestCaseMapper.countByProjectIDAndCreateInThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<ApiTestCase> selectCasesBydIds(List<String> caseIds) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(caseIds);
        return apiTestCaseMapper.selectByExample(example);
    }

    public List<ApiTestCaseWithBLOBs> selectCasesBydApiIds(List<String> apiIds) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andApiDefinitionIdIn(apiIds).andStatusNotEqualTo("Trash");
        return apiTestCaseMapper.selectByExampleWithBLOBs(example);
    }

    public Map<String, String> getRequest(ApiTestCaseRequest request) {
        List<ApiTestCaseInfo> list = extApiTestCaseMapper.getRequest(request);
        for (ApiTestCaseInfo model : list) {
            if (StringUtils.equalsIgnoreCase(model.getApiMethod(), "esb")) {
                esbApiParamService.handleApiEsbParams(model);
            }
        }
        return list.stream().collect(Collectors.toMap(ApiTestCaseWithBLOBs::getId, ApiTestCaseWithBLOBs::getRequest));
    }

    public void deleteBatchByParam(ApiTestBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.isSelectAll()) {
            ids = this.getAllApiCaseIdsByFontedSelect(request.getFilters(), request.getModuleIds(), request.getName(), request.getProjectId(), request.getProtocol(), request.getUnSelectIds(), request.getStatus(), null, request.getCombine());
        }
        this.deleteBatch(ids);
    }

    public void editApiBathByParam(ApiTestBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.isSelectAll()) {
            ids = this.getAllApiCaseIdsByFontedSelect(request.getFilters(), request.getModuleIds(), request.getName(), request.getProjectId(), request.getProtocol(), request.getUnSelectIds(), request.getStatus(), null, request.getCombine());
        }
        ApiTestCaseExample apiDefinitionExample = new ApiTestCaseExample();
        apiDefinitionExample.createCriteria().andIdIn(ids);
        if (StringUtils.isNotEmpty(request.getPriority())) {
            ApiTestCaseWithBLOBs apiDefinitionWithBLOBs = new ApiTestCaseWithBLOBs();
            apiDefinitionWithBLOBs.setPriority(request.getPriority());
            apiDefinitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
            apiTestCaseMapper.updateByExampleSelective(apiDefinitionWithBLOBs, apiDefinitionExample);
        }
        if (StringUtils.isNotEmpty(request.getEnvId())) {
            List<ApiTestCaseWithBLOBs> bloBs = apiTestCaseMapper.selectByExampleWithBLOBs(apiDefinitionExample);
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiTestCaseMapper batchMapper = sqlSession.getMapper(ApiTestCaseMapper.class);

            bloBs.forEach(apiTestCase -> {
                JSONObject req = JSON.parseObject(apiTestCase.getRequest());
                req.put("useEnvironment", request.getEnvId());
                String requestStr = JSON.toJSONString(req);
                apiTestCase.setRequest(requestStr);
                batchMapper.updateByPrimaryKeySelective(apiTestCase);
            });
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    public void updateByApiDefinitionId(List<String> ids, String path, String method, String protocol) {
        if ((StringUtils.isNotEmpty(method) || StringUtils.isNotEmpty(path) && RequestType.HTTP.equals(protocol))) {
            ApiTestCaseExample apiDefinitionExample = new ApiTestCaseExample();
            apiDefinitionExample.createCriteria().andApiDefinitionIdIn(ids);
            List<ApiTestCaseWithBLOBs> bloBs = apiTestCaseMapper.selectByExampleWithBLOBs(apiDefinitionExample);
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiTestCaseMapper batchMapper = sqlSession.getMapper(ApiTestCaseMapper.class);

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            bloBs.forEach(apiTestCase -> {
                MsHTTPSamplerProxy req = JSON.parseObject(apiTestCase.getRequest(), MsHTTPSamplerProxy.class);
                try {
                    JSONObject element = JSON.parseObject(apiTestCase.getRequest());
                    ElementUtil.dataFormatting(element);

                    if (element != null && StringUtils.isNotEmpty(element.getString("hashTree"))) {
                        LinkedList<MsTestElement> elements = mapper.readValue(element.getString("hashTree"), new TypeReference<LinkedList<MsTestElement>>() {
                        });
                        req.setHashTree(elements);
                    }
                    if (StringUtils.isNotEmpty(method)) {
                        req.setMethod(method);
                    }
                    if (StringUtils.isNotEmpty(path)) {
                        req.setPath(path);
                    }
                } catch (Exception e) {
                    LogUtil.error(e);
                }
                String requestStr = JSON.toJSONString(req);
                apiTestCase.setRequest(requestStr);
                batchMapper.updateByPrimaryKeySelective(apiTestCase);
            });
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    private List<String> getAllApiCaseIdsByFontedSelect(Map<String, List<String>> filters, List<String> moduleIds, String name, String projectId, String protocol, List<String> unSelectIds, String status, String apiId, Map<String, Object> combine) {
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
        }
        for (ApiTestCaseInfo model : list) {
            if (StringUtils.equalsIgnoreCase(model.getApiMethod(), "esb")) {
                esbApiParamService.handleApiEsbParams(model);
            }
        }

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

    public ApiDefinition findApiUrlAndMethodById(String id) {
        return extApiTestCaseMapper.findApiUrlAndMethodById(id);
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
            ids = this.getAllApiCaseIdsByFontedSelect(request.getFilters(), request.getModuleIds(), request.getName(), request.getProjectId(), request.getProtocol(), request.getUnSelectIds(), request.getStatus(), request.getApiDefinitionId(), request.getCombine());
        }
        this.deleteToGc(ids);
    }

    public List<String> reduction(ApiTestBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.isSelectAll()) {
            ids = this.getAllApiCaseIdsByFontedSelect(request.getFilters(), request.getModuleIds(), request.getName(), request.getProjectId(), request.getProtocol(), request.getUnSelectIds(), request.getStatus(), null, request.getCombine());
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

    public DeleteCheckResult checkDeleteDatas(ApiTestBatchRequest request) {
        List<String> deleteIds = request.getIds();
        if (request.isSelectAll()) {
            deleteIds = this.getAllApiCaseIdsByFontedSelect(request.getFilters(), request.getModuleIds(), request.getName(), request.getProjectId(), request.getProtocol(), request.getUnSelectIds(), request.getStatus(), request.getApiDefinitionId(), request.getCombine());
        }
        DeleteCheckResult result = new DeleteCheckResult();
        List<String> checkMsgList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<ApiScenarioReferenceId> apiScenarioReferenceIdList = apiScenarioReferenceIdService.findByReferenceIdsAndRefType(deleteIds, MsTestElementConstants.REF.name());
            if (CollectionUtils.isNotEmpty(apiScenarioReferenceIdList)) {
                Map<String, List<String>> scenarioDic = new HashMap<>();
                apiScenarioReferenceIdList.forEach(item -> {
                    String refreceID = item.getReferenceId();
                    String scenarioId = item.getApiScenarioId();
                    if (scenarioDic.containsKey(refreceID)) {
                        scenarioDic.get(refreceID).add(scenarioId);
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(scenarioId);
                        scenarioDic.put(refreceID, list);
                    }
                });

                for (Map.Entry<String, List<String>> entry : scenarioDic.entrySet()) {
                    String refreceId = entry.getKey();
                    List<String> scenarioIdList = entry.getValue();
                    if (CollectionUtils.isNotEmpty(scenarioIdList)) {
                        List<String> scenarioNameList = extApiScenarioMapper.selectNameByIdIn(scenarioIdList);
                        String deleteCaseName = extApiTestCaseMapper.selectNameById(refreceId);

                        if (StringUtils.isNotEmpty(deleteCaseName) && CollectionUtils.isNotEmpty(scenarioNameList)) {
                            String nameListStr = "[";
                            for (String name : scenarioNameList) {
                                nameListStr += name + ",";
                            }
                            if (nameListStr.length() > 1) {
                                nameListStr = nameListStr.substring(0, nameListStr.length() - 1) + "]";
                            }
                            String msg = deleteCaseName + " " + Translator.get("delete_check_reference_by") + ": " + nameListStr + " ";
                            checkMsgList.add(msg);
                        }
                    }
                }
            }
        }

        result.setDeleteFlag(checkMsgList.isEmpty());
        result.setCheckMsg(checkMsgList);
        return result;
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
        MsHTTPSamplerProxy msHTTPSamplerProxy = JSONObject.parseObject(request, MsHTTPSamplerProxy.class);
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

        JmxInfoDTO jmxInfoDTO = apiTestService.getJmxInfoDTO(runRequest, new ArrayList<>());
        jmxInfoDTO.setId(apiTestCase.getId());
        jmxInfoDTO.setVersion(apiTestCase.getVersion());
        jmxInfoDTO.setName(apiTestCase.getName());
        return jmxInfoDTO;
    }

    public List<ApiTestCase> getApiCaseByIds(List<String> apiCaseIds) {
        if (CollectionUtils.isNotEmpty(apiCaseIds)) {
            ApiTestCaseExample example = new ApiTestCaseExample();
            example.createCriteria().andIdIn(apiCaseIds);
            return apiTestCaseMapper.selectByExample(example);
        }
        return new ArrayList<>();
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(ApiTestCaseWithBLOBs.class, ApiTestCaseMapper.class,
                extApiTestCaseMapper::selectProjectIds,
                extApiTestCaseMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, ApiTestCaseWithBLOBs.class,
                apiTestCaseMapper::selectByPrimaryKey,
                extApiTestCaseMapper::getPreOrder,
                extApiTestCaseMapper::getLastOrder,
                apiTestCaseMapper::updateByPrimaryKeySelective);
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
        List<ParamsDTO> environments = extApiTestCaseMapper.getApiCaseEnvironments(caseIds);
        if (CollectionUtils.isEmpty(environments)) {
            return null;
        }
        try {
            List<String> envIds = environments.stream().map(ParamsDTO::getValue).collect(Collectors.toList());
            ApiTestEnvironmentExample example = new ApiTestEnvironmentExample();
            example.createCriteria().andIdIn(envIds);
            List<ApiTestEnvironment> environmentList = apiTestEnvironmentMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(environmentList)) {
                return null;
            }
            Map<String, String> envMap = environmentList.stream()
                    .collect(Collectors.toMap(ApiTestEnvironment::getId, ApiTestEnvironment::getName));

            Map<String, String> caseEnvMap = environments.stream().collect(HashMap::new, (m, v) -> m.put(v.getId(), v.getValue()), HashMap::putAll);
            caseEnvMap.forEach((k, v) -> {
                if (envMap.containsKey(v)) {
                    caseEnvMap.put(k, envMap.get(v));
                }
            });
            return caseEnvMap;
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

}
