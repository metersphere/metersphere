package io.metersphere.track.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.member.QueryMemberRequest;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.service.UserService;
import io.metersphere.track.dto.TestCaseTestDTO;
import io.metersphere.track.dto.TestPlanCaseDTO;
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

    public List<TestPlanTestCaseWithBLOBs> listAll() {
        TestPlanTestCaseExample example = new TestPlanTestCaseExample();
        example.createCriteria();
        return testPlanTestCaseMapper.selectByExampleWithBLOBs(example);
    }

    public void updateIssues(int issuesCount, String id, String caseId, String issues) {
        extTestPlanTestCaseMapper.update(issuesCount, id, caseId, issues);
    }

    public List<TestPlanCaseDTO> list(QueryTestPlanCaseRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
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

    public List<String> getTestPlanTestCaseIds(String testId) {
        return extTestPlanTestCaseMapper.getTestPlanTestCaseIds(testId);
    }

    public int updateTestCaseStates(List<String> ids, String reportStatus) {
        return extTestPlanTestCaseMapper.updateTestCaseStates(ids, reportStatus);
    }

    /**
     * 更新测试计划关联接口测试的功能用例的状态
     *
     * @param testId 接口测试id
     */
    public void updateTestCaseStates(String testId, String testName, String planId, String testType) {
        TestPlan testPlan = testPlanService.getTestPlan(planId);
        if (BooleanUtils.isNotTrue(testPlan.getAutomaticStatusUpdate())) {
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
        return extTestPlanTestCaseMapper.listForMinder(request);
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
}
