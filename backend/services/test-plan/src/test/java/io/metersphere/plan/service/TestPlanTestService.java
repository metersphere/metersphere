package io.metersphere.plan.service;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.mapper.FunctionalCaseMapper;
import io.metersphere.plan.domain.*;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanFunctionalCaseMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.*;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.springframework.stereotype.Service;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestPlanTestService {
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;
    @Resource
    private FunctionalCaseMapper functionalCaseMapper;
    @Resource
    private TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;

    public TestPlan selectTestPlanByName(String name) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andNameEqualTo(name);
        TestPlan testPlan = testPlanMapper.selectByExample(testPlanExample).get(0);
        return testPlan;
    }
    public void checkTestPlanByAddTest() {
        /*
        抽查：
            testPlan_13没有设置计划开始时间、没有设置重复添加用例和自动更新状态、阈值为100、描述为空；
            testPlan_53检查是否设置了计划开始结束时间；
            testPlan_123是否设置了重复添加用例和自动更新状态；
            testPlan_173的阈值是否不等于100、描述不会为空
         */
        TestPlan testPlan = selectTestPlanByName("testPlan_13");
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        Assertions.assertNull(testPlan.getPlannedStartTime());
        Assertions.assertNull(testPlan.getPlannedEndTime());
        Assertions.assertEquals(testPlanConfig.getPassThreshold(), 100);
        Assertions.assertEquals(testPlanConfig.getRepeatCase(), false);
        Assertions.assertEquals(testPlanConfig.getAutomaticStatusUpdate(), false);
        Assertions.assertNull(testPlan.getDescription());

        testPlan = selectTestPlanByName("testPlan_53");
        testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        Assertions.assertNotNull(testPlan.getPlannedStartTime());
        Assertions.assertNotNull(testPlan.getPlannedEndTime());
        Assertions.assertEquals(testPlanConfig.getPassThreshold(), 100);
        Assertions.assertEquals(testPlanConfig.getRepeatCase(), false);
        Assertions.assertEquals(testPlanConfig.getAutomaticStatusUpdate(), false);
        Assertions.assertNull(testPlan.getDescription());

        testPlan = selectTestPlanByName("testPlan_123");
        testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        Assertions.assertNull(testPlan.getPlannedStartTime());
        Assertions.assertNull(testPlan.getPlannedEndTime());
        Assertions.assertEquals(testPlanConfig.getPassThreshold(), 100);
        Assertions.assertEquals(testPlanConfig.getRepeatCase(), true);
        Assertions.assertEquals(testPlanConfig.getAutomaticStatusUpdate(), true);
        Assertions.assertNull(testPlan.getDescription());

        testPlan = selectTestPlanByName("testPlan_173");
        testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        Assertions.assertNull(testPlan.getPlannedStartTime());
        Assertions.assertNull(testPlan.getPlannedEndTime());
        Assertions.assertNotEquals(testPlanConfig.getPassThreshold(), 100);
        Assertions.assertEquals(testPlanConfig.getRepeatCase(), false);
        Assertions.assertEquals(testPlanConfig.getAutomaticStatusUpdate(), false);
        Assertions.assertNotNull(testPlan.getDescription());
    }

    public void updateTestPlanTypeToGroup(String[] testPlanIds) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andIdIn(List.of(testPlanIds));
        TestPlan testPlan = new TestPlan();
        testPlan.setType(TestPlanConstants.TEST_PLAN_TYPE_GROUP);
        testPlanMapper.updateByExampleSelective(testPlan, testPlanExample);
    }

    public List<TestPlan> selectByProjectIdAndNames(String projectId, String[] strings) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andProjectIdEqualTo(projectId).andNameIn(List.of(strings));
        return testPlanMapper.selectByExample(testPlanExample);
    }

    public boolean checkDataCount(String projectId, int finalCount) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andProjectIdEqualTo(projectId);
        return testPlanMapper.countByExample(testPlanExample) == finalCount;
    }

    public long countByModuleId(String id) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andModuleIdEqualTo(id);
        return testPlanMapper.countByExample(testPlanExample);
    }

    public List<FunctionalCase> createFunctionCase(int caseNums, String projectId) {
        List<FunctionalCase> returnList = new ArrayList<>();
        for (int i = 0; i < caseNums; i++) {
            FunctionalCase functionalCase = new FunctionalCase();
            functionalCase.setId(IDGenerator.nextStr());
            functionalCase.setProjectId(projectId);
            functionalCase.setNum(NumGenerator.nextNum(projectId, ApplicationNumScope.CASE_MANAGEMENT));
            functionalCase.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
            functionalCase.setName("function_" + projectId + "_" + i);
            functionalCase.setReviewStatus("UN_REVIEWED");
            functionalCase.setPos((long) (i * 64));
            functionalCase.setRefId(functionalCase.getId());
            functionalCase.setLastExecuteResult(FunctionalCaseExecuteResult.UN_EXECUTED.name());
            functionalCase.setLatest(true);
            functionalCase.setCreateUser("admin");
            functionalCase.setCreateTime(System.currentTimeMillis());
            functionalCase.setUpdateTime(System.currentTimeMillis());
            functionalCase.setVersionId("v6.6.6");
            functionalCase.setTemplateId("none");
            functionalCase.setCaseEditType("step");
            functionalCase.setDeleted(false);
            functionalCase.setPublicCase(false);
            returnList.add(functionalCase);
        }
        functionalCaseMapper.batchInsert(returnList);
        return returnList;
    }

    public long countResource(String id, String resourceFunctionalCase) {
        if (StringUtils.equals(TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE, resourceFunctionalCase)) {
            TestPlanFunctionalCaseExample example = new TestPlanFunctionalCaseExample();
            example.createCriteria().andTestPlanIdEqualTo(id);
            return testPlanFunctionalCaseMapper.countByExample(example);
        }
        return 0;
    }

    public List<TestPlanFunctionalCase> selectTestPlanFunctionalCaseByTestPlanId(String testPlanId) {
        TestPlanFunctionalCaseExample example = new TestPlanFunctionalCaseExample();
        example.createCriteria().andTestPlanIdEqualTo(testPlanId);
        example.setOrderByClause(" pos asc ");
        return testPlanFunctionalCaseMapper.selectByExample(example);
    }
}
