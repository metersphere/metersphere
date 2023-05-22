package io.metersphere.service.plan;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.ApiCaseRelevanceRequest;
import io.metersphere.api.dto.ApiReportEnvConfigDTO;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.QueryReferenceRequest;
import io.metersphere.api.dto.automation.TestPlanApiDTO;
import io.metersphere.api.dto.automation.TestPlanDTO;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.plan.AutomationsRunInfoDTO;
import io.metersphere.api.dto.plan.TestPlanApiCaseBatchRequest;
import io.metersphere.api.dto.plan.TestPlanApiCaseInfoDTO;
import io.metersphere.api.exec.api.ApiCaseExecuteService;
import io.metersphere.api.exec.api.ApiExecuteService;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.ApiScenarioReportMapper;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionExecResultMapper;
import io.metersphere.base.mapper.plan.TestPlanApiCaseMapper;
import io.metersphere.base.mapper.plan.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.enums.ApiReportStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.environment.service.BaseEnvGroupProjectService;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.request.OrderRequest;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.service.BaseProjectService;
import io.metersphere.service.ServiceUtils;
import io.metersphere.service.definition.*;
import io.metersphere.service.plan.remote.TestPlanService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiCaseService {

    @Resource
    TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Lazy
    @Resource
    ApiTestCaseService apiTestCaseService;
    @Resource
    ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Lazy
    @Resource
    ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiCaseExecuteService testPlanApiCaseExecuteService;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    private BaseEnvGroupProjectService environmentGroupProjectService;
    @Lazy
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;
    @Lazy
    @Resource
    private ApiModuleService apiModuleService;
    @Resource
    BaseProjectService baseProjectService;
    @Resource
    private ApiDefinitionExecResultMapper apiDefinitionExecResultMapper;
    @Resource
    private ApiExecuteService apiExecuteService;
    @Resource
    private TestPlanService testPlanService;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;
    @Resource
    private ApiCaseResultService apiCaseResultService;

    public List<TestPlanApiCaseDTO> list(ApiTestCaseRequest request) {
        request.setProjectId(null);
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        List<TestPlanApiCaseDTO> apiTestCases = extTestPlanApiCaseMapper.list(request);
        ServiceUtils.buildVersionInfo(apiTestCases);
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        buildUserInfo(apiTestCases);
        return apiTestCases;
    }

    public void buildUserInfo(List<? extends TestPlanApiCaseDTO> apiTestCases) {
        List<String> userIds = new ArrayList();
        userIds.addAll(apiTestCases.stream().map(TestPlanApiCaseDTO::getCreateUser).collect(Collectors.toList()));
        userIds.addAll(apiTestCases.stream().map(TestPlanApiCaseDTO::getUpdateUser).collect(Collectors.toList()));
        userIds.addAll(apiTestCases.stream().map(TestPlanApiCaseDTO::getUserId).collect(Collectors.toList()));
        if (!CollectionUtils.isEmpty(userIds)) {
            Map<String, String> userMap = ServiceUtils.getUserNameMap(userIds);
            apiTestCases.forEach(caseResult -> {
                caseResult.setCreatorName(userMap.get(caseResult.getCreateUser()));
                caseResult.setUpdateName(userMap.get(caseResult.getUpdateUser()));
                caseResult.setPrincipalName(userMap.get(caseResult.getUserId()));
            });
        }
    }

    public List<String> selectIds(ApiTestCaseRequest request) {
        request.setProjectId(null);
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        List<String> idList = extTestPlanApiCaseMapper.selectIds(request);
        return idList;
    }

    public List<String> getExecResultByPlanId(String plan) {
        return extTestPlanApiCaseMapper.getExecResultByPlanId(plan);
    }

    public Pager<List<ApiTestCaseDTO>> relevanceList(int goPage, int pageSize, ApiTestCaseRequest request) {
        if (StringUtils.isNotBlank(request.getPlanId()) && !request.getAllowedRepeatCase()) { // 不允许重复关联
            List<String> ids = apiTestCaseService.selectIdsNotExistsInPlan(request.getProjectId(), request.getPlanId());
            if (CollectionUtils.isEmpty(ids)) {
                return PageUtils.setPageInfo(PageHelper.startPage(goPage, pageSize, true), new ArrayList<>());
            }
            request.setIds(ids);
        }
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        return PageUtils.setPageInfo(page, apiTestCaseService.listSimple(request));
    }

    public int delete(String id) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andIdEqualTo(id);
        return testPlanApiCaseMapper.deleteByExample(example);
    }

    public int deleteByCaseId(String caseId) {
        return this.deleteByCaseIds(Arrays.asList(caseId));
    }

    public int deleteByCaseIds(List<String> caseIds) {
        if (CollectionUtils.isEmpty(caseIds)) {
            return 0;
        }
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andApiCaseIdIn(caseIds);
        return testPlanApiCaseMapper.deleteByExample(example);
    }

    public int deleteByPlanId(String planId) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        return testPlanApiCaseMapper.deleteByExample(example);
    }

    public void deleteByPlanIds(List<String> planIds) {
        if (CollectionUtils.isEmpty(planIds)) {
            return;
        }
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andTestPlanIdIn(planIds);
        testPlanApiCaseMapper.deleteByExample(example);
    }

    public void deleteApiCaseBath(TestPlanApiCaseBatchRequest request) {
        List<String> deleteIds = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            deleteIds = this.selectIds(request.getCondition());
            if (request.getCondition() != null && request.getCondition().getUnSelectIds() != null) {
                deleteIds.removeAll(request.getCondition().getUnSelectIds());
            }
        }

        if (CollectionUtils.isEmpty(deleteIds)) {
            return;
        }
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andIdIn(deleteIds).andTestPlanIdEqualTo(request.getPlanId());
        testPlanApiCaseMapper.deleteByExample(example);
    }

    public List<TestPlanApiCase> getCasesByPlanId(String planId) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        return testPlanApiCaseMapper.selectByExample(example);
    }

    public List<TestPlanApiCase> getCases(String planId) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        return testPlanApiCaseMapper.selectByExample(example);
    }

    public TestPlanApiCase getById(String id) {
        return testPlanApiCaseMapper.selectByPrimaryKey(id);
    }

    public void setExecResult(String id, String status, Long time) {
        TestPlanApiCase apiCase = new TestPlanApiCase();
        apiCase.setId(id);
        apiCase.setStatus(status);
        apiCase.setUpdateTime(time);
        testPlanApiCaseMapper.updateByPrimaryKeySelective(apiCase);
    }

    public void updateByPrimaryKeySelective(TestPlanApiCase apiCase) {
        testPlanApiCaseMapper.updateByPrimaryKeySelective(apiCase);
    }

    public void deleteByRelevanceProjectIds(String planId, List<String> relevanceProjectIds) {
        TestPlanApiCaseBatchRequest request = new TestPlanApiCaseBatchRequest();
        request.setPlanId(planId);
        request.setIds(extTestPlanApiCaseMapper.getNotRelevanceCaseIds(planId, relevanceProjectIds));
        deleteApiCaseBath(request);
    }

    public void batchUpdateEnv(TestPlanApiCaseBatchRequest request) {
        // 批量修改用例环境
        Map<String, String> rows = request.getSelectRows();
        Set<String> ids = rows.keySet();
        request.setIds(new ArrayList<>(ids));
        Map<String, String> env = new HashMap<>();
        String environmentType = request.getEnvironmentType();
        String environmentGroupId = request.getEnvironmentGroupId();
        if (StringUtils.equals(environmentType, EnvironmentType.JSON.name())) {
            env = request.getProjectEnvMap();
        } else if (StringUtils.equals(environmentType, EnvironmentType.GROUP.name()) && StringUtils.isNotBlank(environmentGroupId)) {
            env = environmentGroupProjectService.getEnvMap(environmentGroupId);
        }
        if (env != null && !env.isEmpty()) {
            Map<String, String> finalEnv = env;
            ids.forEach(id -> {
                TestPlanApiCase apiCase = new TestPlanApiCase();
                apiCase.setId(id);
                apiCase.setEnvironmentId(finalEnv.get(rows.get(id)));
                testPlanApiCaseMapper.updateByPrimaryKeySelective(apiCase);
            });
        }
    }

    public String getState(String id) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andApiCaseIdEqualTo(id);
        return testPlanApiCaseMapper.selectByExample(example).get(0).getStatus();

    }

    public List<TestPlanApiCaseDTO> selectAllTableRows(TestPlanApiCaseBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            ids = this.selectIds(request.getCondition());
            if (request.getCondition() != null && request.getCondition().getUnSelectIds() != null) {
                ids.removeAll(request.getCondition().getUnSelectIds());
            }
        }
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        ApiTestCaseRequest selectReq = new ApiTestCaseRequest();
        selectReq.setIds(ids);
        List<TestPlanApiCaseDTO> returnList = extTestPlanApiCaseMapper.list(selectReq);
        ServiceUtils.buildVersionInfo(returnList);
        return returnList;
    }

    public String getLogDetails(String id) {
        TestPlanApiCase testPlanApiCase = testPlanApiCaseMapper.selectByPrimaryKey(id);
        if (testPlanApiCase != null) {
            ApiTestCaseWithBLOBs testCase = apiTestCaseService.get(testPlanApiCase.getApiCaseId());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), testCase.getProjectId(), testCase.getName(), testPlanApiCase.getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andIdIn(ids);
        List<TestPlanApiCase> nodes = testPlanApiCaseMapper.selectByExample(example);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(nodes)) {
            ApiTestCaseExample testCaseExample = new ApiTestCaseExample();
            testCaseExample.createCriteria().andIdIn(nodes.stream().map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList()));
            List<ApiTestCase> testCases = apiTestCaseMapper.selectByExample(testCaseExample);
            if (CollectionUtils.isNotEmpty(testCases)) {
                List<String> names = testCases.stream().map(ApiTestCase::getName).collect(Collectors.toList());
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), testCases.get(0).getProjectId(), String.join(",", names), nodes.get(0).getCreateUser(), new LinkedList<>());
                return JSON.toJSONString(details);
            }
        }
        return null;
    }

    /**
     * 测试执行
     *
     * @param request
     * @return
     */
    public List<MsExecResponseDTO> run(BatchRunDefinitionRequest request) {
        if (request.getConfig() != null) {
            jMeterService.verifyPool(request.getProjectId(), request.getConfig());
            return testPlanApiCaseExecuteService.run(request);
        }
        return null;
    }

    public void setApiCaseEnv(List<TestPlanApiCase> testPlanApiCases, List<String> planIds, Map<String, String> map) {
        if (CollectionUtils.isEmpty(planIds) || (map != null && map.isEmpty())) {
            return;
        }

        if (CollectionUtils.isEmpty(testPlanApiCases)) {
            TestPlanApiCaseExample caseExample = new TestPlanApiCaseExample();
            caseExample.createCriteria().andIdIn(planIds);
            testPlanApiCases = testPlanApiCaseMapper.selectByExample(caseExample);
        }
        List<String> apiCaseIds = testPlanApiCases.stream().map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(apiCaseIds)) {
            return;
        }

        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(apiCaseIds);
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        Map<String, String> projectCaseIdMap = new HashMap<>(16);
        apiTestCases.forEach(c -> projectCaseIdMap.put(c.getId(), c.getProjectId()));

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanApiCaseMapper mapper = sqlSession.getMapper(TestPlanApiCaseMapper.class);

        testPlanApiCases.forEach(testPlanApiCase -> {
            String caseId = testPlanApiCase.getApiCaseId();
            String projectId = projectCaseIdMap.get(caseId);
            String envId = map.get(projectId);
            if (StringUtils.isNotBlank(envId) && !StringUtils.equals(testPlanApiCase.getEnvironmentId(), envId)) {
                testPlanApiCase.setEnvironmentId(envId);
                mapper.updateByPrimaryKey(testPlanApiCase);
            }
        });

        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public RunModeConfigDTO setApiCaseEnv(RunModeConfigDTO runModeConfig, String planId) {
        TestPlanApiCaseExample caseExample = new TestPlanApiCaseExample();
        caseExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMapper.selectByExample(caseExample);
        List<String> planApiCaseIds = testPlanApiCases.stream().map(TestPlanApiCase::getId).collect(Collectors.toList());
        Map<String, String> envMap = runModeConfig.getEnvMap();
        String envType = runModeConfig.getEnvironmentType();
        String environmentGroupId = runModeConfig.getEnvironmentGroupId();
        if (StringUtils.equals(envType, EnvironmentType.GROUP.name()) && StringUtils.isNotBlank(environmentGroupId)) {
            envMap = environmentGroupProjectService.getEnvMap(environmentGroupId);
        }
        setApiCaseEnv(testPlanApiCases, planApiCaseIds, envMap);
        return runModeConfig;
    }

    public Boolean hasFailCase(String planId, List<String> apiCaseIds) {
        if (CollectionUtils.isEmpty(apiCaseIds)) {
            return false;
        }
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andTestPlanIdEqualTo(planId).andApiCaseIdIn(apiCaseIds).andStatusEqualTo(ApiReportStatus.ERROR.name());
        return testPlanApiCaseMapper.countByExample(example) > 0 ? true : false;
    }

    public ApiTestCaseWithBLOBs getApiTestCaseById(String testPlanApiCaseId) {
        return extTestPlanApiCaseMapper.getApiTestCaseById(testPlanApiCaseId);
    }


    public List<TestPlanApiDTO> getFailureCases(String planId) {
        List<TestPlanApiDTO> apiTestCases = extTestPlanApiCaseMapper.getFailureList(planId, ApiReportStatus.ERROR.name());
        return buildCases(apiTestCases);
    }

    public List<TestPlanApiDTO> getAllCases(String planId) {
        List<TestPlanApiDTO> apiTestCases = extTestPlanApiCaseMapper.getFailureList(planId, null);
        return buildCases(apiTestCases);
    }

    public void buildApiResponse(List<TestPlanApiDTO> cases) {
        if (!org.apache.commons.collections.CollectionUtils.isEmpty(cases)) {
            List<String> reportIds = new ArrayList<>();
            for (TestPlanApiDTO apiCase : cases) {
                if (StringUtils.isEmpty(apiCase.getReportId())) {
                    ApiDefinitionExecResultWithBLOBs result = extApiDefinitionExecResultMapper.selectPlanApiMaxResultByTestIdAndType(apiCase.getId(), "API_PLAN");
                    if (result != null && StringUtils.isNotBlank(result.getContent())) {
                        apiCase.setReportId(result.getId());
                        String contentStr = result.getContent();
                        try {
                            Map content = JSON.parseMap(contentStr);
                            if (StringUtils.isNotEmpty(contentStr)) {
                                ApiReportEnvConfigDTO envConfig = apiDefinitionService.getEnvNameByEnvConfig(result.getProjectId(), result.getEnvConfig());
                                if (envConfig != null) {
                                    content.put("envName", envConfig.getEnvName());
                                    content.put("poolName", envConfig.getResourcePoolName());
                                }
                            }
                            contentStr = JSON.toJSONString(content);
                            apiCase.setResponse(contentStr);
                        } catch (Exception e) {
                            LogUtil.error("解析content失败!", e);
                        }
                    }
                } else {
                    reportIds.add(apiCase.getReportId());
                }
            }
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(reportIds)) {
                ApiDefinitionExecResultExample example = new ApiDefinitionExecResultExample();
                example.createCriteria().andIdIn(reportIds);
                List<ApiDefinitionExecResultWithBLOBs> results = apiDefinitionExecResultMapper.selectByExampleWithBLOBs(example);
                // 格式化数据结果
                Map<String, ApiDefinitionExecResultWithBLOBs> resultMap = results.stream().collect(Collectors.toMap(ApiDefinitionExecResult::getId, item -> item, (k, v) -> k));
                cases.forEach(item -> {
                    if (resultMap.get(item.getReportId()) != null &&
                            StringUtils.isNotBlank(resultMap.get(item.getReportId()).getContent())) {
                        ApiDefinitionExecResultWithBLOBs execResult = resultMap.get(item.getReportId());
                        Map responseObj = new LinkedHashMap();
                        try {
                            responseObj = JSON.parseMap(execResult.getContent());
                        } catch (Exception e) {
                            LogUtil.error("转换content失败!", e);
                        }
                        if (StringUtils.isNotEmpty(execResult.getEnvConfig())) {
                            ApiReportEnvConfigDTO envConfig = apiDefinitionService.getEnvNameByEnvConfig(execResult.getProjectId(), execResult.getEnvConfig());
                            if (envConfig != null) {
                                responseObj.put("envName", envConfig.getEnvName());
                                responseObj.put("poolName", envConfig.getResourcePoolName());
                            }
                        }
                        /*
                         * 之前这里的写法是responseObj.toString()。
                         * 猜测是fastjson转换之后，只是单纯的把JSONObject改成了map。所以这里放进去的不是json格式的数据
                         */
                        item.setResponse(JSON.toJSONString(responseObj));
                    }
                });
            }
        }
    }

    public List<TestPlanApiDTO> buildCases(List<TestPlanApiDTO> apiTestCases) {
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        buildPrincipal(apiTestCases);
        buildUserInfo(apiTestCases);
        return apiTestCases;
    }

    /**
     * 从接口定义查询责任人
     * 如果之后接口用例有独立的责任人，则不从接口定义查
     *
     * @param apiTestCases
     */
    private void buildPrincipal(List<TestPlanApiDTO> apiTestCases) {
        List<String> apiIds = apiTestCases.stream()
                .map(TestPlanApiDTO::getApiDefinitionId)
                .collect(Collectors.toList());

        Map<String, String> userIdMap = apiDefinitionService.selectByIds(apiIds)
                .stream()
                .collect(Collectors.toMap(ApiDefinition::getId, ApiDefinition::getUserId));
        apiTestCases.forEach(item -> item.setUserId(userIdMap.get(item.getApiDefinitionId())));
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(TestPlanApiCase.class, TestPlanApiCaseMapper.class, extTestPlanApiCaseMapper::selectPlanIds, extTestPlanApiCaseMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, TestPlanApiCase.class, testPlanApiCaseMapper::selectByPrimaryKey, extTestPlanApiCaseMapper::getPreOrder, extTestPlanApiCaseMapper::getLastOrder, testPlanApiCaseMapper::updateByPrimaryKeySelective);
    }

    public List<TestPlanApiDTO> getErrorReportCases(String planId) {
        List<TestPlanApiDTO> apiTestCases = extTestPlanApiCaseMapper.getFailureList(planId, ApiReportStatus.FAKE_ERROR.name());
        return buildCases(apiTestCases);
    }

    public List<TestPlanApiDTO> getUnExecuteCases(String planId) {
        List<TestPlanApiDTO> apiTestCases = extTestPlanApiCaseMapper.getFailureList(planId, ApiReportStatus.PENDING.name());
        return buildCases(apiTestCases);
    }

    public List<Map> selectStatusForPlanReport(String planId) {
        return extTestPlanApiCaseMapper.selectForPlanReport(planId);
    }

    public String selectProjectId(String id) {
        return extTestPlanApiCaseMapper.selectProjectId(id);
    }

    public AutomationsRunInfoDTO getPlanProjectEnvMap(List<String> resourceIds) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        List<String> resourcePoolIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(resourceIds)) {
            List<String> reportIdList = new ArrayList<>();
            List<ApiDefinitionExecResultWithBLOBs> execResults = apiDefinitionExecResultService.selectByResourceIdsAndMaxCreateTime(resourceIds);
            Map<String, List<String>> projectConfigMap = new HashMap<>();
            execResults.forEach(item -> {
                String envConf = item.getEnvConfig();
                String projectId = item.getProjectId();
                reportIdList.add(item.getId());
                if (projectConfigMap.containsKey(projectId)) {
                    projectConfigMap.get(projectId).add(envConf);
                } else {
                    projectConfigMap.put(projectId, new ArrayList<>() {{
                        this.add(envConf);
                    }});
                }
            });
            result = apiDefinitionService.getProjectEnvNameByEnvConfig(projectConfigMap);
            if (CollectionUtils.isNotEmpty(reportIdList)) {
                resourcePoolIds = extTestPlanApiCaseMapper.selectResourcePoolIdByReportIds(reportIdList);
            }
        }
        AutomationsRunInfoDTO returnDTO = new AutomationsRunInfoDTO();
        returnDTO.setProjectEnvMap(result);
        returnDTO.setResourcePools(resourcePoolIds);
        return returnDTO;
    }

    public void setProjectEnvMap(Map<String, List<String>> result, Map<String, List<String>> projectEnvMap) {
        if (MapUtils.isNotEmpty(projectEnvMap)) {
            for (Map.Entry<String, List<String>> entry : projectEnvMap.entrySet()) {
                String projectName = entry.getKey();
                List<String> envNameList = entry.getValue();
                if (result.containsKey(projectName)) {
                    envNameList.forEach(envName -> {
                        if (!result.get(projectName).contains(envName)) {
                            result.get(projectName).add(envName);
                        }
                    });
                } else {
                    result.put(projectName, envNameList);
                }
            }
        }
    }

    public void relevanceByCase(ApiCaseRelevanceRequest request) {
        List<String> ids = request.getSelectIds();
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        Collections.reverse(ids);
        relevance(ids, request);
    }

    public void relevance(List<String> relevanceIds, ApiCaseRelevanceRequest request) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);

        ExtTestPlanApiCaseMapper batchMapper = sqlSession.getMapper(ExtTestPlanApiCaseMapper.class);
        TestPlanApiCaseMapper batchBaseMapper = sqlSession.getMapper(TestPlanApiCaseMapper.class);
        Long nextOrder = ServiceUtils.getNextOrder(request.getPlanId(), extTestPlanApiCaseMapper::getLastOrder);

        for (String id : relevanceIds) {
            TestPlanApiCase testPlanApiCase = new TestPlanApiCase();
            testPlanApiCase.setId(UUID.randomUUID().toString());
            testPlanApiCase.setCreateUser(SessionUtils.getUserId());
            testPlanApiCase.setApiCaseId(id);
            testPlanApiCase.setTestPlanId(request.getPlanId());
            testPlanApiCase.setEnvironmentId(request.getEnvironmentId());
            testPlanApiCase.setCreateTime(System.currentTimeMillis());
            testPlanApiCase.setUpdateTime(System.currentTimeMillis());
            testPlanApiCase.setOrder(nextOrder);
            nextOrder += ServiceUtils.ORDER_STEP;
            if (request.getAllowedRepeatCase()) {
                batchBaseMapper.insert(testPlanApiCase);
            } else {
                batchMapper.insertIfNotExists(testPlanApiCase);
            }
        }

        testPlanService.statusReset(request.getPlanId());
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public void relevanceByTestIds(List<String> ids, String planId) {
        TestPlanApiCase t = new TestPlanApiCase();
        Long nextApiOrder = ServiceUtils.getNextOrder(planId, extTestPlanApiCaseMapper::getLastOrder);
        for (String id : ids) {
            ApiTestCaseWithBLOBs apiTest = apiTestCaseMapper.selectByPrimaryKey(id);
            if (null != apiTest) {
                t.setId(UUID.randomUUID().toString());
                t.setTestPlanId(planId);
                t.setApiCaseId(id);
                ApiTestEnvironment apiCaseEnvironment = apiTestCaseService.getApiCaseEnvironment(id);
                if (apiCaseEnvironment != null && StringUtils.isNotBlank(apiCaseEnvironment.getId())) {
                    t.setEnvironmentId(apiCaseEnvironment.getId());
                }
                t.setCreateTime(System.currentTimeMillis());
                t.setUpdateTime(System.currentTimeMillis());
                t.setOrder(nextApiOrder);
                nextApiOrder += 5000;
                TestPlanApiCaseExample example = new TestPlanApiCaseExample();
                example.createCriteria().andTestPlanIdEqualTo(planId).andApiCaseIdEqualTo(t.getApiCaseId());
                if (testPlanApiCaseMapper.countByExample(example) <= 0) {
                    testPlanApiCaseMapper.insert(t);
                }
            }
        }
    }

    public List<String> getStatusByTestPlanId(String planId) {
        return extTestPlanApiCaseMapper.getStatusByTestPlanId(planId);
    }

    public void copyPlan(String sourcePlanId, String targetPlanId) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            TestPlanApiCaseExample testPlanApiCaseExample = new TestPlanApiCaseExample();
            testPlanApiCaseExample.createCriteria().andTestPlanIdEqualTo(sourcePlanId);
            List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMapper.selectByExample(testPlanApiCaseExample);
            TestPlanApiCaseMapper apiCaseMapper = sqlSession.getMapper(TestPlanApiCaseMapper.class);
            if (!CollectionUtils.isEmpty(testPlanApiCases)) {
                for (TestPlanApiCase apiCase : testPlanApiCases) {
                    TestPlanApiCase api = new TestPlanApiCase();
                    api.setId(UUID.randomUUID().toString());
                    api.setTestPlanId(targetPlanId);
                    api.setApiCaseId(apiCase.getApiCaseId());
                    api.setEnvironmentId(apiCase.getEnvironmentId());
                    api.setCreateTime(System.currentTimeMillis());
                    api.setUpdateTime(System.currentTimeMillis());
                    api.setCreateUser(SessionUtils.getUserId());
                    api.setOrder(apiCase.getOrder());
                    apiCaseMapper.insert(api);
                }
            }
            sqlSession.flushStatements();
            if (sqlSession != null && sqlSessionFactory != null) {
                SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
            }
        }
    }

    public boolean haveExecCase(String planId) {
        List<TestPlanApiCase> testPlanApiCases = extTestPlanApiCaseMapper.selectPlanByIdsAndStatusIsNotTrash(Arrays.asList(planId));
        return CollectionUtils.isNotEmpty(testPlanApiCases);
    }

    public Map<String, List<String>> getApiCaseEnv(String planId) {
        TestPlanApiCaseExample caseExample = new TestPlanApiCaseExample();
        caseExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMapper.selectByExample(caseExample);
        List<String> apiCaseIds = testPlanApiCases.stream().map(TestPlanApiCase::getId).collect(Collectors.toList());
        return getApiCaseEnv(apiCaseIds);
    }

    public Map<String, List<String>> getApiCaseEnv(List<String> planApiCaseIds) {
        Map<String, List<String>> envMap = new HashMap<>();
        if (org.apache.commons.collections.CollectionUtils.isEmpty(planApiCaseIds)) {
            return envMap;
        }

        TestPlanApiCaseExample caseExample = new TestPlanApiCaseExample();
        caseExample.createCriteria().andIdIn(planApiCaseIds);
        List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMapper.selectByExample(caseExample);
        List<String> apiCaseIds = testPlanApiCases.stream().map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList());
        if (org.apache.commons.collections.CollectionUtils.isEmpty(apiCaseIds)) {
            return envMap;
        }

        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(apiCaseIds);
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        Map<String, String> projectCaseIdMap = new HashMap<>(16);
        apiTestCases.forEach(c -> projectCaseIdMap.put(c.getId(), c.getProjectId()));

        testPlanApiCases.forEach(testPlanApiCase -> {
            String caseId = testPlanApiCase.getApiCaseId();
            String envId = testPlanApiCase.getEnvironmentId();
            String projectId = projectCaseIdMap.get(caseId);
            if (StringUtils.isNotBlank(projectId) && StringUtils.isNotBlank(envId)) {
                if (envMap.containsKey(projectId)) {
                    List<String> list = envMap.get(projectId);
                    if (!list.contains(envId)) {
                        list.add(envId);
                    }
                } else {
                    List<String> envs = new ArrayList<>();
                    envs.add(envId);
                    envMap.put(projectId, envs);
                }
            }

        });
        return envMap;
    }

    public Boolean isExecuting(String planId) {
        List<TestPlanApiCaseInfoDTO> testPlanApiCaseList = extTestPlanApiCaseMapper.selectLegalDataByTestPlanId(planId);
        return !testPlanApiCaseList.stream().map(TestPlanApiCaseInfoDTO::getApiCaseId).collect(Collectors.toList()).isEmpty();
    }

    public List<TestPlanApiDTO> getFailureListByIds(Set<String> planApiCaseIds) {
        return buildCases(extTestPlanApiCaseMapper.getFailureListByIds(planApiCaseIds, null));
    }

    public List<ApiModuleDTO> getNodeByPlanId(List<String> projectIds, String planId, String protocol) {
        List<ApiModuleDTO> list = new ArrayList<>();
        projectIds.forEach(id -> {
            Project project = baseProjectService.getProjectById(id);
            String name = project.getName();
            List<ApiModuleDTO> nodeList = getNodeDTO(id, planId, protocol);
            ApiModuleDTO apiModuleDTO = new ApiModuleDTO();
            apiModuleDTO.setId(project.getId());
            apiModuleDTO.setName(name);
            apiModuleDTO.setLabel(name);
            apiModuleDTO.setChildren(nodeList);
            if (!org.springframework.util.CollectionUtils.isEmpty(nodeList)) {
                list.add(apiModuleDTO);
            }
        });
        return list;
    }

    private List<ApiModuleDTO> getNodeDTO(String projectId, String planId, String protocol) {
        List<TestPlanApiCase> apiCases = getCasesByPlanId(planId);
        if (apiCases.isEmpty()) {
            return null;
        }
        List<ApiModuleDTO> testCaseNodes = apiModuleService.getApiModulesByProjectAndPro(projectId, protocol);

        List<String> caseIds = apiCases.stream().map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList());

        List<String> definitionIds = apiTestCaseService.selectCasesBydIds(caseIds).stream().filter(apiTestCase -> apiTestCase.getStatus() == null || !CommonConstants.TRASH_STATUS.equals(apiTestCase.getStatus())).map(ApiTestCase::getApiDefinitionId).collect(Collectors.toList());

        List<String> dataNodeIds = apiDefinitionService.selectApiDefinitionBydIds(definitionIds).stream().filter(apiDefinition -> apiDefinition.getStatus() == null || !CommonConstants.TRASH_STATUS.equals(apiDefinition.getStatus())).map(ApiDefinition::getModuleId).collect(Collectors.toList());

        List<ApiModuleDTO> nodeTrees = apiModuleService.getNodeTrees(testCaseNodes);

        Iterator<ApiModuleDTO> iterator = nodeTrees.iterator();
        while (iterator.hasNext()) {
            ApiModuleDTO rootNode = iterator.next();
            if (apiModuleService.pruningTree(rootNode, dataNodeIds)) {
                iterator.remove();
            }
        }
        return nodeTrees;
    }

    public void run(String testId, String reportId) {
        TestPlanApiCase testPlanApiCase = testPlanApiCaseMapper.selectByPrimaryKey(testId);
        if (testPlanApiCase == null) {
            MSException.throwException("用例关系已经被删除");
        }
        ApiTestCaseWithBLOBs apiCase = apiTestCaseMapper.selectByPrimaryKey(testPlanApiCase.getApiCaseId());
        if (apiCase == null) {
            MSException.throwException("用例已经被删除");
        }

        String reportName = apiCase.getName();
        ApiDefinitionExecResultWithBLOBs result = ApiDefinitionExecResultUtil.add(testId, ApiReportStatus.RUNNING.name(), reportId, Objects.requireNonNull(SessionUtils.getUser()).getId());
        result.setName(reportName);
        result.setProjectId(apiCase.getProjectId());
        result.setTriggerMode(TriggerMode.MANUAL.name());
        RunModeConfigDTO runModeConfigDTO = new RunModeConfigDTO();
        jMeterService.verifyPool(result.getProjectId(), runModeConfigDTO);
        if (StringUtils.isNotEmpty(testPlanApiCase.getEnvironmentId())) {
            runModeConfigDTO.setEnvMap(new HashMap<>() {{
                this.put(result.getProjectId(), testPlanApiCase.getEnvironmentId());
            }});
            runModeConfigDTO.setResourcePoolId(runModeConfigDTO.getResourcePoolId());
            result.setEnvConfig(JSON.toJSONString(runModeConfigDTO));
        }
        result.setActuator(runModeConfigDTO.getResourcePoolId());
        apiCaseResultService.batchSave(result);
        apiCase.setId(testId);

        RunCaseRequest request = new RunCaseRequest();
        request.setRunMode(ApiRunMode.API_PLAN.name());
        request.setCaseId(testId);
        request.setReport(result);
        request.setEnvironmentId(testPlanApiCase.getEnvironmentId());
        request.setBloBs(apiCase);
        request.setReportId(reportId);
        request.setTestPlanId(testPlanApiCase.getTestPlanId());
        Map<String, Object> extendedParameters = new HashMap<>();
        extendedParameters.put(ExtendedParameter.SYNC_STATUS, true);
        apiExecuteService.exec(request, extendedParameters);
    }

    public List<TestPlanApiCase> selectByIds(ArrayList<String> list) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andIdIn(list);
        return testPlanApiCaseMapper.selectByExample(example);
    }

    //获取case和测试计划引用关系
    public List<TestPlanDTO> getReference(QueryReferenceRequest request) {
        if (CollectionUtils.isEmpty(request.getOrders())) {
            OrderRequest req = new OrderRequest();
            req.setName("name");
            req.setType("asc");
            request.setOrders(new ArrayList<>() {{
                this.add(req);
            }});
        }
        if (StringUtils.equals(request.getScenarioType(), ReportTypeConstants.API.name())) {
            request.setApiId(request.getId());
        } else {
            request.setScenarioId(request.getId());
        }
        return extTestPlanApiCaseMapper.selectTestPlanByRelevancy(request);
    }

    public List<ApiDefinitionExecResultWithBLOBs> selectExtForPlanReport(String planId) {
        ApiDefinitionExecResultExample example = new ApiDefinitionExecResultExample();
        example.createCriteria().andRelevanceTestPlanReportIdEqualTo(planId);
        List<ApiDefinitionExecResultWithBLOBs> results = apiDefinitionExecResultMapper.selectByExampleWithBLOBs(example);
        return results;
    }

    public List<ApiScenarioReportWithBLOBs> selectExtForPlanScenarioReport(String planId) {
        ApiScenarioReportExample example = new ApiScenarioReportExample();
        example.createCriteria().andRelevanceTestPlanReportIdEqualTo(planId);
        List<ApiScenarioReportWithBLOBs> results = apiScenarioReportMapper.selectByExampleWithBLOBs(example);
        return results;
    }


    public List<String> getApiCaseProjectIds(String planId) {
        TestPlanApiCaseExample caseExample = new TestPlanApiCaseExample();
        caseExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanApiCase> testPlanApiCases = testPlanApiCaseMapper.selectByExample(caseExample);
        List<String> apiCaseIds = testPlanApiCases.stream().map(TestPlanApiCase::getApiCaseId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(apiCaseIds)) {
            return new ArrayList<>();
        }
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andIdIn(apiCaseIds);
        List<ApiTestCase> apiTestCases = apiTestCaseMapper.selectByExample(example);
        return apiTestCases.stream().map(ApiTestCase::getProjectId).distinct().collect(Collectors.toList());
    }
}
