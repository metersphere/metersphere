package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanExample;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.project.dto.MoveNodeSortDTO;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.enums.MoveTypeEnum;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanGroupService extends TestPlanSortService {

    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;

    private static final int MAX_CHILDREN_COUNT = 20;
    
    @Override
    public long getNextOrder(String groupId) {
        long maxPos = extTestPlanMapper.selectMaxPosByGroupId(groupId);
        return maxPos + ServiceUtils.POS_STEP;
    }

    @Override
    public void updatePos(String id, long pos) {
        TestPlan testPlan = new TestPlan();
        testPlan.setId(id);
        testPlan.setPos(pos);
        testPlanMapper.updateByPrimaryKeySelective(testPlan);
    }

    @Override
    public void refreshPos(String groupId) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.createCriteria().andGroupIdEqualTo(groupId);
        testPlanExample.setOrderByClause("pos asc");
        List<TestPlan> testPlans = testPlanMapper.selectByExample(testPlanExample);
        long pos = 1;
        for (TestPlan testPlanItem : testPlans) {
            this.updatePos(testPlanItem.getId(), pos * ServiceUtils.POS_STEP);
            pos++;
        }
    }

    public void sort(PosRequest request) {
        TestPlan dropPlan = testPlanMapper.selectByPrimaryKey(request.getMoveId());
        TestPlan targetPlan = testPlanMapper.selectByPrimaryKey(request.getTargetId());

        // 校验排序的参数 （暂时不支持测试计划的移入移出）
        validateMoveRequest(dropPlan, targetPlan, request.getMoveMode());
        MoveNodeSortDTO sortDTO = super.getNodeSortDTO(
                targetPlan.getGroupId(),
                this.getNodeMoveRequest(request, false),
                extTestPlanMapper::selectDragInfoById,
                extTestPlanMapper::selectNodeByPosOperator
        );

        this.sort(sortDTO);
    }

    private void validateMoveRequest(TestPlan dropPlan, TestPlan targetPlan, String moveType) {
        //测试计划组不能进行移动操作
        if (dropPlan == null || StringUtils.equalsIgnoreCase(dropPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
            throw new MSException(Translator.get("test_plan.drag.node.error"));
        }
        if (targetPlan == null || StringUtils.equalsIgnoreCase(targetPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            throw new MSException(Translator.get("test_plan.drag.node.error"));
        }
        if (StringUtils.equalsIgnoreCase(MoveTypeEnum.APPEND.name(), moveType)) {
            if (!StringUtils.equalsIgnoreCase(targetPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
                throw new MSException(Translator.get("test_plan.drag.node.error"));
            }
        } else if (StringUtils.equalsAnyIgnoreCase(moveType, MoveTypeEnum.BEFORE.name(), MoveTypeEnum.AFTER.name())) {
            if (StringUtils.equalsAny(TestPlanConstants.TEST_PLAN_TYPE_GROUP, dropPlan.getType())) {
                throw new MSException(Translator.get("test_plan.drag.node.error"));
            }
        } else {
            throw new MSException(Translator.get("test_plan.drag.position.error"));
        }
    }

    public void validateGroupCapacity(String groupId, int size) {
        if (!StringUtils.equals(groupId, TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            // 判断测试计划组是否存在
            TestPlan groupPlan = testPlanMapper.selectByPrimaryKey(groupId);
            if (StringUtils.equalsIgnoreCase(groupPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
                throw new MSException(Translator.get("test_plan.group.error"));
            }
            //判断并未归档
            if (StringUtils.equalsIgnoreCase(groupPlan.getStatus(), TestPlanConstants.TEST_PLAN_STATUS_ARCHIVED)) {
                throw new MSException(Translator.get("test_plan.group.error"));
            }
            //判断测试计划组下的测试计划数量是否超过20
            TestPlanExample example = new TestPlanExample();
            example.createCriteria().andGroupIdEqualTo(groupId);
            if (testPlanMapper.countByExample(example) + size > 20) {
                throw new MSException(Translator.getWithArgs("test_plan.group.children.max", MAX_CHILDREN_COUNT));
            }
        }
    }
}
