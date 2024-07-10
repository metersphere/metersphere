package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanExample;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.project.dto.MoveNodeSortDTO;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
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
    public void refreshPos(String rangeId) {
        TestPlanExample testPlanExample = new TestPlanExample();
        testPlanExample.setOrderByClause("pos asc");
        if (StringUtils.contains(rangeId, "_")) {
            String[] rangeIds = rangeId.split("_");
            String projectId = rangeIds[0];
            String testPlanGroupId = rangeIds[1];
            testPlanExample.createCriteria().andProjectIdEqualTo(projectId).andGroupIdEqualTo(testPlanGroupId);
        } else {
            testPlanExample.createCriteria().andGroupIdEqualTo(rangeId);
        }

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
                this.getNodeMoveRequest(request, true),
                extTestPlanMapper::selectDragInfoById,
                extTestPlanMapper::selectNodeByPosOperator
        );
        if (StringUtils.equalsIgnoreCase(sortDTO.getSortRangeId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            sortDTO.setSortRangeId(request.getProjectId() + "_" + TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);
        }
        //判断是否需要刷新排序
        if (this.needRefreshBeforeSort(sortDTO.getPreviousNode(), sortDTO.getNextNode())) {
            this.refreshPos(sortDTO.getSortRangeId());
            dropPlan = testPlanMapper.selectByPrimaryKey(request.getMoveId());
            targetPlan = testPlanMapper.selectByPrimaryKey(request.getTargetId());
            sortDTO = super.getNodeSortDTO(
                    targetPlan.getGroupId(),
                    this.getNodeMoveRequest(request, true),
                    extTestPlanMapper::selectDragInfoById,
                    extTestPlanMapper::selectNodeByPosOperator
            );
            if (StringUtils.equalsIgnoreCase(sortDTO.getSortRangeId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
                sortDTO.setSortRangeId(request.getProjectId() + "_" + TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID);
            }
        }
        this.sort(sortDTO);
    }

    private void validateMoveRequest(TestPlan dropPlan, TestPlan targetPlan, String moveType) {
        //测试计划组不能进行移动操作
        if (dropPlan == null) {
            throw new MSException(Translator.get("test_plan.drag.node.error"));
        }
        if (targetPlan == null) {
            throw new MSException(Translator.get("test_plan.drag.node.error"));
        }
    }

    public TestPlan validateGroupCapacity(String groupId, int size) {
        // 判断测试计划组是否存在
        TestPlan groupPlan = testPlanMapper.selectByPrimaryKey(groupId);
        if (groupPlan == null) {
            throw new MSException(Translator.get("test_plan.group.error"));
        }
        if (!StringUtils.equalsIgnoreCase(groupPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_GROUP)) {
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
        return groupPlan;
    }
}
