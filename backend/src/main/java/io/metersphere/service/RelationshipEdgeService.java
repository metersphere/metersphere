package io.metersphere.service;


import io.metersphere.base.domain.RelationshipEdge;
import io.metersphere.base.domain.RelationshipEdgeExample;
import io.metersphere.base.mapper.RelationshipEdgeMapper;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.RelationshipEdgeRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author jianxingChen
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class RelationshipEdgeService {

    @Resource
    private RelationshipEdgeMapper relationshipEdgeMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public void delete(String sourceId, String targetId) {
        RelationshipEdgeExample example = new RelationshipEdgeExample();
        example.createCriteria()
                .andSourceIdEqualTo(sourceId)
                .andTargetIdEqualTo(targetId);
        relationshipEdgeMapper.deleteByExample(example);
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

    public void saveBatch(RelationshipEdgeRequest request) {

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        RelationshipEdgeMapper batchMapper = sqlSession.getMapper(RelationshipEdgeMapper.class);

        String graphId = getGraphId(request);

        // todo 检验是否有环

        if (CollectionUtils.isNotEmpty(request.getTargetIds())) {
            for (String targetId : request.getTargetIds()) {
                RelationshipEdge edge = getNewRelationshipEdge(graphId, request.getId(), targetId, request.getType());
                batchMapper.insert(edge);
            }
        }

        if (CollectionUtils.isNotEmpty(request.getSourceIds())) {
            for (String sourceId : request.getSourceIds()) {
                RelationshipEdge edge = getNewRelationshipEdge(graphId, sourceId, request.getId(), request.getType());
                batchMapper.insert(edge);
            }
        }

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
     * 获取当前节点所在的图的id
     * @param request
     * @return
     */
    private String getGraphId(RelationshipEdgeRequest request) {
        // 判断这些顶点是否已经和其他顶点连通
        // 连通的话，加到同一个图中，否则新建一个图，即 graphId
        String graphId = UUID.randomUUID().toString();

        List<String> graphNodes = new ArrayList<>();
        graphNodes.add(request.getId());
        if (request.getTargetIds() != null) {
            graphNodes.addAll(request.getTargetIds());
        }
        if (request.getSourceIds() != null) {
            graphNodes.addAll(request.getSourceIds());
        }
        RelationshipEdgeExample example = new RelationshipEdgeExample();
        example.createCriteria()
                .andSourceIdIn(graphNodes);
        example.or(
                example.createCriteria()
                        .andTargetIdIn(graphNodes)
        );
        List<RelationshipEdge> relationshipEdges = relationshipEdgeMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(relationshipEdges)) {
            return relationshipEdges.get(0).getGraphId();
        }
        return graphId;
    }

}
