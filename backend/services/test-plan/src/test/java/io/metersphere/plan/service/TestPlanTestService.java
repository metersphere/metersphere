package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanConfig;
import io.metersphere.plan.domain.TestPlanExample;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.TestPlanConstants;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestPlanTestService {
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;

    public void checkTestPlanByAddTest() {
        /*
        抽查：
            testPlan_13没有设置计划开始时间、没有设置重复添加用例和自动更新状态、阈值为100、描述为空；
            testPlan_53检查是否设置了计划开始结束时间；
            testPlan_123是否设置了重复添加用例和自动更新状态；
            testPlan_173的阈值是否不等于100、描述不会为空
         */
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andNameEqualTo("testPlan_13");
        TestPlan testPlan = testPlanMapper.selectByExample(testPlanExample).get(0);
        TestPlanConfig testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        Assertions.assertNull(testPlan.getPlannedStartTime());
        Assertions.assertNull(testPlan.getPlannedEndTime());
        Assertions.assertEquals(testPlanConfig.getPassThreshold(), 100);
        Assertions.assertEquals(testPlanConfig.getRepeatCase(), false);
        Assertions.assertEquals(testPlanConfig.getAutomaticStatusUpdate(), false);
        Assertions.assertNull(testPlan.getDescription());

        testPlanExample.clear();
        testPlanExample.createCriteria().andNameEqualTo("testPlan_53");
        testPlan = testPlanMapper.selectByExample(testPlanExample).get(0);
        testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        Assertions.assertNotNull(testPlan.getPlannedStartTime());
        Assertions.assertNotNull(testPlan.getPlannedEndTime());
        Assertions.assertEquals(testPlanConfig.getPassThreshold(), 100);
        Assertions.assertEquals(testPlanConfig.getRepeatCase(), false);
        Assertions.assertEquals(testPlanConfig.getAutomaticStatusUpdate(), false);
        Assertions.assertNull(testPlan.getDescription());

        testPlanExample.clear();
        testPlanExample.createCriteria().andNameEqualTo("testPlan_123");
        testPlan = testPlanMapper.selectByExample(testPlanExample).get(0);
        testPlanConfig = testPlanConfigMapper.selectByPrimaryKey(testPlan.getId());
        Assertions.assertNull(testPlan.getPlannedStartTime());
        Assertions.assertNull(testPlan.getPlannedEndTime());
        Assertions.assertEquals(testPlanConfig.getPassThreshold(), 100);
        Assertions.assertEquals(testPlanConfig.getRepeatCase(), true);
        Assertions.assertEquals(testPlanConfig.getAutomaticStatusUpdate(), true);
        Assertions.assertNull(testPlan.getDescription());

        testPlanExample.clear();
        testPlanExample.createCriteria().andNameEqualTo("testPlan_173");
        testPlan = testPlanMapper.selectByExample(testPlanExample).get(0);
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
}
