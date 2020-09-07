package io.metersphere.track.service;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtProjectMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanTestCaseMapper;
import io.metersphere.commons.constants.TestPlanStatus;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.MathUtils;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.track.Factory.ReportComponentFactory;
import io.metersphere.track.domain.ReportComponent;
import io.metersphere.track.dto.TestCaseReportMetricDTO;
import io.metersphere.track.dto.TestPlanCaseDTO;
import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.dto.TestPlanDTOWithMetric;
import io.metersphere.track.request.testcase.PlanCaseRelevanceRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import io.metersphere.track.request.testplan.AddTestPlanRequest;
import io.metersphere.track.request.testplancase.QueryTestPlanCaseRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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

    @Lazy
    @Resource
    TestPlanTestCaseService testPlanTestCaseService;

    @Resource
    ExtProjectMapper extProjectMapper;

    @Resource
    TestCaseReportMapper testCaseReportMapper;

    @Resource
    TestPlanProjectMapper testPlanProjectMapper;
    @Resource
    TestPlanProjectService testPlanProjectService;
    @Resource
    ProjectMapper projectMapper;

    public void addTestPlan(AddTestPlanRequest testPlan) {
        if (getTestPlanByName(testPlan.getName()).size() > 0) {
            MSException.throwException(Translator.get("plan_name_already_exists"));
        }

        String testPlanId = UUID.randomUUID().toString();

        List<String> projectIds = testPlan.getProjectIds();
        projectIds.forEach(id -> {
            TestPlanProject testPlanProject = new TestPlanProject();
            testPlanProject.setProjectId(id);
            testPlanProject.setTestPlanId(testPlanId);
            testPlanProjectMapper.insertSelective(testPlanProject);
        });

        testPlan.setId(testPlanId);
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
        return Optional.ofNullable(testPlanMapper.selectByPrimaryKey(testPlanId)).orElse(new TestPlan());
    }

    public int editTestPlan(TestPlan testPlan) {
        testPlan.setUpdateTime(System.currentTimeMillis());
        checkTestPlanExist(testPlan);
        return testPlanMapper.updateByPrimaryKeySelective(testPlan);
    }

    private void checkTestPlanExist(TestPlan testPlan) {
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

        QueryTestPlanRequest request = new QueryTestPlanRequest();
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
            testPlan.setPassRate(MathUtils.getPercentWithDecimal(testPlan.getTested() == 0 ? 0 : testPlan.getPassed() * 1.0 / testPlan.getTested()));
            testPlan.setTestRate(MathUtils.getPercentWithDecimal(testPlan.getTotal() == 0 ? 0 : testPlan.getTested() * 1.0 / testPlan.getTotal()));
        });

        return testPlans;
    }

    public List<TestPlanCaseDTO> listTestCaseByPlanId(String planId) {
        QueryTestPlanCaseRequest request = new QueryTestPlanCaseRequest();
        request.setPlanId(planId);
        return testPlanTestCaseService.list(request);
    }

    public List<TestPlanCaseDTO> listTestCaseByProjectIds(List<String> projectIds) {
        QueryTestPlanCaseRequest request = new QueryTestPlanCaseRequest();
        request.setProjectIds(projectIds);
        return extTestPlanTestCaseMapper.list(request);
    }

    public TestCaseReportMetricDTO getMetric(String planId) {
        IssuesService issuesService = (IssuesService) CommonBeanFactory.getBean("issuesService");
        QueryTestPlanRequest queryTestPlanRequest = new QueryTestPlanRequest();
        queryTestPlanRequest.setId(planId);

        TestPlanDTO testPlan = extTestPlanMapper.list(queryTestPlanRequest).get(0);
        TestCaseReport testCaseReport = testCaseReportMapper.selectByPrimaryKey(testPlan.getReportId());
        JSONObject content = JSONObject.parseObject(testCaseReport.getContent());
        JSONArray componentIds = content.getJSONArray("components");

        List<ReportComponent> components = ReportComponentFactory.createComponents(componentIds.toJavaList(String.class), testPlan);

        List<TestPlanCaseDTO> testPlanTestCases = listTestCaseByPlanId(planId);
        List<Issues> issues = new ArrayList<>();
        for (TestPlanCaseDTO testCase : testPlanTestCases) {
            List<Issues> issue = issuesService.getIssues(testCase.getCaseId());
            if (issue.size() > 0) {
                for (Issues i : issue) {
                    i.setModel(testCase.getNodePath());
                    String des = i.getDescription().replaceAll("<p>", "").replaceAll("</p>", "");
                    i.setDescription(des);
                    if (i.getLastmodify() == null || i.getLastmodify() == "") {
                        if (i.getReporter() != null || i.getReporter() != "") {
                            i.setLastmodify(i.getReporter());
                        }
                    }
                }
                issues.addAll(issue);
                Collections.sort(issues, Comparator.comparing(Issues::getCreateTime, (t1, t2) -> t2.compareTo(t1)));
            }

            components.forEach(component -> {
                component.readRecord(testCase);
            });
        }
        TestCaseReportMetricDTO testCaseReportMetricDTO = new TestCaseReportMetricDTO();
        components.forEach(component -> {
            component.afterBuild(testCaseReportMetricDTO);
        });
        testCaseReportMetricDTO.setIssues(issues);
        return testCaseReportMetricDTO;
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

        for (String status : statusList) {
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

    public String getProjectNameByPlanId(String testPlanId) {
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(testPlanId);
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andIdIn(projectIds);

        List<Project> projects = projectMapper.selectByExample(projectExample);
        StringBuilder stringBuilder = new StringBuilder();
        String projectName = "";

        if (projects.size() > 0) {
            for (Project project : projects) {
                stringBuilder.append(project.getName()).append("、");
            }
            projectName = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
        }

        return projectName;
    }
}
