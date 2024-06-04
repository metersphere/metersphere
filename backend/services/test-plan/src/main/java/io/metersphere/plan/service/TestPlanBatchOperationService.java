package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.dto.response.TestPlanResponse;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.project.utils.NodeSortUtils;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanBatchOperationService extends TestPlanBaseUtilsService {

    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanGroupService testPlanGroupService;

    public void batchMoveModule(List<TestPlan> testPlanList, String moduleId, String userId) {
        List<String> movePlanIds = new ArrayList<>();
        for (TestPlan testPlan : testPlanList) {
            // 已归档的测试计划无法操作
            if (StringUtils.equalsIgnoreCase(testPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
                continue;
            }
            if (!StringUtils.equalsIgnoreCase(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                // 测试计划组下的测试计划不单独处理， 如果勾选了他的测试计划组，会在下面进行逻辑补足。
                continue;
            }

            movePlanIds.add(testPlan.getId());
            if (StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                List<TestPlanResponse> testPlanItemList = extTestPlanMapper.selectByGroupIds(Collections.singletonList(testPlan.getId()));
                for (TestPlanResponse item : testPlanItemList) {
                    movePlanIds.add(item.getId());
                }
            }
        }
        movePlanIds = movePlanIds.stream().distinct().toList();
        batchMovePlan(movePlanIds, moduleId, userId);
    }

    public void batchMoveGroup(List<TestPlan> testPlanList, String groupId, String userId) {
        // 判断测试计划组是否存在
        String groupModuleId = null;
        if (!StringUtils.equalsIgnoreCase(groupId, TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            TestPlan groupPlan = testPlanMapper.selectByPrimaryKey(groupId);
            if (StringUtils.equalsIgnoreCase(groupPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
                throw new MSException(Translator.get("test_plan.group.error"));
            }
            groupModuleId = groupPlan.getModuleId();
        }


        List<String> movePlanIds = new ArrayList<>();
        for (TestPlan testPlan : testPlanList) {
            // 已归档的测试计划无法操作
            if (StringUtils.equalsIgnoreCase(testPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED) || StringUtils.equalsIgnoreCase(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                continue;
            }
            movePlanIds.add(testPlan.getId());
        }

        long nextPos = testPlanGroupService.getNextOrder(groupId);
        long operationTimestamp = System.currentTimeMillis();
        int index = 0;
        for (TestPlan testPlan : testPlanList) {
            TestPlan updatePlan = new TestPlan();
            updatePlan.setId(testPlan.getId());
            updatePlan.setUpdateTime(operationTimestamp);
            updatePlan.setGroupId(groupId);
            updatePlan.setModuleId(StringUtils.isBlank(groupModuleId) ? testPlan.getModuleId() : groupModuleId);
            updatePlan.setPos(nextPos + index * NodeSortUtils.DEFAULT_NODE_INTERVAL_POS);
            updatePlan.setUpdateUser(userId);
            testPlanMapper.updateByPrimaryKeySelective(updatePlan);
        }
    }

    /**
     * 批量移动计划
     */
    private long batchMovePlan(List<String> ids, String moduleId, String userId) {
        if (CollectionUtils.isNotEmpty(ids)) {
            return extTestPlanMapper.batchMove(ids, moduleId, userId, System.currentTimeMillis());
        } else {
            return 0;
        }
    }

}
