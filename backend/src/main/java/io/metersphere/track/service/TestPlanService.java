package io.metersphere.track.service;


import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestCaseMapper;
import io.metersphere.base.mapper.TestCaseNodeMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.TestPlanTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtProjectMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.commons.constants.TestPlanStatus;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.ProjectRequest;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.track.dto.*;
import io.metersphere.track.request.testcase.PlanCaseRelevanceRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.i18n.Translator;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanService {

    @Resource
    TestPlanMapper testPlanMapper;

    @Resource
    ExtTestPlanMapper extTestPlanMapper;

    @Resource
    ExtTestPlanTestCaseMapper extTestPlanTestCaseMapper;

    @Resource
    TestCaseMapper testCaseMapper;

    @Resource
    TestPlanTestCaseMapper testPlanTestCaseMapper;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    @Resource
    TestCaseNodeMapper testCaseNodeMapper;

    @Resource
    TestCaseNodeService testCaseNodeService;

    @Resource
    ExtProjectMapper extProjectMapper;

    public void addTestPlan(TestPlan testPlan) {
        if (getTestPlanByName(testPlan.getName()).size() > 0) {
            MSException.throwException(Translator.get("plan_name_already_exists"));
        };
        testPlan.setId(UUID.randomUUID().toString());
        testPlan.setStatus(TestPlanStatus.Prepare.name());
        testPlan.setCreateTime(System.currentTimeMillis());
        testPlan.setUpdateTime(System.currentTimeMillis());
        testPlanMapper.insert(testPlan);
    }

    public List<TestPlan> getTestPlanByName(String name) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andWorkspaceIdEqualTo(SessionUtils.getCurrentWorkspaceId())
                .andNameEqualTo(name);
        return testPlanMapper.selectByExample(example);
    }

    public TestPlan getTestPlan(String testPlanId) {
        return testPlanMapper.selectByPrimaryKey(testPlanId);
    }

    public int editTestPlan(TestPlan testPlan) {
        testPlan.setUpdateTime(System.currentTimeMillis());
        checkTestPlanExist(testPlan);
        return testPlanMapper.updateByPrimaryKeySelective(testPlan);
    }

    private void checkTestPlanExist (TestPlan testPlan) {
        if (testPlan.getName() != null) {
            TestPlanExample example = new TestPlanExample();
            example.createCriteria()
                    .andNameEqualTo(testPlan.getName())
                    .andWorkspaceIdEqualTo(SessionUtils.getCurrentWorkspaceId())
                    .andIdNotEqualTo(testPlan.getId());
            if (testPlanMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("plan_name_already_exists"));
            }
        }
    }

    public int deleteTestPlan(String planId) {
        deleteTestCaseByPlanId(planId);
        return testPlanMapper.deleteByPrimaryKey(planId);
    }

    public void deleteTestCaseByPlanId(String testPlanId) {
        TestPlanTestCaseExample testPlanTestCaseExample = new TestPlanTestCaseExample();
        testPlanTestCaseExample.createCriteria().andPlanIdEqualTo(testPlanId);
        testPlanTestCaseMapper.deleteByExample(testPlanTestCaseExample);
    }

    public List<TestPlanDTO> listTestPlan(QueryTestPlanRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extTestPlanMapper.list(request);
    }

    public void testPlanRelevance(PlanCaseRelevanceRequest request) {

        List<String> testCaseIds = request.getTestCaseIds();

        if (testCaseIds.isEmpty()) {
            return;
        }

        TestCaseExample testCaseExample = new TestCaseExample();
        testCaseExample.createCriteria().andIdIn(testCaseIds);

        Map<String, TestCaseWithBLOBs> testCaseMap =
                testCaseMapper.selectByExampleWithBLOBs(testCaseExample)
                .stream()
                .collect(Collectors.toMap(TestCase::getId, testcase -> testcase));

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanTestCaseMapper batchMapper = sqlSession.getMapper(TestPlanTestCaseMapper.class);

        if (!testCaseIds.isEmpty()) {
            testCaseIds.forEach(caseId -> {
                TestCaseWithBLOBs testCase = testCaseMap.get(caseId);
                TestPlanTestCaseWithBLOBs testPlanTestCase = new TestPlanTestCaseWithBLOBs();
                testPlanTestCase.setId(UUID.randomUUID().toString());
                testPlanTestCase.setExecutor(SessionUtils.getUser().getId());
                testPlanTestCase.setCaseId(caseId);
                testPlanTestCase.setCreateTime(System.currentTimeMillis());
                testPlanTestCase.setUpdateTime(System.currentTimeMillis());
                testPlanTestCase.setPlanId(request.getPlanId());
                testPlanTestCase.setStatus(TestPlanStatus.Prepare.name());
                testPlanTestCase.setResults(testCase.getSteps());
                batchMapper.insert(testPlanTestCase);
            });
        }

        sqlSession.flushStatements();

        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getPlanId());
        if (StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Prepare.name())
                || StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Completed.name())) {
            testPlan.setStatus(TestPlanStatus.Underway.name());
            testPlanMapper.updateByPrimaryKey(testPlan);
        }
    }

    public List<TestPlan> recentTestPlans(String currentWorkspaceId) {
        if (StringUtils.isBlank(currentWorkspaceId)) {
            return null;
        }
        TestPlanExample testPlanTestCaseExample = new TestPlanExample();
        testPlanTestCaseExample.createCriteria().andWorkspaceIdEqualTo(currentWorkspaceId);
        testPlanTestCaseExample.setOrderByClause("update_time desc");
        return testPlanMapper.selectByExample(testPlanTestCaseExample);
    }

    public List<TestPlan> listTestAllPlan(String currentWorkspaceId) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andWorkspaceIdEqualTo(currentWorkspaceId);
        return testPlanMapper.selectByExample(testPlanExample);
    }

    public List<TestPlanDTOWithMetric> listRelateAllPlan() {
        SessionUser user = SessionUtils.getUser();

        QueryTestPlanRequest request =  new QueryTestPlanRequest();
        request.setPrincipal(user.getId());
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        request.setPlanIds(extTestPlanTestCaseMapper.findRelateTestPlanId(user.getId(), SessionUtils.getCurrentWorkspaceId()));

        List<String> projectIds = extProjectMapper.getProjectIdByWorkspaceId(SessionUtils.getCurrentOrganizationId());

        List<TestPlanDTOWithMetric> testPlans = extTestPlanMapper.listRelate(request);

        Map<String, List<TestPlanCaseDTO>> testCaseMap = new HashMap<>();
        listTestCaseByProjectIds(projectIds).forEach(testCase -> {
            List<TestPlanCaseDTO> list = testCaseMap.get(testCase.getPlanId());
            if (list == null) {
                list = new ArrayList<>();
                list.add(testCase);
                testCaseMap.put(testCase.getPlanId(), list);
            } else {
                list.add(testCase);
            }
        });

        testPlans.forEach(testPlan -> {
            List<TestPlanCaseDTO> testCases = testCaseMap.get(testPlan.getId());
            testPlan.setTested(0);
            testPlan.setPassed(0);
            testPlan.setTotal(0);
            if (testCases != null) {
                testPlan.setTotal(testCases.size());
                testCases.forEach(testCase -> {
                    if (!StringUtils.equals(testCase.getStatus(), TestPlanTestCaseStatus.Prepare.name())
                            && !StringUtils.equals(testCase.getStatus(), TestPlanTestCaseStatus.Underway.name())) {
                        testPlan.setTested(testPlan.getTested() + 1);
                        if (StringUtils.equals(testCase.getStatus(), TestPlanTestCaseStatus.Pass.name())) {
                            testPlan.setPassed(testPlan.getPassed() + 1);
                        }
                    }
                });
            }
            testPlan.setPassRate(getPercentWithTwoDecimals(testPlan.getTested() == 0 ? 0 : testPlan.getPassed()*1.0/testPlan.getTested()));
            testPlan.setTestRate(getPercentWithTwoDecimals(testPlan.getTotal() == 0 ? 0 : testPlan.getTested()*1.0/testPlan.getTotal()));
        });

        return testPlans;
    }

    private double getPercentWithTwoDecimals(double value) {
        return new BigDecimal(value * 100)
                .setScale(1, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

    public List<TestPlanCaseDTO> listTestCaseByPlanId(String planId) {
        QueryTestPlanCaseRequest request = new QueryTestPlanCaseRequest();
        request.setPlanId(planId);
        return extTestPlanTestCaseMapper.list(request);
    }

    public List<TestPlanCaseDTO> listTestCaseByProjectIds(List<String> projectIds) {
        QueryTestPlanCaseRequest request = new QueryTestPlanCaseRequest();
        request.setProjectIds(projectIds);
        return extTestPlanTestCaseMapper.list(request);
    }

    public TestCaseReportMetricDTO getMetric(String planId) {

        QueryTestPlanRequest queryTestPlanRequest = new QueryTestPlanRequest();
        queryTestPlanRequest.setId(planId);
        TestPlanDTO testPlan = extTestPlanMapper.list(queryTestPlanRequest).get(0);

        Set<String> executors = new HashSet<>();
        Map<String, TestCaseReportStatusResultDTO> reportStatusResultMap = new HashMap<>();

        TestCaseNodeExample testCaseNodeExample = new TestCaseNodeExample();
        testCaseNodeExample.createCriteria().andProjectIdEqualTo(testPlan.getProjectId());
        List<TestCaseNode> nodes = testCaseNodeMapper.selectByExample(testCaseNodeExample);

        List<TestCaseNodeDTO> nodeTrees = testCaseNodeService.getNodeTrees(nodes);

        Map<String, Set<String>> childIdMap = new HashMap<>();
        nodeTrees.forEach(item -> {
            Set<String> childIds = new HashSet<>();
            getChildIds(item, childIds);
            childIdMap.put(item.getId(), childIds);
        });

        List<TestPlanCaseDTO> testPlanTestCases = listTestCaseByPlanId(planId);

        Map<String, TestCaseReportModuleResultDTO> moduleResultMap = new HashMap<>();

        for (TestPlanCaseDTO testCase: testPlanTestCases) {
            executors.add(testCase.getExecutor());
            getStatusResultMap(reportStatusResultMap, testCase);
            getModuleResultMap(childIdMap, moduleResultMap, testCase, nodeTrees);
        }

        nodeTrees.forEach(rootNode -> {
            TestCaseReportModuleResultDTO moduleResult = moduleResultMap.get(rootNode.getId());
            if (moduleResult != null) {
                moduleResult.setModuleName(rootNode.getName());
            }
        });

        for (TestCaseReportModuleResultDTO moduleResult : moduleResultMap.values()) {
            moduleResult.setPassRate(getPercentWithTwoDecimals(moduleResult.getPassCount()*1.0f/moduleResult.getCaseCount()));
            if (moduleResult.getCaseCount() <= 0) {
                moduleResultMap.remove(moduleResult.getModuleId());
            }
        }

        TestCaseReportMetricDTO testCaseReportMetricDTO = new TestCaseReportMetricDTO();
        testCaseReportMetricDTO.setProjectName(testPlan.getProjectName());
        testCaseReportMetricDTO.setPrincipal(testPlan.getPrincipal());
        testCaseReportMetricDTO.setExecutors(new ArrayList<>(executors));
        testCaseReportMetricDTO.setExecuteResult(new ArrayList<>(reportStatusResultMap.values()));
        testCaseReportMetricDTO.setModuleExecuteResult(new ArrayList<>(moduleResultMap.values()));

        return testCaseReportMetricDTO;
    }

    private void getStatusResultMap(Map<String, TestCaseReportStatusResultDTO> reportStatusResultMap, TestPlanCaseDTO testCase) {
        TestCaseReportStatusResultDTO statusResult = reportStatusResultMap.get(testCase.getStatus());
        if (statusResult == null) {
            statusResult = new TestCaseReportStatusResultDTO();
            statusResult.setStatus(testCase.getStatus());
            statusResult.setCount(0);
        }
        statusResult.setCount(statusResult.getCount() + 1);
        reportStatusResultMap.put(testCase.getStatus(), statusResult);
    }

    private void getModuleResultMap(Map<String, Set<String>> childIdMap, Map<String, TestCaseReportModuleResultDTO> moduleResultMap, TestPlanCaseDTO testCase, List<TestCaseNodeDTO> nodeTrees) {
        childIdMap.forEach((rootNodeId, childIds) -> {
            if (childIds.contains(testCase.getNodeId())) {
                TestCaseReportModuleResultDTO moduleResult = moduleResultMap.get(rootNodeId);
                if (moduleResult == null) {
                    moduleResult = new TestCaseReportModuleResultDTO();
                    moduleResult.setCaseCount(0);
                    moduleResult.setPassCount(0);
                    moduleResult.setIssuesCount(0);
                    moduleResult.setModuleId(rootNodeId);
                }
                moduleResult.setCaseCount(moduleResult.getCaseCount() + 1);
                if (StringUtils.equals(testCase.getStatus(), TestPlanTestCaseStatus.Pass.name())) {
                    moduleResult.setPassCount(moduleResult.getPassCount() + 1);
                }
                if (StringUtils.isNotBlank(testCase.getIssues())) {
                    if (JSON.parseObject(testCase.getIssues()).getBoolean("hasIssues")) {
                        moduleResult.setIssuesCount(moduleResult.getIssuesCount() + 1);
                    };
                }
                moduleResultMap.put(rootNodeId, moduleResult);
                return;
            }
        });
    }

    private void getChildIds(TestCaseNodeDTO rootNode, Set<String> childIds) {

        childIds.add(rootNode.getId());

        List<TestCaseNodeDTO> children = rootNode.getChildren();

        if(children != null) {
            Iterator<TestCaseNodeDTO> iterator = children.iterator();
            while(iterator.hasNext()){
                getChildIds(iterator.next(), childIds);
            }
        }
    }

    public List<TestPlan> getTestPlanByIds(List<String> planIds) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andIdIn(planIds);
        return testPlanMapper.selectByExample(example);
    }

    public void editTestPlanStatus(String planId) {
        List<String> statusList = extTestPlanTestCaseMapper.getStatusByPlanId(planId);
        TestPlan testPlan = new TestPlan();
        testPlan.setId(planId);

        for (String status: statusList){
            if (StringUtils.equals(status, TestPlanTestCaseStatus.Prepare.name())
                    || StringUtils.equals(status, TestPlanTestCaseStatus.Underway.name())) {
                testPlan.setStatus(TestPlanStatus.Underway.name());
                testPlanMapper.updateByPrimaryKeySelective(testPlan);
                return;
            }
        }
        testPlan.setStatus(TestPlanStatus.Completed.name());
        testPlanMapper.updateByPrimaryKeySelective(testPlan);
    }
}
