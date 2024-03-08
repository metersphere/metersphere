package io.metersphere.project.service;

import io.metersphere.project.dto.DropNode;
import io.metersphere.project.dto.ModuleSortCountResultDTO;
import io.metersphere.project.dto.MoveNodeSortDTO;
import io.metersphere.project.dto.NodeSortQueryParam;
import io.metersphere.project.utils.NodeSortUtils;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.enums.MoveTypeEnum;
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

    public NodeMoveRequest getNodeMoveRequest(PosRequest posRequest) {
        NodeMoveRequest request = new NodeMoveRequest();
        request.setDragNodeId(posRequest.getMoveId());
        request.setDropNodeId(posRequest.getTargetId());
        request.setDropPosition(StringUtils.equals(MoveTypeEnum.AFTER.name(), posRequest.getMoveMode()) ? -1 : 1);
        return request;
    }

    /**
     * 构建节点排序的参数
     *
     * @param request           拖拽的前端请求参数
     * @param selectIdNodeFunc  通过id查询节点的函数
     * @param selectPosNodeFunc 通过parentId和pos运算符查询节点的函数
     * @return
     */
    public MoveNodeSortDTO getNodeSortDTO(String projectId , NodeMoveRequest request, Function<String, DropNode> selectIdNodeFunc, Function<NodeSortQueryParam, DropNode> selectPosNodeFunc) {
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
            sortParam.setParentId(projectId);
            nextNode = selectPosNodeFunc.apply(sortParam);
        } else if (request.getDropPosition() == -1) {
            //dropPosition=-1: 放到dropNode节点前，原dropNode前面的节点之后
            nextNode = dropNode;
            NodeSortQueryParam sortParam = new NodeSortQueryParam();
            sortParam.setPos(nextNode.getPos());
            sortParam.setOperator(MOVE_POS_OPERATOR_LESS);
            sortParam.setParentId(projectId);
            previousNode = selectPosNodeFunc.apply(sortParam);
        } else {
            throw new MSException(Translator.get("invalid_parameter") + ": dropPosition");
        }

        return new MoveNodeSortDTO(projectId, dragNode, previousNode, nextNode);
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
            refreshPos(sortDTO.getProjectId());
        }
    }

}
