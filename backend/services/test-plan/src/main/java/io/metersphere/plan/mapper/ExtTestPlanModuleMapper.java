package io.metersphere.plan.mapper;

import io.metersphere.project.dto.NodeSortQueryParam;
import io.metersphere.system.dto.sdk.BaseModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtTestPlanModuleMapper {
    List<BaseTreeNode> selectBaseByProjectId(String projectId);

    List<BaseTreeNode> selectIdAndParentIdByProjectId(String projectId);

    Long getMaxPosByParentId(String parentId);

    List<String> selectChildrenIdsByParentIds(@Param("ids") List<String> strings);

    List<String> selectPlanIdsByModuleIds(@Param("ids") List<String> strings);

    void deleteByIds(@Param("ids") List<String> deleteId);

    BaseModule selectBaseModuleById(String dragNodeId);

    BaseModule selectModuleByParentIdAndPosOperator(NodeSortQueryParam nodeSortQueryParam);

    List<String> selectIdsByProjectId(String projectId);

    List<String> selectChildrenIdsSortByPos(String parentId);

    String selectNameById(String id);

    String selectProjectIdByModuleId(String id);

    List<BaseTreeNode> selectBaseByIds(@Param("ids") List<String> ids);

    List<String> selectIdByProjectIdAndTestPlanId(@Param("projectId") String projectId, @Param("testPlanId") String testPlanId);
}
