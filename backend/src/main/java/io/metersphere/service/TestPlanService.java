package io.metersphere.service;


import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.TestCaseMapper;
import io.metersphere.base.mapper.TestPlanMapper;
import io.metersphere.base.mapper.TestPlanTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.commons.constants.TestPlanStatus;
import io.metersphere.commons.constants.TestPlanTestCaseStatus;
import io.metersphere.controller.request.testcase.PlanCaseRelevanceRequest;
import io.metersphere.controller.request.testcase.QueryTestCaseRequest;
import io.metersphere.controller.request.testcase.QueryTestPlanRequest;
import io.metersphere.dto.TestPlanDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
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
    TestCaseMapper testCaseMapper;

    @Resource
    TestPlanTestCaseMapper testPlanTestCaseMapper;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    public void addTestPlan(TestPlan testPlan) {
        testPlan.setId(UUID.randomUUID().toString());
        testPlan.setStatus(TestPlanStatus.Prepare.name());
        testPlan.setCreateTime(System.currentTimeMillis());
        testPlan.setUpdateTime(System.currentTimeMillis());
        testPlanMapper.insert(testPlan);
    }


    public List<TestPlan> getTestPlan(String testPlanId) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andIdEqualTo(testPlanId);
        return testPlanMapper.selectByExampleWithBLOBs(testPlanExample);
    }

    public int editTestPlan(TestPlan testPlan) {
        testPlan.setUpdateTime(System.currentTimeMillis());
        return testPlanMapper.updateByPrimaryKeySelective(testPlan);
    }

    public int deleteTestPlan(String testPalnId) {
        return testPlanMapper.deleteByPrimaryKey(testPalnId);
    }

    public List<TestPlanDTO> listTestPlan(QueryTestPlanRequest request) {
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
                TestPlanTestCase testPlanTestCase = new TestPlanTestCase();
                testPlanTestCase.setExecutor(testCase.getMaintainer());
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
        if (StringUtils.equals(testPlan.getStatus(), TestPlanStatus.Prepare.name())) {
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
}
