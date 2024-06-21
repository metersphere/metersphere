package io.metersphere.project.service;

import io.metersphere.project.dto.DropNode;
import io.metersphere.project.dto.ModuleSortCountResultDTO;
import io.metersphere.project.dto.MoveNodeSortDTO;
import io.metersphere.project.dto.NodeSortQueryParam;
import io.metersphere.project.utils.NodeSortUtils;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.dto.sdk.request.PosRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;
public abstract class MoveNodeService {

    public static final long DEFAULT_NODE_INTERVAL_POS = NodeSortUtils.DEFAULT_NODE_INTERVAL_POS;

    public abstract long getNextOrder(String projectId);

    public abstract void updatePos(String id, long pos);

    public abstract void refreshPos(String testPlanId);


    private static final String MOVE_POS_OPERATOR_LESS = "lessThan";
    private static final String MOVE_POS_OPERATOR_MORE = "moreThan";
    private static final String DRAG_NODE_NOT_EXIST = "drag_node.not.exist";

    /**
     * 构建节点移动的请求参数
     *
     * @param posRequest 拖拽的前端请求参数
     * @param isDesc     是否是降序排列
     */
    public NodeMoveRequest getNodeMoveRequest(PosRequest posRequest, boolean isDesc) {
        NodeMoveRequest request = new NodeMoveRequest();
        request.setDragNodeId(posRequest.getMoveId());
        request.setDropNodeId(posRequest.getTargetId());
        request.setAndConvertDropPosition(posRequest.getMoveMode(), isDesc);
        return request;
    }

    /**
     * 构建节点排序的参数
     *
     * @param sortRangeId       排序范围ID
     * @param request           拖拽的前端请求参数
     * @param selectIdNodeFunc  通过id查询节点的函数
     * @param selectPosNodeFunc 通过parentId和pos运算符查询节点的函数
     */
    public MoveNodeSortDTO getNodeSortDTO(String sortRangeId, NodeMoveRequest request, Function<String, DropNode> selectIdNodeFunc, Function<NodeSortQueryParam, DropNode> selectPosNodeFunc) {
        if (StringUtils.equals(request.getDragNodeId(), request.getDropNodeId())) {
            //两种节点不能一样
            throw new MSException(Translator.get("invalid_parameter") + ": drag node  and drop node");
        }

        DropNode dragNode = selectIdNodeFunc.apply(request.getDragNodeId());
        if (dragNode == null) {
            throw new MSException(Translator.get(DRAG_NODE_NOT_EXIST) + ":" + request.getDragNodeId());
        }

        DropNode dropNode = selectIdNodeFunc.apply(request.getDropNodeId());
        if (dropNode == null) {
            throw new MSException(Translator.get(DRAG_NODE_NOT_EXIST) + ":" + request.getDropNodeId());
        }

        DropNode previousNode;
        DropNode nextNode;

        if (request.getDropPosition() == 1) {
            //dropPosition=1: 放到dropNode节点后，原dropNode后面的节点之前
            previousNode = dropNode;
            NodeSortQueryParam sortParam = new NodeSortQueryParam();
            sortParam.setPos(previousNode.getPos());
            sortParam.setOperator(MOVE_POS_OPERATOR_MORE);
            sortParam.setParentId(sortRangeId);
            nextNode = selectPosNodeFunc.apply(sortParam);
        } else if (request.getDropPosition() == -1) {
            //dropPosition=-1: 放到dropNode节点前，原dropNode前面的节点之后
            nextNode = dropNode;
            NodeSortQueryParam sortParam = new NodeSortQueryParam();
            sortParam.setPos(nextNode.getPos());
            sortParam.setOperator(MOVE_POS_OPERATOR_LESS);
            sortParam.setParentId(sortRangeId);
            previousNode = selectPosNodeFunc.apply(sortParam);
        } else {
            throw new MSException(Translator.get("invalid_parameter") + ": dropPosition");
        }

        return new MoveNodeSortDTO(sortRangeId, dragNode, previousNode, nextNode);
    }

    //判断是否存在需要提前排序的异常数据
    public boolean needRefreshBeforeSort(DropNode previousNode, DropNode nextNode) {
        long previousPos = previousNode == null ? -1 : previousNode.getPos();
        long nextPos = nextNode == null ? -1 : nextNode.getPos();
        return nextPos - previousPos <= 1 || previousPos == 0 || nextPos == 0;
    }
    //排序
    public void sort(MoveNodeSortDTO sortDTO) {
        // 获取相邻节点
        DropNode previousNode = sortDTO.getPreviousNode();
        DropNode nextNode = sortDTO.getNextNode();
        ModuleSortCountResultDTO countResultDTO = NodeSortUtils.countModuleSort(
                previousNode == null ? -1 : previousNode.getPos(),
                nextNode == null ? -1 : nextNode.getPos());
        updatePos(sortDTO.getSortNode().getId(), countResultDTO.getPos());
        if (countResultDTO.isRefreshPos()) {
            refreshPos(sortDTO.getSortRangeId());
        }
    }

}
