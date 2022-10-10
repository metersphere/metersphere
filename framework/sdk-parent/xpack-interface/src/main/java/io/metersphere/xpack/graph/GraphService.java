package io.metersphere.xpack.graph;

import io.metersphere.dto.RelationshipGraphData;
import io.metersphere.request.BaseQueryRequest;
import io.metersphere.xpack.graph.request.GraphBatchRequest;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public interface GraphService {

    /**
     * 生成关系图
     * @param centerNodeIds 选中的节点ID数组
     * @param findNodesFunc 根据节点ID数组查找对应节点信息的方法
     * @return
     */
    RelationshipGraphData getGraphData(List<String> centerNodeIds,
                                       Function<Set<String>,
                                       List<RelationshipGraphData.Node>> findNodesFunc);

    /**
     * 生成关系图
     * @param centerNodeId 选中的节点ID
     * @param findNodesFunc 根据节点ID数组查找对应节点信息的方法
     * @return
     */
    RelationshipGraphData getGraphData(String centerNodeId,
                                       Function<Set<String>,
                                       List<RelationshipGraphData.Node>> findNodesFunc);

    /**
     * 列表勾选-生成关系图
     * @param request 列表的查询条件
     * @param getIdsByBaseQueryFunc 根据查询条件查询ID的方法
     * @param findNodesFunc 根据节点ID数组查找对应节点信息的方法
     * @return
     */
    RelationshipGraphData getGraphDataByCondition(GraphBatchRequest request,
                                                  Function<BaseQueryRequest, List<String>> getIdsByBaseQueryFunc,
                                                  Function<Set<String>, List<RelationshipGraphData.Node>> findNodesFunc);
}
