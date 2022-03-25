package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.commons.constants.IssueRefType;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.service.ProjectApplicationService;
import io.metersphere.service.UserService;
import io.metersphere.track.dto.*;
import io.metersphere.track.request.testcase.TestPlanCaseBatchRequest;
import io.metersphere.track.request.testcase.TrackCount;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
import io.metersphere.track.request.testplancase.TestPlanFuncCaseBatchRequest;
import io.metersphere.track.request.testplancase.TestPlanFuncCaseConditions;
import io.metersphere.track.request.testreview.SaveCommentRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanTestCaseService {

    @Resource
    TestPlanTestCaseMapper testPlanTestCaseMapper;
    @Resource
    UserService userService;
    @Resource
    TestPlanService testPlanService;
    @Resource
    TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    TestPlanLoadCaseService testPlanLoadCaseService;
    @Resource
    TestPlanScenarioCaseService testPlanScenarioCaseService;
    @Resource
    ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private TestCaseMapper testCaseMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestCaseTestMapper testCaseTestMapper;
    @Resource
    private TestCaseCommentService testCaseCommentService;
    @Resource
    private TestCaseService testCaseService;
    @Resource
    private TestCaseIssueService testCaseIssueService;
    @Resource
    private ProjectApplicationService projectApplicationService;

    public List<TestPlanTestCaseWithBLOBs> listAll() {
        TestPlanTestCaseExample example = new TestPlanTestCaseExample();
        example.createCriteria();
        return testPlanTestCaseMapper.selectByExampleWithBLOBs(example);
    }

    public void updateIssues(int issuesCount, String id, String caseId, String issues) {
        extTestPlanTestCaseMapper.update(issuesCount, id, caseId, issues);//to
    }

    public List<TestPlanCaseDTO> list(QueryTestPlanCaseRequest request) {
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        List<TestPlanCaseDTO> list = extTestPlanTestCaseMapper.list(request);
        QueryMemberRequest queryMemberRequest = new QueryMemberRequest();
        queryMemberRequest.setProjectId(request.getProjectId());
        Map<String, String> userMap = userService.getProjectMemberList(queryMemberRequest)
                .stream().collect(Collectors.toMap(User::getId, User::getName));
        list.forEach(item -> {
            item.setExecutorName(userMap.get(item.getExecutor()));
            item.setMaintainerName(userMap.get(item.getMaintainer()));
        });
        return list;
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

    public void editTestCase(TestPlanTestCaseWithBLOBs testPlanTestCase) {
        if (StringUtils.equals(TestPlanTestCaseStatus.Prepare.name(), testPlanTestCase.getStatus())) {
            testPlanTestCase.setStatus(TestPlanTestCaseStatus.Underway.name());
        }
        testPlanTestCase.setExecutor(SessionUtils.getUser().getId());
        testPlanTestCase.setUpdateTime(System.currentTimeMillis());
        testPlanTestCase.setRemark(null);
        testPlanTestCaseMapper.updateByPrimaryKeySelective(testPlanTestCase);
    }

    public int deleteTestCase(String id) {
        return testPlanTestCaseMapper.deleteByPrimaryKey(id);
    }

    public void editTestCaseBath(TestPlanCaseBatchRequest request) {
        TestPlanTestCaseExample testPlanTestCaseExample = getBatchExample(request);
        TestPlanTestCaseWithBLOBs testPlanTestCase = new TestPlanTestCaseWithBLOBs();
        BeanUtils.copyBean(testPlanTestCase, request);
        testPlanTestCase.setUpdateTime(System.currentTimeMillis());
        testPlanTestCaseMapper.updateByExampleSelective(
                testPlanTestCase,
                testPlanTestCaseExample);
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

    public TestPlanCaseDTO get(String testplanTestCaseId) {
        TestPlanCaseDTO testPlanCaseDTO = extTestPlanTestCaseMapper.get(testplanTestCaseId);
        List<TestCaseTestDTO> testCaseTestDTOS = extTestPlanTestCaseMapper.listTestCaseTest(testPlanCaseDTO.getCaseId());
        testCaseTestDTOS.forEach(dto -> {
            setTestName(dto);
        });
        testPlanCaseDTO.setList(testCaseTestDTOS);
        return testPlanCaseDTO;
    }

    private void setTestName(TestCaseTestDTO dto) {
        String type = dto.getTestType();
        String id = dto.getTestId();
        switch (type) {
            case "performance":
                LoadTest loadTest = loadTestMapper.selectByPrimaryKey(id);
                if (loadTest != null) {
                    dto.setTestName(loadTest.getName());
                }
                break;
            case "testcase":
                ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = apiTestCaseMapper.selectByPrimaryKey(id);
                if (apiTestCaseWithBLOBs != null) {
                    dto.setTestName(apiTestCaseWithBLOBs.getName());
                }
                break;
            case "automation":
                ApiScenarioWithBLOBs apiScenarioWithBLOBs = apiScenarioMapper.selectByPrimaryKey(id);
                if (apiScenarioWithBLOBs != null) {
                    dto.setTestName(apiScenarioWithBLOBs.getName());
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
                    Boolean hasApiFailCase = testPlanApiCaseService.hasFailCase(testPlanTestCase.getPlanId(), apiCaseIds);
                    Boolean hasScenarioFailCase = testPlanScenarioCaseService.hasFailCase(testPlanTestCase.getPlanId(), automationIds);
                    Boolean hasLoadFailCase = testPlanLoadCaseService.hasFailCase(testPlanTestCase.getPlanId(), performanceIds);
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

                    SaveCommentRequest saveCommentRequest = new SaveCommentRequest();
                    saveCommentRequest.setCaseId(testPlanTestCase.getCaseId());
                    saveCommentRequest.setId(UUID.randomUUID().toString());
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
            testPlanTestCaseMapper.updateByPrimaryKeySelective(item);
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
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(id), testCase.getProjectId(), testCase.getName(), planTestCaseWithBLOBs.getCreateUser(), new LinkedList<>());
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

    public void calculatePlanReport(String planId, TestPlanSimpleReportDTO report) {
        List<PlanReportCaseDTO> planReportCaseDTOS = extTestPlanTestCaseMapper.selectForPlanReport(planId);
        TestPlanFunctionResultReportDTO functionResult = report.getFunctionResult();
        List<TestCaseReportStatusResultDTO> statusResult = new ArrayList<>();
        Map<String, TestCaseReportStatusResultDTO> statusResultMap = new HashMap<>();

        TestPlanUtils.buildStatusResultMap(planReportCaseDTOS, statusResultMap, report, TestPlanTestCaseStatus.Pass.name());
        TestPlanUtils.addToReportCommonStatusResultList(statusResultMap, statusResult);

        TestPlanUtils.addToReportStatusResultList(statusResultMap, statusResult, TestPlanTestCaseStatus.Blocking.name());
        TestPlanUtils.addToReportStatusResultList(statusResultMap, statusResult, TestPlanTestCaseStatus.Skip.name());
        TestPlanUtils.addToReportStatusResultList(statusResultMap, statusResult, TestPlanTestCaseStatus.Underway.name());
        functionResult.setCaseData(statusResult);
    }

    public List<TestPlanCaseDTO> getFailureCases(String planId) {
        List<TestPlanCaseDTO> allCases = extTestPlanTestCaseMapper.getCases(planId, "Failure");
        return buildCaseInfo(allCases);
    }

    public List<TestPlanCaseDTO> getAllCases(String planId) {
        List<TestPlanCaseDTO> allCases = extTestPlanTestCaseMapper.getCases(planId, null);
        return buildCaseInfo(allCases);
    }

    public List<TestPlanCaseDTO> buildCaseInfo(List<TestPlanCaseDTO> cases) {
        if(CollectionUtils.isNotEmpty(cases)){
            Map<String, Project> projectMap = ServiceUtils.getProjectMap(
                    cases.stream().map(TestPlanCaseDTO::getProjectId).collect(Collectors.toList()));
            Map<String, String> userNameMap = ServiceUtils.getUserNameMap(
                    cases.stream().map(TestPlanCaseDTO::getExecutor).collect(Collectors.toList()));
            cases.forEach(item -> {
                if(projectMap.containsKey(item.getProjectId())){
                    item.setProjectName(projectMap.get(item.getProjectId()).getName());
                }
                ProjectConfig config = projectApplicationService.getSpecificTypeValue(item.getProjectId(), ProjectApplicationType.CASE_CUSTOM_NUM.name());
                boolean customNum = config.getCaseCustomNum();
                item.setIsCustomNum(customNum);
                item.setExecutorName(userNameMap.get(item.getExecutor()));
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
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, TestPlanTestCaseWithBLOBs.class,
                testPlanTestCaseMapper::selectByPrimaryKey,
                extTestPlanTestCaseMapper::getPreOrder,
                extTestPlanTestCaseMapper::getLastOrder,
                testPlanTestCaseMapper::updateByPrimaryKeySelective);
    }

}
