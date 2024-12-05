package io.metersphere.project.service;

import io.metersphere.project.dto.ModuleCountDTO;
import io.metersphere.project.dto.ModuleSortCountResultDTO;
import io.metersphere.project.dto.NodeSortDTO;
import io.metersphere.project.dto.NodeSortQueryParam;
import io.metersphere.project.utils.NodeSortUtils;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.BaseModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ModuleTreeService {

    protected static final long LIMIT_POS = NodeSortUtils.DEFAULT_NODE_INTERVAL_POS;

    public BaseTreeNode getDefaultModule(String name) {
        //默认模块下不允许创建子模块。  它本身也就是叶子节点。
        return new BaseTreeNode(ModuleConstants.DEFAULT_NODE_ID, name, ModuleConstants.NODE_TYPE_DEFAULT, ModuleConstants.ROOT_NODE_PARENT_ID);
    }

    //构建树结构，并为每个节点计算资源数量
    public List<BaseTreeNode> buildTreeAndCountResource(List<BaseTreeNode> traverseList, @NotNull List<ModuleCountDTO> moduleCountDTOList, boolean haveVirtualRootNode, String virtualRootName) {
        //构建模块树
        List<BaseTreeNode> baseTreeNodeList = this.buildTreeAndCountResource(traverseList, haveVirtualRootNode, virtualRootName);
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
    public List<BaseTreeNode> buildTreeAndCountResource(List<BaseTreeNode> traverseList, boolean haveVirtualRootNode, String virtualRootName) {

        List<BaseTreeNode> baseTreeNodeList = new ArrayList<>();
        if (haveVirtualRootNode) {
            BaseTreeNode defaultNode = this.getDefaultModule(virtualRootName);
            defaultNode.genModulePath(null);
            baseTreeNodeList.add(defaultNode);
        }
        int lastSize = 0;
        Map<String, BaseTreeNode> baseTreeNodeMap = new HashMap<>();

        // 根节点预先处理
        baseTreeNodeList.addAll(traverseList.stream().filter(treeNode -> StringUtils.equalsIgnoreCase(treeNode.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)).toList());
        baseTreeNodeList.forEach(item -> baseTreeNodeMap.put(item.getId(), item));
        traverseList = (List<BaseTreeNode>) CollectionUtils.removeAll(traverseList, baseTreeNodeList);

        // 循环处理子节点
        while (CollectionUtils.isNotEmpty(traverseList) && traverseList.size() != lastSize) {
            lastSize = traverseList.size();
            List<BaseTreeNode> notMatchedList = new ArrayList<>();
            for (BaseTreeNode treeNode : traverseList) {
                if (!baseTreeNodeMap.containsKey(treeNode.getParentId()) && !StringUtils.equalsIgnoreCase(treeNode.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                    notMatchedList.add(treeNode);
                    continue;
                }
                BaseTreeNode node = new BaseTreeNode(treeNode.getId(), treeNode.getName(), treeNode.getType(), treeNode.getParentId());
                node.genModulePath(baseTreeNodeMap.get(treeNode.getParentId()));
                baseTreeNodeMap.put(treeNode.getId(), node);

                if (StringUtils.equalsIgnoreCase(treeNode.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                    baseTreeNodeList.add(node);
                } else if (baseTreeNodeMap.containsKey(treeNode.getParentId())) {
                    baseTreeNodeMap.get(treeNode.getParentId()).addChild(node);
                }
            }
            traverseList = notMatchedList;
        }
        return baseTreeNodeList;
    }

    private static final String MOVE_POS_OPERATOR_LESS = "lessThan";
    private static final String MOVE_POS_OPERATOR_MORE = "moreThan";
    private static final String MOVE_POS_OPERATOR_LATEST = "latest";
    private static final String DRAG_NODE_NOT_EXIST = "drag_node.not.exist";


    /**
     * 构建节点排序的参数
     *
     * @param request           拖拽的前端请求参数
     * @param selectIdNodeFunc  通过id查询节点的函数
     * @param selectPosNodeFunc 通过parentId和pos运算符查询节点的函数
     * @return
     */
    public NodeSortDTO getNodeSortDTO(NodeMoveRequest request, Function<String, BaseModule> selectIdNodeFunc, Function<NodeSortQueryParam, BaseModule> selectPosNodeFunc) {
        if (StringUtils.equals(request.getDragNodeId(), request.getDropNodeId())) {
            //两种节点不能一样
            throw new MSException(Translator.get("invalid_parameter") + ": drag node  and drop node");
        }
        
        BaseModule dragNode = selectIdNodeFunc.apply(request.getDragNodeId());
        if (dragNode == null) {
            throw new MSException(Translator.get(DRAG_NODE_NOT_EXIST) + ":" + request.getDragNodeId());
        }

        BaseModule dropNode = selectIdNodeFunc.apply(request.getDropNodeId());
        if (dropNode == null) {
            throw new MSException(Translator.get(DRAG_NODE_NOT_EXIST) + ":" + request.getDropNodeId());

        }
        BaseModule parentModule;
        BaseModule previousNode;
        BaseModule nextNode = null;
        if (request.getDropPosition() == 0) {
            //dropPosition=0: 放到dropNode节点内，最后一个节点之后
            parentModule = new BaseModule(dropNode.getId(), dropNode.getName(), dropNode.getPos(), dropNode.getProjectId(), dropNode.getParentId());

            NodeSortQueryParam sortParam = new NodeSortQueryParam();
            sortParam.setParentId(dropNode.getId());
            sortParam.setOperator(MOVE_POS_OPERATOR_LATEST);
            previousNode = selectPosNodeFunc.apply(sortParam);
        } else {
            if (StringUtils.equalsIgnoreCase(dropNode.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                parentModule = new BaseModule(ModuleConstants.ROOT_NODE_PARENT_ID, ModuleConstants.ROOT_NODE_PARENT_ID, 0, dragNode.getProjectId(), ModuleConstants.ROOT_NODE_PARENT_ID);
            } else {
                parentModule = selectIdNodeFunc.apply(dropNode.getParentId());
            }
            if (request.getDropPosition() == 1) {
                //dropPosition=1: 放到dropNode节点后，原dropNode后面的节点之前
                previousNode = dropNode;

                NodeSortQueryParam sortParam = new NodeSortQueryParam();
                sortParam.setParentId(parentModule.getId());
                sortParam.setPos(previousNode.getPos());
                sortParam.setOperator(MOVE_POS_OPERATOR_MORE);
                nextNode = selectPosNodeFunc.apply(sortParam);
            } else if (request.getDropPosition() == -1) {
                //dropPosition=-1: 放到dropNode节点前，原dropNode前面的节点之后
                nextNode = dropNode;

                NodeSortQueryParam sortParam = new NodeSortQueryParam();
                sortParam.setParentId(parentModule.getId());
                sortParam.setPos(nextNode.getPos());
                sortParam.setOperator(MOVE_POS_OPERATOR_LESS);
                previousNode = selectPosNodeFunc.apply(sortParam);
            } else {
                throw new MSException(Translator.get("invalid_parameter") + ": dropPosition");
            }
        }

        return new NodeSortDTO(dragNode, parentModule, previousNode, nextNode);
    }

    /**
     * 模块树排序
     */
    public void sort(@Validated NodeSortDTO nodeMoveDTO) {
        // 获取相邻节点
        BaseModule previousNode = nodeMoveDTO.getPreviousNode();
        BaseModule nextNode = nodeMoveDTO.getNextNode();

        ModuleSortCountResultDTO countResultDTO = NodeSortUtils.countModuleSort(
                previousNode == null ? -1 : previousNode.getPos(),
                nextNode == null ? -1 : nextNode.getPos());
        updatePos(nodeMoveDTO.getNode().getId(), countResultDTO.getPos());
        if (countResultDTO.isRefreshPos()) {
            refreshPos(nodeMoveDTO.getParent().getId());
        }
    }

    public abstract void updatePos(String id, long pos);

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

    public Map<String, Long> getIdCountMapByBreadth(List<BaseTreeNode> treeNodeList) {
        Map<String, Long> returnMap = new HashMap<>();
        List<BaseTreeNode> whileList = new ArrayList<>(treeNodeList);
        while (CollectionUtils.isNotEmpty(whileList)) {
            List<BaseTreeNode> childList = new ArrayList<>();
            for (BaseTreeNode treeNode : whileList) {
                returnMap.put(treeNode.getId(), treeNode.getCount());
                childList.addAll(treeNode.getChildren());
            }
            whileList = childList;
        }
        return returnMap;
    }
}
