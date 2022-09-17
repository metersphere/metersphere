package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.dto.EnvironmentType;
import io.metersphere.api.dto.automation.TestPlanFailureApiDTO;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import io.metersphere.api.dto.definition.ApiTestCaseRequest;
import io.metersphere.api.dto.definition.BatchRunDefinitionRequest;
import io.metersphere.api.dto.definition.TestPlanApiCaseDTO;
import io.metersphere.api.exec.api.ApiCaseExecuteService;
import io.metersphere.api.exec.scenario.ApiScenarioEnvService;
import io.metersphere.api.service.ApiDefinitionExecResultService;
import io.metersphere.api.service.ApiDefinitionService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiTestCaseMapper;
import io.metersphere.base.mapper.TestPlanApiCaseMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanApiCaseMapper;
import io.metersphere.commons.constants.ExecuteResult;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.dto.MsExecResponseDTO;
import io.metersphere.dto.RunModeConfigDTO;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.service.EnvironmentGroupProjectService;
import io.metersphere.track.dto.PlanReportCaseDTO;
import io.metersphere.track.dto.TestCaseReportStatusResultDTO;
import io.metersphere.track.dto.TestPlanApiResultReportDTO;
import io.metersphere.track.dto.TestPlanSimpleReportDTO;
import io.metersphere.track.request.testcase.TestPlanApiCaseBatchRequest;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiCaseService {

    @Resource
    TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    ApiTestCaseService apiTestCaseService;
    @Resource
    ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;
    @Lazy
    @Resource
    ApiDefinitionExecResultService apiDefinitionExecResultService;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiCaseExecuteService testPlanApiCaseExecuteService;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    @Resource
    @Lazy
    private TestPlanService testPlanService;
    @Resource
    private EnvironmentGroupProjectService environmentGroupProjectService;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ApiScenarioEnvService apiScenarioEnvService;


    public TestPlanApiCase getInfo(String caseId, String testPlanId) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andApiCaseIdEqualTo(caseId).andTestPlanIdEqualTo(testPlanId);
        return testPlanApiCaseMapper.selectByExample(example).get(0);
    }

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
        userIds.addAll(apiTestCases.stream().map(TestPlanApiCaseDTO::getCreateUserId).collect(Collectors.toList()));
        userIds.addAll(apiTestCases.stream().map(TestPlanApiCaseDTO::getUpdateUser).collect(Collectors.toList()));
        userIds.addAll(apiTestCases.stream().map(TestPlanApiCaseDTO::getUserId).collect(Collectors.toList()));
        if (!org.apache.commons.collections.CollectionUtils.isEmpty(userIds)) {
            Map<String, String> userMap = ServiceUtils.getUserNameMap(userIds);
            apiTestCases.forEach(caseResult -> {
                caseResult.setCreatorName(userMap.get(caseResult.getCreateUserId()));
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
        if (StringUtils.isNotBlank(request.getPlanId()) && !testPlanService.isAllowedRepeatCase(request.getPlanId())) { // 不允许重复关联
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
        example.createCriteria()
                .andIdEqualTo(id);
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
        example.createCriteria()
                .andApiCaseIdIn(caseIds);
        return testPlanApiCaseMapper.deleteByExample(example);
    }

    public int deleteByPlanId(String planId) {
        List<String> ids = extTestPlanApiCaseMapper.getIdsByPlanId(planId);
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(planId);
        return testPlanApiCaseMapper.deleteByExample(example);
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
        example.createCriteria()
                .andIdIn(deleteIds)
                .andTestPlanIdEqualTo(request.getPlanId());
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
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(testPlanApiCase.getTestPlanId());
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
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(testCases)) {
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
            RunModeConfigDTO config = request.getConfig();
            if (config != null) {
                String envType = config.getEnvironmentType();
                String envGroupId = config.getEnvironmentGroupId();
                Map<String, String> envMap = config.getEnvMap();
                if ((StringUtils.equals(envType, EnvironmentType.JSON.toString()) && envMap != null && !envMap.isEmpty())) {
                    setApiCaseEnv(null, request.getPlanIds(), envMap);
                } else if ((StringUtils.equals(envType, EnvironmentType.GROUP.toString()) && StringUtils.isNotBlank(envGroupId))) {
                    Map<String, String> map = environmentGroupProjectService.getEnvMap(envGroupId);
                    setApiCaseEnv(null, request.getPlanIds(), map);
                }
            }
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

    public Boolean hasFailCase(String planId, List<String> apiCaseIds) {
        if (CollectionUtils.isEmpty(apiCaseIds)) {
            return false;
        }
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(planId)
                .andApiCaseIdIn(apiCaseIds)
                .andStatusEqualTo("error");
        return testPlanApiCaseMapper.countByExample(example) > 0 ? true : false;
    }

    public ApiTestCaseWithBLOBs getApiTestCaseById(String testPlanApiCaseId) {
        return extTestPlanApiCaseMapper.getApiTestCaseById(testPlanApiCaseId);
    }

    /**
     * 计算测试计划中接口用例的相关数据
     *
     * @param planId
     * @param report
     * @return 接口用例的最新执行报告
     */
    public void calculatePlanReport(String planId, TestPlanSimpleReportDTO report) {
        List<PlanReportCaseDTO> planReportCaseDTOS = extTestPlanApiCaseMapper.selectForPlanReport(planId);
        //计算测试计划中接口用例的相关数据
        calculatePlanReport(report, planReportCaseDTOS);
        //记录接口用例的运行环境信息
        List<String> idList = planReportCaseDTOS.stream().map(PlanReportCaseDTO::getId).collect(Collectors.toList());
        this.initProjectEnvironment(report, idList, "ApiCase");
    }

    /**
     * 初始化项目环境信息
     *
     * @param report
     * @param resourceIds  资源ID
     * @param resourceType 资源类型 ApiCase/Scenario
     */
    public void initProjectEnvironment(TestPlanSimpleReportDTO report, List<String> resourceIds, String resourceType) {
        if (!CollectionUtils.isEmpty(resourceIds)) {
            if (StringUtils.equalsIgnoreCase("ApiCase", resourceType)) {
                List<ApiDefinitionExecResultWithBLOBs> execResults = apiDefinitionExecResultService.selectByResourceIdsAndMaxCreateTime(resourceIds);
                execResults.forEach(item -> {
                    String envConf = item.getEnvConfig();
                    String projectId = item.getProjectId();
                    Map<String, List<String>> projectEnvMap = apiDefinitionService.getProjectEnvNameByEnvConfig(projectId, envConf);
                    this.setProjectEnvMap(report, projectEnvMap);
                });
            } else if (StringUtils.equalsIgnoreCase("Scenario", resourceType)) {
                Map<String, List<String>> projectEnvMap = apiScenarioEnvService.selectProjectEnvMapByTestPlanScenarioIds(resourceIds);
                this.setProjectEnvMap(report, projectEnvMap);
            }
        }

    }

    private void setProjectEnvMap(TestPlanSimpleReportDTO report, Map<String, List<String>> projectEnvMap) {
        if (report != null && MapUtils.isNotEmpty(projectEnvMap)) {
            if (report.getProjectEnvMap() == null) {
                report.setProjectEnvMap(new LinkedHashMap<>());
            }
            for (Map.Entry<String, List<String>> entry : projectEnvMap.entrySet()) {
                String projectName = entry.getKey();
                List<String> envNameList = entry.getValue();
                if (report.getProjectEnvMap().containsKey(projectName)) {
                    envNameList.forEach(envName -> {
                        if (!report.getProjectEnvMap().get(projectName).contains(envName)) {
                            report.getProjectEnvMap().get(projectName).add(envName);
                        }
                    });
                } else {
                    report.getProjectEnvMap().put(projectName, envNameList);
                }
            }
        }
    }

    public void calculatePlanReport(List<String> apiReportIds, TestPlanSimpleReportDTO report) {
        List<PlanReportCaseDTO> planReportCaseDTOS = apiDefinitionExecResultService.selectForPlanReport(apiReportIds);
        calculatePlanReport(report, planReportCaseDTOS);
    }

    public void calculatePlanReportByApiCaseList(List<TestPlanFailureApiDTO> apiCaseList, TestPlanSimpleReportDTO report) {
        List<PlanReportCaseDTO> planReportCaseDTOS = new ArrayList<>();
        for (TestPlanFailureApiDTO dto : apiCaseList) {
            PlanReportCaseDTO reportCaseDTO = new PlanReportCaseDTO();
            reportCaseDTO.setId(dto.getId());
            reportCaseDTO.setCaseId(dto.getCaseId());
            reportCaseDTO.setReportId(dto.getReportId());
            reportCaseDTO.setStatus(dto.getExecResult());
            planReportCaseDTOS.add(reportCaseDTO);
        }
        calculatePlanReport(report, planReportCaseDTOS);
    }

    private void calculatePlanReport(TestPlanSimpleReportDTO report, List<PlanReportCaseDTO> planReportCaseDTOS) {
        TestPlanApiResultReportDTO apiResult = report.getApiResult();
        List<TestCaseReportStatusResultDTO> statusResult = new ArrayList<>();
        Map<String, TestCaseReportStatusResultDTO> statusResultMap = new HashMap<>();

        TestPlanUtils.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, "success");

        TestPlanUtils.addToReportCommonStatusResultList(statusResultMap, statusResult);

        apiResult.setApiCaseData(statusResult);
    }

    public List<TestPlanFailureApiDTO> getFailureCases(String planId) {
        List<TestPlanFailureApiDTO> apiTestCases = extTestPlanApiCaseMapper.getFailureList(planId, "error");
        return buildCases(apiTestCases);
    }

    public List<TestPlanFailureApiDTO> getAllCases(String planId) {
        List<TestPlanFailureApiDTO> apiTestCases = extTestPlanApiCaseMapper.getFailureList(planId, null);
        return buildCases(apiTestCases);
    }

    public List<TestPlanFailureApiDTO> buildCases(List<TestPlanFailureApiDTO> apiTestCases) {
        if (CollectionUtils.isEmpty(apiTestCases)) {
            return apiTestCases;
        }
        buildUserInfo(apiTestCases);
        return apiTestCases;
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(TestPlanApiCase.class, TestPlanApiCaseMapper.class,
                extTestPlanApiCaseMapper::selectPlanIds,
                extTestPlanApiCaseMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, TestPlanApiCase.class,
                testPlanApiCaseMapper::selectByPrimaryKey,
                extTestPlanApiCaseMapper::getPreOrder,
                extTestPlanApiCaseMapper::getLastOrder,
                testPlanApiCaseMapper::updateByPrimaryKeySelective);
    }

    public List<TestPlanFailureApiDTO> getByApiExecReportIds(Map<String, String> testPlanApiCaseReportMap, Map<String, TestPlanFailureApiDTO> apiCaseInfoDTOMap) {
        if (testPlanApiCaseReportMap.isEmpty()) {
            return new ArrayList<>();
        }
        String defaultStatus = "error";
        Map<String, String> reportResult = apiDefinitionExecResultService.selectReportResultByReportIds(testPlanApiCaseReportMap.values());
        Map<String, String> savedReportMap = new HashMap<>(testPlanApiCaseReportMap);
        List<TestPlanFailureApiDTO> apiTestCases = new ArrayList<>();
        for (TestPlanFailureApiDTO dto : apiCaseInfoDTOMap.values()) {
            String testPlanApiCaseId = dto.getId();
            String reportId = savedReportMap.get(testPlanApiCaseId);
            savedReportMap.remove(testPlanApiCaseId);
            dto.setReportId(reportId);
            if (StringUtils.isEmpty(reportId)) {
                dto.setExecResult(defaultStatus);
            } else {
                String status = reportResult.get(reportId);
                if (status == null) {
                    status = defaultStatus;
                }
                dto.setExecResult(status);
            }
            apiTestCases.add(dto);
        }
        return buildCases(apiTestCases);
    }

    public List<TestPlanFailureApiDTO> getErrorReportCases(String planId) {
        List<TestPlanFailureApiDTO> apiTestCases = extTestPlanApiCaseMapper.getFailureList(planId, ExecuteResult.ERROR_REPORT_RESULT.toString());
        return buildCases(apiTestCases);
    }

    public List<TestPlanFailureApiDTO> getUnExecuteCases(String planId) {
        List<TestPlanFailureApiDTO> apiTestCases = extTestPlanApiCaseMapper.getFailureList(planId, "unExecute");
        return buildCases(apiTestCases);
    }
}
