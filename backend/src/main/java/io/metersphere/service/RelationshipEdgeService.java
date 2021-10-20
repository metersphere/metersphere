package io.metersphere.service;


import io.metersphere.base.domain.RelationshipEdge;
import io.metersphere.base.domain.RelationshipEdgeExample;
import io.metersphere.base.mapper.RelationshipEdgeMapper;
import io.metersphere.base.mapper.ext.ExtRelationshipEdgeMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.RelationshipEdgeRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
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
    private ExtRelationshipEdgeMapper extRelationshipEdgeMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public void delete(String sourceId, String targetId) {
        RelationshipEdgeExample example = new RelationshipEdgeExample();
        example.createCriteria()
                .andSourceIdEqualTo(sourceId)
                .andTargetIdEqualTo(targetId);
        relationshipEdgeMapper.deleteByExample(example);
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
        }  else if (StringUtils.equals(relationshipType, "POST")) {
            return getByTargetId(id);
        }
        return new ArrayList<>();
    }

    public List<String> getRelationIdsByType(String relationshipType, List<RelationshipEdge> relationshipEdges) {
        if (StringUtils.equals(relationshipType, "PRE")) {
            return relationshipEdges.stream()
                    .map(RelationshipEdge::getTargetId)
                    .collect(Collectors.toList());
        }  else if (StringUtils.equals(relationshipType, "POST")) {
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

    /**
     * 保存新的边
     * 校验是否存在环
     * 同时将两个不连通的图合并成一个图
     * @param request
     */
    public void saveBatch(RelationshipEdgeRequest request) {

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        RelationshipEdgeMapper batchMapper = sqlSession.getMapper(RelationshipEdgeMapper.class);

        String graphId = UUID.randomUUID().toString();
        List<RelationshipEdge> relationshipEdges = getEdgesBySaveRequest(request);
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

        // 判断是否有环, 两个方向都搜索一遍
        if (directedCycle(request.getId(), relationshipEdges, new HashSet<>(), true) ||
                directedCycle(request.getId(), relationshipEdges, new HashSet<>(), false)) {
            MSException.throwException("关联后存在循环依赖，请检查依赖关系");
        };

        relationshipEdges.forEach(item -> {
            if (addEdgesIds.contains(item.getSourceId() + item.getTargetId())) {
                batchMapper.insert(item);
            } else {
                item.setGraphId(graphId); // 把原来图的id设置成合并后新的图的id
                batchMapper.updateByPrimaryKey(item);
            }
        });
        sqlSession.flushStatements();
    }

    private RelationshipEdge getNewRelationshipEdge(String graphId, String sourceId, String targetId, String type) {
        RelationshipEdge edge = new  RelationshipEdge();
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
     * @param request
     * @return
     */
    public List<RelationshipEdge>  getEdgesBySaveRequest(RelationshipEdgeRequest request) {
        List<String> graphNodes = new ArrayList<>();
        graphNodes.add(request.getId());
        if (request.getTargetIds() != null) {
            graphNodes.addAll(request.getTargetIds());
        }
        if (request.getSourceIds() != null) {
            graphNodes.addAll(request.getSourceIds());
        }

        List<String> graphIds = extRelationshipEdgeMapper.getGraphIdsByNodeIds(graphNodes);
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
     * @param id
     * @param edges
     * @param markSet
     * @param isForwardDirection
     * @return
     */
    public boolean directedCycle(String id, List<RelationshipEdge> edges, Set<String> markSet, Boolean isForwardDirection) {

        if (markSet.contains(id)) {
            // 如果已经访问过该节点，则说明存在环
            return true;
        }

        markSet.add(id);
        ArrayList<String> nextLevelNodes = new ArrayList();
        for (RelationshipEdge relationshipEdge : edges) {
            if (isForwardDirection) {// 正向则搜索 sourceId 是当前节点的边
                if (id.equals(relationshipEdge.getSourceId())) {
                    nextLevelNodes.add(relationshipEdge.getTargetId());
                }
            } else {
                if (id.equals(relationshipEdge.getTargetId())) {
                    nextLevelNodes.add(relationshipEdge.getSourceId());
                }
            }
        }

        for (String nextNode : nextLevelNodes) {
            if (directedCycle(nextNode, edges, markSet, isForwardDirection)) {
                return true;
            };
        }

        // 关键，递归完这一条路径要把这个标记去掉，否则会误判为有环
        // 比如 1->3, 1->2->3 , 3 经过多次但是无环
        markSet.remove(id);

        return false;
    }

    /**
     * 给定一个节点获取跟他关联的所有节点的id
     * @param nodeId
     * @return
     */
    public List<String> getRelationshipIds(String nodeId) {
        List<RelationshipEdge> sourceRelationshipEdges = getBySourceId(nodeId);
        List<RelationshipEdge> targetRelationshipEdges = getByTargetId(nodeId);
        List<String> ids = sourceRelationshipEdges.stream().map(RelationshipEdge::getTargetId).collect(Collectors.toList());
        ids.addAll(targetRelationshipEdges.stream().map(RelationshipEdge::getSourceId).collect(Collectors.toList()));
        ids.add(nodeId);
        return ids;
    }

}
