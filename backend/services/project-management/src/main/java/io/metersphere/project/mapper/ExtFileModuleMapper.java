package io.metersphere.project.mapper;

import io.metersphere.project.domain.FileModule;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtFileModuleMapper {
    List<BaseTreeNode> selectBaseByProjectId(String projectId);

    List<BaseTreeNode> selectIdAndParentIdByProjectId(String projectId);

    List<String> selectChildrenIdsByParentIds(@Param("ids") List<String> deleteIds);

    List<String> selectChildrenIdsSortByPos(String parentId);

    void deleteByIds(@Param("ids") List<String> deleteId);

    Long getMaxPosByParentId(String parentId);

    List<String> selectIdsByProjectId(String projectId);

    FileModule getLastModuleByParentId(String id);

    FileModule getNextModuleInParentId(@Param("parentId") String parentId, @Param("pos") long pos);

    FileModule getPreviousModuleInParentId(@Param("parentId") String parentId, @Param("pos") long pos);

    String selectNameById(String moduleId);
}
