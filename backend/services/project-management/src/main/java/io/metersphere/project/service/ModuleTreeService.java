package io.metersphere.project.service;

import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.dto.BaseModule;
import io.metersphere.sdk.dto.BaseTreeNode;
import io.metersphere.sdk.dto.request.NodeMoveRequest;
import io.metersphere.sdk.util.Translator;

public abstract class ModuleTreeService {

    protected static final int LIMIT_POS = 64;

    public BaseTreeNode getDefaultModule() {
        //默认模块下不允许创建子模块。  它本身也就是叶子节点。
        return new BaseTreeNode(ModuleConstants.DEFAULT_NODE_ID, Translator.get("default.module"), ModuleConstants.NODE_TYPE_DEFAULT);
    }


    /**
     * 模块树排序
     */
    public void sort(NodeMoveRequest nodeMoveRequest) {
        // 获取相邻节点
        BaseModule previousNode = getNode(nodeMoveRequest.getPreviousNodeId());
        BaseModule nextNode = getNode(nodeMoveRequest.getNextNodeId());
        if (previousNode == null && nextNode == null) {
            // 没有相邻节点，pos为0
            updatePos(nodeMoveRequest.getNodeId(), 0);
        } else {
            boolean refreshPos = false;
            int pos;
            if (nextNode == null) {
                pos = previousNode.getPos() + LIMIT_POS;
            } else if (previousNode == null) {
                pos = nextNode.getPos() / 2;
                if (pos < 2) {
                    refreshPos = true;
                }
            } else {
                int quantityDifference = (nextNode.getPos() - previousNode.getPos()) / 2;
                if (quantityDifference <= 2) {
                    refreshPos = true;
                }
                pos = previousNode.getPos() + quantityDifference;
            }

            updatePos(nodeMoveRequest.getNodeId(), pos);
            if (refreshPos) {
                refreshPos(nodeMoveRequest.getParentId());
            }
        }
    }

    public abstract BaseModule getNode(String id);

    public abstract void updatePos(String id, int pos);

    public abstract void refreshPos(String parentId);
}
