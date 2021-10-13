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

    public List<RelationshipEdge> getBySourceIdAndRelationType(String sourceId, String relationType) {
        RelationshipEdgeExample example = new RelationshipEdgeExample();
        example.createCriteria()
                .andSourceIdEqualTo(sourceId)
                .andRelationshipTypeEqualTo(relationType);
        return relationshipEdgeMapper.selectByExample(example);
    }

    public void saveBatch(RelationshipEdgeRequest request) {
        if (CollectionUtils.isEmpty(request.getTargetIds())) {
            return;
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        RelationshipEdgeMapper batchMapper = sqlSession.getMapper(RelationshipEdgeMapper.class);
        String graphId = UUID.randomUUID().toString();

        // 判断这些顶点是否已经和其他顶点连通
        // 连通的话，加到同一个图中，否则新建一个图，即 graphId
        List<String> graphNodes = new ArrayList<>();
        graphNodes.add(request.getSourceId());
        graphNodes.addAll(request.getTargetIds());
        RelationshipEdgeExample example = new RelationshipEdgeExample();
        example.createCriteria()
                .andSourceIdIn(graphNodes)
                .andRelationshipTypeEqualTo(request.getRelationshipType());
        example.or(
                example.createCriteria()
                        .andTargetIdIn(graphNodes)
                        .andRelationshipTypeEqualTo(request.getRelationshipType())
        );
        List<RelationshipEdge> relationshipEdges = relationshipEdgeMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(relationshipEdges)) {
            graphId = relationshipEdges.get(0).getGraphId();
        }

        // todo 检验是否有环

        for (String targetId : request.getTargetIds()) {
            RelationshipEdge edge = new  RelationshipEdge();
            edge.setSourceId(request.getSourceId());
            edge.setTargetId(targetId);
            edge.setGraphId(graphId);
            edge.setRelationshipType(request.getRelationshipType());
            edge.setResourceType(request.getResourceType());
            edge.setCreator(SessionUtils.getUserId());
            edge.setCreateTime(System.currentTimeMillis());
            batchMapper.insert(edge);
        }

        sqlSession.flushStatements();
    }

}
