package io.metersphere.project.service;

import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.NodeSortDTO;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.dto.BaseModule;
import io.metersphere.sdk.dto.BaseTreeNode;
import io.metersphere.sdk.util.Translator;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ModuleTreeService {

    protected static final int LIMIT_POS = 64;

    public BaseTreeNode getDefaultModule() {
        //默认模块下不允许创建子模块。  它本身也就是叶子节点。
        return new BaseTreeNode(ModuleConstants.DEFAULT_NODE_ID, Translator.get("default.module"), ModuleConstants.NODE_TYPE_DEFAULT, ModuleConstants.ROOT_NODE_PARENT_ID);
    }


    //构建树结构，并为每个节点计算资源数量
    public List<BaseTreeNode> buildTreeAndCountResource(List<BaseTreeNode> traverseList, @NotNull List<ModuleCountDTO> moduleCountDTOList, boolean haveVirtualRootNode) {
        //构建模块树
        List<BaseTreeNode> baseTreeNodeList = this.buildTreeAndCountResource(traverseList, haveVirtualRootNode);
        //构建模块节点统计的数据结构
        Map<String, Integer> resourceCountMap = moduleCountDTOList.stream().collect(Collectors.toMap(ModuleCountDTO::getModuleId, ModuleCountDTO::getDataCount));
        //为每个节点赋值资源数量
        this.sumModuleResourceCount(baseTreeNodeList, resourceCountMap);
        return baseTreeNodeList;
    }

    /**
     * 遍历构建节点树
     *
     * @param traverseList        要遍历的节点集合（会被清空）
     * @param haveVirtualRootNode 是否包含虚拟跟节点
     */
    public List<BaseTreeNode> buildTreeAndCountResource(List<BaseTreeNode> traverseList, boolean haveVirtualRootNode) {
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
    public void sort(@Validated NodeSortDTO nodeMoveDTO) {
        // 获取相邻节点
        BaseModule previousNode = nodeMoveDTO.getPreviousNode();
        BaseModule nextNode = nodeMoveDTO.getNextNode();

        if (previousNode == null && nextNode == null) {
            // 没有相邻节点，pos为0
            updatePos(nodeMoveDTO.getNode().getId(), 0);
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

            updatePos(nodeMoveDTO.getNode().getId(), pos);
            if (refreshPos) {
                refreshPos(nodeMoveDTO.getParent().getId());
            }
        }
    }

    public abstract void updatePos(String id, int pos);

    public abstract void refreshPos(String parentId);

    /**
     * 通过深度遍历的方式，在为节点赋值资源统计数量的同时，同步计算其子节点的资源数量，并添加到父节点上
     */
    private void sumModuleResourceCount(List<BaseTreeNode> baseTreeNodeList, Map<String, Integer> resourceCountMap) {
        for (BaseTreeNode node : baseTreeNodeList) {
            //赋值子节点的资源数量
            this.sumModuleResourceCount(node.getChildren(), resourceCountMap);
            //当前节点的资源数量（包含子节点）
            long childResourceCount = 0;
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
