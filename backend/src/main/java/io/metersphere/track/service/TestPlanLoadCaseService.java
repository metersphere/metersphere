package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.api.exec.utils.NamedThreadFactory;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.TestPlanLoadCaseMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanLoadCaseMapper;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.constants.TestPlanLoadCaseStatus;
import io.metersphere.commons.constants.TestPlanStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.performance.request.QueryTestPlanRequest;
import io.metersphere.performance.request.RunTestPlanRequest;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.track.dto.*;
import io.metersphere.track.request.testplan.LoadCaseReportBatchRequest;
import io.metersphere.track.request.testplan.LoadCaseReportRequest;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import io.metersphere.track.request.testplan.RunBatchTestPlanRequest;
import io.metersphere.track.service.utils.ParallelExecTask;
import io.metersphere.track.service.utils.SerialExecTask;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanLoadCaseService {
    @Resource
    TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;
    @Resource
    private ExtTestPlanLoadCaseMapper extTestPlanLoadCaseMapper;
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    @Lazy
    private TestPlanService testPlanService;
    @Resource
    private ExtLoadTestMapper extLoadTestMapper;

    public Pager<List<LoadTestDTO>> relevanceList(LoadCaseRequest request, int goPage, int pageSize) {
        List<OrderRequest> orders = ServiceUtils.getDefaultSortOrder(request.getOrders());
        orders.forEach(i -> i.setPrefix("load_test"));
        request.setOrders(orders);
        if (testPlanService.isAllowedRepeatCase(request.getTestPlanId())) {
            request.setRepeatCase(true);
        }
        List<String> ids = extTestPlanLoadCaseMapper.selectIdsNotInPlan(request);
        if (CollectionUtils.isEmpty(ids)) {
            return PageUtils.setPageInfo(PageHelper.startPage(goPage, pageSize, true), new ArrayList <>());
        }
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        QueryTestPlanRequest newRequest = new QueryTestPlanRequest();
        BeanUtils.copyBean(newRequest, request);
        Map filters = new HashMap();
        filters.put("id", ids);
        newRequest.setFilters(filters);
        List<LoadTestDTO> loadTestDTOS = extLoadTestMapper.list(newRequest);
        return PageUtils.setPageInfo(page, loadTestDTOS);
    }

    public List<TestPlanLoadCaseDTO> list(LoadCaseRequest request) {
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        List<TestPlanLoadCaseDTO> testPlanLoadCaseDTOList = extTestPlanLoadCaseMapper.selectTestPlanLoadCaseList(request);
        return testPlanLoadCaseDTOList;
    }

    public List<String> selectTestPlanLoadCaseIds(LoadCaseRequest request) {
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        return extTestPlanLoadCaseMapper.selectTestPlanLoadCaseId(request);
    }

    public void relevanceCase(LoadCaseRequest request) {
        List<String> caseIds = request.getCaseIds();
        String planId = request.getTestPlanId();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);

        TestPlanLoadCaseMapper testPlanLoadCaseMapper = sqlSession.getMapper(TestPlanLoadCaseMapper.class);
        Long nextOrder = ServiceUtils.getNextOrder(request.getTestPlanId(), extTestPlanLoadCaseMapper::getLastOrder);

        // 尽量保持与用例顺序一致
        Collections.reverse(caseIds);

        for (String id : caseIds) {
            TestPlanLoadCaseWithBLOBs t = new TestPlanLoadCaseWithBLOBs();
            t.setId(UUID.randomUUID().toString());
            t.setCreateUser(SessionUtils.getUserId());
            t.setTestPlanId(planId);
            t.setLoadCaseId(id);
            t.setCreateTime(System.currentTimeMillis());
            t.setUpdateTime(System.currentTimeMillis());
            t.setOrder(nextOrder);
            LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(id);
            if (loadTest != null) {
                t.setTestResourcePoolId(loadTest.getTestResourcePoolId());
                t.setLoadConfiguration(loadTest.getLoadConfiguration());
                t.setAdvancedConfiguration(loadTest.getAdvancedConfiguration());
            }
            nextOrder += ServiceUtils.ORDER_STEP;
            testPlanLoadCaseMapper.insert(t);
        }

        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getTestPlanId());
        if (org.apache.commons.lang3.StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Prepare.name())
                || org.apache.commons.lang3.StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Completed.name())) {
            testPlan.setStatus(TestPlanStatus.Underway.name());
            testPlan.setActualStartTime(System.currentTimeMillis());  // 将状态更新为进行中时，开始时间也要更新
            testPlan.setActualEndTime(null);
            testPlanMapper.updateByPrimaryKey(testPlan);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public void delete(String id) {
        TestPlanLoadCaseExample testPlanLoadCaseExample = new TestPlanLoadCaseExample();
        testPlanLoadCaseExample.createCriteria().andIdEqualTo(id);
        testPlanLoadCaseMapper.deleteByExample(testPlanLoadCaseExample);
    }

    public void deleteByTestId(String testId) {
        TestPlanLoadCaseExample testPlanLoadCaseExample = new TestPlanLoadCaseExample();
        testPlanLoadCaseExample.createCriteria().andLoadCaseIdEqualTo(testId);
        testPlanLoadCaseMapper.deleteByExample(testPlanLoadCaseExample);
    }

    public String run(RunTestPlanRequest request) {
        String reportId = performanceTestService.run(request);
        TestPlanLoadCaseWithBLOBs testPlanLoadCase = new TestPlanLoadCaseWithBLOBs();
        testPlanLoadCase.setId(request.getTestPlanLoadId());
        testPlanLoadCase.setLoadReportId(reportId);
        testPlanLoadCaseMapper.updateByPrimaryKeySelective(testPlanLoadCase);
        return reportId;
    }

    public void runBatch(RunBatchTestPlanRequest request) {
        try {
            if (request.getConfig() != null && request.getConfig().getMode().equals(RunModeConstants.SERIAL.toString())) {
                serialRun(request);
            } else {
                ExecutorService executorService = Executors.newFixedThreadPool(request.getRequests().size(), new NamedThreadFactory("TestPlanLoadCaseService"));
                request.getRequests().forEach(item -> {
                    executorService.submit(new ParallelExecTask(performanceTestService, testPlanLoadCaseMapper, item));
                });
            }
        } catch (Exception e) {
            if (StringUtils.isNotEmpty(e.getMessage())) {
                MSException.throwException("测试正在运行, 请等待！");
            } else {
                MSException.throwException("请求参数错误，请刷新后执行！");
            }
        }
    }

    private void serialRun(RunBatchTestPlanRequest request) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(request.getRequests().size(), new NamedThreadFactory("TestPlanLoadCaseService-serial"));
        for (RunTestPlanRequest runTestPlanRequest : request.getRequests()) {
            Future<LoadTestReportWithBLOBs> future = executorService.submit(new SerialExecTask(performanceTestService, testPlanLoadCaseMapper, loadTestReportMapper, runTestPlanRequest));
            LoadTestReportWithBLOBs report = future.get();
            // 如果开启失败结束执行，则判断返回结果状态
            if (request.getConfig().isOnSampleError()) {
                TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
                example.createCriteria().andLoadReportIdEqualTo(report.getId());
                List<TestPlanLoadCase> cases = testPlanLoadCaseMapper.selectByExample(example);
                if (CollectionUtils.isEmpty(cases) || !cases.get(0).getStatus().equals(TestPlanLoadCaseStatus.success.name())) {
                    break;
                }
            }
        }
    }

    public Boolean isExistReport(LoadCaseReportRequest request) {
        String reportId = request.getReportId();
        String testPlanLoadCaseId = request.getTestPlanLoadCaseId();
        LoadTestReportExample example = new LoadTestReportExample();
        example.createCriteria().andIdEqualTo(reportId);
        List<LoadTestReport> loadTestReports = loadTestReportMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(loadTestReports)) {
            TestPlanLoadCaseWithBLOBs testPlanLoadCase = new TestPlanLoadCaseWithBLOBs();
            testPlanLoadCase.setId(testPlanLoadCaseId);
            testPlanLoadCase.setLoadReportId("");
            testPlanLoadCaseMapper.updateByPrimaryKeySelective(testPlanLoadCase);
            return false;
        }
        return true;
    }

    public void deleteByRelevanceProjectIds(String id, List<String> relevanceProjectIds) {
        LoadTestExample loadTestExample = new LoadTestExample();
        loadTestExample.createCriteria().andProjectIdIn(relevanceProjectIds);
        List<LoadTest> loadTests = loadTestMapper.selectByExample(loadTestExample);
        TestPlanLoadCaseExample testPlanLoadCaseExample = new TestPlanLoadCaseExample();
        TestPlanLoadCaseExample.Criteria criteria = testPlanLoadCaseExample.createCriteria().andTestPlanIdEqualTo(id);
        if (!CollectionUtils.isEmpty(loadTests)) {
            List<String> ids = loadTests.stream().map(LoadTest::getId).collect(Collectors.toList());
            criteria.andLoadCaseIdNotIn(ids);
        }
        testPlanLoadCaseMapper.deleteByExample(testPlanLoadCaseExample);
    }

    public void batchDelete(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
        example.createCriteria().andIdIn(ids);
        testPlanLoadCaseMapper.deleteByExample(example);
    }

    public void batchDelete(LoadCaseReportBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            ids = this.selectTestPlanLoadCaseIds(request.getCondition());
            if (request.getCondition().getUnSelectIds() != null) {
                ids.removeAll(request.getCondition().getUnSelectIds());
            }
        }

        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
        example.createCriteria().andIdIn(ids);
        testPlanLoadCaseMapper.deleteByExample(example);
    }

    public void update(TestPlanLoadCaseWithBLOBs testPlanLoadCase) {
        if (!StringUtils.isEmpty(testPlanLoadCase.getId())) {
            testPlanLoadCaseMapper.updateByPrimaryKeySelective(testPlanLoadCase);
        }
    }

    public void updateByApi(TestPlanLoadCase testPlanLoadCase) {
        String testPlanId = testPlanLoadCase.getTestPlanId();
        String loadCaseId = testPlanLoadCase.getLoadCaseId();
        String status = testPlanLoadCase.getStatus();
        extTestPlanLoadCaseMapper.updateCaseStatusByApi(testPlanId, loadCaseId, status);
    }

    public List<String> getStatus(String planId) {
        return extTestPlanLoadCaseMapper.getStatusByTestPlanId(planId);
    }

    public List<TestPlanLoadCaseDTO> selectAllTableRows(LoadCaseReportBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.getCondition() != null && request.getCondition().isSelectAll()) {
            ids = this.selectTestPlanLoadCaseIds(request.getCondition());
            if (request.getCondition().getUnSelectIds() != null) {
                ids.removeAll(request.getCondition().getUnSelectIds());
            }
        }

        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        LoadCaseRequest tableReq = new LoadCaseRequest();
        tableReq.setIds(ids);
        tableReq.setOrders(ServiceUtils.getDefaultSortOrder(tableReq.getOrders()));

        List<TestPlanLoadCaseDTO> list = extTestPlanLoadCaseMapper.selectByIdIn(tableReq);
        return list;
    }

    public String getLogDetails(String id) {
        TestPlanLoadCase bloBs = testPlanLoadCaseMapper.selectByPrimaryKey(id);
        if (bloBs != null) {
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(bloBs.getTestPlanId());
            LoadTest test = loadTestMapper.selectByPrimaryKey(bloBs.getLoadCaseId());
            if (test != null && testPlan != null) {
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), testPlan.getProjectId(), test.getName(), bloBs.getCreateUser(), new LinkedList<>());
                return JSON.toJSONString(details);
            }
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        TestPlanLoadCaseExample caseExample = new TestPlanLoadCaseExample();
        caseExample.createCriteria().andIdIn(ids);
        List<TestPlanLoadCase> cases = testPlanLoadCaseMapper.selectByExample(caseExample);
        if (CollectionUtils.isNotEmpty(cases)) {
            LoadTestExample example = new LoadTestExample();
            example.createCriteria().andIdIn(cases.stream().map(TestPlanLoadCase::getLoadCaseId).collect(Collectors.toList()));
            List<LoadTest> loadTests = loadTestMapper.selectByExample(example);
            List<String> names = loadTests.stream().map(LoadTest::getName).collect(Collectors.toList());
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(cases.get(0).getTestPlanId());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), testPlan.getProjectId(), String.join(",", names), testPlan.getCreator(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids, String planId) {
        TestPlanLoadCaseExample caseExample = new TestPlanLoadCaseExample();
        caseExample.createCriteria().andLoadCaseIdIn(ids).andTestPlanIdEqualTo(planId);
        List<TestPlanLoadCase> cases = testPlanLoadCaseMapper.selectByExample(caseExample);
        if (CollectionUtils.isNotEmpty(cases)) {
            LoadTestExample example = new LoadTestExample();
            example.createCriteria().andIdIn(cases.stream().map(TestPlanLoadCase::getLoadCaseId).collect(Collectors.toList()));
            List<LoadTest> loadTests = loadTestMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(loadTests)) {
                List<String> names = loadTests.stream().map(LoadTest::getName).collect(Collectors.toList());
                TestPlan testPlan = testPlanMapper.selectByPrimaryKey(cases.get(0).getTestPlanId());
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), testPlan.getProjectId(), String.join(",", names), testPlan.getCreator(), new LinkedList<>());
                return JSON.toJSONString(details);
            }
        }
        return null;
    }

    public String getRunLogDetails(List<RunTestPlanRequest> requests) {
        LoadTestExample example = new LoadTestExample();
        example.createCriteria().andIdIn(requests.stream().map(RunTestPlanRequest::getId).collect(Collectors.toList()));
        List<LoadTest> loadTests = loadTestMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(loadTests)) {
            List<String> ids = loadTests.stream().map(LoadTest::getId).collect(Collectors.toList());
            List<String> names = loadTests.stream().map(LoadTest::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), loadTests.get(0).getProjectId(), String.join(",", names), null, new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public Boolean hasFailCase(String planId, List<String> performanceIds) {
        if (CollectionUtils.isEmpty(performanceIds)) {
            return false;
        }
        TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
        example.createCriteria()
                .andTestPlanIdEqualTo(planId)
                .andLoadCaseIdIn(performanceIds)
                .andStatusEqualTo(TestPlanLoadCaseStatus.error.name());
        return testPlanLoadCaseMapper.countByExample(example) > 0 ? true : false;
    }

    public void deleteByPlanId(String planId) {
        if (StringUtils.isBlank(planId)) {
            return;
        }
        TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
        example.createCriteria().andTestPlanIdEqualTo(planId);
        testPlanLoadCaseMapper.deleteByExample(example);
    }

    public void calculatePlanReport(String planId, TestPlanSimpleReportDTO report) {
        List<PlanReportCaseDTO> planReportCaseDTOS = extTestPlanLoadCaseMapper.selectForPlanReport(planId);
        calculatePlanReport(report, planReportCaseDTOS);
    }

    public void calculatePlanReport(List<String> reportIds, TestPlanSimpleReportDTO report) {
        List<PlanReportCaseDTO> planReportCaseDTOS = extLoadTestReportMapper.selectForPlanReport(reportIds);
        // 性能测试的报告状态跟用例的执行状态不一样
        planReportCaseDTOS.forEach(item -> {
            if (item.getStatus().equals(PerformanceTestStatus.Completed.name())) {
                item.setStatus(TestPlanLoadCaseStatus.success.name());
            } else if (item.getStatus().equals(PerformanceTestStatus.Error.name())) {
                item.setStatus(TestPlanLoadCaseStatus.error.name());
            } else {
                item.setStatus(TestPlanLoadCaseStatus.run.name());
            }
        });
        calculatePlanReport(report, planReportCaseDTOS);
    }

    private void calculatePlanReport(TestPlanSimpleReportDTO report, List<PlanReportCaseDTO> planReportCaseDTOS) {
        TestPlanLoadResultReportDTO loadResult = new TestPlanLoadResultReportDTO();
        report.setLoadResult(loadResult);
        List<TestCaseReportStatusResultDTO> statusResult = new ArrayList<>();
        Map<String, TestCaseReportStatusResultDTO> statusResultMap = new HashMap<>();

        TestPlanUtils.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, TestPlanLoadCaseStatus.success.name());
        TestPlanUtils.addToReportCommonStatusResultList(statusResultMap, statusResult);

        loadResult.setCaseData(statusResult);
    }

    public List<TestPlanLoadCaseDTO> getAllCases(String planId) {
        List<TestPlanLoadCaseDTO> cases = extTestPlanLoadCaseMapper.getCases(planId, null);
        return buildCases(cases);
    }

    public List<TestPlanLoadCaseDTO> getAllCases(Collection<String> ids, Collection<String> reportIds) {
        List<TestPlanLoadCaseDTO> cases = extTestPlanLoadCaseMapper.getCasesByIds(ids, reportIds);
        return buildCases(cases);
    }

    public List<TestPlanLoadCaseDTO> getFailureCases(String planId) {
        List<TestPlanLoadCaseDTO> failureCases = extTestPlanLoadCaseMapper.getCases(planId, TestPlanLoadCaseStatus.error.name());
        return buildCases(failureCases);
    }

    public List<TestPlanLoadCaseDTO> buildCases(List<TestPlanLoadCaseDTO> cases) {
//        Map<String, Project> projectMap = ServiceUtils.getProjectMap(
//                failureCases.stream().map(TestPlanCaseDTO::getProjectId).collect(Collectors.toList()));
        Map<String, String> userNameMap = ServiceUtils.getUserNameMap(
                cases.stream().map(TestPlanLoadCaseDTO::getCreateUser).collect(Collectors.toList()));
        cases.forEach(item -> {
//            item.setProjectName(projectMap.get(item.getProjectId()).getName());
            item.setUserName(userNameMap.get(item.getCreateUser()));
        });
        return cases;
    }

    public String getPlanLoadCaseConfig(String loadCaseId) {
        if (StringUtils.isBlank(loadCaseId)) {
            return "";
        }
        TestPlanLoadCaseWithBLOBs testPlanLoadCase = testPlanLoadCaseMapper.selectByPrimaryKey(loadCaseId);
        if (testPlanLoadCase != null) {
            return testPlanLoadCase.getLoadConfiguration();
        }
        return "";
    }


    public String getAdvancedConfiguration(String loadCaseId) {
        if (StringUtils.isBlank(loadCaseId)) {
            return "";
        }
        TestPlanLoadCaseWithBLOBs testPlanLoadCase = testPlanLoadCaseMapper.selectByPrimaryKey(loadCaseId);
        if (testPlanLoadCase != null) {
            return testPlanLoadCase.getAdvancedConfiguration();
        }
        return "";
    }

    public TestPlanLoadCase getTestPlanLoadCase(String loadCaseId) {
        if (StringUtils.isBlank(loadCaseId)) {
            return new TestPlanLoadCase();
        }
        return testPlanLoadCaseMapper.selectByPrimaryKey(loadCaseId);
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(TestPlanLoadCaseWithBLOBs.class, TestPlanLoadCaseMapper.class,
                extTestPlanLoadCaseMapper::selectPlanIds,
                extTestPlanLoadCaseMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, TestPlanLoadCaseWithBLOBs.class,
                testPlanLoadCaseMapper::selectByPrimaryKey,
                extTestPlanLoadCaseMapper::getPreOrder,
                extTestPlanLoadCaseMapper::getLastOrder,
                testPlanLoadCaseMapper::updateByPrimaryKeySelective);
    }

    public void checkStatusByDeleteLoadCaseReportId(String reportId) {
        List<String> updatedId = extTestPlanLoadCaseMapper.selectIdByLoadCaseReportIdAndStatusIsRun(reportId);
        if (CollectionUtils.isNotEmpty(updatedId)) {
            for (String id : updatedId) {
                extTestPlanLoadCaseMapper.updateStatusNullById(id);
            }
        }
    }
}
