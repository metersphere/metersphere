package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanExample;
import io.metersphere.plan.dto.request.TestPlanBatchProcessRequest;
import io.metersphere.plan.dto.request.TestPlanBatchRequest;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
        int affectedPlanCount = batchArchivedPlan(plans, userId);
        if (affectedGroupPlanCount <= 0 && affectedPlanCount <= 0) {
            // 暂无可归档的计划
            throw new MSException(Translator.get("no_plan_to_archive"));
        }
    }

    /**
     * 批量归档组
     *
     * @param planGroups 计划组
     */
    private int batchArchivedGroup(Map<String, List<TestPlan>> planGroups, TestPlanBatchProcessRequest request, String userId) {
        //TODO 批量归档计划组
        return 0;
    }

    /**
     * 批量归档计划
     *
     * @param plans 归档测试计划集合
     */
    private int batchArchivedPlan(Map<String, List<TestPlan>> plans, String userId) {
        if (plans.containsKey(TestPlanConstants.TEST_PLAN_TYPE_PLAN)) {
            List<TestPlan> testPlans = plans.get(TestPlanConstants.TEST_PLAN_TYPE_PLAN);
            List<String> ids = testPlans.stream().filter(plan -> StringUtils.equals(plan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_COMPLETED))
                    .map(TestPlan::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ids)) {
                return 0;
            }
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
