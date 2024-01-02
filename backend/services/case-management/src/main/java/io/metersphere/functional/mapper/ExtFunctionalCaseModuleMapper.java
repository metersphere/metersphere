package io.metersphere.functional.mapper;

import io.metersphere.functional.request.AssociateCaseModuleRequest;
import io.metersphere.project.dto.NodeSortQueryParam;
import io.metersphere.system.dto.sdk.BaseModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFunctionalCaseModuleMapper {
    List<BaseTreeNode> selectBaseByProjectId(@Param("projectId")String projectId);

    List<BaseTreeNode> selectBaseByIds(@Param("ids") List<String> ids);

    List<String> selectChildrenIdsByParentIds(@Param("ids") List<String> deleteIds);

    List<String> selectChildrenIdsSortByPos(String parentId);

    Long getMaxPosByParentId(String parentId);

    BaseModule selectBaseModuleById(String dragNodeId);

    BaseModule selectModuleByParentIdAndPosOperator(NodeSortQueryParam nodeSortQueryParam);

    List<BaseTreeNode> selectIdAndParentIdByProjectId(String projectId);

    List<BaseTreeNode> selectApiCaseModuleByRequest(@Param("request") AssociateCaseModuleRequest request);


}
