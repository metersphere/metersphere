package io.metersphere.system.utils;


import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.RelationshipEdgeDTO;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RelationshipEdgeUtils {

    public static void checkEdge(List<RelationshipEdgeDTO> edgeDTOS) {
        HashSet<String> nodeIds = new HashSet<>();
        nodeIds.addAll(edgeDTOS.stream().map(RelationshipEdgeDTO::getSourceId).collect(Collectors.toSet()));
        nodeIds.addAll(edgeDTOS.stream().map(RelationshipEdgeDTO::getTargetId).collect(Collectors.toSet()));
        // 判断是否有环
        HashSet<String> visitedSet = new HashSet<>();
        nodeIds.forEach(nodeId -> {
            if (!visitedSet.contains(nodeId) && directedCycle(nodeId, edgeDTOS, new HashSet<>(), visitedSet)) {
                throw new MSException(Translator.get("cycle_relationship"));
            }
        });
    }

    /**
     * 给定一点，深度搜索该连通图中是否存在环
     *
     * @param id
     * @param edges
     * @param markSet    标记该路径上经过的节点
     * @param visitedSet 标记访问过的节点
     * @return
     */
    public static boolean directedCycle(String id, List<RelationshipEdgeDTO> edges, Set<String> markSet, Set<String> visitedSet) {

        if (markSet.contains(id)) {
            // 如果已经访问过该节点，则说明存在环
            return true;
        }

        markSet.add(id);
        visitedSet.add(id);

        ArrayList<String> nextLevelNodes = new ArrayList();
        for (RelationshipEdgeDTO relationshipEdge : edges) {
            if (id.equals(relationshipEdge.getSourceId())) {
                nextLevelNodes.add(relationshipEdge.getTargetId());
            }
        }

        for (String nextNode : nextLevelNodes) {
            if (directedCycle(nextNode, edges, markSet, visitedSet)) {
                return true;
            }
        }

        // 关键，递归完这一条路径要把这个标记去掉，否则会误判为有环
        // 比如 1->3, 1->2->3 , 3 经过多次但是无环
        markSet.remove(id);

        return false;
    }


    public static void updateGraphId(String id, Function<String, RelationshipEdgeDTO> getGraphIdFunc, Function<String, List<RelationshipEdgeDTO>> getEdgeByGraphIdFunc, BiConsumer<List, String> updateFunc) {
        RelationshipEdgeDTO edge = getGraphIdFunc.apply(id);
        if (edge == null) {
            throw new MSException(Translator.get("relationship_not_exist"));
        }
        List<RelationshipEdgeDTO> edges = getEdgeByGraphIdFunc.apply(edge.getGraphId());

        // 去掉要删除的边
        edges = edges.stream()
                .filter(i -> !i.getSourceId().equals(edge.getSourceId()) && !i.getTargetId().equals(edge.getTargetId()))
                .collect(Collectors.toList());

        Set<String> nodes = new HashSet<>();
        Set<String> markSet = new HashSet<>();
        nodes.addAll(edges.stream().map(RelationshipEdgeDTO::getSourceId).collect(Collectors.toSet()));
        nodes.addAll(edges.stream().map(RelationshipEdgeDTO::getTargetId).collect(Collectors.toSet()));

        dfsForMark(edge.getSourceId(), edges, markSet, true);
        dfsForMark(edge.getSourceId(), edges, markSet, false);

        // 如果连通的点减少，说明形成了两个不连通子图，重新设置graphId
        if (markSet.size() != nodes.size()) {
            List<String> updateIds = new ArrayList<>(markSet);
            updateFunc.accept(updateIds, UUID.randomUUID().toString());
        }
    }

    /**
     * 遍历标记经过的节点
     *
     * @param node
     * @param edges
     * @param markSet
     * @param isForwardDirection
     */
    public static void dfsForMark(String node, List<RelationshipEdgeDTO> edges, Set<String> markSet, boolean isForwardDirection) {
        markSet.add(node);

        Set<String> nextLevelNodes = new HashSet<>();

        for (RelationshipEdgeDTO edge : edges) {
            if (isForwardDirection) {
                if (node.equals(edge.getSourceId())) {
                    nextLevelNodes.add(edge.getTargetId());
                }
            } else {
                if (node.equals(edge.getTargetId())) {
                    nextLevelNodes.add(edge.getSourceId());
                }
            }
        }

        nextLevelNodes.forEach(nextNode -> {
            if (!markSet.contains(nextNode)) {
                dfsForMark(nextNode, edges, markSet, true);
                dfsForMark(nextNode, edges, markSet, false);
            }
        });
    }
}
