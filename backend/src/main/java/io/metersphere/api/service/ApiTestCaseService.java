package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.ApiCaseBatchRequest;
import io.metersphere.api.dto.DeleteCheckResult;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.MsTestPlan;
import io.metersphere.api.dto.definition.request.MsThreadGroup;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.constants.TestPlanStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.DefinitionReference;
import io.metersphere.service.FileService;
import io.metersphere.service.QuotaService;
import io.metersphere.service.UserService;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.aspectj.util.FileUtil;
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
    private JMeterService jMeterService;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private EsbApiParamService esbApiParamService;
    @Resource
    private ApiScenarioReferenceIdService apiScenarioReferenceIdService;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;

    private static final String BODY_FILE_DIR = FileUtils.BODY_FILE_DIR;

    //查询测试用例详情
    public ApiTestCaseWithBLOBs getInfoJenkins(String id) {
        ApiTestCaseWithBLOBs apiTest = apiTestCaseMapper.selectByPrimaryKey(id);
        return apiTest;
    }

    public List<ApiTestCaseResult> list(ApiTestCaseRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
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
        buildUserInfo(apiTestCases);
        return apiTestCases;
    }

    public List<String> idSimple(ApiTestCaseRequest request) {
        request = this.initRequest(request, true, true);

        List<String> ids = extApiTestCaseMapper.idSimple(request);
        return ids;
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
            request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
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

    public void buildUserInfo(List<? extends ApiTestCaseDTO> apiTestCases) {
        List<String> userIds = new ArrayList();
        userIds.addAll(apiTestCases.stream().map(ApiTestCaseDTO::getCreateUserId).collect(Collectors.toList()));
        userIds.addAll(apiTestCases.stream().map(ApiTestCaseDTO::getUpdateUserId).collect(Collectors.toList()));
        if (!CollectionUtils.isEmpty(userIds)) {
            Map<String, User> userMap = userService.queryNameByIds(userIds);
            apiTestCases.forEach(caseResult -> {
                User createUser = userMap.get(caseResult.getCreateUserId());
                if (createUser != null) {
                    caseResult.setCreateUser(createUser.getName());
                }
                User updateUser = userMap.get(caseResult.getUpdateUserId());
                if (updateUser != null) {
                    caseResult.setUpdateUser(updateUser.getName());
                }
            });
        }
    }

    public ApiTestCaseWithBLOBs get(String id) {
//        ApiTestCaseWithBLOBs returnBlobs = apiTestCaseMapper.selectByPrimaryKey(id);
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
        ApiTestCase test = createTest(request);
        FileUtils.createBodyFiles(request.getId(), bodyFiles);
        return test;
    }

    private void checkQuota() {
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        if (quotaService != null) {
            quotaService.checkAPITestQuota();
        }
    }

    public ApiTestCase update(SaveApiTestCaseRequest request, List<MultipartFile> bodyFiles) {
        deleteFileByTestId(request.getId());
        request.setBodyUploadIds(null);
        ApiTestCase test = updateTest(request);
        FileUtils.createBodyFiles(request.getId(), bodyFiles);
        return test;
    }

    public void delete(String testId) {
        extTestPlanTestCaseMapper.deleteByTestCaseID(testId);
        deleteFileByTestId(testId);
        extApiDefinitionExecResultMapper.deleteByResourceId(testId);
        apiTestCaseMapper.deleteByPrimaryKey(testId);
        esbApiParamService.deleteByResourceId(testId);
        deleteBodyFiles(testId);
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
        ApiTestCaseExample example = new ApiTestCaseExample();
        ApiTestCaseExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(request.getName()).andApiDefinitionIdEqualTo(request.getApiDefinitionId());
        if (StringUtils.isNotBlank(request.getId())) {
            criteria.andIdNotEqualTo(request.getId());
        }
        if (apiTestCaseMapper.countByExample(example) > 0) {
            return true;
        }
        return false;
    }

    private ApiTestCase updateTest(SaveApiTestCaseRequest request) {
        checkNameExist(request);

        if (StringUtils.isNotEmpty(request.getEsbDataStruct())) {
            request = esbApiParamService.handleEsbRequest(request);
        }

        final ApiTestCaseWithBLOBs test = new ApiTestCaseWithBLOBs();
        test.setId(request.getId());
        test.setName(request.getName());
        test.setApiDefinitionId(request.getApiDefinitionId());
        test.setUpdateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        test.setProjectId(request.getProjectId());
        test.setRequest(JSONObject.toJSONString(request.getRequest()));
        test.setPriority(request.getPriority());
        test.setUpdateTime(System.currentTimeMillis());
        test.setDescription(request.getDescription());
        if (StringUtils.equals("[]", request.getTags())) {
            test.setTags("");
        } else {
            test.setTags(request.getTags());
        }
        apiTestCaseMapper.updateByPrimaryKeySelective(test);
        return test;
    }

    private ApiTestCase createTest(SaveApiTestCaseRequest request) {
        checkNameExist(request);

        if (StringUtils.isNotEmpty(request.getEsbDataStruct()) || StringUtils.isNotEmpty(request.getBackEsbDataStruct())) {
            request = esbApiParamService.handleEsbRequest(request);
        }

        final ApiTestCaseWithBLOBs test = new ApiTestCaseWithBLOBs();
        test.setId(request.getId());
        test.setName(request.getName());
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
        if (StringUtils.equals("[]", request.getTags())) {
            test.setTags("");
        } else {
            test.setTags(request.getTags());
        }
        FileUtils.copyBdyFile(request.getApiDefinitionId(), request.getId());
        apiTestCaseMapper.insert(test);
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

    public void editApiBath(ApiCaseBatchRequest request) {
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
        }
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(ids);
        apiTestCaseMapper.deleteByExample(example);
    }

    public void relevanceByApi(ApiCaseRelevanceRequest request) {
        if (CollectionUtils.isEmpty(request.getSelectIds())) {
            return;
        }
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
        apiTestCases.forEach(apiTestCase -> {
            TestPlanApiCase testPlanApiCase = new TestPlanApiCase();
            testPlanApiCase.setId(UUID.randomUUID().toString());
            testPlanApiCase.setCreateUser(SessionUtils.getUserId());
            testPlanApiCase.setApiCaseId(apiTestCase.getId());
            testPlanApiCase.setTestPlanId(request.getPlanId());
            testPlanApiCase.setEnvironmentId(request.getEnvironmentId());
            testPlanApiCase.setCreateTime(System.currentTimeMillis());
            testPlanApiCase.setUpdateTime(System.currentTimeMillis());
            batchMapper.insertIfNotExists(testPlanApiCase);
        });
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
        example.createCriteria().andApiDefinitionIdIn(apiIds);
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
            ids = this.getAllApiCaseIdsByFontedSelect(request.getFilters(), request.getModuleIds(), request.getName(), request.getProjectId(), request.getProtocol(), request.getUnSelectIds(), request.getStatus(),null);
        }
        this.deleteBatch(ids);
    }

    public void editApiBathByParam(ApiTestBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.isSelectAll()) {
            ids = this.getAllApiCaseIdsByFontedSelect(request.getFilters(), request.getModuleIds(), request.getName(), request.getProjectId(), request.getProtocol(), request.getUnSelectIds(), request.getStatus(),null);
        }
        ApiTestCaseExample apiDefinitionExample = new ApiTestCaseExample();
        apiDefinitionExample.createCriteria().andIdIn(ids);
        if (StringUtils.isNotEmpty(request.getPriority())) {
            ApiTestCaseWithBLOBs apiDefinitionWithBLOBs = new ApiTestCaseWithBLOBs();
            apiDefinitionWithBLOBs.setPriority(request.getPriority());
            apiDefinitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
            apiTestCaseMapper.updateByExampleSelective(apiDefinitionWithBLOBs, apiDefinitionExample);
        }
        if ((StringUtils.isNotEmpty(request.getMethod()) || StringUtils.isNotEmpty(request.getPath())) && RequestType.HTTP.equals(request.getProtocol())) {
            List<ApiTestCaseWithBLOBs> bloBs = apiTestCaseMapper.selectByExampleWithBLOBs(apiDefinitionExample);
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiTestCaseMapper batchMapper = sqlSession.getMapper(ApiTestCaseMapper.class);

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            bloBs.forEach(apiTestCase -> {
                MsHTTPSamplerProxy req = JSON.parseObject(apiTestCase.getRequest(), MsHTTPSamplerProxy.class);
                try {
                    JSONObject element = JSON.parseObject(apiTestCase.getRequest());
                    if (element != null && StringUtils.isNotEmpty(element.getString("hashTree"))) {
                        LinkedList<MsTestElement> elements = mapper.readValue(element.getString("hashTree"), new TypeReference<LinkedList<MsTestElement>>() {
                        });
                        req.setHashTree(elements);
                    }
                    if (StringUtils.isNotEmpty(request.getMethod())) {
                        req.setMethod(request.getMethod());
                    }
                    if (StringUtils.isNotEmpty(request.getPath())) {
                        req.setPath(request.getPath());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.error(e.getMessage());
                }
                String requestStr = JSON.toJSONString(req);
                apiTestCase.setRequest(requestStr);
                batchMapper.updateByPrimaryKeySelective(apiTestCase);
            });
            sqlSession.flushStatements();

        }
        if (StringUtils.isNotEmpty(request.getEnvId())) {
            List<ApiTestCaseWithBLOBs> bloBs = apiTestCaseMapper.selectByExampleWithBLOBs(apiDefinitionExample);
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiTestCaseMapper batchMapper = sqlSession.getMapper(ApiTestCaseMapper.class);

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            bloBs.forEach(apiTestCase -> {
                JSONObject req = JSON.parseObject(apiTestCase.getRequest());
                req.put("useEnvironment", request.getEnvId());
                String requestStr = JSON.toJSONString(req);
                apiTestCase.setRequest(requestStr);
                batchMapper.updateByPrimaryKeySelective(apiTestCase);
            });
            sqlSession.flushStatements();
        }
    }

    private List<String> getAllApiCaseIdsByFontedSelect(Map<String, List<String>> filters, List<String> moduleIds, String name, String projectId, String protocol, List<String> unSelectIds, String status,String apiId) {
        ApiTestCaseRequest selectRequest = new ApiTestCaseRequest();
        selectRequest.setFilters(filters);
        selectRequest.setModuleIds(moduleIds);
        selectRequest.setName(name);
        selectRequest.setProjectId(projectId);
        selectRequest.setProtocol(protocol);
        selectRequest.setStatus(status);
        selectRequest.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        selectRequest.setApiDefinitionId(apiId);
        List<ApiTestCaseDTO> list = extApiTestCaseMapper.listSimple(selectRequest);
        List<String> allIds = list.stream().map(ApiTestCaseDTO::getId).collect(Collectors.toList());
        List<String> ids = allIds.stream().filter(id -> !unSelectIds.contains(id)).collect(Collectors.toList());
        return ids;
    }

    public String run(RunCaseRequest request) {
        ApiTestCaseWithBLOBs testCaseWithBLOBs = new ApiTestCaseWithBLOBs();
        if (StringUtils.equals(request.getRunMode(), ApiRunMode.JENKINS_API_PLAN.name())) {
            testCaseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(request.getReportId());
            request.setCaseId(request.getReportId());
        } else {
            testCaseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(request.getCaseId());

        }
        if (StringUtils.equals(request.getRunMode(), ApiRunMode.JENKINS.name())) {
            request.setReportId(request.getEnvironmentId());
        }
        if (StringUtils.equals(request.getRunMode(), ApiRunMode.JENKINS_API_PLAN.name())) {
            //通过测试计划id查询环境
            request.setReportId(request.getTestPlanId());
        }
        // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
        if (testCaseWithBLOBs != null && StringUtils.isNotEmpty(testCaseWithBLOBs.getRequest())) {
            try {
                HashTree jmeterHashTree = this.generateHashTree(request, testCaseWithBLOBs);
/*
                String runMode = ApiRunMode.JENKINS.name();
*/
                // 调用执行方法
                jMeterService.runLocal(request.getCaseId(), jmeterHashTree, request.getReportId(), request.getRunMode());

            } catch (Exception ex) {
                LogUtil.error(ex.getMessage());
            }
        }
        return request.getReportId();
    }

    public String run(ApiTestCaseWithBLOBs apiCaseBolbs, String id, String debugReportId, String testPlanID, String runMode) {
        // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
        if (apiCaseBolbs != null && StringUtils.isNotEmpty(apiCaseBolbs.getRequest())) {
            try {
                ApiTestCase apiTestCase = apiTestCaseMapper.selectByPrimaryKey(apiCaseBolbs.getId());
                RunCaseRequest request = new RunCaseRequest();
                request.setCaseId(apiTestCase.getId());
                request.setTestPlanId(testPlanID);
                HashTree jmeterHashTree = this.generateHashTree(request, apiCaseBolbs);
                // 调用执行方法
                jMeterService.runLocal(id, jmeterHashTree, debugReportId, runMode);
            } catch (Exception ex) {
                LogUtil.error(ex.getMessage());
            }
        }
        return id;
    }

    public HashTree generateHashTree(RunCaseRequest request, ApiTestCaseWithBLOBs testCaseWithBLOBs) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MsTestElement element = mapper.readValue(testCaseWithBLOBs.getRequest(), new TypeReference<MsTestElement>() {
        });
        element.setProjectId(testCaseWithBLOBs.getProjectId());
        if (StringUtils.isBlank(request.getEnvironmentId())) {
            TestPlanApiCaseExample example = new TestPlanApiCaseExample();
            example.createCriteria().andTestPlanIdEqualTo(request.getTestPlanId()).andApiCaseIdEqualTo(request.getCaseId());
            List<TestPlanApiCase> list = testPlanApiCaseMapper.selectByExample(example);
            request.setEnvironmentId(list.get(0).getEnvironmentId());
            element.setName(list.get(0).getId());
        } else {
            element.setName(request.getCaseId());
        }

        // 测试计划
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setHashTree(new LinkedList<>());
        HashTree jmeterHashTree = new ListedHashTree();

        // 线程组
        MsThreadGroup group = new MsThreadGroup();
        group.setLabel(testCaseWithBLOBs.getName());
        group.setName(testCaseWithBLOBs.getId());
        group.setOnSampleError(true);
        LinkedList<MsTestElement> hashTrees = new LinkedList<>();
        hashTrees.add(element);
        group.setHashTree(hashTrees);
        testPlan.getHashTree().add(group);
        ApiTestEnvironmentService environmentService = CommonBeanFactory.getBean(ApiTestEnvironmentService.class);
        ApiTestEnvironmentWithBLOBs environment = environmentService.get(request.getEnvironmentId());
        ParameterConfig parameterConfig = new ParameterConfig();

        Map<String, EnvironmentConfig> envConfig = new HashMap<>(16);
        if (environment != null && environment.getConfig() != null) {
            EnvironmentConfig environmentConfig = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
            envConfig.put(testCaseWithBLOBs.getProjectId(), environmentConfig);
            parameterConfig.setConfig(envConfig);
        }

        testPlan.toHashTree(jmeterHashTree, testPlan.getHashTree(), parameterConfig);
        return jmeterHashTree;
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
        if(StringUtils.isEmpty(request.getId()) && CollectionUtils.isEmpty(request.getIds())){
            list = new ArrayList<>();
        }else {
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
        if(CollectionUtils.isNotEmpty(ids)){
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
            ids = this.getAllApiCaseIdsByFontedSelect(request.getFilters(), request.getModuleIds(), request.getName(), request.getProjectId(), request.getProtocol(), request.getUnSelectIds(), request.getStatus(),request.getApiDefinitionId());
        }
        this.deleteToGc(ids);
    }

    public List<String> reduction(ApiTestBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.isSelectAll()) {
            ids = this.getAllApiCaseIdsByFontedSelect(request.getFilters(), request.getModuleIds(), request.getName(), request.getProjectId(), request.getProtocol(), request.getUnSelectIds(), request.getStatus(),null);
        }

        List<String> cannotReductionAPiName = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(ids)){
            List<ApiTestCaseDTO> cannotReductionApiCaseList = extApiTestCaseMapper.getCannotReductionApiCaseList(ids);
            List<String> cannotReductionCaseId = new ArrayList<>();
            for (ApiTestCaseDTO apiTestCaseDTO:cannotReductionApiCaseList) {
                if(!cannotReductionAPiName.contains(apiTestCaseDTO.getApiName())){
                    cannotReductionAPiName.add(apiTestCaseDTO.getApiName());
                }
                cannotReductionCaseId.add(apiTestCaseDTO.getId());
            }
            cannotReductionApiCaseList.stream().map(ApiTestCaseDTO::getId).collect(Collectors.toList());
            List<String> deleteIds = ids.stream().filter(id -> !cannotReductionCaseId.contains(id)).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(deleteIds)){
                extApiTestCaseMapper.checkOriginalStatusByIds(deleteIds);
                extApiTestCaseMapper.reduction(deleteIds);
            }
        }
        return cannotReductionAPiName;
    }

    public List<String> selectCaseIdsByApiIds(List<String> apiIds) {
        if(CollectionUtils.isEmpty(apiIds)){
            return  new ArrayList<>(0);
        }else {
            return extApiTestCaseMapper.selectCaseIdsByApiIds(apiIds);
        }
    }

    public DeleteCheckResult checkDeleteDatas(ApiTestBatchRequest request) {
        List<String> deleteIds = request.getIds();
        if (request.isSelectAll()) {
            deleteIds = this.getAllApiCaseIdsByFontedSelect(request.getFilters(), request.getModuleIds(), request.getName(), request.getProjectId(), request.getProtocol(), request.getUnSelectIds(), request.getStatus(),request.getApiDefinitionId());
        }
        DeleteCheckResult result = new DeleteCheckResult();
        List<String> checkMsgList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(deleteIds)){
            List<ApiScenarioReferenceId> apiScenarioReferenceIdList = apiScenarioReferenceIdService.findByReferenceIdsAndRefType(deleteIds, MsTestElementConstants.REF.name());
            if(CollectionUtils.isNotEmpty(apiScenarioReferenceIdList)){
                Map<String,List<String>> scenarioDic = new HashMap<>();
                apiScenarioReferenceIdList.forEach( item ->{
                    String refreceID = item.getReferenceId();
                    String scenarioId = item.getApiScenarioId();
                    if(scenarioDic.containsKey(refreceID)){
                        scenarioDic.get(refreceID).add(scenarioId);
                    }else {
                        List<String> list = new ArrayList<>();
                        list.add(scenarioId);
                        scenarioDic.put(refreceID,list);
                    }
                });

                for (Map.Entry<String,List<String>> entry : scenarioDic.entrySet()){
                    String refreceId = entry.getKey();
                    List<String> scenarioIdList = entry.getValue();
                    if(CollectionUtils.isNotEmpty(scenarioIdList)){
                        List<String> scenarioNameList = extApiScenarioMapper.selectNameByIdIn(scenarioIdList);
                        String deleteCaseName = extApiTestCaseMapper.selectNameById(refreceId);

                        if(StringUtils.isNotEmpty(deleteCaseName) && CollectionUtils.isNotEmpty(scenarioNameList)){
                            String nameListStr = "【";
                            for (String name : scenarioNameList) {
                                nameListStr+= name +",";
                            }
                            if(nameListStr.length() > 1){
                                nameListStr = nameListStr.substring(0,nameListStr.length()-1) + "】";
                            }
                            String msg = deleteCaseName+" "+Translator.get("delete_check_reference_by")+": "+nameListStr+" ";
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
}
