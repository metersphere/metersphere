package io.metersphere.api.mapper;

import io.metersphere.api.dto.scenario.ApiScenarioModuleRequest;
import io.metersphere.project.dto.NodeSortQueryParam;
import io.metersphere.system.dto.sdk.BaseModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtApiScenarioModuleMapper {
    List<BaseTreeNode> selectBaseByRequest(@Param("request") ApiScenarioModuleRequest request);

    List<BaseTreeNode> selectIdAndParentIdByRequest(@Param("request") ApiScenarioModuleRequest request);

    List<String> selectChildrenIdsByParentIds(@Param("ids") List<String> deleteIds);

    List<String> selectChildrenIdsSortByPos(String parentId);

    void deleteByIds(@Param("ids") List<String> deleteId);

    Long getMaxPosByParentId(String parentId);

    BaseModule selectBaseModuleById(String dragNodeId);

    BaseModule selectModuleByParentIdAndPosOperator(NodeSortQueryParam nodeSortQueryParam);

    List<BaseTreeNode> selectNodeByIds(@Param("ids") List<String> ids);

    List<BaseTreeNode> selectBaseByIds(@Param("ids") List<String> ids);

    void deleteScenarioToGc(@Param("ids") List<String> ids, @Param("userId") String userId, @Param("time") long time);

}
