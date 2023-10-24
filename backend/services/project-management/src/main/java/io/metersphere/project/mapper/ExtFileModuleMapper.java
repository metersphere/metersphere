package io.metersphere.project.mapper;

import io.metersphere.sdk.dto.BaseTreeNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFileModuleMapper {
    List<BaseTreeNode> selectBaseByProjectId(String projectId);

    List<BaseTreeNode> selectIdAndParentIdByProjectId(String projectId);

    List<String> selectChildrenIdsByParentIds(@Param("ids") List<String> deleteIds);

    List<String> selectChildrenIdsSortByPos(String parentId);

    void deleteByIds(@Param("ids") List<String> deleteId);

    Integer getMaxPosByParentId(String parentId);

    List<String> selectIdsByProjectId(String projectId);
}
