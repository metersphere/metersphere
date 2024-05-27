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

    /**
     * 构建节点排序的参数
     *
     * @param request           拖拽的前端请求参数
     * @param selectIdNodeFunc  通过id查询节点的函数
     * @param selectPosNodeFunc 通过parentId和pos运算符查询节点的函数
     * @return
     */


    //    public MoveNodeSortDTO getNodeSortDTO(NodeMoveRequest request, String testPlanId, Function<String, DropNode> selectIdNodeFunc, Function<NodeSortQueryParam, DropNode> selectPosNodeFunc) {
    //
    //
    //        if (StringUtils.equals(request.getDragNodeId(), request.getDropNodeId())) {
    //            //两种节点不能一样
    //            throw new MSException(Translator.get("invalid_parameter") + ": drag node  and drop node");
    //        }
    //
    //        DropNode dragNode = selectIdNodeFunc.apply(request.getDragNodeId());
    //        if (dragNode == null) {
    //            throw new MSException(Translator.get(DRAG_NODE_NOT_EXIST) + ":" + request.getDragNodeId());
    //        }
    //
    //        DropNode dropNode = selectIdNodeFunc.apply(request.getDropNodeId());
    //        if (dropNode == null) {
    //            throw new MSException(Translator.get(DRAG_NODE_NOT_EXIST) + ":" + request.getDropNodeId());
    //        }
    //
    //        DropNode previousNode;
    //        DropNode nextNode;
    //
    //        if (request.getDropPosition() == 1) {
    //            //dropPosition=1: 放到dropNode节点后，原dropNode后面的节点之前
    //            previousNode = dropNode;
    //
    //            NodeSortQueryParam sortParam = new NodeSortQueryParam();
    //            sortParam.setParentId(testPlanId);
    //            sortParam.setPos(previousNode.getPos());
    //            sortParam.setOperator(MOVE_POS_OPERATOR_MORE);
    //            nextNode = selectPosNodeFunc.apply(sortParam);
    //        } else if (request.getDropPosition() == -1) {
    //            //dropPosition=-1: 放到dropNode节点前，原dropNode前面的节点之后
    //            nextNode = dropNode;
    //            NodeSortQueryParam sortParam = new NodeSortQueryParam();
    //            sortParam.setPos(nextNode.getPos());
    //            sortParam.setParentId(testPlanId);
    //            sortParam.setOperator(MOVE_POS_OPERATOR_LESS);
    //            previousNode = selectPosNodeFunc.apply(sortParam);
    //        } else {
    //            throw new MSException(Translator.get("invalid_parameter") + ": dropPosition");
    //        }
    //        return new MoveNodeSortDTO(testPlanId, dragNode, previousNode, nextNode);
    //    }

}
