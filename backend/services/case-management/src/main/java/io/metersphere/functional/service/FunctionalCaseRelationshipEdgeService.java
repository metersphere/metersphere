/**
 * @filename:FunctionalCaseRelationshipEdgeServiceImpl 2023年5月17日
 * @project ms  V3.x
 * Copyright(c) 2018 wx Co. Ltd.
 * All right reserved.
 */
package io.metersphere.functional.service;


import io.metersphere.functional.domain.FunctionalCaseRelationshipEdge;
import io.metersphere.functional.domain.FunctionalCaseRelationshipEdgeExample;
import io.metersphere.functional.dto.FunctionalCaseRelationshipDTO;
import io.metersphere.functional.mapper.ExtFunctionalCaseRelationshipEdgeMapper;
import io.metersphere.functional.mapper.FunctionalCaseRelationshipEdgeMapper;
import io.metersphere.functional.request.RelationshipAddRequest;
import io.metersphere.functional.request.RelationshipDeleteRequest;
import io.metersphere.functional.request.RelationshipRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.RelationshipEdgeDTO;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.RelationshipEdgeUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class FunctionalCaseRelationshipEdgeService {

    @Resource
    private FunctionalCaseRelationshipEdgeMapper functionalCaseRelationshipEdgeMapper;
    @Resource
    private ExtFunctionalCaseRelationshipEdgeMapper extFunctionalCaseRelationshipEdgeMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;


    public List<String> getExcludeIds(String id) {
        List<FunctionalCaseRelationshipEdge> relationshipEdges = getRelationshipEdges(id);
        List<String> ids = relationshipEdges.stream().map(FunctionalCaseRelationshipEdge::getTargetId).collect(Collectors.toList());
        ids.addAll(relationshipEdges.stream().map(FunctionalCaseRelationshipEdge::getSourceId).collect(Collectors.toList()));
        ids.add(id);
        List<String> list = ids.stream().distinct().toList();
        return list;
    }

    private List<FunctionalCaseRelationshipEdge> getRelationshipEdges(String id) {
        FunctionalCaseRelationshipEdgeExample example = new FunctionalCaseRelationshipEdgeExample();
        example.createCriteria()
                .andSourceIdEqualTo(id);
        example.or(
                example.createCriteria()
                        .andTargetIdEqualTo(id)
        );
        return functionalCaseRelationshipEdgeMapper.selectByExample(example);
    }


    /**
     * 添加前后置用例
     *
     * @param request request
     * @param ids     ids
     */
    public void add(RelationshipAddRequest request, List<String> ids, String userId) {
        List<FunctionalCaseRelationshipEdge> edgeList = getExistRelationshipEdges(ids, request.getId());
        Set<String> addEdgesIds = new HashSet<>();
        String graphId = IDGenerator.nextStr();
        switch (request.getType()) {
            case "PRE":
                addPre(request, ids, userId, edgeList, addEdgesIds, graphId);
                break;
            case "POST":
                addPost(request, ids, userId, edgeList, addEdgesIds, graphId);
                break;
            default:
                break;
        }
        List<RelationshipEdgeDTO> edgeDTOS = edgeList.stream().map(column -> new RelationshipEdgeDTO(column.getSourceId(), column.getTargetId())).collect(Collectors.toList());
        RelationshipEdgeUtils.checkEdge(edgeDTOS);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseRelationshipEdgeMapper batchMapper = sqlSession.getMapper(FunctionalCaseRelationshipEdgeMapper.class);
        edgeList.forEach(item -> {
            if (addEdgesIds.contains(item.getSourceId() + item.getTargetId())) {
                if (batchMapper.selectByPrimaryKey(item.getId()) == null) {
                    batchMapper.insert(item);
                }
            } else {
                item.setGraphId(graphId); // 把原来图的id设置成合并后新的图的id
                batchMapper.updateByPrimaryKey(item);
            }
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);

    }

    /**
     * 添加后置用例
     *
     * @param request
     * @param ids
     * @param userId
     * @param edgeList
     * @param addEdgesIds
     * @param graphId
     */
    private void addPost(RelationshipAddRequest request, List<String> ids, String userId, List<FunctionalCaseRelationshipEdge> edgeList, Set<String> addEdgesIds, String graphId) {
        ids.forEach(sourceId -> {
            FunctionalCaseRelationshipEdge edge = createRelationshipEdge(sourceId, request.getId(), graphId, userId);
            edgeList.add(edge);
            addEdgesIds.add(edge.getSourceId() + edge.getTargetId());
        });
    }


    /**
     * 获取已经存在的依赖关系图
     *
     * @param ids
     * @param id
     * @return
     */
    private List<FunctionalCaseRelationshipEdge> getExistRelationshipEdges(List<String> ids, String id) {
        List<String> graphNodes = new ArrayList<>();
        graphNodes.add(id);
        graphNodes.addAll(ids);
        List<String> graphIds = extFunctionalCaseRelationshipEdgeMapper.getGraphIds(graphNodes);
        if (CollectionUtils.isEmpty(graphIds)) {
            return new ArrayList<>();
        }
        FunctionalCaseRelationshipEdgeExample example = new FunctionalCaseRelationshipEdgeExample();
        example.createCriteria()
                .andGraphIdIn(graphIds);

        return functionalCaseRelationshipEdgeMapper.selectByExample(example);
    }


    /**
     * 添加前置用例
     *
     * @param request request
     * @param ids     ids
     */
    private void addPre(RelationshipAddRequest request, List<String> ids, String userId, List<FunctionalCaseRelationshipEdge> edgeList, Set<String> addEdgesIds, String graphId) {
        ids.forEach(targetId -> {
            FunctionalCaseRelationshipEdge edge = createRelationshipEdge(request.getId(), targetId, graphId, userId);
            edgeList.add(edge);
            addEdgesIds.add(edge.getSourceId() + edge.getTargetId());
        });
    }

    private FunctionalCaseRelationshipEdge createRelationshipEdge(String sourceId, String targetId, String graphId, String userId) {
        FunctionalCaseRelationshipEdge edge = new FunctionalCaseRelationshipEdge();
        edge.setId(IDGenerator.nextStr());
        edge.setSourceId(sourceId);
        edge.setTargetId(targetId);
        edge.setGraphId(graphId);
        edge.setCreateUser(userId);
        edge.setCreateTime(System.currentTimeMillis());
        edge.setUpdateTime(System.currentTimeMillis());
        return edge;
    }

    public List<FunctionalCaseRelationshipDTO> getFunctionalCasePage(RelationshipRequest request) {
        List<FunctionalCaseRelationshipDTO> list = extFunctionalCaseRelationshipEdgeMapper.list(request, request.getSortString());
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list;
    }


    public void delete(RelationshipDeleteRequest request) {
        checkResource(request);
        RelationshipEdgeUtils.updateGraphId(request.getId(), extFunctionalCaseRelationshipEdgeMapper::getGraphId, extFunctionalCaseRelationshipEdgeMapper::getEdgeByGraphId, extFunctionalCaseRelationshipEdgeMapper::update);
        functionalCaseRelationshipEdgeMapper.deleteByPrimaryKey(request.getId());
    }

    private void checkResource(RelationshipDeleteRequest request) {
        FunctionalCaseRelationshipEdgeExample example = new FunctionalCaseRelationshipEdgeExample();
        FunctionalCaseRelationshipEdgeExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(request.getId());
        if(StringUtils.equalsIgnoreCase(request.getType(), "PRE")){
            criteria.andTargetIdEqualTo(request.getCaseId());
        }else {
            criteria.andSourceIdEqualTo(request.getCaseId());
        }

        if(functionalCaseRelationshipEdgeMapper.countByExample(example) == 0){
            throw new MSException(Translator.get("resource_not_exist"));
        }
    }


}