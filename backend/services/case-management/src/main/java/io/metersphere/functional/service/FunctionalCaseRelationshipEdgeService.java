/**
 * @filename:FunctionalCaseRelationshipEdgeServiceImpl 2023年5月17日
 * @project ms  V3.x
 * Copyright(c) 2018 wx Co. Ltd.
 * All right reserved.
 */
package io.metersphere.functional.service;


import io.metersphere.functional.domain.FunctionalCaseRelationshipEdge;
import io.metersphere.functional.domain.FunctionalCaseRelationshipEdgeExample;
import io.metersphere.functional.mapper.ExtFunctionalCaseRelationshipEdgeMapper;
import io.metersphere.functional.mapper.FunctionalCaseRelationshipEdgeMapper;
import io.metersphere.functional.request.RelationshipAddRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
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
        HashSet<String> nodeIds = new HashSet<>();
        nodeIds.addAll(edgeList.stream().map(FunctionalCaseRelationshipEdge::getSourceId).collect(Collectors.toSet()));
        nodeIds.addAll(edgeList.stream().map(FunctionalCaseRelationshipEdge::getTargetId).collect(Collectors.toSet()));
        // 判断是否有环
        HashSet<String> visitedSet = new HashSet<>();
        nodeIds.forEach(nodeId -> {
            if (!visitedSet.contains(nodeId) && directedCycle(nodeId, edgeList, new HashSet<>(), visitedSet)) {
                throw new MSException(Translator.get("cycle_relationship"));
            }
            ;
        });
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        FunctionalCaseRelationshipEdgeMapper batchMapper = sqlSession.getMapper(FunctionalCaseRelationshipEdgeMapper.class);

        edgeList.forEach(item -> {
            if (addEdgesIds.contains(item.getSourceId() + item.getTargetId())) {
                if (batchMapper.selectByPrimaryKey(item.getId()) == null) {
                    batchMapper.insert(item);
                } else {
                    item.setGraphId(graphId); // 把原来图的id设置成合并后新的图的id
                    batchMapper.updateByPrimaryKey(item);
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
     * 给定一点，深度搜索该连通图中是否存在环
     *
     * @param id
     * @param edges
     * @param markSet    标记该路径上经过的节点
     * @param visitedSet 标记访问过的节点
     * @return
     */
    public boolean directedCycle(String id, List<FunctionalCaseRelationshipEdge> edges, Set<String> markSet, Set<String> visitedSet) {

        if (markSet.contains(id)) {
            // 如果已经访问过该节点，则说明存在环
            return true;
        }

        markSet.add(id);
        visitedSet.add(id);

        ArrayList<String> nextLevelNodes = new ArrayList();
        for (FunctionalCaseRelationshipEdge relationshipEdge : edges) {
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
}