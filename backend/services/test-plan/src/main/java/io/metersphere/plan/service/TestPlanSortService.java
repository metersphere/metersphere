package io.metersphere.plan.service;

import io.metersphere.project.service.MoveNodeService;
import io.metersphere.project.utils.NodeSortUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//测试计划关联表 通用方法
@Service
@Transactional(rollbackFor = Exception.class)
public abstract class TestPlanSortService extends MoveNodeService {

    protected static final long DEFAULT_NODE_INTERVAL_POS = NodeSortUtils.DEFAULT_NODE_INTERVAL_POS;

    public abstract void updatePos(String id, long pos);

    public abstract void refreshPos(String testPlanId);

    private static final String MOVE_POS_OPERATOR_LESS = "lessThan";
    private static final String MOVE_POS_OPERATOR_MORE = "moreThan";
    private static final String DRAG_NODE_NOT_EXIST = "drag_node.not.exist";

}
