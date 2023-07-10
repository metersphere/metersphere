package io.metersphere.plan.service;

import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestCaseMapper;
import io.metersphere.base.mapper.TestCaseTestMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.TestPlanTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.commons.constants.IssueRefType;
import io.metersphere.commons.constants.MicroServiceName;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.*;
import io.metersphere.constants.TestCaseCommentType;
import io.metersphere.dto.*;
import io.metersphere.excel.constants.TestPlanTestCaseStatus;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.StatusReference;
import io.metersphere.plan.dto.TestCaseReportStatusResultDTO;
import io.metersphere.plan.dto.TestPlanReportDataStruct;
import io.metersphere.plan.request.function.*;
import io.metersphere.plan.service.remote.api.PlanApiAutomationService;
import io.metersphere.plan.service.remote.api.PlanApiTestCaseService;
import io.metersphere.plan.service.remote.api.PlanTestPlanApiCaseService;
import io.metersphere.plan.service.remote.api.PlanTestPlanScenarioCaseService;
import io.metersphere.plan.service.remote.performance.PlanPerformanceTestService;
import io.metersphere.plan.service.remote.performance.PlanTestPlanLoadCaseService;
import io.metersphere.plan.utils.TestPlanStatusCalculator;
import io.metersphere.request.OrderRequest;
import io.metersphere.request.ResetOrderRequest;
import io.metersphere.request.member.QueryMemberRequest;
import io.metersphere.request.testcase.TrackCount;
import io.metersphere.request.testreview.SaveCommentRequest;
import io.metersphere.service.*;
import io.metersphere.utils.DiscoveryUtil;
import io.metersphere.xpack.track.dto.IssuesDao;
import io.metersphere.xpack.version.service.ProjectVersionService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanTestCaseService {

    @Resource
    TestPlanTestCaseMapper testPlanTestCaseMapper;
    @Resource
    BaseUserService baseUserService;
    @Resource
    TestPlanService testPlanService;
    @Resource
    PlanTestPlanApiCaseService planTestPlanApiCaseService;
    @Resource
    PlanTestPlanScenarioCaseService planTestPlanScenarioCaseService;
    @Resource
    PlanTestPlanLoadCaseService planTestPlanLoadCaseService;
    @Resource
    ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;
    @Resource
    private PlanPerformanceTestService planPerformanceTestService;
    @Resource
    private PlanApiTestCaseService planApiTestCaseService;
    @Resource
    private PlanApiAutomationService panApiAutomationService;
    @Resource
    private TestCaseMapper testCaseMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestCaseTestMapper testCaseTestMapper;
    @Resource
    private io.metersphere.service.TestCaseCommentService testCaseCommentService;
    @Resource
    private io.metersphere.service.TestCaseService testCaseService;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private FunctionCaseExecutionInfoService functionCaseExecutionInfoService;
    @Resource
    private CustomFieldTestCaseService customFieldTestCaseService;
    @Resource
    private TestCaseSyncStatusService testCaseSyncStatusService;

    private static final String CUSTOM_NUM = "custom_num";
    private static final String NUM = "num";

    public List<TestPlanTestCaseWithBLOBs> listAll() {
        TestPlanTestCaseExample example = new TestPlanTestCaseExample();
        example.createCriteria();
        return testPlanTestCaseMapper.selectByExampleWithBLOBs(example);
    }

    public void updateIssues(int issuesCount, String id, String caseId, String issues) {
        extTestPlanTestCaseMapper.update(issuesCount, id, caseId, issues);//to
    }

    public List<TestPlanCaseDTO> list(QueryTestPlanCaseRequest request) {
        ServiceUtils.buildCombineTagsToSupportMultiple(request);
        List<TestPlanCaseDTO> list = extTestPlanTestCaseMapper.list(request);
        Map<String, List<CustomFieldDao>> fieldMap =
                customFieldTestCaseService.getMapByResourceIdsForList(list.stream().map(TestPlanCaseDTO::getCaseId).collect(Collectors.toList()));
        list.forEach(i -> i.setFields(fieldMap.get(i.getCaseId())));
        if (CollectionUtils.isNotEmpty(list)) {
            // 设置版本信息
            ServiceUtils.buildVersionInfo(list);
            ServiceUtils.buildProjectInfo(list);
            ServiceUtils.buildCustomNumInfo(list);

            QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
            queryMemberRequest.setProjectId(request.getProjectId());
            Map<String, String> userMap = baseUserService.getProjectMemberList(queryMemberRequest)
                    .stream().collect(Collectors.toMap(User::getId, User::getName));
            List<String> versionIds = list.stream().map(TestPlanCaseDTO::getVersionId).collect(Collectors.toList());
            ProjectVersionService projectVersionService = CommonBeanFactory.getBean(ProjectVersionService.class);
            if (projectVersionService != null) {
                Map<String, String> projectVersionMap = projectVersionService.getProjectVersionByIds(versionIds).stream()
                        .collect(Collectors.toMap(ProjectVersion::getId, ProjectVersion::getName));
                list.forEach(item -> {
                    item.setVersionName(projectVersionMap.get(item.getVersionId()));
                });
            }
            list.forEach(item -> {
                item.setExecutorName(userMap.get(item.getExecutor()));
                item.setMaintainerName(userMap.get(item.getMaintainer()));
            });
        }
        return list;
    }

    public QueryTestPlanCaseRequest setCustomNumOrderParam(QueryTestPlanCaseRequest request) {
        List<OrderRequest> orders = ServiceUtils.getDefaultSortOrder(request.getOrders());
        // CUSTOM_NUM ORDER
        boolean customOrderFlag = orders.stream().anyMatch(order -> StringUtils.equals(order.getName(), CUSTOM_NUM));
        if (customOrderFlag) {
            // 判断当前项目时候开启自定义字段的配置
            boolean customNumEnable = baseProjectApplicationService.checkCustomNumByProjectId(request.getProjectId());
            orders.forEach(order -> {
                if (StringUtils.equals(order.getName(), CUSTOM_NUM)) {
                    if (customNumEnable) {
                        order.setName(CUSTOM_NUM);
                    } else {
                        order.setName(NUM);
                    }
                }
            });
        }
        request.setOrders(orders);
        return request;
    }

    public QueryTestPlanCaseRequest wrapQueryTestPlanCaseRequest(QueryTestPlanCaseRequest request) {
        ProjectApplication projectApplication = baseProjectApplicationService.getProjectApplication(request.getProjectId(), ProjectApplicationType.CASE_CUSTOM_NUM.name());
        return request;
    }

    public List<TestPlanCaseDTO> listByPlanId(QueryTestPlanCaseRequest request) {
        List<TestPlanCaseDTO> list = extTestPlanTestCaseMapper.listByPlanId(request);
        return list;
    }

    public List<TestPlanCaseDTO> listByNode(QueryTestPlanCaseRequest request) {
        List<TestPlanCaseDTO> list = extTestPlanTestCaseMapper.listByNode(request);
        return list;
    }

    public List<TestPlanCaseDTO> listByNodes(QueryTestPlanCaseRequest request) {
        List<TestPlanCaseDTO> list = extTestPlanTestCaseMapper.listByNodes(request);
        return list;
    }

    public void editTestCase(TestPlanFuncCaseEditRequest testPlanTestCase) {
        if (!StringUtils.equals(TestPlanTestCaseStatus.Prepare.name(), testPlanTestCase.getStatus())) {
            //记录功能用例执行信息
            functionCaseExecutionInfoService.insertExecutionInfo(testPlanTestCase.getId(), testPlanTestCase.getStatus());
        }
        setUpdateCaseExecutor(testPlanTestCase);

        testPlanTestCase.setUpdateTime(System.currentTimeMillis());
        testPlanTestCase.setRemark(null);
        testPlanTestCaseMapper.updateByPrimaryKeySelective(testPlanTestCase);
        testCaseService.updateLastExecuteStatus(testPlanTestCase.getCaseId(), testPlanTestCase.getStatus());

        saveComment(testPlanTestCase);
    }

    private void setUpdateCaseExecutor(TestPlanTestCaseWithBLOBs testPlanTestCase) {
        if (StringUtils.isNotBlank(testPlanTestCase.getStatus())) {
            TestPlanTestCaseWithBLOBs originData = testPlanTestCaseMapper.selectByPrimaryKey(testPlanTestCase.getId());
            testPlanTestCase.setCaseId(originData.getCaseId());
            if (!StringUtils.equals(originData.getStatus(), testPlanTestCase.getStatus())) {
                // 更新了状态才更新执行人
                testPlanTestCase.setExecutor(SessionUtils.getUser().getId());
            }
        }
    }

    private void saveComment(TestPlanFuncCaseEditRequest testPlanTestCase) {
        if (StringUtils.isNotEmpty(testPlanTestCase.getComment())) {
            SaveCommentRequest saveCommentRequest = new SaveCommentRequest();
            saveCommentRequest.setCaseId(testPlanTestCase.getCaseId());
            saveCommentRequest.setDescription(testPlanTestCase.getComment());
            saveCommentRequest.setStatus(testPlanTestCase.getStatus());
            saveCommentRequest.setType(TestCaseCommentType.PLAN.name());
            testCaseCommentService.saveComment(saveCommentRequest);
        }
    }

    public int deleteTestCase(String id) {
        return testPlanTestCaseMapper.deleteByPrimaryKey(id);
    }

    public int deleteToGc(List<String> caseIds) {
        return updateIsDel(caseIds, true);
    }

    private int updateIsDel(List<String> caseIds, Boolean isDel) {
        if (CollectionUtils.isNotEmpty(caseIds)) {
            TestPlanTestCaseExample example = new TestPlanTestCaseExample();
            example.createCriteria().andCaseIdIn(caseIds);
            TestPlanTestCaseWithBLOBs record = new TestPlanTestCaseWithBLOBs();
            record.setIsDel(isDel);
            return testPlanTestCaseMapper.updateByExampleSelective(record, example);
        }
        return 0;
    }

    public void editTestCaseBath(TestPlanCaseBatchRequest request) {
        TestPlanTestCaseExample testPlanTestCaseExample = getBatchExample(request);
        TestPlanTestCaseWithBLOBs testPlanTestCase = new TestPlanTestCaseWithBLOBs();
        if (BooleanUtils.isFalse(request.isModifyExecutor()) && StringUtils.isNotBlank(SessionUtils.getUserId())) {
            request.setExecutor(SessionUtils.getUserId());
        }
        BeanUtils.copyBean(testPlanTestCase, request);
        testPlanTestCase.setUpdateTime(System.currentTimeMillis());
        testPlanTestCaseMapper.updateByExampleSelective(
                testPlanTestCase,
                testPlanTestCaseExample);
        if (StringUtils.isNotBlank(testPlanTestCase.getStatus()) &&
                !StringUtils.equals(TestPlanTestCaseStatus.Prepare.name(), testPlanTestCase.getStatus())) {
            //记录功能用例执行信息
            request.getIds().forEach(caseId -> {
                functionCaseExecutionInfoService.insertExecutionInfo(caseId, testPlanTestCase.getStatus());
            });
        }
        if (StringUtils.isNotBlank(request.getStatus())) {
            List<String> caseIds = extTestPlanTestCaseMapper.getCaseIdsByIds(request.getIds());
            testCaseService.updateLastExecuteStatus(caseIds, request.getStatus());
        }
    }

    public TestPlanTestCaseExample getBatchExample(TestPlanCaseBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> extTestPlanTestCaseMapper.selectIdsByQuery(query));
        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
        testPlanTestCaseExample.createCriteria().andIdIn(request.getIds());
        return testPlanTestCaseExample;
    }

    public List<TestPlanCaseDTO> getRecentTestCases(QueryTestPlanCaseRequest request, int count) {
        buildQueryRequest(request, count);
        if (request.getPlanIds().isEmpty()) {
            return new ArrayList<>();
        }

        List<TestPlanCaseDTO> recentTestedTestCase = extTestPlanTestCaseMapper.getRecentTestedTestCase(request);
        List<String> planIds = recentTestedTestCase.stream().map(TestPlanCaseDTO::getPlanId).collect(Collectors.toList());

        if (planIds.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, String> testPlanMap = testPlanService.getTestPlanByIds(planIds).stream()
                .collect(Collectors.toMap(TestPlan::getId, TestPlan::getName));

        recentTestedTestCase.forEach(testCase -> {
            testCase.setPlanName(testPlanMap.get(testCase.getPlanId()));
        });
        return recentTestedTestCase;
    }

    public List<TestPlanCaseDTO> getPendingTestCases(QueryTestPlanCaseRequest request, int count) {
        buildQueryRequest(request, count);
        if (request.getPlanIds().isEmpty()) {
            return new ArrayList<>();
        }
        return extTestPlanTestCaseMapper.getPendingTestCases(request);
    }

    public void buildQueryRequest(QueryTestPlanCaseRequest request, int count) {
        SessionUser user = SessionUtils.getUser();
        List<String> relateTestPlanIds = extTestPlanTestCaseMapper.findRelateTestPlanId(user.getId(), SessionUtils.getCurrentWorkspaceId(), SessionUtils.getCurrentProjectId());
        PageHelper.startPage(1, count, true);
        request.setPlanIds(relateTestPlanIds);
        request.setExecutor(user.getId());
    }

    public TestPlanCaseDTO get(String id) {
        TestPlanCaseDTO testPlanCaseDTO = extTestPlanTestCaseMapper.get(id);
        ServiceUtils.buildCustomNumInfo(testPlanCaseDTO);
        List<TestCaseTestDTO> testCaseTestDTOS = extTestPlanTestCaseMapper.listTestCaseTest(testPlanCaseDTO.getCaseId());
        testCaseTestDTOS.forEach(this::setTestName);
        testPlanCaseDTO.setList(testCaseTestDTOS);
        buildCustomField(testPlanCaseDTO);
        return testPlanCaseDTO;
    }

    private void buildCustomField(TestPlanCaseDTO data) {
        data.setFields(testCaseService.getCustomFieldByCaseId(data.getCaseId()));
    }

    private void setTestName(TestCaseTestDTO dto) {
        String type = dto.getTestType();
        String id = dto.getTestId();
        Set<String> serviceIdSet = DiscoveryUtil.getServiceIdSet();
        switch (type) {
            case "performance":
                if (serviceIdSet.contains(MicroServiceName.PERFORMANCE_TEST)) {
                    LoadTest loadTest = planPerformanceTestService.get(id);
                    if (loadTest != null) {
                        dto.setTestName(loadTest.getName());
                    }
                }
                break;
            case "testcase":
                if (serviceIdSet.contains(MicroServiceName.API_TEST)) {
                    ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = planApiTestCaseService.get(id);
                    if (apiTestCaseWithBLOBs != null) {
                        dto.setTestName(apiTestCaseWithBLOBs.getName());
                    }
                }
                break;
            case "automation":
                if (serviceIdSet.contains(MicroServiceName.API_TEST)) {
                    ApiScenarioWithBLOBs apiScenarioWithBLOBs = panApiAutomationService.get(id);
                    if (apiScenarioWithBLOBs != null) {
                        dto.setTestName(apiScenarioWithBLOBs.getName());
                    }
                }
                break;
            default:
                break;
        }
    }

    public void deleteTestCaseBath(TestPlanCaseBatchRequest request) {
        TestPlanTestCaseExample example = getBatchExample(request);
        testPlanTestCaseMapper.deleteByExample(example);
    }

    /**
     * 更新测试计划关联接口测试的功能用例的状态
     *
     * @param testId 接口测试id
     */
    public void updateTestCaseStates(String testId, String testName, String planId, String testType) {
        TestPlan testPlan1 = testPlanService.getTestPlan(planId);
        if (BooleanUtils.isNotTrue(testPlan1.getAutomaticStatusUpdate())) {
            return;
        }
        TestCaseTestExample example = new TestCaseTestExample();
        example.createCriteria().andTestIdEqualTo(testId);
        // 获取跟改接口测试有关联是功能用例id
        List<TestCaseTest> testCaseTests = testCaseTestMapper.selectByExample(example);

        testCaseTests.forEach(testCaseTest -> {

            TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
            testPlanTestCaseExample.createCriteria()
                    .andCaseIdEqualTo(testCaseTest.getTestCaseId())
                    .andPlanIdEqualTo(planId);
            // 获取该功能用例与测试计划关联的用例
            List<TestPlanTestCase> testPlanTestCases = testPlanTestCaseMapper.selectByExample(testPlanTestCaseExample);

            try {
                testPlanTestCases.forEach(testPlanTestCase -> {
                    // 获取跟该功能用例关联的所有自动化用例
                    List<TestCaseTestDTO> relateTests = extTestPlanTestCaseMapper.listTestCaseTest(testPlanTestCase.getCaseId());
                    List<String> apiCaseIds = new ArrayList<>();
                    List<String> performanceIds = new ArrayList<>();
                    List<String> automationIds = new ArrayList<>();
                    relateTests.forEach(item -> {
                        String type = item.getTestType();
                        String id = item.getTestId();
                        if (StringUtils.equals(TrackCount.TESTCASE, type)) {
                            apiCaseIds.add(id);
                        } else if (StringUtils.equals(TrackCount.AUTOMATION, type)) {
                            automationIds.add(id);
                        } else if (StringUtils.equals(TrackCount.PERFORMANCE, type)) {
                            performanceIds.add(id);
                        }
                    });
                    Boolean hasApiFailCase = planTestPlanApiCaseService.hasFailCase(testPlanTestCase.getPlanId(), apiCaseIds);
                    Boolean hasScenarioFailCase = planTestPlanScenarioCaseService.hasFailCase(testPlanTestCase.getPlanId(), automationIds);
                    Boolean hasLoadFailCase = planTestPlanLoadCaseService.hasFailCase(testPlanTestCase.getPlanId(), performanceIds);
                    String status = TestPlanTestCaseStatus.Pass.name();
                    if (hasApiFailCase || hasScenarioFailCase || hasLoadFailCase) {
                        status = TestPlanTestCaseStatus.Failure.name();
                    }

                    String tip = "执行成功";
                    if (StringUtils.equals(TrackCount.TESTCASE, testType) && hasApiFailCase) {
                        tip = "执行失败";
                    } else if (StringUtils.equals(TrackCount.AUTOMATION, testType) && hasScenarioFailCase) {
                        tip = "执行失败";
                    } else if (StringUtils.equals(TrackCount.PERFORMANCE, testType) && hasLoadFailCase) {
                        tip = "执行失败";
                    }

                    TestPlanTestCaseWithBLOBs item = new TestPlanTestCaseWithBLOBs();
                    item.setId(testPlanTestCase.getId());
                    item.setStatus(status);
                    testPlanTestCaseMapper.updateByPrimaryKeySelective(item);

                    testCaseService.updateLastExecuteStatus(testPlanTestCase.getCaseId(), testPlanTestCase.getStatus());

                    SaveCommentRequest saveCommentRequest = new SaveCommentRequest();
                    saveCommentRequest.setCaseId(testPlanTestCase.getCaseId());
                    saveCommentRequest.setType(TestCaseCommentType.PLAN.name());
                    saveCommentRequest.setDescription("关联的测试：[" + testName + "]" + tip);
                    testCaseCommentService.saveComment(saveCommentRequest);
                });
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        });
    }

    public List<TestPlanCaseDTO> listForMinder(QueryTestPlanCaseRequest request) {
        List<OrderRequest> orders = ServiceUtils.getDefaultOrder(request.getOrders());
        orders.forEach(order -> {
            if (order.getName().equals("create_time")) {
                order.setPrefix("pc");
            }
        });
        request.setOrders(orders);
        List<TestPlanCaseDTO> cases = extTestPlanTestCaseMapper.listForMinder(request);
        List<String> caseIds = cases.stream().map(TestPlanCaseDTO::getId).collect(Collectors.toList());
        HashMap<String, List<IssuesDao>> issueMap = testCaseService.buildMinderIssueMap(caseIds, IssueRefType.PLAN_FUNCTIONAL.name());
        for (TestPlanCaseDTO item : cases) {
            List<IssuesDao> issues = issueMap.get(item.getId());
            if (issues != null) {
                item.setIssueList(issues);
            }
        }
        return cases;
    }

    public void editTestCaseForMinder(List<TestPlanTestCaseWithBLOBs> testPlanTestCases) {
        testPlanTestCases.forEach(item -> {
            item.setUpdateTime(System.currentTimeMillis());
            setUpdateCaseExecutor(item);
            testPlanTestCaseMapper.updateByPrimaryKeySelective(item);
            testCaseService.updateLastExecuteStatus(item.getCaseId(), item.getStatus());
        });
    }

    public List<String> idList(TestPlanFuncCaseBatchRequest request) {
        List<String> returnIdList = new ArrayList<>();
        TestPlanFuncCaseConditions conditions = request.getCondition();
        if (conditions != null && conditions.isSelectAll()) {
            conditions.setOrders(ServiceUtils.getDefaultOrder(conditions.getOrders()));
            returnIdList = extTestPlanTestCaseMapper.selectIds(conditions);
            if (conditions.getUnSelectIds() != null) {
                returnIdList.removeAll(conditions.getUnSelectIds());
            }
        }
        return returnIdList;
    }

    public String getLogDetails(String id) {
        TestPlanTestCaseWithBLOBs planTestCaseWithBLOBs = testPlanTestCaseMapper.selectByPrimaryKey(id);
        if (planTestCaseWithBLOBs != null) {
            TestCase testCase = testCaseMapper.selectByPrimaryKey(planTestCaseWithBLOBs.getCaseId());
            TestPlan testPlan = testPlanMapper.selectByPrimaryKey(planTestCaseWithBLOBs.getPlanId());
            List<DetailColumn> columns = new LinkedList<>();
            DetailColumn executeStatusColumn = new DetailColumn("状态", "lastExecuteResult", StatusReference.statusMap.get(testCase.getLastExecuteResult()), null);
            columns.add(executeStatusColumn);
            // 增加评论内容
            List<TestCaseCommentDTO> dtos = testCaseCommentService.getCaseComments(planTestCaseWithBLOBs.getCaseId());
            if (CollectionUtils.isNotEmpty(dtos)) {
                List<String> names = dtos.stream().map(TestCaseCommentDTO::getDescription).collect(Collectors.toList());
                DetailColumn detailColumn = new DetailColumn("评论", "comment", String.join(StringUtils.LF, names), null);
                columns.add(detailColumn);
            }
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(testCase.getId()), testCase.getProjectId(), testCase.getName(), planTestCaseWithBLOBs.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getLogDetails(List<String> ids) {
        TestPlanTestCaseExample example = new TestPlanTestCaseExample();
        example.createCriteria().andIdIn(ids);
        List<TestPlanTestCase> nodes = testPlanTestCaseMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(nodes)) {
            TestCaseExample testCaseExample = new TestCaseExample();
            testCaseExample.createCriteria().andIdIn(nodes.stream().map(TestPlanTestCase::getCaseId).collect(Collectors.toList()));
            List<TestCase> testCases = testCaseMapper.selectByExample(testCaseExample);
            if (CollectionUtils.isNotEmpty(testCases)) {
                List<String> names = testCases.stream().map(TestCase::getName).collect(Collectors.toList());
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), testCases.get(0).getProjectId(), String.join(",", names), nodes.get(0).getCreateUser(), new LinkedList<>());
                return JSON.toJSONString(details);
            }
        }
        return null;
    }

    public String getCaseLogDetails(List<TestPlanTestCaseWithBLOBs> testCases) {
        // 更新状态
        if (CollectionUtils.isNotEmpty(testCases)) {
            List<DetailColumn> columns = new LinkedList<>();
            TestCaseExample example = new TestCaseExample();
            List<String> ids = testCases.stream().map(TestPlanTestCaseWithBLOBs::getCaseId).collect(Collectors.toList());
            example.createCriteria().andIdIn(ids);
            List<TestCase> cases = testCaseMapper.selectByExample(example);
            if (cases != null && cases.size() > 0) {
                List<String> names = cases.stream().map(TestCase::getName).collect(Collectors.toList());
                OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(ids), cases.get(0).getProjectId(), String.join(",", names), cases.get(0).getCreateUser(), columns);
                return JSON.toJSONString(details);
            }
        }
        return null;
    }


    public void calculatePlanReport(String planId, TestPlanReportDataStruct report) {
        try {
            List<PlanReportCaseDTO> planReportCaseDTOS = extTestPlanTestCaseMapper.selectForPlanReport(planId);
            this.calculatePlanReport(planReportCaseDTOS, report);
        } catch (MSException e) {
            LogUtil.error(e);
        }
    }

    public void calculateReportByTestCaseList(String operator, TestPlan testPlan, boolean isTestPlanExecuteOver, List<TestPlanCaseDTO> testPlanCaseList, TestPlanReportDataStruct report) {
        try {
            if (ObjectUtils.anyNotNull(testPlan, report) && CollectionUtils.isNotEmpty(testPlanCaseList)) {
                List<PlanReportCaseDTO> planReportCaseDTOList = new ArrayList<>();
                testPlanCaseList.forEach(testPlanCaseDTO -> {
                    PlanReportCaseDTO planReportCaseDTO = new PlanReportCaseDTO();
                    planReportCaseDTO.setId(testPlanCaseDTO.getId());
                    planReportCaseDTO.setStatus(testPlanCaseDTO.getStatus());
                    planReportCaseDTO.setCaseId(testPlanCaseDTO.getCaseId());
                    planReportCaseDTOList.add(planReportCaseDTO);
                });

                if (testPlan.getAutomaticStatusUpdate()) {
                    if (isTestPlanExecuteOver) {
                        testCaseSyncStatusService.syncStatusByTestPlanExecuteOver(operator, testPlan.getName(), planReportCaseDTOList, report.getApiAllCases(), report.getScenarioAllCases(), report.getLoadAllCases(), report.getUiAllCases());
                    } else {
                        testCaseSyncStatusService.getTestCaseStatusByTestPlanExecuteOver(planReportCaseDTOList, report.getApiAllCases(), report.getScenarioAllCases(), report.getLoadAllCases(), report.getUiAllCases());
                    }

                }
                this.calculatePlanReport(planReportCaseDTOList, report);
            }
        } catch (MSException e) {
            LogUtil.error(e);
        }
    }

    private void calculatePlanReport(List<PlanReportCaseDTO> planReportCaseDTOList, TestPlanReportDataStruct report) {
        TestPlanFunctionResultReportDTO functionResult = report.getFunctionResult();
        List<TestCaseReportStatusResultDTO> statusResult = new ArrayList<>();
        Map<String, TestCaseReportStatusResultDTO> statusResultMap = new HashMap<>();

        TestPlanStatusCalculator.buildStatusResultMap(planReportCaseDTOList, statusResultMap, report, TestPlanTestCaseStatus.Pass.name());
        TestPlanStatusCalculator.addToReportCommonStatusResultList(statusResultMap, statusResult);

        TestPlanStatusCalculator.addToReportStatusResultList(statusResultMap, statusResult, TestPlanTestCaseStatus.Blocking.name());
        TestPlanStatusCalculator.addToReportStatusResultList(statusResultMap, statusResult, TestPlanTestCaseStatus.Skip.name());
        functionResult.setCaseData(statusResult);
    }

    public List<TestPlanCaseDTO> getAllCasesByStatusList(String planId, List<String> statusList) {
        return buildCaseInfo(extTestPlanTestCaseMapper.getCasesByStatusList(planId, statusList));
    }

    public List<TestPlanCaseDTO> getAllCases(String planId) {
        return buildCaseInfo(this.getAllCasesByStatusList(planId, null));
    }

    public List<TestPlanCaseDTO> buildCaseInfo(List<TestPlanCaseDTO> cases) {
        if (CollectionUtils.isNotEmpty(cases)) {
            Map<String, Project> projectMap = ServiceUtils.getProjectMap(
                    cases.stream().map(TestPlanCaseDTO::getProjectId).collect(Collectors.toList()));
            List<String> userIds = new ArrayList();
            userIds.addAll(cases.stream().map(TestPlanCaseDTO::getExecutor).collect(Collectors.toList()));
            userIds.addAll(cases.stream().map(TestPlanCaseDTO::getMaintainer).collect(Collectors.toList()));
            Map<String, String> userNameMap = ServiceUtils.getUserNameMap(userIds);
            cases.forEach(item -> {
                if (projectMap.containsKey(item.getProjectId())) {
                    item.setProjectName(projectMap.get(item.getProjectId()).getName());
                }
                ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(item.getProjectId(), ProjectApplicationType.CASE_CUSTOM_NUM.name());
                boolean customNum = config.getCaseCustomNum();
                item.setIsCustomNum(customNum);
                item.setExecutorName(userNameMap.get(item.getExecutor()));
                item.setMaintainerName(userNameMap.get(item.getMaintainer()));
            });
        }

        return cases;
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(TestPlanTestCaseWithBLOBs.class, TestPlanTestCaseMapper.class,
                extTestPlanTestCaseMapper::selectPlanIds,
                extTestPlanTestCaseMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, TestPlanTestCaseWithBLOBs.class,
                testPlanTestCaseMapper::selectByPrimaryKey,
                extTestPlanTestCaseMapper::getPreOrder,
                extTestPlanTestCaseMapper::getLastOrder,
                testPlanTestCaseMapper::updateByPrimaryKeySelective);
    }

    public int reduction(List<String> caseIds) {
        return updateIsDel(caseIds, false);
    }
}
