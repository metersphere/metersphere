package io.metersphere.functional.mapper;

import io.metersphere.functional.domain.FunctionalCaseModule;
import io.metersphere.project.dto.NodeSortQueryParam;
import io.metersphere.system.dto.sdk.BaseModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFunctionalCaseModuleMapper {
    List<BaseTreeNode> selectBaseByProjectId(String projectId);

    List<BaseTreeNode> selectIdAndParentIdByProjectId(String projectId);

    List<String> selectChildrenIdsByParentIds(@Param("ids") List<String> deleteIds);

    List<String> selectChildrenIdsSortByPos(String parentId);

    void removeToTrashByIds(@Param("ids") List<String> deleteId);

    Long getMaxPosByParentId(String parentId);

    List<String> selectIdsByProjectId(String projectId);

    FunctionalCaseModule getLastModuleByParentId(String id);

    FunctionalCaseModule getNextModuleInParentId(@Param("parentId") String parentId, @Param("pos") long pos);

    FunctionalCaseModule getPreviousModuleInParentId(@Param("parentId") String parentId, @Param("pos") long pos);

    String selectNameById(String moduleId);

    BaseModule selectBaseModuleById(String dragNodeId);

    BaseModule selectModuleByParentIdAndPosOperator(NodeSortQueryParam nodeSortQueryParam);
}
