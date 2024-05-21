package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.dto.request.TestPlanBatchProcessRequest;
import io.metersphere.plan.dto.request.TestPlanBatchRequest;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.sdk.constants.TestPlanConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanBatchMoveService extends TestPlanBaseUtilsService {

    @Resource
    private ExtTestPlanMapper extTestPlanMapper;

    public void batchMove(Map<String, List<TestPlan>> plans, TestPlanBatchRequest request, String userId) {
        batchMoveGroup(plans, request, userId);
        batchMovePlan(plans, request, userId);
    }

    /**
     * 批量移动组
     *
     * @param plans
     */
    private void batchMoveGroup(Map<String, List<TestPlan>> plans, TestPlanBatchProcessRequest request, String userId) {
        //TODO 批量移动计划组
    }


    /**
     * 批量移动计划
     *
     * @param plans
     */
    private void batchMovePlan(Map<String, List<TestPlan>> plans, TestPlanBatchRequest request, String userId) {
        if (plans.containsKey(TestPlanConstants.TEST_PLAN_TYPE_PLAN)) {
            List<TestPlan> testPlans = plans.get(TestPlanConstants.TEST_PLAN_TYPE_PLAN);
            testPlans.forEach(testPlan -> {
                testPlan.setModuleId(request.getModuleId());
                //        5.21，查询需求文档、测试用例：测试计划名称允许重复
                //                validateTestPlan(testPlan);
            });
            List<String> ids = testPlans.stream().map(TestPlan::getId).collect(Collectors.toList());
            extTestPlanMapper.batchMove(ids, request.getModuleId(), userId, System.currentTimeMillis());

        }
    }

}
