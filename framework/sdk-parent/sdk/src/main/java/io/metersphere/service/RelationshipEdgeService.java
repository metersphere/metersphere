package io.metersphere.service;


import io.metersphere.base.domain.RelationshipEdge;
import io.metersphere.base.domain.RelationshipEdgeExample;
import io.metersphere.base.domain.RelationshipEdgeKey;
import io.metersphere.base.mapper.RelationshipEdgeMapper;
import io.metersphere.base.mapper.ext.BaseRelationshipEdgeMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.request.RelationshipEdgeRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author jianxingChen
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class RelationshipEdgeService {

    @Resource
    private RelationshipEdgeMapper relationshipEdgeMapper;
    @Resource
    private BaseRelationshipEdgeMapper baseRelationshipEdgeMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public void delete(String sourceId, String targetId) {
        RelationshipEdgeExample example = new RelationshipEdgeExample();
        example.createCriteria()
                .andSourceIdEqualTo(sourceId)
                .andTargetIdEqualTo(targetId);
        List<RelationshipEdge> list = relationshipEdgeMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(list)) {
            String graphId = relationshipEdgeMapper.selectByExample(example).get(0).getGraphId();
            updateGraphId(graphId, sourceId, targetId);
            relationshipEdgeMapper.deleteByExample(example);
        }
    }

    public void delete(String sourceId, List<String> targetIds) {
        targetIds.forEach(targetId -> {
            delete(sourceId, targetId);
        });
    }

    /**
     * 删除边后，若形成两个不连通子图，则拆分图
     *
     * @param graphId
     * @param sourceId
     * @param targetId
     */
    public void updateGraphId(String graphId, String sourceId, String targetId) {
        RelationshipEdgeExample graphExample = new RelationshipEdgeExample();
        graphExample.createCriteria()
                .andGraphIdEqualTo(graphId);
        List<RelationshipEdge> edges = relationshipEdgeMapper.selectByExample(graphExample);

        // 去掉要删除的边
        edges = edges.stream()
                .filter(i -> !i.getSourceId().equals(sourceId) && !i.getTargetId().equals(targetId))
                .collect(Collectors.toList());

        Set<String> nodes = new HashSet<>();
        Set<String> markSet = new HashSet<>();
        nodes.addAll(edges.stream().map(RelationshipEdgeKey::getSourceId).collect(Collectors.toSet()));
        nodes.addAll(edges.stream().map(RelationshipEdgeKey::getTargetId).collect(Collectors.toSet()));

        dfsForMark(sourceId, edges, markSet, true);
        dfsForMark(sourceId, edges, markSet, false);

        // 如果连通的点减少，说明形成了两个不连通子图，重新设置graphId
        if (markSet.size() != nodes.size()) {
            List<String> updateIds = new ArrayList<>(markSet);
            RelationshipEdgeExample updateGraphExample = new RelationshipEdgeExample();
            updateGraphExample.createCriteria()
                    .andSourceIdIn(updateIds);
            updateGraphExample.or(
                    updateGraphExample.createCriteria().andTargetIdIn(updateIds)
            );
            RelationshipEdge edge = new RelationshipEdge();
            edge.setGraphId(UUID.randomUUID().toString());
            relationshipEdgeMapper.updateByExampleSelective(edge, updateGraphExample);
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
    public void dfsForMark(String node, List<RelationshipEdge> edges, Set<String> markSet, boolean isForwardDirection) {
        markSet.add(node);

        Set<String> nextLevelNodes = new HashSet<>();

        for (RelationshipEdge edge : edges) {
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

    public void delete(String sourceIdOrTargetId) {
        RelationshipEdgeExample example = new RelationshipEdgeExample();
        example.createCriteria()
                .andSourceIdEqualTo(sourceIdOrTargetId);
        example.or(example.createCriteria()
                .andTargetIdEqualTo(sourceIdOrTargetId));
        relationshipEdgeMapper.deleteByExample(example);
    }

    public void delete(List<String> sourceIdOrTargetIds) {
        RelationshipEdgeExample example = new RelationshipEdgeExample();
        example.createCriteria()
                .andSourceIdIn(sourceIdOrTargetIds);
        example.or(example.createCriteria()
                .andTargetIdIn(sourceIdOrTargetIds));
        relationshipEdgeMapper.deleteByExample(example);
    }

    public List<RelationshipEdge> getRelationshipEdgeByType(String id, String relationshipType) {
        if (StringUtils.equals(relationshipType, "PRE")) {
            return getBySourceId(id);
        } else if (StringUtils.equals(relationshipType, "POST")) {
            return getByTargetId(id);
        }
        return new ArrayList<>();
    }

    public List<String> getRelationIdsByType(String relationshipType, List<RelationshipEdge> relationshipEdges) {
        if (StringUtils.equals(relationshipType, "PRE")) {
            return relationshipEdges.stream()
                    .map(RelationshipEdge::getTargetId)
                    .collect(Collectors.toList());
        } else if (StringUtils.equals(relationshipType, "POST")) {
            return relationshipEdges.stream()
                    .map(RelationshipEdge::getSourceId)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public List<RelationshipEdge> getBySourceId(String sourceId) {
        RelationshipEdgeExample example = new RelationshipEdgeExample();
        example.createCriteria()
                .andSourceIdEqualTo(sourceId);
        return relationshipEdgeMapper.selectByExample(example);
    }

    public List<RelationshipEdge> getByTargetId(String targetId) {
        RelationshipEdgeExample example = new RelationshipEdgeExample();
        example.createCriteria()
                .andTargetIdEqualTo(targetId);
        return relationshipEdgeMapper.selectByExample(example);
    }

    public List<RelationshipEdge> getBySourceIdOrTargetId(String id) {
        RelationshipEdgeExample example = new RelationshipEdgeExample();
        example.createCriteria()
                .andSourceIdEqualTo(id);
        example.or(
                example.createCriteria()
                        .andTargetIdEqualTo(id)
        );
        return relationshipEdgeMapper.selectByExample(example);
    }

    /**
     * 保存新的边
     * 校验是否存在环
     * 同时将两个不连通的图合并成一个图
     *
     * @param request
     */
    public void saveBatch(RelationshipEdgeRequest request) {

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        RelationshipEdgeMapper batchMapper = sqlSession.getMapper(RelationshipEdgeMapper.class);
        BaseRelationshipEdgeMapper baseBatchMapper = sqlSession.getMapper(BaseRelationshipEdgeMapper.class);

        String graphId = UUID.randomUUID().toString();
        LogUtil.info("saveBatch graphId: " + graphId);
        List<RelationshipEdge> relationshipEdges = getEdgesBySaveRequest(request);

        LogUtil.info("saveBatch relationshipEdges: " + relationshipEdges.size());
        Set<String> addEdgesIds = new HashSet<>();

        if (CollectionUtils.isNotEmpty(request.getTargetIds())) {
            request.getTargetIds().forEach(targetId -> {
                RelationshipEdge edge = getNewRelationshipEdge(graphId, request.getId(), targetId, request.getType());
                relationshipEdges.add(edge);
                addEdgesIds.add(edge.getSourceId() + edge.getTargetId());
            });
        }

        if (CollectionUtils.isNotEmpty(request.getSourceIds())) {
            request.getSourceIds().forEach(sourceId -> {
                RelationshipEdge edge = getNewRelationshipEdge(graphId, sourceId, request.getId(), request.getType());
                relationshipEdges.add(edge);
                addEdgesIds.add(edge.getSourceId() + edge.getTargetId());
            });
        }

        HashSet<String> nodeIds = new HashSet<>();
        nodeIds.addAll(relationshipEdges.stream().map(RelationshipEdge::getSourceId).collect(Collectors.toSet()));
        nodeIds.addAll(relationshipEdges.stream().map(RelationshipEdge::getTargetId).collect(Collectors.toSet()));

        // 构建邻接表
        Map<String, List<String>> adjacencyList = new HashMap<>();
        for (RelationshipEdge edge : relationshipEdges) {
            String source = edge.getSourceId();
            String target = edge.getTargetId();
            adjacencyList.computeIfAbsent(source, k -> new ArrayList<>()).add(target);
        }

        // 环检测颜色标记
        Map<String, Integer> colors = new HashMap<>();
        for (String nodeId : nodeIds) {
            if (colors.getOrDefault(nodeId, 0) == 0) {
                if (hasCycle(nodeId, adjacencyList, colors)) {
                    MSException.throwException("关联后存在循环依赖，请检查依赖关系");
                }
            }
        }

        List<RelationshipEdge> insertBatchList = new ArrayList<>(), updateBatchList = new ArrayList<>();
        relationshipEdges.forEach(item -> {
            if (addEdgesIds.contains(item.getSourceId() + item.getTargetId())) {
                if (batchMapper.selectByPrimaryKey(item) == null) {
                    insertBatchList.add(item);
                } else {
                    item.setGraphId(graphId); // 把原来图的id设置成合并后新的图的id
                    updateBatchList.add(item);
                }
            } else {
                item.setGraphId(graphId); // 把原来图的id设置成合并后新的图的id
                updateBatchList.add(item);
            }
        });

        int batchSize = 1000;
        for (int i = 0; i < insertBatchList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, insertBatchList.size());
            List<RelationshipEdge> subList = insertBatchList.subList(i, end);
            baseBatchMapper.insertBatch(subList);
        }

        for (int i = 0; i < updateBatchList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, updateBatchList.size());
            List<RelationshipEdge> subList = updateBatchList.subList(i, end);
            Map<String, Object> params = new HashMap<>();
            params.put("graphId", graphId);
            params.put("items", subList);
            baseBatchMapper.batchUpdateGraphId(params);
        }

        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private boolean hasCycle(String start, Map<String, List<String>> adjacencyList, Map<String, Integer> colors) {
        if (colors.getOrDefault(start, 0) == 1) {
            return true; // 发现环
        }
        if (colors.getOrDefault(start, 0) == 2) {
            return false; // 已经访问过，没有环
        }
        colors.put(start, 1); // 标记为正在访问
        for (String neighbor : adjacencyList.getOrDefault(start, Collections.emptyList())) {
            if (hasCycle(neighbor, adjacencyList, colors)) {
                return true;
            }
        }
        colors.put(start, 2); // 标记为已访问
        return false;
    }

    private RelationshipEdge getNewRelationshipEdge(String graphId, String sourceId, String targetId, String type) {
        RelationshipEdge edge = new RelationshipEdge();
        edge.setCreator(SessionUtils.getUserId());
        edge.setGraphId(graphId);
        edge.setCreateTime(System.currentTimeMillis());
        edge.setSourceId(sourceId);
        edge.setTargetId(targetId);
        edge.setType(type);
        return edge;
    }

    /**
     * 查找要关联的边所在图的所有的边
     *
     * @param request
     * @return
     */
    public List<RelationshipEdge> getEdgesBySaveRequest(RelationshipEdgeRequest request) {
        List<String> graphNodes = new ArrayList<>();
        graphNodes.add(request.getId());
        if (request.getTargetIds() != null) {
            graphNodes.addAll(request.getTargetIds());
        }
        if (request.getSourceIds() != null) {
            graphNodes.addAll(request.getSourceIds());
        }

        List<String> graphIds = baseRelationshipEdgeMapper.getGraphIdsByNodeIds(graphNodes);
        if (CollectionUtils.isEmpty(graphIds)) {
            return new ArrayList<>();
        }
        RelationshipEdgeExample example = new RelationshipEdgeExample();
        example.createCriteria()
                .andGraphIdIn(graphIds);

        return relationshipEdgeMapper.selectByExample(example);
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
    public boolean directedCycle(String id, List<RelationshipEdge> edges, Set<String> markSet, Set<String> visitedSet) {

        if (markSet.contains(id)) {
            // 如果已经访问过该节点，则说明存在环
            return true;
        }

        markSet.add(id);
        visitedSet.add(id);

        ArrayList<String> nextLevelNodes = new ArrayList();
        for (RelationshipEdge relationshipEdge : edges) {
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

    /**
     * 给定一个节点获取直接关联的节点的id
     *
     * @param nodeId
     * @return
     */
    public List<String> getRelationshipIds(String nodeId) {
        List<RelationshipEdge> sourceRelationshipEdges = getBySourceIdOrTargetId(nodeId);
        List<String> ids = sourceRelationshipEdges.stream().map(RelationshipEdge::getTargetId).collect(Collectors.toList());
        ids.addAll(sourceRelationshipEdges.stream().map(RelationshipEdge::getSourceId).collect(Collectors.toList()));
        ids.add(nodeId);
        return ids;
    }

    public int getRelationshipCount(String id, Function<List<String>, Integer> countByIdsFunc) {
        List<String> ids = getRelationshipIds(id);
        ids = ids.stream().filter(i -> !i.equals(id)).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(ids)) {
            return countByIdsFunc.apply(ids);
        }
        return 0;
    }
}
