package io.metersphere.service;

import io.metersphere.base.domain.RelationshipEdge;
import io.metersphere.base.domain.RelationshipEdgeExample;
import io.metersphere.base.mapper.RelationshipEdgeMapper;
import io.metersphere.dto.RelationshipGraphData;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.request.GraphBatchRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class GraphService {

    @Resource
    RelationshipEdgeMapper relationshipEdgeMapper;

    private static Integer GRAPH_EDGES_WIDTH = 5;

    public RelationshipGraphData getGraphDataByCondition(GraphBatchRequest request,
                                                         Function<BaseQueryRequest, List<String>> getIdsByBaseQueryFunc,
                                                         Function<Set<String>, List<RelationshipGraphData.Node>> findNodesFunc) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                getIdsByBaseQueryFunc::apply);
        List<String> ids = request.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return new RelationshipGraphData();
        }
        return getGraphData(ids, findNodesFunc::apply);
    }

    /**
     * 获取图的数据
     *
     * @param centerNodeIds 给定中心节点的 id
     * @param findNodesFunc 根据id列表查询节点
     * @return
     */
    public RelationshipGraphData getGraphData(List<String> centerNodeIds, Function<Set<String>, List<RelationshipGraphData.Node>> findNodesFunc) {

        List<RelationshipGraphData.Node> nodes = new ArrayList<>();
        List<RelationshipGraphData.Edge> edges = new ArrayList<>();

        // 标记搜索过的节点，避免出现死循环
        Set<String> markSet = new HashSet<>();
        Integer xAxis = null;
        Comparator<RelationshipGraphData.Node> comparatorX = Comparator.comparing(RelationshipGraphData.Node::getX);
        Comparator<RelationshipGraphData.Node> comparatorY = Comparator.comparing(RelationshipGraphData.Node::getY);
        Set<String> centerNodeIdSet = centerNodeIds.stream().collect(Collectors.toSet());

        for (int i = 0; i < centerNodeIds.size(); i++) {
            String centerNodeId = centerNodeIds.get(i);
            if (markSet.contains(centerNodeId)) {
                continue;
            }

            // 记录当前 Y 坐标的层级中，X 坐标的最小值和最大值
            HashMap<Integer, RelationshipGraphData.XAxisMark> axisMarkMap = new HashMap<>();
            // 查询该节点所在图的所有边
            List<RelationshipEdge> relationshipEdges = getByNodeId(centerNodeId);

            Set<String> nodeIds = new HashSet<>(); // 所有顶点的id
            Set<String> sourceIds = relationshipEdges.stream().map(RelationshipEdge::getSourceId).collect(Collectors.toSet());
            Set<String> targetIds = relationshipEdges.stream().map(RelationshipEdge::getTargetId).collect(Collectors.toSet());
            nodeIds.addAll(sourceIds);
            nodeIds.addAll(targetIds);

            // 获取所有顶点
            List<RelationshipGraphData.Node> currentNodes;

            if (CollectionUtils.isEmpty(nodeIds)) {
                // 如果是孤岛的话，根据 id 查询出该节点
                currentNodes = findNodesFunc.apply(new HashSet<>(Arrays.asList(centerNodeId)));
            } else {
                // 查询当前点能遍历的所有顶点（排除了回收站的节点）
                currentNodes = findNodesFunc.apply(nodeIds);
            }

            Map<String, RelationshipGraphData.Node> nodeMap = currentNodes.stream().collect(Collectors.toMap(RelationshipGraphData.Node::getId, item -> item));

            Iterator<RelationshipEdge> iterator = relationshipEdges.iterator();
            while (iterator.hasNext()) {
                // 放进回收站的边不遍历
                RelationshipEdge next = iterator.next();
                if (nodeMap.get(next.getTargetId()) == null || nodeMap.get(next.getSourceId()) == null) {
                    iterator.remove();
                }
            }

            RelationshipGraphData.Node node = nodeMap.get(centerNodeId);

            if (node == null) {
                continue;
            }

            node.setY(0);
            node.setX(0);
            axisMarkMap.put(0, new RelationshipGraphData.XAxisMark(0, 0));

            bfsForXY(node, relationshipEdges, nodeMap, markSet, axisMarkMap, true);
            bfsForXY(node, relationshipEdges, nodeMap, markSet, axisMarkMap, false);

            // X 为空说明没有遍历到，可能是因为图中某个节点放入回收站导致中心节点访问不到其他节点
            currentNodes = currentNodes.stream().filter(item -> item.getX() != null).collect(Collectors.toList());

            if (xAxis != null) {
                Integer min = currentNodes.stream().min(comparatorX).get().getX();
                // 将这张图平移到上一张图的右侧
                for (RelationshipGraphData.Node currentNode : currentNodes) {
                    currentNode.setX(currentNode.getX() + xAxis - min + GRAPH_EDGES_WIDTH);
                }
            }

            if (CollectionUtils.isEmpty(currentNodes)) {
                continue;
            }

            // 记录当前 X 的最大值
            xAxis = currentNodes.stream().max(comparatorX).get().getX();

            int nodesSize = nodes.size();
            nodes.addAll(currentNodes);

            currentNodes.forEach(item -> {
                item.setCategory(2); // 默认其他节点为分组2
            });

            for (int j = nodesSize; j < nodes.size(); j++) {
                nodes.get(j).setIndex(j); // 设置下标
                if (centerNodeIds.contains(nodes.get(j).getId())) {
                    nodes.get(j).setCategory(0); // 中心节点为分组0
                }
            }

            // 记录与中心节点直接关联的节点id
            Set<String> directRelationshipNode = new HashSet<>();
            // 得到所有的边
            for (RelationshipEdge relationshipEdge : relationshipEdges) {
                RelationshipGraphData.Edge edge = new RelationshipGraphData.Edge();
                RelationshipGraphData.Node sourceNode = nodeMap.get(relationshipEdge.getSourceId());
                RelationshipGraphData.Node targetNode = nodeMap.get(relationshipEdge.getTargetId());
                if (sourceNode == null || targetNode == null) {
                    continue;
                }
                edge.setSource(sourceNode.getIndex());
                edge.setTarget(targetNode.getIndex());
                if (sourceNode.getX() != null && targetNode.getX() != null && Math.abs(sourceNode.getX() - targetNode.getX()) > 5 && sourceNode.getY() == targetNode.getY()) {
                    edge.setCurveness(0.2F);
                }
                edges.add(edge);
                if (centerNodeIdSet.contains(relationshipEdge.getSourceId())) {
                    directRelationshipNode.add(relationshipEdge.getTargetId());
                }
                if (centerNodeIdSet.contains(relationshipEdge.getTargetId())) {
                    directRelationshipNode.add(relationshipEdge.getSourceId());
                }
            }

            currentNodes.forEach(item -> {
                if (directRelationshipNode.contains(item.getId()) && item.getCategory() != 0) {
                    item.setCategory(1);  // 直接关联的节点为分组1
                }
            });
        }

        RelationshipGraphData relationshipGraphData = new RelationshipGraphData();
        relationshipGraphData.setLinks(edges);
        relationshipGraphData.setData(nodes);
        if (CollectionUtils.isNotEmpty(nodes)) {
            int xWith = nodes.stream().max(comparatorX).get().getX() - nodes.stream().min(comparatorX).get().getX();
            int yWith = nodes.stream().max(comparatorY).get().getY() - nodes.stream().min(comparatorY).get().getY();
            xWith = xWith == 0 ? 1 : xWith;
            yWith = yWith == 0 ? 1 : yWith;
            if (xWith / yWith > 2) {
                // 如果太扁平，沿Y轴拉伸一下
                nodes.forEach(node -> node.setY(node.getY() * 2));
            } else if (yWith / xWith > 2) {
                // 如果太窄，沿X轴拉伸一下
                nodes.forEach(node -> node.setX(node.getX() * 2));
            }
            int xUnitCount = (xWith) / GRAPH_EDGES_WIDTH + 1;
            int yUnitCount = (yWith) / GRAPH_EDGES_WIDTH + 1;
            relationshipGraphData.setXUnitCount(xUnitCount);
            relationshipGraphData.setYUnitCount(yUnitCount);
        }
        return relationshipGraphData;
    }

    public RelationshipGraphData getGraphData(String centerNode, Function<Set<String>, List<RelationshipGraphData.Node>> findNodesFunc) {
        return this.getGraphData(Arrays.asList(centerNode), findNodesFunc);
    }

    /**
     * 广度优先搜索，设置 x,y 的值
     *
     * @param node
     * @param edges
     * @param nodeMap
     * @param markSet            标记当前节点已搜索过，避免反向搜索时出现死循环
     * @param isForwardDirection 箭头方向，按不同方向遍历
     */
    private void bfsForXY(RelationshipGraphData.Node node, List<RelationshipEdge> edges, Map<String, RelationshipGraphData.Node> nodeMap, Set<String> markSet, HashMap<Integer, RelationshipGraphData.XAxisMark> axisMarkMap, Boolean isForwardDirection) {
        markSet.add(node.getId());

        List<RelationshipGraphData.Node> nextLevelNodes = new ArrayList<>();

        for (RelationshipEdge relationshipEdge : edges) {
            RelationshipGraphData.Node nextNode = null;
            if (isForwardDirection) {// 正向则搜索 sourceId 是当前节点的边
                if (node.getId().equals(relationshipEdge.getSourceId())) {
                    nextNode = nodeMap.get(relationshipEdge.getTargetId());
                }
            } else {
                if (node.getId().equals(relationshipEdge.getTargetId())) {
                    nextNode = nodeMap.get(relationshipEdge.getSourceId());
                }
            }
            if (nextNode != null) { // 节点可能在回收站
                nextLevelNodes.add(nextNode);
            }
        }

        int nextLevelNodesCount = nextLevelNodes.size();

        Integer nextY;
        if (isForwardDirection) {
            // 正向搜索，Y 轴 -1
            nextY = node.getY() - GRAPH_EDGES_WIDTH;
        } else {
            nextY = node.getY() + GRAPH_EDGES_WIDTH;
        }


        // 获取下一层级的 x 坐标轴已分配的最大，最小值
        RelationshipGraphData.XAxisMark nextLevelXAxisMark;
        nextLevelXAxisMark = axisMarkMap.get(nextY);

        for (int i = 0; i < nextLevelNodesCount; i++) {
            RelationshipGraphData.Node nextNode = nextLevelNodes.get(i);

            if (nextNode.getY() == null) {
                nextNode.setY(nextY);
            }

            if (nextLevelXAxisMark == null && nextNode.getX() == null) {
                nextLevelXAxisMark = new RelationshipGraphData.XAxisMark(node.getX(), node.getX());
                axisMarkMap.put(nextY, nextLevelXAxisMark);
                if (i == 0 && nextLevelNodesCount % 2 != 0) {
                    // 如果是奇数个，把第一个放在与当前节点 x 轴相同的位置
                    nextNode.setX(node.getX());
                }
            }

            // 没设置过才设置
            if (nextNode.getX() == null) {
                if (i % 2 != 0) { // 左边放一个，右边放一个
                    nextNode.setX(nextLevelXAxisMark.getMin() - GRAPH_EDGES_WIDTH);
                    nextLevelXAxisMark.setMin(nextLevelXAxisMark.getMin() - GRAPH_EDGES_WIDTH);
                } else {
                    nextNode.setX(nextLevelXAxisMark.getMax() + GRAPH_EDGES_WIDTH);
                    nextLevelXAxisMark.setMax(nextLevelXAxisMark.getMax() + GRAPH_EDGES_WIDTH);
                }
            }
        }

        nextLevelNodes.forEach(nextNode -> {
            if (!markSet.contains(nextNode.getId())) {
                bfsForXY(nextNode, edges, nodeMap, markSet, axisMarkMap, true);
                bfsForXY(nextNode, edges, nodeMap, markSet, axisMarkMap, false);
            }
        });
    }

    /**
     * 给定一个节点，搜索出该节点所在的图的所有边
     *
     * @param nodeId
     */
    public List<RelationshipEdge> getByNodeId(String nodeId) {
        String graphId = getGraphIdByNodeId(nodeId);
        if (StringUtils.isNotBlank(graphId)) {
            RelationshipEdgeExample example = new RelationshipEdgeExample();
            example.createCriteria().andGraphIdEqualTo(graphId);
            return relationshipEdgeMapper.selectByExample(example);
        }
        return new ArrayList<>();
    }

    public String getGraphIdByNodeId(String nodeId) {
        RelationshipEdgeExample example = new RelationshipEdgeExample();
        example.createCriteria().andSourceIdEqualTo(nodeId);
        List<RelationshipEdge> relationshipEdges = relationshipEdgeMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(relationshipEdges)) {
            example.clear();
            example.createCriteria().andTargetIdEqualTo(nodeId);
            relationshipEdges = relationshipEdgeMapper.selectByExample(example);
        }
        if (CollectionUtils.isNotEmpty(relationshipEdges)) {
            return relationshipEdges.get(0).getGraphId();
        }
        return null;
    }
}
