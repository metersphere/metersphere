package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanExample;
import io.metersphere.plan.dto.request.TestPlanBatchProcessRequest;
import io.metersphere.plan.dto.request.TestPlanBatchRequest;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanBatchArchivedService extends TestPlanBaseUtilsService {

    @Resource
    private TestPlanMapper testPlanMapper;

    public void batchArchived(Map<String, List<TestPlan>> plans, TestPlanBatchRequest request, String userId) {
        int affectedGroupPlanCount = batchArchivedGroup(plans, request, userId);
        int affectedPlanCount = batchArchivedPlan(plans, request, userId);
        if (affectedGroupPlanCount <= 0 && affectedPlanCount <= 0) {
            // 暂无可归档的计划
            throw new MSException("");
        }
    }

    /**
     * 批量移动组
     *
     * @param plans
     */
    private int batchArchivedGroup(Map<String, List<TestPlan>> plans, TestPlanBatchProcessRequest request, String userId) {
        //TODO 批量归档计划组
        return 0;
    }

    /**
     * 批量移动计划
     *
     * @param plans 归档测试计划集合
     */
    private int batchArchivedPlan(Map<String, List<TestPlan>> plans, TestPlanBatchRequest request, String userId) {
        if (plans.containsKey(TestPlanConstants.TEST_PLAN_TYPE_PLAN)) {
            List<TestPlan> testPlans = plans.get(TestPlanConstants.TEST_PLAN_TYPE_PLAN);
            testPlans.forEach(testPlan -> {
                testPlan.setModuleId(request.getModuleId());
                validateTestPlan(testPlan);
            });
            List<String> ids = testPlans.stream().map(TestPlan::getId).collect(Collectors.toList());
            TestPlan record = new TestPlan();
            record.setStatus(TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED);
            record.setUpdateUser(userId);
            record.setUpdateTime(System.currentTimeMillis());
            TestPlanExample example = new TestPlanExample();
            example.createCriteria().andIdIn(ids);
            return testPlanMapper.updateByExampleSelective(record, example);
        } else {
            return 0;
        }
    }
}
