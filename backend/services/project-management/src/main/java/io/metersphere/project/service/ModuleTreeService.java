package io.metersphere.project.service;

import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.dto.BaseModule;
import io.metersphere.sdk.dto.BaseTreeNode;
import io.metersphere.sdk.dto.request.NodeMoveRequest;
import io.metersphere.sdk.util.Translator;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ModuleTreeService {

    protected static final int LIMIT_POS = 64;

    public BaseTreeNode getDefaultModule() {
        //默认模块下不允许创建子模块。  它本身也就是叶子节点。
        return new BaseTreeNode(ModuleConstants.DEFAULT_NODE_ID, Translator.get("default.module"), ModuleConstants.NODE_TYPE_DEFAULT);
    }


    public List<BaseTreeNode> traverseToBuildTree(List<BaseTreeNode> traverseList, @NotNull List<ModuleCountDTO> moduleCountDTOList, boolean haveVirtualRootNode) {
        List<BaseTreeNode> baseTreeNodeList = this.traverseToBuildTree(traverseList, haveVirtualRootNode);
        Map<String, Integer> resourceCountMap = moduleCountDTOList.stream().collect(Collectors.toMap(ModuleCountDTO::getModuleId, ModuleCountDTO::getDataCount));
        this.sumModuleResourceCount(baseTreeNodeList, resourceCountMap);
        return baseTreeNodeList;
    }

    /**
     * 遍历构建节点树
     *
     * @param traverseList        要遍历的节点集合（会被清空）
     * @param haveVirtualRootNode 是否包含虚拟跟节点
     * @return
     */
    public List<BaseTreeNode> traverseToBuildTree(List<BaseTreeNode> traverseList, boolean haveVirtualRootNode) {
        List<BaseTreeNode> baseTreeNodeList = new ArrayList<>();
        if (haveVirtualRootNode) {
            BaseTreeNode defaultNode = this.getDefaultModule();
            baseTreeNodeList.add(defaultNode);
        }
        int lastSize = 0;
        Map<String, BaseTreeNode> baseTreeNodeMap = new HashMap<>();
        while (CollectionUtils.isNotEmpty(traverseList) && traverseList.size() != lastSize) {
            List<BaseTreeNode> notMatchedList = new ArrayList<>();
            for (BaseTreeNode treeNode : traverseList) {
                if (StringUtils.equals(treeNode.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                    BaseTreeNode node = new BaseTreeNode(treeNode.getId(), treeNode.getName(), treeNode.getType(), treeNode.getParentId());
                    baseTreeNodeList.add(node);
                    baseTreeNodeMap.put(treeNode.getId(), node);
                } else {
                    if (baseTreeNodeMap.containsKey(treeNode.getParentId())) {
                        BaseTreeNode node = new BaseTreeNode(treeNode.getId(), treeNode.getName(), treeNode.getType(), treeNode.getParentId());
                        baseTreeNodeMap.get(treeNode.getParentId()).addChild(node);
                        baseTreeNodeMap.put(treeNode.getId(), node);
                    } else {
                        notMatchedList.add(treeNode);
                    }
                }
            }
            traverseList = notMatchedList;
        }
        //剩余的节点没有匹配上，直接放到根节点下
        traverseList.forEach(treeNode -> {
            BaseTreeNode node = new BaseTreeNode(treeNode.getId(), treeNode.getName(), ModuleConstants.NODE_TYPE_DEFAULT, treeNode.getParentId());
            baseTreeNodeMap.get(treeNode.getParentId()).addChild(node);
        });
        return baseTreeNodeList;
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

    /**
     * 通过深度遍历的方式，将资源数量复制到节点上，并将子节点上的资源数量统计到父节点上
     *
     * @param baseTreeNodeList
     * @param resourceCountMap
     */
    private void sumModuleResourceCount(List<BaseTreeNode> baseTreeNodeList, Map<String, Integer> resourceCountMap) {
        for (BaseTreeNode node : baseTreeNodeList) {
            //赋值子节点的资源数量
            this.sumModuleResourceCount(node.getChildren(), resourceCountMap);
            //当前节点的资源数量（包含子节点）
            int childResourceCount = 0;
            for (BaseTreeNode childNode : node.getChildren()) {
                childResourceCount += childNode.getCount();
            }

            if (resourceCountMap.containsKey(node.getId())) {
                node.setCount(childResourceCount + resourceCountMap.get(node.getId()));
            } else {
                node.setCount(childResourceCount);
            }

        }
    }

    public long getAllCount(List<ModuleCountDTO> moduleCountDTOList) {
        long count = 0;
        for (ModuleCountDTO countDTO : moduleCountDTOList) {
            count += countDTO.getDataCount();
        }
        return count;
    }
}
