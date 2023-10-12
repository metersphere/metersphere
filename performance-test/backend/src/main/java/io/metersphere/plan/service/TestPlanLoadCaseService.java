package io.metersphere.plan.service;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.TestPlanLoadCaseMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanLoadCaseMapper;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.constants.TestPlanLoadCaseStatus;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.*;
import io.metersphere.i18n.Translator;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.plan.dto.LoadPlanReportDTO;
import io.metersphere.plan.dto.TestPlanLoadCaseDTO;
import io.metersphere.plan.request.LoadCaseReportBatchRequest;
import io.metersphere.plan.request.LoadCaseReportRequest;
import io.metersphere.plan.request.LoadCaseRequest;
import io.metersphere.plan.request.RunBatchTestPlanRequest;
import io.metersphere.plan.service.remote.TestPlanService;
import io.metersphere.request.*;
import io.metersphere.service.*;
import io.metersphere.xpack.resourcepool.service.ValidQuotaResourcePoolService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanLoadCaseService {
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
    private TestPlanService testPlanService;
    @Resource
    private MetricQueryService metricQueryService;
    @Resource
    private ExtLoadTestMapper extLoadTestMapper;
    @Resource
    private PerfExecService perfExecService;
    @Resource
    private PerformanceReportService performanceReportService;
    @Resource
    private BaseTestResourcePoolService baseTestResourcePoolService;
    @Resource
    private ValidQuotaResourcePoolService ValidQuotaResourcePoolService;


    public Pager<List<LoadTestDTO>> relevanceList(LoadCaseRequest request, int goPage, int pageSize) {
        List<OrderRequest> orders = ServiceUtils.getDefaultSortOrder(request.getOrders());
        orders.forEach(i -> i.setPrefix("load_test"));
        request.setOrders(orders);
        if (request.getAllowedRepeatCase()) {
            request.setRepeatCase(true);
        }
        List<String> ids = extTestPlanLoadCaseMapper.selectIdsNotInPlan(request);
        if (CollectionUtils.isEmpty(ids)) {
            return PageUtils.setPageInfo(PageHelper.startPage(goPage, pageSize, true), new ArrayList<>());
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

        testPlanService.statusReset(request.getTestPlanId());

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
        String reportId = UUID.randomUUID().toString();
        request.setReportId(reportId);
        TestPlanLoadCaseWithBLOBs testPlanLoadCase = testPlanLoadCaseMapper.selectByPrimaryKey(request.getTestPlanLoadId());
        testPlanLoadCase.setLoadReportId(reportId);
        testPlanLoadCaseMapper.updateByPrimaryKeySelective(testPlanLoadCase);

        // 设置本次报告中的压力配置信息
        request.setLoadConfiguration(testPlanLoadCase.getLoadConfiguration());
        request.setAdvancedConfiguration(testPlanLoadCase.getAdvancedConfiguration());
        request.setTestResourcePoolId(testPlanLoadCase.getTestResourcePoolId());

        performanceTestService.run(request);

        return reportId;
    }

    public void runBatch(RunBatchTestPlanRequest request) {
        if (request != null && CollectionUtils.isNotEmpty(request.getRequests())) {
            Map<String, String> reqMap = request.getRequests().stream().collect(Collectors.toMap(RunTestPlanRequest::getTestPlanLoadId, a -> a.getId(), (k1, k2) -> k1));
            perfExecService.run(null, request.getConfig(), ReportTriggerMode.BATCH.name(), reqMap);
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
            testPlanLoadCase.setLoadReportId(StringUtils.EMPTY);
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
            TestPlan testPlan = testPlanService.get(bloBs.getTestPlanId());
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
            TestPlan testPlan = testPlanService.get(cases.get(0).getTestPlanId());
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
                TestPlan testPlan = testPlanService.get(cases.get(0).getTestPlanId());
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

    public void deleteByPlanIds(List<String> planIds) {
        if (CollectionUtils.isEmpty(planIds)) {
            return;
        }
        TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
        example.createCriteria().andTestPlanIdIn(planIds);
        testPlanLoadCaseMapper.deleteByExample(example);
    }


    public List<TestPlanLoadCaseDTO> getAllCases(String planId) {
        List<TestPlanLoadCaseDTO> cases = extTestPlanLoadCaseMapper.getCases(planId, null);
        return buildCases(cases);
    }

    public List<TestPlanLoadCaseDTO> getAllCases(Map<String, String> loadCaseReportMap) {
        List<TestPlanLoadCaseDTO> cases = extTestPlanLoadCaseMapper.getCasesByIds(loadCaseReportMap.keySet());
        for (TestPlanLoadCaseDTO loadCaseDTO : cases) {
            String reportID = loadCaseReportMap.get(loadCaseDTO.getId());
            String status = null;
            if (StringUtils.isNoneEmpty(reportID)) {
                status = extLoadTestReportMapper.selectStatusById(reportID);
            }
            if (StringUtils.isEmpty(status)) {
                status = Translator.get("not_execute");
            } else if (StringUtils.equals(PerformanceTestStatus.Completed.name(), status)) {
                status = TestPlanLoadCaseStatus.success.name();
            }
            loadCaseDTO.setReportId(reportID);
            loadCaseDTO.setLoadReportId(reportID);
            loadCaseDTO.setStatus(status);
        }
        return buildCases(cases);
    }

    public List<TestPlanLoadCaseDTO> getFailureCases(String planId) {
        List<TestPlanLoadCaseDTO> failureCases = extTestPlanLoadCaseMapper.getCases(planId, TestPlanLoadCaseStatus.error.name());
        return buildCases(failureCases);
    }

    public List<TestPlanLoadCaseDTO> buildCases(List<TestPlanLoadCaseDTO> cases) {
        Map<String, String> userNameMap = ServiceUtils.getUserNameMap(
                cases.stream().map(TestPlanLoadCaseDTO::getCreateUser).collect(Collectors.toList()));
        cases.forEach(item -> {
            item.setUserName(userNameMap.get(item.getCreateUser()));
        });
        return cases;
    }

    public String getPlanLoadCaseConfig(String loadCaseId) {
        if (StringUtils.isBlank(loadCaseId)) {
            return StringUtils.EMPTY;
        }
        TestPlanLoadCaseWithBLOBs testPlanLoadCase = testPlanLoadCaseMapper.selectByPrimaryKey(loadCaseId);
        if (testPlanLoadCase != null) {
            return testPlanLoadCase.getLoadConfiguration();
        }
        return StringUtils.EMPTY;
    }

    public String getPlanLoadCaseResourcePoolId(String loadReportId) {
        if (StringUtils.isBlank(loadReportId)) {
            return StringUtils.EMPTY;
        }
        LoadTestReportWithBLOBs loadCases = loadTestReportMapper.selectByPrimaryKey(loadReportId);
        if (loadCases != null) {
            return loadCases.getTestResourcePoolId();
        }
        return StringUtils.EMPTY;
    }


    public String getAdvancedConfiguration(String loadCaseId) {
        if (StringUtils.isBlank(loadCaseId)) {
            return StringUtils.EMPTY;
        }
        TestPlanLoadCaseWithBLOBs testPlanLoadCase = testPlanLoadCaseMapper.selectByPrimaryKey(loadCaseId);
        if (testPlanLoadCase != null) {
            return testPlanLoadCase.getAdvancedConfiguration();
        }
        return StringUtils.EMPTY;
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

    public List<String> getStatusByTestPlanId(String planId) {
        return extTestPlanLoadCaseMapper.getStatusByTestPlanId(planId);
    }

    public void copyPlan(String sourcePlanId, String targetPlanId) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            TestPlanLoadCaseExample example = new TestPlanLoadCaseExample();
            example.createCriteria().andTestPlanIdEqualTo(sourcePlanId);
            List<TestPlanLoadCaseWithBLOBs> loadCases = testPlanLoadCaseMapper.selectByExampleWithBLOBs(example);
            TestPlanLoadCaseMapper mapper = sqlSession.getMapper(TestPlanLoadCaseMapper.class);
            if (!org.apache.commons.collections.CollectionUtils.isEmpty(loadCases)) {
                Long nextLoadOrder = ServiceUtils.getNextOrder(targetPlanId, extTestPlanLoadCaseMapper::getLastOrder);
                for (TestPlanLoadCaseWithBLOBs loadCase : loadCases) {
                    TestPlanLoadCaseWithBLOBs load = new TestPlanLoadCaseWithBLOBs();
                    load.setId(UUID.randomUUID().toString());
                    load.setTestPlanId(targetPlanId);
                    load.setLoadCaseId(loadCase.getLoadCaseId());
                    load.setCreateTime(System.currentTimeMillis());
                    load.setUpdateTime(System.currentTimeMillis());
                    load.setCreateUser(SessionUtils.getUserId());
                    load.setOrder(nextLoadOrder);
                    load.setTestResourcePoolId(loadCase.getTestResourcePoolId());
                    load.setLoadConfiguration(loadCase.getLoadConfiguration());
                    load.setAdvancedConfiguration(loadCase.getAdvancedConfiguration());
                    mapper.insert(load);
                    nextLoadOrder += 5000;
                }
            }
            sqlSession.flushStatements();
        }
    }

    public LoadPlanReportDTO buildLoadReport(PlanSubReportRequest request) {
        LoadPlanReportDTO loadPlanReport = new LoadPlanReportDTO();
        String planId = request.getPlanId();
        Boolean saveResponse = request.getSaveResponse();
        Map config = request.getConfig();
        if (ServiceUtils.checkConfigEnable(config, "load")) {
            List<TestPlanLoadCaseDTO> allCases = null;
            if (checkReportConfig(config, "load", "all")) {
                allCases = getAllCases(planId);
                if (saveResponse) {
                    buildLoadResponse(allCases);
                }
                loadPlanReport.setLoadAllCases(allCases);
            }
            if (checkReportConfig(config, "load", "failure")) {
                List<TestPlanLoadCaseDTO> failureCases = null;
                if (!org.apache.commons.collections.CollectionUtils.isEmpty(allCases)) {
                    failureCases = allCases.stream()
                            .filter(i -> StringUtils.isNotBlank(i.getStatus())
                                    && i.getStatus().equals("error"))
                            .collect(Collectors.toList());
                } else {
                    failureCases = getFailureCases(planId);
                }
                loadPlanReport.setLoadFailureCases(failureCases);
            }
        }
        return loadPlanReport;
    }

    public void buildLoadResponse(List<TestPlanLoadCaseDTO> cases) {
        if (!CollectionUtils.isEmpty(cases)) {
            cases.forEach(item -> {
                LoadCaseReportRequest request = new LoadCaseReportRequest();
                String reportId = item.getLoadReportId();
                if (StringUtils.isNotBlank(reportId)) {
                    request.setTestPlanLoadCaseId(item.getId());
                    request.setReportId(reportId);
                    Boolean existReport = isExistReport(request);
                    if (existReport) {
                        try {
                            LoadTestReportWithBLOBs loadTestReport = performanceReportService.getLoadTestReport(reportId);
                            ReportTimeInfo reportTimeInfo = performanceReportService.getReportTimeInfo(reportId);
                            TestPlanLoadCaseDTO.ResponseDTO response = new TestPlanLoadCaseDTO.ResponseDTO();
                            if (loadTestReport != null) {
                                BeanUtils.copyBean(response, loadTestReport);
                            }
                            if (reportTimeInfo != null) {
                                BeanUtils.copyBean(response, reportTimeInfo);
                            }

                            // 压力配置
                            if (StringUtils.isBlank(loadTestReport.getLoadConfiguration())) {
                                String loadConfiguration = performanceTestService.getLoadConfiguration(item.getId());
                                response.setFixLoadConfiguration(loadConfiguration);
                            }
                            List<LoadTestExportJmx> jmxContent = performanceReportService.getJmxContent(reportId);
                            if (!org.apache.commons.collections.CollectionUtils.isEmpty(jmxContent)) {
                                response.setJmxContent(JSON.toJSONString(jmxContent.get(0)));
                                response.setFixJmxContent(jmxContent);
                            }

                            // 概览
                            TestOverview testOverview = performanceReportService.getTestOverview(reportId);
                            response.setTestOverview(testOverview);
                            List<ChartsData> loadChartData = performanceReportService.getLoadChartData(reportId);
                            response.setLoadChartData(loadChartData);
                            List<ChartsData> responseTimeChartData = performanceReportService.getResponseTimeChartData(reportId);
                            response.setResponseTimeChartData(responseTimeChartData);
                            List<ChartsData> errorChartData = performanceReportService.getErrorChartData(reportId);
                            response.setErrorChartData(errorChartData);
                            List<ChartsData> responseCodeChartData = performanceReportService.getResponseCodeChartData(reportId);
                            response.setResponseCodeChartData(responseCodeChartData);

                            // 报告详情
                            List<String> reportKeys = Arrays.asList(
                                    "ALL",
                                    "ActiveThreadsChart",
                                    "TransactionsChart",
                                    "ResponseTimeChart",
                                    "ResponseTimePercentilesChart",
                                    "ResponseCodeChart",
                                    "ErrorsChart",
                                    "LatencyChart",
                                    "BytesThroughputChart");

                            Map<String, List<ChartsData>> checkOptions = new HashMap<>();
                            reportKeys.forEach(reportKey -> {
                                List<ChartsData> reportChart = performanceReportService.getReportChart(reportKey, reportId);
                                checkOptions.put(reportKey, reportChart);
                            });
                            response.setCheckOptions(checkOptions);

                            // 统计分析
                            List<Statistics> reportStatistics = performanceReportService.getReportStatistics(reportId);
                            response.setReportStatistics(reportStatistics);

                            // 错误分析
                            List<Errors> reportErrors = performanceReportService.getReportErrors(reportId);
                            response.setReportErrors(reportErrors);
                            List<ErrorsTop5> reportErrorsTop5 = performanceReportService.getReportErrorsTOP5(reportId);
                            response.setReportErrorsTop5(reportErrorsTop5);
                            SamplesRecord samplesRecord = performanceReportService.getErrorSamples(reportId);
                            response.setErrorSamples(samplesRecord);
                            // 日志详情
                            List<LogDetailDTO> reportLogResource = performanceReportService.getReportLogResource(reportId);
                            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(reportLogResource)) {
                                for (LogDetailDTO log : reportLogResource) {
                                    List<LoadTestReportLog> reportLogs = performanceReportService.getReportLogs(reportId, log.getResourceId());
                                    log.setReportLogs(reportLogs);
                                }
                            }
                            response.setReportLogResource(reportLogResource);
                            List<TestResourcePoolDTO> testResourcePoolDTOS = ValidQuotaResourcePoolService.listValidQuotaResourcePools();
                            response.setResourcePools(testResourcePoolDTOS);
                            List<Monitor> reportResource = metricQueryService.queryReportResource(reportId);
                            response.setReportResource(reportResource);
                            List<MetricData> metricData = metricQueryService.queryMetric(reportId);
                            response.setMetricData(metricData);
                            item.setResponse(response);
                        } catch (Exception e) {
                            LogUtil.error(e);
                        }
                    }
                }
            });
        }
    }

    public LoadPlanReportDTO buildExecuteLoadReport(PlanSubReportRequest request) {
        LoadPlanReportDTO loadPlanReport = new LoadPlanReportDTO();
        Boolean saveResponse = request.getSaveResponse();
        Map config = request.getConfig();
        Map<String, String> loadCaseReportMap = request.getReportIdMap();
        if (MapUtils.isEmpty(loadCaseReportMap)) {
            loadPlanReport.setLoadAllCases(new ArrayList<>());
            return loadPlanReport;
        }
        if (ServiceUtils.checkConfigEnable(config, "load")) {
            List<TestPlanLoadCaseDTO> allCases = null;
            if (checkReportConfig(config, "load", "all")) {
                allCases = getAllCases(loadCaseReportMap);
                if (saveResponse) {
                    buildLoadResponse(allCases);
                }
                loadPlanReport.setLoadAllCases(allCases);
            }
            if (checkReportConfig(config, "load", "failure")) {
                List<TestPlanLoadCaseDTO> failureCases = null;
                if (!org.apache.commons.collections.CollectionUtils.isEmpty(allCases)) {
                    failureCases = allCases.stream()
                            .filter(i -> StringUtils.isNotBlank(i.getStatus())
                                    && StringUtils.equalsAnyIgnoreCase(i.getStatus(), "error"))
                            .collect(Collectors.toList());
                }
                loadPlanReport.setLoadFailureCases(failureCases);
            }
        }
        return loadPlanReport;
    }

    private boolean checkReportConfig(Map config, String key, String subKey) {
        return ServiceUtils.checkConfigEnable(config, key, subKey);
    }

    public Boolean haveExecCase(String planId) {
        TestPlanLoadCaseExample loadCaseExample = new TestPlanLoadCaseExample();
        loadCaseExample.createCriteria().andTestPlanIdEqualTo(planId);
        List<TestPlanLoadCase> testPlanLoadCases = testPlanLoadCaseMapper.selectByExample(loadCaseExample);
        return !CollectionUtils.isEmpty(testPlanLoadCases);
    }

    public void relevanceByTestIds(List<String> ids, String planId) {
        Long nextLoadOrder = ServiceUtils.getNextOrder(planId, extTestPlanLoadCaseMapper::getLastOrder);
        for (String id : ids) {
            LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(id);
            TestPlanLoadCaseWithBLOBs t = new TestPlanLoadCaseWithBLOBs();
            t.setId(UUID.randomUUID().toString());
            t.setTestPlanId(planId);
            t.setLoadCaseId(id);
            t.setCreateTime(System.currentTimeMillis());
            t.setUpdateTime(System.currentTimeMillis());
            t.setOrder(nextLoadOrder);
            if (loadTest != null) {
                t.setTestResourcePoolId(loadTest.getTestResourcePoolId());
                t.setLoadConfiguration(loadTest.getLoadConfiguration());
                t.setAdvancedConfiguration(loadTest.getAdvancedConfiguration());
            }
            nextLoadOrder += 5000;
            TestPlanLoadCaseExample testPlanLoadCaseExample = new TestPlanLoadCaseExample();
            testPlanLoadCaseExample.createCriteria().andTestPlanIdEqualTo(planId).andLoadCaseIdEqualTo(t.getLoadCaseId());
            if (testPlanLoadCaseMapper.countByExample(testPlanLoadCaseExample) <= 0) {
                testPlanLoadCaseMapper.insert(t);
            }
        }
    }

    public List<PlanReportCaseDTO> selectStatusForPlanReport(String planId) {
        return extTestPlanLoadCaseMapper.selectForPlanReport(planId);
    }

    public Boolean isExecuting(String planId, String projectId) {
        LoadCaseRequest loadCaseRequest = new LoadCaseRequest();
        loadCaseRequest.setTestPlanId(planId);
        loadCaseRequest.setProjectId(projectId);
        return !list(loadCaseRequest)
                .stream()
                .map(TestPlanLoadCaseDTO::getLoadCaseId)
                .collect(Collectors.toList()).isEmpty();
    }

    public List<String> getResourcePoolFromReportByPlanId(String planId) {
        return extTestPlanLoadCaseMapper.getResourcePoolByPlanId(planId);
    }

    public List<String> getCaseResourcePoolByPlanId(String planId) {
        return extTestPlanLoadCaseMapper.getCaseResourcePoolByPlanId(planId);
    }
}
